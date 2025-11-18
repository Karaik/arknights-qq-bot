# research1 – `example` 项目解析与 JSON 语义整理

本笔记基于 `backend/src/main/resources/example` 下的 Python 项目（罗德岛集成战略分析仪），梳理其完整流程与森空岛集成战略 API 的数据字段含义。内容可直接用于在 Spring Boot 后端中重建相同能力。

> 说明：示例项目使用 `.env` 加载 `HYPERGRYPH_TOKEN` 作为桌面应用输入方式；当前 Spring Boot 后端已改为通过接口 Header `X-SKLAND-TOKEN` 绑定凭证，不再依赖环境变量。

## 1. 运行流程概览

1. **凭证引导 (`src/bootstrap.py`)**
   - 应用首次运行会在持久化目录写入 `.env` 模板 `HYPERGRYPH_TOKEN=""` 并弹窗提醒填写；
   - 通过 `python-dotenv` 读取 `HYPERGRYPH_TOKEN`，若为空则提示退出。

2. **启动入口 (`main.py`)**
   - `load_config()` 读取 `config/app_config.ini`，该文件提供 API 基地址、`USER_AGENT` 等常量；
   - `setup_logging()` 将日志落地到 `logs/app.log`，同时输出到标准输出；
   - `SklandClient.authenticate(token)` 执行完整认证（下文详述）。若失败，直接弹窗并终止；
   - 构造 `RogueService`（业务分析）、`AppWindow`（Tk UI）和 `UIController`，默认主题为“萨卡兹的无终奇语”。

3. **认证 & 数据抓取 (`src/api/skland_client.py`)**
   1. `POST https://as.hypergryph.com/user/oauth2/v2/grant` 传入 `token` 与 `appCode`，获取 `oauth_code`；
   2. `POST https://zonai.skland.com/api/v1/user/auth/generate_cred_by_code` 传 `kind=1` 与 `code`，得到 `cred` 与 `token`（用于后续签名）；
   3. `GET https://zonai.skland.com/api/v1/game/player/binding` 携带签名头部，筛到 `appCode=arknights` 的 `uid`；
   4. `GET https://zonai.skland.com/api/v1/game/arknights/rogue?uid=xxx` 抓取集成战略数据（主体 JSON）。

   > **签名算法**：按照 `path + query + body + timestamp + headers_for_sign_json` 拼接字符串，用二次摘要（`HMAC-SHA256` -> `MD5`）生成 `sign`，并与 `cred`、`platform/timestamp/vName` 写入请求头。

4. **数据持久化 (`src/services/data_manager.py`)**
   - SQLite 文件 `data/rogue_data.db`，表结构：
     ```sql
     CREATE TABLE IF NOT EXISTS rogue_runs (
       id TEXT PRIMARY KEY,
       uid TEXT NOT NULL,
       theme TEXT NOT NULL,
       start_ts INTEGER,
       record_data TEXT
     );
     ```
   - 每次 API 返回的 `records` 列表会 `INSERT OR REPLACE` 到本地，保证同一 `id` 的对局只保留最新版本；
   - 查询时按 `start_ts DESC` 返回某 UID+主题的全部历史记录，供分析使用。

5. **业务分析 (`src/services/rogue_service.py`)**
   - 依据 `config/rogue_theme_config.json` 的 `keys` 映射读写 JSON 字段，`analysis_rules`、`ending_rules`、`stats_definitions` 描述各赛季差异；
   - `_determine_ending()` 根据 `gainRelicList` 是否包含特定 ID 来判定普通结局、滚动局和“五结局”伴生角色；
   - `_analyze_records()` 计算：
     - 有效对局（`score` 大于 `min_score_for_valid`）；
     - 近 7 天内的对局（以 `startTs` 为准）；
     - 胜率/连胜：同一布尔序列上取最大连续成功次数；
     - “第五结局”达成率：成功且拾取 `rogue_4_relic_final_11` 的场次；
     - 近期对局明细：难度、分队、得分、耗时、滚动标记、指定遗愿（`primary_totem_id`）数量等；
   - 分队名通过 `config/aliases.json` 翻译成短别名（如“矛头分队”→“矛头”）。

6. **界面更新 (`src/ui/*.py`)**
   - `UIController` 异步拉数据，主线程刷新 Tk 组件；
   - 样式配置位于 `config/ui_theme.json`，此处略。

## 2. JSON 字段语义（`GET /game/arknights/rogue?uid=` 响应）

整体结构（参见 `docs/api/rogue_api_structure.md`）：

```json
{
  "code": 0,
  "message": "OK",
  "timestamp": "1750956605",
  "data": {
    "topics": [...],
    "showConfig": {...},
    "history": {...},
    "career": {...},
    "gameUserInfo": {...},
    "itemInfo": {...}
  }
}
```

### 2.1 topics（赛季/主题列表）
- `id`: 主题 ID（如 `rogue_4`）
- `name`: 主题中文名（如“萨卡兹的无终奇语”）
- `isSelected`: 是否为当前数据展示的主题
- `pic` / `titlePic`: 封面与标题图 URL
- `dynamic`: 包含主题宣传视频、轮播等动态素材

### 2.2 showConfig（展示开关）
- `charSwitch`: 是否允许展示干员
- `skinSwitch`: 是否展示皮肤
- `standingsSwitch`: 是否公开排行榜

