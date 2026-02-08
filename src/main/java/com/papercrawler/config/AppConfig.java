package com.papercrawler.config;

import java.util.List;
import java.util.Map;

/**
 * 应用程序配置类，提供更便捷的配置访问方法
 */
public class AppConfig {

    // 私有构造函数，防止实例化
    private AppConfig() {}

    // 爬虫配置
    public static String getMode() {
        return CrawlerConfig.getCrawlerMode();
    }

    public static boolean isThreadMode() {
        return "thread".equalsIgnoreCase(getMode());
    }

    public static boolean isAsyncMode() {
        return "async".equalsIgnoreCase(getMode());
    }

    public static boolean isDistributedMode() {
        return "distributed".equalsIgnoreCase(getMode());
    }

    public static int getMaxConnections() {
        return CrawlerConfig.getMaxConnections();
    }

    public static int getRequestTimeout() {
        return CrawlerConfig.getRequestTimeout();
    }

    public static int getRetryAttempts() {
        return CrawlerConfig.getRetryAttempts();
    }

    public static double getDelayBetweenRequests() {
        return CrawlerConfig.getDelayBetweenRequests();
    }

    public static String getUserAgent() {
        return CrawlerConfig.getUserAgent();
    }

    public static String getLogLevel() {
        return CrawlerConfig.getLogLevel();
    }

    // 存储配置
    public static String getOutputDir() {
        return CrawlerConfig.getOutputDir();
    }

    public static String getOutputFormat() {
        return CrawlerConfig.getOutputFormat();
    }

    public static int getMaxFileSizeMB() {
        return CrawlerConfig.getMaxFileSizeMB();
    }

    public static int getBatchSize() {
        return CrawlerConfig.getBatchSize();
    }

    public static boolean isDatabaseEnabled() {
        return CrawlerConfig.isDatabaseEnabled();
    }

    // arXiv配置
    public static String getArxivBaseUrl() {
        return CrawlerConfig.getArxivBaseUrl();
    }

    public static String getArxivSearchQuery() {
        return CrawlerConfig.getArxivSearchQuery();
    }

    public static int getArxivMaxResults() {
        return CrawlerConfig.getArxivMaxResults();
    }

    // bioRxiv配置
    public static String getBiorxivBaseUrl() {
        return CrawlerConfig.getBiorxivBaseUrl();
    }

    public static int getBiorxivInterval() {
        return CrawlerConfig.getBiorxivInterval();
    }

    // ChemRxiv配置
    public static String getChemRxivBaseUrl() {
        return CrawlerConfig.getChemRxivBaseUrl();
    }

    public static int getChemRxivPageSize() {
        return CrawlerConfig.getChemRxivPageSize();
    }

    // 关键词配置
    public static Map<String, List<String>> getKeywords() {
        return CrawlerConfig.getKeywords();
    }

    public static List<String> getPhysicsKeywords() {
        return CrawlerConfig.getKeywords().getOrDefault("physics", List.of());
    }

    public static List<String> getMaterialsKeywords() {
        return CrawlerConfig.getKeywords().getOrDefault("materials", List.of());
    }

    public static List<String> getChemistryKeywords() {
        return CrawlerConfig.getKeywords().getOrDefault("chemistry", List.of());
    }

    public static List<String> getBiologyKeywords() {
        return CrawlerConfig.getKeywords().getOrDefault("biology", List.of());
    }

    public static List<String> getElectricalEngineeringKeywords() {
        return CrawlerConfig.getKeywords().getOrDefault("electrical_engineering", List.of());
    }

    // 数据库配置
    public static String getDatabaseUrl() {
        return CrawlerConfig.getDatabaseUrl();
    }

    public static String getDatabaseUsername() {
        return CrawlerConfig.getDatabaseUsername();
    }

    public static String getDatabasePassword() {
        return CrawlerConfig.getDatabasePassword();
    }

    public static int getDatabasePoolSize() {
        return CrawlerConfig.getDatabasePoolSize();
    }

    // 代理配置
    public static boolean isProxyEnabled() {
        return CrawlerConfig.isProxyEnabled();
    }

    public static String getProxyHost() {
        return CrawlerConfig.getProxyHost();
    }

    public static int getProxyPort() {
        return CrawlerConfig.getProxyPort();
    }

    public static String getProxyUsername() {
        return CrawlerConfig.getProxyUsername();
    }

    public static String getProxyPassword() {
        return CrawlerConfig.getProxyPassword();
    }

    /**
     * 验证所有必需的配置
     */
    public static void validateAll() {
        // 验证爬虫模式
        if (!isValidMode(getMode())) {
            throw new IllegalArgumentException("Invalid crawler mode: " + getMode());
        }

        // 验证连接数
        if (getMaxConnections() <= 0) {
            throw new IllegalArgumentException("Max connections must be greater than 0");
        }

        // 验证超时时间
        if (getRequestTimeout() <= 0) {
            throw new IllegalArgumentException("Request timeout must be greater than 0");
        }

        // 验证重试次数
        if (getRetryAttempts() < 0) {
            throw new IllegalArgumentException("Retry attempts cannot be negative");
        }

        // 验证API基础URL
        validateUrl(getArxivBaseUrl(), "arXiv base URL");
        validateUrl(getBiorxivBaseUrl(), "bioRxiv base URL");
        validateUrl(getChemRxivBaseUrl(), "ChemRxiv base URL");

        // 验证输出格式
        if (!isValidOutputFormat(getOutputFormat())) {
            throw new IllegalArgumentException("Invalid output format: " + getOutputFormat());
        }

        System.out.println("All configuration validated successfully");
    }

    private static boolean isValidMode(String mode) {
        return "thread".equalsIgnoreCase(mode) ||
                "async".equalsIgnoreCase(mode) ||
                "distributed".equalsIgnoreCase(mode);
    }

    private static boolean isValidOutputFormat(String format) {
        return "json".equalsIgnoreCase(format) ||
                "csv".equalsIgnoreCase(format) ||
                "xml".equalsIgnoreCase(format);
    }

    private static void validateUrl(String url, String name) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException(name + " must be a valid URL starting with http:// or https://");
        }
    }

    /**
     * 获取配置摘要
     */
    public static String getConfigSummary() {
        return String.format("""
            === Application Configuration Summary ===
            Mode: %s
            Max Connections: %d
            Request Timeout: %d seconds
            Retry Attempts: %d
            Delay Between Requests: %.1f seconds
            Output Directory: %s
            Output Format: %s
            arXiv Max Results: %d
            Keywords: Physics(%d), Materials(%d), Chemistry(%d), Biology(%d), EE(%d)
            Database Enabled: %s
            Proxy Enabled: %s
            =========================================
            """,
                getMode(),
                getMaxConnections(),
                getRequestTimeout(),
                getRetryAttempts(),
                getDelayBetweenRequests(),
                getOutputDir(),
                getOutputFormat(),
                getArxivMaxResults(),
                getPhysicsKeywords().size(),
                getMaterialsKeywords().size(),
                getChemistryKeywords().size(),
                getBiologyKeywords().size(),
                getElectricalEngineeringKeywords().size(),
                isDatabaseEnabled(),
                isProxyEnabled()
        );
    }
}