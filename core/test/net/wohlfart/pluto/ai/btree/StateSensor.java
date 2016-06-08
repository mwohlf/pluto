package net.wohlfart.pluto.ai.btree;

import net.wohlfart.pluto.ai.btree.IBehavior.State;
import net.wohlfart.pluto.ai.btree.TriggerBehavior.StateHolder;

class StateSensor implements StateHolder {

    private State state;

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public State getState() {
        return state;
    }

}
