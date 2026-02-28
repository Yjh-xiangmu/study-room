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
      // 系统管理员侧边栏骨架
      component: () => import('../layout/admin/AdminLayout.vue'),
      children: [
        {
          // 这里的路径对应我们在侧边栏菜单里的 index="/admin/room"
          path: 'room',
          name: 'RoomManage',
          component: () => import('../views/admin/RoomManage.vue')
        },
        // 👇 新加的座位管理路由
        {
          path: 'seat',
          name: 'SeatManage',
          component: () => import('../views/admin/SeatManage.vue')
        },
        {
          path: 'user',
          name: 'UserManage',
          component: () => import('../views/admin/UserManage.vue')
        },
        {
          path: 'forum',
          name: 'AdminForum',
          component: () => import('../views/admin/AdminForum.vue')
        },
        {
          path: 'repair',
          name: 'RepairManage',
          component: () => import('../views/admin/RepairManage.vue')
        },
        {
          path: 'home',
          name: 'AdminHome',
          component: () => import('../views/admin/AdminHome.vue') // 记得去建个空的 AdminHome.vue 占位
        },
        {
          path: 'order',
          name: 'AdminOrder',
          component: () => import('../views/admin/AdminOrder.vue') // 记得去建个空的 AdminOrder.vue 占位
        },
        {
          path: 'feedback',
          name: 'AdminFeedback',
          component: () => import('../views/admin/AdminFeedback.vue')
        },
      ]
    },
    {
      path: '/manager',
      name: 'ManagerLayout',
      // 门店管理员侧边栏骨架
      component: () => import('../layout/manager/ManagerLayout.vue'),
      children: [
        // 后续的门店管理员子功能页面写在这里
        {
          path: 'verify',
          name: 'ManagerVerify',
          component: () => import('../views/manager/ManagerVerify.vue')
        }
      ]
    },
    {
      path: '/student',
      name: 'StudentLayout',
      // 学生侧边栏骨架
      component: () => import('../layout/student/StudentLayout.vue'),
      children: [
        {
          path: 'home', // 对应我们左侧菜单的 index="/student/home"
          name: 'StudentHome',
          component: () => import('../views/student/StudentHome.vue')
        },
        {
          path: 'booking',
          name: 'StudentBooking',
          component: () => import('../views/student/Booking.vue')
        },
        // 👇 新加的“我的预约”路由
        {
          path: 'order',
          name: 'StudentOrder',
          component: () => import('../views/student/StudentOrder.vue')
        },
        {
          path: 'forum',
          name: 'StudentForum',
          component: () => import('../views/student/StudentForum.vue')
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: () => import('../views/student/StudentProfile.vue')
        },
        {
          path: 'repair',
          name: 'StudentRepair',
          component: () => import('../views/student/StudentRepair.vue')
        },
        {
          path: 'ai',
          name: 'StudentAI',
          component: () => import('../views/student/StudentAI.vue')
        },
      ]
    }
  ]
})

export default router
