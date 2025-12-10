# Arknights Bot Backend

基于 Spring Boot 的后台服务，当前聚焦鹰角/森空岛鉴权链路，供后续机器人或前端使用。

## 快速开始
1) 安装 Maven/Java 17，进入 `backend`。
2) 配置 `src/main/resources/application.yml`：填写 `auth.hypergryph.api-key`（可为空禁用），确认域名/路径/UA。
3) 运行：`mvn spring-boot:run` 或 IDE 启动主类 `com.karaik.gamebot.ArknightsBotApplication`。
4) 文档：`/swagger-ui.html` 或 `/doc.html`（Knife4j，已注入 `X-API-KEY` 头）。

## 配置要点
- `auth.hypergryph.*`：登录接口路径、UA、`api-key`、`phone_code_cooldown_seconds`（短信冷却）。
- `auth.skland.*`：cred 生成/校验路径，签名字段（当前 `/user/check` 无需签名）。
- 端口默认 `1230`，可在 `application.yml` 修改。

## 可用接口
- `POST /api/auth/send_phone_code`：`{phone,type}`，`type=2` 必填，受冷却限制。
- `POST /api/auth/token_by_phone_code`：`{phone,code}`。
- `POST /api/auth/token_by_phone_password`：`{phone,password}`。
- `POST /api/auth/grant`：`{token}` -> `oauth_code + uid`。
- `POST /api/auth/generate_cred_by_code`：`{code,kind=1}` -> `cred + token`。
- `GET /api/auth/user/check`：Header `Cred=<cred>`，官方 `/user/check`。

## 开发规范
- 必带 Header `X-API-KEY`（配置为空则关闭）；日志脱敏，注释中文说明固定值/规则。
- DTO 置于 `auth/dto/request|response`，业务在 Service，Controller 仅编排。
- 更新配置字段需同步 `AuthProperties` 与文档 `01_auth_api_flow.md`。

## 已知风险
- 人机验证：短信登录可能返回极验挑战，当前未自动过验，需要前端完成后携票据重试。
- 配置绑定：`AuthProperties` 字段为下划线命名，确保 yml 键符合松散绑定规则。
