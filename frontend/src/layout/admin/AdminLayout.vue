<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo-area">
        <el-icon class="logo-icon" color="#3b82f6" :size="24"><DataBoard /></el-icon>
        <span class="logo-text">平台管理后台</span>
      </div>
      <el-menu class="custom-menu" default-active="1">
        <el-menu-item index="1"><el-icon><PieChart /></el-icon><span>数据看板</span></el-menu-item>
        <el-menu-item index="2"><el-icon><User /></el-icon><span>用户与信用管理</span></el-menu-item>
        <el-menu-item index="3"><el-icon><OfficeBuilding /></el-icon><span>自习室与座位管理</span></el-menu-item>
        <el-menu-item index="4"><el-icon><List /></el-icon><span>订单流水管理</span></el-menu-item>
        <el-menu-item index="5"><el-icon><Filter /></el-icon><span>交流板内容审核</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>当前页面</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar size="small" icon="Avatar" style="background: #3b82f6;" />
              <span class="username">超级管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item icon="SwitchButton" @click="logout">退出系统</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-bg">
        <div class="content-card">
          <h2>管理端内容区域 (占位)</h2>
          <p>管理端同样采用了现代化的圆角卡片设计~</p>
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const logout = () => router.push('/login')
</script>

<style scoped>
.layout-container { height: 100vh; background-color: #f3f4f6; }
.aside { background-color: #ffffff; border-right: none; box-shadow: 2px 0 8px rgba(0,0,0,0.02); z-index: 10; }
.logo-area { height: 70px; display: flex; align-items: center; justify-content: center; border-bottom: 1px dashed #f3f4f6; margin-bottom: 10px;}
.logo-text { font-size: 18px; font-weight: bold; color: #1f2937; margin-left: 8px; letter-spacing: 1px;}
.custom-menu { border-right: none; padding: 0 10px; }
.custom-menu .el-menu-item { border-radius: 10px; margin-bottom: 5px; color: #4b5563; height: 50px; line-height: 50px; }
.custom-menu .el-menu-item:hover { background-color: #eff6ff; color: #3b82f6; }
.custom-menu .el-menu-item.is-active { background-color: #eff6ff; color: #3b82f6; font-weight: bold; }
.header { background-color: #ffffff; border-bottom: none; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 8px rgba(0,0,0,0.02); z-index: 9; }
.header-right { display: flex; align-items: center; }
.user-dropdown { display: flex; align-items: center; cursor: pointer; color: #333; }
.username { margin: 0 8px; font-size: 14px; font-weight: bold; }
.main-bg { padding: 20px; }
.content-card { background-color: #ffffff; border-radius: 16px; padding: 24px; height: 100%; box-sizing: border-box; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
</style>
