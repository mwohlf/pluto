package net.wohlfart.pluto.stage.loader;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;

import net.wohlfart.pluto.entity.fab.roam.ColorFunction;
import net.wohlfart.pluto.entity.fab.roam.ColorFunction.GradientHeight;
import net.wohlfart.pluto.entity.fab.roam.HeightFunction;
import net.wohlfart.pluto.entity.fab.roam.SimplexIteration;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.util.Utils;

/**
 * synchronous, stateless parameter converters that can convert plain json into
 * parameter instances and don't need to run async or access to the scene graph
 *
 */
final class ConverterContainer {

    private static final HashMap<Class<?>, ParameterConverter<?>> BY_TARGET_CLASS = new HashMap<>();
    private static final HashMap<String, ParameterConverter<?>> BY_NAME = new HashMap<>();

    interface ParameterConverter<T> {

        T convert(JsonValue jsonValue);

    }

    private ConverterContainer() {
    }

    static {
        ConverterContainer.BY_TARGET_CLASS.put(String.class, new StringConverter());
        ConverterContainer.BY_TARGET_CLASS.put(Color.class, new ColorConverter());
        ConverterContainer.BY_TARGET_CLASS.put(Position.class, new PositionConverter());
        ConverterContainer.BY_TARGET_CLASS.put(Vector3.class, new Vector3Converter());
        ConverterContainer.BY_TARGET_CLASS.put(Float.TYPE, new FloatConverter());
        ConverterContainer.BY_TARGET_CLASS.put(Integer.TYPE, new IntegerConverter());
        ConverterContainer.BY_TARGET_CLASS.put(Long.TYPE, new LongConverter());
        ConverterContainer.BY_TARGET_CLASS.put(Quaternion.class, new QuaternionConverter());

        ConverterContainer.BY_NAME.put("primitive", new PrimitiveConverter());
        ConverterContainer.BY_NAME.put("attribute", new AttributeConverter());
        ConverterContainer.BY_NAME.put("heightFunction", new HeightFunctionConverter());
        ConverterContainer.BY_NAME.put("colorFunction", new ColorFunctionConverter());
    }

    static <C> boolean containsKey(Class<C> key) {
        return ConverterContainer.BY_TARGET_CLASS.containsKey(key);
    }

    static <C> boolean containsKey(String key) {
        return ConverterContainer.BY_NAME.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    static <C> ParameterConverter<C> get(Class<C> key) {
        return (ParameterConverter<C>) ConverterContainer.BY_TARGET_CLASS.get(key);
    }

    @SuppressWarnings("unchecked")
    static <C> ParameterConverter<C> get(String key) {
        return (ParameterConverter<C>) ConverterContainer.BY_NAME.get(key);
    }

    static class StringConverter implements ParameterConverter<String> {

        @Override
        public String convert(JsonValue jsonValue) {
            return jsonValue.asString();
        }

    }

    static class FloatConverter implements ParameterConverter<Float> {

        @Override
        public Float convert(JsonValue jsonValue) {
            return jsonValue.asFloat();
        }

    }

    static class IntegerConverter implements ParameterConverter<Integer> {

        @Override
        public Integer convert(JsonValue jsonValue) {
            return jsonValue.asInt();
        }

    }

    static class LongConverter implements ParameterConverter<Long> {

        @Override
        public Long convert(JsonValue jsonValue) {
            return jsonValue.asLong();
        }

    }

    static class PrimitiveConverter implements ParameterConverter<Integer> {

        @Override // see: https://www.opengl.org/wiki/Primitive
        public Integer convert(JsonValue jsonValue) {
            switch (jsonValue.asString()) {
                case "GL_LINES":
                    return GL20.GL_LINES;
                case "GL_TRIANGLES":
                    return GL20.GL_TRIANGLES;
                default:
            }
            throw new IllegalArgumentException("unknown value for '" + jsonValue.name + "' value was '" + jsonValue + "'");
        }

    }

    static class AttributeConverter implements ParameterConverter<VertexAttribute> {

        @Override // see: https://www.opengl.org/wiki/Primitive
        public VertexAttribute convert(JsonValue jsonValue) {
            switch (jsonValue.asString()) {
                case "Normal":
                    return new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE);
                default:
            }
            throw new IllegalArgumentException("unknown value for '" + jsonValue.name + "' value was '" + jsonValue + "'");
        }

    }

    static class HeightFunctionConverter implements ParameterConverter<HeightFunction> {

        public static final HeightFunction ROCK1_HEIGHT = new SimplexIteration(6, 1f, 0.007f);
        public static final HeightFunction ROCK2_HEIGHT = new SimplexIteration(6, 0.7f, 100f);

        @Override // see: https://www.opengl.org/wiki/Primitive
        public HeightFunction convert(JsonValue jsonValue) {
            switch (jsonValue.asString()) {
                case "ROCK1":
                    return HeightFunctionConverter.ROCK1_HEIGHT;
                case "ROCK2":
                    return HeightFunctionConverter.ROCK2_HEIGHT;
                default:
            }
            throw new IllegalArgumentException("unknown value for '" + jsonValue.name + "' value was '" + jsonValue + "'");
        }

    }

    static class ColorFunctionConverter implements ParameterConverter<ColorFunction> {

        public static final ColorFunction ROCK1_COLOR = new GradientHeight(new SimplexIteration(6, 1f, 0.007f));
        public static final ColorFunction ROCK2_COLOR = new GradientHeight(new SimplexIteration(6, 0.7f, 100f));

