# 集成战略模块阶段 5 – API 层与对接

## 1. 访问鉴权

- 所有 `RoguelikeController` 下的接口默认要求请求头 `X-API-KEY`；
- 配置项 `roguelike.api-key` 定义密钥（`application.yml` 默认 `local-dev-key`，生产请注入真实值）；
- 若配置为空则不校验，便于本地调试。

## 2. REST 接口

| Method & Path | 描述 | 请求参数 | 备注 |
|---------------|------|----------|------|
| `POST /api/skland/credentials/{userKey}` | 绑定/更新森空岛 Token 与 UID | Header：`X-API-KEY`、`X-SKLAND-TOKEN`；Body：`uid`（可选） | 使用 userKey 管理各自的森空岛凭证 |
| `GET /api/roguelike/themes` | 列出所有可用主题 | Header `X-API-KEY` | 返回 `RoguelikeThemeSummary` 列表 |
| `GET /api/roguelike/{userKey}/{themeId}/analysis?refresh=false` | 查询指定主题的分析结果 | `userKey`：业务标识（将用于查找已绑定的 UID/token）；`themeId`：主题 ID（或名称）；`refresh`：可选，是否强制拉取最新 | 若缓存/数据库无数据，返回 404 |
| `POST /api/roguelike/{userKey}/refresh?themeId=` | 立即拉取并分析最新数据 | `userKey`：业务标识；`themeId`：可选，未提供则使用 API 返回的当前主题 | 适用于 Bot 主动刷新 |

- 响应均使用 `ApiResponse<T>` 包裹，成功 `code=0`，失败返回对应 HTTP 状态。
- `RoguelikeAnalysisResult` 字段：`themeId/name/totalRuns/winRate/recentRuns`；`recentRuns` 为 `RoguelikeRunInsight`。

## 3. 前端/Bot 对接

- Bot 或前端需持有 API Key，调用 `refresh` → `analysis` 的顺序即可获取最新数据；
- 也可以调用 `themes` 展示下拉列表；
- 后续若扩展更多字段（例如 career、近 7 日统计），在 `RoguelikeAnalysisResult` 中新增即可。

## 4. 文档同步

- 根 `Agent.md`、`backend/Agent.md` 已更新 API 位置与鉴权策略；
- 如需新增路径，请先更新本文件，再补充 Swagger/Javadoc，保持阶段记录。
