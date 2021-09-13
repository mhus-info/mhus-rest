package de.mhus.rest.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.mhus.rest.core.transform.PojoTransformer;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestTransformer {

    Class<?>[] value() default {PojoTransformer.class};

}
