# RentWise 模块化单体 MVP Demo 设计说明

日期：2026-10-08  
版本：v0.1

## 1. 目标

本阶段交付一个可运行、可演示、可上传 GitHub 的 **RentWise 模块化单体（Modular Monolith）MVP Demo**。

核心目标不是提前完成最终微服务系统，而是先跑通业务闭环：

> 初始诊断 → 能力画像 → 针对训练 → Mastery 更新 → 阶段测评

同时在单体内部提前按未来微服务边界组织代码，使后续能够平滑拆分为 User Service、Training Service、Profile Service、Plan Service。

## 2. 本阶段范围

### 2.1 必做

1. 预置 10 个诊断案例，5 个主题各 2 个。
2. 用户能够完成一次 10 题初始诊断。
3. 系统根据诊断结果生成 5 个主题的 Mastery。
4. 系统能够识别当前最低 Mastery 主题。
5. 系统根据薄弱主题推荐下一道训练题。
6. 用户答题后，对应主题 Mastery 能够重新计算。
7. 同一主题连续答错 2 题后，触发识别卡，并降低下一题难度。
8. 用户能够完成一次简单阶段测评，测评结果继续更新 Mastery。
9. 关键业务数据持久化到 MySQL。
10. 提供 REST API 与 Swagger/OpenAPI，能够从接口层完整演示核心闭环。

### 2.2 暂不做

- Nacos
- Sentinel
- Gateway
- Seata
- SkyWalking
- Redis
- RocketMQ
- Vue3 正式前端
- 真实租房合同上传
- OCR / PDF 解析
- AI 合同分析
- 法律判断
- 多地区规则
- 复杂社区
- 复杂后台审核工作流

这些内容属于后续课程迭代，不进入 v0.1 Demo。

## 3. 模块边界

虽然当前只有一个 Spring Boot 应用，但代码按四个领域模块组织。

### 3.1 User 模块

职责：用户身份与基础用户信息。

当前 Demo 只保留最小用户能力，不提前实现复杂 RBAC。

预留未来拆分目标：`user-service`。

### 3.2 Training 模块

职责：训练内容与答题事实。

包含：

- 风险主题
- 模拟合同案例
- 案例难度
- 识别卡
- 初始诊断题
- 普通训练题
- 阶段测评题
- 答题记录

Training 只记录“用户做了什么”，不负责决定最终 Mastery。

预留未来拆分目标：`training-service`。

### 3.3 Profile 模块

职责：根据用户答题事实计算并维护能力状态。

核心内容：

- 五类 Mastery
- 最近同主题答题窗口
- 连续错误次数
- 能力画像

预留未来拆分目标：`profile-service`。

### 3.4 Plan 模块

职责：根据能力状态决定“下一步练什么”。

包含：

- 薄弱主题优先
- 下一题主题选择
- 下一题难度选择
- 连续错误干预
- 阶段测评触发与下一阶段建议

Plan 不直接访问 Profile 的 Repository；单体阶段通过模块业务接口获取能力状态，为后续远程调用留边界。

预留未来拆分目标：`plan-service`。

## 4. 模块依赖原则

核心原则：

> Training = 事实，Profile = 状态，Plan = 决策。

约束：

1. 模块之间不直接访问对方 Repository。
2. 不跨模块直接传递 JPA Entity，优先使用 DTO / Facade 接口。
3. Controller 不承载 Mastery 与推荐算法。
4. Mastery 计算只存在于 Profile 模块。
5. 推荐决策只存在于 Plan 模块。
6. MySQL 当前可共用一个库，但表按模块逻辑分组，避免跨模块外键耦合。

## 5. Mastery 规则

每个主题的 Mastery 使用最近最多 8 道同主题答题记录计算。

难度权重：

- EASY = 1
- MEDIUM = 2
- HARD = 3

计算：

`Mastery = 正确题难度权重之和 / 全部题难度权重之和 × 100`

若不足 8 道，则使用当前已有记录。

分档：

- 0～49：薄弱
- 50～79：一般
- 80～100：掌握较好

