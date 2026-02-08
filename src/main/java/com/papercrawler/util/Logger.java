package com.papercrawler.util;

import com.papercrawler.config.CrawlerConfig;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单的日志记录器，支持不同日志级别和文件输出
 */
public class Logger {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final String className;
    private final LogLevel logLevel;

    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        DEBUG(1),
        INFO(2),
        WARN(3),
        ERROR(4);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public static LogLevel fromString(String level) {
            if (level == null) {
                return INFO;
            }

            switch (level.toUpperCase()) {
                case "DEBUG": return DEBUG;
                case "INFO": return INFO;
                case "WARN": return WARN;
                case "ERROR": return ERROR;
                default: return INFO;
            }
        }
    }

    /**
     * 构造函数
     */
    private Logger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
        this.logLevel = LogLevel.fromString(CrawlerConfig.getLogLevel());
    }

    /**
     * 获取类对应的Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    /**
     * 记录DEBUG级别日志
     */
    public void debug(String message) {
        if (logLevel.getLevel() <= LogLevel.DEBUG.getLevel()) {
            log(LogLevel.DEBUG, message, null);
        }
    }

    /**
     * 记录DEBUG级别日志（带格式）
     */
    public void debug(String format, Object... args) {
        if (logLevel.getLevel() <= LogLevel.DEBUG.getLevel()) {
            log(LogLevel.DEBUG, String.format(format, args), null);
        }
    }

    /**
     * 记录INFO级别日志
     */
    public void info(String message) {
        if (logLevel.getLevel() <= LogLevel.INFO.getLevel()) {
            log(LogLevel.INFO, message, null);
        }
    }

    /**
     * 记录INFO级别日志（带格式）
     */
    public void info(String format, Object... args) {
        if (logLevel.getLevel() <= LogLevel.INFO.getLevel()) {
            log(LogLevel.INFO, String.format(format, args), null);
        }
    }

    /**
     * 记录WARN级别日志
     */
    public void warn(String message) {
        if (logLevel.getLevel() <= LogLevel.WARN.getLevel()) {
            log(LogLevel.WARN, message, null);
        }
    }

    /**
     * 记录WARN级别日志（带格式）
     */
    public void warn(String format, Object... args) {
        if (logLevel.getLevel() <= LogLevel.WARN.getLevel()) {
            log(LogLevel.WARN, String.format(format, args), null);
        }
    }

    /**
     * 记录ERROR级别日志
     */
    public void error(String message) {
        if (logLevel.getLevel() <= LogLevel.ERROR.getLevel()) {
            log(LogLevel.ERROR, message, null);
        }
    }

    /**
     * 记录ERROR级别日志（带异常）
     */
    public void error(String message, Throwable throwable) {
        if (logLevel.getLevel() <= LogLevel.ERROR.getLevel()) {
            log(LogLevel.ERROR, message, throwable);
        }
    }

    /**
     * 记录ERROR级别日志（带格式和异常）
     */
    public void error(Throwable throwable, String format, Object... args) {
        if (logLevel.getLevel() <= LogLevel.ERROR.getLevel()) {
            log(LogLevel.ERROR, String.format(format, args), throwable);
        }
    }

    /**
     * 内部日志记录方法
     */
    private void log(LogLevel level, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String threadName = Thread.currentThread().getName();

        // 构建日志消息
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format("[%s] [%s] [%s] [%s] %s",
                timestamp, level.name(), threadName, className, message));

        // 添加异常信息
        if (throwable != null) {
            logMessage.append("\n").append(formatThrowable(throwable));
        }

        // 输出到控制台
        System.out.println(logMessage.toString());
    }

    /**
     * 格式化异常信息
     */
    private String formatThrowable(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        // 递归处理cause
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            sb.append("Caused by: ").append(formatThrowable(cause));
        }

        return sb.toString();
    }

    /**
     * 性能测量工具
     */
    public Stopwatch startTimer() {
        return new Stopwatch();
    }

    /**
     * 性能测量类
     */
    public class Stopwatch {
        private final long startTime;

        private Stopwatch() {
            this.startTime = System.currentTimeMillis();
        }

        /**
         * 获取经过的时间（毫秒）
         */
        public long elapsedMillis() {
            return System.currentTimeMillis() - startTime;
        }

        /**
         * 记录经过的时间
         */
        public void logElapsed(String operation) {
            long elapsed = elapsedMillis();
            info("%s completed in %d ms", operation, elapsed);
        }

        /**
         * 记录经过的时间（带条件）
         */
        public void logElapsedIf(String operation, long threshold) {
            long elapsed = elapsedMillis();
            if (elapsed > threshold) {
                warn("%s took %d ms (threshold: %d ms)", operation, elapsed, threshold);
            } else {
                debug("%s completed in %d ms", operation, elapsed);
            }
        }
    }

    /**
     * 创建进度条
     */
    public ProgressBar createProgressBar(String name, int total) {
        return new ProgressBar(name, total, this);
    }

    /**
     * 进度条类
     */
    public static class ProgressBar {
        private final String name;
        private final int total;
        private final Logger logger;
        private int current = 0;
        private long startTime;

        public ProgressBar(String name, int total, Logger logger) {
            this.name = name;
            this.total = total;
            this.logger = logger;
            this.startTime = System.currentTimeMillis();
        }

        public void update() {
            update(1);
        }

        public void update(int increment) {
            current += increment;
            if (current > total) {
                current = total;
            }

            // 每10%或完成时记录进度
            int percent = (int) ((double) current / total * 100);
            if (percent % 10 == 0 || current == total) {
                long elapsed = System.currentTimeMillis() - startTime;
                double itemsPerSecond = current / (elapsed / 1000.0);

                logger.info("%s: %d/%d (%.1f%%) - %.1f items/sec",
                        name, current, total, (double) current / total * 100, itemsPerSecond);
            }
        }

        public void complete() {
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("%s completed in %d ms", name, elapsed);
        }
    }
}