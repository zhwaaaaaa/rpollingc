package com.zhw.rpollingc.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtils {
    private AnnotationUtils() {
    }


    public static <T extends Annotation> T findAnnotation(Class<?> repositoryClass,
                                                          Class<T> annotation) {
        return repositoryClass.getDeclaredAnnotation(annotation);
    }

    public static <T extends Annotation> T findAnnotation(Method method,
                                                          Class<T> annotation) {
        return method.getDeclaredAnnotation(annotation);
    }
}
