# todolist1 – 集成战略后端开发清单

> 说明：任务按阶段排列，每个子项均为可勾选条目，完成后将 `[ ]` 改为 `[x]`。所有描述以 “萨卡兹的无终奇语” 为基线，其它肉鸽主题在同一框架下扩展，参考 `backend/research1.md`。  
> **企业级约束**：包结构必须遵循分层命名（`com.karaik.gamebot.<领域>.<层级>`），禁止跨层直接互调；同时，每完成一个阶段，务必更新 `backend/Agent.md` 与根目录 `Agent.md`，记录本阶段的设计与接口变化。

## 阶段 0：准备与研究
- [x] **确认需求**：复盘研究文档，明确认证链路、数据结构、结局规则。
- [x] **规划包结构**：建立 `com.karaik.gamebot.roguelike` 顶级包，并细分：
  - `client`（HTTP/签名）
  - `config`（配置模型与加载）
  - `domain`（实体/DTO）
  - `repository`
  - `service`
  - `theme.<主题英文键>`（主题专属解析器）
- [x] **跨业务拓展**：同步评估未来非肉鸽功能的命名空间，确保可以按同样分层扩展。
- [x] **主题清单**：枚举首批支持的主题与配置模板。
- [x] **安全策略**：决定 Hypergryph Token 的存储、加密与注入方式。
- [x] **文档同步**：阶段完成后更新 backend/Agent 与根 Agent，写清包结构、主题范围与安全策略。

## 阶段 1：通信与认证
- [x] **配置模型**：使用 `@ConfigurationProperties` 承载 API URL、AppCode、UserAgent 等（如 `RoguelikeApiProperties`）。
- [x] **签名工具**：实现 `RoguelikeSignatureHelper`（HMAC-SHA256 → MD5），并用单测覆盖示例。
- [x] **HTTP 客户端**：基于 `WebClient` 的 `RoguelikeHttpClient`，支持超时/异常包装。
- [x] **认证链路**：
  - [x] `grant`：token → `oauth_code`
  - [x] `generate_cred_by_code`: `code` → `cred` + `token`
  - [x] `player/binding`: 获取 `uid`
  - [x] `rogue info`: 拉取 `topics/history/...`
- [x] **异常分类**：区分凭证错误、网络异常、API 非 0 code，并产出业务异常类。
- [x] **文档同步**：记录配置模型、HTTP 客户端、异常策略。

## 阶段 2：持久化层
- [x] **数据库表**：`roguelike_run`（`id` PK，`uid`，`theme_id`，`start_ts`，`record_json`，`created_at`，`updated_at`；可选结构化列 `score`、`success` 等）。
- [x] **实体 & Mapper**：定义 `RoguelikeRunEntity`、`RoguelikeRunMapper`（MyBatis-Plus），提供批量插入/更新。
- [x] **仓储实现**：`RoguelikeRunRepository` 暴露 `saveRuns`、`listRuns`，隐藏底层 ORM。
- [x] **迁移脚本**：如需从旧 SQLite 导入，提供脚本或工具类。
- [x] **文档同步**：说明表结构、实体关系与迁移策略。

## 阶段 3：主题配置与解析接口
- [x] **资源整理**：在 `resources/roguelike/themes/` 存放每个主题的 JSON 配置。
- [x] **配置加载器**：实现 `RoguelikeThemeRegistry`，启动时加载并校验主题配置，按 `themeId/name` 索引。
- [x] **Analyzer 接口**：定义 `RoguelikeThemeAnalyzer`，方法包含 `supports` 与 `analyze`。
- [x] **抽象实现**：提供 `AbstractThemeAnalyzer` 处理共性流程，便于主题子类微调字段或规则。
- [x] **首个主题**：为 `萨卡兹的无终奇语` 在 `roguelike.theme.skadi` 下实现 analyzer 并配套单测。
- [x] **文档同步**：写明配置目录结构、Registry 机制、Analyzer 扩展步骤。

## 阶段 4：业务服务层
- [ ] **RoguelikeService**：
  - [ ] 调用客户端刷新 raw data，并将 `records` 落表
  - [ ] 基于 `topics` 自动匹配 theme/analyzer
  - [ ] 聚合分析结果为统一 DTO
- [ ] **多 UID 支持**：预留账号映射（用户→多 UID），供前端/机器人挑选。
- [ ] **缓存策略**：实现短期缓存或手动刷新机制，防止频繁访问 API。
- [ ] **文档同步**：描述服务职责、缓存策略、多 UID 设计。

## 阶段 5：API 层与对接
- [ ] **REST 控制器**：例如 `RoguelikeController` 提供：
  - `[GET] /api/roguelike/themes`
  - `[GET] /api/roguelike/{themeId}/analysis`
  - `[POST] /api/roguelike/refresh`
- [ ] **响应模型**：定义玩家信息、career、总览、近 7 日/近期对局卡片结构，与前端/Bot 对齐。
- [ ] **鉴权策略**：实现 API Key / Header 校验或 Spring Security 过滤器。
- [ ] **文档同步**：更新 API 章节与鉴权方案。

## 阶段 6：测试与验证
- [ ] **单元测试**：覆盖签名工具、HTTP 客户端（MockServer）、Theme Analyzer、Repository。
- [ ] **集成测试**：使用 Testcontainers/H2，模拟 “凭证→拉数据→解析→返回 DTO” 流程。
- [ ] **性能基准**：验证批量写入、分析计算在大数据量下的耗时。
- [ ] **文档同步**：记录测试范围、依赖及性能结论。

## 阶段 7：Bot/前端对接准备
- [ ] **Bot**：在机器人模块添加新 API 客户端，替换健康检查示例，实践主题分析回显。
- [ ] **前端**：提供 Swagger/OpenAPI/MOCK 数据，协助前端展示仪表盘。
- [ ] **协议文档**：在 `Agent.md` 系列中写清接口协议、请求头、响应示例。
- [ ] **文档同步**：说明跨模块依赖与对接步骤。

## 阶段 8：部署与运维
- [ ] **配置管理**：为 dev/test/prod 提供 Token 注入方案（环境变量或密管服务）。
- [ ] **监控指标**：记录调用成功率、失败原因、最近刷新时间；需要时推送到监控系统。
- [ ] **告警策略**：当认证失败或 API 变更时及时告警。
- [ ] **文档同步**：更新运维章节，包含配置、监控与告警配置。

完成每个阶段后，除了勾选本列表，还需立即更新 backend/Agent 与根 Agent 文档，确保后续协作者能按企业级分层规范继续迭代。新增主题时，仅需：
1. 在 `roguelike/theme/<new>` 创建 analyzer；
2. 添加对应配置文件；
3. 在 Registry 注册即可。
