
import http from './http'

import type {
    ApiResponse,
    TrainingAnswerRequest,
    TrainingAnswerResponse,
} from '../types/api'

// 提交一条正式训练答案
export async function submitTrainingAnswer(
    request: TrainingAnswerRequest,
): Promise<TrainingAnswerResponse> {
    const response = await http.post<ApiResponse<TrainingAnswerResponse>>(
        '/training/answers',
        request,
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(
            response.data.message || '提交训练答案失败',
        )
    }

    return response.data.data
}
