<template>
  <div>
    <div class="login-container">
      <h1  class="back_text">WELCOME</h1>
      <div style="width: 350px" class="login-box">
        <div style="font-weight: bold; font-size: 24px; text-align: center;margin-bottom: 30px;">志愿者招募管理平台
        </div>
        <el-form :model="data.form" ref="formRef" :rules="rules">
          <el-form-item prop="username">
            <el-input  prefix-icon="User" type="text" v-model="data.form.username" placeholder="请输入登录账号" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input prefix-icon="Lock" show-password v-model="data.form.password" placeholder="请输入登录密码"/>
          </el-form-item>
          <el-form-item prop="usercode">
            <el-input prefix-icon="Lock" v-model="data.form.usercode" type="text" placeholder="输入验证码" style="width: 170px"/>
            <img :src="data.url" alt="" style="height: 25px;margin-left: 10px;cursor: pointer;"
                 @click="changeVerify"/>
          </el-form-item>
          <el-form-item prop="role">
            <el-select style="width: 100%" v-model="data.form.role">
              <el-option value="ADMIN" label="管理员"></el-option>
              <el-option value="TENANT" label="普通用户"></el-option>
              <el-option value="ISSUER" label="发布者"></el-option>
            </el-select>
          </el-form-item>
          <el-button type="primary" style="width: 100%;" @click="login">登 录</el-button>
          <div style="margin-top: 30px;text-align: right;">还没有账号? 请 <a href="/register">注册</a></div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";  // 新增 onMounted
// import {reactive, ref} from "vue";
import request from "@/utils/request.js";  //发送异步请求库
import {ElMessage} from "element-plus";   // 弹出提示框架库
import router from "@/router";         //路由配置

const data = reactive({
  form: {role: 'ADMIN'},  //默认管理员
  url: import.meta.env.VITE_BASE_URL + '/createVerify'
})

//表单验证规则
const rules = reactive({
  username: [
    {required: true, message: '请输入登录账号', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}
  ],
  usercode: [{required: true, message: '请输入验证码', trigger: 'blur'}]
})

//提交登录请求验证表单
const formRef = ref();

onMounted(() => {
  changeVerify();  // 调用已有的验证码切换方法，实现初始化刷新
});

//提交登录请求
const login = () => {
  //异步请求,发送Ajax
  formRef.value.validate((valid) => {
    if (valid) {
      //校验验证码是否正确
      request.post('/checkVerify?verify=' + data.form.usercode).then(result => {
        console.log('验证码校验响应:', JSON.stringify(result));
        
        // 检查验证码校验结果
        // 后端可能返回 {code: "200", data: true/false} 或直接返回 boolean
        const verifySuccess = (result && result.code === '200') || result === true;
        
        if (verifySuccess) {
          //验证码校验成功
          if (data.form.role == 'TENANT' || data.form.role == 'ISSUER') {
            //普通用户和发布者使用同一个登录接口
            const roleText = data.form.role == 'TENANT' ? '普通用户' : '发布者';
            request.post('/member/login', {
              "uname": data.form.username,
              "upass": data.form.password,
              "role": data.form.role
            }).then(res => {
              console.log(roleText + '登录响应:', JSON.stringify(res));
              if (res.code === '200' && res.data) {
                // 确保返回的数据不为 null
                const userData = res.data;
                
                // 检查后端返回的 role 是否与用户选择的角色匹配
                if (userData.role && userData.role !== data.form.role) {
                  ElMessage.error(`该账号是${userData.role === 'TENANT' ? '普通用户' : '发布者'}账号,请选择正确的角色登录!`);
                  return;
                }
                
                // 如果后端没返回 role,使用用户选择的角色
                if (!userData.role) {
                  userData.role = data.form.role;
                }
                
                console.log('存储的用户数据:', JSON.stringify(userData));
                
                //向浏览器缓存中存储登录信息
                localStorage.setItem('login-user', JSON.stringify(userData));
                
                // 如果是发布者登录,调用更新数据库接口刷新活动过期状态
                if (userData.role === 'ISSUER') {
                  request.post('/updateDataBase', {
                    "uname": data.form.username,
                    "upass": data.form.password
                  }).then(updateRes => {
                    console.log('数据库更新响应:', JSON.stringify(updateRes));
                  }).catch(updateError => {
                    console.error('数据库更新失败:', updateError);
                    // 即使更新失败也不影响登录流程
                  });
                }
                
                //弹出一个提示框
                ElMessage.success(roleText + '登录成功!');
                
                // 延迟跳转，确保数据已保存
                setTimeout(() => {
                  //路由到主页
                  router.push('/home');
                }, 100);
              } else if (res.code === '200' && !res.data) {
                // 后端返回成功但 data 为 null,说明用户名或密码错误
                ElMessage.error('用户名或密码错误!');
              } else {
                //登录失败弹出错误消息！
                ElMessage.error(res.msg || '登录失败');
              }
            }).catch(error => {
              console.error(roleText + '登录错误:', error);
              ElMessage.error('登录请求失败: ' + (error.message || '网络错误'));
            });
          }
          else {
            //管理员登录
            request.post('/admins/login', {
              "username": data.form.username,
              "password": data.form.password,
              "role": data.form.role
            }).then(res => {
              console.log('管理员登录响应:', JSON.stringify(res));
              if (res.code === '200' && res.data) {
                // 确保返回的数据不为 null
                const userData = res.data;
                if (!userData.role) {
                  userData.role = 'ADMIN'; // 如果后端没返回 role，手动设置
                }
                console.log('存储的用户数据:', JSON.stringify(userData));
                
                //向浏览器缓存中存储登录信息
                localStorage.setItem('login-user', JSON.stringify(userData));
                //弹出一个提示框
                ElMessage.success('管理员登录成功!');
                
                // 延迟跳转，确保数据已保存
                setTimeout(() => {
                  //路由到主页
                  router.push('/home');
                }, 100);
              } else if (res.code === '200' && !res.data) {
                // 后端返回成功但 data 为 null,说明用户名或密码错误
                ElMessage.error('用户名或密码错误!');
              } else {
                //登录失败弹出错误消息！
                ElMessage.error(res.msg || '登录失败');
              }
            }).catch(error => {
              console.error('管理员登录错误:', error);
              ElMessage.error('登录请求失败: ' + (error.message || '网络错误'));
            });
          }
        } else {
          ElMessage.error("验证码输入错误");
        }
      }).catch(error => {
        // 验证码校验请求失败
        console.error('验证码校验错误:', error);
        ElMessage.error('验证码校验失败,请重新输入');
        // 刷新验证码
        changeVerify();
      });
    }
  })
}

//验证码切换
const changeVerify = () => {
  data.url = import.meta.env.VITE_BASE_URL + '/createVerify?' + Math.random()//加上随机数,防止缓存问题
}

</script>

<style scoped>
 .back_text {
  position: fixed;
  top: 25%;
  left: 50%;
  color: #fff;
  transform: translate(-50%, -50VH);
  font-size: 50vh;
  text-shadow: 10px 10px 10px rgb(113, 203, 131);
} 

.login-container {
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3a5e94 0%, #e686dc 100%);
  background-size: cover;
}

.login-box {
  background-color: #fff;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #ddd;
  padding: 30px;
  border-radius: 5px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

</style>
