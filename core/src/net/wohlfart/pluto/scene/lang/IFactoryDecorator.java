package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.Method;

public interface IFactoryDecorator<D> {

    D create();

    D setTypedValue(D entity, String key, Value<?> value);

    ISetterDecorator getSetterFor(String key);

    Method getMethodFor(String key);

}
