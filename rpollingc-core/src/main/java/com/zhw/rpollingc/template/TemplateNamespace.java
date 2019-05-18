package com.zhw.rpollingc.template;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TemplateNamespace {

    private Class<?> repositoryClass;
    private Map<Method, TemplateMeta> metaMap;

    public TemplateNamespace(Class<?> repositoryClass, Map<Method, TemplateMeta> metaMap) {
        this.repositoryClass = repositoryClass;
        this.metaMap = new HashMap<>(metaMap);
    }

    public TemplateMeta getMeta(Method method) {
        return metaMap.get(method);
    }


    @Override
    public String toString() {
        return "{" +
                "repositoryClass=" + repositoryClass.getName() +
                ", metaMap=" + metaMap +
                '}';
    }
}
