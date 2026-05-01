<template>
  <div>
    <div class="card" style="margin-bottom: 10px;">
      <el-input v-model="data.searchName" :prefix-icon="Search" style="width: 300px; margin-right: 10px"
                placeholder="请输入活动名称"></el-input>
      <el-select v-model="data.searchStatus" placeholder="活动状态" style="width: 150px; margin-right: 10px;" clearable>
        <el-option label="进行中" :value="0"></el-option>
        <el-option label="已过期" :value="1"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>
    
    <div class="card">
      <div style="margin-bottom: 20px;">
        <el-statistic title="活动总数" :value="data.stats.total" />
        <el-statistic title="进行中" :value="data.stats.active" style="margin-left: 50px;" />
        <el-statistic title="已过期" :value="data.stats.expired" style="margin-left: 50px;" />
      </div>
      
      <el-table :data="data.tableData" v-loading="data.loading" stripe>
        <el-table-column label="序号" type="index" width="70"></el-table-column>
        <el-table-column label="活动名称" prop="activityName" width="180"></el-table-column>
        <el-table-column label="活动描述" prop="activityDesc" show-overflow-tooltip></el-table-column>
        <el-table-column label="发起人ID" prop="initiatorId" width="100" align="center"></el-table-column>
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
        <el-table-column label="所需人数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.requiredPeople }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-tag v-if="scope.row.isExpired === 0" type="success">进行中</el-tag>
            <el-tag v-else type="info">已过期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
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

  <!-- 详情对话框 -->
  <el-dialog 
    v-model="data.detailVisible" 
    title="活动详情" 
    width="50%"
  >
    <el-descriptions :column="2" border v-if="data.currentActivity">
      <el-descriptions-item label="活动名称" :span="2">{{ data.currentActivity.activityName }}</el-descriptions-item>
      <el-descriptions-item label="活动描述" :span="2">{{ data.currentActivity.activityDesc }}</el-descriptions-item>
      <el-descriptions-item label="发起人ID">{{ data.currentActivity.initiatorId }}</el-descriptions-item>
      <el-descriptions-item label="所需人数">{{ data.currentActivity.requiredPeople }}</el-descriptions-item>
      <el-descriptions-item label="活动时间">{{ formatDate(data.currentActivity.activityTime) }}</el-descriptions-item>
      <el-descriptions-item label="招募时间">{{ formatDate(data.currentActivity.recruitStartTime) }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDate(data.currentActivity.createdAt) }}</el-descriptions-item>
      <el-descriptions-item label="活动状态">
        <el-tag v-if="data.currentActivity.isExpired === 0" type="success">进行中</el-tag>
        <el-tag v-else type="info">已过期</el-tag>
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const data = reactive({
  searchName: '',
  searchStatus: null,
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  loading: false,
  detailVisible: false,
  currentActivity: null,
  stats: {
    total: 0,
    active: 0,
    expired: 0
  }
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await request.get('/volunteer/activity/list')
    if (res.code === '200' && res.data) {
      data.stats.total = res.data.length
      data.stats.active = res.data.filter(item => item.isExpired === 0).length
      data.stats.expired = res.data.filter(item => item.isExpired === 1).length
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载数据
const load = async () => {
  data.loading = true
  try {
    const res = await request.get('/volunteer/activity/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.searchName
      }
    })
    if (res.code === '200') {
      let list = res.data.list || []
      
      // 如果选择了状态过滤
      if (data.searchStatus !== null && data.searchStatus !== '') {
        list = list.filter(item => item.isExpired === data.searchStatus)
      }
      
      data.tableData = list
      data.total = res.data.total || 0
      
      // 刷新统计
      await loadStats()
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
  data.searchStatus = null
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

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此活动吗?', '提示', { type: 'warning' })
    const res = await request.delete(`/volunteer/activity/delete/${id}`)
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    console.log('取消删除')
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

.el-statistic {
  display: inline-block;
}
</style>
