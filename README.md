# RentWise

RentWise 是一个面向第一次独立租房用户的**租房签约条款识别训练系统**。当前仓库进入 **v0.2：JWT Authentication & RBAC**，仍保持模块化单体架构，在 v0.1 自适应训练闭环外增加真实用户身份与权限边界。

> RentWise 不替用户审合同，也不判断条款合法/违法。它训练用户识别“哪些内容值得继续确认”。

## v0.2 核心闭环

```text
注册
  ↓
登录
  ↓
JWT Access Token
  ↓
初始诊断
  ↓
五类 Mastery 能力画像
  ↓
薄弱主题优先训练
  ↓
即时反馈 + 建议追问
  ↓
Mastery 更新
  ↓
阶段测评
```

### 身份与权限

- 用户名 + 密码注册
- BCrypt 密码哈希，不保存明文密码
- 登录后签发 60 分钟 JWT Access Token
- Spring Security 无状态认证（STATELESS）
- `LEARNER / CONTENT_EDITOR / ADMIN` 三种固定角色
- 公开注册只能创建 `LEARNER`
- learner-facing API 不再接受客户端自行指定 `userId`
- diagnosis / assessment 的 `sessionId` 会校验归属，防止跨用户访问

当前 v0.2 不做 Refresh Token、Token 黑名单、验证码、找回密码、OAuth2/OIDC、第三方登录、动态权限表或前端登录页面。

## 为什么仍然先做模块化单体

代码只部署为一个 Spring Boot 应用，但内部按未来服务边界组织：

```text
user      -> 用户身份
training  -> 题目与答题事实
profile   -> Mastery 与能力画像
plan      -> 下一步训练决策
auth      -> 注册 / 登录
security  -> JWT / Spring Security / RBAC
```

核心原则：

```text
User = identity
Training = facts
Profile = state
Plan = decision
```

v0.2 先把认证授权边界做正确，不提前拆微服务。

## 技术栈

- Java 17
- Spring Boot 3.4.5
- Spring Security 6
- Spring Web
- Spring Data JPA
- Bean Validation
- MySQL 8
- JJWT 0.13.0
- BCrypt
- springdoc-openapi / Swagger UI
- Maven
- JUnit 5 / MockMvc / AssertJ
- H2（仅测试环境）

## 本地运行

### 1. 准备环境

需要：

- JDK 17+
- Maven 3.9+
- MySQL 8

### 2. 初始化数据库

全新环境：

```bash
mysql -u root -p < sql/init.sql
```

默认数据库名：

```text
rentwise
```

> 如果你从 v0.1 的本地数据库直接升级，旧 `users` 表只有 `id + username`，并可能包含无密码的 `demo_user`。v0.2 的用户表结构不兼容这个无密码 Demo 用户。开发环境中若没有需要保留的数据，建议先备份后重新初始化 `rentwise` 数据库，再启动 v0.2。不要在生产或有重要数据的环境中直接删除数据库。

### 3. 配置数据库

默认配置：

```text
DB_URL=jdbc:mysql://localhost:3306/rentwise?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=root
```

Windows PowerShell 示例：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
```

### 4. 配置 JWT Secret

`JWT_SECRET` 不写入仓库，运行前必须在环境变量中提供至少 32 字节的本地密钥。

PowerShell 示例：

```powershell
$env:JWT_SECRET="rentwise-local-secret-change-me-1234567890"
```

Access Token 默认有效期为 3600 秒，也可以通过：

```powershell
$env:JWT_EXPIRATION_SECONDS="3600"
```

覆盖。

### 5. 启动

```powershell
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

Swagger UI：

```text
http://localhost:8080/swagger-ui.html
```

## v0.2 演示顺序

### 1. 注册

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "atopos",
  "password": "12345678"
}
```

### 2. 登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "atopos",
  "password": "12345678"
}
```

从响应中取得 `accessToken`。

### 3. 在 Swagger 中授权

点击 **Authorize**，输入：

```text
Bearer <accessToken>
```

之后调用受保护接口。

### 4. 验证当前用户

```http
GET /api/users/me
```

### 5. 开始诊断

```http
POST /api/diagnosis/start
```

不再传 `userId`。

### 6. 提交 10 道诊断答案

```http
POST /api/diagnosis/{sessionId}/answers
Content-Type: application/json

{
  "caseId": 1,
  "selectedClarify": true
}
```

### 7. 完成诊断

```http
POST /api/diagnosis/{sessionId}/finish
```

### 8. 查看能力画像

```http
GET /api/profile/me
```

### 9. 获取并提交训练题

```http
GET /api/training/next
```

```http
POST /api/training/answers
Content-Type: application/json

{
  "caseId": 5,
  "selectedClarify": true
}
```

### 10. 阶段测评

```http
POST /api/assessment/start
```

然后：

```http
POST /api/assessment/{sessionId}/finish
Content-Type: application/json

{
  "answers": [
    {"caseId": 1, "selectedClarify": true},
    {"caseId": 3, "selectedClarify": true},
    {"caseId": 5, "selectedClarify": true},
    {"caseId": 7, "selectedClarify": true},
    {"caseId": 9, "selectedClarify": true}
  ]
}
```

## 401 与 403

```text
401 Unauthorized
```

表示没有成功认证，例如 Token 缺失、过期、格式错误或签名无效。

```text
403 Forbidden
```

表示身份已经认证成功，但当前角色没有访问目标资源的权限。

## 测试

```bash
mvn clean test
```

v0.2 重点验证：

- 公开注册只能创建 LEARNER
- BCrypt 保存密码哈希
- 用户名重复（含大小写变体）被拒绝
- 正确登录返回 JWT
- 错误 / 不存在 / disabled 账号登录失败
- 缺失、损坏、篡改、过期 Token 被拒绝
- 角色不足返回 403
- learner API 不再信任客户端 userId
- 不同用户不能操作对方的 diagnosis / assessment session
- 原有诊断 → Mastery → 自适应训练 → 阶段测评闭环仍然通过

## 产品设计与实现文档

- `docs/mvp.md`
- `docs/architecture.md`
- `docs/api.md`
- `docs/superpowers/specs/2026-10-09-rentwise-v0.2-auth-security-design.md`
- `docs/superpowers/plans/2026-10-09-rentwise-v0.2-auth-security-implementation.md`
