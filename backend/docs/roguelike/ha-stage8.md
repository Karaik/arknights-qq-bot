# 集成战略模块阶段 8 – 高可用评估与整改计划

本阶段聚焦于后端在多人协作、长时间运行场景下的可用性，评估现状并提出整改方案，确保后续接入方（Bot、前端、第三方服务）可以稳定使用肉鸽数据能力。

## 1. 体系概览

```
Client (Bot/Swagger) ──> API Gateway (Spring Boot)
                              │
                              ├─ SklandCredentialController → SklandTokenStore / RoguelikeAccountService
                              └─ RoguelikeController → RoguelikeApplicationService
                                              ├─ RoguelikeAuthService → RoguelikeHttpClient → Hypergryph/Skland
                                              ├─ RoguelikeRunRepository (MyBatis-Plus + DB)
                                              └─ RoguelikeThemeAnalyzers + 缓存
```

应用层 (`DefaultRoguelikeApplicationService`) 负责 orchestrate，控制器仅专注于鉴权和入参校验。日志统一使用 `event=... key=value` 格式，异常由 `RoguelikeExceptionHandler` 归一化为业务响应。

## 2. 现状评估

| 模块 | 现状 | 风险 |
| --- | --- | --- |
| SklandTokenStore | InMemory 实现，随实例掉电丢失 | 节点重启后需人工重新绑定，无法横向扩展 |
| RoguelikeAccountService | InMemory 映射 | 与 TokenStore 相同问题 |
| RoguelikeAuthService / HttpClient | 单线程串行访问官方接口，无重试/限流 | 官方临时抖动会直接暴露给调用方；频繁失败可能触发封禁 |
| 缓存策略 | 应用层本地 5 分钟 TTL | 多节点部署会缓存不一致；缺乏主动失效策略 |
| DAO 层 | MyBatis-Plus + MySQL/H2 | 缺少慢查询监控、表结构索引尚未针对大数据量调优 |
| 监控与告警 | 仅日志手工排查 | 无外部指标、无告警渠道 |
| 文档 & 接入规范 | 阶段 0-7 已发布，但缺少高可用与运维建议 | 新同学难以理解扩容、降级方案 |

## 3. 改进路线

1. **状态存储持久化**  
   - 将 `SklandTokenStore`、`RoguelikeAccountService` 改为基于数据库或 Redis 的实现，提供冷热备份策略；  
   - 支持按环境区分（dev 用内存，prod 用持久化）。

2. **认证链路容错**  
   - 在 `RoguelikeHttpClient` 引入超时、重试、熔断配置（Resilience4j/Spring Retry）；  
   - 按外部接口类型埋点 metrics（成功率、延迟、401/429 次数），必要时加入限流/延迟队列。

3. **缓存升级**  
   - 将 5 分钟缓存迁移到集中式组件（Redis 或 Caffeine + distributed cache），允许多实例共享；  
   - 提供手动失效 API，便于管理员在发现数据异常时强制刷新。

4. **日志 & 追踪**  
   - 统一 log pattern（event、userKey、requestId），引入 MDC/requestId 贯穿调用链；  
   - 关键节点（绑定、刷新、外部接口错误）输出结构化 JSON 或发送到 APM。

5. **运维与告警**  
   - 将 `mvn test`、Swagger 生成、数据库迁移纳入 CI；  
   - 配置 Prometheus/Micrometer 指标：`hypergryph_call_total`、`roguelike_refresh_duration` 等；  
   - 设定告警：认证 401/429、DB 写入失败、缓存命中率过低。

6. **文档与协作**  
   - 根据上述改动同步 `Agent.md`、README、Stage 文档；  
   - 在 `todolist1.md` 的阶段 9-10 中拆解实施步骤，便于多位同学并行处理。

## 4. 执行建议

1. **短期 (本阶段)**  
   - 完成代码重构：统一应用层入口、日志规范、Javadoc；  
   - 输出本高可用评估文档，并在 todolist 中跟踪整改项。

2. **中期 (Stage 9)**  
   - 落地持久化 TokenStore、AccountService；  
   - 引入集中式缓存与基础监控面板；  
   - 完成配置管理（dev/test/prod）与 secrets handling。

3. **长期 (Stage 10+)**  
   - 结合消息队列/任务调度实现定时刷新；  
   - 构建回放/自愈机制（例如失败自动降级为缓存数据、人工触发重试）；  
   - 对外接口支持批量/异步调用，以满足更大规模的 Bot 请求。

通过以上改造，系统可以从单实例 POC 演化为可横向扩展、具备监控告警能力的企业级服务，同时降低新人加入时的理解成本。后续所有 PR 需对照该文档检查是否破坏既定的高可用策略。 
