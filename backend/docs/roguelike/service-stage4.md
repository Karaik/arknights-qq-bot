# 集成战略模块阶段 4 – 业务服务

## 1. 账号与森空岛凭证
- `RoguelikeAccountService`：抽象用户键 → 游戏 UID 的映射，默认实现 `RoguelikeAccountServiceImpl` 以 Map 形式维护绑定（未绑定时直接返回输入）。
- `SklandTokenStore`：存储 userKey 对应的森空岛 Hypergryph Token（默认 InMemory，可扩展至数据库/配置中心）。
- 通过这些抽象，可在后续阶段挂接正式的账号系统与凭证管理。

## 2. 核心服务

`RoguelikeService` 负责 orchestrate：
1. 基于 `userKey` 从 `SklandTokenStore` 取出森空岛 Token，调用 `RoguelikeAuthService` 完成认证并得到 `cred/token/uid`。
2. 调用 `RoguelikeHttpClient` 拉取最新 `/game/arknights/rogue` 数据。
3. 合并记录：`RoguelikeRunRepository.saveRuns(uid, themeId, records)`。
4. 选择主题：
   - 从 API `topics` 或调用参数得到 `themeId/name`；
   - 通过 `RoguelikeThemeRegistry` 获取配置与对应 Analyzer。
5. 计算结果：`analyzer.analyze(topic, historyFromRepository)`，输出 `RoguelikeAnalysisResult`。

## 3. 缓存策略

- `ConcurrentHashMap` 缓存 Key = `uid:themeId`，Value = `CacheEntry(result, expireAt)`；
- 默认 TTL 5 分钟，可按需修改；
- `getAnalysis(userKey, themeId, refresh)`：
  - `refresh=true` → 触发远端拉取 + 覆盖缓存；
  - `refresh=false` → 若缓存过期则使用 DB 历史重新分析。
- `listAnalyses(userKey)`：遍历所有主题，返回缓存或 DB 结果（若历史为空则跳过）。

## 4. 对外接口

- `refreshAndAnalyze(String userKey, String themeIdOrName)`：
  - 用于主动拉最新数据（Bot/前端刷新按钮）。
- `getAnalysis(String userKey, String themeIdOrName, boolean refresh)`：
  - 读取缓存或重新分析，方便 API 查询。
- `listAnalyses(String userKey)`：
  - 针对仪表盘/管理后台展示所有主题的概览。

## 5. 测试建议

- 单元/集成测试可使用 MockWebServer 或 stub repository，验证：
  - 缓存命中/过期；
  - 多主题处理；
  - accountService 绑定行为。
- 当前阶段依赖现有 `mvn test` 覆盖基础流程，后续可在 Stage5/Stage6 增强。

（后续阶段将在该服务基础上暴露 REST API 并接入前端/Bot。）
