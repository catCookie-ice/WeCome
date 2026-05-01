<template>
  <div>
    <div class="card" style="margin-bottom: 10px;">
      <el-input v-model="data.searchName" :prefix-icon="Search" style="width: 300px; margin-right: 10px"
                placeholder="请输入活动名称"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>
    
    <div class="card">
      <el-row :gutter="20">
        <el-col :span="8" v-for="activity in data.tableData" :key="activity.id">
          <el-card shadow="hover" class="activity-card">
            <div class="activity-header">
              <h3>{{ activity.activityName }}</h3>
              <el-tag v-if="activity.isExpired === 0" type="success">招募中</el-tag>
              <el-tag v-else type="info">已结束</el-tag>
            </div>
            
            <div class="activity-content">
              <p class="description">{{ activity.activityDesc }}</p>
              
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>活动时间：{{ formatDate(activity.activityTime) }}</span>
              </div>
              
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>招募时间：{{ formatDate(activity.recruitStartTime) }}</span>
              </div>
              
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>所需人数：{{ activity.requiredPeople }} 人</span>
              </div>
              
              <div class="info-item" v-if="activity.is_full === 1">
                <el-tag type="danger" size="small">已满员</el-tag>
              </div>
            </div>
            
            <div class="activity-footer">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleApply(activity)"
                :disabled="activity.isExpired === 1 || activity.is_full === 1"
              >
                <el-icon><CirclePlus /></el-icon>
                申请参加
              </el-button>
              <el-button type="info" size="small" @click="handleView(activity)">
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div style="margin-top: 20px; text-align: center;" v-if="data.tableData.length === 0">
        <el-empty description="暂无活动数据" />
      </div>
      
      <div style="margin-top: 20px; text-align: center;">
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

  <!-- 详情对话框 -->
  <el-dialog 
    v-model="data.detailVisible" 
    title="活动详情" 
    width="50%"
  >
    <el-descriptions :column="2" border v-if="data.currentActivity">
      <el-descriptions-item label="活动名称" :span="2">{{ data.currentActivity.activityName }}</el-descriptions-item>
      <el-descriptions-item label="活动描述" :span="2">{{ data.currentActivity.activityDesc }}</el-descriptions-item>
      <el-descriptions-item label="所需人数">{{ data.currentActivity.requiredPeople }}</el-descriptions-item>
      <el-descriptions-item label="活动状态">
        <el-tag v-if="data.currentActivity.isExpired === 0" type="success">招募中</el-tag>
        <el-tag v-else type="info">已结束</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="活动时间">{{ formatDate(data.currentActivity.activityTime) }}</el-descriptions-item>
      <el-descriptions-item label="招募时间">{{ formatDate(data.currentActivity.recruitStartTime) }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Search, Clock, Calendar, User, CirclePlus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const data = reactive({
  searchName: '',
  tableData: [],
  pageNum: 1,
  pageSize: 9, // 每页显示9个，3行每行3个
  total: 0,
  loading: false,
  detailVisible: false,
  currentActivity: null,
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
    const res = await request.get('/volunteer/activity/list', {
      params: {
        isExpired: 0 // 只显示未过期的活动
      }
    })
    if (res.code === '200' && res.data) {
      let list = res.data
      
      // 如果有搜索条件，进行过滤
      if (data.searchName) {
        list = list.filter(item => 
          item.activityName && item.activityName.includes(data.searchName)
        )
      }
      
      data.total = list.length
      // 手动分页
      const start = (data.pageNum - 1) * data.pageSize
      const end = start + data.pageSize
      data.tableData = list.slice(start, end)
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
  data.searchName = ''
  data.pageNum = 1
  load()
}

// 分页切换
const handleCurrentChange = (pageNum) => {
  data.pageNum = pageNum
  load()
}

// 查看详情
const handleView = (row) => {
  data.currentActivity = row
  data.detailVisible = true
}

// 申请活动
const handleApply = async (activity) => {
  try {
    await ElMessageBox.confirm(
      `确定要申请参加"${activity.activityName}"吗?`, 
      '提示', 
      { type: 'info' }
    )
    
    const res = await request.post('/application/member/apply', null, {
      params: {
        activityId: activity.id,
        memberId: data.user.id
      }
    })
    
    if (res.code === '200') {
      ElMessage.success('申请成功，请等待审核')
      load()
    } else {
      ElMessage.error(res.msg || '申请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('申请失败')
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

.activity-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.activity-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.activity-content {
  margin-bottom: 15px;
}

.description {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.6;
  min-height: 60px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.info-item .el-icon {
  margin-right: 5px;
  color: #409EFF;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style>
