# Backend Agent 指南

## 模块概览

- **技术栈**：Java 17、Spring Boot 3.5.7、MyBatis-Plus、SpringDoc，开发态默认使用 H2，生产可切换至 MySQL。
- **入口类**：`com.karaik.gamebot.ArknightsBotApplication`。
- **已实现接口**：`GET /api/health`，返回 `{"code":0,"message":"ok","data":"ok"}`。
- **统一响应体**：`com.karaik.gamebot.common.api.ApiResponse`，所有 REST 接口需使用该封装。

## 本地命令

```bash
mvn -q test          # Spring Boot + MockMvc 单测
mvn spring-boot:run  # 以 dev 配置启动（内存数据库，端口 8080）
```

默认激活 `dev` profile，使用 MySQL 模式的内存 H2。若需要连接真实 MySQL，请设置 `SPRING_PROFILES_ACTIVE=mysql` 并在 `application.yml` 中补充账号信息。
- 森空岛 Token 通过调用 `POST /api/skland/credentials/{userKey}` 并在 Header 中填写 `X-SKLAND-TOKEN` 来绑定，无需配置额外环境变量。

## 实现注意事项

1. **分层约定**：参考 `prompt-init.md`，DTO 放在 `domain/dto`，Service 实现在 `service/impl`，Controller 区分 `api` 与 `internal`。
2. **MyBatis-Plus**：实体使用 `@TableName`、`@TableId`，自定义 SQL 放入 `src/main/resources/mapper`。
3. **参数校验**：优先使用 `jakarta.validation` 注解并配合 `@Valid`，异常统一转换成 `ApiResponse#error`。
4. **OpenAPI**：新增 Controller 自动出现在 SpringDoc 中，路径务必置于 `/api` 前缀下。
5. **测试**：新增接口时复制 `HealthControllerTest` 的写法，用 MockMvc 校验状态码与响应结构。
- **Roguelike 模块规划**：阶段 0 已在 `backend/docs/roguelike/architecture-stage0.md` 定义 `com.karaik.gamebot.roguelike` 包结构与多主题策略，实现时需遵循该分层。
- **通信/认证基线**：阶段 1 文档 `backend/docs/roguelike/platform-stage1.md` 记录了配置模型、签名算法、HTTP 客户端与认证流程，请在扩展时保持一致。
- **持久化落地**：阶段 2 在 `backend/docs/roguelike/persistence-stage2.md` 描述 `roguelike_run` 表、MyBatis-Plus 实体与 `RoguelikeRunRepository`，以及 `scripts/sqlite_to_mysql.py` 的迁移用法；SQL 脚本统一放在根目录 `database/ddl`，`dev` Profile 自动执行 `V1__init_roguelike_schema.sql`。
- **主题配置与解析**：阶段 3 文档 `backend/docs/roguelike/theme-stage3.md` 描述 `roguelike/themes/*.json`、`RoguelikeThemeRegistry` 与 `RoguelikeThemeAnalyzer` 的扩展方式，新增主题时务必遵循。
- **业务服务层**：阶段 4 文档 `backend/docs/roguelike/service-stage4.md` 介绍了 `RoguelikeService`、账号映射与缓存策略；使用 `RoguelikeAccountService` 绑定多 UID。
- **API 层**：阶段 5 文档 `backend/docs/roguelike/api-stage5.md` 描述 REST 接口、API Key 鉴权与响应模型。
- **森空岛凭证**：通过 `POST /api/skland/credentials/{userKey}` 绑定每个 userKey 的森空岛 Token/UID，`RoguelikeService` 会自动读取 `SklandTokenStore` 调用官方接口。
- **测试与验证**：阶段 6 文档 `backend/docs/roguelike/testing-stage6.md` 汇总单元/集成测试覆盖与性能基线。
- **联调文档**：阶段 7 文档 `backend/docs/roguelike/integration-stage7.md` 说明 Swagger/OpenAPI 获取方式、Mock 数据与调用顺序。
- **调用 Flow 参考**：`backend/docs/roguelike/backend-call-flow.md` 以图文并茂的形式描述 Token 绑定、刷新、分析的具体链路与配置项归属，排查参数问题时可直接对照。

## 交接前检查

- 若引入新的配置文件、Profile 或跨模块契约，务必同步更新本 `Agent.md`。
- 保证 `application.yml` 在无外部依赖下亦可运行（H2 或 Mock 服务）。
- 提交前必须让 `mvn -q test` 通过。
