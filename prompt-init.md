# arknights-qq-bot – 项目结构与技术选型说明

> 目标：一个仓库内同时包含 Web 后端、Web 前端、QQ 机器人三部分，统一复用同一套业务与数据库，用尽可能“工业级但不折腾”的方式搭建骨架，后续尽量交给 AI/代码代理自动生成。

---

## 1. 总体概览

- 仓库名：`arknights-qq-bot`
- Java 包名统一：`com.karaik.gamebot`
- 主要模块：
  - `backend/`：Java 17 + Spring Boot 3 + MyBatis-Plus 的 Web 后端（REST API）
  - `frontend/`：React + TypeScript + Vite 的 Web 前端（管理后台 / 仪表盘）
  - `bot/`：基于 Mirai 的 QQ 机器人（Java/Kotlin），负责：
    - 定时从后端拉取报表，发到 QQ 群
    - 监听 QQ 群命令，调用后端接口，返回查询结果

---

## 2. 开发环境要求

建议统一如下环境，方便 CI / 代理操作：

- JDK：**Java 17**
- 构建工具：
  - 后端：**Maven 3.9+**
  - 机器人：也使用 Maven，保证栈统一
- Node.js：**Node 20+ LTS**（使用当前 LTS 即可）
- 包管理器：`npm` 或 `pnpm`（任选其一，README 中以 `npm` 为例）
- 数据库：MySQL 8.x（或 MariaDB 10.6+）
- 版本控制：Git

---

## 3. 仓库目录结构（目标状态）

```text
arknights-qq-bot/
  backend/                 # Spring Boot 后端（Maven）
    pom.xml
    src/
      main/
        java/com/karaik/gamebot/...
        resources/
          application.yml
  frontend/                # 前端 (Vite + React + TS)
    package.json
    vite.config.ts
    src/...
  bot/                     # QQ 机器人 (Mirai + Maven)
    pom.xml
    src/
      main/
        java/com/karaik/gamebot/bot/...
        resources/
          bot-config.yml
  docs/
    ARCHITECTURE.md        # （可选）后续逐步补充的设计文档
  .gitignore
  README.md                # 对外总说明（可由本文件演化）


> 说明：后端与机器人各自一个 Maven 工程，前端单独一个 Node 工程，共享同一个 Git 仓库。

------

## 4. 后端模块：`backend/`

### 4.1 职责

- 对外暴露 REST API：
  - 把游戏官方接口数据做一层封装（持久化、清洗、聚合）
  - 对前端提供查询接口（战报列表、明细、统计）
  - 对 QQ 机器人提供专用接口（例如：某群绑定的 UID 战报摘要）
- 内部：
  - 使用 MyBatis-Plus 做 ORM 与 CRUD 简化
  - 封装游戏 API 调用逻辑
  - 后续可以加定时任务，周期性同步数据

### 4.2 技术栈与依赖（稳定版本）

- **语言**：Java 17
- **框架**：
  - Spring Boot **3.5.7**
  - Spring Web / Validation / Actuator
- **ORM / 数据访问**：
  - MyBatis-Plus Spring Boot 3 Starter，版本 **3.5.14**
- **数据库驱动**：
  - MySQL Connector/J（使用 Spring Boot 依赖管理自动选版本）
- **接口文档**：
  - springdoc-openapi-starter-webmvc-ui **2.8.14**
- **辅助**：
  - Lombok
  - MapStruct（可选，用于 DTO ↔ Entity 映射）
- **测试**：
  - spring-boot-starter-test
  - mybatis-plus-boot-starter-test **3.5.14**

### 4.3 后端目录结构

```text
backend/
  pom.xml
  src/
    main/
      java/com/karaik/gamebot/
        ArknightsBotApplication.java        # Spring Boot 启动类
        config/                             # MyBatis-Plus、Swagger/OpenAPI 等配置
        common/                             # 通用响应封装、异常处理
        domain/
          entity/                           # 实体类（与数据表对应）
          dto/                              # DTO / VO
        mapper/                             # MyBatis-Plus Mapper 接口
        service/
          impl/                             # Service 实现
        controller/
          api/                              # 对前端的 REST API
          internal/                         # 提供给 QQ 机器人或内部用的接口
      resources/
        application.yml
        mapper/                             # XML（如需要自定义 SQL）
    test/
      java/com/karaik/gamebot/
        ...                                 # 单元测试 / 集成测试
