package com.papercrawler.parser;

import com.papercrawler.model.Paper;
import com.papercrawler.model.Author;
import com.papercrawler.config.CrawlerConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BiorxivParser implements PaperParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<Paper> parsePapers(String content) {
        List<Paper> papers = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode collection = root.path("collection");

            if (collection.isArray()) {
                for (JsonNode item : collection) {
                    Paper paper = parsePaperItem(item);
                    if (paper != null) {
                        papers.add(paper);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bioRxiv response", e);
        }

        return papers;
    }

    private Paper parsePaperItem(JsonNode item) {
        try {
            Paper paper = new Paper();
            paper.setSource("biorxiv");

            // 解析基本字段
            paper.setId(item.path("doi").asText());
            paper.setTitle(item.path("title").asText().replaceAll("\\s+", " ").trim());
            paper.setSummary(item.path("abstract").asText().replaceAll("\\s+", " ").trim());
            paper.setDoi(item.path("doi").asText());

            // 解析作者
            String authorsStr = item.path("authors").asText();
            paper.setAuthors(parseAuthors(authorsStr));

            // 解析分类
            String category = item.path("category").asText();
            if (!category.isEmpty()) {
                paper.getCategories().add(category);
            }

            // 解析日期
            String dateStr = item.path("date").asText();
            if (!dateStr.isEmpty()) {
                paper.setPublishedDate(LocalDateTime.parse(dateStr + "T00:00:00Z",
                        DateTimeFormatter.ISO_DATE_TIME));
                paper.setUpdatedDate(paper.getPublishedDate());
            }

            // 解析PDF URL
            String jatsXml = item.path("jatsxml").asText();
            if (!jatsXml.isEmpty()) {
                paper.setPdfUrl(jatsXml.replace(".xml", ".pdf"));
            }

            // 解析版本
            paper.setVersion(item.path("version").asInt(1));

            return paper;

        } catch (Exception e) {
            System.err.println("Error parsing bioRxiv item: " + e.getMessage());
            return null;
        }
    }

    private List<Author> parseAuthors(String authorsStr) {
        List<Author> authors = new ArrayList<>();

        if (authorsStr == null || authorsStr.trim().isEmpty()) {
            return authors;
        }

        // bioRxiv作者格式通常是"LastName1 FirstName1, LastName2 FirstName2"
        String[] authorNames = authorsStr.split(",\\s*");

        for (String authorName : authorNames) {
            if (!authorName.trim().isEmpty()) {
                Author author = new Author();
                author.setName(authorName.trim());
                authors.add(author);
            }
        }

        return authors;
    }

    @Override
    public String buildQueryUrl(int start, int maxResults) {
        String baseUrl = CrawlerConfig.getBiorxivBaseUrl();
        int interval = CrawlerConfig.getBiorxivInterval();

        return String.format("%s/%d/%d", baseUrl, start, interval);
    }

    @Override
    public String getSourceName() {
        return "biorxiv";
    }

    @Override
    public Optional<Paper> parsePaper(String content) {
        try {
            JsonNode item = objectMapper.readTree(content);
            Paper paper = parsePaperItem(item);
            return Optional.ofNullable(paper);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}