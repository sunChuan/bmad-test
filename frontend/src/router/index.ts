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
    if (to.meta.requiresAuth && !token) {
        // Navigate to SSO (simulated by /login)
        next({ name: 'Login' })
    } else {
        next()
    }
})

export default router
