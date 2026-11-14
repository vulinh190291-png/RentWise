
import http from './http'
import axios from 'axios'

import type {
    ApiResponse,
    UserProfileResponse,
} from '../types/api'

export class ProfileNotInitializedError extends Error {
    constructor() {
        super('请先完成初始诊断，再查看学习画像。')
        this.name = 'ProfileNotInitializedError'
    }
}

// 获取当前用户的学习画像
export async function getMyProfile(): Promise<UserProfileResponse> {
    const response = await http.get<ApiResponse<UserProfileResponse>>(
        '/profile/me',
    ).catch((error: unknown) => {
        // Only this endpoint's 404 means no profile; outages remain errors.
        if (axios.isAxiosError(error) && error.response?.status === 404) {
            throw new ProfileNotInitializedError()
        }
        throw error
    })

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '获取学习画像失败')
    }

    return response.data.data
}
