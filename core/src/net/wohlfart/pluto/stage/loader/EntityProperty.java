package net.wohlfart.pluto.stage.loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation for setting properties for creating entities
 * TODO: move to lang after json removal
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityProperty {

    String name();

    String type(); // hack

    String values() default "";

}
