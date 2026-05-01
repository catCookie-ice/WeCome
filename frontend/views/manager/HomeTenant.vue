<template>
  <div class="tenant-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>欢迎回来，{{ user.nickname || user.username }}！</h1>
        <p>加入志愿者队伍，用爱心温暖社会 ❤️</p>
      </div>
      <div class="banner-stats">
        <div class="stat-item">
          <div class="stat-number">{{ stats.myApplications }}</div>
          <div class="stat-label">我的申请</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.approvedCount }}</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.availableActivities }}</div>
          <div class="stat-label">可参与活动</div>
        </div>
      </div>
    </div>

    <!-- 热门志愿活动推荐 -->
    <div class="activities-section">
      <div class="section-header">
        <h2 class="section-title">
          <i class="el-icon-star-on"></i> 热门志愿活动
        </h2>
        <el-button type="primary" link @click="goToVolunteerList">
          查看更多 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>

      <!-- 活动卡片网格 -->
      <div class="activities-grid" v-loading="loading">
        <el-empty v-if="!loading && activities.length === 0" description="暂无可参与的志愿活动" />
        
        <div class="activity-card" v-for="activity in activities" :key="activity.id">
          <div class="card-header" :style="{ background: getRandomGradient() }">
            <h3 class="activity-name">{{ activity.activityName }}</h3>
            <el-tag v-if="activity.is_full === 1" type="danger" size="small">已满员</el-tag>
            <el-tag v-else-if="activity.isExpired === 1" type="info" size="small">已过期</el-tag>
            <el-tag v-else type="success" size="small">招募中</el-tag>
          </div>
          
          <div class="card-body">
            <p class="activity-desc">{{ activity.activityDesc || '暂无描述' }}</p>
            
            <div class="activity-info">
              <div class="info-item">
                <i class="el-icon-time"></i>
                <span>{{ formatTime(activity.activityTime) }}</span>
              </div>
              <div class="info-item">
                <i class="el-icon-user"></i>
                <span>需要 {{ activity.requiredPeople }} 人</span>
              </div>
            </div>
          </div>
          
          <div class="card-footer">
            <el-button 
              type="primary" 
              size="small"
              :disabled="activity.isExpired === 1 || activity.is_full === 1"
              @click="applyActivity(activity)"
            >
              立即申请
            </el-button>
            <el-button size="small" @click="viewDetail(activity)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新资讯 -->
    <div class="news-section">
      <div class="section-header">
        <h2 class="section-title">
          <i class="el-icon-document"></i> 最新资讯
        </h2>
        <el-button type="primary" link @click="goToNews">
          查看更多 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
      
      <div class="news-list" v-loading="newsLoading">
        <el-empty v-if="!newsLoading && newsList.length === 0" description="暂无资讯" />
        
        <div class="news-item" v-for="news in newsList" :key="news.id" @click="goToNews">
          <div class="news-icon">📰</div>
          <div class="news-content">
            <h4>{{ news.title }}</h4>
            <p>{{ news.description }}</p>
          </div>
          <div class="news-meta">
            <span class="news-time">{{ news.createTime }}</span>
            <span class="news-views">👁 {{ news.count || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 活动详情对话框 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="600px">
      <div v-if="currentActivity" class="detail-content">
        <h3>{{ currentActivity.activityName }}</h3>
        <el-divider />
        
        <div class="detail-item">
          <label>活动描述：</label>
          <p>{{ currentActivity.activityDesc || '暂无描述' }}</p>
        </div>
        
        <div class="detail-item">
          <label>活动时间：</label>
          <p>{{ formatTime(currentActivity.activityTime) }}</p>
        </div>
        
        <div class="detail-item">
          <label>招募时间：</label>
          <p>{{ formatTime(currentActivity.recruitStartTime) }}</p>
        </div>
        
        <div class="detail-item">
          <label>所需人数：</label>
          <p>{{ currentActivity.requiredPeople }} 人</p>
        </div>
        
        <div class="detail-item">
          <label>活动状态：</label>
          <el-tag v-if="currentActivity.is_full === 1" type="danger">已满员</el-tag>
          <el-tag v-else-if="currentActivity.isExpired === 1" type="info">已过期</el-tag>
          <el-tag v-else type="success">招募中</el-tag>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          :disabled="currentActivity?.isExpired === 1 || currentActivity?.is_full === 1"
          @click="applyActivity(currentActivity)"
        >
          立即申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const user = ref(JSON.parse(localStorage.getItem('login-user') || '{}'));

// 数据状态
const loading = ref(false);
const newsLoading = ref(false);
const stats = ref({
  myApplications: 0,
  approvedCount: 0,
  availableActivities: 0
});
const activities = ref([]);
const newsList = ref([]);
const detailVisible = ref(false);
const currentActivity = ref(null);

// 颜色渐变数组
// 状态标准化函数 - 统一转换为大写
const normalizeStatus = (status) => {
  if (!status) return 'PENDING'
  return String(status).toUpperCase()
}

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
];

// 获取随机渐变色
const getRandomGradient = () => {
  return gradients[Math.floor(Math.random() * gradients.length)];
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '未知时间';
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 加载统计数据
const loadStats = async () => {
  try {
    // 获取我的申请数量
    const applicationsRes = await request.get('/application/page', {
      params: { userId: user.value.id, pageNum: 1, pageSize: 100 }
    });
    if (applicationsRes.code === '200' && applicationsRes.data) {
      const applications = (applicationsRes.data.list || applicationsRes.data.records || [])
        .map(app => ({
          ...app,
          status: normalizeStatus(app.status)  // 统一转换为大写
        }));
      stats.value.myApplications = applications.length;
      stats.value.approvedCount = applications.filter(app => app.status === 'APPROVED').length;
    }

    // 获取可参与活动数量
    const activitiesRes = await request.get('/volunteer/activity/list', {
      params: { isExpired: 0 }
    });
    if (activitiesRes.code === '200' && activitiesRes.data) {
      stats.value.availableActivities = activitiesRes.data.filter(act => act.is_full !== 1).length;
    }
  } catch (error) {
    console.error('Load stats error:', error);
  }
};

// 加载热门活动（前6个）
const loadActivities = async () => {
  loading.value = true;
  try {
    const res = await request.get('/volunteer/activity/list', {
      params: { isExpired: 0 }
    });
    
    if (res.code === '200') {
      // 只显示前6个活动
      activities.value = (res.data || []).filter(act => act.is_full !== 1).slice(0, 6);
    }
  } catch (error) {
    console.error('Load activities error:', error);
    ElMessage.error('加载活动失败');
  } finally {
    loading.value = false;
  }
};

// 加载最新资讯（前5条）
const loadNews = async () => {
  newsLoading.value = true;
  try {
    const res = await request.get('/news/selectPage', {
      params: { pageNum: 1, pageSize: 5, title: '' }
    });
    
    if (res.code === '200') {
      newsList.value = res.data?.list || res.data?.records || [];
    }
  } catch (error) {
    console.error('Load news error:', error);
  } finally {
    newsLoading.value = false;
  }
};

// 申请参加活动
const applyActivity = async (activity) => {
  if (!activity) return;
  
  detailVisible.value = false;
  
  try {
    const res = await request.post('/application/member/apply', null, {
      params: {
        activityId: activity.id,
        memberId: user.value.id
      }
    });
    
    if (res.code === '200') {
      ElMessage.success('申请成功，请等待审核');
      loadStats(); // 刷新统计数据
    } else {
      ElMessage.error(res.msg || '申请失败');
    }
  } catch (error) {
    console.error('Apply error:', error);
    ElMessage.error('申请失败：' + (error.message || '网络错误'));
  }
};

// 查看活动详情
const viewDetail = (activity) => {
  currentActivity.value = activity;
  detailVisible.value = true;
};

// 导航到活动列表
const goToVolunteerList = () => {
  router.push('/volunteerList');
};

// 导航到资讯页
const goToNews = () => {
  router.push('/news');
};

// 页面加载时初始化数据
onMounted(() => {
  loadStats();
  loadActivities();
  loadNews();
});
</script>

<style scoped>
.tenant-home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.banner-content h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.banner-content p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.banner-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 区块样式 */
.activities-section,
.news-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  color: #1f2329;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 活动卡片网格 */
.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.activity-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  background: white;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 20px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-name {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.card-body {
  padding: 20px;
}

.activity-desc {
  color: #6b7280;
  margin: 0 0 15px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.card-footer {
  padding: 15px 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 10px;
}

/* 资讯列表 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.news-item:hover {
  background-color: #f9fafb;
  border-color: #165dff;
}

.news-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.news-content {
  flex: 1;
  min-width: 0;
}

.news-content h4 {
  margin: 0 0 5px 0;
  color: #1f2329;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-content p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  align-items: flex-end;
  flex-shrink: 0;
}

.news-time,
.news-views {
  font-size: 12px;
  color: #86909c;
}

/* 详情对话框 */
.detail-content {
  padding: 10px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-item label {
  font-weight: bold;
  color: #1f2329;
  margin-bottom: 5px;
  display: block;
}

.detail-item p {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    gap: 20px;
  }
  
  .activities-grid {
    grid-template-columns: 1fr;
  }
  
  .news-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .news-meta {
    align-items: flex-start;
    flex-direction: row;
    gap: 15px;
  }
}
</style>