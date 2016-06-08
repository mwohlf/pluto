package net.wohlfart.pluto.event;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

// TODO: do some optimization and combine multiple event on the same object (e.g. two moves can be combined into one...)
public class EventBus implements IEventBus<Poolable> {

    protected static final Logger LOGGER = LoggerService.forClass(EventBus.class);

    private final ConcurrentLinkedQueue<Poolable> queue = new ConcurrentLinkedQueue<>();

    private final Collection<HandlerInfo> handlers = new CopyOnWriteArrayList<>();

    public void setSubscriber(Iterable<Object> subscribers) {
        EventBus.LOGGER.debug("<setSubscriber> for" + subscribers);
        for (final Object object : subscribers) {
            register(object);
        }
    }

    @Override
    public void register(Object newSubscriber) {
        //Gdx.app.debug(Pluto.LOG_TAG, "<register> for " + newSubscriber);

        final Class<?> clazz = newSubscriber.getClass();
        boolean foundAnnotation = false;
        //Gdx.app.debug(Pluto.LOG_TAG, "<register> checking " + clazz);
        for (final Method superClazzMethod : clazz.getMethods()) {
            //Gdx.app.debug(Pluto.LOG_TAG, "<register> checking method " + superClazzMethod
            //        + " in class " + clazz);
            if (superClazzMethod.isAnnotationPresent(Subscribe.class)) {
                checkAnnotation(newSubscriber, clazz, superClazzMethod);
                foundAnnotation = true;
            }
        }
        if (!foundAnnotation) {
            EventBus.LOGGER.error("<register> no subscribe annotation found for " + newSubscriber.getClass().getSimpleName());
        }
    }

    private void checkAnnotation(Object subscriber, Class<?> clazz, Method method) {
        //Gdx.app.debug(Pluto.LOG_TAG, "<checkAnnotation> subscribe found for " + method.getName()
        //        + " in class " + clazz);
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method " + method + " on class " + clazz
                    + " has @Subscribe annotation, but requires " + parameterTypes.length
                    + " arguments.  Event handler methods must require a single argument (the event).");
        }

        if (!Poolable.class.isAssignableFrom(parameterTypes[0])) {
            throw new IllegalArgumentException("Method " + method + " on class " + clazz
                    + " has parameter type " + parameterTypes[0]
                    + " which is not assignment-compatible with " + Poolable.class);
        }

        final HandlerInfo info = new HandlerInfo(parameterTypes[0], method, subscriber);
        if (handlers.contains(info)) {
            throw new IllegalArgumentException("Method " + method + " on class " + clazz + " is registered twice.");
        }

        handlers.add(info);
    }

    @Override
    public void unregister(Object subscriber) {
        final Iterator<HandlerInfo> iter = handlers.iterator();
        while (iter.hasNext()) {
            final HandlerInfo handler = iter.next();
            if (handler.matchesSubscriber(subscriber)) {
                iter.remove();
            }
        }
    }

    @Override
    public void post(Poolable event) {
        assert event != null;
        queue.add(event);
    }

    @Override
    public boolean hasEvent() {
        return !queue.isEmpty();
    }

    @Override
    public void fireEvent() {
        final Poolable event = queue.poll();
        if (event == null) {
            //Gdx.app.debug(Pluto.LOG_TAG, "<fireEvent> ignoring since no events are available, "
            //        + "use hasEvent() before calling fireEvent()");
            return;
        }
        //int invokeCount = 0;
        //Gdx.app.debug(Pluto.LOG_TAG, "<fireEvent> "
        //        + "handler found for event " + event
        //        + ", subscriber is " + handler.subscriber.getClass().getName()
        //        + " on method " + handler.method.getName());
        //invokeCount++;
        for (final HandlerInfo handlerInfo : handlers) {
            if (handlerInfo.matchesEvent(event)) {
                handlerInfo.invoke(event);
            }
        }
        //Gdx.app.debug(Pluto.LOG_TAG, "<fireEvent> " + invokeCount + " handler for  event " + event);
        event.reset();
    }

    private static class HandlerInfo {
        private final Object subscriber;
        private final Method method;
        private final Class<?> eventClass;

        HandlerInfo(Class<?> eventClass, Method method, Object subscriber) {
            this.eventClass = eventClass;
            this.method = method;
            this.subscriber = subscriber;
        }

        void invoke(Object event) {
            try {
                method.invoke(subscriber, event);
            } catch (final Exception ex) {
                throw new GdxRuntimeException(ex);
            }
        }

        boolean matchesEvent(Object event) {
            return event.getClass().equals(eventClass);
        }

        boolean matchesSubscriber(Object subscriber) {
            return this.subscriber == subscriber;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((eventClass == null) ? 0 : eventClass.hashCode());
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            result = prime * result + ((subscriber == null) ? 0 : subscriber.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }

            final HandlerInfo that = (HandlerInfo) object;

            if (eventClass == null) {
                if (that.eventClass != null) {
                    return false;
                }
            } else if (!eventClass.equals(that.eventClass)) {
                return false;
            }

            if (method == null) {
                if (that.method != null) {
                    return false;
                }
            } else if (!method.equals(that.method)) {
                return false;
            }

            if (subscriber == null) {
                if (that.subscriber != null) {
                    return false;
                }
            } else if (!subscriber.equals(that.subscriber)) {
                return false;
            }

            return true;
        }

    }

    @Override
    public void flush() {
        while (!queue.isEmpty()) {
            final Poolable event = queue.poll();
            event.reset();
        }
    }

    @Override
    public void fireAllEvents() {
        while (hasEvent()) {
            fireEvent();
        }
    }

}
