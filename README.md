[文档索引](docs/README.md) · [治理报告](docs/governance-report.md)

# RentWise

> 租房合同风险识别与自适应训练平台｜初代源码交付版本（v0.4 微服务改造阶段）

## 1. 项目简介

RentWise 面向初次独立租房的用户，通过真实风格的租房合同条款案例，帮助用户练习识别签约过程中的潜在风险和需要进一步澄清的信息。

系统形成 **注册/登录 → 十题初始诊断 → 五类能力画像 → 薄弱项训练推荐 → 答题反馈与阶段测评** 的学习闭环。

**使用边界：** 本系统用于租房风险识别教育，不提供法律意见，也不自动判断合同条款是否合法。

## 2. 当前已实现的主要功能

- 用户注册、登录；基于 JWT 的身份认证、BCrypt 密码哈希。
- 基于 MySQL 案例数据的十题初始诊断，保存诊断会话和答题记录。
- 五类风险主题的掌握度（Mastery）画像展示。
- 根据薄弱主题进行训练推荐，记录训练答题并提供反馈。
- 阶段测评与学习结果展示。
- Vue 3 前端界面，以及 Gateway、Learning、Profile 的阶段性服务拆分。

五类知识主题分别为：押金返还（`DEPOSIT_RETURN`）、提前退租（`EARLY_TERMINATION`）、维修责任（`REPAIR_RESPONSIBILITY`）、费用承担（`COST_BEARING`）、违约责任（`BREACH_LIABILITY`）。

## 3. 当前实际架构（请与最终目标架构区分）

```text
Vue 3 前端（默认 5173）
       |
       | /api/**
       v
Gateway Service（8080）
       |
       | lb://training-service
       | Nacos 服务发现（启用 NACOS_ENABLED=true 时）
       v
Learning Service（8082）
  Maven 模块：learning-service
  服务注册名：training-service
  现阶段承载：User / Auth / Diagnosis / Training / Plan 等业务
       |                             \
       | MySQL                        | HTTP（当前默认固定地址）
       v                              v
rentwise_training_v04       Profile Service（127.0.0.1:8083）
                                     |
                                     v
                            rentwise_profile_v04

Nacos 注册中心：127.0.0.1:8848
```

说明：

1. 当前源码模块包括 `gateway-service`、`learning-service`、`profile-service`。**并非**四个最终业务微服务都已独立部署。
2. Gateway 在启用 Nacos Discovery 后，使用 `lb://training-service` 发现 Learning 实例。
3. Learning 调用 Profile 的当前默认配置是 `http://127.0.0.1:8083`，由 `PROFILE_SERVICE_URL` 控制；**目前不应宣称该内部调用已经改为按 Nacos 服务名发现**。
4. Profile 当前配置绑定在 `127.0.0.1:8083`；它的配置文件未显式声明 Nacos Discovery，因此不能仅根据配置断言它已通过 Nacos 注册。
5. 旧版单体数据库 `rentwise` 可能仍保留用于历史版本验证；本版本主要使用两个 v0.4 业务数据库。

## 4. 技术栈与开发环境

| 类型 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.5.x（最近测试日志显示 3.5.16）、Spring Web、Spring Security、Spring Data JPA |
| 认证 | JWT、BCrypt |
| 微服务阶段性组件 | Spring Cloud Gateway、Nacos Discovery、Spring Cloud LoadBalancer（按模块实际依赖） |
| 数据库 | MySQL 8 |
| API 文档 | Springdoc OpenAPI / Swagger UI |
| 前端 | Vue 3、TypeScript、Vite、Pinia、Vue Router、Axios、Element Plus |
| 测试 | JUnit 5、MockMvc、H2（测试环境） |
| 构建 | Maven、npm |

具体依赖版本以根目录和各模块 `pom.xml` 及 `frontend/package.json` 为准。

## 5. 目录结构

```text
RentWise/
├── frontend/                  # Vue 前端
├── gateway-service/           # Gateway 统一入口
├── learning-service/          # 训练、诊断、认证等现阶段核心业务
├── profile-service/           # 独立画像服务
├── sql/                       # SQL 与迁移脚本（按版本审查后使用）
├── docs/                      # API、架构、产品及历史版本文档
├── pom.xml                    # Maven 父工程
├── README.md
└── CHANGELOG.md
```

课程七项前置设计材料如单独提交，应以其最新版本为准；当前仓库不要求必须存在 `docs/course-design/` 目录。

## 6. 本地运行

### 6.1 准备条件

- JDK 17、Maven、MySQL 8、Node.js 与 npm。
- 已安装并启动 Nacos，服务地址默认 `127.0.0.1:8848`。
- 准备好与当前源码兼容的数据库结构和案例数据：
  - `rentwise_training_v04`
  - `rentwise_profile_v04`
- 在各服务的 **独立终端** 中配置所需环境变量。**不要把真实密码或密钥写入 README、Git 历史或截图。**

> 数据库注意：当前 `application.yml` 设置了 `spring.jpa.hibernate.ddl-auto=update`，而 `spring.sql.init.mode=never`。前者不等于完整的版本化数据库迁移或演示数据初始化；不要假定一个空数据库在启动后就会自动包含全部可演示的案例。

