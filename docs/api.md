# RentWise v0.1 API

所有接口返回统一结构：

```json
{
  "success": true,
  "data": {},
  "message": "OK"
}
```

## Diagnosis

### POST `/api/diagnosis/start?userId={userId}`

创建 10 题初始诊断。

### POST `/api/diagnosis/{sessionId}/answers`

```json
{
  "caseId": 1,
  "selectedClarify": true
}
```

### POST `/api/diagnosis/{sessionId}/finish`

完成诊断，初始化 Profile 和 Plan，并返回下一道训练题。重复调用不会重复创建 Mastery 或计划。

## Profile

### GET `/api/profile/{userId}`

返回五类 Mastery。未完成初始化的用户返回明确的 404 领域错误。

## Training

### GET `/api/training/next?userId={userId}`

根据最弱主题和连续错误状态推荐下一题。

### POST `/api/training/answers`

```json
{
  "userId": 1,
  "caseId": 5,
  "selectedClarify": true
}
```

返回：答题结果、解释、建议追问、更新后的 Mastery、下一题。

## Assessment

### POST `/api/assessment/start?userId={userId}`

创建 5 题混合主题阶段测评。

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