### 2.3 history（玩家历史数据）
- `medal`: `{count, current}` —— 勋章总数与当前拥有
- `modeGrade`: 当前最高难度（整数，18 对应“直面魂灵”）
- `mode`: 难度名称
- `score`: 历史累积分
- `bpLevel`: 协议等级
- `chars`: 常用干员列表简表（稀有度、职业等）
- `tagList`: 历史标签（含图标、描述、ID）
- `records`: **核心对局数组**（见 §2.5）
- `favourRecords`: 收藏记录，当前多为空
- `zone`: 主题区域（通常空字符串）

### 2.4 career（生涯统计）
- `clearInfo`: 最近通关信息，如难度 `grade` 和结局列表 `endings`
- `invest`: 累计投入的资源
- `node`: 累计探索节点
- `step`: 累计步数
- `gold` / `hope`: 当前持有源石锭/希望值

### 2.5 records（单局对战记录）
核心字段与含义：

| 字段 | 说明 |
|------|------|
| `id` | 对局唯一 ID，用于数据库主键 |
| `startTs` / `endTs` | Unix 秒级时间戳 |
| `modeGrade` / `mode` | 难度等级及名称 |
| `band`: `{id,name}` | 使用分队（如 `rogue_4_band_7` / “矛头分队”） |
| `score` | 本局评分 |
| `success` | 1=通关，0=失败 |
| `lastStage` | 最后停留的关卡名 |
| `initChars` / `lastChars` / `troopChars` | 干员阵容列表，每项包含干员 ID、职业、精英与等级信息 |
| `gainRelicList` | 拾取的收藏品 ID 列表（判定结局与滚动局的依据） |
| `cntCrossedZone` / `cntArrivedNode` 等 | 节点、战斗等计数（普通/精英/BOSS、招募、升级） |
| `totemList` | 遗愿/碎片数组，每条形如 `{id: "rogue_4_fragment_D_01", count: 1}` |
| `tagList` | 该局获得的策略标签 |
| `endingText` | 富文本形式的结局描述 |
| `isCollect` | 是否被玩家收藏 |

> **结局判定**：`rogue_theme_config.json` 中指定 `ending_rules`。以无终奇语为例：
> - `is_rolling_relic = rogue_4_relic_explore_7`：若拾取该纪念品即视为滚动先祖；
> - `endings`: `[2,3,4,5]` 对应特定纪念品 ID，若胜利且拥有即认为达成该结局；否则默认结局为 `1`；
> - `ending_5_companions`: 第五结局伴随角色，通过额外 relic 判断；
> - `text_templates`: 拼装展示文本。

### 2.6 gameUserInfo（玩家卡片）
- `name`, `level`, `avatar`, `isOfficial` 等基础属性。UI 头部直接引用该区。

### 2.7 itemInfo（物品元数据）
- 本体是一个字典：`{ itemId: { name, description, usage, ... } }`；
- `gainRelicList`、`totemList` 等引用的 ID 在这里查中文名与描述。

## 3. 主题配置 (`config/rogue_theme_config.json`)

| 键 | 作用 |
|----|------|
| `keys` | 把 API 字段映射到通用名，例如 `success_status=success`、`relic_list=gainRelicList`，方便后续赛季只需改配置即可复用分析逻辑。 |
| `analysis_rules.min_score_for_valid` | 过滤掉分数过低或手动投降的记录，避免干扰胜率统计。 |
| `analysis_rules.primary_totem_id` | 统计特定遗愿（如“构想”）数量时使用的 ID。 |
| `ending_rules.is_rolling_relic` | 判断是否滚动局的收藏品。 |
| `ending_rules.endings` | 胜利后根据拾取到的关键 relic 判定 2/3/4/5 结局。 |
| `ending_rules.default_win_ending` | 未命中任何特殊结局时的编号。 |
| `ending_rules.text_templates` | 失败/滚动/成功的文字模板，带参数。 |
| `stats_definitions.fifth_ending` | 定义“五结局”统计条件，此处为 “成功且拾取结局 5 的 relic”。 |

> 因此若要支持新的集成战略主题，只需新增一段配置：填好主题 ID、字段映射、特殊结局 relic、统计规则等即可。

## 4. 研究结论与迁移建议

1. **认证链路需完整复制**：Spring 端必须复刻 `grant -> generate_cred -> binding -> rogue info` 这一串调用与签名逻辑，否则无法得到 `cred/token/uid`。
2. **持久化策略**：可直接映射成 MyBatis-Plus 实体 `RogueRunEntity`，主键 `id`，其余字段存 JSON 或拆成列；当前 Python 实现将整个记录 JSON 序列化入库，查询时再 `json.loads`。
3. **结局判断依赖配置**：不要在代码里硬编码任何 relic ID，把每赛季逻辑都写在 `rogue_theme_config.json` 的 `keys/ending_rules/analysis_rules` 中，Service 只做通用遍历。
4. **统计口径**：胜率、连胜、五结局的判定流程清晰，照搬 `_calculate_max_streak` 与 `_determine_ending` 即可；
5. **重要 JSON 字段** 已在 §2 列出，尤其 `records`、`career`、`gameUserInfo`、`itemInfo` 提供了 UI 需要的所有基础数据。

据此，可在 Java 后端中实现：
- `SklandClient`（RestTemplate/WebClient + HMAC-MD5 签名）；
- `RogueRunRepository`（MySQL/H2）；
- `RogueAnalysisService`（对应 `_analyze_records`）；
- `ThemeConfig`（加载 JSON，提供字段映射）。

这份研究文档可作为后续接口设计与实体建模的依据。进一步扩展时，可继续解析 `docs/pic/*.png` 中的 UI 参考或者 `src/ui` 代码，以便实现 Web 版可视化。*** End Patch
