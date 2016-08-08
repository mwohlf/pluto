package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wohlfart.pluto.util.ISupplier;

public class FactoryDecoratorImpl<D> implements IFactoryDecorator<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FactoryDecoratorImpl.class);

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
        LOGGER.debug("<put> values for " + key + " method: " + method + " setter: " + setter);
        setters.put(key, setter);
        methods.put(key, method);
    }

    @Override
    public D create() {
        return supplier.get();
    }

    @Override
    public D setTypedValue(D entity, String key, Value<?> value) throws Exception {
        final ISetterDecorator setter = setters.get(key);
        if (setter == null) {
            throw new EvalException("no setter for " + key);
        }
        final Method method = methods.get(key);
        if (method == null) {
            throw new EvalException("no method for " + key);
        }
        setter.setValue(entity, method, value);
        return entity;
    }

}
