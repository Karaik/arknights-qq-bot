# Arknights QQ Bot – Agent 操作指南

本仓库同时包含 Spring Boot 后端、React/Vite 前端与 Kotlin/Mirai QQ 机器人三个可部署目标，它们共用同一业务模型与接口。所有代码代理在开始工作前都应阅读本文件，以免破坏跨模块约定。

## 总览

- **backend/**：Java 17 + Spring Boot 3.5.7，已提供 `/api/health`，并集成 MyBatis-Plus、SpringDoc 等通用依赖。
- **frontend/**：React 18 + TypeScript + Vite，包含 Login、Dashboard 占位页面以及统一的 HTTP 请求封装。
- **bot/**：Kotlin 17 + Mirai + OkHttp，提供配置加载与定时调用后端健康检查的调度器，后续可在此基础上实现 QQ 指令逻辑。
- **Roguelike 规划**：阶段文档位于 `backend/docs/roguelike/`（Stage0 架构、Stage1 平台、Stage2 持久化、Stage3 主题、Stage4 服务、Stage5 API、Stage6 测试、Stage7 联调文档），所有后续实现须遵守分层与数据库流程；SQL 统一存放在 `database/ddl/`，便于 DBA 审核与执行。每个 userKey 需通过 `/api/skland/credentials/{userKey}` 绑定森空岛 Token/UID 后才能刷新/查询。

## 常用操作

1. **安装依赖环境**
   - Java 17 / Maven 3.9+
   - Node.js 20 LTS（自带 npm）
   - Kotlin 编译器由 Maven 插件自动拉取
   - 进行后端（开发/测试）时，需要配置环境变量 `HYPERGRYPH_TOKEN`（可复制 `backend/.env.example` 为本地 `.env`，或直接在 shell 环境中导出）
2. **保持构建通过**
   - 后端：`mvn -q -f backend/pom.xml test`
   - 前端：在 `frontend/` 目录运行 `npm install && npm run build`
   - 机器人：`mvn -q -f bot/pom.xml test`
3. **接口/DTO 联动流程**
   - 先在后端更新实体/DTO/返回体；
   - 再同步前端 `src/api`、`src/types` 中的类型；
   - 最后更新机器人侧 JSON 解析（例如 `BackendHealthProbe` 或新建的数据类）。

## 目录速查

| 目录 | 说明 |
|------|------|
| `backend/` | Spring Boot 服务，已有统一响应封装与 Health 控制器 |
| `frontend/` | Vite 项目，包含基础页面与 API 工具 |
| `bot/` | Mirai 机器人，支持 YAML 配置与后端健康轮询 |
| `docs/` | 预留的设计文档目录 |

## 测试要求

- 后端测试放在 `backend/src/test/java`，使用 Spring Boot Test + MockMvc。
- 前端改动最少要跑 `npm run build`；涉及复杂逻辑时补充 Vitest。
- 机器人模块用 Kotlin Test + JUnit5，HTTP 交互可以借助 MockWebServer。

## 交付清单

1. 修改任意模块后务必同步更新对应的 `Agent.md`。
2. 运行该模块的测试或构建命令，确保无错误。
3. 若调整了公共契约（DTO、路由、环境变量等），保证其它模块也完成同步。

遵守以上规则即可让新的代码代理快速介入并持续扩展该项目。
