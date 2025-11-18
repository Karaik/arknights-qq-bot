# 集成战略模块阶段 6 – 测试与验证

## 1. 单元 / 组件测试

- `RoguelikeSignatureHelperTest`：验证签名算法可稳定生成 `sign`。
- `RoguelikeRunRepositoryTest`：使用 H2 + MyBatis-Plus 验证 `saveRuns` / `listRuns`。
- `RoguelikeThemeRegistryTest`：确保 `roguelike/themes/*.json` 能正确加载。
- `SkadiThemeAnalyzerTest`：构造样例数据，校验胜率与近期对局统计。
- `RoguelikeControllerTest`：基于 MockMvc 验证 API Key 校验、主题列表、分析刷新等行为。

以上覆盖了 Stage 1~5 关键模块，后续新增服务/主题时需同步补充测试。

## 2. 集成测试

- `mvn -q -f backend/pom.xml test`：启动 Spring Boot 上下文，模拟完整流程（H2 数据源 + Controller + Service）。
- 若需要连真实 MySQL，可切换 `SPRING_PROFILES_ACTIVE=mysql`，当前配置指向本地 `mysqlLocal (root/123456)`。

## 3. 性能基线

- 目前数据规模较小（测试环境仅存储少量对局记录），`mvn test` 全流程约 12s 完成；
- `RoguelikeService` 在内存中缓存 5 分钟结果，避免频繁访问远端 API；
- 后续若出现大量历史记录，可在 `RoguelikeRunRepository` 上增加分页/统计索引，并通过 JMH/自研脚本评估。

## 4. 文档

- 根 `Agent.md`、`backend/Agent.md` 以及 `todolist1.md` 已反映最新测试状态；
- 每当新增测试工具或性能结论，请更新本文件，确保阶段记录完整。
