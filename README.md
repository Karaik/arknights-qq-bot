一个针对 `明日方舟` 的 QQ 机器人项目，目前尚在开发中，整体计划为
 - 1 以 [这个项目](https://github.com/Choimoe/Rhodes-Rogue-Analyst) 为基础，解析肉鸽数据，并将解析结果与获取的结果存入 DB
 - 2 以自定义 QQ 指令让机器人自动调取，并输出格式化数据，或直接生成图像
 - 3 能登录后台查看数据情况

## 文档索引

- 根目录 `Agent.md`：总体开发协作规范。
- `backend/Agent.md`：后端模块具体约定。
- Roguelike 阶段文档：
  - `backend/docs/roguelike/architecture-stage0.md`
  - `backend/docs/roguelike/platform-stage1.md`
  - `backend/docs/roguelike/persistence-stage2.md`
  - `backend/docs/roguelike/theme-stage3.md`
  - `backend/docs/roguelike/service-stage4.md`
- 数据库脚本与说明：`database/ddl/`、`database/docs/README.md`。
