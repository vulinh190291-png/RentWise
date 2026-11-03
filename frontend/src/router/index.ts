import { createRouter, createWebHistory } from 'vue-router'

import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Dashboard from '../views/dashboard/Dashboard.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/dashboard',
        },
        {
            path: '/login',
            name: 'login',
            component: Login,
        },
        {
            path: '/register',
            name: 'register',
            component: Register,
        },
        {
            path: '/dashboard',
            name: 'dashboard',
            component: Dashboard,
            meta: {
                requiresAuth: true,
            },
        },
    ],
})

router.beforeEach((to) => {
    const token = sessionStorage.getItem('rentwise_access_token')

    if (to.meta.requiresAuth && !token) {
        return { name: 'login' }
    }

    if ((to.name === 'login' || to.name === 'register') && token) {
        return { name: 'dashboard' }
    }
})

export default router