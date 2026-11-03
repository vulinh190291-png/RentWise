import http from './http'

import type {
    ApiResponse,
    RegisterRequest,
    RegisterResponse,
    LoginRequest,
    LoginResponse,
} from '../types/api'

// 用户注册
export async function register(
    payload: RegisterRequest,
): Promise<RegisterResponse> {
    const response = await http.post<ApiResponse<RegisterResponse>>(
        '/auth/register',
        payload,
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '注册失败')
    }

    return response.data.data
}

// 用户登录
export async function login(
    payload: LoginRequest,
): Promise<LoginResponse> {
    const response = await http.post<ApiResponse<LoginResponse>>(
        '/auth/login',
        payload,
    )

    if (!response.data.success || !response.data.data) {
        throw new Error(response.data.message || '登录失败')
    }

    return response.data.data
}