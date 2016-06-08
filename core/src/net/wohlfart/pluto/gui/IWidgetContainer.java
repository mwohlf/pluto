package net.wohlfart.pluto.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;

import aurelienribon.tweenengine.Timeline;
import net.wohlfart.pluto.gui.item.ActionCommand;
import net.wohlfart.pluto.gui.item.SingleSelectCommand;
import net.wohlfart.pluto.gui.item.ToggleCommand;
import net.wohlfart.pluto.gui.widget.SelectBoxHandle;
import net.wohlfart.pluto.gui.widget.WidgetHandle;

public interface IWidgetContainer {

    boolean removeActor(Actor delegate);

    void addActorAt(int index, Actor delegate);

    Timeline hide();

    Array<Actor> getChildren();

    <T> SelectBoxHandle<T> add(final SingleSelectCommand<T> cmd);

    WidgetHandle<Label> add(String string);

    WidgetHandle<TextButton> add(ActionCommand cmd);

    WidgetHandle<CheckBox> add(ToggleCommand cmd);

    Stage getStage();

}
