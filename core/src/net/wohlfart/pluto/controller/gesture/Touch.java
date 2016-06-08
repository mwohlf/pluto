package net.wohlfart.pluto.controller.gesture;

import com.badlogic.gdx.math.Vector3;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("serial")
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID", justification = "never serialized")
public class Touch extends Vector3 {

    // TODO: this needs some refactoring

    public static final Touch INVALID = new Touch();

    public enum Action {
        UP,
        DOWN,
        MOVE,
        UNDEFINED
    }

    static final int FUZZY_SQUARE_DOTS = 100;
    static final int DELAY_TRIGGER_MSEC = (int) (0.5f * 1000);
    static final int MOVE_MEMORY_MSEC = (int) (1f * 1000);

    int digit;
    long time = Long.MIN_VALUE;
    float force;
    Action action = Action.UNDEFINED;
    boolean consumed;

    public Touch() {
        this(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    Touch(int x, int y) {
        super(x, y, 0);
    }

    public Touch move() {
        return action(Action.MOVE);
    }

    public Touch down() {
        return action(Action.DOWN);
    }

    public Touch up() {
        return action(Action.UP);
    }

    public Touch consume() {
        this.consumed = true;
        return this;
    }

    public Touch action(Action action) {
        this.action = action;
        return this;
    }

    public Touch set(Touch other) {
        super.set(other);
        this.digit = other.digit;
        this.time = other.time;
        this.force = other.force;
        this.action = other.action;
        return this;
    }

    public Touch set(Touch other, Action action) {
        super.set(other);
        this.digit = other.digit;
        this.time = other.time;
        this.force = other.force;
        this.action = action;
        return this;
    }

    public Touch digit(int digit) {
        this.digit = digit;
        return this;
    }

    public Touch position(float x, float y) {
        this.x = x;
        this.y = y;
        this.z = 0;
        return this;
    }

    public Touch time(long now) {
        this.time = now;
        return this;
    }

    // return true if the this is one of the provided actions
    public boolean isOneOf(Action... actions) {
        for (final Action currentAction : actions) {
            if (this.action == currentAction) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + (consumed ? 1231 : 1237);
        result = prime * result + digit;
        result = prime * result + Float.floatToIntBits(force);
        result = prime * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Touch other = (Touch) obj;
        if (action != other.action) {
            return false;
        }
        if (consumed != other.consumed) {
            return false;
        }
        if (digit != other.digit) {
            return false;
        }
        if (Float.floatToIntBits(force) != Float.floatToIntBits(other.force)) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (time != other.time) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() { //@formatter:off
        return "DigitTouch ["
            + "x=" + x + ", "
            + "y=" + y + ", "
            + "z=" + z + ", "
            + "digit=" + digit + ", "
            + "time=" + time + ", "
            + "force=" + force + ", "
            + "action=" + action
            + "]"; //@formatter:on
    }

}
