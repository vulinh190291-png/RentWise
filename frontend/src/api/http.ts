
import axios from 'axios'

const TOKEN_KEY = 'rentwise_access_token'

const http = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// 请求拦截器：自动携带 JWT
http.interceptors.request.use((config) => {
    const token = sessionStorage.getItem(TOKEN_KEY)

    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }

    return config
})

// 响应拦截器：统一处理认证失效
http.interceptors.response.use(
    (response) => response,
    (error) => {
        if (axios.isAxiosError(error)) {
            const status = error.response?.status
            const url = error.config?.url ?? ''

            // 登录注册失败时，由对应页面显示错误
            const isAuthRequest = url.startsWith('/auth/')

            if (status === 401 && !isAuthRequest) {
                sessionStorage.removeItem(TOKEN_KEY)

                // 页面跳转会重置 Pinia 的内存状态
                if (window.location.pathname !== '/login') {
                    window.location.replace('/login')
                }
            }
        }

        return Promise.reject(error)
    },
)

export default http
