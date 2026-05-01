import {createRouter, createWebHistory} from 'vue-router'

// 导入拆分后的两个 Home 组件
import HomeAdmin from '@/views/manager/HomeAdmin.vue';
import HomeTenant from '@/views/manager/HomeTenant.vue';
import HomeIssuer from '@/views/manager/HomeIssuer.vue';

//在 Vue Router 中，路由配置里的 path 是从 URL 中域名（或端口）之后的部分开始匹配的，也就是 URL 中 http://域名:端口/ 后面的路径部分
const router = createRouter({//Vue Router 的核心函数，用于创建路由实例（即最终导出的 router 对象），是路由功能的入口
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [//[ /* 路由规则数组 */ ] // 页面映射规则
    {
      path: '/',
      name: 'Manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/home',//这里的 /home 是绝对路径（以 / 开头），表示从网站根路径（http://域名/）开始解析，直接对应路由配置中的 /home 路径。无论它写在哪个父路由下，都会被解析为 http://域名/home。
      children: [
        {
          path: 'home',
          name: 'Home',
          // 先不指定固定 component，通过 beforeEnter 动态设置
          component: () => import('@/views/manager/Home.vue'), // 临时占位，后续会替换
          beforeEnter: (to, from, next) => {
            // 1. 从缓存获取登录用户信息
            const user = JSON.parse(localStorage.getItem('login-user') || '{}');
            // 2. 根据角色切换组件
            if (user.role === 'ADMIN') {
              // 管理员：加载 HomeAdmin 组件
              to.matched[1].components.default = HomeAdmin;
            } 
            else if (user.role === 'TENANT') {
              // 会员：加载 HomeTenant 组件
              to.matched[1].components.default = HomeTenant;
            }
            else if (user.role === 'ISSUER') {
              // 发布人：加载 HomeIssuer 组件
              to.matched[1].components.default = HomeIssuer;
            }
            next(); // 放行路由
          }
        },

        // { path: 'home', name: 'Home', component: () => import('@/views/manager/Home.vue')},//这里的 home 是相对路径（不以 / 开头），它的完整路径是父路由路径 + 子路由路径。在你的配置中，父路由 path: '/'，因此子路由 path: 'home' 的完整路径是 / + home = /home，最终对应 http://域名/home
        //但需要注意的是，这种父子关系并不是由 path: '/' 定义的，而是由 children 数组中的路由规则定义的。
        // 但在解析路由时，Vue Router 会根据当前请求的路径（如 http://域名/home）来匹配路由规则。如果请求路径是 /home，Vue Router 会先查找是否有父路由 path: '/'，加载父路由，再加载子路由 path: 'home'。
        // 值得注意的是，在加载完父路由后，依旧会从完整的路径开始匹配子路由，而不是从父路由路径开始匹配。
        { path: 'member', name: 'Member', component: () => import('@/views/manager/Member.vue')},
        { path: 'news', name: 'News', component: () => import('@/views/manager/News.vue')},
        { path: 'person', name: 'Person', component: () => import('@/views/manager/Person.vue')},
        { path: 'leavemsg', name: 'Leavemsg', component: () => import('@/views/manager/Leavemsg.vue')},
        { path: 'myleavemsg', name: 'Myleavemsg', component: () => import('@/views/manager/Myleavemsg.vue')},
        // 管理员查看志愿活动
        { path: 'voluntterAdmin', name: 'VoluntterAdmin', component: () => import('@/views/manager/VoluntterAdmin.vue')},
        // 发布者志愿活动管理
        { path: 'volunteerActivity', name: 'VolunteerActivity', component: () => import('@/views/manager/VolunteerActivity.vue')},
        // 活动申请管理
        { path: 'applicationManage', name: 'ApplicationManage', component: () => import('@/views/manager/ApplicationManage.vue')},
        // 会员查看活动列表
        { path: 'volunteerList', name: 'VolunteerList', component: () => import('@/views/manager/VolunteerList.vue')},
        // 会员的申请记录
        { path: 'myApplication', name: 'MyApplication', component: () => import('@/views/manager/MyApplication.vue')},
        // AI聊天
        { path: 'aiChat', name: 'AiChat', component: () => import('@/views/manager/AiChat.vue')}
        
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue')
    }
  ]
})


// 设置路由之前进行校验合法性
router.beforeEach(to => {
  // 直接放行的路由 如:登录、注册路由名字直接放开，不进行拦截处理
  if (to.name === 'Login'||to.name === 'Register') {
    return true;
  }
  // 如果没有登录不允许路由到主界面home
  // 统一登录身份认证,  在没有登录的情况不允许进入需要登录身份认证的页面
  // 具体实现方式: 登录成功后，保存token,  在全局前置导航守卫处统一判断
  const userStr = localStorage.getItem('login-user');
  if (userStr && userStr !== 'null' && userStr !== 'undefined') {
    try {
      const user = JSON.parse(userStr);
      // 检查用户对象是否有效（至少要有id或role属性）
      if (user && (user.id || user.role)) {
        return true; // 放行
      }
    } catch (e) {
      console.error('解析用户信息失败', e);
      localStorage.removeItem('login-user');
      return { path: '/login' };
    }
  }
  //如果之前没有登录过那么先去登录
  return { path: '/login' } //重定向到/路由登录界面
})
export default router
//将创建好的路由实例导出，在 Vue 应用的入口文件（如 src/main.js）中通过 app.use(router) 挂载到 Vue 实例上