
# RentWise

基于 Spring Boot 与 Vue 3 的租赁合同风险识别训练系统。

## 项目简介

RentWise 面向初次独立租房的用户，通过真实风格的租赁合同条款案例，训练用户识别签约过程中值得进一步澄清的风险信息。

系统提供初始诊断、学习画像、自适应训练、即时反馈和阶段测评，形成持续学习闭环。

> 本项目是租赁风险识别教育工具，不提供法律意见，也不自动判断合同条款是否合法。

## 核心功能

1. 用户注册与登录：JWT 身份认证和 BCrypt 密码加密存储。
2. 初始诊断：完成 10 道租赁合同条款识别题。
3. 学习画像：展示五个知识主题的掌握度。
4. 个性化推荐：优先推荐薄弱主题的训练案例。
5. 自适应训练：提交答案后获得反馈，动态更新掌握度。
6. 阶段测评：完成 5 道主题测评题，更新学习画像与训练计划。

五个知识主题：

- 押金返还（DEPOSIT_RETURN）
- 提前退租（EARLY_TERMINATION）
- 维修责任（REPAIR_RESPONSIBILITY）
- 费用承担（COST_BEARING）
- 违约责任（BREACH_LIABILITY）

## 技术栈

### 后端

- Java 17、Spring Boot 3.4.5
- Spring Security、JWT、BCrypt
- Spring Data JPA、MySQL 8
- Maven、Swagger / OpenAPI
- JUnit 5、MockMvc、H2（测试环境）

### 前端

- Vue 3、TypeScript、Vite
- Pinia、Vue Router
- Axios、Element Plus

## 系统架构

当前采用模块化单体架构（Modular Monolith）。

主要业务模块：

| 模块 | 职责 |
| --- | --- |
| User | 用户身份 |
| Training | 案例、诊断、训练、测评及答题记录 |
| Profile | 掌握度计算与学习画像 |
| Plan | 个性化训练推荐 |

Auth / Security 负责身份认证和权限控制。

核心设计原则：

User = Identity  
Training = Facts  
Profile = State  
Plan = Decision

目前仍为单个 Spring Boot 应用，尚未拆分为独立微服务。

## 本地运行

### 1. 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8
- Node.js 与 npm

### 2. 初始化数据库

默认数据库名称：`rentwise`

全新环境使用：

`sql/init.sql`

可以通过 MySQL Shell 的 SQL 模式执行初始化脚本。

已有数据库不要重复执行旧版迁移脚本，以免破坏现有数据。

旧版迁移脚本位置：

`sql/migrations/v0.1-to-v0.2.sql`

### 3. 配置环境变量

后端使用以下环境变量：

- DB_USERNAME：MySQL 用户名
- DB_PASSWORD：MySQL 密码
- JWT_SECRET：JWT 签名密钥，至少 32 字节

可选：

- DB_URL：数据库连接地址
- SERVER_PORT：服务端口，默认 8080
- JWT_EXPIRATION_SECONDS：Token 有效期，默认 3600 秒

Windows PowerShell 示例：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = Read-Host "MySQL root 密码"

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

不要将实际密码或 JWT 密钥提交至 GitHub。

### 4. 启动后端

在项目根目录执行：

```powershell
mvn spring-boot:run
```

后端默认地址：

http://localhost:8080

Swagger UI：

http://localhost:8080/swagger-ui.html

### 5. 启动前端

另开一个 PowerShell 终端：

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

前端默认地址：

http://localhost:5173

Vite 将 `/api` 请求代理到后端的 8080 端口。

## 主要 API

| 功能 | 接口 |
| --- | --- |
| 注册 | POST /api/auth/register |
| 登录 | POST /api/auth/login |
| 当前用户 | GET /api/users/me |
| 开始诊断 | POST /api/diagnosis/start |
| 提交诊断答案 | POST /api/diagnosis/{sessionId}/answers |
| 完成诊断 | POST /api/diagnosis/{sessionId}/finish |
| 学习画像 | GET /api/profile/me |
| 获取训练推荐 | GET /api/training/next |
| 提交训练答案 | POST /api/training/answers |
| 开始阶段测评 | POST /api/assessment/start |
| 完成阶段测评 | POST /api/assessment/{sessionId}/finish |

除公开认证接口外，业务接口需要 Bearer JWT。

## 测试

后端测试：

```powershell
mvn clean test
```

前端生产构建：

```powershell
cd frontend
npm.cmd run build
```

业务验收范围：

- 注册、登录与退出
- 初始诊断
- 五主题学习画像
- 个性化训练与反馈
- 阶段测评
- JWT 失效与未授权访问
- 重复提交与网络异常

## 已知限制

- 题库规模较小，部分训练案例可能重复。
- 尚未实现训练历史趋势图。
- 不提供 Refresh Token。
- 阶段测评刷新页面后，未提交的答案不会保留。
- 当前不是分布式微服务系统。
- 不提供法律意见或自动合同合法性认定。

## 后续演进

根据课程要求和真实业务需求，逐步评估：

- Nacos：服务注册发现和配置管理
- Gateway：统一 API 入口
- Sentinel：限流和服务降级
- Seata：跨服务事务协调
- SkyWalking：分布式链路追踪

以上组件属于后续规划，不属于当前 v0.3 的已实现功能。

## 项目文档

- docs/architecture.md
- docs/api.md
- CHANGELOG.md

## 版本说明

- v0.1：基础业务闭环
- v0.2：JWT 身份认证与权限控制
- v0.3：Vue 前端与完整学习交互闭环

- `docs/mvp.md`
- `docs/architecture.md`
- `docs/api.md`
- `docs/versions/v0.1/...`
- `docs/versions/v0.2/...`
