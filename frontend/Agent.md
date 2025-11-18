# Frontend Agent 指南

## 模块概览

- **技术栈**：Node.js 20+、React 18、TypeScript、Vite 7。
- **入口**：`src/main.tsx` 渲染 `App.tsx`，其中组合了 Login 与 Dashboard 占位页面。
- **HTTP 层**：`src/api/http.ts` 统一封装 `fetch`，后端地址来自 `VITE_API_BASE_URL`（默认 `http://localhost:8080`）。
- **环境变量**：`.env.development` 已提供示例，可按需新增 `.env.production` 等文件。

## 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 启动 Vite 开发服务器
npm run build        # TypeScript 检查 + 生产构建
```

## 目录提示

- `src/pages/Login`：认证界面占位，未来可在此接入真实登录流程。
- `src/pages/Dashboard`：调用 `/api/health` 并展示后端连通性，可继续扩展为数据看板。
- `src/api`：放置所有 API 帮助函数，按照资源拆分文件（如 `reports.ts`、`bindings.ts`）。

## 扩展建议

1. 与后端保持 DTO 对齐，必要时在 `src/types` 中定义共享类型。
2. 若需要全局状态或请求缓存，可选用 React Query、Zustand 等，并在本文件记录决定。
3. 新增业务逻辑时最好配套 Vitest/React Testing Library 测试。
4. 新环境变量必须以 `VITE_` 开头，同时在文档中说明默认值与用途。

## 交付检查

- 至少执行 `npm run build`，确保类型与打包无误。
- 如新增命令、配置或约定，及时更新本 `Agent.md`，方便后续代理快速上手。
