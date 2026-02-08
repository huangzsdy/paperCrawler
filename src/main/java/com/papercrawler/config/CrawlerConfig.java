package com.papercrawler.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class CrawlerConfig {
    private static Toml tomlConfig;

    public static void loadConfig(String configPath) {
        try {
            // 尝试从文件系统加载
            File configFile = new File(configPath);
            if (configFile.exists()) {
                tomlConfig = new Toml().read(configFile);
            } else {
                // 尝试从类路径加载
                InputStream inputStream = CrawlerConfig.class.getClassLoader()
                        .getResourceAsStream(configPath);
                if (inputStream != null) {
                    tomlConfig = new Toml().read(inputStream);
                } else {
                    throw new RuntimeException("Config file not found: " + configPath);
                }
            }

            // 验证配置
            validateConfig();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private static void validateConfig() {
        // 验证必需的配置项
        if (getCrawlerMode() == null || getCrawlerMode().isEmpty()) {
            throw new RuntimeException("Missing required configuration: crawler.mode");
        }

        if (getArxivBaseUrl() == null || getArxivBaseUrl().isEmpty()) {
            throw new RuntimeException("Missing required configuration: arxiv.base_url");
        }

        // 验证模式值
        String mode = getCrawlerMode().toLowerCase();
        if (!mode.equals("thread") && !mode.equals("async") && !mode.equals("distributed")) {
            throw new RuntimeException("Invalid crawler mode: " + mode);
        }
    }

    // 爬虫配置
    public static String getCrawlerMode() {
        return tomlConfig.getString("crawler.mode", "thread");
    }

    public static int getMaxConnections() {
        return tomlConfig.getLong("crawler.max_connections", 20L).intValue();
    }

    public static int getRequestTimeout() {
        return tomlConfig.getLong("crawler.request_timeout", 30L).intValue();
    }

    public static int getRetryAttempts() {
        return tomlConfig.getLong("crawler.retry_attempts", 3L).intValue();
    }

    public static double getDelayBetweenRequests() {
        return tomlConfig.getDouble("crawler.delay_between_requests", 1.0);
    }

    public static String getUserAgent() {
        return tomlConfig.getString("crawler.user_agent", "AcademicPaperCrawler/1.0");
    }

    public static String getLogLevel() {
        return tomlConfig.getString("crawler.log_level", "INFO");
    }

    // 存储配置
    public static String getOutputDir() {
        return tomlConfig.getString("storage.output_dir", "./data");
    }

    public static String getOutputFormat() {
        return tomlConfig.getString("storage.output_format", "json");
    }

    public static int getMaxFileSizeMB() {
        return tomlConfig.getLong("storage.max_file_size_mb", 100L).intValue();
    }

    public static int getBatchSize() {
        return tomlConfig.getLong("storage.batch_size", 50L).intValue();
    }

    public static boolean isDatabaseEnabled() {
        return tomlConfig.getBoolean("storage.database_enabled", false);
    }

    // arXiv配置
    public static String getArxivBaseUrl() {
        return tomlConfig.getString("arxiv.base_url");
    }

    public static String getArxivSearchQuery() {
        return tomlConfig.getString("arxiv.search_query");
    }

    public static int getArxivMaxResults() {
        return tomlConfig.getLong("arxiv.max_results", 1000L).intValue();
    }

    public static int getArxivStartIndex() {
        return tomlConfig.getLong("arxiv.start_index", 0L).intValue();
    }

    public static String getArxivSortBy() {
        return tomlConfig.getString("arxiv.sort_by", "submittedDate");
    }

    public static String getArxivSortOrder() {
        return tomlConfig.getString("arxiv.sort_order", "descending");
    }

    // bioRxiv配置
    public static String getBiorxivBaseUrl() {
        return tomlConfig.getString("biorxiv.base_url", "https://api.biorxiv.org/details/biorxiv");
    }

    public static int getBiorxivInterval() {
        return tomlConfig.getLong("biorxiv.interval", 100L).intValue();
    }

    public static String getBiorxivFormat() {
        return tomlConfig.getString("biorxiv.format", "json");
    }

    // ChemRxiv配置
    public static String getChemRxivBaseUrl() {
        return tomlConfig.getString("chemrxiv.base_url");
    }

    public static int getChemRxivPageSize() {
        return tomlConfig.getLong("chemrxiv.page_size", 100L).intValue();
    }

    public static String getChemRxivSort() {
        return tomlConfig.getString("chemrxiv.sort", "publishedDate");
    }

    // 关键词配置
    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> getKeywords() {
        return (Map<String, List<String>>) tomlConfig.getTable("keywords").toMap();
    }

    // 数据库配置
    public static String getDatabaseUrl() {
        return tomlConfig.getString("database.url", "jdbc:mysql://localhost:3306/papers");
    }

    public static String getDatabaseUsername() {
        return tomlConfig.getString("database.username", "crawler");
    }

    public static String getDatabasePassword() {
        return tomlConfig.getString("database.password", "password");
    }

    public static int getDatabasePoolSize() {
        return tomlConfig.getLong("database.pool_size", 10L).intValue();
    }

    // 代理配置
    public static boolean isProxyEnabled() {
        return tomlConfig.getBoolean("proxy.enabled", false);
    }

    public static String getProxyHost() {
        return tomlConfig.getString("proxy.host", "");
    }

    public static int getProxyPort() {
        return tomlConfig.getLong("proxy.port", 8080L).intValue();
    }

    public static String getProxyUsername() {
        return tomlConfig.getString("proxy.username", "");
    }

    public static String getProxyPassword() {
        return tomlConfig.getString("proxy.password", "");
    }
}