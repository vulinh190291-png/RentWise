# RentWise v0.1 架构说明

## 1. 当前形态

RentWise v0.1 是一个模块化单体。只部署一个 Spring Boot 进程，但内部保持未来微服务边界。

```text
Client / Swagger
      |
      v
Spring Boot Application
      |
+-----+----------+----------+------+
| User | Training | Profile | Plan |
+-----+----------+----------+------+
      |
      v
    MySQL 8
```

## 2. 四个模块

### User

负责最小用户身份数据。v0.1 不提前实现复杂登录、JWT、RBAC。

### Training

拥有：案例、识别卡、诊断/测评会话、答题记录。它只回答“用户做了什么”。

### Profile

根据 Training 暴露的答题事实计算 Mastery、连续错误状态和能力画像。它回答“用户现在掌握多少”。

### Plan

读取 Profile 状态，通过 Training 选择合适案例，决定薄弱主题和下一题难度。它回答“用户下一步练什么”。

## 3. 边界约束

- 不跨模块直接访问 Repository。
- 不跨模块传 JPA Entity。
- Mastery 算法只在 Profile。
- 推荐决策只在 Plan。
- Training 只提供事实数据和案例选择能力。
- 当前共用一个 MySQL 库，但没有跨模块外键。

## 4. 后续拆分

未来对应：

```text
user        -> user-service
training    -> training-service
profile     -> profile-service
plan        -> plan-service
```

单体中的 Facade 调用未来可以替换为 OpenFeign/HTTP 调用，而业务职责不需要重写。

## 5. 中间件演进

- Nacos：服务注册发现 + 配置中心
- Sentinel：诊断/训练限流；Profile 不可用时 Plan 降级
- Gateway：统一入口
- JWT/RBAC：统一身份与权限
- Seata：首次诊断初始化跨服务一致性
- SkyWalking：Training → Profile → Plan 链路追踪
- Redis/RocketMQ：有真实缓存或异步场景后再接入
