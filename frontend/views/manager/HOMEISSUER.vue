<template>
  <div>
    <div style="margin-bottom: 20px; font-size: 24px; font-weight: bold; color: #333;">
      发布者工作台
    </div>

    <!-- 待处理申请提醒 -->
    <el-alert
        v-if="data.pendingCount > 0"
        :title="`您有 ${data.pendingCount} 条待处理的活动申请，请及时处理！`"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon :size="30"><TrophyBase /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ data.stats.totalActivities }}</div>
            <div class="stat-label">发布的活动</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ data.stats.totalApplications }}</div>
            <div class="stat-label">活动申请数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon :size="30"><Select /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ data.stats.approvedApplications }}</div>
            <div class="stat-label">已通过申请</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon :size="30"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ data.pendingCount }}</div>
            <div class="stat-label">待处理申请</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近发布的活动 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold; font-size: 16px;">我发布的活动</span>
          <el-button type="primary" size="small" @click="goToActivityManage">管理活动</el-button>
        </div>
      </template>
      <el-table :data="data.recentActivities" style="width: 100%" v-loading="data.loading">
        <el-table-column prop="activityName" label="活动名称" width="180"></el-table-column>
        <el-table-column prop="activityDesc" label="活动描述" show-overflow-tooltip></el-table-column>
        <el-table-column label="活动时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.activityTime) }}
          </template>
        </el-table-column>
        <el-table-column label="招募时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.recruitStartTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isExpired === 0" type="success">进行中</el-tag>
            <el-tag v-else type="info">已过期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所需人数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.requiredPeople }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 待处理的申请 -->
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold; font-size: 16px;">待处理的申请</span>
          <el-button type="primary" size="small" @click="goToApplicationManage">查看全部</el-button>
        </div>
      </template>
      <el-table :data="data.pendingApplications" style="width: 100%" v-loading="data.loading">
        <el-table-column prop="activityName" label="活动名称" width="180"></el-table-column>
        <el-table-column prop="userName" label="申请人" width="120"></el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag type="warning">待处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleApprove(scope.row.id, true)">通过</el-button>
            <el-button type="danger" size="small" @click="handleApprove(scope.row.id, false)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TrophyBase, User, Select, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import router from '@/router'

// 状态标准化函数 - 统一转换为大写
const normalizeStatus = (status) => {
  if (!status) return 'PENDING'
  return String(status).toUpperCase()
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('login-user') || '{}'),
  pendingCount: 0,
  stats: {
    totalActivities: 0,
    totalApplications: 0,
    approvedApplications: 0,
  },
  recentActivities: [],
  pendingApplications: [],
  loading: false,
  pageNum: 1,
  pageSize: 20,
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载统计数据
const loadStats = async () => {
  data.loading = true
  try {
    // 检查待处理申请数量
    const pendingRes = await request.get(`/application/issuer/pending/count?issuerId=${data.user.id}`)
    if (pendingRes.code === '200') {
      data.pendingCount = pendingRes.data || 0
    }

    // 使用发布者专用接口加载活动列表
    const activitiesRes = await request.get('/volunteer/activity/publisher/list', {
      params: {
        publisherId: data.user.id,
        pageNum: data.pageNum,
        pageSize: data.pageSize  // 获取足够多的数据用于统计
      }
    })
    if (activitiesRes.code === '200' && activitiesRes.data && activitiesRes.data.list) {
      const myActivities = activitiesRes.data.list
      data.stats.totalActivities = activitiesRes.data.total || myActivities.length
      data.recentActivities = myActivities.slice(0, 5) // 显示最近5条
    }

    // 加载申请列表
    const applicationsRes = await request.get(`/application/page?pageNum=1&pageSize=100`)
    if (applicationsRes.code === '200' && applicationsRes.data && applicationsRes.data.list) {
      // 过滤出与当前发布者相关的申请,并标准化状态值
      const myApplications = applicationsRes.data.list
        .map(app => ({
          ...app,
          status: normalizeStatus(app.status)  // 统一转换为大写
        }))
        .filter(app => {
          const activity = data.recentActivities.find(a => a.id === app.activityId)
          return activity !== undefined
        })
      
      data.stats.totalApplications = myApplications.length
      data.stats.approvedApplications = myApplications.filter(app => app.status === 'APPROVED').length
      data.pendingApplications = myApplications.filter(app => app.status === 'PENDING').slice(0, 5)
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    data.loading = false
  }
}

// 处理申请
const handleApprove = async (applicationId, approved) => {
  try {
    const res = await request.put(`/application/issuer/process?applicationId=${applicationId}&issuerId=${data.user.id}&approved=${approved}`)
    if (res.code === '200') {
      ElMessage.success(approved ? '已通过申请' : '已拒绝申请')
      loadStats() // 刷新数据
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 跳转到活动管理页面
const goToActivityManage = () => {
  router.push('/volunteerActivity')
}

// 跳转到申请管理页面
const goToApplicationManage = () => {
  router.push('/applicationManage')
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}
</style>
