package net.wohlfart.pluto.controller;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap;

/**
 * listen for keyboard and mouse-wheel events and calculate a transform matrix
 */
class DesktopInput extends InputAdapter implements ITransformCalculator {

    private static final float TRANSLATE_UNITS_PER_SECOND = 10f;

    private static final float ROTATE_DEGREE_PER_SECOND = 360f / 4f;

    private static final float SCROLL_UNITS = 2f;

    private final IntMap<Action> keyCodes = new IntMap<>();

    private final IntMap<Action> pressed;

    private final ScrollAction scrollAction = new ScrollAction(DesktopInput.SCROLL_UNITS);

    protected final Matrix4 transform = new Matrix4();

    DesktopInput() {
        keyCodes.put(Keys.Q, new TranslateAction(new Vector3(Vector3.Z).scl(+DesktopInput.TRANSLATE_UNITS_PER_SECOND)));
        keyCodes.put(Keys.X, new TranslateAction(new Vector3(Vector3.Z).scl(-DesktopInput.TRANSLATE_UNITS_PER_SECOND)));
        keyCodes.put(Keys.Z, new TranslateAction(new Vector3(Vector3.Y).scl(+DesktopInput.TRANSLATE_UNITS_PER_SECOND)));
        keyCodes.put(Keys.W, new TranslateAction(new Vector3(Vector3.Y).scl(-DesktopInput.TRANSLATE_UNITS_PER_SECOND)));
        keyCodes.put(Keys.A, new TranslateAction(new Vector3(Vector3.X).scl(+DesktopInput.TRANSLATE_UNITS_PER_SECOND)));
        keyCodes.put(Keys.D, new TranslateAction(new Vector3(Vector3.X).scl(-DesktopInput.TRANSLATE_UNITS_PER_SECOND)));
        keyCodes.put(Keys.RIGHT, new RotateAction(Vector3.Y, DesktopInput.ROTATE_DEGREE_PER_SECOND));
        keyCodes.put(Keys.LEFT, new RotateAction(Vector3.Y, -DesktopInput.ROTATE_DEGREE_PER_SECOND));
        keyCodes.put(Keys.UP, new RotateAction(Vector3.X, DesktopInput.ROTATE_DEGREE_PER_SECOND));
        keyCodes.put(Keys.DOWN, new RotateAction(Vector3.X, -DesktopInput.ROTATE_DEGREE_PER_SECOND));
        keyCodes.put(Keys.P, new RotateAction(Vector3.Z, DesktopInput.ROTATE_DEGREE_PER_SECOND));
        keyCodes.put(Keys.L, new RotateAction(Vector3.Z, -DesktopInput.ROTATE_DEGREE_PER_SECOND));
        pressed = new IntMap<>(keyCodes.size); // to avoid resizing
    }

    /**
     * resets and recalculates the rotation since the last call returns the
     * internal state of the
     *
     * @param deltaSeconds
     *            time since last call in seconds
     */
    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        transform.idt();
        for (final Action action : pressed.values().toArray()) {
            action.perform(deltaSeconds);
        }
        scrollAction.perform();
        return transform;
    }

    public boolean hasTransform() {
        return pressed.size > 0 || !MathUtils.isZero(scrollAction.amount, 0.01f);
    }

    /**
     * Called when a key was pressed down. Reports the key code, as found in
     * Keys.
     */
    @Override
    public boolean keyDown(int key) {
        if (!keyCodes.containsKey(key)) {
            return false;
        }
        pressed.put(key, keyCodes.get(key));
        return true;
    }

    /**
     * Called when a key was lifted.
     */
    @Override
    public boolean keyUp(int key) {
        if (!pressed.containsKey(key)) {
            return false;
        }
        pressed.remove(key);
        return true;
    }

    /**
     * Called when the scroll wheel of the mouse was turned. Reports either -1
     * or 1 depending on the direction of spin. This will never be called for
     * touch screen devices.
     */
    @Override
    public boolean scrolled(int amount) {
        scrollAction.update(amount);
        return true;
    }

    private final class ScrollAction {
        private final float scrollUnits;
        private float amount;

        private ScrollAction(float scrollUnits) {
            this.scrollUnits = scrollUnits;
        }

        public void update(int amount) {
            this.amount += amount;
        }

        public void perform() {
            transform.translate(0, 0, scrollUnits * amount);
            amount = 0;
        }
    }

    private interface Action {
        void perform(float delta);
    }

    private class TranslateAction implements Action {
        private final Vector3 translation;

        TranslateAction(Vector3 translation) {
            this.translation = translation;
        }

        @Override
        public void perform(float delta) {
            transform.translate(translation.x * delta, translation.y * delta, translation.z * delta);
        }
    }

    private class RotateAction implements Action {
        private final Vector3 axis;
        private final float degrees;

        RotateAction(Vector3 axis, float degrees) {
            this.axis = axis;
            this.degrees = degrees;
        }

        @Override
        public void perform(float delta) {
            transform.rotate(axis, degrees * delta);
        }
    }

}
