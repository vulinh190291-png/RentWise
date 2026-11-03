import axios from 'axios'

const http = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// 自动携带 JWT
http.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('rentwise_access_token')

    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }

    return config
})

export default http