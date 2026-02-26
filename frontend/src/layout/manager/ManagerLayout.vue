<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo-title">门店管理端</div>
      <el-menu
        default-active="1"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/manager/home">
          <el-icon><DataBoard /></el-icon>
          <span>门店看板</span>
        </el-menu-item>
        <el-menu-item index="/manager/verify">
          <el-icon><Check /></el-icon>
          <span>入座核销</span>
        </el-menu-item>
        <el-menu-item index="/manager/recharge">
          <el-icon><Money /></el-icon>
          <span>余额充值</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">欢迎回来，店长</div>
        <div class="header-right">
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DataBoard, Check, Money } from '@element-plus/icons-vue'

const router = useRouter()

const handleLogout = () => {
  // 清除本地存储的用户信息并跳回登录页
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
}
.logo-title {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-size: 20px;
  font-weight: bold;
  background-color: #2b3643;
}
.el-menu-vertical {
  border-right: none;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.header-left {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
