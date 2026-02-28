<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo-title">系统管理总后台</div>
      <el-menu
        default-active="/admin/home"
        class="el-menu-vertical"
        background-color="#2c3e50"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/admin/home">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>

        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户与信用管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/repair">
          <el-icon><Tools /></el-icon>
          <span>设备报修处理</span>
        </el-menu-item>
        <el-sub-menu index="/admin/store-manage">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>门店与座位管理</span>
          </template>
          <el-menu-item index="/admin/room">自习室门店设置</el-menu-item>
          <el-menu-item index="/admin/seat">座位与分区管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/order">
          <el-icon><List /></el-icon>
          <span>订单流水管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/forum">
          <el-icon><ChatDotSquare /></el-icon>
          <span>交流板内容审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/feedback">
          <el-icon><Message /></el-icon>
          <span>意见反馈查看</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>总后台</el-breadcrumb-item>
            <el-breadcrumb-item>当前页面</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="admin-name">超级管理员，您好</span>
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
import {
  DataLine, User, OfficeBuilding, List, ChatDotSquare, Tools, Message
} from '@element-plus/icons-vue'

const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #2c3e50;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  z-index: 10;
}
.logo-title {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  background-color: #1a252f;
  letter-spacing: 1px;
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
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.header-left {
  font-size: 14px;
}
.header-right {
  display: flex;
  align-items: center;
}
.admin-name {
  margin-right: 15px;
  font-size: 14px;
  color: #606266;
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
