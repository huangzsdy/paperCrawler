package com.papercrawler.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 爬虫任务模型类
 */
public class CrawlerTask {
    public enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    private String taskId;
    private String source;
    private String query;
    private TaskStatus status;
    private int progress;
    private int total;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private Map<String, Object> metadata;

    public CrawlerTask() {
        this.metadata = new HashMap<>();
        this.status = TaskStatus.PENDING;
        this.progress = 0;
    }

    public CrawlerTask(String taskId, String source, String query) {
        this();
        this.taskId = taskId;
        this.source = source;
        this.query = query;
    }

    // Getter和Setter方法
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // 辅助方法
    public void start() {
        this.status = TaskStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.progress = this.total;
        this.endTime = LocalDateTime.now();
    }

    public void fail(String error) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = error;
        this.endTime = LocalDateTime.now();
    }

    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public boolean isRunning() {
        return status == TaskStatus.RUNNING;
    }

    public boolean isFailed() {
        return status == TaskStatus.FAILED;
    }

    public double getProgressPercentage() {
        if (total == 0) return 0.0;
        return (double) progress / total * 100;
    }

    public long getDurationInMillis() {
        if (startTime == null || endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).toMillis();
    }

    public void updateProgress(int progress, int total) {
        this.progress = progress;
        this.total = total;
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    @Override
    public String toString() {
        return String.format(
                "CrawlerTask{taskId='%s', source='%s', status=%s, progress=%d/%d (%.1f%%)}",
                taskId, source, status, progress, total, getProgressPercentage()
        );
    }
}