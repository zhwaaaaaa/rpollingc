package com.zhw.rpollingc.template;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DefaultRequestResolver implements RequestResolver {

    public static final Object EMPTY_BODY = new Object();

    public int parameterNum;
    private SortedMap<Integer, String> paramNames;


    @Override
    public void init(Class<?> repositoryClass,
                     RpollingcTemplate repository,
                     Method method, RpollingcTemplate template) {
        int parameterNum = method.getParameterCount();
        if (parameterNum == 0) {
            this.parameterNum = 0;
        } else {
            final Annotation[][] paramAnnotations = method.getParameterAnnotations();
            final SortedMap<Integer, String> map = new TreeMap<>();
            for (int paramIndex = 0; paramIndex < parameterNum; paramIndex++) {
                String name = null;
                boolean hasParamAnnotation = false;

                for (Annotation annotation : paramAnnotations[paramIndex]) {
                    if (annotation instanceof BodyKey) {
                        hasParamAnnotation = true;
                        name = ((BodyKey) annotation).value();
                        break;
                    }
                }
                if (!hasParamAnnotation && parameterNum == 1) {
                    this.parameterNum = 1;
                    return;
                }
                if (name == null) {
                    name = String.valueOf(paramIndex);
                }
                map.put(paramIndex, name);
            }
            paramNames = map;
            this.parameterNum = parameterNum;
        }

    }

    @Override
    public Object resolveBody(Object[] args) {
        switch (parameterNum) {
            case 0:
                return EMPTY_BODY;
            case 1:
                return args[0];
            default:
                return getMultiParameter(args);
        }
    }

    private Map<String, Object> getMultiParameter(Object[] args) {
        Map<String, Object> ret = new HashMap<>(parameterNum);
        for (Map.Entry<Integer, String> entry : paramNames.entrySet()) {
            ret.put(entry.getValue(), args[entry.getKey()]);
        }
        return ret;
    }

    @Override
    public String resolveUrl(String templateUrl, Object[] args) {
        return templateUrl;
    }
}
