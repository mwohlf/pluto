package net.wohlfart.pluto.entity;

public interface Callback<T> {

    void ready(T entity);

}
