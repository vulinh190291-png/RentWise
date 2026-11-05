
import http from './http'

import type {
    ApiResponse,
    NextTrainingResponse,
} from '../types/api'

// 获取下一步训练推荐
export async function getNextTraining(): Promise<NextTrainingResponse> {
    const response = await http.get<ApiResponse<NextTrainingResponse>>(
        '/training/next',
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '获取训练推荐失败')
    }

    return response.data.data
}