连续错误规则：

- 同主题连续错 2 题：触发识别卡；
- 下一题难度降低一级；
- 继续优先训练当前主题。

## 6. 核心业务流程

### 6.1 初始诊断

1. 用户开始诊断。
2. Training 返回 10 个诊断案例。
3. 用户逐题提交答案。
4. Training 保存答题事实。
5. Profile 根据五个主题答题结果初始化 Mastery。
6. Plan 找到当前最薄弱主题并给出首轮训练建议。
7. 返回能力画像与推荐结果。

### 6.2 针对训练

1. Plan 获取用户最新 Profile。
2. Plan 选择优先主题与难度。
3. Training 返回符合条件且近期未重复的案例。
4. 用户提交答案。
5. Training 保存记录。
6. Profile 重算对应主题 Mastery 与连续错误状态。
7. Plan 决定下一题。

### 6.3 阶段测评

1. Plan 决定进入阶段测评。
2. Training 提供混合主题题目。
3. 用户完成测评。
4. Training 保存答题记录。
5. Profile 更新 Mastery。
6. Plan 生成下一阶段建议。

## 7. 数据设计（v0.1）

建议首版表：

### User

- `users`

### Training

- `risk_topic`
- `training_case`
- `learning_card`
- `answer_record`
- `diagnosis_session`

### Profile

- `user_mastery`

### Plan

- `training_plan`

后续如果发现 `profile_snapshot` 或 `recommendation_state` 确有必要再增加，不提前制造表。

## 8. API 草案

只定义当前核心闭环所需接口。

- `POST /api/diagnosis/start`
- `POST /api/diagnosis/{sessionId}/answers`
- `POST /api/diagnosis/{sessionId}/finish`
- `GET /api/profile/{userId}`
- `GET /api/training/next?userId=...`
- `POST /api/training/answers`
- `POST /api/assessment/start`
- `POST /api/assessment/{sessionId}/finish`

实际实现时如果接口命名需要微调，可以调整，但不扩张业务范围。

## 9. 技术栈

- Java 17
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- MySQL 8
- Bean Validation
- Lombok
- springdoc-openapi / Swagger UI
- Maven

## 10. GitHub 仓库目标结构

```text
RentWise/
├── src/
│   └── main/java/.../rentwise/
│       ├── user/
│       ├── training/
│       ├── profile/
│       ├── plan/
│       └── common/
├── docs/
│   ├── architecture.md
│   ├── mvp.md
│   └── api.md
├── sql/
│   └── init.sql
├── README.md
├── pom.xml
└── .gitignore
```

## 11. 后续微服务演进

v0.1 完成后，再按课程节奏演进：

1. 将四个模块拆成独立服务。
2. Nacos：服务注册与配置中心。
3. Sentinel：诊断 / 训练接口限流；Profile 不可用时 Plan 降级。
4. Gateway + JWT + RBAC：统一入口和鉴权。
5. Seata：首次诊断初始化跨服务一致性。
6. SkyWalking：追踪 Training → Profile → Plan 调用链。
7. Redis / RocketMQ：只在真实业务场景下引入。

后续每引入一个中间件，都必须回答：

1. 它解决什么问题？
2. RentWise 哪个真实场景需要它？
3. 如果没有真实场景，如何最小成本满足课程要求而不污染业务设计？

## 12. v0.1 完成标准

满足以下条件即可认为 Demo v0.1 完成：

- 项目可以在本地启动；
- MySQL 初始化成功；
- Swagger 可以访问；
- 能完整演示“诊断 → 画像 → 训练 → Mastery 更新 → 阶段测评”；
- 不同答题表现能够产生不同 Mastery 与后续训练推荐；
- 连续错 2 题能够触发识别卡与降难；
- 核心逻辑有自动化测试；
- README 能让另一个开发者独立启动 Demo；
- 代码结构保持未来四服务边界。

## 13. 非目标

本 Demo 不用于证明真实市场需求已经被验证，也不声称具备法律审查能力。它只验证 RentWise 的产品核心闭环与后续微服务化的业务边界。
