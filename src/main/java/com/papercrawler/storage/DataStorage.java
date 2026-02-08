package com.papercrawler.storage;

import com.papercrawler.model.Paper;

import java.util.List;

public interface DataStorage {
    /**
     * 初始化存储
     */
    void initialize() throws StorageException;

    /**
     * 保存单篇论文
     */
    void savePaper(Paper paper) throws StorageException;

    /**
     * 批量保存论文
     */
    void savePapers(List<Paper> papers) throws StorageException;

    /**
     * 关闭存储
     */
    void close() throws StorageException;

    class StorageException extends Exception {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}