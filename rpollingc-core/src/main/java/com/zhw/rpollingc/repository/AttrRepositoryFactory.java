package com.zhw.rpollingc.repository;

import com.zhw.rpollingc.AttrTemplateConfiguration;
import com.zhw.rpollingc.template.TemplateNamespace;
import com.zhw.rpollingc.request.RequestBuilder;

import java.lang.reflect.Proxy;
import java.util.List;

public class AttrRepositoryFactory {

    private AttrTemplateConfiguration configuration;
    private ClassLoader classLoader;
    private RequestBuilder RequestBuilder;

    public AttrRepositoryFactory() {
    }

    public AttrRepositoryFactory(AttrTemplateConfiguration configuration,
                                       RequestBuilder RequestBuilder) {
        this(configuration, null, RequestBuilder);
    }

    public AttrRepositoryFactory(AttrTemplateConfiguration configuration, ClassLoader classLoader) {
        this.configuration = configuration;
        this.classLoader = classLoader;
    }

    public AttrRepositoryFactory(AttrTemplateConfiguration configuration,
                                       ClassLoader classLoader,
                                       RequestBuilder RequestBuilder) {
        this.configuration = configuration;
        this.classLoader = classLoader;
        this.RequestBuilder = RequestBuilder;
    }

    public AttrTemplateConfiguration getConfiguration() {
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

    public void setConfiguration(AttrTemplateConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @SuppressWarnings("unchecked")
    public <T> T createRepository(Class<T> repositoryClass) {
        TemplateNamespace namespace = configuration.getTemplateNamespace(repositoryClass);
        List<AttrInterceptor> interceptors = configuration.getInterceptors();
        ClassLoader cl = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        ProxyRepositoryHandler handler = new ProxyRepositoryHandler(repositoryClass,
                namespace,
                interceptors,
                RequestBuilder);
        return (T) Proxy.newProxyInstance(cl, new Class[]{repositoryClass}, handler);
    }


}
