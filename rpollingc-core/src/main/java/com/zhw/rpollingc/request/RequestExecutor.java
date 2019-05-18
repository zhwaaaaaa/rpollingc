package com.zhw.rpollingc.request;


import com.zhw.rpollingc.request.remote.Exception;

import java.util.function.Consumer;

public interface RequestExecutor {


    void submitRequest(String url, Object body, Consumer<String> onSuccess,
                       Consumer<Throwable> onError) throws Exception;

    void submitRequest(String url, Object body, Consumer<String> onSuccess,
                       Consumer<Throwable> onError, long waitingTimeout) throws Exception;

}
