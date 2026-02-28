<template>
  <div class="recharge-container">
    <div class="page-title">
      <h2 style="margin: 0; color: #333;"><el-icon><Money /></el-icon> 线下余额充值</h2>
      <p style="color: #999; font-size: 14px; margin-top: 10px;">请先收取学生现金或微信转账，再在此处为学生账户增加余额。</p>
    </div>

    <div class="action-area">
      <el-card shadow="hover" class="box-card">

        <div class="step-one">
          <el-input
            v-model="phone"
            placeholder="请输入需要充值的学生手机号"
            size="large"
            style="width: 300px; margin-right: 15px;"
            clearable>
          </el-input>
          <el-button type="primary" size="large" icon="Search" @click="searchUser">查询账户</el-button>
        </div>

        <el-divider></el-divider>

        <div v-if="student" class="step-two">
          <el-descriptions title="学生账户信息" :column="2" border>
            <el-descriptions-item label="学生姓名"><strong>{{ student.nickname }}</strong></el-descriptions-item>
            <el-descriptions-item label="手机号">{{ student.username }}</el-descriptions-item>
            <el-descriptions-item label="信用积分">
              <el-tag type="success">{{ student.creditScore }} 分</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前钱包余额">
              <strong style="color: #E6A23C; font-size: 16px;">￥{{ student.balance.toFixed(2) }}</strong>
            </el-descriptions-item>
          </el-descriptions>

          <div class="recharge-form" style="margin-top: 30px;">
            <span style="font-size: 16px; font-weight: bold; margin-right: 15px;">本次充值金额：</span>
            <el-input-number v-model="amount" :min="1" :max="500" :step="10" size="large"></el-input-number>
            <span style="margin-left: 10px;">元</span>

            <el-button type="success" size="large" style="margin-left: 30px;" @click="submitRecharge" :loading="recharging">确认充值入账</el-button>
          </div>
        </div>

        <el-empty v-else description="请先输入正确的手机号查询学生账户"></el-empty>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Money, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const phone = ref('')
const student = ref(null)
const amount = ref(50)
const recharging = ref(false)

const searchUser = async () => {
  if (!phone.value) return ElMessage.warning('请输入手机号！')
  try {
    const res = await axios.get(`http://localhost:8080/api/manager/user/info?phone=${phone.value}`)
    if (res.data.code === 200) {
      student.value = res.data.data
      amount.value = 50 // 重置默认金额
      ElMessage.success('已找到该账户！')
    } else {
      student.value = null
      ElMessage.error(res.data.msg)
    }
  } catch (e) {
    ElMessage.error('查询失败！')
  }
}

const submitRecharge = () => {
  ElMessageBox.confirm(`请确认您已线下收取 ${amount.value} 元？操作后将直接打入学生账户不可撤销！`, '充值确认', {
    confirmButtonText: '已收款，确认充值',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    recharging.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/manager/user/recharge', {
        phone: student.value.username,
        amount: amount.value
      })
      if (res.data.code === 200) {
        ElMessage.success({ message: res.data.msg, duration: 4000 })
        // 充值成功后重新查询一下，刷新显示的余额
        searchUser()
      } else {
        ElMessage.error(res.data.msg)
      }
    } finally {
      recharging.value = false
    }
  }).catch(() => {})
}
</script>

<style scoped>
.recharge-container { min-height: 80vh; }
.page-title { margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px solid #dcdfe6;}
.box-card { max-width: 800px; padding: 20px; border-radius: 8px;}
.step-one { display: flex; align-items: center; }
.recharge-form { background-color: #f8f9fa; padding: 20px; border-radius: 8px; display: flex; align-items: center;}
</style>
