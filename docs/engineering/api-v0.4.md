# 当前 v0.4 API 说明

> Status: Draft
> Implementation: Partial
> Baseline: v0.4 / 25e67be3b43dc2dae35af57bd0da94acd86ecec9
> Owner: TBD
> Updated: 2026-11-13
> Source: 当前 Controller、DTO、SecurityConfig 与服务源码；静态核验，未运行接口

## 状态与访问入口
下列 14 个接口均有当前源码实现；运行验证均为 **Unverified（本轮未调用）**，不表示接口尚未编码。文档为 Draft，不能作为已通过联调的契约。目标 v1.0 四服务 API 不在本说明范围内。

业务入口默认 Gateway `http://localhost:8080`，路由 `/api/**` 到 Learning（默认 8082，注册名 training-service）；需实际启用 Nacos 并有健康实例。内部路径不匹配现有 Gateway 路由，按服务地址调用。Swagger `/swagger-ui.html` 和 `/v3/api-docs` 在 Learning 开放，当前 Gateway 未为其配置路由。

业务请求/响应使用 JSON。除 POST 注册、登录外，`/api/**` 需 `Authorization: Bearer <token>`；用户身份来自 JWT，不由客户端传 userId。内部接口使用 `X-Internal-Key`，不使用普通用户 JWT 作为内部凭证。

## 业务接口（11 个，源码已实现 / 运行 Unverified）
成功响应统一为 `ApiResponse<T>`：`{ "success": true, "data": T, "message": "OK" }`。表中类型指 data，未指定请求体的接口无需 JSON 请求体。

| 方法与路径 | 请求体 | 响应 data |
| --- | --- | --- |
| POST `/api/auth/register` | RegisterRequest | RegisterResponse |
| POST `/api/auth/login` | LoginRequest | LoginResponse |
| GET `/api/users/me` | 无 | CurrentUserResponse |
| POST `/api/diagnosis/start` | 无 | DiagnosisStartResponse |
| POST `/api/diagnosis/{sessionId}/answers` | SubmitAnswerRequest | AnswerResult |
| POST `/api/diagnosis/{sessionId}/finish` | 无 | DiagnosisCompleteResponse |
| GET `/api/profile/me` | 无 | UserProfileResponse |
| GET `/api/training/next` | 无 | NextTrainingResponse |
| POST `/api/training/answers` | TrainingAnswerRequest | TrainingAnswerResponse |
| POST `/api/assessment/start` | 无 | DiagnosisStartResponse（复用类型） |
| POST `/api/assessment/{sessionId}/finish` | AssessmentFinishRequest | AssessmentResultResponse |

### 请求校验与训练同步
RegisterRequest 的 username 非空且长度 3–32，password 非空且长度 8–64；LoginRequest 两项均非空。诊断/训练答题 caseId 不得为 null；训练答题还必须提供 UUID requestId。selectedClarify 为 Java primitive boolean，未标 NotNull，不能文档化成“缺失必然校验失败”。测评 answers 为非空列表，逐项校验 caseId；题目数量/会话状态等业务条件由服务层决定。

训练请求示意（caseId 必须替换为当前实际题目）：
```json
{"caseId":5,"selectedClarify":true,"requestId":"d8bfe87e-56d0-4c9e-9423-60a8147fd361"}
```
TrainingAnswerResponse 新增 syncStatus：`SYNCED` 表示同步及推荐流程正常返回；捕获画像相关 502/504 DomainException 时返回 `PENDING`，updatedMastery 和 nextTraining 为 null，答案与反馈仍返回。不能把 HTTP 成功等同于画像同步完成。请求去重实现见 TrainingAnswerPersistenceService；重试效果、并发与故障恢复待运行验证。

## 内部接口（3 个，源码已实现 / 运行 Unverified）
内部成功响应直接返回对象，**不包装 ApiResponse**。缺失或错误的 X-Internal-Key 在 Controller 抛出 401；内部错误体不可假定为业务统一包装。

| 服务、方法与路径 | 请求体 | 直接响应 |
| --- | --- | --- |
| Learning GET `/internal/training/users/{userId}/snapshot` | 无 | ProfileSnapshot |
| Profile POST `/internal/profiles/{userId}/sync` | ProfileSnapshot | ProfileView |
| Profile GET `/internal/profiles/{userId}` | 无 | ProfileView |

