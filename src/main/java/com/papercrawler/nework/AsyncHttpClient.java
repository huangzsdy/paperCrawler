package com.papercrawler.network;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Optional;

public class AsyncHttpClient {
    private final HttpClient httpClient;
    private final ExecutorService executorService;

    public AsyncHttpClient() {
        this.httpClient = new HttpClient();
        this.executorService = Executors.newCachedThreadPool();
    }

    public AsyncHttpClient(int threadPoolSize) {
        this.httpClient = new HttpClient();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * 异步执行GET请求
     */
    public CompletableFuture<Optional<String>> getAsync(String url) {
        return CompletableFuture.supplyAsync(() -> httpClient.get(url), executorService);
    }

    /**
     * 异步执行POST请求
     */
    public CompletableFuture<Optional<String>> postAsync(String url, String body) {
        return CompletableFuture.supplyAsync(() -> httpClient.post(url, body), executorService);
    }

    /**
     * 异步批量获取多个URL
     */
    public CompletableFuture<Optional<String>[]> getAllAsync(String... urls) {
        @SuppressWarnings("unchecked")
        CompletableFuture<Optional<String>>[] futures = new CompletableFuture[urls.length];

        for (int i = 0; i < urls.length; i++) {
            futures[i] = getAsync(urls[i]);
        }

        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    @SuppressWarnings("unchecked")
                    Optional<String>[] results = new Optional[urls.length];
                    for (int i = 0; i < urls.length; i++) {
                        results[i] = futures[i].join();
                    }
                    return results;
                });
    }

    /**
     * 异步检查URL是否可达
     */
    public CompletableFuture<Boolean> isUrlReachableAsync(String url) {
        return CompletableFuture.supplyAsync(() -> httpClient.isUrlReachable(url), executorService);
    }

    /**
     * 异步获取响应头
     */
    public CompletableFuture<Optional<String>> getHeaderAsync(String url, String headerName) {
        return CompletableFuture.supplyAsync(() -> httpClient.getHeader(url, headerName), executorService);
    }

    /**
     * 带回调的异步GET请求
     */
    public void getWithCallback(String url, ResponseCallback callback) {
        CompletableFuture.runAsync(() -> {
            Optional<String> response = httpClient.get(url);
            if (callback != null) {
                callback.onResponse(response);
            }
        }, executorService);
    }

    /**
     * 关闭异步客户端
     */
    public void close() {
        try {
            httpClient.close();
            executorService.shutdown();
        } catch (Exception e) {
            System.err.println("Error closing async HTTP client: " + e.getMessage());
        }
    }

    /**
     * 回调接口
     */
    @FunctionalInterface
    public interface ResponseCallback {
        void onResponse(Optional<String> response);
    }

    /**
     * 获取异步客户端状态信息
     */
    public String getStatus() {
        return String.format("Async HTTP Client - Executor: %s, %s",
                executorService.getClass().getSimpleName(),
                httpClient.getStatus());
    }
}