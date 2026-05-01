<template>
  <div>
    <div style="height: 60px; background-color: #fff; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px">WeCome 志愿者招募管理平台</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <img :src="user.photos||'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt=""
             style="width: 30px; height: 30px;border-radius: 50%;">
        <span style="margin-left: 5px" v-if="user.role==='ADMIN'">【管理员】{{ user.username }}</span>
        <span style="margin-left: 5px" v-if="user.role==='TENANT'">【普通用户】{{ user.uname }}</span>
        <span style="margin-left: 5px" v-if="user.role==='ISSUER'">【发布者】{{ user.nickname || user.uname }}</span>
      </div>
    </div>

    <div style="display: flex">
      <div style="width: 200px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px)">
        <el-menu
            router
            style="border: none"
            :default-active="$route.path"
            :default-openeds="['/home', '2']"
        >
          <el-menu-item index="/home">
            <el-icon>
              <HomeFilled/>
            </el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <el-sub-menu index="2" v-if="user.role==='ADMIN'">
            <template #title>
              <el-icon>
                <Avatar/>
              </el-icon>
              <span>普通用户管理</span>
            </template>
            <el-menu-item index="/member">
              <el-icon>
                <User/>
              </el-icon>
              <span>普通用户信息</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="3">
            <template #title>
              <el-icon>
                <ChatLineSquare/>
              </el-icon>
              <span>资讯管理</span>
            </template>
            <el-menu-item index="/news">
              <el-icon>
                <ChatDotSquare/>
              </el-icon>
              <span>资讯信息</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="4" v-if="user.role==='ADMIN'">
            <template #title>
              <el-icon>
                <TrophyBase/>
              </el-icon>
              <span>志愿活动</span>
            </template>
            <el-menu-item index="/voluntterAdmin">
              <el-icon>
                <Management/>
              </el-icon>
              <span>活动管理</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="5" v-if="user.role==='TENANT'">
            <template #title>
              <el-icon>
                <TrophyBase/>
              </el-icon>
              <span>志愿活动</span>
            </template>
            <el-menu-item index="/volunteerList">
              <el-icon>
                <List/>
              </el-icon>
              <span>活动列表</span>
            </el-menu-item>
            <el-menu-item index="/myApplication">
              <el-icon>
                <Document/>
              </el-icon>
              <span>我的申请</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="6" v-if="user.role==='ADMIN'">
            <template #title>
              <el-icon><Comment /></el-icon>
              <span>留言管理</span>
            </template>
            <el-menu-item index="/leavemsg">
              <el-icon><Comment /></el-icon>
              <span>留言信息</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="8" v-if="user.role==='TENANT'">
            <template #title>
              <el-icon><Comment /></el-icon>
              <span>留言信息</span>
            </template>
            <el-menu-item index="/myleavemsg">
              <el-icon><Comment /></el-icon>
              <span>我的留言</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 发布者菜单 -->
          <el-sub-menu index="8" v-if="user.role==='ISSUER'">
            <template #title>
              <el-icon><TrophyBase /></el-icon>
              <span>志愿活动</span>
            </template>
            <el-menu-item index="/volunteerActivity">
              <el-icon><Management /></el-icon>
              <span>活动管理</span>
            </el-menu-item>
            <el-menu-item index="/applicationManage">
              <el-icon><Document /></el-icon>
              <span>申请管理</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/person" v-if="user.role==='TENANT' || user.role==='ISSUER'">
            <el-icon>
              <User/>
            </el-icon>
            <span>个人资料</span>
          </el-menu-item>
          
          <el-menu-item index="/aiChat">
            <el-icon>
              <ChatDotRound/>
            </el-icon>
            <span>AI助手</span>
          </el-menu-item>
          
          <el-menu-item @click="logout">
            <el-icon>
              <SwitchButton/>
            </el-icon>
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div style="flex: 1; width: 0; background-color: #f8f8ff; padding: 10px">
        <router-view/>
      </div>
    </div>

  </div>
</template>

<script setup>
import {useRoute} from 'vue-router'
import router from "@/router";

const $route = useRoute()
console.log($route.path)

const user = JSON.parse(localStorage.getItem('login-user') || '{}')
//alert(JSON.stringify(user));

//退出
const logout = () => {
  ElMessageBox.confirm('确定退出系统吗?', '确认', {type: 'warning'}).then(res => {
    //清空本地登录的token信息
    localStorage.removeItem('login-user');
    // 路由到登录页面
    router.push('/login');
  }).catch(err => {
    console.error(err);
  })
}

</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #dcede9 !important;
}

.el-menu-item:hover {
  color: #11A983;
}

:deep(th) {
  color: #333;
}
</style>