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
            <el-input v-model="loginForm.username" placeholder="请输入手机号" prefix-icon="User" size="large" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password class="custom-input"></el-input>
          </el-form-item>
          <el-form-item>
            <el-radio-group v-model="loginForm.role" class="role-group">
              <el-radio label="student">考研学生</el-radio>
              <el-radio label="manager">门店管理员</el-radio>
              <el-radio label="admin">系统管理员</el-radio>
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
            <el-input v-model="registerForm.username" placeholder="请输入手机号 (作为登录账号)" prefix-icon="User" size="large" class="custom-input"></el-input>
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
import axios from 'axios'

const router = useRouter()
const isLogin = ref(true)

const loginForm = ref({
  username: '',
  password: '',
  role: 'student'
})

const registerForm = ref({
  username: '',
  nickname: '',
  password: ''
})

// 手机号正则校验函数
const validatePhone = (phone) => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

const handleRealLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('账号和密码不能为空！')
    return
  }

  try {
    // 角色映射转换：admin -> 0, student -> 1, manager -> 2
    let roleInt = 1;
    if (loginForm.value.role === 'admin') {
      roleInt = 0;
    } else if (loginForm.value.role === 'manager') {
      roleInt = 2;
    }

    const res = await axios.post('http://localhost:8080/api/user/login', {
      username: loginForm.value.username,
      password: loginForm.value.password,
      role: roleInt
    });

    if (res.data.code === 200) {
      ElMessage.success('登录成功！');
      localStorage.setItem('userInfo', JSON.stringify(res.data.data));

      // 根据三种不同角色，跳转到不同的路由后台页面
      if (roleInt === 0) {
        router.push('/admin')
      } else if (roleInt === 2) {
        router.push('/manager') // 这里暂定路由为 /manager，后续我们需要建这个页面
      } else {
        router.push('/student')
      }
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (error) {
    ElMessage.error('服务连接失败，请检查后端！');
  }
}

const handleRealRegister = async () => {
  const { username, nickname, password } = registerForm.value;

  if (!username || !password || !nickname) {
    ElMessage.warning('所有信息都必须填写哦！')
    return
  }

  if (!validatePhone(username)) {
    ElMessage.warning('请输入正确的11位手机号格式！')
    return
  }

  try {
    const res = await axios.post('http://localhost:8080/api/user/register', {
      username: username,
      phone: username,
      nickname: nickname,
      password: password
    });

    if (res.data.code === 200) {
      ElMessage.success('注册成功！');
      isLogin.value = true;
      loginForm.value.username = username;
      // 注册完默认把角色切回学生（因为只有学生能通过外部自行注册）
      loginForm.value.role = 'student';
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (error) {
    ElMessage.error('注册失败，请检查网络！');
  }
}
</script>

<style scoped>
/* 样式保持不变 */
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
/* 如果三个选项挤在一起，这里稍微缩小一点字体或者间距确保美观 */
.role-group :deep(.el-radio) { margin-right: 15px; }
.role-group :deep(.el-radio:last-child) { margin-right: 0; }
.submit-btn { width: 100%; background-color: #10b981; border-color: #10b981; font-weight: bold; letter-spacing: 2px; margin-top: 10px;}
.submit-btn:hover { background-color: #059669; border-color: #059669; }
.form-footer { margin-top: 15px; text-align: center; font-size: 14px; }
.register-hint { color: #9ca3af; margin-right: 5px; }
</style>
