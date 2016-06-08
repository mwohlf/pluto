package net.wohlfart.pluto.scene.lang;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.ai.btree.Parallel;
import net.wohlfart.pluto.ai.btree.Sequential;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.Position;

/**
 * container for a variable
 */
public class Value<T> implements Comparable<Value<T>> {

    public static final Value<Object> NULL = new Value<>(Object.class, new Object());
    public static final Value<Object> INVALID = new Value<>(Object.class, new Object());

    private static final Set<Class<?>> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            Double.class, Long.class, String.class, Position.class, Color.class,
            IBehavior.class, IEntityCommand.class, FutureEntity.class,
            List.class, Vector3.class, Quaternion.class, Boolean.class));

    private final T value;

    private final Class<T> clazz;

    public static <T> Value<T> of(Value<T> val) {
        return new Value<T>(val.clazz, val.value);
    }

    public Value<?> or(Value value) {
        if (this == NULL) {
            return value;
        }
        return this;
    }

    public static Value<Double> of(Double v) {
        return new Value<Double>(Double.class, v);
    }

    public static Value<Double> of(Float v) {
        return new Value<Double>(Double.class, v.doubleValue());
    }

    public static Value<Long> of(Long v) {
        return new Value<Long>(Long.class, v);
    }

    public static Value<String> of(String v) {
        return new Value<String>(String.class, v);
    }

    public static Value<Position> of(Position v) {
        return new Value<Position>(Position.class, v);
    }

    public static Value<Color> of(Color v) {
        return new Value<Color>(Color.class, v);
    }

    public static Value<IBehavior> of(IBehavior v) {
        return new Value<IBehavior>(IBehavior.class, v);
    }

    public static Value<FutureEntity> of(FutureEntity v) {
        return new Value<FutureEntity>(FutureEntity.class, v);
    }

    public static Value<List> of(List<Value<?>> v) {
        return new Value<List>(List.class, v);
    }

    public static Value<Vector3> of(Vector3 v) {
        return new Value<>(Vector3.class, v);
    }

    public static Value<Boolean> of(Boolean v) {
        return new Value<Boolean>(Boolean.class, v);
    }

    public static Value<Long> of(int v) {
        return of((long) v);
    }

    public static Value<Quaternion> of(Quaternion v) {
        return new Value<Quaternion>(Quaternion.class, v);
    }

    private Value(Class<T> clazz, T value) {
        if (value == null) {
            throw new RuntimeException("value is null, clazz was " + clazz);
        }
        if (clazz != Object.class && !SUPPORTED_TYPES.contains(clazz)) {
            throw new EvalException("type not supported: " + clazz);
        }
        this.clazz = clazz;
        this.value = value;
    }

    public Value(Value<T> value) {
        this.clazz = value.clazz;
        this.value = value.value;
    }

    private boolean is(Class<?> clazz) {
        return this.clazz == clazz;
    }

    private T as(Class<T> clazz) {
        return value;
    }

    public boolean isDouble() {
        return is(Double.class);
    }

    public boolean isLong() {
        return is(Long.class);
    }

    public boolean isNumber() {
        return isLong() || isDouble();
    }

    public boolean isString() {
        return is(String.class);
    }

    public boolean isPosition() {
        return is(Position.class);
    }

    public boolean isColor() {
        return is(Color.class);
    }

    public boolean isBehavior() {
        return is(IBehavior.class);
    }

    public boolean isEntity() {
        return is(IEntityCommand.class);
    }

    public boolean isList() {
        return is(List.class);
    }

    public boolean isVector() {
        return is(Vector3.class);
    }

    public boolean isBoolean() {
        return is(Boolean.class);
    }

    public boolean isFloat() {
        return is(Double.class) || is(Long.class);
    }

    public boolean isNull() {
        return this == NULL;
    }

    public Double asDouble() {
        return ((Number) value).doubleValue();
    }

    public Long asLong() {
        return ((Number) value).longValue();
    }

    public Float asFloat() {
        return ((Number) value).floatValue();
    }

    public Integer asInteger() {
        return ((Number) value).intValue();
    }

    public String asString() {
        return String.valueOf(value);
    }

    public Position asPosition() {
        return (Position) value;
    }

    public Color asColor() {
        return (Color) value;
    }

    public IBehavior<?> asBehavior() {
        return (IBehavior<?>) value;
    }

    public FutureEntity asEntity() {
        return (FutureEntity) value;
    }

    public List<Value<?>> asList() {
        return (List<Value<?>>) value;
    }

    public Vector3 asVector() {
        return (Vector3) value;
    }

    public Quaternion asQuaternion() {
        return (Quaternion) value;
    }

    public Boolean asBoolean() {
        return (Boolean) value;
    }

    public int asPrimitive() {
        switch ((String) value) {
            case "GL_LINES":
                return GL20.GL_LINES;
            case "GL_TRIANGLES":
                return GL20.GL_TRIANGLES;
        }
        throw new GdxRuntimeException("unknowning primitive type: " + value);
    }

    public VertexAttribute asVertexAttribute() {
        switch ((String) value) {
            case "Normal":
                return new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE);
            case "TextureCoordinates":
                return new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE);
        }
        throw new GdxRuntimeException("unknowning vertex attribute: " + value);
    }

    @Override
    public String toString() {
        return "Value: " + String.valueOf(value)
                + " with clazz: " + clazz;
    }

    @Override
    public int compareTo(Value<T> that) {
        // TODO
        return 0;
    }

    public Value<?> minus(Value<?> that) {
        if (this.isDouble() && that.isDouble()) {
            return Value.of((Double) this.value - (Double) that.value);
        } else if (this.isDouble() && that.isLong()) {
            return Value.of((Double) this.value - (Long) that.value);
        } else if (this.isLong() && that.isDouble()) {
            return Value.of((Long) this.value - (Double) that.value);
        } else if (this.isLong() && that.isLong()) {
            return Value.of((Long) this.value - (Long) that.value);
        }
        return this;
    }

    public Value<?> plus(Value<?> that) {
        if (this.isDouble() && that.isDouble()) {
            return Value.of((Double) this.value + (Double) that.value);
        } else if (this.isDouble() && that.isLong()) {
            return Value.of((Double) this.value + (Long) that.value);
        } else if (this.isLong() && that.isDouble()) {
            return Value.of((Long) this.value + (Double) that.value);
        } else if (this.isLong() && that.isLong()) {
            return Value.of((Long) this.value + (Long) that.value);
        }
        return Value.INVALID;
    }

    public Value<?> parallel(Value right) {
        Parallel result;
        if (this.value instanceof Parallel) {
            result = (Parallel) value;
        } else {
            result = new Parallel();
            result.addChild((IBehavior) this.value);
        }
        result.addChild(right.asBehavior());
        return Value.of(result);
    }

    public Value sequential(Value right) {
        Sequential result;
        if (this.value instanceof Sequential) {
            result = (Sequential) value;
        } else {
            result = new Sequential();
            result.addChild((IBehavior) this.value);
        }
        result.addChild(right.asBehavior());
        return Value.of(result);
    }

}
