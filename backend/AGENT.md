请严格遵循以下约定与流程。

## 技术栈与结构
- **框架**：Spring Boot 3.5.7（Web + WebFlux），Java 17。
- **HTTP 客户端**：`WebClient`。
- **文档**：Springdoc + Knife4j（访问 `/swagger-ui.html` 或 `/doc.html`，以 `Knife4j` 为主，已注入 `X-API-KEY` 全局头）。
- **模块划分**：按功能分包，功能包内再按职责细分（controller/service/dto/config/client/support 等）：
  - `auth/*`：登录鉴权链路（controller/service/dto/request|response/config/client/support）。
  - `common/*`：通用响应、拦截器、OpenAPI 配置。
- **入口**：`com.karaik.gamebot.ArknightsBotApplication`.

## 分层/分包约定
- 先按领域分一级包，再在包内按职责分子包（controller、service、dto、config、client、support）。新增功能请创建新的一级功能包，勿把所有 controller/service 混在同级。

## 配置与运行
- 配置文件：`src/main/resources/application.yml`
  - `auth.hypergryph.*`：登录相关路径、UA、`api-key`、短信冷却时间等。
  - `auth.skland.*`：森空岛 cred 生成/校验路径，签名字段。
  - 端口默认 `1230`（如需更改同步文档）。
- **注意**：`AuthProperties` 字段使用下划线命名（如 `base_url`），配置键必须与之匹配（`base-url` 可松散绑定到 `base_url`）。
- 运行：`mvn spring-boot:run`（需要本地 Maven），或使用 IDE 运行主类。

## 全局约束
- 所有接口必须校验 Header `X-API-KEY`（默认值见配置 `auth.hypergryph.api-key`，为空则关闭校验）。
- 日志使用中文提示，敏感字段需脱敏（手机号只保留后 4 位）。
- 代码注释中文，记录业务规则/固定参数来源。
- 注释格式：若某行/块需要解释，注释放在目标代码的上一行，并与上一行代码留一空行。
- 禁止引入未在配置声明的常量；固定值（如短信 type=2）需在代码/注释中标明来源。

## 已实现的 Auth 接口（路径遵循官方命名）
- `POST /api/auth/send_phone_code`：body `{phone, type}`，`type` 必须为 `2`，内置冷却（配置 `phone_code_cooldown_seconds`）。
- `POST /api/auth/token_by_phone_code`：body `{phone, code}`，短信登录取 token。
- `POST /api/auth/token_by_phone_password`：body `{phone, password}`。
- `POST /api/auth/grant`：body `{token}`，返回 `oauth_code + uid`。
- `POST /api/auth/generate_cred_by_code`：body `{code, kind=1}`，返回 `cred + token`。
- `GET /api/auth/user/check`：Header `Cred=<cred>`，官方 `/api/v1/user/check`，无需签名/token。

## 外部接口调用注意事项
- 对于官方 API，请直接参考 `src/main/resources/skland_api_doc.md`。
- **token_by_phone_code**：可能返回人机验证挑战（极验）。当前后端未自动过验，需前端拿 challenge/gt 完成验证码后重试（可扩展 DTO/返回结构标注挑战信息）。
- **签名规则**（仅用于需要 sign 的接口，当前 `/user/check` 不需要）：
  - 基础头：`platform/timestamp/dId/vName`（配置 `auth.skland`）。
  - 字符串：`path + query(无?) + bodyJson + timestamp + headersJson`。
  - `sign = md5(hmac_sha256(token, 字符串))`，token 为 `generate_cred_by_code` 返回。
- 速率限制：短信接口按手机号冷却 `phone_code_cooldown_seconds` 秒，命中返回 400 和剩余秒数。

## 错误处理与日志
- 控制器统一包装 `ApiResponse`（code/message/data），`WebClientResponseException` 打印状态码与响应体。
- 参数校验失败返回 400，业务规则（如 type≠2、冷却中、cred 为空）使用 `IllegalArgumentException` 抛出。
- 请避免在日志中输出完整凭证/token/cred。

## 开发规范
- DTO 放在 `auth/dto/request|response`。
- Service 只做业务与外部调用封装；Controller 不做业务判断。
- 配置新增字段需同步到 `application.yml`（含注释）与 `AuthProperties`。
- 文档更新：
  - `src/main/resources/01_auth_api_flow.md`：接口路径/参数、固定值、冷却等。
  - 根级 README（本文件同级）简述功能与快速启动。

## 待办/风险
- 人机验证未实现自动化，需要产品决定接入极验或前端/Koishi 完成挑战后带票据重试。
- 配置字段下划线与松散绑定：若启动时报缺少配置，请确认 yml 键符合下划线字段。

遵循以上规范可直接参与开发，新增接口务必与文档和配置保持同步。若有破坏性改动，更新本文件及 README。
