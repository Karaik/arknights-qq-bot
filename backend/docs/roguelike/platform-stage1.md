# 集成战略模块阶段 1 – 通信与认证

## 1. 配置模型

- `hypergryph.*` 配置通过 `RoguelikeApiProperties` 暴露，仅包含：
  - `endpoint`：`grant/cred/binding/rogue-info` 四个固定 URL；
  - `app`：`appCode`、`userAgent`、`versionName`。
- 森空岛 Token 不再写入配置/环境变量，而是由调用方在 `POST /api/skland/credentials/{userKey}` 时通过 Header `X-SKLAND-TOKEN` 传入并保存到 `SklandTokenStore`。
- YAML `backend/src/main/resources/application.yml` 仅记录固定端点与 UA，便于定制。

## 2. 签名算法与 HTTP 客户端

- `RoguelikeSignatureHelper` 负责生成请求头：
  - 构造 `platform/timestamp/dId/vName` JSON；
  - 计算 `HMAC-SHA256(token, path + query + body + timestamp + headersJson)`；
  - 对结果再做 MD5，即 `sign`。
- `RoguelikeHttpClient` 基于 `WebClient`：
  - `requestOAuthCode`、`requestCredAndToken` 走 JSON POST；
  - `requestBindings`、`requestRogueInfo` 走带签名 Header 的 GET；
  - 所有异常统一封装为 `RoguelikeApiException`，方便上层捕获。

## 3. 认证流程

`RoguelikeAuthService.authenticate()` 步骤：
1. 校验 token 是否存在；
2. 调用 `/grant` 获取 `oauth_code`；
3. 调用 `/generate_cred_by_code` 获得 `cred/token`；
4. 调用 `/game/player/binding` 获取 `uid`（筛选 `appCode=arknights`）；
5. 返回 `AuthFlow(cred, token, uid)`（供后续拉取 Rogue 数据）。

## 4. 测试策略

- `RoguelikeSignatureHelperTest` 验证签名输出非空且包含必要 header；
- `mvn -q -f backend/pom.xml test` 覆盖 Spring Boot 现有测试；
- 未来可通过 MockWebServer/WireMock 补充更多 HTTP 单元测试。

## 5. 文档同步

- 根 `Agent.md` 与 `backend/Agent.md` 已增加 Stage1 描述；
- 若后续调整端点/认证方式，需要同步更新本文件与 `Agent` 文档，保持阶段记录。
