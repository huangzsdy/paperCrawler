package com.papercrawler.parser;

import com.papercrawler.model.Paper;

import java.util.List;
import java.util.Optional;

public interface PaperParser {
    /**
     * 解析API响应内容为论文列表
     */
    List<Paper> parsePapers(String content);

    /**
     * 构建API查询URL
     */
    String buildQueryUrl(int start, int maxResults);

    /**
     * 获取数据源名称
     */
    String getSourceName();

    /**
     * 解析单篇论文
     */
    Optional<Paper> parsePaper(String content);
}