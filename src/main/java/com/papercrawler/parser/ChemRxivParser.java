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

public class ChemRxivParser implements PaperParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Override
    public List<Paper> parsePapers(String content) {
        List<Paper> papers = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode itemHits = root.path("itemHits");

            if (itemHits.isArray()) {
                for (JsonNode hit : itemHits) {
                    JsonNode item = hit.path("item");
                    if (item.isObject()) {
                        Paper paper = parsePaperItem(item);
                        if (paper != null) {
                            papers.add(paper);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ChemRxiv response", e);
        }

        return papers;
    }

    private Paper parsePaperItem(JsonNode item) {
        try {
            Paper paper = new Paper();
            paper.setSource("chemrxiv");

            // 解析基本字段
            paper.setId(item.path("id").asText());
            paper.setTitle(item.path("title").asText().replaceAll("\\s+", " ").trim());
            paper.setDoi(item.path("doi").asText());

            // 解析摘要
            if (item.has("description") && item.get("description").isTextual()) {
                paper.setSummary(item.get("description").asText().replaceAll("\\s+", " ").trim());
            }

            // 解析作者
            if (item.has("authors") && item.get("authors").isArray()) {
                List<Author> authors = new ArrayList<>();
                for (JsonNode authorNode : item.get("authors")) {
                    Author author = new Author();
                    String firstName = authorNode.path("firstName").asText();
                    String lastName = authorNode.path("lastName").asText();
                    author.setName((firstName + " " + lastName).trim());
                    author.setAffiliation(authorNode.path("affiliation").asText());
                    author.setOrcid(authorNode.path("orcid").asText());
                    authors.add(author);
                }
                paper.setAuthors(authors);
            }

            // 解析分类
            if (item.has("categories") && item.get("categories").isArray()) {
                for (JsonNode categoryNode : item.get("categories")) {
                    paper.getCategories().add(categoryNode.asText());
                }
            }

            // 解析日期
            String publishedDateStr = item.path("publishedDate").asText();
            if (!publishedDateStr.isEmpty()) {
                paper.setPublishedDate(LocalDateTime.parse(publishedDateStr, DATE_FORMATTER));
            }

            String updatedDateStr = item.path("updatedDate").asText();
            if (!updatedDateStr.isEmpty()) {
                paper.setUpdatedDate(LocalDateTime.parse(updatedDateStr, DATE_FORMATTER));
            }

            // 解析PDF URL
            if (item.has("pdfUrl") && item.get("pdfUrl").isTextual()) {
                paper.setPdfUrl(item.get("pdfUrl").asText());
            }

            // 解析版本
            paper.setVersion(item.path("version").asInt(1));

            return paper;

        } catch (Exception e) {
            System.err.println("Error parsing ChemRxiv item: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String buildQueryUrl(int start, int maxResults) {
        String baseUrl = CrawlerConfig.getChemRxivBaseUrl();
        int pageSize = CrawlerConfig.getChemRxivPageSize();

        return String.format("%s?limit=%d&skip=%d&orderBy=publishedDate",
                baseUrl, pageSize, start);
    }

    @Override
    public String getSourceName() {
        return "chemrxiv";
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