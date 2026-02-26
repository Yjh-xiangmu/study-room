import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login' // 默认跳转到登录页
    },
    {
      path: '/login',
      name: 'Login',
      // 登录/注册页面
      component: () => import('../views/login/Login.vue')
    },
    {
      path: '/admin',
      name: 'AdminLayout',
      // 管理员侧边栏骨架
      component: () => import('../layout/admin/AdminLayout.vue'),
      children: [
        // 后续的管理员子功能页面写在这里
      ]
    },
    {
      path: '/student',
      name: 'StudentLayout',
      // 学生侧边栏骨架
      component: () => import('../layout/student/StudentLayout.vue'),
      children: [
        // 后续的学生子功能页面写在这里
      ]
    }
  ]
})

export default router
