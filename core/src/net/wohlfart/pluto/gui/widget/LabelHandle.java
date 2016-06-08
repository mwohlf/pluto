package net.wohlfart.pluto.gui.widget;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

import net.wohlfart.pluto.gui.IWidgetContainer;

public class LabelHandle implements WidgetHandle<Label> {

    private final String i18nKey;

    private Label delegate;

    public LabelHandle(String i18nKey, String text, Skin skin) {
        delegate = new Label(text, skin);
        this.i18nKey = i18nKey;
    }

    public void setText(CharSequence string) {
        delegate.setText(string);
    }

    public String getI18nKey() {
        return i18nKey;
    }

    @Override
    public Label getDelegate() {
        return delegate;
    }

    @Override
    public Label update(IWidgetContainer container, I18NBundle i18nBundle, Skin skin) {
        final int index = container.getChildren().indexOf(delegate, true);
        final Label result = new Label(i18nBundle.get(i18nKey), skin);
        result.setPosition(delegate.getX(), delegate.getY());
        for (final EventListener listener : delegate.getListeners()) {
            result.addListener(listener);
        }
        // swap actor
        container.removeActor(delegate);
        delegate = result;
        container.addActorAt(index, delegate);
        return delegate;
    }

}
