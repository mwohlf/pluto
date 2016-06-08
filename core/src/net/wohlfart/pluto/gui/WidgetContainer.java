package net.wohlfart.pluto.gui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectSet;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.TweenCallback;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.wohlfart.pluto.gui.item.ActionCommand;
import net.wohlfart.pluto.gui.item.SingleSelectCommand;
import net.wohlfart.pluto.gui.item.ToggleCommand;
import net.wohlfart.pluto.gui.widget.CheckBoxHandle;
import net.wohlfart.pluto.gui.widget.Choice;
import net.wohlfart.pluto.gui.widget.LabelHandle;
import net.wohlfart.pluto.gui.widget.SelectBoxHandle;
import net.wohlfart.pluto.gui.widget.TextButtonHandle;
import net.wohlfart.pluto.gui.widget.WidgetHandle;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.resource.ResourceManager.ResourceObserver;

/**
 * turning logical command objects into viewable widgets, and layout them look
 * and feel of the widgets depends on - skin - i18n - density
 *
 * don't use screen positions or size here for layout, use the viewport
 */
public class WidgetContainer extends Group implements ResourceObserver, IWidgetContainer {

    private I18NBundle i18nBundle;
    private Skin skin;
    private final ILayout layout;

    private final ObjectSet<WidgetHandle<? extends Actor>> skinnables = new ObjectSet<>();

    WidgetContainer(ResourceManager resourceManager, ILayout layout) {
        super();
        skin = resourceManager.getSkin();
        i18nBundle = resourceManager.getI18nBundle();
        this.layout = layout;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    Timeline show() {
        final Timeline timeline = layout.show(this);
        timeline.setCallback((type, baseTween) -> setVisible(true)).setCallbackTriggers(TweenCallback.START);
        return timeline;
    }

    @Override
    public Timeline hide() {
        final Timeline timeline = layout.hide(this);
        timeline.setCallback((type, baseTween) -> setVisible(false)).setCallbackTriggers(TweenCallback.COMPLETE);
        return timeline;
    }

    Timeline refresh() {
        return layout.refresh(this);
    }

    /** update for skin, density and locale */
    @Override
    public void updateResources(ResourceManager resourceManager) {
        skin = resourceManager.getSkin();
        i18nBundle = resourceManager.getI18nBundle();
        final DensityViewport viewport = (DensityViewport) getStage().getViewport();
        final boolean wasVisible = isVisible();
        setVisible(false);
        viewport.updateDensity(resourceManager.getDensity());
        updateComponents(); // refreshes the textures and stuff, causing a flicker if we stay visible
        if (wasVisible) {
            setVisible(true);
            layout.refresh(this).start(resourceManager.getTweenManager());
        }
    }

    @Override
    public <T> SelectBoxHandle<T> add(final SingleSelectCommand<T> cmd) {
        final SelectBoxHandle<T> widgetHandle = new SelectBoxHandle<>(cmd.getItems(), skin);
        final SelectBox<Choice<T>> delegate = widgetHandle.getDelegate();
        for (final Choice<T> choice : delegate.getItems()) {
            choice.setText(localize(choice.getI18nKey()));
        }
        delegate.addCaptureListener(new ChangeListener() {
            @Override
            @SuppressWarnings("unchecked")
            @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "actor can only be button which is an instance of SelectBox<Choice<T>>")
            public void changed(final ChangeEvent event, Actor actor) {
                final SelectBox<Choice<T>> box = (SelectBox<Choice<T>>) actor;
                final T value = box.getSelected().getValue();
                //noinspection InnerClassTooDeeplyNested
                widgetHandle.onFinishHide(new Action() {
                    @Override
                    public boolean act(float delta) {
                        cmd.select(value);
                        return true;
                    }
                });
            }
        });
        delegate.setWidth(170);
        skinnables.add(widgetHandle);
        addActor(delegate);
        delegate.setVisible(false);
        return widgetHandle;
    }

    @Override
    public LabelHandle add(String string) {
        final LabelHandle label = new LabelHandle("null", string, skin);
        final Label delegate = label.getDelegate();
        skinnables.add(label);
        addActor(delegate);
        delegate.setVisible(false);
        return label;
    }

    @Override
    public TextButtonHandle add(final ActionCommand cmd) {
        final TextButtonHandle widgetHandle = new TextButtonHandle(
                cmd.getI18nKey(), localize(cmd.getI18nKey()), skin);
        final TextButton delegate = widgetHandle.getDelegate();
        delegate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cmd.execute();
            }
        });
        skinnables.add(widgetHandle);
        addActor(delegate);
        delegate.setVisible(false);
        return widgetHandle;
    }

    @Override
    public CheckBoxHandle add(final ToggleCommand cmd) {
        final CheckBoxHandle widgetHandle = new CheckBoxHandle(
                cmd.getI18nKey(), localize(cmd.getI18nKey()), skin);
        final CheckBox delegate = widgetHandle.getDelegate();
        delegate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cmd.toggle(widgetHandle.getDelegate().isChecked());
            }
        });
        skinnables.add(widgetHandle);
        addActor(delegate);
        delegate.setVisible(false);
        return widgetHandle;
    }

    private String localize(String i18nKey) {
        return " " + i18nBundle.get(i18nKey) + " ";
    }

    // call this after resources have changed and we need to reload skin,
    // locale and refresh the layout
    // TODO: need a fix for: the client can't use the returned object from the add method because we remove it here...
    private void updateComponents() {
        for (final WidgetHandle<? extends Actor> handle : skinnables) {
            handle.update(this, i18nBundle, skin);
        }
    }

}
