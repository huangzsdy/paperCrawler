package com.papercrawler.scheduler;

import com.papercrawler.model.Paper;

import java.util.concurrent.CompletableFuture;

public interface Scheduler {
    /**
     * 启动爬虫任务
     */
    CompletableFuture<Void> start();

    /**
     * 停止爬虫任务
     */
    void stop();

    /**
     * 获取已完成的任务数量
     */
    int getCompletedCount();

    /**
     * 获取失败的任务数量
     */
    int getFailedCount();

    /**
     * 添加论文处理回调
     */
    void onPaperProcessed(PaperProcessor processor);

    @FunctionalInterface
    interface PaperProcessor {
        void process(Paper paper);
    }
}