<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-left">
        <h1 class="brand-title">研途共享自习室</h1>
        <p class="brand-desc">愿你轻装上阵，一战成硕。</p>
      </div>
      <div class="login-right">
        <h2 class="form-title">{{ isLogin ? '欢迎登录' : '注册新账号' }}</h2>

        <el-form v-if="isLogin" :model="loginForm" class="login-form">
          <el-form-item>
            <el-input v-model="loginForm.username" placeholder="请输入学号/手机号" prefix-icon="User" size="large" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-radio-group v-model="loginForm.role" class="role-group">
              <el-radio label="student">考研学生</el-radio>
              <el-radio label="admin">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="primary" @click="handleRealLogin" size="large" round>登 录</el-button>
          </el-form-item>
          <div class="form-footer">
            <span class="register-hint">还没有账号？</span>
            <el-link type="primary" :underline="false" @click="isLogin = false">立即注册</el-link>
          </div>
        </el-form>

        <el-form v-else :model="registerForm" class="login-form">
          <el-form-item>
            <el-input v-model="registerForm.username" placeholder="设置账号 (学号/手机号)" prefix-icon="User" size="large" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="registerForm.nickname" placeholder="怎么称呼您？(昵称)" prefix-icon="Postcard" size="large" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="registerForm.password" type="password" placeholder="设置密码" prefix-icon="Lock" size="large" show-password class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="success" @click="handleRealRegister" size="large" round>注 册</el-button>
          </el-form-item>
          <div class="form-footer">
            <span class="register-hint">已有账号？</span>
            <el-link type="primary" :underline="false" @click="isLogin = true">返回登录</el-link>
          </div>
        </el-form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
// 控制显示登录还是注册
const isLogin = ref(true)

// 登录表单数据
const loginForm = ref({
  username: '',
  password: '',
  role: 'student'
})

// 注册表单数据
const registerForm = ref({
  username: '',
  nickname: '',
  password: ''
})

// 真实的登录逻辑 (准备对接后端)
const handleRealLogin = () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('账号和密码不能为空哦！')
    return
  }
  ElMessage.info('准备向后端发送登录请求...')
  // TODO: 这里即将写入 axios 请求后端的代码

  // 暂时保留原本的跳转逻辑用于查看页面，后端通了之后会删掉这里
  if (loginForm.value.role === 'admin') {
    router.push('/admin')
  } else {
    router.push('/student')
  }
}

// 真实的注册逻辑 (准备对接后端)
const handleRealRegister = () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('账号和密码必须填写！')
    return
  }
  ElMessage.info('准备向后端发送注册请求...')
  // TODO: 这里即将写入 axios 请求后端的代码，注册成功后提示并切换回登录页
}
</script>

<style scoped>
/* 样式保持不变，非常清爽的小清新风格 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%);
}
.login-card {
  display: flex;
  width: 800px;
  height: 480px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand-title { font-size: 32px; font-weight: bold; margin-bottom: 15px; letter-spacing: 2px;}
.brand-desc { font-size: 16px; opacity: 0.9; }
.login-right {
  flex: 1;
  padding: 40px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.form-title { font-size: 24px; color: #333; margin-bottom: 30px; text-align: center; }
.custom-input :deep(.el-input__wrapper) { border-radius: 8px; box-shadow: 0 0 0 1px #e5e7eb inset; }
.custom-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #10b981 inset; }
.role-group { display: flex; justify-content: center; width: 100%; margin-top: 5px;}
.submit-btn { width: 100%; background-color: #10b981; border-color: #10b981; font-weight: bold; letter-spacing: 2px; margin-top: 10px;}
.submit-btn:hover { background-color: #059669; border-color: #059669; }
.form-footer { margin-top: 15px; text-align: center; font-size: 14px; }
.register-hint { color: #9ca3af; margin-right: 5px; }
</style>
