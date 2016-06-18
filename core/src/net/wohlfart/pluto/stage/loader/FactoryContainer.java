package net.wohlfart.pluto.stage.loader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.ai.FleeBehavior;
import net.wohlfart.pluto.ai.MoveBehavior;
import net.wohlfart.pluto.ai.MoveToBehavior;
import net.wohlfart.pluto.ai.SeekBehavior;
import net.wohlfart.pluto.ai.SpinBehavior;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.entity.CameraSetup;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.effects.LightCommand;
import net.wohlfart.pluto.entity.effects.SmokeCommand;
import net.wohlfart.pluto.entity.fab.CubeCommand;
import net.wohlfart.pluto.entity.fab.IcosphereCommand;
import net.wohlfart.pluto.entity.fab.ObjectCommand;
import net.wohlfart.pluto.entity.fab.QuadCommand;
import net.wohlfart.pluto.entity.fab.TriangleCommand;
import net.wohlfart.pluto.entity.fab.WaypointCommand;
import net.wohlfart.pluto.entity.fab.roam.RoamBodyCommand;
import net.wohlfart.pluto.scene.lang.IFactoryDecorator;
import net.wohlfart.pluto.shader.SkyboxCommand;
import net.wohlfart.pluto.util.ISupplier;

public final class FactoryContainer {
    private static final Logger LOGGER = LoggerService.forClass(FactoryContainer.class);

    private static final Map<String, IFactoryDecorator<IEntityCommand>> ENTITY_BY_NAME = new HashMap<>();

    private static final Map<String, IFactoryDecorator<IBehavior>> BEHAVIOR_BY_NAME = new HashMap<>();

    static {
        // this should run off the render thread
        //
        FactoryContainer.registerCommand(new ISupplier<CameraSetup>() {
            @Override
            public CameraSetup get() {
                return new CameraSetup();
            }
        });

        FactoryContainer.registerCommand(new ISupplier<SkyboxCommand>() {
            @Override
            public SkyboxCommand get() {
                return new SkyboxCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<LightCommand>() {
            @Override
            public LightCommand get() {
                return new LightCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<IcosphereCommand>() {
            @Override
            public IcosphereCommand get() {
                return new IcosphereCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<CubeCommand>() {
            @Override
            public CubeCommand get() {
                return new CubeCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<ObjectCommand>() {
            @Override
            public ObjectCommand get() {
                return new ObjectCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<WaypointCommand>() {
            @Override
            public WaypointCommand get() {
                return new WaypointCommand();
            }
        });

        FactoryContainer.registerCommand(new ISupplier<TriangleCommand>() {
            @Override
            public TriangleCommand get() {
                return new TriangleCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<QuadCommand>() {
            @Override
            public QuadCommand get() {
                return new QuadCommand();
            }
        });

        FactoryContainer.registerCommand(new ISupplier<RoamBodyCommand>() {
            @Override
            public RoamBodyCommand get() {
                return new RoamBodyCommand();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<SmokeCommand>() {
            @Override
            public SmokeCommand get() {
                return new SmokeCommand();
            }
        });

        FactoryContainer.registerCommand(new ISupplier<SpinBehavior>() {
            @Override
            public SpinBehavior get() {
                return new SpinBehavior();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<MoveBehavior>() {
            @Override
            public MoveBehavior get() {
                return new MoveBehavior();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<MoveToBehavior>() {
            @Override
            public MoveToBehavior get() {
                return new MoveToBehavior();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<SeekBehavior>() {
            @Override
            public SeekBehavior get() {
                return new SeekBehavior();
            }
        });
        FactoryContainer.registerCommand(new ISupplier<FleeBehavior>() {
            @Override
            public FleeBehavior get() {
                return new FleeBehavior();
            }
        });
    }

    private FactoryContainer() {
    }

    public static IFactoryDecorator<IEntityCommand> getEntityCommand(String key) {
        return FactoryContainer.ENTITY_BY_NAME.get(key);
    }

    public static IFactoryDecorator<IBehavior> getBehavior(String key) {
        return FactoryContainer.BEHAVIOR_BY_NAME.get(key);
    }

    // pre-read the setter methods of the IEntityCommand
    @SuppressWarnings("unchecked")
    static <D> void registerCommand(ISupplier<D> supplier) {
        final FactoryDecoratorImpl<D> decorator = new FactoryDecoratorImpl<>(supplier);
        final Class<?> clazz = supplier.get().getClass();
        for (final Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(EntityProperty.class)) {
                final EntityProperty methodAnnotation = method.getAnnotation(EntityProperty.class);
                final Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException("more than on parameter is not yet supported for method " + method);
                }
                decorator.put(methodAnnotation.name(), null, method);
            }
        }

        final EntityElement classAnnotation = clazz.getAnnotation(EntityElement.class);
        final String jsonType = classAnnotation.type();
        if (IBehavior.class.isAssignableFrom(clazz)) {
            if (FactoryContainer.BEHAVIOR_BY_NAME.containsKey(jsonType)) {
                FactoryContainer.LOGGER.error("<registerCommand> behavior type already used: " + jsonType);
            } else {
                FactoryContainer.BEHAVIOR_BY_NAME.put(jsonType, (IFactoryDecorator<IBehavior>) decorator);
            }
        } else if (IEntityCommand.class.isAssignableFrom(clazz)) {
            if (FactoryContainer.ENTITY_BY_NAME.containsKey(jsonType)) {
                FactoryContainer.LOGGER.error("<registerCommand> entity type already used: " + jsonType);
            } else {
                FactoryContainer.ENTITY_BY_NAME.put(jsonType, (IFactoryDecorator<IEntityCommand>) decorator);
            }
        } else {
            FactoryContainer.LOGGER.error("<registerCommand> unknown type for class '" + clazz + "', ignoring");
        }
    }

}
