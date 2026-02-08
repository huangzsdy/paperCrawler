package com.papercrawler.scheduler;

import com.papercrawler.config.CrawlerConfig;
import com.papercrawler.model.Paper;
import com.papercrawler.parser.PaperParser;
import com.papercrawler.network.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedScheduler implements Scheduler {
    private final PaperParser parser;
    private final List<PaperProcessor> processors = new ArrayList<>();
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final ExecutorService executorService;

    public DistributedScheduler(PaperParser parser) {
        this.parser = parser;
        this.executorService = Executors.newFixedThreadPool(
                CrawlerConfig.getMaxConnections()
        );
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 分布式调度逻辑 - 这里简化为多节点模拟
                List<CompletableFuture<Void>> nodeFutures = new ArrayList<>();

                // 模拟多个分布式节点
                int numberOfNodes = 3;
                int papersPerNode = CrawlerConfig.getArxivMaxResults() / numberOfNodes;

                for (int node = 0; node < numberOfNodes; node++) {
                    final int currentNode = node;
                    final int startIndex = currentNode * papersPerNode;

                    CompletableFuture<Void> nodeFuture = CompletableFuture.runAsync(() -> {
                        processNode(startIndex, papersPerNode, "Node-" + currentNode);
                    }, executorService);

                    nodeFutures.add(nodeFuture);
                }

                // 等待所有节点完成
                CompletableFuture.allOf(nodeFutures.toArray(new CompletableFuture[0])).join();

            } catch (Exception e) {
                failedCount.incrementAndGet();
                throw new RuntimeException("Distributed scheduler failed", e);
            }
        });
    }

    private void processNode(int start, int count, String nodeId) {
        try (HttpClient httpClient = new HttpClient()) {
            System.out.printf("Node %s processing papers from %d to %d%n",
                    nodeId, start, start + count);

            String url = parser.buildQueryUrl(start, count);
            String content = httpClient.get(url).orElseThrow(() ->
                    new RuntimeException("Failed to fetch content for node " + nodeId));

            List<Paper> papers = parser.parsePapers(content);

            // 模拟分布式处理延迟
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            // 处理论文
            for (Paper paper : papers) {
                for (PaperProcessor processor : processors) {
                    processor.process(paper);
                }
                completedCount.incrementAndGet();
            }

            System.out.printf("Node %s completed: %d papers%n", nodeId, papers.size());

        } catch (Exception e) {
            failedCount.incrementAndGet();
            System.err.printf("Node %s failed: %s%n", nodeId, e.getMessage());
        }
    }

    @Override
    public void stop() {
        executorService.shutdown();
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