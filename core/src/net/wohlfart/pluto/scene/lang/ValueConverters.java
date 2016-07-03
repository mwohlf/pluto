package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.ashley.core.Entity;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.entity.fab.roam.ColorFunctionEnum;
import net.wohlfart.pluto.entity.fab.roam.HeightFunctionEnum;
import net.wohlfart.pluto.util.IConsumer;

public class ValueConverters {
    private static final Logger LOGGER = LoggerService.forClass(ValueConverters.class);

    private static final Map<String, ISetterDecorator> CONVERTERS = new HashMap<>();

    static {
        CONVERTERS.put("String", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asString());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Long", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asLong());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Integer", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asLong().intValue());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Float", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asDouble().floatValue());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Color", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asColor());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Position", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asPosition());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Vector3", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asVector());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Quaternion", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asQuaternion());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Behavior", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asBehavior());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Primitive", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asPrimitive());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("VertexAttribute", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                try {
                    method.invoke(entity, value.asVertexAttribute());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("Entity", new ISetterDecorator() {
            @Override
            public void setValue(final Object targetEntity, Method method, Value<?> value) {
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
            public void setValue(Object entity, Method method, Value<?> value) {
                // TODO: we need more than enums here
                final String enumName = value.asString();
                try {
                    method.invoke(entity, HeightFunctionEnum.valueOf(enumName).get());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        CONVERTERS.put("ColorFunction", new ISetterDecorator() {
            @Override
            public void setValue(Object entity, Method method, Value<?> value) {
                // TODO: we need more than enums here
                final String enumName = value.asString();
                try {
                    method.invoke(entity, ColorFunctionEnum.valueOf(enumName).get());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
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
