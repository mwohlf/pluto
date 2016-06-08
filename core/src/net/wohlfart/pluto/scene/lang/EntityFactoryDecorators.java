package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

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
import net.wohlfart.pluto.shader.SkyboxCommand;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.util.ISupplier;

// see: http://jakubdziworski.github.io/java/2016/04/01/antlr_visitor_vs_listener.html
public class EntityFactoryDecorators {
    private static final Logger LOGGER = LoggerService.forClass(EntityFactoryDecorators.class);

    private final Map<String, IFactoryDecorator<IEntityCommand>> FACTORY_DECORATORS = new HashMap<>();

    {
        newFactoryDecorator(new ISupplier<CameraSetup>() {
            @Override
            public CameraSetup get() {
                return new CameraSetup();
            }
        });
        newFactoryDecorator(new ISupplier<SkyboxCommand>() {
            @Override
            public SkyboxCommand get() {
                return new SkyboxCommand();
            }
        });
        newFactoryDecorator(new ISupplier<LightCommand>() {
            @Override
            public LightCommand get() {
                return new LightCommand();
            }
        });
        newFactoryDecorator(new ISupplier<IcosphereCommand>() {
            @Override
            public IcosphereCommand get() {
                return new IcosphereCommand();
            }
        });
        newFactoryDecorator(new ISupplier<CubeCommand>() {
            @Override
            public CubeCommand get() {
                return new CubeCommand();
            }
        });
        newFactoryDecorator(new ISupplier<ObjectCommand>() {
            @Override
            public ObjectCommand get() {
                return new ObjectCommand();
            }
        });
        newFactoryDecorator(new ISupplier<WaypointCommand>() {
            @Override
            public WaypointCommand get() {
                return new WaypointCommand();
            }
        });
        newFactoryDecorator(new ISupplier<TriangleCommand>() {
            @Override
            public TriangleCommand get() {
                return new TriangleCommand();
            }
        });
        newFactoryDecorator(new ISupplier<QuadCommand>() {
            @Override
            public QuadCommand get() {
                return new QuadCommand();
            }
        });
        newFactoryDecorator(new ISupplier<RoamBodyCommand>() {
            @Override
            public RoamBodyCommand get() {
                return new RoamBodyCommand();
            }
        });
        newFactoryDecorator(new ISupplier<SmokeCommand>() {
            @Override
            public SmokeCommand get() {
                return new SmokeCommand();
            }
        });

    }

    // pre-read the setter methods of the IEntityCommand
    @SuppressWarnings("unchecked")
    private <D extends IEntityCommand> void newFactoryDecorator(ISupplier<D> supplier) {
        final FactoryDecoratorImpl<D> factoryDecorator = new FactoryDecoratorImpl<>(supplier);
        final Class<?> clazz = supplier.get().getClass();
        // setup the factory decorator
        for (final Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(EntityProperty.class)) {
                final EntityProperty methodAnnotation = method.getAnnotation(EntityProperty.class);
                final Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException("more than on parameter is not yet supported for method " + method);
                }
                factoryDecorator.put(methodAnnotation.name(), ValueConverters.get(methodAnnotation.type()), method);
            }
        }

        final EntityElement classAnnotation = clazz.getAnnotation(EntityElement.class);
        final String type = classAnnotation.type();
        if (FACTORY_DECORATORS.containsKey(type)) {
            LOGGER.error("<newFactoryDecorator> entity type already used: " + type);
        } else {
            FACTORY_DECORATORS.put(type, (FactoryDecoratorImpl<IEntityCommand>) factoryDecorator);
        }
    }

    public IFactoryDecorator<IEntityCommand> get(String type) {
        return FACTORY_DECORATORS.get(type);
    }

}
