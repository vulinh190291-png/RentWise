import http from './http'

import type {
    ApiResponse,
    CurrentUser,
} from '../types/api'

// 获取当前登录用户
export async function getCurrentUser(): Promise<CurrentUser> {
    const response = await http.get<ApiResponse<CurrentUser>>('/users/me')

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '获取用户信息失败')
    }

    return response.data.data
}