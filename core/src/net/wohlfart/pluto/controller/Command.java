package net.wohlfart.pluto.controller;

public interface Command {

    // must be unique
    String getKey();

    void execute();

}
