package net.wohlfart.pluto.stage.loader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.scene.lang.IFactoryDecorator;
import net.wohlfart.pluto.scene.lang.ISetterDecorator;
import net.wohlfart.pluto.scene.lang.Value;
import net.wohlfart.pluto.util.ISupplier;

@Deprecated // remove when removing json...
public class FactoryDecoratorImpl<D> implements IFactoryDecorator<D> {
    private static final Logger LOGGER = LoggerService.forClass(FactoryDecoratorImpl.class);

    // the entity being wrapped
    private final ISupplier<D> supplier;

    // setter storage for all valid properties
    private final Map<String, ISetterDecorator> setters = new HashMap<>();

    private final Map<String, Method> methods = new HashMap<>();

    public FactoryDecoratorImpl(ISupplier<D> supplier) {
        this.supplier = supplier;

    }

    public void put(String key, ISetterDecorator setter, Method method) {
        if (setter == null || method == null) {
            LOGGER.debug("<put> null values for " + key + " method: " + method + " setter: " + setter + " ignoring");
            return;
        }
        setters.put(key, setter);
        methods.put(key, method);
    }

    @Override
    public D create() {
        return supplier.get();
    }

    @Override
    public ISetterDecorator getSetterFor(String key) {
        return setters.get(key);
    }

    @Override
    public Method getMethodFor(String key) {
        return methods.get(key);
    }

    @Override
    public D setTypedValue(D entity, String key, Value<?> value) {
        throw new GdxRuntimeException("");
    }

}
