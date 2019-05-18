package com.zhw.rpollingc.template;


import com.zhw.rpollingc.utils.AnnotationUtils;
import com.zhw.rpollingc.utils.ArrayUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.util.internal.StringUtil;
import io.reactivex.Observable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RepositoryAnalyzer {

    private ClassLoader classLoader;
    private Class<? extends RequestResolver> defaultRequestResolverClass = DefaultRequestResolver.class;

    public RepositoryAnalyzer() {
    }

    public RepositoryAnalyzer(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public RepositoryAnalyzer(ClassLoader classLoader,
                              Class<? extends RequestResolver> defaultRequestResolverClass) {
        this.classLoader = classLoader;
        this.defaultRequestResolverClass = defaultRequestResolverClass;
    }

    public Map<Method, TemplateMeta> analyze(Class<?> repositoryClass) throws IllegalStateException {

        if (!repositoryClass.isInterface()) {
            throw new IllegalStateException(repositoryClass.getName() + "is not interface");
        }
        Method[] methods = repositoryClass.getDeclaredMethods();
        if (ArrayUtils.isEmpty(methods)) {
            throw new IllegalStateException(repositoryClass.getName() + "require methods");
        }

        AttrTemplate repository = AnnotationUtils.findAnnotation(repositoryClass, AttrTemplate.class);

        Map<Method, TemplateMeta> methodTemplateMetaMap = new HashMap<>();
        for (Method method : methods) {
            AttrTemplate template = AnnotationUtils.findAnnotation(method, AttrTemplate.class);
            String url = analyzeUrl(repositoryClass, repository, method, template);
            RequestResolver requestResolver = analyzeBodyResolver(repositoryClass, repository, method, template);
            TypeReference<?> resultClass = analyzeResultType(repositoryClass, repository, method, template);
            boolean async = analyzeAsync(repositoryClass, repository, method, template);
            int timeoutSecond = analyzeTimeoutSecond(repositoryClass, repository, method, template);
            TemplateMeta templateMeta = new TemplateMeta(url, requestResolver, resultClass, async, timeoutSecond);
            methodTemplateMetaMap.put(method, templateMeta);
        }
        return methodTemplateMetaMap;
    }

    private boolean analyzeAsync(Class<?> repositoryClass, AttrTemplate repository, Method method, AttrTemplate template) {
        Class<?> returnType = method.getReturnType();
        return returnType.equals(Observable.class);
    }

    private int analyzeTimeoutSecond(Class<?> repositoryClass, AttrTemplate repository, Method method, AttrTemplate template) {
        if (template != null) {
            return template.timeoutSecond();
        }
        if (repository != null) {
            return repository.timeoutSecond();
        }
        return 1800;
    }

    private TypeReference<?> analyzeResultType(Class<?> repositoryClass, AttrTemplate repository, Method method, AttrTemplate template) {
        final Type returnType = method.getGenericReturnType();
        if (analyzeAsync(repositoryClass, repository, method, template)) {
            return new TypeReference<Object>() {
                @Override
                public Type getType() {
                    if (returnType instanceof ParameterizedType) {
                        return ((ParameterizedType) returnType).getActualTypeArguments()[0];
                    }
                    return returnType;
                }
            };
        }
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return returnType;
            }
        };

    }

    private RequestResolver analyzeBodyResolver(Class<?> repositoryClass, AttrTemplate repository, Method method,
                                                AttrTemplate template) {
        Class<? extends RequestResolver> defaultRequestResolverClass = this.defaultRequestResolverClass;
        if (template != null) {
            defaultRequestResolverClass = template.requestResolverClass();
        } else if (repository != null) {
            defaultRequestResolverClass = repository.requestResolverClass();
        }

        try {
            RequestResolver requestResolver = defaultRequestResolverClass.newInstance();
            requestResolver.init(repositoryClass, repository, method, template);
            return requestResolver;
        } catch (Exception e) {
            throw new RuntimeException(defaultRequestResolverClass.getName() + " require no argument constructor", e);
        }
    }

    private String analyzeUrl(Class<?> repositoryClass, AttrTemplate repository, Method method, AttrTemplate template) {

        String url = StringUtil.EMPTY_STRING;
        if (repository != null) {
            url += repository.url().trim();
        }
        if (template != null) {
            url += template.url();
        }
        if (url.equals(StringUtil.EMPTY_STRING)) {
            throw new IllegalStateException(repositoryClass.getName() + "." + method.getName() + "requireUrl");
        }

        return url;
    }

    public Map<Method, TemplateMeta> analyze(String repositoryClass) throws IllegalStateException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            return analyze(classLoader.loadClass(repositoryClass));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


}
