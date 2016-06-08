package net.wohlfart.pluto.controller;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * listen for key down events and perform action from the command map
 */
public class CommandInput extends InputAdapter {

    // mapping keys to action strings
    private final IntMap<String> actionMap = new IntMap<>();
    // mapping action strings to commands
    private final ObjectMap<String, Command> commandMap;

    public CommandInput(ObjectMap<String, Command> commandMap) {
        this.commandMap = commandMap;
    }

    @Override
    public boolean keyDown(int key) {
        if (!actionMap.containsKey(key)) {
            return false;
        }
        commandMap.get(actionMap.get(key), NULL_COMMAND).execute();
        return true;
    }

    public void putKeyAction(int key, String action) {
        actionMap.put(key, action);
    }

    public void removeKeyAction(int key, String action) {
        actionMap.remove(key);
    }

    private static final Command NULL_COMMAND = new Command() {

        @Override
        public String getKey() {
            assert false;
            return null;
        }

        @Override
        public void execute() {
            // do nothing
        }

    };

}
