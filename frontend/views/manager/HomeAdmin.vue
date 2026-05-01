<template>
  <div class="admin-home">
    <!-- 顶部欢迎信息与统计卡片 -->
    <div class="welcome-bar">
      <h1>欢迎回来，{{ user.username }}</h1>
      <p class="login-info">上次登录时间：{{ lastLoginTime || '首次登录' }}</p>
    </div>

    <!-- 核心数据统计 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">总用户数</div>
        <div class="stat-value">{{ stats.userCount }}</div>
        <div class="stat-trend" :class="{ up: stats.userTrend > 0, down: stats.userTrend < 0 }">
          <i class="el-icon-arrow-up" v-if="stats.userTrend > 0"></i>
          <i class="el-icon-arrow-down" v-if="stats.userTrend < 0"></i>
          <span>{{ Math.abs(stats.userTrend) }}% 较上月</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-title">志愿活动总数</div>
        <div class="stat-value">{{ stats.activityCount }}</div>
        <div class="stat-trend" :class="{ up: stats.activityTrend > 0, down: stats.activityTrend < 0 }">
          <i class="el-icon-arrow-up" v-if="stats.activityTrend > 0"></i>
          <i class="el-icon-arrow-down" v-if="stats.activityTrend < 0"></i>
          <span>{{ Math.abs(stats.activityTrend) }}% 较上月</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-title">待审核申请</div>
        <div class="stat-value">{{ stats.pendingApplications }}</div>
        <div class="stat-trend" :class="{ up: stats.applicationTrend > 0, down: stats.applicationTrend < 0 }">
          <i class="el-icon-arrow-up" v-if="stats.applicationTrend > 0"></i>
          <i class="el-icon-arrow-down" v-if="stats.applicationTrend < 0"></i>
          <span>{{ Math.abs(stats.applicationTrend) }}% 较昨日</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-title">待处理留言</div>
        <div class="stat-value">{{ stats.pendingMessages }}</div>
        <div class="stat-action">
          <a @click="goTo('leavemsg')">立即处理 →</a>
        </div>
      </div>
    </div>

    <!-- 管理功能快捷入口 -->
    <div class="functions-section">
      <h2 class="section-title">管理功能中心</h2>
      <div class="functions-grid">
        <div class="function-card" @click="goTo('member')">
          <i class="el-icon-user"></i>
          <h3>用户管理</h3>
          <p>会员信息维护、权限管理</p>
        </div>
        <div class="function-card" @click="goTo('voluntterAdmin')">
          <i class="el-icon-star-off"></i>
          <h3>志愿活动</h3>
          <p>活动审核、信息管理</p>
        </div>
        <div class="function-card" @click="goTo('news')">
          <i class="el-icon-document"></i>
          <h3>资讯管理</h3>
          <p>发布公告、政策资讯</p>
        </div>
        <div class="function-card" @click="goTo('leavemsg')">
          <i class="el-icon-chat-line-square"></i>
          <h3>留言管理</h3>
          <p>处理会员留言反馈</p>
        </div>
        <div class="function-card" @click="goTo('aiChat')">
          <i class="el-icon-magic-stick"></i>
          <h3>AI助手</h3>
          <p>智能对话、图像生成</p>
        </div>
        <div class="function-card" @click="goTo('person')">
          <i class="el-icon-setting"></i>
          <h3>个人设置</h3>
          <p>修改密码、资料维护</p>
        </div>
      </div>
    </div>

    <!-- 最近动态 -->
    <!-- <div class="recent-activities">
      <h2 class="section-title">最近系统动态</h2>
      <div class="activity-list">
        <div class="activity-item" v-for="(item, index) in activities" :key="index">
          <div class="activity-time">{{ item.time }}</div>
          <div class="activity-content">{{ item.content }}</div>
        </div>
      </div>
    </div> -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
// import { use } from 'react';

// 路由实例
const router = useRouter();

// 登录用户信息
const user = ref(JSON.parse(localStorage.getItem('login-user') || '{}'));

// 上次登录时间（实际项目从接口获取）
const lastLoginTime = ref('2025-10-14 09:30:22');

