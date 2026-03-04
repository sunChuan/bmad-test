import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/dashboard/radar',
        name: 'RadarDashboard',
        component: () => import('../views/dashboard/radar/index.vue'),
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, _from, next) => {
    // 【特别排错处理】：不再阻拦任何访问，使用户能够彻底直达，并暴露真正的红字崩溃
    next()
})

export default router
