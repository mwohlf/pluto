package net.wohlfart.pluto.gui.widget;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

import net.wohlfart.pluto.gui.IWidgetContainer;

public class CheckBoxHandle implements WidgetHandle<CheckBox> {

    private final String i18nKey;

    private CheckBox delegate;

    public CheckBoxHandle(String i18nKey, String text, Skin skin) {
        delegate = new CheckBox(text, skin);
        this.i18nKey = i18nKey;
    }

    @SuppressWarnings("BooleanParameter")
    public void setChecked(boolean isChecked) {
        delegate.setChecked(isChecked);
    }

    @Override
    public CheckBox getDelegate() {
        return delegate;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    @Override
    public CheckBox update(IWidgetContainer container, I18NBundle i18nBundle, Skin skin) {
        final int index = container.getChildren().indexOf(delegate, true);
        final CheckBox result = new CheckBox(i18nBundle.get(i18nKey), skin);
        result.setPosition(delegate.getX(), delegate.getY());
        for (final EventListener listener : delegate.getListeners()) {
            result.addListener(listener);
        }
        result.setChecked(delegate.isChecked());
        // swap actors
        container.removeActor(delegate);
        delegate = result;
        container.addActorAt(index, delegate);
        return delegate;
    }

}