ProfileSnapshot = `{snapshotVersion: long, topics: TopicFacts[], recentUserAnswers: AnswerFact[]}`；TopicFacts = `{topic: String, answers: AnswerFact[]}`；AnswerFact = `{topic: String, difficulty: String, correct: boolean}`。ProfileView = `{userId: Long, topics: TopicView[]}`；TopicView 字段为 topic、topicName、masteryScore、level、consecutiveWrong、lastDifficulty（类型见源码）。Learning snapshot 对 userId ≤ 0 返回 400，无答题事实可能为 409。

## 错误与验证边界
Learning 的 GlobalExceptionHandler 将 DTO 校验失败映射到 400，将 DomainException 映射到其 status，响应为 `{success:false,data:null,message:...}`。SecurityConfig 配置认证/授权处理器；未认证和权限不足分别由其处理，不能推断所有框架异常、Gateway 错误和内部错误都有相同结构。

现有集成测试可作为复验入口，但 test 配置关闭 remote-enabled，因此本地模式测试不证明 Profile HTTP、Nacos 或 Gateway 联调通过。本轮未运行 Maven、启动服务或发起 HTTP 请求。

## DTO 字段与源码依据
以下为当前 Java record 声明摘录，保留字段类型及校验注解；不是目标接口设计。

### LoginRequest

[源码](../../learning-service/src/main/java/com/rentwise/auth/dto/LoginRequest.java)

```java
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
```

### LoginResponse

[源码](../../learning-service/src/main/java/com/rentwise/auth/dto/LoginResponse.java)

```java
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {}
```

### RegisterRequest

[源码](../../learning-service/src/main/java/com/rentwise/auth/dto/RegisterRequest.java)

```java
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 32) String username,
        @NotBlank @Size(min = 8, max = 64) String password
) {}
```

### RegisterResponse

[源码](../../learning-service/src/main/java/com/rentwise/auth/dto/RegisterResponse.java)

```java
public record RegisterResponse(Long id, String username, UserRole role) {}
```

### NextTrainingResponse

[源码](../../learning-service/src/main/java/com/rentwise/plan/dto/NextTrainingResponse.java)

```java
public record NextTrainingResponse(Long userId, RiskTopic topic, Difficulty difficulty,
                                   CaseView trainingCase, LearningCardView learningCard, String reason) {}
```

### PlanSummary

[源码](../../learning-service/src/main/java/com/rentwise/plan/dto/PlanSummary.java)

```java
public record PlanSummary(Long userId, RiskTopic focusTopic, Difficulty preferredDifficulty) {}
```

### TopicMasteryView

[源码](../../learning-service/src/main/java/com/rentwise/profile/dto/TopicMasteryView.java)

```java
public record TopicMasteryView(RiskTopic topic, String topicName, int masteryScore,
                               String level, int consecutiveWrong, Difficulty lastDifficulty) {}
```

### UserProfileResponse

[源码](../../learning-service/src/main/java/com/rentwise/profile/dto/UserProfileResponse.java)

```java
public record UserProfileResponse(Long userId, List<TopicMasteryView> topics) {}
```

### AnswerFactDto

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/AnswerFactDto.java)

```java
public record AnswerFactDto(Long answerId, Long caseId, RiskTopic topic, Difficulty difficulty,
                            boolean correct, Instant answeredAt) {}
```

### AnswerResult

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/AnswerResult.java)

```java
public record AnswerResult(Long answerId, Long caseId, RiskTopic topic, Difficulty difficulty, boolean correct) {}
```

### AssessmentAnswerInput

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/AssessmentAnswerInput.java)

```java
public record AssessmentAnswerInput(@NotNull Long caseId, boolean selectedClarify) {}
```

### AssessmentFinishRequest

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/AssessmentFinishRequest.java)

```java
public record AssessmentFinishRequest(@NotEmpty List<@Valid AssessmentAnswerInput> answers) {}
```

### AssessmentResultResponse

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/AssessmentResultResponse.java)

```java
public record AssessmentResultResponse(Long sessionId, Long userId, UserProfileResponse profile,
                                       NextTrainingResponse nextTraining) {}
```

### CaseView

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/CaseView.java)

