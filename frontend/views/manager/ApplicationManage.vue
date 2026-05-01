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
      <el-table :data="data.tableData" v-loading="data.loading">
        <el-table-column label="序号" type="index" width="70"></el-table-column>
        <el-table-column label="活动名称" prop="activityName" width="180"></el-table-column>
        <el-table-column label="申请人" prop="userName" width="120"></el-table-column>
        <el-table-column label="申请人联系方式" prop="userPhone" width="130"></el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
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
        <el-table-column label="处理人" prop="reviewerName" width="120"></el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              type="success" 
              size="small" 
              @click="handleProcess(scope.row.id, true)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              type="danger" 
              size="small" 
              @click="handleProcess(scope.row.id, false)"
            >
              拒绝
            </el-button>
            <el-tag v-else type="info" size="small">已处理</el-tag>
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
import { ElMessage, ElMessageBox } from 'element-plus'
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
    // 如果是发布者，使用发布者专用接口
    if (data.user.role === 'ISSUER') {
      const params = {
        issuerId: data.user.id,
        pageNum: data.pageNum,
        pageSize: data.pageSize
      }
      
      // 添加状态过滤参数(如果有选择)
      if (data.searchStatus) {
        params.status = data.searchStatus.toLowerCase()  // 后端接受小写: pending/approved/rejected
      }
      
      const res = await request.get('/application/issuer/applications', { params })
      
      if (res.code === '200' && res.data) {
        // 标准化状态值为大写
        data.tableData = (res.data.list || []).map(app => ({
          ...app,
          status: normalizeStatus(app.status)  // 统一转换为大写
        }))
        data.total = res.data.total || 0
      }
    } else {
      // 管理员使用原有接口查看所有申请
      const params = {
        pageNum: data.pageNum,
        pageSize: data.pageSize
      }
      
      if (data.searchStatus) {
        params.status = data.searchStatus
      }
      
      const res = await request.get('/application/page', { params })
      
      if (res.code === '200' && res.data) {
        // 标准化状态值
        data.tableData = (res.data.list || []).map(app => ({
          ...app,
          status: normalizeStatus(app.status)  // 统一转换为大写
        }))
        data.total = res.data.total || 0
      }
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

// 处理申请
const handleProcess = async (applicationId, approved) => {
  try {
    await ElMessageBox.confirm(
      `确定${approved ? '通过' : '拒绝'}此申请吗?`, 
      '提示', 
      { type: 'warning' }
    )
    
    const res = await request.put('/application/issuer/process', null, {
      params: {
        applicationId: applicationId,
        issuerId: data.user.id,
        approved: approved
      }
    })
    
    if (res.code === '200') {
      ElMessage.success(`已${approved ? '通过' : '拒绝'}申请`)
      load()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
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
