package com.zhw.rpollingc.repository;

import com.zhw.rpollingc.template.RequestResolver;
import com.zhw.rpollingc.template.TemplateMeta;
import com.zhw.rpollingc.template.TemplateNamespace;
import com.zhw.rpollingc.request.Request;
import com.zhw.rpollingc.request.RequestBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ProxyRepositoryHandler implements InvocationHandler {

    private Class<?> repositoryClass;
    private TemplateNamespace namespace;
    private List<RpollingcInterceptor> interceptors;
    private RequestBuilder RequestBuilder;

    public ProxyRepositoryHandler(Class<?> repositoryClass,
                                        TemplateNamespace namespace,
                                        List<RpollingcInterceptor> interceptors,
                                        RequestBuilder RequestBuilder) {
        this.repositoryClass = repositoryClass;
        this.namespace = namespace;
        this.interceptors = interceptors;
        this.RequestBuilder = RequestBuilder;
    }

    /**
     * invoke for repository toString()
     * @return string
     */
    @Override
    public String toString() {
        return "JDKProxy{" +
                "repositoryClass=" + repositoryClass.getName() +
                ", namespace=" + namespace +
                ", interceptors=" + interceptors +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyRepositoryHandler)) return false;
        ProxyRepositoryHandler that = (ProxyRepositoryHandler) o;
        return Objects.equals(repositoryClass, that.repositoryClass) &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(interceptors, that.interceptors) &&
                Objects.equals(RequestBuilder, that.RequestBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryClass, namespace, interceptors, RequestBuilder);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        TemplateMeta meta = namespace.getMeta(method);
        if (meta != null) {
            RpollingcExecution execution = new ChainRpollingcExecution(interceptors, meta);
            String templateUrl = meta.getTemplateUrl();
            RequestResolver requestResolver = meta.getRequestResolver();
            Object body = requestResolver.resolveBody(args);
            String url = requestResolver.resolveUrl(templateUrl, args);
            return execution.invoke(url, body);
        }
        // not related to interface declare method，maybe related to object.invoke to this
        return method.invoke(this, args);
    }

    private class ChainRpollingcExecution implements RpollingcExecution {

        private Iterator<RpollingcInterceptor> interceptorIterator;
        private TemplateMeta templateMeta;

        public ChainRpollingcExecution(List<RpollingcInterceptor> interceptors,
                                        TemplateMeta templateMeta) {
            this.interceptorIterator = interceptors.iterator();
            this.templateMeta = templateMeta;
        }

        @Override
        public boolean isAsync() {
            return templateMeta.isAsync();
        }

        @Override
        public Object invoke(String url, Object body) throws Throwable {
            if (interceptorIterator.hasNext()) {
                return interceptorIterator.next().intercept(this, url, body);
            } else {
                Request request = RequestBuilder
                        .build()
                        .url(url)
                        .body(body)
                        .timeout(templateMeta.getTimeoutSecond());

                if (isAsync()) {
                    return request.sendAsync(templateMeta.getResultClass());
                }
                return request.send(templateMeta.getResultClass());
            }
        }

    }

}
