# QQ Bot Agent 指南

## 模块概览

- **技术栈**：Kotlin 2.0.21、JVM 17、Maven、Mirai Core 2.16.0、OkHttp 4.12。
- **入口类**：`com.karaik.gamebot.bot.AknBotLauncher`。
- **当前功能**：初始化 Mirai 配置、读取 `bot-config.yml`，并通过 `BackendHealthProbe` 定时请求后端 `/api/health`，以验证链路连通性。
- **配置文件**：`src/main/resources/bot-config.yml` 保存 QQ 账号、后端地址与健康检查周期，由 `BotConfigLoader` 解析。

## 常用命令

```bash
mvn -q test    # Kotlin Test + JUnit5，含 MockWebServer 用例
mvn package    # 生成可运行的 jar（未来部署可复用）
```

测试会使用 MockWebServer 模拟后端，因此无需真实服务即可验证 YAML 读取与 JSON 解析逻辑。

## 扩展指南

1. **Mirai 登录**：待准备好凭证管理后，可在 `AknBotLauncher` 中引入 `BotFactory.newBot(...)` 实际登录。
2. **HTTP 调用**：可复用 `BackendHealthProbe` 的封装，或按接口创建新的数据类并用 Jackson/ kotlinx-serialization 解析。
3. **调度**：目前采用 `ScheduledExecutorService`，若后续需要更复杂任务，可迁移到协程或 Quartz，并在此记录决策。
4. **测试策略**：涉及 HTTP 的部分优先使用 MockWebServer；调度/业务逻辑可用 Kotlin 测试替身，保持测试快速稳定。

## 交接前检查

- 非必要不要改动 `bot-config.yml` 默认值；如确有需要，请在本文写明原因。
- 保持 `mvn -q test` 绿色。
- 若新增构建、部署或运行方式，务必同步更新本 `Agent.md` 以便下一位代理延续工作。
