<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo-title">研途共享自习室</div>
      <el-menu
        default-active="/student/home"
        class="el-menu-vertical"
        background-color="#ffffff"
        text-color="#333333"
        active-text-color="#10b981"
        router
      >
        <el-menu-item index="/student/home">
          <el-icon><DataBoard /></el-icon>
          <span>学习看板</span>
        </el-menu-item>

        <el-menu-item index="/student/booking">
          <el-icon><Location /></el-icon>
          <span>预约自习室</span>
        </el-menu-item>

        <el-menu-item index="/student/order">
          <el-icon><Document /></el-icon>
          <span>我的预约</span>
        </el-menu-item>

        <el-menu-item index="/student/forum">
          <el-icon><ChatLineRound /></el-icon>
          <span>研友交流板</span>
        </el-menu-item>
        <el-menu-item index="/student/ai">
          <el-icon><Cpu /></el-icon>
          <span>AI 伴学助理</span>
        </el-menu-item>
        <el-menu-item index="/student/repair">
          <el-icon><Warning /></el-icon>
          <span>设备故障报修</span>
        </el-menu-item>

        <el-menu-item index="/student/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="motto">愿你轻装上阵，一战成硕 🎓</span>
        </div>
        <div class="header-right">
          <span class="user-name">{{ userInfo.nickname || '同学' }}，你好！</span>
          <el-button type="danger" plain size="small" @click="handleLogout" icon="SwitchButton">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
// 确保引入了 Warning 图标
import {
  DataBoard,
  Location,
  Document,
  ChatLineRound,
  Warning,
  User,
  SwitchButton,
  Cpu
} from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside { background-color: #ffffff; box-shadow: 2px 0 8px rgba(0,0,0,0.05); z-index: 10; }
.logo-title { height: 60px; line-height: 60px; text-align: center; color: #10b981; font-size: 20px; font-weight: bold; letter-spacing: 1px; border-bottom: 1px solid #f0f2f5; }
.el-menu-vertical { border-right: none; }
.header { background-color: #fff; border-bottom: 1px solid #f0f2f5; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
.motto { font-size: 15px; color: #606266; font-weight: 500; }
.header-right { display: flex; align-items: center; }
.user-name { margin-right: 15px; font-size: 14px; color: #333; font-weight: bold; }
.main-content { background-color: #f4f7f6; padding: 20px; }
</style>
