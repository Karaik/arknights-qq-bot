# 集成战略模块阶段 0 方案

本文件记录阶段 0 的调研成果，确保后续实现遵守企业级分层与扩展要求。

## 1. 目标与范围

- 构建 `com.karaik.gamebot.roguelike` 领域，为森空岛集成战略（肉鸽）提供统一业务能力；
- 允许在同一套流程内接入多个主题（如萨卡兹的无终奇语、宾夕法尼亚巨炮等），并为未来的非肉鸽模式预留扩展空间；
- 在后端开发/测试阶段，通过环境变量 `HYPERGRYPH_TOKEN` 提供凭证，避免泄漏到版本库。

## 2. 包及层级设计

```
com.karaik.gamebot
└── roguelike
    ├── client          # HTTP 请求、签名算法、DTO（grant/cred/binding/rogue info）
    ├── config          # Spring @ConfigurationProperties、主题配置装载、Registry
    ├── domain
    │   ├── dto         # 对外暴露的响应 DTO
    │   └── model       # 内部模型，如 RawData、RunRecord
    ├── repository      # MyBatis-Plus Mapper + Repository 接口
    ├── service         # 核心服务（拉取数据、落库、缓存、聚合分析）
    └── theme
        ├── api         # `RoguelikeThemeAnalyzer` 接口与抽象基类
        ├── skadi       # “萨卡兹的无终奇语”实现
        └── ...         # 其他主题按 `theme.<key>` 新增
```

- 未来引入其他业务（例如“公开招募分析”）时，可在 `com.karaik.gamebot.<newdomain>` 内复用类似层级；
- 各层之间只允许同级/下游依赖，禁止跨层直接访问（例如 Controller 只调 Service，Service 调 Repository + Theme Analyzer）。

## 3. 主题与配置策略

| 主题代号 | 主题名称        | 备注                       |
|----------|-------------|----------------------------|
| `rogue_4`| 萨卡兹的无终奇语    | 首个实现，已有完整配置示例 |
| `rogue_3`| 萨米          | 待补充配置                 |
| `rogue_2`| 水月与深蓝之树（示例） | 待补充配置                 |

- 每个主题在 `resources/roguelike/themes/<id>.json` 内维护规则：
  - `keys`：API 字段映射；
  - `analysisRules`：过滤和统计阈值；
  - `endingRules`：特殊结局与滚动判定；
  - `statsDefinitions`：胜率、连胜等指标说明。
- `RoguelikeThemeRegistry` 负责将配置加载为 POJO，并按 `themeId/name` 提供查询。

## 4. 凭证与安全

- 在 `backend/.env.example` 提供 `HYPERGRYPH_TOKEN` 样例，开发者复制为 `.env` 或直接设置 shell 环境变量；
- `application.yml` 通过 `hypergryph.token=${HYPERGRYPH_TOKEN:}` 引用环境变量，实现零硬编码；
- 生产环境需通过 CI/CD 的 Secrets 或密钥管理服务注入该变量；
- 所有日志、异常信息禁止打印完整 Token。

## 5. 交付要求

- 阶段 0 完成后已具备清晰的包结构、主题列表与安全策略；
- 后续阶段在实现前，应先检查本文件，确保新代码符合层级约束；
- 每完成一个阶段，需同步更新 `backend/Agent.md` 与根 `Agent.md`，记录新的模块/接口/配置。

（阶段 0 完成，后续按 `backend/todolist1.md` 阶段 1 开始编码。）

