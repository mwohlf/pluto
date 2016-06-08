package net.wohlfart.pluto.resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.wohlfart.pluto.scene.FutureEntity;

public class Executor implements Disposable {

    protected static final Logger LOGGER = LoggerService.forClass(Executor.class);

    private static final int MAX_TIMEOUT = 5; // [s]

    private final ExecutorService executorService;

    protected final AtomicInteger openTaskCounter = new AtomicInteger();

    public Executor() {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int threads = Math.max(1, cores / 2);
        executorService = Executors.newFixedThreadPool(threads, new ExecutorThreadFactory());
        // to create the first thread so we don't have a delay later...
        executorService.execute(() -> Executor.LOGGER.info("<Executor.init> started threadpool"));
    }

    private static final class ExecutorThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(@Nonnull Runnable r) {
            final Thread thread = new Thread(r, Executor.class.getSimpleName());
            thread.setDaemon(true);
            return thread;
        }
    }

    // this method must be called on the render thread
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "using callback to get notified when the result is available")
    private void execute(final Runnable worker) {
        if (executorService.isShutdown()) {
            Executor.LOGGER.info("not accepting new tasks, scheduler is shutting down");
            return;
        }
        openTaskCounter.incrementAndGet();
        executorService.submit(worker);
    }

    public int getOpenTasks() {
        return openTaskCounter.get();
    }

    @Override
    public void dispose() {
        try {
            executorService.shutdown();
            final boolean stopped = executorService.awaitTermination(Executor.MAX_TIMEOUT, TimeUnit.SECONDS);
            if (!stopped) {
                Executor.LOGGER.error("timeout stopping BackgroundScheduler, calling shutdownNow()");
                executorService.shutdownNow();
            }
        } catch (final InterruptedException ex) {
            throw new GdxRuntimeException("Couldn't shutdown loading thread", ex);
        }
    }

    // TODO: check if this can be easier done with annotations
    public static class BackgroundWorker<I, O> implements Runnable {

        protected volatile Executor scheduler;
        protected final FutureEntity futureEntity;
        protected final I input;

        public BackgroundWorker(I input, FutureEntity futureEntity) {
            this.input = input;
            this.futureEntity = futureEntity;
        }

        void schedule(Executor scheduler) {
            this.scheduler = scheduler;
            this.scheduler.execute(this);
        }

        // implement this method to run something off the render thread
        // note that there is no OpenGL context available
        public O backgroundTask(I input) {
            return null;
        }

        // override to be called back on the render thread with the result from
        // backgroundTask()
        @SuppressWarnings("NoopMethodInAbstractClass")
        public void onSuccess(I input, O output) {
            // called in the render thread
        }

        @Override
        public void run() {
            try {
                // running in a worker thread
                final O result = backgroundTask(input);
                Gdx.app.postRunnable(() -> {
                    // success: callback in the render thread
                    callOnSuccess(result);
                });
            } catch (final Throwable ex) {
                Gdx.app.postRunnable(() -> {
                    // error: callback in the render thread
                    callOnFailure(ex);
                });
            }
        }

        private void callOnSuccess(O output) {
            try {
                onSuccess(input, output);
            } catch (final Exception ex) {
                throw new GdxRuntimeException("exception while scheduling result from background worker", ex);
            } finally {
                scheduler.openTaskCounter.decrementAndGet();
            }
        }

        private void callOnFailure(Throwable exception) {
            try {
                onFailure(exception);
            } catch (final Exception ex) {
                throw new GdxRuntimeException("exception in call to onFailure while handling '"
                        + exception.getClass().getSimpleName() + "'", ex);
            } finally {
                scheduler.openTaskCounter.decrementAndGet();
            }
        }

        // override to get notified on any exception from the backgroundTask() method
        public void onFailure(Throwable ex) {
            Executor.LOGGER.error("<onFailure> rethrowing exception: " + ex.getMessage());
            throw new GdxRuntimeException(ex);
        }

    }

}
