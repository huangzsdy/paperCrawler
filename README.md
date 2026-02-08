# Academic Paper Crawler 🎓📄

一个基于Java的高性能学术论文爬虫系统，支持多线程、异步和分布式三种并发模式，能够爬取arXiv、bioRxiv和ChemRxiv的公开论文数据。

## 🌟 功能特性

### 🎯 核心功能
- **多源爬取**：同时支持arXiv、bioRxiv、ChemRxiv三大预印本平台
- **智能分类**：按学科自动分类论文（物理、材料、化学、生物学、电气工程）
- **多种并发模式**：支持线程、异步、分布式三种调度策略
- **灵活配置**：基于TOML的配置文件，支持运行时动态调整
- **多种输出格式**：支持JSON、CSV、XML输出，可扩展数据库存储

### 🔧 技术特性
- **模块化设计**：高内聚、低耦合的架构设计
- **现代Java**：使用Java 11+特性，包括CompletableFuture、Stream API
- **错误恢复**：完善的异常处理和自动重试机制
- **性能优化**：连接池管理、指数退避重试、批量处理
- **可扩展性**：插件式设计，易于添加新的数据源和存储后端

## 📁 项目结构

```
src/main/java/com/papercrawler/
├── config/                      # 配置管理模块
│   ├── CrawlerConfig.java      # 主配置类
│   └── AppConfig.java          # 应用配置辅助类
├── model/                       # 数据模型
│   ├── Paper.java              # 论文实体
│   ├── Author.java             # 作者实体
│   └── CrawlerTask.java        # 爬虫任务模型
├── parser/                      # 解析器模块
│   ├── PaperParser.java        # 解析器接口
│   ├── ArxivParser.java        # arXiv解析器
│   ├── BiorxivParser.java      # bioRxiv解析器
│   └── ChemRxivParser.java     # ChemRxiv解析器
├── scheduler/                   # 调度器模块
│   ├── Scheduler.java          # 调度器接口
│   ├── ThreadScheduler.java    # 多线程调度器
│   ├── AsyncScheduler.java     # 异步调度器
│   └── DistributedScheduler.java # 分布式调度器
├── storage/                     # 存储模块
│   ├── DataStorage.java        # 存储接口
│   └── FileStorage.java        # 文件存储实现
├── network/                     # 网络模块
│   ├── HttpClient.java         # HTTP客户端
│   └── AsyncHttpClient.java    # 异步HTTP客户端
├── util/                        # 工具模块
│   ├── TOMLConfigLoader.java   # TOML配置加载器
│   ├── BloomFilter.java        # 布隆过滤器
│   └── Logger.java             # 日志记录器
└── Main.java                    # 应用程序入口
```

## 🚀 快速开始

### 环境要求

- **Java**: JDK 11 或更高版本
- **Maven**: 3.6+ 或 Gradle
- **操作系统**: Windows, macOS, Linux
- **内存**: 至少 2GB 可用内存
- **网络**: 可访问互联网

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/papercrawler.git
   cd papercrawler
   ```

2. **构建项目**
   ```bash
   # 使用Maven
   mvn clean package
   
   # 或使用Gradle
   ./gradlew build
   ```

3. **配置应用**
   ```bash
   # 复制示例配置文件
   cp config/config.example.toml config/config.toml
   
   # 编辑配置文件
   vi config/config.toml
   ```

4. **运行应用**
   ```bash
   # 方式1: 直接运行
   java -jar target/papercrawler-1.0.0.jar
   
   # 方式2: 通过Maven运行
   mvn exec:java -Dexec.mainClass="com.papercrawler.Main"
   ```

## ⚙️ 配置说明

### 配置文件示例 (config/config.toml)

```toml
# 爬虫配置
[crawler]
mode = "thread"           # thread, async, distributed
max_connections = 20
request_timeout = 30
retry_attempts = 3
delay_between_requests = 1.0
user_agent = "AcademicPaperCrawler/1.0"
log_level = "INFO"

# 存储配置
[storage]
output_dir = "./data"
output_format = "json"    # json, csv, xml
max_file_size_mb = 100
batch_size = 50
database_enabled = false

# arXiv API配置
[arxiv]
base_url = "https://export.arxiv.org/api/query"
search_query = "cat:cond-mat+OR+cat:hep-+OR+cat:quant-ph+OR+cat:physics+OR+cat:cond-mat.mtrl-sci+OR+cat:physics.chem-ph+OR+cat:q-bio+OR+cat:eess.SP+OR+cat:eess.SY"
max_results = 1000

# 关键词分类
[keywords]
physics = ["cond-mat", "hep-", "quant-ph", "physics"]
materials = ["cond-mat.mtrl-sci"]
chemistry = ["physics.chem-ph"]
biology = ["q-bio"]
electrical_engineering = ["eess.SP", "eess.SY"]
```

### 配置项详解

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `crawler.mode` | `thread` | 并发模式：thread(多线程), async(异步), distributed(分布式) |
| `crawler.max_connections` | 20 | 最大并发连接数 |
| `crawler.request_timeout` | 30 | 请求超时时间(秒) |
| `crawler.retry_attempts` | 3 | 失败重试次数 |
| `crawler.delay_between_requests` | 1.0 | 请求间隔(秒) |
| `storage.output_format` | `json` | 输出格式：json, csv, xml |
| `arxiv.max_results` | 1000 | arXiv最大获取论文数 |

## 📊 使用指南

### 基本使用

```bash
# 启动爬虫（使用默认配置）
java -jar papercrawler.jar

