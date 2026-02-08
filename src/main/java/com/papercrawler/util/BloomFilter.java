package com.papercrawler.util;

import java.util.BitSet;
import java.util.function.Function;

/**
 * 布隆过滤器实现，用于快速判断元素是否存在
 */
public class BloomFilter {
    private final BitSet bitSet;
    private final int size;
    private final int hashFunctions;
    private int elementCount = 0;

    /**
     * 创建布隆过滤器
     * @param size 位数组大小
     * @param hashFunctions 哈希函数数量
     */
    public BloomFilter(int size, int hashFunctions) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (hashFunctions <= 0) {
            throw new IllegalArgumentException("Number of hash functions must be positive");
        }

        this.bitSet = new BitSet(size);
        this.size = size;
        this.hashFunctions = hashFunctions;
    }

    /**
     * 创建具有默认参数的布隆过滤器
     * @param expectedElements 预期元素数量
     * @param falsePositiveRate 预期误判率
     */
    public BloomFilter(int expectedElements, double falsePositiveRate) {
        if (expectedElements <= 0) {
            throw new IllegalArgumentException("Expected elements must be positive");
        }
        if (falsePositiveRate <= 0 || falsePositiveRate >= 1) {
            throw new IllegalArgumentException("False positive rate must be between 0 and 1");
        }

        // 计算最佳大小和哈希函数数量
        this.size = calculateOptimalSize(expectedElements, falsePositiveRate);
        this.hashFunctions = calculateOptimalHashFunctions(expectedElements, size);
        this.bitSet = new BitSet(size);
    }

    /**
     * 添加元素
     */
    public void add(String item) {
        if (item == null || item.isEmpty()) {
            return;
        }

        for (int i = 0; i < hashFunctions; i++) {
            int hash = hash(item, i);
            bitSet.set(Math.abs(hash % size), true);
        }
        elementCount++;
    }

    /**
     * 批量添加元素
     */
    public void addAll(Iterable<String> items) {
        for (String item : items) {
            add(item);
        }
    }

    /**
     * 检查元素是否可能存在
     * @return true: 可能存在（可能有误判），false: 一定不存在
     */
    public boolean mightContain(String item) {
        if (item == null || item.isEmpty()) {
            return false;
        }

        for (int i = 0; i < hashFunctions; i++) {
            int hash = hash(item, i);
            if (!bitSet.get(Math.abs(hash % size))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取当前元素数量
     */
    public int getElementCount() {
        return elementCount;
    }

    /**
     * 获取位数组大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取哈希函数数量
     */
    public int getHashFunctions() {
        return hashFunctions;
    }

    /**
     * 计算估计的误判率
     */
    public double estimateFalsePositiveRate() {
        if (elementCount == 0) {
            return 0.0;
        }

        double probability = Math.pow(1 - Math.exp(-hashFunctions * elementCount / (double) size), hashFunctions);
        return Math.min(probability, 1.0);
    }

    /**
     * 清空布隆过滤器
     */
    public void clear() {
        bitSet.clear();
        elementCount = 0;
    }

    /**
     * 保存布隆过滤器状态到字节数组
     */
    public byte[] toByteArray() {
        return bitSet.toByteArray();
    }

    /**
     * 从字节数组加载布隆过滤器状态
     */
    public void fromByteArray(byte[] bytes) {
        BitSet restoredBitSet = BitSet.valueOf(bytes);
        for (int i = 0; i < size && i < restoredBitSet.size(); i++) {
            bitSet.set(i, restoredBitSet.get(i));
        }
    }

    /**
     * 计算哈希值
     */
    private int hash(String item, int seed) {
        int hash = seed;
        for (char c : item.toCharArray()) {
            hash = (hash * 31) + c;
        }
        return Math.abs(hash);
    }

    /**
     * 计算最佳位数组大小
     */
    private int calculateOptimalSize(int n, double p) {
        return (int) Math.ceil(-(n * Math.log(p)) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 计算最佳哈希函数数量
     */
    private int calculateOptimalHashFunctions(int n, int m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    /**
     * 合并两个布隆过滤器
     * 注意：两个过滤器必须具有相同的大小和哈希函数数量
     */
    public void merge(BloomFilter other) {
        if (this.size != other.size || this.hashFunctions != other.hashFunctions) {
            throw new IllegalArgumentException("Cannot merge bloom filters with different parameters");
        }

        this.bitSet.or(other.bitSet);
        this.elementCount += other.elementCount;
    }

    /**
     * 获取过滤器状态信息
     */
    public String getStatus() {
        int bitsSet = bitSet.cardinality();
        double fillRatio = (double) bitsSet / size;

        return String.format(
                "Bloom Filter Status - Size: %d, Hash Functions: %d, Elements: %d, " +
                        "Bits Set: %d (%.2f%%), Estimated False Positive Rate: %.6f",
                size, hashFunctions, elementCount, bitsSet, fillRatio * 100, estimateFalsePositiveRate()
        );
    }
}