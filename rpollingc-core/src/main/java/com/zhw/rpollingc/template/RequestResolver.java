package com.zhw.rpollingc.template;

import java.lang.reflect.Method;

public interface RequestResolver {

    void init(Class<?> repositoryClass, RpollingcTemplate repository, Method method, RpollingcTemplate template);

    Object resolveBody(Object[] args);

    String resolveUrl(String templateUrl, Object[] args);
}
