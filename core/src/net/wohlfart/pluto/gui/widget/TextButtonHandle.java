package net.wohlfart.pluto.gui.widget;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.I18NBundle;

import net.wohlfart.pluto.gui.IWidgetContainer;

public class TextButtonHandle implements WidgetHandle<TextButton> {

    private final String i18nKey;

    private TextButton delegate;

    public TextButtonHandle(String i18nKey, String text, Skin skin) {
        delegate = new TextButton(text, skin);
        this.i18nKey = i18nKey;
    }

    @Override
    public TextButton getDelegate() {
        return delegate;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    @Override
    public TextButton update(IWidgetContainer container, I18NBundle i18nBundle, Skin skin) {
        final int index = container.getChildren().indexOf(delegate, true);
        final TextButton result = new TextButton(i18nBundle.get(i18nKey), skin);
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
