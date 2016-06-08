package net.wohlfart.pluto.util;

public interface ISupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