        @Override // see: https://www.opengl.org/wiki/Primitive
        public ColorFunction convert(JsonValue jsonValue) {
            switch (jsonValue.asString()) {
                case "ROCK1":
                    return ColorFunctionConverter.ROCK1_COLOR;
                case "ROCK2":
                    return ColorFunctionConverter.ROCK2_COLOR;
                default:
            }
            throw new IllegalArgumentException("unknown value for '" + jsonValue.name + "' value was '" + jsonValue + "'");
        }

    }

    static class ColorConverter implements ParameterConverter<Color> {

        @Override
        public Color convert(JsonValue jsonValue) {
            final String[] strings = jsonValue.asString().split(",");
            final float[] floats = new float[strings.length];
            for (int i = 0; i < strings.length; i++) {
                floats[i] = Float.valueOf(strings[i]);
            }
            return new Color(floats[0], floats[1], floats[2], floats[3]);
        }

    }

    static class Vector3Converter implements ParameterConverter<Vector3> {

        private static final String X = "X";
        private static final String Y = "Y";
        private static final String Z = "Z";

        private static final String DIRECTION = "direction";
        private static final String SCALE = "scale";

        @Override
        public Vector3 convert(JsonValue jsonValue) {
            if (jsonValue.isObject()
                    && jsonValue.has(Vector3Converter.DIRECTION)
                    && jsonValue.has(Vector3Converter.SCALE)) {
                // vector defined by direction and scale
                final Vector3 direction = (Vector3) ConverterContainer.BY_TARGET_CLASS.get(Vector3.class).convert(jsonValue.get(Vector3Converter.DIRECTION));
                final float scale = (Float) ConverterContainer.BY_TARGET_CLASS.get(Float.TYPE).convert(jsonValue.get(Vector3Converter.SCALE));
                return direction.nor().scl(scale);
            } else if (jsonValue.isArray()) {
                // vector defined as array
                final float[] floats = jsonValue.asFloatArray();
                return new Vector3(floats[0], floats[1], floats[2]);
            } else if (jsonValue.isString() && jsonValue.asString().contains(",")) {
                // vector defined as string
                final String[] strings = jsonValue.asString().split(",");
                final float[] floats = new float[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    floats[i] = Float.valueOf(strings[i]);
                }
                return new Vector3(floats[0], floats[1], floats[2]);
            } else if (jsonValue.isString()
                    && Vector3Converter.X.equalsIgnoreCase(jsonValue.asString())) {
                return new Vector3().set(Vector3.X);

            } else if (jsonValue.isString()
                    && Vector3Converter.Y.equalsIgnoreCase(jsonValue.asString())) {
                return new Vector3().set(Vector3.Y);
            } else if (jsonValue.isString()
                    && Vector3Converter.Z.equalsIgnoreCase(jsonValue.asString())) {
                return new Vector3().set(Vector3.Z);
            } else {
                throw new GdxRuntimeException("unknown vector config: " + jsonValue);
            }
        }

    }

    static class PositionConverter implements ParameterConverter<Position> {

        private static final Pattern UNDERSCORE = Pattern.compile("_", Pattern.LITERAL);

        @Override
        public Position convert(JsonValue jsonValue) {
            if (jsonValue.isArray()) {
                final double[] doubles = jsonValue.asDoubleArray();
                return new Position(doubles[0], doubles[1], doubles[2]);
            } else if (jsonValue.isString()) {
                final String[] strings = jsonValue.asString().split(",");
                final double[] doubles = new double[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    doubles[i] = Double.valueOf(UNDERSCORE.matcher(strings[i]).replaceAll(Matcher.quoteReplacement("")));
                }
                return new Position(doubles[0], doubles[1], doubles[2]);
            } else {
                throw new GdxRuntimeException("unknown position config: " + jsonValue);
            }
        }

    }

    static class QuaternionConverter implements ParameterConverter<Quaternion> {

        private static final String AXIS = "axis";
        private static final String DEGREE = "degree";

        @Override
        public Quaternion convert(JsonValue jsonValue) {
            if (jsonValue.isObject()
                    && jsonValue.has(QuaternionConverter.AXIS)
                    && jsonValue.has(QuaternionConverter.DEGREE)) {
                // rotation defined by axis and degree
                final Vector3 axis = (Vector3) ConverterContainer.BY_TARGET_CLASS.get(Vector3.class).convert(jsonValue.get(QuaternionConverter.AXIS));
                final float degree = (Float) ConverterContainer.BY_TARGET_CLASS.get(Float.TYPE).convert(jsonValue.get(QuaternionConverter.DEGREE));
                return Utils.createQuaternion(axis, degree);
            } else if (jsonValue.isArray()) {
                // rotation as quaternion
                final float[] floats = jsonValue.asFloatArray();
                return new Quaternion(floats[0], floats[1], floats[2], floats[3]);
            } else if (jsonValue.isString()) {
                // rotation as string
                final String[] strings = jsonValue.asString().split(",");
                final float[] floats = new float[strings.length];
                for (int i = 0; i < strings.length; i++) {
                    floats[i] = Float.valueOf(strings[i]);
                }
                return new Quaternion(floats[0], floats[1], floats[2], floats[3]);
            } else {
                throw new GdxRuntimeException("unknown quaternion config: " + jsonValue);
            }
        }

    }

}
