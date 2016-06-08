package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.util.ISupplier;

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
        LOGGER.debug("<put> values for " + key + " method: " + method + " setter: " + setter);
        setters.put(key, setter);
        methods.put(key, method);
    }

    @Override
    public D create() {
        return supplier.get();
    }

    @Override // TODO: make private after json removal
    public ISetterDecorator getSetterFor(String key) {
        return setters.get(key);
    }

    @Override // TODO: make private after json removal
    public Method getMethodFor(String key) {
        return methods.get(key);
    }

    @Override
    public D setTypedValue(D entity, String key, Value<?> value) {
        final ISetterDecorator setter = getSetterFor(key);
        final Method method = getMethodFor(key);
        if (setter == null) {
            throw new EvalException("no setter for " + key);
        }
        setter.setValue(entity, method, value);
        return entity;
    }

}