```

### 4.4 `backend/pom.xml` 示例（骨架）

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.7</version>
        <relativePath/>
    </parent>

    <groupId>com.karaik.gamebot</groupId>
    <artifactId>arknights-qq-bot-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>arknights-qq-bot-backend</name>
    <description>Arknights battle report backend service</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.14</mybatis-plus.version>
        <springdoc.version>2.8.14</springdoc.version>
        <mapstruct.version>1.6.2</mapstruct.version>
        <lombok.version>1.18.36</lombok.version>
    </properties>

    <dependencies>
        <!-- Web & Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus for Spring Boot 3 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Database driver (version 由 Spring Boot 管理) -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <optional>true</optional>
        </dependency>

        <!-- MapStruct (可选) -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>${mapstruct.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- OpenAPI / Swagger UI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Actuator（可选，用于监控） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter-test</artifactId>
            <version>${mybatis-plus.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot 打包插件 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- MapStruct + Lombok 注解处理（可选） -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 4.5 启动类骨架

```java
package com.karaik.gamebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArknightsBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArknightsBotApplication.class, args);
    }
}
```

### 4.6 基础配置 `application.yml`（示例）

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/arknights_bot?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
    username: your_user
    password: your_password
  jackson:
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
```

> 启动后，接口文档访问路径：`http://localhost:8080/swagger-ui.html`。

------

## 5. 前端模块：`frontend/`（React + TS + Vite）

### 5.1 职责

- 提供浏览器侧界面：
  - 用户登录（后续对接后端认证）
  - 查看自己的关卡战报、统计图表
  - 管理 QQ 群与游戏账号的绑定关系
  - 查看机器人发送的各类报表历史

### 5.2 技术栈

- Node 20+ LTS
- React 18
- TypeScript
- Vite（构建工具）
- UI 框架可选：
  - Ant Design / MUI / Tailwind CSS 等，初期可先不指定，由代理自动选择一个主流方案

### 5.3 初始化指令（给代理/自己执行）

在仓库根目录：

```bash
# 1. 使用 Vite 初始化 React + TS 项目
npm create vite@latest frontend -- --template react-ts

cd frontend

# 2. 安装依赖
npm install

# 3.（可选）安装 UI 组件库，例如 Ant Design
npm install antd

# 4. 开发启动
npm run dev
```

### 5.4 建议的前端目录结构

```text
frontend/
  src/
    api/                # 封装调用 backend 的 HTTP 方法
    components/         # 通用组件
    pages/
      Login/
      Dashboard/
      Reports/
      Admin/
    router/             # 路由定义
    store/              # 全局状态（如使用 Zustand / Redux）
    types/              # TS 类型声明
    utils/
    main.tsx
    App.tsx
  public/
  index.html
```

### 5.5 与后端交互约定（关键点）

- 所有接口统一以 `/api` 开头，例如：

  - `GET /api/reports/latest`
  - `GET /api/reports/{uid}`
  - `POST /api/bindings`

- 前端通过环境变量配置后端地址：

  - `.env.development`

    ```env
    VITE_API_BASE_URL=http://localhost:8080
    ```

  - 请求封装时，统一从 `import.meta.env.VITE_API_BASE_URL` 读取。

------

## 6. QQ 机器人模块：`bot/`（Mirai + Maven）

### 6.1 职责

- 登录 QQ 账号（使用 Mirai）
- 对接 QQ 群消息：
  - 解析符合约定格式的指令，例如：`/ark report 12345678`
  - 调用后端 REST API，拿到数据后格式化发回 QQ 群
- 定时任务：
  - 每天固定时间拉取最近战报，生成摘要推送到指定群

### 6.2 技术栈设计

- JVM 语言：**Kotlin + Java 17**
  - 业务逻辑可以使用 Kotlin，方便使用 Mirai 生态
- 依赖：
  - Mirai Core（具体版本建议由代理运行时从 Maven Central 查询最新稳定版并填入，下文用占位符表示）
  - OkHttp / WebClient（用于调用后端接口）
  - Jackson / kotlinx-serialization（用于 JSON 序列化）

### 6.3 机器人 Maven 工程骨架 `bot/pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.karaik.gamebot</groupId>
    <artifactId>arknights-qq-bot-qq</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>arknights-qq-bot-qq</name>
    <description>Arknights QQ bot based on Mirai</description>

    <properties>
        <kotlin.version>2.0.0</kotlin.version>
        <java.version>17</java.version>
        <mirai.version>${mirai.version}</mirai.version> <!-- TODO: 由代理填入当前稳定版 -->
    </properties>

    <dependencies>
        <!-- Mirai Core -->
        <dependency>
            <groupId>net.mamoe</groupId>
            <artifactId>mirai-core-jvm</artifactId>
            <version>${mirai.version}</version>
        </dependency>

        <!-- HTTP 客户端，用于调用 backend -->
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.12.0</version>
        </dependency>

        <!-- JSON（可替换为你偏好的库） -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Kotlin 标准库 -->
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Kotlin 编译插件 -->
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <jvmTarget>${java.version}</jvmTarget>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 6.4 机器人与后端交互约定

- 机器人只通过 HTTP/HTTPS 调用后端，不直接访问数据库。
- 后端为机器人提供一组“内部 API”（可带有简单签名/Token 校验）：
  - `GET /api/bot/reports/daily?groupId=xxx`
  - `POST /api/bot/command/parse`
