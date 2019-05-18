package com.zhw.rpollingc.repository;

import com.zhw.rpollingc.RpollingcTemplateConfiguration;
import com.zhw.rpollingc.template.TemplateNamespace;
import com.zhw.rpollingc.request.RequestBuilder;

import java.lang.reflect.Proxy;
import java.util.List;

public class RpollingcRepositoryFactory {

    private RpollingcTemplateConfiguration configuration;
    private ClassLoader classLoader;
    private RequestBuilder RequestBuilder;

    public RpollingcRepositoryFactory() {
    }

    public RpollingcRepositoryFactory(RpollingcTemplateConfiguration configuration,
                                       RequestBuilder RequestBuilder) {
        this(configuration, null, RequestBuilder);
    }

    public RpollingcRepositoryFactory(RpollingcTemplateConfiguration configuration, ClassLoader classLoader) {
        this.configuration = configuration;
        this.classLoader = classLoader;
    }

    public RpollingcRepositoryFactory(RpollingcTemplateConfiguration configuration,
                                       ClassLoader classLoader,
                                       RequestBuilder RequestBuilder) {
        this.configuration = configuration;
        this.classLoader = classLoader;
        this.RequestBuilder = RequestBuilder;
    }

    public RpollingcTemplateConfiguration getConfiguration() {
        return configuration;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public RequestBuilder getRequestBuilder() {
        return RequestBuilder;
    }

    public void setRequestBuilder(RequestBuilder RequestBuilder) {
        this.RequestBuilder = RequestBuilder;
    }

    public void setConfiguration(RpollingcTemplateConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @SuppressWarnings("unchecked")
    public <T> T createRepository(Class<T> repositoryClass) {
        TemplateNamespace namespace = configuration.getTemplateNamespace(repositoryClass);
        List<RpollingcInterceptor> interceptors = configuration.getInterceptors();
        ClassLoader cl = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        ProxyRepositoryHandler handler = new ProxyRepositoryHandler(repositoryClass,
                namespace,
                interceptors,
                RequestBuilder);
        return (T) Proxy.newProxyInstance(cl, new Class[]{repositoryClass}, handler);
    }


}