```java
public record CaseView(Long caseId, RiskTopic topic, Difficulty difficulty, String clauseText, String question) {}
```

### DiagnosisCompleteResponse

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/DiagnosisCompleteResponse.java)

```java
public record DiagnosisCompleteResponse(Long sessionId, Long userId, UserProfileResponse profile,
                                        PlanSummary plan, NextTrainingResponse nextTraining) {}
```

### DiagnosisFinishResult

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/DiagnosisFinishResult.java)

```java
public record DiagnosisFinishResult(Long sessionId, Long userId, boolean alreadyFinished) {}
```

### DiagnosisStartResponse

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/DiagnosisStartResponse.java)

```java
public record DiagnosisStartResponse(Long sessionId, Long userId, List<CaseView> cases) {}
```

### FeedbackView

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/FeedbackView.java)

```java
public record FeedbackView(boolean correct, String explanation, String suggestedQuestion) {}
```

### LearningCardView

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/LearningCardView.java)

```java
public record LearningCardView(RiskTopic topic, String title, String content) {}
```

### SubmitAnswerRequest

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/SubmitAnswerRequest.java)

```java
public record SubmitAnswerRequest(@NotNull Long caseId, boolean selectedClarify) {}
```

### TrainingAnswerRequest

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/TrainingAnswerRequest.java)

```java
public record TrainingAnswerRequest(
        @NotNull Long caseId,
        boolean selectedClarify,
        @NotNull UUID requestId
) {
}
```

### TrainingAnswerResponse

[源码](../../learning-service/src/main/java/com/rentwise/training/dto/TrainingAnswerResponse.java)

```java
public record TrainingAnswerResponse(
        AnswerResult answer,
        FeedbackView feedback,
        TopicMasteryView updatedMastery,
        NextTrainingResponse nextTraining,
        String syncStatus
) {
}
```

### CurrentUserResponse

[源码](../../learning-service/src/main/java/com/rentwise/user/dto/CurrentUserResponse.java)

```java
public record CurrentUserResponse(Long id, String username, UserRole role) {}
```

## Controller 与基础设施来源

- [UserController](../../learning-service/src/main/java/com/rentwise/user/controller/UserController.java)
- [AssessmentController](../../learning-service/src/main/java/com/rentwise/training/controller/AssessmentController.java)
- [DiagnosisController](../../learning-service/src/main/java/com/rentwise/training/controller/DiagnosisController.java)
- [TrainingController](../../learning-service/src/main/java/com/rentwise/training/controller/TrainingController.java)
- [TrainingInternalController](../../learning-service/src/main/java/com/rentwise/training/controller/TrainingInternalController.java)
- [ProfileController](../../learning-service/src/main/java/com/rentwise/profile/controller/ProfileController.java)
- [PlanController](../../learning-service/src/main/java/com/rentwise/plan/controller/PlanController.java)
- [AuthController](../../learning-service/src/main/java/com/rentwise/auth/controller/AuthController.java)
- [ProfileInternalController](../../profile-service/src/main/java/com/rentwise/profilesvc/ProfileInternalController.java)
- [SecurityConfig](../../learning-service/src/main/java/com/rentwise/security/SecurityConfig.java)
- [ApiResponse](../../learning-service/src/main/java/com/rentwise/common/response/ApiResponse.java)
- [GlobalExceptionHandler](../../learning-service/src/main/java/com/rentwise/common/exception/GlobalExceptionHandler.java)
- [TrainingWorkflowService](../../learning-service/src/main/java/com/rentwise/training/service/TrainingWorkflowService.java)
- [TrainingAnswerPersistenceService](../../learning-service/src/main/java/com/rentwise/training/service/TrainingAnswerPersistenceService.java)
- [TrainingSnapshotService](../../learning-service/src/main/java/com/rentwise/training/service/TrainingSnapshotService.java)
- [ProfileSnapshot](../../profile-service/src/main/java/com/rentwise/profilesvc/ProfileSnapshot.java)
- [ProfileSnapshotService](../../profile-service/src/main/java/com/rentwise/profilesvc/ProfileSnapshotService.java)
- [application-test](../../learning-service/src/test/resources/application-test.yml)

[当前架构](../architecture/current/v0.4.md) · [历史 v0.3 API](../api.md) · [导航](../README.md)
