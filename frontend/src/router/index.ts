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
    const token = localStorage.getItem('access_token');

    // 如果缓存中包含 Token，且处于无需认证的鉴权页（如未重定向的跳板页），自动回流至大屏
    if (token && to.name === 'Login') {
        next({ name: 'RadarDashboard' })
        return
    }

    if (to.meta.requiresAuth && !token) {
        // Navigate to SSO (simulated by /login)
        next({ name: 'Login' })
    } else {
        next()
    }
})

export default router
