<template>
    <div>
        <div class="login-container">
            <h1  class="back_text">我们来过 不止来过</h1>
            <div style="width: 350px" class="login-box">
                <div style="font-weight: bold; font-size: 24px; text-align: center;margin-bottom: 30px;">用 户 注 册</div>
                <el-form :model="data.form" ref="formRef" :rules="rules">
                    <el-form-item prop="username">
                        <el-input prefix-icon="User" type="text" v-model="data.form.uname" placeholder="请输入账号" />
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input prefix-icon="Lock" show-password v-model="data.form.upass" placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item prop="name">
                        <el-input prefix-icon="Lock" v-model="data.form.nickname" placeholder="请输入昵称" />
                    </el-form-item>
                    <el-form-item prop="phone">
                        <el-input prefix-icon="Lock" v-model="data.form.phone" placeholder="请输入手机号" />
                    </el-form-item>
                    <el-form-item prop="role">
                        <el-select style="width: 100%" v-model="data.form.role">
                            <el-option value="TENANT" label="普通用户"></el-option>
                            <el-option value="ISSUER" label="发布人"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-button type="primary" style="width: 100%;" @click="register">注 册</el-button>
                </el-form>
                <div style="margin-top: 30px;text-align: right;">已有账号? 请 <a href="/login">登录</a></div>
            </div>
        </div>
    </div>
</template>

<script setup>

import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import router from "@/router";

const data = reactive({
    form: { role: 'TENANT' }
})

const rules = reactive({
    uname: [
        { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    upass: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
})

const formRef = ref()

const register = () => {
    formRef.value.validate((valid) => {
        if (valid) {
            request.post('/member/register', data.form).then(res => {
                console.log(JSON.stringify(res));
                if (res.code === '200') {
                    ElMessage.success('注册成功，现在去登录！');
                    //location.href = '/home'
                    router.push('/login')// 跳转到主页
                } else {
                    ElMessage.error(res.msg);
                }
            })
        }
    })
}
</script>

<style scoped>
 .back_text {
  position: fixed;
  top: 5%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 10vh;
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