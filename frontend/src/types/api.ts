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