export interface ApiResponse<T> {
    success: boolean
    data: T | null
    message: string
}

export type UserRole =
    | 'LEARNER'
    | 'CONTENT_EDITOR'
    | 'ADMIN'

export interface RegisterRequest {
    username: string
    password: string
}

export interface RegisterResponse {
    id: number
    username: string
    role: UserRole
}

export interface LoginRequest {
    username: string
    password: string
}

export interface LoginResponse {
    accessToken: string
    tokenType: string
    expiresIn: number
}

export interface CurrentUser {
    id: number
    username: string
    role: UserRole
}

// ==============================
// Diagnosis 学习诊断模块
// ==============================

// 风险知识主题
export type RiskTopic =
    | 'DEPOSIT_RETURN'
    | 'EARLY_TERMINATION'
    | 'REPAIR_RESPONSIBILITY'
    | 'COST_BEARING'
    | 'BREACH_LIABILITY'

// 题目难度
export type Difficulty =
    | 'EASY'
    | 'MEDIUM'
    | 'HARD'

// 诊断题目
export interface CaseView {
    caseId: number
    topic: RiskTopic
    difficulty: Difficulty
    clauseText: string
    question: string
}

// 开始诊断响应
export interface DiagnosisStartResponse {
    sessionId: number
    userId: number
    cases: CaseView[]
}

// 提交答案请求
export interface SubmitAnswerRequest {
    caseId: number
    selectedClarify: boolean
}

// 答题结果
export interface AnswerResult {
    answerId: number
    caseId: number
    topic: RiskTopic
    difficulty: Difficulty
    correct: boolean
}

// 单个知识主题掌握情况
export interface TopicMasteryView {
    topic: RiskTopic
    topicName: string
    masteryScore: number
    level: string
    consecutiveWrong: number
    lastDifficulty: Difficulty | null
}

// 用户学习画像
export interface UserProfileResponse {
    userId: number
    topics: TopicMasteryView[]
}

// 学习计划摘要
export interface PlanSummary {
    userId: number
    focusTopic: RiskTopic
    preferredDifficulty: Difficulty
}

// 学习卡片
export interface LearningCardView {
    topic: RiskTopic
    title: string
    content: string
}

// 下一步训练推荐
export interface NextTrainingResponse {
    userId: number
    topic: RiskTopic
    difficulty: Difficulty
    trainingCase: CaseView
    learningCard: LearningCardView | null
    reason: string
}

// 诊断完成响应
export interface DiagnosisCompleteResponse {
    sessionId: number
    userId: number
    profile: UserProfileResponse
    plan: PlanSummary
    nextTraining: NextTrainingResponse
}

//
// Adaptive Training 自适应训练模块
//

// 提交训练答案
export interface TrainingAnswerRequest {
    caseId: number
    selectedClarify: boolean
}

// 后端提供的即时反馈
export interface FeedbackView {
    correct: boolean
    explanation: string
    suggestedQuestion: string
}

// 提交训练答案后的完整响应
export interface TrainingAnswerResponse {
    answer: AnswerResult
    feedback: FeedbackView
    updatedMastery: TopicMasteryView
    nextTraining: NextTrainingResponse
}