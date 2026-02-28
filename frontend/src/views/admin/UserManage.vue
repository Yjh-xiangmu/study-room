<template>
  <div class="user-manage-container">
    <div class="header-action">
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="输入手机号或昵称搜索" clearable style="width: 300px; margin-right: 15px;"></el-input>
        <el-button type="primary" @click="fetchData">搜索用户</el-button>
      </div>
      <el-button type="success" @click="handleAddUser">+ 新增用户</el-button>
    </div>

    <el-table :data="userList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
      <el-table-column label="角色" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.role === 1 ? 'success' : 'warning'">
            {{ scope.row.role === 1 ? '考研学生' : '门店管理员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="登录账号(手机号)" width="150" align="center"></el-table-column>
      <el-table-column prop="nickname" label="用户昵称" width="150" align="center"></el-table-column>

      <el-table-column label="信用积分" width="100" align="center">
        <template #default="scope">
          <strong v-if="scope.row.role === 1" :style="{ color: scope.row.creditScore < 80 ? '#F56C6C' : '#67C23A' }">
            {{ scope.row.creditScore }} 分
          </strong>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>

      <el-table-column label="钱包余额" width="120" align="center">
        <template #default="scope">
          <strong v-if="scope.row.role === 1" style="color: #E6A23C;">￥{{ scope.row.balance.toFixed(2) }}</strong>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="注册时间" align="center" width="180"></el-table-column>

      <el-table-column label="操作" width="300" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.role === 1" size="small" type="success" plain @click="handleRecharge(scope.row)">充值</el-button>
          <el-button v-if="scope.row.role === 1" size="small" type="warning" plain @click="handleEditCredit(scope.row)">信用</el-button>

          <el-button size="small" type="primary" plain @click="handleEditUser(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDeleteUser(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑用户信息' : '新增用户账号'" v-model="dialogVisible" width="450px" @close="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分配角色" v-if="!form.id">
          <el-radio-group v-model="form.role">
            <el-radio :label="1">考研学生</el-radio>
            <el-radio :label="2">门店管理员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="登录账号">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入11位手机号"></el-input>
          <div v-if="form.id" style="font-size: 12px; color: #999;">账号创建后不可修改</div>
        </el-form-item>

        <el-form-item label="用户昵称">
          <el-input v-model="form.nickname" placeholder="怎么称呼？"></el-input>
        </el-form-item>

        <el-form-item label="登录密码">
          <el-input v-model="form.password" show-password :placeholder="form.id ? '不修改请留空' : '默认密码: 123456'"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定保存</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="`为学生 [${currentUser.nickname || currentUser.username}] 充值`" v-model="rechargeDialogVisible" width="400px" @close="rechargeAmount = 100">
      <div style="text-align: center; margin: 20px 0;">
        <span style="margin-right: 15px;">充值金额 (元):</span>
        <el-input-number v-model="rechargeAmount" :min="1" :step="50" :precision="2"></el-input-number>
      </div>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="`调整学生 [${currentUser.nickname || currentUser.username}] 的信用分`" v-model="creditDialogVisible" width="400px">
      <div style="text-align: center; margin: 20px 0;">
        <span style="margin-right: 15px;">当前信用分:</span>
        <el-input-number v-model="currentUser.creditScore" :min="0" :max="100" :step="1"></el-input-number>
      </div>
      <template #footer>
        <el-button @click="creditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreditEdit">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const searchKeyword = ref('')
const loading = ref(false)
const userList = ref([])

// 弹窗状态
const rechargeDialogVisible = ref(false)
const creditDialogVisible = ref(false)
const dialogVisible = ref(false) // 新增/编辑的统一弹窗

const currentUser = ref({})
const rechargeAmount = ref(100.00)

// 通用表单数据 (合并了新增和编辑)
const form = reactive({ id: null, username: '', nickname: '', password: '', role: 1 })
const oldPassword = ref('') // 编辑时暂存的旧密码

onMounted(() => { fetchData() })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/admin/user/list', { params: { keyword: searchKeyword.value } })
    if (res.data.code === 200) userList.value = res.data.data
  } finally {
    loading.value = false
  }
}

// === 新增/编辑用户逻辑 ===
const handleAddUser = () => {
  dialogVisible.value = true
}

const handleEditUser = (row) => {
  form.id = row.id
  form.username = row.username
  form.nickname = row.nickname
  form.role = row.role
  oldPassword.value = row.password
  form.password = '' // 编辑时密码框留空
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.username) return ElMessage.warning('账号不能为空！')

  try {
    if (form.id) {
      // 走编辑更新接口
      const payload = {
        id: form.id,
        nickname: form.nickname,
        password: form.password ? form.password : oldPassword.value
      }
      const res = await axios.put('http://localhost:8080/api/admin/user/update', payload)
      if (res.data.code === 200) {
        ElMessage.success('修改成功！')
        dialogVisible.value = false
        fetchData()
      }
    } else {
      // 走新增接口
      const res = await axios.post('http://localhost:8080/api/admin/user/add', form)
      if (res.data.code === 200) {
        ElMessage.success('用户添加成功！')
        dialogVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.data.msg) // 弹出账号已存在的错误
      }
    }
  } catch (error) {
    ElMessage.error('操作失败，请检查网络！')
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, username: '', nickname: '', password: '', role: 1 })
  oldPassword.value = ''
}

// === 充值逻辑 ===
const handleRecharge = (row) => { currentUser.value = Object.assign({}, row); rechargeDialogVisible.value = true }
const submitRecharge = async () => {
  const res = await axios.put(`http://localhost:8080/api/admin/user/recharge/${currentUser.value.id}?amount=${rechargeAmount.value}`)
  if (res.data.code === 200) { ElMessage.success('充值成功！'); rechargeDialogVisible.value = false; fetchData() }
}

// === 信用分逻辑 ===
const handleEditCredit = (row) => { currentUser.value = Object.assign({}, row); creditDialogVisible.value = true }
const submitCreditEdit = async () => {
  const res = await axios.put('http://localhost:8080/api/admin/user/update', { id: currentUser.value.id, creditScore: currentUser.value.creditScore })
  if (res.data.code === 200) { ElMessage.success('信用分修改成功！'); creditDialogVisible.value = false; fetchData() }
}

// === 删除逻辑 ===
const handleDeleteUser = (id) => {
  ElMessageBox.confirm('确定要永久删除该用户吗？相关订单数据可能受影响。', '危险操作', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const res = await axios.delete(`http://localhost:8080/api/admin/user/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功！')
      fetchData()
    }
  }).catch(() => {})
}
</script>

<style scoped>
.user-manage-container { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-action { display: flex; justify-content: space-between; align-items: center; }
</style>
