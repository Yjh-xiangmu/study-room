<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo-area">
        <el-icon class="logo-icon" color="#10b981" :size="24"><Reading /></el-icon>
        <span class="logo-text">研途自习室</span>
      </div>
      <el-menu class="custom-menu" default-active="1">
        <el-menu-item index="1"><el-icon><House /></el-icon><span>首页</span></el-menu-item>
        <el-menu-item index="2"><el-icon><Location /></el-icon><span>自习室大厅</span></el-menu-item>
        <el-menu-item index="3"><el-icon><Calendar /></el-icon><span>我的预约</span></el-menu-item>
        <el-menu-item index="4"><el-icon><ChatDotRound /></el-icon><span>研友交流板</span></el-menu-item>
        <el-menu-item index="5"><el-icon><Wallet /></el-icon><span>我的钱包</span></el-menu-item>
        <el-menu-item index="6"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="greeting">🌤️ 早上好，愿你今天也能高效学习！</span>
        </div>
        <div class="header-right">
          <el-tag type="success" effect="light" round class="credit-tag">信用分: 100</el-tag>
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar size="small" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="username">同学，你好</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item icon="User">个人资料</el-dropdown-item>
                <el-dropdown-item icon="SwitchButton" divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-bg">
        <div class="content-card">
          <h2>学生端内容区域 (占位)</h2>
          <p>看！现在外面有浅灰色的背景，内容区变成了纯白色的圆角卡片，看起来是不是高级多了？</p>
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
.logo-text { font-size: 18px; font-weight: bold; color: #333; margin-left: 8px; letter-spacing: 1px;}
.custom-menu { border-right: none; padding: 0 10px; }
.custom-menu .el-menu-item { border-radius: 10px; margin-bottom: 5px; color: #6b7280; height: 50px; line-height: 50px; }
.custom-menu .el-menu-item:hover { background-color: #f3f4f6; color: #10b981; }
.custom-menu .el-menu-item.is-active { background-color: #ecfdf5; color: #10b981; font-weight: bold; }
.header { background-color: #ffffff; border-bottom: none; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 8px rgba(0,0,0,0.02); z-index: 9; }
.greeting { color: #6b7280; font-size: 14px; }
.header-right { display: flex; align-items: center; }
.credit-tag { margin-right: 20px; font-weight: bold; }
.user-dropdown { display: flex; align-items: center; cursor: pointer; color: #333; }
.username { margin: 0 8px; font-size: 14px; }
.main-bg { padding: 20px; }
.content-card { background-color: #ffffff; border-radius: 16px; padding: 24px; height: 100%; box-sizing: border-box; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
</style>
