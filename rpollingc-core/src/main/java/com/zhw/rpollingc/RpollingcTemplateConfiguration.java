package com.zhw.rpollingc;

import com.zhw.rpollingc.repository.RpollingcExecution;
import com.zhw.rpollingc.repository.RpollingcInterceptor;
import com.zhw.rpollingc.template.RepositoryAnalyzer;
import com.zhw.rpollingc.template.TemplateMeta;
import com.zhw.rpollingc.template.TemplateNamespace;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.reactivex.internal.functions.Functions;
import io.reactivex.Observable;

import java.lang.reflect.Method;
import java.util.*;

public class RpollingcTemplateConfiguration {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(RpollingcTemplateConfiguration.class);

    private static final class DebugInterceptor implements RpollingcInterceptor {

        @Override
        public Object intercept(RpollingcExecution execution, String url, Object body) throws Throwable {
            try {
                Object invoke = execution.invoke(url, body);
                if (execution.isAsync()) {
                    ((Observable) invoke).subscribe(Functions.emptyConsumer(), e -> logErr(url, e));
                }
                return invoke;
            } catch (Throwable e) {
                logErr(url, e);
                throw e;
            }
        }

        private void logErr(String url, Object e) {
            log.error("error Rpollingc url={}", url, e);
        }
    }

    private List<RpollingcInterceptor> interceptors;

    private Map<Class<?>, TemplateNamespace> namespaceMap;

    private RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer();

    public RpollingcTemplateConfiguration() {
        namespaceMap = new HashMap<>();
        interceptors = new ArrayList<>();
        String property = System.getProperty("rpollingc.debug");
        if ("true".equals(property)) {
            interceptors.add(new DebugInterceptor());
        }
    }

    public RpollingcTemplateConfiguration(RepositoryAnalyzer repositoryAnalyzer) {
        this.repositoryAnalyzer = repositoryAnalyzer;
    }

    public RpollingcTemplateConfiguration(List<RpollingcInterceptor> interceptors, Map<Class<?>, TemplateNamespace> namespaceMap) {
        this.interceptors = new ArrayList<>(interceptors);
        this.namespaceMap = new HashMap<>(namespaceMap);
    }

    public void addInterceptor(RpollingcInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<RpollingcInterceptor> getInterceptors() {
        return new ArrayList<>(interceptors);
    }

    public void registerRepository(Class<?> repositoryClass) {
        Map<Method, TemplateMeta> metaMap = repositoryAnalyzer.analyze(repositoryClass);
        namespaceMap.put(repositoryClass, new TemplateNamespace(repositoryClass, metaMap));
    }

    public TemplateNamespace getTemplateNamespace(Class<?> repositoryClass) {
        if (namespaceMap.containsKey(repositoryClass)) {
            return namespaceMap.get(repositoryClass);
        }
        throw new IllegalStateException(repositoryClass.getName() + " not registered");
    }


}
