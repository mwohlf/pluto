package net.wohlfart.pluto.scene.lang;

import java.lang.reflect.Method;

/*
 *  setter is responsible for convertig the value object into whatever the method is able to consume
 */
public interface ISetterDecorator {

    void setValue(Object entity, Method method, Value<?> value) throws Exception;

}
