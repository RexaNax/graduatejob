import { createRouter, createWebHashHistory, createWebHistory } from "vue-router";

/**
 * 路由配置
 * 
 * 【原理讲解】
 * 1. 路由守卫：在 beforeEach 中检查是否登录
 * 2. 白名单：login、register、share 页面不需要登录
 * 3. 未登录访问其他页面 → 跳转到登录页
 */
export const routes = [
    // 登录页
    {
        path: "/login",
        name: "login",
        hidden: true,
        component: () => import("../views/login/Login.vue"),
        meta: { requiresAuth: false }
    },
    // 分享页面（不需要登录）
    {
        path: "/share/:code",
        name: "share",
        hidden: true,
        component: () => import("../views/share/ShareAccess.vue"),
        meta: { requiresAuth: false }
    },
    // 根路由
    {
        path: "/",
        redirect: "index",
        hidden: true
    },
    {
        path: "/index",
        name: "index",
        hidden: true,
        component: () => import("../views/index/Index.vue")
    },
    {
        path: "/transTemplate",
        name: "transTemplate",
        hidden: true,
        component: () => import("../views/transTemplate/TransTemplate.vue")
    },
    {
        path: "/transProgress",
        name: "transProgress",
        hidden: true,
        component: () => import("../views/transProgress/TransProgress.vue")
    },
    {
        path: "/trash",
        name: "trash",
        hidden: true,
        component: () => import("../views/trash/trash.vue")
    },
    {
        path: "/videoPreview",
        name: "videoPreview",
        hidden: true,
        component: () => import("../views/preview/VideoPreview.vue")
    },
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

/**
 * 路由守卫
 * 
 * 【原理】
 * 1. 每次路由跳转前执行
 * 2. 检查目标页面是否需要登录
 * 3. 检查 localStorage 中是否有 token
 * 4. 未登录则跳转到登录页
 */
router.beforeEach((to, from, next) => {
    // 不需要认证的页面直接放行
    if (to.meta.requiresAuth === false) {
        next();
        return;
    }
    
    // 检查是否有 token
    const token = localStorage.getItem('token');
    if (!token) {
        // 未登录，跳转到登录页
        next('/login');
    } else {
        next();
    }
});

export default router;
