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

## 实现注意事项

1. **分层约定**：参考 `prompt-init.md`，DTO 放在 `domain/dto`，Service 实现在 `service/impl`，Controller 区分 `api` 与 `internal`。
2. **MyBatis-Plus**：实体使用 `@TableName`、`@TableId`，自定义 SQL 放入 `src/main/resources/mapper`。
3. **参数校验**：优先使用 `jakarta.validation` 注解并配合 `@Valid`，异常统一转换成 `ApiResponse#error`。
4. **OpenAPI**：新增 Controller 自动出现在 SpringDoc 中，路径务必置于 `/api` 前缀下。
5. **测试**：新增接口时复制 `HealthControllerTest` 的写法，用 MockMvc 校验状态码与响应结构。

## 交接前检查

- 若引入新的配置文件、Profile 或跨模块契约，务必同步更新本 `Agent.md`。
- 保证 `application.yml` 在无外部依赖下亦可运行（H2 或 Mock 服务）。
- 提交前必须让 `mvn -q test` 通过。

