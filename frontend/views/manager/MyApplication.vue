<template>
  <div>
    <div class="card" style="margin-bottom: 10px;">
      <el-select v-model="data.searchStatus" placeholder="请选择状态" style="width: 200px; margin-right: 10px;" clearable>
        <el-option label="待处理" value="PENDING"></el-option>
        <el-option label="已通过" value="APPROVED"></el-option>
        <el-option label="已拒绝" value="REJECTED"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>
    
    <div class="card">
      <el-table :data="data.tableData" v-loading="data.loading" stripe>
        <el-table-column label="序号" type="index" width="70"></el-table-column>
        <el-table-column label="活动名称" prop="activityName" width="200"></el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.reviewTime) }}
          </template>
        </el-table-column>
        <el-table-column label="处理结果" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.status === 'PENDING'">-</span>
            <span v-else-if="scope.row.status === 'APPROVED'" style="color: #67C23A;">恭喜您，申请已通过！</span>
            <span v-else-if="scope.row.status === 'REJECTED'" style="color: #F56C6C;">很遗憾，申请未通过</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div style="margin-top: 10px;">
        <el-pagination 
          background 
          layout="prev, pager, next, total" 
          @current-change="handleCurrentChange"
          v-model:current-page="data.pageNum" 
          v-model:page-size="data.pageSize" 
          :total="data.total"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

// 定义状态枚举常量
const ApplicationStatus = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED'
}

// 状态标准化函数 - 统一转换为大写
const normalizeStatus = (status) => {
  if (!status) return ApplicationStatus.PENDING
  const upperStatus = String(status).toUpperCase()
  return ApplicationStatus[upperStatus] || upperStatus
}

const data = reactive({
  searchStatus: '',
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  loading: false,
  user: JSON.parse(localStorage.getItem('login-user') || '{}')
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 加载数据
const load = async () => {
  data.loading = true
  try {
    const params = {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id
    }
    
    if (data.searchStatus) {
      params.status = data.searchStatus
    }
    
    const res = await request.get('/application/page', { params })
    
    if (res.code === '200' && res.data) {
      // 只显示当前普通用户的申请,并标准化状态值
      const myApplications = (res.data.list || [])
        .filter(app => app.userId === data.user.id)
        .map(app => ({
          ...app,
          status: normalizeStatus(app.status)  // 统一转换为大写
        }))
      
      data.tableData = myApplications
      data.total = myApplications.length
    }
  } catch (error) {
    console.error('加载失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    data.loading = false
  }
}

// 重置
const reset = () => {
  data.searchStatus = ''
  data.pageNum = 1
  load()
}

// 分页切换
const handleCurrentChange = (pageNum) => {
  data.pageNum = pageNum
  load()
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.card {
  background: white;
  padding: 20px;
  border-radius: 4px;
}
</style>
