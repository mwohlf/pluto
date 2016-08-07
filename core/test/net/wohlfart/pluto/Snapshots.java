package net.wohlfart.pluto;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;

import net.wohlfart.pluto.util.Utils;

/**
 * see: http://www.badlogicgames.com/forum/viewtopic.php?f=15&t=22060
 * http://stackoverflow.com/questions/27998757/how-to-run-libgdx-headless-without-gpu-but-still-rendering-frames
 */
public class Snapshots {

    static final int WIDTH = 1024;
    static final int HEIGHT = 500;

    private static final int BOOTUP_DELAY = 10_000;
    private static final int SHUTDOWN_DELAY = 10_000;

    private final Pluto pluto = new Pluto();

    //@Test
    //@Ignore
    public void smokeTest() throws InterruptedException {
        // desktop startup
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(WIDTH, HEIGHT);
        config.disableAudio(true);
        final Thread thread = new Thread(() -> {
            // runs the render loop
            new Lwjgl3Application(pluto, config);
        });
        thread.setDaemon(true);
        thread.start();

        while (pluto.getCurrentStage().getTasksLeft() > 0
                || null == pluto.getResourceManager()
                || !(pluto.getCurrentStage() instanceof AbstractGraphStage)) {
            thread.sleep(500);
            // wait for the render thread to call the create() method
        }
        final FileHandle file = new FileHandle("/tmp/output1.png");
        takeScreenshot(file);
        Assert.assertTrue(file.exists());
        pluto.getResourceManager().invokeLater(() -> {
            pluto.scheduleTransitionToStage(null, IStage.SYSTEM_EXIT);
        });
    }

    private void takeScreenshot(final FileHandle fileHandle) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Assert.assertTrue(null != pluto.getResourceManager());
        pluto.getResourceManager().invokeLater(() -> {
            doTakeScreenshot(fileHandle);
            latch.countDown();
        });
        latch.await();
    }

    private void doTakeScreenshot(final FileHandle fileHandle) {
        assert Utils.isRenderThread();
        final byte[] pixelData = ScreenUtils.getFrameBufferPixels(true);
        final Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA8888);
        final ByteBuffer pixels = pixmap.getPixels();
        pixels.clear();
        pixels.put(pixelData);
        pixels.position(0);
        PixmapIO.writePNG(fileHandle, pixmap);
    }

}
