package net.wohlfart.pluto.gui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

import net.wohlfart.pluto.gui.IWidgetContainer;

public interface WidgetHandle<D> {

    D update(IWidgetContainer container, I18NBundle i18nBundle, Skin skin);

    D getDelegate();

}
