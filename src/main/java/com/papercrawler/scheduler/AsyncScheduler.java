package com.papercrawler.scheduler;

import com.papercrawler.config.CrawlerConfig;
import com.papercrawler.model.Paper;
import com.papercrawler.parser.PaperParser;
import com.papercrawler.network.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncScheduler implements Scheduler {
    private final PaperParser parser;
    private final HttpClient httpClient;
    private final List<PaperProcessor> processors = new ArrayList<>();
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final List<CompletableFuture<Void>> futures = new ArrayList<>();

    public AsyncScheduler(PaperParser parser) {
        this.parser = parser;
        this.httpClient = new HttpClient();
    }

    @Override
    public CompletableFuture<Void> start() {
        int maxResults = CrawlerConfig.getArxivMaxResults();
        int batchSize = 100;

        for (int start = 0; start < maxResults; start += batchSize) {
            final int currentStart = start;
            final int currentSize = Math.min(batchSize, maxResults - currentStart);

            CompletableFuture<Void> future = CompletableFuture
                    .supplyAsync(() -> parser.buildQueryUrl(currentStart, currentSize))
                    .thenApplyAsync(url -> httpClient.get(url).orElse(null))
                    .thenApplyAsync(content -> content != null ? parser.parsePapers(content) : new ArrayList<Paper>())
                    .thenAcceptAsync(papers -> {
                        for (Paper paper : papers) {
                            for (PaperProcessor processor : processors) {
                                processor.process(paper);
                            }
                            completedCount.incrementAndGet();
                        }
                    })
                    .exceptionally(throwable -> {
                        failedCount.incrementAndGet();
                        System.err.println("Async task failed: " + throwable.getMessage());
                        return null;
                    });

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public void stop() {
        futures.forEach(future -> future.cancel(true));
        try {
            httpClient.close();
        } catch (Exception e) {
            System.err.println("Error closing HTTP client: " + e.getMessage());
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