### 6.2 启动 Profile Service

在项目根目录打开 PowerShell 终端 A，先设置环境变量：

```powershell
$env:PROFILE_DB_PASSWORD = Read-Host "PROFILE_DB_PASSWORD"
$env:INTERNAL_API_KEY = Read-Host "INTERNAL_API_KEY"

mvn -pl profile-service spring-boot:run
```

默认数据库账号为 `root`；如使用专用账号，可另外设置 `PROFILE_DB_USERNAME`。内部通信密钥必须与 Learning Service 配置的值相同。

### 6.3 启动 Learning Service

在项目根目录打开另一个 PowerShell 终端 B：

```powershell
$env:DB_PASSWORD = Read-Host "DB_PASSWORD"
$env:JWT_SECRET = Read-Host "JWT_SECRET"
$env:INTERNAL_API_KEY = Read-Host "INTERNAL_API_KEY"
$env:NACOS_ENABLED = "true"
$env:PROFILE_REMOTE_ENABLED = "true"
$env:PROFILE_SERVICE_URL = "http://127.0.0.1:8083"

mvn -pl learning-service spring-boot:run
```

JWT 密钥应使用项目认可的足够长度和强度的随机值。这里的 `INTERNAL_API_KEY` 必须与 Profile 终端一致。

默认训练数据库账号为 `root`；可通过 `DB_USERNAME`、`DB_URL` 调整。Learning 的 `spring.application.name` 是 `training-service`，端口默认 `8082`。

### 6.4 启动 Gateway Service

在项目根目录打开 PowerShell 终端 C：

```powershell
$env:NACOS_ENABLED = "true"
mvn -pl gateway-service spring-boot:run
```

默认 Gateway 地址为 `http://localhost:8080`。现有路由将 `/api/**` 转发至 `lb://training-service`，因此请确认 Nacos 中存在健康的 `training-service` 实例。

### 6.5 启动前端

在第四个终端执行：

```powershell
cd frontend
npm install
npm run dev
```

开发环境前端通常位于 `http://localhost:5173`，以 Vite 实际输出地址为准。前端应使用实际已配置的 API 基础地址。

### 6.6 常见问题

- **Gateway 返回 503：** 检查 Nacos 是否运行、Learning 是否以 `training-service` 名称注册，以及 Gateway 和 Learning 终端是否均设置了 `NACOS_ENABLED=true`。
- **画像接口失败：** 检查 Profile 是否监听 `127.0.0.1:8083`，`PROFILE_SERVICE_URL` 是否正确，以及两端 `INTERNAL_API_KEY` 是否一致。
- **数据库连接失败：** 检查 MySQL 是否启动、数据库是否存在、账号和密码环境变量是否正确。
- **前端空白或请求失败：** 检查 Vite 控制台、浏览器 Network/Console 与 Gateway 状态。

## 7. 构建与测试

在项目根目录运行后端测试：

```powershell
mvn -pl learning-service,gateway-service,profile-service -am test
```

构建前端：

```powershell
cd frontend
npm run build
```

以本机测试输出中的 `BUILD SUCCESS`、失败数为 0 和前端成功构建信息为准。

## 8. 初代源码验收说明

本阶段已经进行过的实际验证包括：

- 用户能够登录并进入前端页面。
- 十题诊断会话的状态在数据库中为 `FINISHED`，且存在相应十条诊断答题记录。
- 训练与阶段测评答题记录可在数据库中查询。
- 前端五类 Mastery 值与 Profile 数据库对应账号的实际记录一致。
- 根据薄弱主题的训练推荐可在前端展示。
- Nacos 控制台有服务健康实例的检查记录。
- 后端 Maven 测试、前端构建经本地验收通过（以保存的日志为凭）。

验收截图应保留登录、诊断结果、能力画像、SQL 核对和服务注册状态；对外分享时应隐藏密码、JWT 和内部密钥。

**验收边界：** 页面及数据库验证不自动等于所有内部 HTTP 调用、熔断、限流、分布式事务和链路追踪已经实现。第 11 周初代源码以稳定的真实业务闭环为交付重点。

## 9. 后续版本计划（未作为初代源码的完成声明）

- 将 User、Training、Profile、Plan 四个业务边界逐步独立部署。
- 将 Learning → Profile 的固定地址调用升级为通过服务发现寻址，并补充跨服务异常与超时验证。
- 根据课程后续评分要求评估并验证 Sentinel、SkyWalking、Seata 等组件。
- 进一步完善独立数据库归属、接口契约、部署复现与自动化测试。

## 10. 代码版本

- 本阶段初代源码分支：`feat/v0.4-microservices`
- 验收时已推送的基线提交：`576c3a6`
- GitHub 仓库：<https://github.com/vulinh190291-png/RentWise>

修改本 README 后，应创建新的文档提交。课程正式交付时，请提供包含最终 README 的分支链接和实际最新 Commit，而不只提供默认 `main` 分支。
