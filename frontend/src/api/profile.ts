
import http from './http'

import type {
    ApiResponse,
    UserProfileResponse,
} from '../types/api'

// 获取当前用户的学习画像
export async function getMyProfile(): Promise<UserProfileResponse> {
    const response = await http.get<ApiResponse<UserProfileResponse>>(
        '/profile/me',
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '获取学习画像失败')
    }

    return response.data.data
}
