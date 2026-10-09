# RentWise v0.2 API

所有接口返回统一结构：

```json
{
  "success": true,
  "data": {},
  "message": "OK"
}
```

除注册、登录和 Swagger/OpenAPI 文档外，业务接口都需要：

```http
Authorization: Bearer <access-token>
```

## Auth

### POST `/api/auth/register`

```json
{
  "username": "atopos",
  "password": "12345678"
}
```

公开注册固定创建 `LEARNER`，不能由客户端指定角色。

### POST `/api/auth/login`

```json
{
  "username": "atopos",
  "password": "12345678"
}
```

返回 `accessToken`、`tokenType=Bearer`、`expiresIn=3600`。

### GET `/api/users/me`

返回当前 JWT 对应的 `id / username / role`。

## Diagnosis

### POST `/api/diagnosis/start`

创建 10 题初始诊断。用户身份来自 JWT，不再接收 `userId`。

### POST `/api/diagnosis/{sessionId}/answers`

```json
{
  "caseId": 1,
  "selectedClarify": true
}
```

### POST `/api/diagnosis/{sessionId}/finish`

完成诊断，初始化 Profile 和 Plan，并返回下一道训练题。

## Profile

### GET `/api/profile/me`

返回当前用户的五类 Mastery。

## Training

### GET `/api/training/next`

根据当前用户的最弱主题和连续错误状态推荐下一题。

### POST `/api/training/answers`

```json
{
  "caseId": 5,
  "selectedClarify": true
}
```

返回答题结果、解释、建议追问、更新后的 Mastery、下一题。

## Assessment

### POST `/api/assessment/start`

创建当前用户的 5 题混合主题阶段测评。

### POST `/api/assessment/{sessionId}/finish`

```json
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

提交测评答案、更新相关主题 Mastery，并返回下一阶段建议。

## Security semantics

- 未携带 Token、Token 格式错误、签名无效或过期：`401 Unauthorized`。
- 已认证但角色权限不足：`403 Forbidden`。
- diagnosis / assessment 的 `sessionId` 必须属于当前 JWT 用户；未知 session 与跨用户 session 都按 `404 Not Found` 处理，避免泄露其他用户会话是否存在。
- Access Token 默认有效期为 3600 秒。
