package com.example.gbird;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private final int maxRetry;

    public RetryInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        int retryCount = 0;

        while (!response.isSuccessful() && retryCount < maxRetry) {
            retryCount++;
            response.close();
            response = chain.proceed(request);
        }

        return response;
    }
}

