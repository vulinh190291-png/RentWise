
import http from './http'

import type {
    ApiResponse,
    AssessmentFinishRequest,
    AssessmentResultResponse,
    DiagnosisStartResponse,
} from '../types/api'

// 开始阶段测评，获取 5 道题
export async function startAssessment(): Promise<DiagnosisStartResponse> {
    const response = await http.post<ApiResponse<DiagnosisStartResponse>>(
        '/assessment/start',
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '开始测评失败')
    }

    return response.data.data
}

// 一次性提交 5 道题答案
export async function finishAssessment(
    sessionId: number,
    request: AssessmentFinishRequest,
): Promise<AssessmentResultResponse> {
    const response = await http.post<ApiResponse<AssessmentResultResponse>>(
        `/assessment/${sessionId}/finish`,
        request,
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '提交测评失败')
    }

    return response.data.data
}
