# 集成战略模块阶段 3 – 主题配置与解析接口

## 1. 资源结构

- `backend/src/main/resources/roguelike/themes/`：存放每个主题的配置文件，命名规则 `rogue_<序号>.json`。
- 配置内容对应 `RoguelikeThemeConfig`，字段包含 `keys`、`analysisRules`、`endingRules`、`statsDefinitions` 等，便于每个主题自定义。

## 2. 注册与加载

- `RoguelikeThemeRegistry` 在启动时扫描 `classpath*:roguelike/themes/*.json`，利用 `ObjectMapper` 解析并缓存：
  - `findByIdOrName`：按 `themeId` 或中文名检索；
  - `getRequired`：未找到时抛出 `RoguelikeConfigException`；
  - `listThemes`：供前端列出可用主题。

## 3. Analyzer 规范

- `RoguelikeThemeAnalyzer` 接口：
  - `supports(String themeIdOrName)`：是否支持指定主题；
  - `RoguelikeAnalysisResult analyze(Map<String,Object> rawTopic, List<Map<String,Object>> historyRecords)`。
- `AbstractThemeAnalyzer` 提供共性实现：
  - 基于 `RoguelikeThemeConfig` 判断有效对局、计算胜率、生成最近若干场的 `RoguelikeRunInsight`；
  - `SkadiThemeAnalyzer` 作为首个主题实现（对应 `rogue_4`）。
- 输出 DTO：
  - `RoguelikeAnalysisResult`（`themeId/name`、总场次、胜率、近战列表）；
  - `RoguelikeRunInsight`（runId、是否成功、结局、分队、开始日期）。

## 4. 扩展流程

新增主题时：
1. 在 `resources/roguelike/themes/` 新增 JSON（参考 `rogue_4.json`）；
2. 实现对应的 Analyzer（可继承 `AbstractThemeAnalyzer`）并注册为 Spring Bean；
3. 如需特殊逻辑，可在子类覆写 `isValidRun`、`success` 或封装额外字段；
4. 更新阶段文档与 `Agent.md`，说明主题接入情况。

## 5. 测试建议

- Registry 层面：编写单测验证 JSON 可被正确加载、识别；
- Analyzer 层面：构造样例 `historyRecords`，确保 `totalRuns/winRate/recentRuns` 计算正确；
- 后续阶段可结合 Repository/Service 做集成测试。

（阶段 3 完成，后续阶段将基于这些配置与解析器实现业务服务与 API 输出。）
