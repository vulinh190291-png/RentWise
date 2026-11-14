import { createRouter, createWebHistory } from 'vue-router'
const Training = () => import('../views/training/Training.vue')
const Login = () => import('../views/auth/Login.vue')
const Register = () => import('../views/auth/Register.vue')
const Dashboard = () => import('../views/dashboard/Dashboard.vue')
const Diagnosis = () => import('../views/diagnosis/Diagnosis.vue')
const Results = () => import('../views/results/Results.vue')
const Assessment = () => import('../views/assessment/Assessment.vue')

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/explore',
            name: 'explore',
            component: () => import('../views/explore/Explore.vue'),
        },
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
        {
            path: '/diagnosis',
            name: 'diagnosis',
            component: Diagnosis,
            meta: {
                requiresAuth: true,
            },
        },
        {
            path: '/results',
            name: 'results',
            component: Results,
            meta: {
                requiresAuth: true,
            },
        },
        {
            path: '/training',
            name: 'training',
            component: Training,
            meta: {
                requiresAuth: true,
            },
        },
        {
            path: '/assessment',
            name: 'assessment',
            component: Assessment,
            meta: {
                requiresAuth: true,
            },
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/login',
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
