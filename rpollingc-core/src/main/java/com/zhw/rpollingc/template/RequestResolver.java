package com.zhw.rpollingc.template;

import java.lang.reflect.Method;

public interface RequestResolver {

    void init(Class<?> repositoryClass, AttrTemplate repository, Method method, AttrTemplate template);

    Object resolveBody(Object[] args);

    String resolveUrl(String templateUrl, Object[] args);
}
