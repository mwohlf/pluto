package net.wohlfart.pluto.gui.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;

import net.wohlfart.pluto.Logging;
import net.wohlfart.pluto.gui.IWidgetContainer;

public class SelectBoxHandle<T> implements WidgetHandle<SelectBox<Choice<T>>> {

    private ChoiceSelectBox<T> delegate;

    private static final Action NULL_ACTION = new Action() {
        @Override
        public boolean act(float delta) {
            return true; // do nothing
        }
    };

    // can be set by client code to perform an action when item is selected
    protected Action hideAction = SelectBoxHandle.NULL_ACTION;

    public SelectBoxHandle(Array<Choice<T>> items, Skin skin) {
        delegate = new ChoiceSelectBox<>(skin);
        delegate.setItems(items);
    }

    @Override
    public SelectBox<Choice<T>> getDelegate() {
        return delegate;
    }

    public void onFinishHide(Action action) {
        this.hideAction = action;
    }

    public void select(T value) {
        delegate.select(value);
    }

    @Override
    public SelectBox<Choice<T>> update(IWidgetContainer container, I18NBundle i18nBundle, Skin skin) {
        final int index = container.getChildren().indexOf(delegate, true);

        final ChoiceSelectBox<T> result = new ChoiceSelectBox<>(skin);
        result.setPosition(delegate.getX(), delegate.getY());
        result.setItems(delegate.getItems());

        for (final Choice<T> choice : result.getItems()) {
            choice.setText(" " + i18nBundle.get(choice.getI18nKey()) + " ");
        }

        result.getSelection().setProgrammaticChangeEvents(false);
        for (final EventListener listener : delegate.getCaptureListeners()) {
            result.addCaptureListener(listener);
        }
        result.setWidth(delegate.getWidth());
        result.setSelected(delegate.getSelected());
        result.getSelection().setProgrammaticChangeEvents(true);
        // swap actor
        container.removeActor(delegate);
        delegate = result;
        container.addActorAt(index, delegate);
        return delegate;

    }

    private final class ChoiceSelectBox<U> extends SelectBox<Choice<U>> {

        private ChoiceSelectBox(Skin skin) {
            super(skin);
        }

        @Override
        protected void onHide(Actor selectBoxList) {
            // we use a select box to trigger a density change, we need to wait for the animation to finish
            // before changing the density to avoid flickering
            selectBoxList.getColor().a = 1;
            selectBoxList.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f, Interpolation.fade),
                    hideAction,
                    Actions.removeActor()));
        }

        public void select(U value) {
            getSelection().setProgrammaticChangeEvents(false); // don't fire event
            for (final Choice<U> item : getItems()) {
                if (item.getValue().equals(value)) {
                    setSelected(item);
                    break;
                }
            }
            if (!getSelected().getValue().equals(value)) {
                Logging.ROOT.error("<select> couldn't select: " + value);
            }
            getSelection().setProgrammaticChangeEvents(true);
        }

    }

}
