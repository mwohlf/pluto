package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.Method;

public interface ISetterDecorator {

    void setValue(Object entity, Method method, Value<?> value);

}
