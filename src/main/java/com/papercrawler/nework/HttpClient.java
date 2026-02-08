package com.papercrawler.network;

import com.papercrawler.config.CrawlerConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HttpClient implements AutoCloseable {
    private final CloseableHttpClient httpClient;
    private final RequestConfig requestConfig;

    public HttpClient() {
        this.requestConfig = RequestConfig.custom()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(CrawlerConfig.getRequestTimeout()))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(CrawlerConfig.getRequestTimeout()))
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(CrawlerConfig.getRequestTimeout()))
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(CrawlerConfig.getUserAgent())
                .setMaxConnTotal(CrawlerConfig.getMaxConnections())
                .setMaxConnPerRoute(CrawlerConfig.getMaxConnections())
                .build();
    }

    /**
     * 执行GET请求
     */
    public Optional<String> get(String url) {
        return executeWithRetry(url, 0);
    }

    /**
     * 执行POST请求
     */
    public Optional<String> post(String url, String body) {
        return executePostWithRetry(url, body, 0);
    }

    /**
     * 带重试机制的GET请求
     */
    private Optional<String> executeWithRetry(String url, int retryCount) {
        if (retryCount >= CrawlerConfig.getRetryAttempts()) {
            System.err.println("Max retry attempts reached for URL: " + url);
            return Optional.empty();
        }

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);

        // 设置请求头
        httpGet.setHeader("Accept", "application/json,application/xml,text/xml,text/html");
        httpGet.setHeader("Accept-Charset", "UTF-8");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);

                // 添加请求延迟
                Thread.sleep((long) (CrawlerConfig.getDelayBetweenRequests() * 1000));

                return Optional.of(responseBody);
            } else if (statusCode >= 500 && statusCode < 600) {
                // 服务器错误，重试
                System.err.println("Server error " + statusCode + " for URL: " + url + ", retrying...");
                Thread.sleep(calculateBackoffDelay(retryCount));
                return executeWithRetry(url, retryCount + 1);
            } else {
                System.err.println("HTTP request failed with status code: " + statusCode + " for URL: " + url);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }

            System.err.println("Error executing request for URL: " + url + " - " + e.getMessage());

            // 网络错误，重试
            try {
                Thread.sleep(calculateBackoffDelay(retryCount));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }

            return executeWithRetry(url, retryCount + 1);
        }
    }

    /**
     * 带重试机制的POST请求
     */
    private Optional<String> executePostWithRetry(String url, String body, int retryCount) {
        if (retryCount >= CrawlerConfig.getRetryAttempts()) {
            System.err.println("Max retry attempts reached for URL: " + url);
            return Optional.empty();
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        // 设置请求头和请求体
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");

        if (body != null && !body.isEmpty()) {
            httpPost.setEntity(new StringEntity(body, "UTF-8"));
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200 || statusCode == 201) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);

                Thread.sleep((long) (CrawlerConfig.getDelayBetweenRequests() * 1000));
                return Optional.of(responseBody);
            } else if (statusCode >= 500 && statusCode < 600) {
                System.err.println("Server error " + statusCode + " for URL: " + url + ", retrying...");
                Thread.sleep(calculateBackoffDelay(retryCount));
                return executePostWithRetry(url, body, retryCount + 1);
            } else {
                System.err.println("HTTP POST failed with status code: " + statusCode + " for URL: " + url);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }

            System.err.println("Error executing POST request for URL: " + url + " - " + e.getMessage());

            try {
                Thread.sleep(calculateBackoffDelay(retryCount));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }

            return executePostWithRetry(url, body, retryCount + 1);
        }
    }

    /**
     * 计算指数退避延迟
     */
    private long calculateBackoffDelay(int retryCount) {
        long baseDelay = 1000; // 1秒基础延迟
        long maxDelay = 30000; // 30秒最大延迟
        long delay = baseDelay * (1L << retryCount); // 指数退避: 1s, 2s, 4s, 8s...
        return Math.min(delay, maxDelay);
    }

    /**
     * 检查URL是否可达
     */
    public boolean isUrlReachable(String url) {
        HttpGet httpHead = new HttpGet(url);
        httpHead.setConfig(requestConfig);

        try (CloseableHttpResponse response = httpClient.execute(httpHead)) {
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == 200 || statusCode == 404; // 404也算可达，只是资源不存在
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取响应头信息
     */
    public Optional<String> getHeader(String url, String headerName) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String headerValue = response.getFirstHeader(headerName).getValue();
            return Optional.ofNullable(headerValue);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * 获取HTTP客户端状态信息
     */
    public String getStatus() {
        return String.format("HTTP Client - Max Connections: %d, Timeout: %d seconds",
                CrawlerConfig.getMaxConnections(), CrawlerConfig.getRequestTimeout());
    }
}