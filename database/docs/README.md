# Database Artifacts

企业级约定：所有数据库脚本与说明集中放在仓库根目录 `database/` 下，按功能分类，便于 CI/CD 与人工审核。

## 目录结构

```
database/
  ddl/
    V1__init_roguelike_schema.sql   # 建表与基础索引
  docs/
    README.md                       # 本文件，可扩展每个版本的说明
```

命名规范参考 Flyway/Major version 习惯：`V<序号>__<描述>.sql`，新增脚本时递增版本号，并保持描述清晰（英文或拼音）。

## 使用方式

- **开发/测试**：`backend` 模块在 `dev` Profile 下会通过 `spring.sql.init.schema-locations=file:../database/ddl/V1__init_roguelike_schema.sql` 自动执行脚本，无需额外动作。
- **生产/手动操作**：DBA 可在数据库中手动执行对应 SQL，或使用企业内部的数据库迁移系统（Flyway/Liquibase），脚本位置即为官方来源。
- **数据迁移**：如果需要从旧 SQLite 导入历史记录，可配合 `backend/scripts/sqlite_to_mysql.py`（需手动导入），但所有结构性变更仍以 `database/ddl/*.sql` 为准。

新增表或索引时，请：
1. 在 `database/ddl` 添加新的 `V<n>__description.sql`；
2. 更新 `database/docs/README.md` 记录变更摘要；
3. 在相关阶段文档/Agent 指出依赖关系。