// 系统统计数据（设置默认值）
const stats = ref({
  userCount: 0,              // 总用户数
  userTrend: 0,              // 用户增长趋势（%）
  activityCount: 0,          // 志愿活动总数
  activityTrend: 0,          // 活动增长趋势（%）
  pendingApplications: 0,    // 待审核申请数
  applicationTrend: 0,       // 申请趋势（%）
  pendingMessages: 0         // 待处理留言
});

// 最近动态
const activities = ref([
  { time: '10:23', content: '发布者「张三」创建了新志愿活动「社区环保行动」' },
  { time: '09:15', content: '会员「李四」申请参加「敬老院关怀活动」' },
  { time: '昨天', content: '系统自动关闭了3个已过期的志愿活动' }
]);

onMounted(() => {
  console.log(user);
});

// 跳转至管理页面
const goTo = (path) => {
  router.push(`/${path}`);
};

// 加载统计数据
onMounted(async () => {
  try {
    // 注意：接口路径是 /admins/dashboard（复数形式）
    const res = await request.get('/admins/dashboard');
    console.log('Dashboard API Response:', res); // 调试日志
    
    // 检查响应格式
    if (!res) {
      console.error('Response is null or undefined');
      ElMessage.warning('服务器响应为空');
      return;
    }
    
    if (res.code === '200' || res.code === 200) {
      // 确保 data 存在
      if (res.data) {
        stats.value = {
          userCount: res.data.userCount || 0,
          userTrend: res.data.userTrend || 0,
          activityCount: res.data.activityCount || 0,
          activityTrend: res.data.activityTrend || 0,
          pendingApplications: res.data.pendingApplications || 0,
          applicationTrend: res.data.applicationTrend || 0,
          pendingMessages: res.data.pendingMessages || 0
        };
        console.log('Dashboard data loaded successfully:', stats.value);
      } else {
        console.error('Response data is empty');
        ElMessage.warning('服务器返回数据为空');
      }
    } else {
      console.error('Failed response code:', res.code, 'Message:', res.msg);
      ElMessage.error(res.msg || '加载数据失败，请刷新重试');
    }
  } catch (error) {
    console.error('Dashboard API Error:', error);
    console.error('Error details:', {
      message: error.message,
      response: error.response,
      request: error.request
    });
    ElMessage.error('网络错误：' + (error.message || '无法获取数据'));
  }
});
</script>

<style scoped>
.admin-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
}

/* 欢迎栏 */
.welcome-bar {
  background-color: white;
  padding: 20px 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.welcome-bar h1 {
  margin: 0 0 10px 0;
  color: #1f2329;
  font-size: 22px;
}

.login-info {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: white;
  padding: 20px;
  box-shadow: rgba(0, 0, 0, 0.4) 0px 2px 4px, rgba(0, 0, 0, 0.3) 0px 7px 13px -3px, rgba(0, 0, 0, 0.2) 0px -3px 0px inset;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
}


.stat-title {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2329;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.up {
  color: #00b42a;
}

.stat-trend.down {
  color: #f53f3f;
}

.stat-action {
  margin-top: 8px;
}

.stat-action a {
  color: #165dff;
  font-size: 13px;
  text-decoration: none;
}

.stat-action a:hover {
  text-decoration: underline;
}

/* 功能区 */
.functions-section {
  margin-bottom: 30px;
}

.section-title {
  color: #1f2329;
  font-size: 18px;
  margin: 0 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.functions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 每排3个功能，更适合管理场景 */
  gap: 20px;
}

.function-card {
  background-color: white;
  border-radius: 8px;
  padding: 25px 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e5e7eb;
}

.function-card:hover {
  border-color: #165dff;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.1);
  transform: translateY(-5px);
}

.function-card i {
  font-size: 36px;
  color: #165dff;
  margin-bottom: 15px;
}

.function-card h3 {
  margin: 0 0 8px 0;
  color: #1f2329;
  font-size: 16px;
}

.function-card p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

/* 最近动态 */
.recent-activities {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.activity-list {
  margin-top: 15px;
}

.activity-item {
  padding: 12px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  color: #86909c;
  font-size: 12px;
  margin-bottom: 5px;
}

.activity-content {
  color: #1f2329;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .functions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .functions-grid {
    grid-template-columns: 1fr;
  }
}
</style>