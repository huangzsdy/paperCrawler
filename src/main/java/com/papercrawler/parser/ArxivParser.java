package com.papercrawler.parser;

import com.papercrawler.model.Paper;
import com.papercrawler.model.Author;
import com.papercrawler.config.CrawlerConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArxivParser implements PaperParser {
    private static final DateTimeFormatter ARXIV_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public List<Paper> parsePapers(String content) {
        List<Paper> papers = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(content);
            Elements entries = doc.select("entry");

            for (Element entry : entries) {
                Paper paper = parseEntry(entry);
                if (paper != null) {
                    papers.add(paper);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse arXiv response", e);
        }

        return papers;
    }

    private Paper parseEntry(Element entry) {
        try {
            Paper paper = new Paper();
            paper.setSource("arxiv");

            // 解析ID
            String id = entry.select("id").text();
            paper.setId(extractArxivId(id));

            // 解析标题
            String title = entry.select("title").text().replaceAll("\\s+", " ").trim();
            paper.setTitle(title);

            // 解析摘要
            String summary = entry.select("summary").text().replaceAll("\\s+", " ").trim();
            paper.setSummary(summary);

            // 解析作者
            List<Author> authors = new ArrayList<>();
            Elements authorElements = entry.select("author > name");
            for (Element authorElem : authorElements) {
                authors.add(new Author(authorElem.text()));
            }
            paper.setAuthors(authors);

            // 解析分类
            List<String> categories = new ArrayList<>();
            String categoriesStr = entry.select("arxiv|primary_category").attr("term");
            if (!categoriesStr.isEmpty()) {
                categories.add(categoriesStr);
            }
            paper.setCategories(categories);

            // 解析日期
            String publishedStr = entry.select("published").text();
            if (!publishedStr.isEmpty()) {
                paper.setPublishedDate(LocalDateTime.parse(publishedStr, ARXIV_DATE_FORMATTER));
            }

            String updatedStr = entry.select("updated").text();
            if (!updatedStr.isEmpty()) {
                paper.setUpdatedDate(LocalDateTime.parse(updatedStr, ARXIV_DATE_FORMATTER));
            }

            // 解析PDF链接
            Elements links = entry.select("link[title=pdf]");
            if (!links.isEmpty()) {
                paper.setPdfUrl(links.first().attr("href"));
            }

            return paper;

        } catch (Exception e) {
            System.err.println("Error parsing arXiv entry: " + e.getMessage());
            return null;
        }
    }

    private String extractArxivId(String idText) {
        // 从完整URL中提取arXiv ID
        int lastSlash = idText.lastIndexOf('/');
        if (lastSlash != -1) {
            return idText.substring(lastSlash + 1);
        }
        return idText;
    }

    @Override
    public String buildQueryUrl(int start, int maxResults) {
        String baseUrl = CrawlerConfig.getArxivBaseUrl();
        String searchQuery = CrawlerConfig.getArxivSearchQuery();

        return String.format("%s?search_query=%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=descending",
                baseUrl, searchQuery, start, maxResults);
    }

    @Override
    public String getSourceName() {
        return "arxiv";
    }

    @Override
    public Optional<Paper> parsePaper(String content) {
        try {
            Document doc = Jsoup.parse(content);
            Element entry = doc.select("entry").first();
            if (entry != null) {
                Paper paper = parseEntry(entry);
                return Optional.ofNullable(paper);
            }
        } catch (Exception e) {
            // 记录错误但返回空
        }
        return Optional.empty();
    }
}