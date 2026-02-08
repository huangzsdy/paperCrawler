package com.papercrawler;

import com.papercrawler.config.CrawlerConfig;
import com.papercrawler.model.Paper;
import com.papercrawler.parser.PaperParser;
import com.papercrawler.parser.ArxivParser;
import com.papercrawler.parser.BiorxivParser;
import com.papercrawler.parser.ChemRxivParser;
import com.papercrawler.scheduler.Scheduler;
import com.papercrawler.scheduler.ThreadScheduler;
import com.papercrawler.scheduler.AsyncScheduler;
import com.papercrawler.scheduler.DistributedScheduler;
import com.papercrawler.storage.DataStorage;
import com.papercrawler.storage.FileStorage;
import com.papercrawler.storage.DatabaseStorage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   Academic Paper Crawler v1.0");
        System.out.println("==========================================");

        try {
            // 加载配置文件
            System.out.println("Loading configuration...");
            CrawlerConfig.loadConfig("config/config.toml");

            // 初始化数据存储
            System.out.println("Initializing data storage...");
            DataStorage storage = createStorage();
            storage.initialize();

            // 获取关键词配置
            Map<String, List<String>> keywords = CrawlerConfig.getKeywords();

            // 处理arXiv论文
            processArxivPapers(storage, keywords);

            // 处理bioRxiv论文
            processBiorxivPapers(storage, keywords);

            // 处理ChemRxiv论文
            processChemRxivPapers(storage, keywords);

            // 关闭存储
            storage.close();

            System.out.println("\n==========================================");
            System.out.println("   Crawling completed successfully!");
            System.out.println("==========================================");

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static DataStorage createStorage() {
        if (CrawlerConfig.isDatabaseEnabled()) {
            return new DatabaseStorage();
        } else {
            return new FileStorage();
        }
    }

    private static void processArxivPapers(DataStorage storage,
                                           Map<String, List<String>> keywords) throws Exception {
        System.out.println("\nProcessing arXiv papers...");

        // 创建arXiv解析器
        PaperParser arxivParser = new ArxivParser();

        // 创建调度器
        Scheduler scheduler = createScheduler(arxivParser);

        // 设置论文处理回调
        scheduler.onPaperProcessed(paper -> {
            try {
                storage.savePaper(paper);
                System.out.printf("✓ Saved arXiv paper: %s%n", paper.getTitle());
            } catch (Exception e) {
                System.err.printf("✗ Failed to save paper: %s%n", e.getMessage());
            }
        });

        // 启动爬虫
        CompletableFuture<Void> future = scheduler.start();
        future.join();

        System.out.printf("arXiv processing completed. Processed: %d, Failed: %d%n",
                scheduler.getCompletedCount(), scheduler.getFailedCount());
    }

    private static void processBiorxivPapers(DataStorage storage,
                                             Map<String, List<String>> keywords) throws Exception {
        System.out.println("\nProcessing bioRxiv papers...");

        PaperParser biorxivParser = new BiorxivParser();
        Scheduler scheduler = createScheduler(biorxivParser);

        scheduler.onPaperProcessed(paper -> {
            try {
                storage.savePaper(paper);
                System.out.printf("✓ Saved bioRxiv paper: %s%n", paper.getTitle());
            } catch (Exception e) {
                System.err.printf("✗ Failed to save paper: %s%n", e.getMessage());
            }
        });

        CompletableFuture<Void> future = scheduler.start();
        future.join();

        System.out.printf("bioRxiv processing completed. Processed: %d, Failed: %d%n",
                scheduler.getCompletedCount(), scheduler.getFailedCount());
    }

    private static void processChemRxivPapers(DataStorage storage,
                                              Map<String, List<String>> keywords) throws Exception {
        System.out.println("\nProcessing ChemRxiv papers...");

        PaperParser chemRxivParser = new ChemRxivParser();
        Scheduler scheduler = createScheduler(chemRxivParser);

        scheduler.onPaperProcessed(paper -> {
            try {
                storage.savePaper(paper);
                System.out.printf("✓ Saved ChemRxiv paper: %s%n", paper.getTitle());
            } catch (Exception e) {
                System.err.printf("✗ Failed to save paper: %s%n", e.getMessage());
            }
        });

        CompletableFuture<Void> future = scheduler.start();
        future.join();

        System.out.printf("ChemRxiv processing completed. Processed: %d, Failed: %d%n",
                scheduler.getCompletedCount(), scheduler.getFailedCount());
    }

    private static Scheduler createScheduler(PaperParser parser) {
        String mode = CrawlerConfig.getCrawlerMode();

        switch (mode.toLowerCase()) {
            case "thread":
                return new ThreadScheduler(parser);

            case "async":
                return new AsyncScheduler(parser);

            case "distributed":
                return new DistributedScheduler(parser);

            default:
                throw new IllegalArgumentException("Unknown scheduler mode: " + mode);
        }
    }
}