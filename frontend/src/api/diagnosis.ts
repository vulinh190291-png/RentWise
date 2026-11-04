import http from './http'

import type {
    ApiResponse,
    DiagnosisStartResponse,
    SubmitAnswerRequest,
    AnswerResult,
    DiagnosisCompleteResponse,
} from '../types/api'

// 统一处理后端响应
function unwrapData<T>(
    response: ApiResponse<T>,
    fallbackMessage: string,
): T {
    if (!response.success || response.data === null) {
        throw new Error(response.message || fallbackMessage)
    }

    return response.data
}

// 开始诊断
export async function startDiagnosis(): Promise<DiagnosisStartResponse> {
    const response = await http.post<ApiResponse<DiagnosisStartResponse>>(
        '/diagnosis/start',
    )

    return unwrapData(response.data, '开始诊断失败')
}

// 提交答案
export async function submitDiagnosisAnswer(
    sessionId: number,
    request: SubmitAnswerRequest,
): Promise<AnswerResult> {
    const response = await http.post<ApiResponse<AnswerResult>>(
        `/diagnosis/${sessionId}/answers`,
        request,
    )

    return unwrapData(response.data, '提交答案失败')
}

// 完成诊断
export async function finishDiagnosis(
    sessionId: number,
): Promise<DiagnosisCompleteResponse> {
    const response = await http.post<ApiResponse<DiagnosisCompleteResponse>>(
        `/diagnosis/${sessionId}/finish`,
    )

    return unwrapData(response.data, '完成诊断失败')
}