# 指定配置文件
java -jar papercrawler.jar --config /path/to/config.toml

# 指定爬取源
java -jar papercrawler.jar --sources arxiv,biorxiv

# 指定输出目录
java -jar papercrawler.jar --output ./my_data
```

### 并发模式选择

1. **线程模式** (thread)
    - 适用于I/O密集型任务
    - 资源共享方便，调试简单
    - 配置：`mode = "thread"`

2. **异步模式** (async)
    - 基于CompletableFuture实现
    - 轻量级，高并发
    - 配置：`mode = "async"`

3. **分布式模式** (distributed)
    - 模拟分布式处理
    - 适用于大规模数据爬取
    - 配置：`mode = "distributed"`

### 输出数据格式

#### JSON格式示例
```json
{
  "id": "2101.12345v1",
  "title": "Quantum Machine Learning",
  "summary": "A comprehensive review of quantum machine learning...",
  "authors": [
    {"name": "John Doe", "affiliation": "MIT", "orcid": "0000-0000-0000-0001"}
  ],
  "doi": "10.1234/example",
  "pdf_url": "https://arxiv.org/pdf/2101.12345.pdf",
  "source": "arxiv",
  "categories": ["quant-ph", "cs.LG"],
  "published_date": "2023-01-01T00:00:00Z",
  "version": 1
}
```

#### CSV格式示例
```csv
id,title,authors,doi,source,categories,published_date,version
2101.12345v1,"Quantum Machine Learning",1,10.1234/example,arxiv,"quant-ph;cs.LG",2023-01-01T00:00:00Z,1
```

## 🔬 开发指南

### 添加新的论文源

1. 实现 `PaperParser` 接口
   ```java
   public class NewSourceParser implements PaperParser {
       @Override
       public List<Paper> parsePapers(String content) {
           // 解析逻辑
       }
       
       @Override
       public String buildQueryUrl(int start, int maxResults) {
           // 构建查询URL
       }
   }
   ```

2. 在 `Main.java` 中注册
   ```java
   // 创建解析器实例
   PaperParser newParser = new NewSourceParser();
   
   // 使用调度器处理
   Scheduler scheduler = createScheduler(newParser);
   ```

3. 更新配置文件
   ```toml
   [newsource]
   base_url = "https://api.newsource.org/v1/papers"
   api_key = "your_api_key"
   ```

### 自定义存储后端

1. 实现 `DataStorage` 接口
   ```java
   public class CustomStorage implements DataStorage {
       @Override
       public void initialize() throws StorageException {
           // 初始化逻辑
       }
       
       @Override
       public void savePaper(Paper paper) throws StorageException {
           // 存储逻辑
       }
   }
   ```

2. 在 `Main.java` 中使用
   ```java
   DataStorage storage = new CustomStorage();
   storage.initialize();
   ```

### 扩展关键词

编辑配置文件中的 `[keywords]` 部分：

```toml
[keywords]
# 添加新学科
computer_science = ["cs.AI", "cs.LG", "cs.CV"]
mathematics = ["math.NA", "math.OC"]
```

## 📈 性能优化

### 连接池配置
```toml
[crawler]
max_connections = 50  # 根据网络情况调整
```

### 批量处理
```toml
[storage]
batch_size = 100  # 每批次处理论文数
```

### 重试策略
```toml
[crawler]
retry_attempts = 5
delay_between_requests = 2.0
```

## 🐛 故障排除

### 常见问题

1. **网络连接失败**
   ```
   解决方案：
   1. 检查网络连接
   2. 配置代理（如果使用）
   3. 增加请求超时时间
   ```

2. **内存不足**
   ```
   解决方案：
   1. 减小batch_size
   2. 增加JVM内存：-Xmx2g
   3. 使用文件存储而非内存存储
   ```

3. **API限制**
   ```
   解决方案：
   1. 增加请求间隔
   2. 使用API密钥（如果支持）
   3. 分批次爬取
   ```

### 调试模式

启用调试日志：
```toml
[crawler]
log_level = "DEBUG"
```

或通过命令行：
```bash
java -Dlog_level=DEBUG -jar papercrawler.jar
```

## 🤝 贡献指南

我们欢迎任何形式的贡献！请参考以下步骤：

1. **Fork 仓库**
2. **创建特性分支**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **提交更改**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **推送到分支**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **创建 Pull Request**

### 开发规范

- 遵循Google Java代码风格
- 添加单元测试
- 更新相关文档
- 确保向后兼容性

## 📄 许可证

本项目采用MIT许可证。详见LICENSE文件。

## 🙏 致谢

- arXiv、bioRxiv、ChemRxiv提供开放的API接口
- 所有贡献者和用户
- 使用的开源库：
    - Apache HttpClient
    - Jackson JSON
    - Jsoup HTML解析器
    - TOML4J配置解析

## 📞 支持与反馈

如果您遇到问题或有建议：

1. 查看FAQ.md
2. 提交https://github.com/yourusername/papercrawler/issues
3. 加入https://github.com/yourusername/papercrawler/discussions

---

**Happy Crawling!** 🎉

*如果这个项目对您有帮助，请给我们一个⭐️！*