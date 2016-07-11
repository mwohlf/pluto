package net.wohlfart.pluto.scene.lang;

public interface IFactoryDecorator<D> {

    D create();

    D setTypedValue(D entity, String key, Value<?> value) throws Exception;

}
