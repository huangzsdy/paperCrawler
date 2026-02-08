package com.papercrawler.scheduler;

import com.papercrawler.config.CrawlerConfig;
import com.papercrawler.model.Paper;
import com.papercrawler.parser.PaperParser;
import com.papercrawler.network.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadScheduler implements Scheduler {
    private final ExecutorService executor;
    private final HttpClient httpClient;
    private final PaperParser parser;
    private final List<PaperProcessor> processors = new ArrayList<>();
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);

    public ThreadScheduler(PaperParser parser) {
        this.parser = parser;
        this.httpClient = new HttpClient();
        this.executor = Executors.newFixedThreadPool(CrawlerConfig.getMaxConnections());
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                int maxResults = CrawlerConfig.getArxivMaxResults();
                int batchSize = 100;

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (int start = 0; start < maxResults; start += batchSize) {
                    final int currentStart = start;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        processBatch(currentStart, Math.min(batchSize, maxResults - currentStart));
                    }, executor);

                    futures.add(future);

                    // 控制请求频率
                    try {
                        Thread.sleep((long) (CrawlerConfig.getDelayBetweenRequests() * 1000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                // 等待所有任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            } catch (Exception e) {
                failedCount.incrementAndGet();
                throw new RuntimeException("Scheduler execution failed", e);
            }
        });
    }

    private void processBatch(int start, int maxResults) {
        try {
            String url = parser.buildQueryUrl(start, maxResults);
            String content = httpClient.get(url).orElseThrow(() ->
                    new RuntimeException("Failed to fetch content from " + url));

            List<Paper> papers = parser.parsePapers(content);

            for (Paper paper : papers) {
                // 通知所有处理器
                for (PaperProcessor processor : processors) {
                    processor.process(paper);
                }
                completedCount.incrementAndGet();
            }

        } catch (Exception e) {
            failedCount.incrementAndGet();
            System.err.println("Batch processing failed: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            httpClient.close();
        } catch (Exception e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int getCompletedCount() {
        return completedCount.get();
    }

    @Override
    public int getFailedCount() {
        return failedCount.get();
    }

    @Override
    public void onPaperProcessed(PaperProcessor processor) {
        processors.add(processor);
    }
}