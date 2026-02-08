package com.papercrawler.storage;

import com.papercrawler.model.Paper;
import com.papercrawler.config.CrawlerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileStorage implements DataStorage {
    private final ObjectMapper objectMapper;
    private BufferedWriter writer;
    private Path outputFile;
    private int currentSize = 0;
    private boolean firstPaper = true;

    public FileStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void initialize() throws StorageException {
        try {
            String outputDir = CrawlerConfig.getOutputDir();
            String format = CrawlerConfig.getOutputFormat();

            Files.createDirectories(Paths.get(outputDir));

            String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            outputFile = Paths.get(outputDir, String.format("papers_%s.%s", timestamp, format));

            writer = new BufferedWriter(new FileWriter(outputFile.toFile()));

            if ("json".equals(format)) {
                writer.write("[\n");
            } else if ("csv".equals(format)) {
                writer.write("id,title,authors,doi,source,categories,published_date,version\n");
            }

            System.out.printf("File storage initialized. Output file: %s%n", outputFile);

        } catch (IOException e) {
            throw new StorageException("Failed to initialize file storage", e);
        }
    }

    @Override
    public void savePaper(Paper paper) throws StorageException {
        try {
            String format = CrawlerConfig.getOutputFormat();

            switch (format) {
                case "json":
                    savePaperAsJson(paper);
                    break;

                case "csv":
                    savePaperAsCsv(paper);
                    break;

                case "xml":
                    savePaperAsXml(paper);
                    break;

                default:
                    throw new StorageException("Unsupported format: " + format);
            }

            currentSize++;

            // 检查文件大小并轮转
            if (currentSize >= CrawlerConfig.getBatchSize()) {
                rotateFile();
            }

        } catch (IOException e) {
            throw new StorageException("Failed to save paper", e);
        }
    }

    private void savePaperAsJson(Paper paper) throws IOException {
        if (!firstPaper) {
            writer.write(",\n");
        }

        String json = objectMapper.writeValueAsString(paper);
        writer.write("  " + json);
        firstPaper = false;
    }

    private void savePaperAsCsv(Paper paper) throws IOException {
        StringBuilder csvLine = new StringBuilder();

        // 转义特殊字符
        csvLine.append(escapeCsvField(paper.getId())).append(",");
        csvLine.append(escapeCsvField(paper.getTitle())).append(",");
        csvLine.append(escapeCsvField(String.valueOf(paper.getAuthors().size()))).append(",");
        csvLine.append(escapeCsvField(paper.getDoi())).append(",");
        csvLine.append(escapeCsvField(paper.getSource())).append(",");
        csvLine.append(escapeCsvField(String.join(";", paper.getCategories()))).append(",");
        csvLine.append(escapeCsvField(paper.getPublishedDate() != null ?
                paper.getPublishedDate().toString() : "")).append(",");
        csvLine.append(escapeCsvField(String.valueOf(paper.getVersion())));

        writer.write(csvLine.toString());
        writer.newLine();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "\"\"";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private void savePaperAsXml(Paper paper) throws IOException {
        writer.write(String.format("  <paper>\n" +
                        "    <id>%s</id>\n" +
                        "    <title><![CDATA[%s]]></title>\n" +
                        "    <source>%s</source>\n" +
                        "  </paper>\n",
                escapeXml(paper.getId()),
                escapeXml(paper.getTitle()),
                escapeXml(paper.getSource())));
    }

    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void rotateFile() throws StorageException {
        try {
            if (writer != null) {
                if ("json".equals(CrawlerConfig.getOutputFormat())) {
                    writer.write("\n]");
                }
                writer.close();
            }

            System.out.printf("File rotated: %s (size: %d papers)%n", outputFile, currentSize);

            // 重置计数器并重新初始化
            currentSize = 0;
            firstPaper = true;
            initialize();

        } catch (IOException e) {
            throw new StorageException("Failed to rotate file", e);
        }
    }

    @Override
    public void savePapers(List<Paper> papers) throws StorageException {
        for (Paper paper : papers) {
            savePaper(paper);
        }
    }

    @Override
    public void close() throws StorageException {
        try {
            if (writer != null) {
                if ("json".equals(CrawlerConfig.getOutputFormat())) {
                    writer.write("\n]");
                }
                writer.close();
                System.out.printf("File storage closed. Total papers saved: %d%n", currentSize);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to close storage", e);
        }
    }
}