package com.jfinal.ext.plugin.monogodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Collection {
    String name()

    default "";

    String desc() default "";

    Class<?> myType() default Object.class;
}
