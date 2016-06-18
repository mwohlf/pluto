package net.wohlfart.pluto.scene.lang;

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
import net.wohlfart.pluto.ai.btree.Parallel;
import net.wohlfart.pluto.ai.btree.Sequential;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.util.ISupplier;

// see: http://jakubdziworski.github.io/java/2016/04/01/antlr_visitor_vs_listener.html
public class BehaviorFactoryDecorators {
    private static final Logger LOGGER = LoggerService.forClass(BehaviorFactoryDecorators.class);

    private final Map<String, IFactoryDecorator<IBehavior>> FACTORY_DECORATORS = new HashMap<>();

    {
        newFactoryDecorator(new ISupplier<SpinBehavior>() {
            @Override
            public SpinBehavior get() {
                return new SpinBehavior();
            }
        });
        newFactoryDecorator(new ISupplier<MoveBehavior>() {
            @Override
            public MoveBehavior get() {
                return new MoveBehavior();
            }
        });
        newFactoryDecorator(new ISupplier<MoveToBehavior>() {
            @Override
            public MoveToBehavior get() {
                return new MoveToBehavior();
            }
        });
        newFactoryDecorator(new ISupplier<SeekBehavior>() {
            @Override
            public SeekBehavior get() {
                return new SeekBehavior();
            }
        });
        newFactoryDecorator(new ISupplier<FleeBehavior>() {
            @Override
            public FleeBehavior get() {
                return new FleeBehavior();
            }
        });
        newFactoryDecorator(new ISupplier<Parallel>() {
            @Override
            public Parallel get() {
                return new Parallel();
            }
        });
        newFactoryDecorator(new ISupplier<Sequential>() {
            @Override
            public Sequential get() {
                return new Sequential();
            }
        });

    }

    // pre-read the setter methods of the IEntityCommand
    @SuppressWarnings("unchecked")
    private <D extends IBehavior> void newFactoryDecorator(ISupplier<D> supplier) {
        final FactoryDecoratorImpl<D> factoryDecorator = new FactoryDecoratorImpl<>(supplier);
        final Class<?> clazz = supplier.get().getClass();
        // setup the factory decorator
        for (final Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(EntityProperty.class)) {
                final EntityProperty methodAnnotation = method.getAnnotation(EntityProperty.class);
                final Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException(
                            "more than on parameter is not yet supported for method " + method);
                }
                factoryDecorator.put(methodAnnotation.name(), ValueConverters.get(methodAnnotation.type()), method);
            }
        }

        final EntityElement classAnnotation = clazz.getAnnotation(EntityElement.class);
        final String type = classAnnotation.type();
        if (FACTORY_DECORATORS.containsKey(type)) {
            LOGGER.error("<newFactoryDecorator> entity type already used: " + type);
        } else {
            FACTORY_DECORATORS.put(type, (IFactoryDecorator<IBehavior>) factoryDecorator);
        }
    }

    public IFactoryDecorator<IBehavior> get(String type) {
        return FACTORY_DECORATORS.get(type);
    }

}
