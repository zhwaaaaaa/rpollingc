package com.zhw.rpollingc.template;

import com.fasterxml.jackson.core.type.TypeReference;

public class TemplateMeta {
    private String templateUrl;
    private RequestResolver requestResolver;
    private TypeReference<?> resultClass;
    private boolean async;
    private int timeoutSecond;

    public TemplateMeta() {
    }

    public TemplateMeta(String templateUrl,
                        RequestResolver requestResolver,
                        TypeReference<?> resultClass,
                        boolean async,
                        int timeoutSecond) {
        this.templateUrl = templateUrl;
        this.requestResolver = requestResolver;
        this.resultClass = resultClass;
        this.async = async;
        this.timeoutSecond = timeoutSecond;
        if (timeoutSecond <= 0) {
            throw new IllegalArgumentException("timeoutSecond must great than 0");
        }
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public RequestResolver getRequestResolver() {
        return requestResolver;
    }

    public TypeReference<?> getResultClass() {
        return resultClass;
    }

    public boolean isAsync() {
        return async;
    }

    public int getTimeoutSecond() {
        return timeoutSecond;
    }
}
