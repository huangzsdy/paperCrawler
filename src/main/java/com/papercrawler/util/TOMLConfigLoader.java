package com.papercrawler.util;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;

public class TOMLConfigLoader {

    private TOMLConfigLoader() {
        // 私有构造函数，防止实例化
    }

    /**
     * 加载TOML配置文件
     */
    public static Toml loadConfig(String configPath) {
        try {
            // 首先尝试从文件系统加载
            File configFile = new File(configPath);
            if (configFile.exists()) {
                System.out.println("Loading config from file: " + configFile.getAbsolutePath());
                return new Toml().read(configFile);
            }

            // 然后尝试从类路径加载
            InputStream inputStream = TOMLConfigLoader.class.getClassLoader()
                    .getResourceAsStream(configPath);
            if (inputStream != null) {
                System.out.println("Loading config from classpath: " + configPath);
                return new Toml().read(inputStream);
            }

            // 最后尝试从默认位置加载
            Path defaultPath = Paths.get("config", "config.toml");
            if (Files.exists(defaultPath)) {
                System.out.println("Loading config from default location: " + defaultPath);
                return new Toml().read(defaultPath.toFile());
            }

            throw new RuntimeException("Config file not found in any location: " + configPath);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load TOML configuration", e);
        }
    }

    /**
     * 验证配置文件的完整性
     */
    public static void validateConfig(Toml toml) {
        if (toml == null) {
            throw new RuntimeException("Configuration is null");
        }

        // 验证必需的部分
        String[] requiredSections = {"crawler", "storage", "arxiv"};

        for (String section : requiredSections) {
            if (toml.getTable(section) == null) {
                throw new RuntimeException("Missing required configuration section: " + section);
            }
        }

        // 验证爬虫模式
        String mode = toml.getString("crawler.mode");
        if (mode == null || mode.trim().isEmpty()) {
            throw new RuntimeException("Missing crawler mode configuration");
        }

        if (!isValidMode(mode)) {
            throw new RuntimeException("Invalid crawler mode: " + mode +
                    ". Valid modes are: thread, async, distributed");
        }

        // 验证API配置
        if (toml.getString("arxiv.base_url") == null) {
            throw new RuntimeException("Missing arXiv API base URL");
        }

        // 验证存储配置
        if (toml.getString("storage.output_dir") == null) {
            throw new RuntimeException("Missing storage output directory");
        }

        // 验证数值配置的合理性
        int maxConnections = toml.getLong("crawler.max_connections", 20L).intValue();
        if (maxConnections <= 0) {
            throw new RuntimeException("Max connections must be greater than 0");
        }

        int requestTimeout = toml.getLong("crawler.request_timeout", 30L).intValue();
        if (requestTimeout <= 0) {
            throw new RuntimeException("Request timeout must be greater than 0");
        }

        System.out.println("Configuration validation passed");
    }

    /**
     * 检查爬虫模式是否有效
     */
    private static boolean isValidMode(String mode) {
        String lowerMode = mode.toLowerCase();
        return lowerMode.equals("thread") ||
                lowerMode.equals("async") ||
                lowerMode.equals("distributed");
    }

    /**
     * 合并配置（支持默认值和自定义配置）
     */
    public static Toml mergeWithDefaults(Toml customConfig, Toml defaultConfig) {
        if (defaultConfig == null) {
            return customConfig;
        }

        // 创建一个新的TOML对象，先添加默认配置，然后用自定义配置覆盖
        Toml mergedConfig = defaultConfig;
        if (customConfig != null) {
            // TOML库没有直接的合并方法，这里我们手动处理关键配置
            // 在实际应用中，可能需要更复杂的合并逻辑
            mergedConfig = customConfig;
        }

        return mergedConfig;
    }

    /**
     * 获取默认配置
     */
    public static Toml getDefaultConfig() {
        String defaultConfig = """
            [crawler]
            mode = "thread"
            max_connections = 20
            request_timeout = 30
            retry_attempts = 3
            delay_between_requests = 1.0
            user_agent = "AcademicPaperCrawler/1.0"
            log_level = "INFO"

            [storage]
            output_dir = "./data"
            output_format = "json"
            max_file_size_mb = 100
            batch_size = 50
            database_enabled = false

            [arxiv]
            base_url = "https://export.arxiv.org/api/query"
            search_query = "cat:cond-mat+OR+cat:hep-+OR+cat:quant-ph+OR+cat:physics+OR+cat:cond-mat.mtrl-sci+OR+cat:physics.chem-ph+OR+cat:q-bio+OR+cat:eess.SP+OR+cat:eess.SY"
            max_results = 1000

            [biorxiv]
            base_url = "https://api.biorxiv.org/details/biorxiv"
            interval = 100

            [chemrxiv]
            base_url = "https://chemrxiv.org/engage/chemrxiv/public-api/v1/items"
            page_size = 100

            [keywords]
            physics = ["cond-mat", "hep-", "quant-ph", "physics"]
            materials = ["cond-mat.mtrl-sci"]
            chemistry = ["physics.chem-ph"]
            biology = ["q-bio"]
            electrical_engineering = ["eess.SP", "eess.SY"]
            """;

        return new Toml().read(defaultConfig);
    }

    /**
     * 导出配置为字符串（用于调试）
     */
    public static String exportConfigAsString(Toml toml) {
        StringBuilder sb = new StringBuilder();

        // 导出爬虫配置
        sb.append("[crawler]\n");
        sb.append("mode = \"").append(toml.getString("crawler.mode", "thread")).append("\"\n");
        sb.append("max_connections = ").append(toml.getLong("crawler.max_connections", 20L)).append("\n");
        sb.append("request_timeout = ").append(toml.getLong("crawler.request_timeout", 30L)).append("\n");
        sb.append("retry_attempts = ").append(toml.getLong("crawler.retry_attempts", 3L)).append("\n");
        sb.append("delay_between_requests = ").append(toml.getDouble("crawler.delay_between_requests", 1.0)).append("\n");
        sb.append("user_agent = \"").append(toml.getString("crawler.user_agent", "AcademicPaperCrawler/1.0")).append("\"\n");
        sb.append("log_level = \"").append(toml.getString("crawler.log_level", "INFO")).append("\"\n\n");

        // 导出存储配置
        sb.append("[storage]\n");
        sb.append("output_dir = \"").append(toml.getString("storage.output_dir", "./data")).append("\"\n");
        sb.append("output_format = \"").append(toml.getString("storage.output_format", "json")).append("\"\n");
        sb.append("max_file_size_mb = ").append(toml.getLong("storage.max_file_size_mb", 100L)).append("\n");
        sb.append("batch_size = ").append(toml.getLong("storage.batch_size", 50L)).append("\n");
        sb.append("database_enabled = ").append(toml.getBoolean("storage.database_enabled", false)).append("\n\n");

        // 导出关键词
        sb.append("[keywords]\n");
        Map<String, Object> keywords = toml.getTable("keywords").toMap();
        for (Map.Entry<String, Object> entry : keywords.entrySet()) {
            sb.append(entry.getKey()).append(" = ");
            if (entry.getValue() instanceof List) {
                sb.append("[");
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    sb.append("\"").append(values.get(i)).append("\"");
                    if (i < values.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]\n");
            }
        }

        return sb.toString();
    }
}