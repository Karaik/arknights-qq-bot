# 集成战略模块阶段 7 – 接口文档与联调准备（Backend）

## 1. Swagger/OpenAPI 输出

- Swagger UI：`http://localhost:8080/swagger-ui.html`；
- Knife4j UI：`http://localhost:8080/doc.html`（包含接口分组、调试历史等增强体验）；
- 机器可读的 OpenAPI JSON：`GET /v3/api-docs`；
- 依赖 `springdoc-openapi-starter-webmvc-ui`，如需导出 YAML，可使用 `?format=yaml` 参数或通过 CI 管道自动下载。

## 2. 安全要求

- 所有肉鸽接口需要 Header `X-API-KEY`，配置项 `roguelike.api-key` 定义密钥；
- Swagger 页面已通过 `@SecurityScheme(name = "roguelikeApiKey")` 表达，当在 Swagger UI 中填写 API Key 后即可调试。

## 3. Mock/示例

- 主题列表示例：
  ```http
  GET /api/roguelike/themes
  Header: X-API-KEY: local-dev-key
  Response:
  {
    "code": 0,
    "message": "ok",
    "data": [
      {"themeId": "rogue_4", "name": "萨卡兹的无终奇语"}
    ]
  }
  ```
- 刷新数据示例：
  ```
  POST /api/roguelike/user123/refresh?themeId=rogue_4
  Header: X-API-KEY: local-dev-key
  ```
  返回 `RoguelikeAnalysisResult`，字段见 Stage5 说明。

## 4. 联调指引

1. 先调用 `POST /api/skland/credentials/{userKey}` 绑定森空岛 Token（必要时附带 UID）；
2. 调用 `GET /api/roguelike/themes`，获取可用主题；
3. 先 `POST /api/roguelike/{userKey}/refresh` 拉取最新数据；
4. 再 `GET /api/roguelike/{userKey}/{themeId}/analysis` 获取展示数据；
4. 如果接口需要 Mock，可直接使用上述返回例子或从 Swagger 下载 JSON。

文档变更请同步更新 `Agent.md` 以及本文件，保持阶段记录。
