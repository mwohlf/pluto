package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.entity.fab.roam.ColorFunctionEnum;
import net.wohlfart.pluto.entity.fab.roam.HeightFunctionEnum;
import net.wohlfart.pluto.util.IConsumer;

public class ValueConverters {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverters.class);

    private static final Map<String, ISetterDecorator> CONVERTERS = new HashMap<>();

    static {
        CONVERTERS.put("String", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asString());
            }
        });
        CONVERTERS.put("Long", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asLong());
            }
        });
        CONVERTERS.put("Integer", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asLong().intValue());
            }
        });
        CONVERTERS.put("Float", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asDouble().floatValue());
            }
        });
        CONVERTERS.put("Color", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asColor());
            }
        });
        CONVERTERS.put("Position", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asPosition());
            }
        });
        CONVERTERS.put("Vector3", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asVector());
            }
        });
        CONVERTERS.put("Quaternion", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asQuaternion());
            }
        });
        CONVERTERS.put("Behavior", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asBehavior());
            }
        });
        CONVERTERS.put("Primitive", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asPrimitive());
            }
        });
        CONVERTERS.put("VertexAttribute", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                method.invoke(entity, value.asVertexAttribute());
            }
        });
        CONVERTERS.put("Entity", new ISetterDecorator() {
            @Override
            public void setValue(final Object targetEntity, Method method, Value<?> value) throws Exception {
                value.asEntity().then(new IConsumer<Entity>() {
                    @Override
                    public void apply(Entity valueEntity) {
                        try {
                            method.invoke(targetEntity, valueEntity);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });

        CONVERTERS.put("HeightFunction", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                // TODO: we need more than enums here
                final String enumName = value.asString();
                method.invoke(entity, HeightFunctionEnum.valueOf(enumName).get());
            }
        });
        CONVERTERS.put("ColorFunction", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) throws Exception {
                // TODO: we need more than enums here
                final String enumName = value.asString();
                method.invoke(entity, ColorFunctionEnum.valueOf(enumName).get());
            }
        });

    }

    public static ISetterDecorator get(String type) {
        if (!CONVERTERS.containsKey(type)) {
            LOGGER.debug("<get> no decorator found for type '" + type + "'");
        }
        return CONVERTERS.get(type);
    }

}
