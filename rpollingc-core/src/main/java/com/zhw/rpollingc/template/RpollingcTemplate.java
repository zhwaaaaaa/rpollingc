package com.zhw.rpollingc.template;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpollingcTemplate {

    String url() default "";

    int timeoutSecond() default 1800;

    Class<? extends RequestResolver> requestResolverClass() default DefaultRequestResolver.class;
    
}
