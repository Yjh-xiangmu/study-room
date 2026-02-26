<template>
  <div class="user-manage-container">
    <div class="header-action">
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="输入手机号或昵称搜索" clearable style="width: 300px; margin-right: 15px;"></el-input>
        <el-button type="primary" @click="fetchData">搜索用户</el-button>
      </div>
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

    <el-dialog :title="`编辑用户信息`" v-model="editDialogVisible" width="450px" @close="resetEditForm">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="账号(手机号)">
          <el-input v-model="editForm.username" disabled placeholder="账号不可更改"></el-input>
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="editForm.nickname"></el-input>
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="editForm.password" show-password placeholder="不修改请留空"></el-input>
          <div style="font-size: 12px; color: #999; line-height: 1.2; margin-top: 5px;">如果用户忘记密码，可在此直接重置。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditUser">保存修改</el-button>
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

// 弹窗控制变量
const rechargeDialogVisible = ref(false)
const creditDialogVisible = ref(false)
const editDialogVisible = ref(false)

// 当前操作状态
const currentUser = ref({})
const rechargeAmount = ref(100.00)
const editForm = reactive({ id: null, username: '', nickname: '', password: '' })
const oldPassword = ref('') // 暂存旧密码

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

// 1. 充值功能
const handleRecharge = (row) => { currentUser.value = Object.assign({}, row); rechargeDialogVisible.value = true }
const submitRecharge = async () => {
  const res = await axios.put(`http://localhost:8080/api/admin/user/recharge/${currentUser.value.id}?amount=${rechargeAmount.value}`)
  if (res.data.code === 200) { ElMessage.success('充值成功！'); rechargeDialogVisible.value = false; fetchData() }
}

// 2. 信用分修改功能
const handleEditCredit = (row) => { currentUser.value = Object.assign({}, row); creditDialogVisible.value = true }
const submitCreditEdit = async () => {
  const res = await axios.put('http://localhost:8080/api/admin/user/update', { id: currentUser.value.id, creditScore: currentUser.value.creditScore })
  if (res.data.code === 200) { ElMessage.success('信用分修改成功！'); creditDialogVisible.value = false; fetchData() }
}

// 3. 【新增】通用编辑功能 (处理密码重置)
const handleEditUser = (row) => {
  editForm.id = row.id
  editForm.username = row.username
  editForm.nickname = row.nickname
  oldPassword.value = row.password // 记住原来的密码，防止没填时被清空
  editForm.password = '' // 弹窗里密码框默认留空
  editDialogVisible.value = true
}

const submitEditUser = async () => {
  // 组装要更新的数据，如果密码框留空，则沿用旧密码
  const payload = {
    id: editForm.id,
    nickname: editForm.nickname,
    password: editForm.password ? editForm.password : oldPassword.value
  }

  const res = await axios.put('http://localhost:8080/api/admin/user/update', payload)
  if (res.data.code === 200) {
    ElMessage.success('用户信息修改成功！')
    editDialogVisible.value = false
    fetchData()
  }
}
const resetEditForm = () => { Object.assign(editForm, { id: null, username: '', nickname: '', password: '' }) }

// 4. 【新增】删除用户功能
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