- 机器人模块内需要实现：
  - 配置加载（QQ 号、密码/扫码、后端 URL、群 ID 映射）
  - 消息监听与命令解析
  - 调用后端，拿到 JSON → 文本拼装 → 返回 QQ 群

------

## 7. 跨模块约定（重要）

1. **统一实体语义**

   - 后端 `domain.entity` 中的核心实体（如 Player、Report、StageRecord 等）对应数据库表。
   - 对外返回 DTO（在 `domain.dto` 中定义），前端及机器人只依赖 DTO 字段。

2. **统一 API 规范**

   - HTTP 返回统一结构，例如：

     ```json
     {
       "code": 0,
       "message": "ok",
       "data": { ... }
     }
     ```

   - 错误码与错误信息集中定义。

3. **鉴权策略**

   - 初期可以将安全简化为：
     - Web 前端通过简单的 Token / API Key
     - 机器人通过固定密钥（在 header 中传入）
   - 后期再升级为 OAuth2 / JWT。

------

## 8. 供 Agent 使用的执行步骤 Checklist

> 下方是可以直接给代码代理的一份任务清单，从空目录开始搭建项目骨架。

1. **初始化 Git 仓库**
   - 创建目录 `arknights-qq-bot/`
   - 初始化 Git：`git init`
   - 创建 `.gitignore`：
     - 至少忽略：`target/`, `node_modules/`, `.idea/`, `.vscode/`, `dist/`
2. **创建后端模块 `backend/`**
   - 在 `arknights-qq-bot/backend` 下创建 Maven 项目：
     - `groupId`：`com.karaik.gamebot`
     - `artifactId`：`arknights-qq-bot-backend`
     - `java.version`：17
   - 使用本文件中的 `backend/pom.xml` 覆盖生成的 POM。
   - 创建启动类 `com.karaik.gamebot.ArknightsBotApplication`.
   - 创建 `application.yml`，内容根据示例填入。
   - 启动应用：`mvn spring-boot:run`，确认可以访问 `/swagger-ui.html`。
   - 新增一个测试接口：
     - `GET /api/health` 返回 `"ok"`。
3. **创建前端模块 `frontend/`**
   - 在仓库根目录执行：
     - `npm create vite@latest frontend -- --template react-ts`
   - 在 `frontend/src/pages` 下创建基本页面：
     - `Login`, `Dashboard`（内容暂时简单占位）
   - 新建 `src/api/http.ts`，封装 HTTP 客户端，从 `VITE_API_BASE_URL` 读取后端地址。
   - 在 Dashboard 页面调用 `/api/health` 接口，并展示返回结果（例如显示“Backend: ok”）。
   - 执行 `npm run dev` 验证前端能正常启动。
4. **创建机器人模块 `bot/`**
   - 在 `arknights-qq-bot/bot` 下创建 Maven 项目：
     - `groupId`：`com.karaik.gamebot`
     - `artifactId`：`arknights-qq-bot-qq`
   - 使用本文件中的 `bot/pom.xml` 覆盖默认 POM。
   - 由代理查询当前 Mirai 最新稳定版，替换 `${mirai.version}`。
   - 新建入口类（Kotlin）：
     - 包名：`com.karaik.gamebot.bot`
     - 类名：`AknBotLauncher`
     - 实现最简单的登录逻辑（可以先留空登录配置，只打印“bot started”）：
       - 初始化 Mirai 环境
       - 打印启动日志
   - 在机器人中创建一个简单的定时任务：
     - 每隔固定时间调用后端 `/api/health`
     - 在控制台打印返回结果，验证机器人→后端网络路径正常。
5. **联调准备**
   - 确保本地或服务器上：
     - MySQL 已启动并创建数据库 `arknights_bot`
     - 后端启动无错误，`/api/health` 返回正常
     - 前端使用 `VITE_API_BASE_URL` 正确指向后端地址
     - 机器人可以成功访问后端 `/api/health`

------

## 9. 后续可迭代方向（留给将来）

- 为后端加上：
  - 定时任务（`@Scheduled`）定期拉取游戏官方数据并入库
  - 分环境配置（`application-dev.yml` / `application-prod.yml`）
  - 更完善的异常处理与统一错误码体系
- 为前端增加：
  - 登录/权限模块（简单 JWT / Session）
  - 图表展示（ECharts / Recharts）用于战力变化、关卡通过率统计
  - 队伍配置、作战详情等复杂视图
- 为机器人增加：
  - 动态配置热加载（修改配置后无需重启）
  - 更丰富的指令体系（如：帮助、绑定/解绑账号、最近 N 场战报查询）
  - 同一机器人服务多个群的多租户逻辑（每群不同绑定、不同推送策略）

