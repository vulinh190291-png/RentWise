
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

import { login as loginApi } from '../api/auth'
import { getCurrentUser } from '../api/user'

import type {
    CurrentUser,
    LoginRequest,
} from '../types/api'

const TOKEN_KEY = 'rentwise_access_token'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(
        sessionStorage.getItem(TOKEN_KEY),
    )

    const user = ref<CurrentUser | null>(null)
    const loading = ref(false)

    const isAuthenticated = computed(() => !!token.value)

    // 登录
    async function login(payload: LoginRequest) {
        loading.value = true

        try {
            const result = await loginApi(payload)

            token.value = result.accessToken

            sessionStorage.setItem(
                TOKEN_KEY,
                result.accessToken,
            )

            user.value = await getCurrentUser()
        } catch (error) {
            logout()
            throw error
        } finally {
            loading.value = false
        }
    }

    // 获取当前用户
    async function loadCurrentUser() {
        if (!token.value) {
            user.value = null
            return
        }

        try {
            user.value = await getCurrentUser()
        } catch (error) {
            logout()
            throw error
        }
    }

    // 退出登录
    function logout() {
        token.value = null
        user.value = null

        sessionStorage.removeItem(TOKEN_KEY)
    }

    return {
        token,
        user,
        loading,
        isAuthenticated,
        login,
        loadCurrentUser,
        logout,
    }
})
