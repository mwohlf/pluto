package net.wohlfart.pluto.gui.item;

import net.wohlfart.pluto.gui.widget.Choice;

import com.badlogic.gdx.utils.Array;

public abstract class SingleSelectCommand<T> {
    private final Array<Choice<T>> items;

    public SingleSelectCommand(Array<Choice<T>> items) {
        this.items = items;
    }

    public Array<Choice<T>> getItems() {
        return items;
    }

    public abstract void select(T selection);

}
