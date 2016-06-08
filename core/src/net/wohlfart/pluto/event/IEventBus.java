package net.wohlfart.pluto.event;

public interface IEventBus<T> {

    void register(Object subscriber);

    void unregister(Object subscriber);

    // resets the event as soon as it is delivered
    void post(T event);

    // return true if there are some event waiting to be delivered
    boolean hasEvent();

    // fires a single event
    void fireEvent();

    // fires all events
    void fireAllEvents();

    // remove all events from the queue
    void flush();

}
