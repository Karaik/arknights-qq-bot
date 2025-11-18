# 集成战略模块阶段 2 – 持久化设计

## 1. 数据表

表 `roguelike_run`（开发态由 `database/ddl/V1__init_roguelike_schema.sql` 自动初始化）：

| 字段        | 类型         | 描述                         |
|-------------|--------------|------------------------------|
| `id`        | VARCHAR(64)  | 对局 ID，主键               |
| `uid`       | VARCHAR(32)  | 玩家 UID                     |
| `theme_id`  | VARCHAR(64)  | 集成战略主题 ID             |
| `start_ts`  | BIGINT       | 对局开始时间（秒）          |
| `record_json` | CLOB       | 原始 JSON 记录              |
| `created_at`/`updated_at` | TIMESTAMP | 由 MyBatis-Plus 自动填充 |

索引：`idx_roguelike_run_uid_theme (uid, theme_id)`。

## 2. MyBatis-Plus 实体与仓储

- `RoguelikeRunEntity`：映射上述字段，并通过 `RoguelikeMetaObjectHandler` 自动填充时间戳。
- `RoguelikeRunRepository`：
  - `saveRuns(uid, themeId, runs)`：遍历 API 返回的记录，若存在相同 `id` 则 `updateById`，否则 `insert`。
  - `listRuns(uid, themeId)`：按 `start_ts DESC` 返回所有历史记录并反序列化为 `Map<String, Object>`。

## 3. 迁移脚本

`backend/scripts/sqlite_to_mysql.py` 用于将旧版 `rogue_data.db` 导入 MySQL/MariaDB，结构脚本使用 `database/ddl/V1__init_roguelike_schema.sql`：

```bash
pip install mysql-connector-python
python backend/scripts/sqlite_to_mysql.py /path/to/rogue_data.db
```

通过环境变量 `MYSQL_HOST`、`MYSQL_DB` 等注入数据库信息。

## 4. 测试

`RoguelikeRunRepositoryTest` 基于 H2/Mock 数据验证：

1. `saveRuns` 可写入记录；
2. `listRuns` 能按主题/UID 返回 JSON 数据。

该阶段完成后，后续服务层可直接依赖 `RoguelikeRunRepository` 读取历史数据。
