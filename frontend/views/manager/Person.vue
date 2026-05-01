<template>
  <div>
    <div class="card" style="width: 100%; padding: 40px">
      <el-form :model="data.form" ref="formRef" label-width="100px" label-position="right" style="padding-right: 40px">
        <el-form-item label="普通用户头像">
           <el-upload class="avatar-uploader" action="http://localhost:8080/files/upload" :show-file-list="false" :on-success="handleImgUploadSuccess"><!--成功后调用handleImgUploadSuccess -->
            <img v-if="data.form.photos" :src="data.form.photos" class="avatar"><!--如果有图片路径,则显示图片.form.photos再赋值给src，浏览器会检测到图片路径，发现是一个url,所以又向后端发送请求,获取图片-->
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="普通用户账号" prop="uname">
          <el-input v-model="data.form.uname" autocomplete="off" disabled />
        </el-form-item>
        <el-form-item label="普通用户密码" prop="upass">
          <el-input show-password v-model="data.form.upass" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="普通用户昵称">
          <el-input v-model="data.form.nickname" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="data.form.phone" autocomplete="off"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="update">保 存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {Plus} from "@element-plus/icons-vue"

const data = reactive({
  form: JSON.parse(localStorage.getItem('login-user') || "{}")
})

const handleImgUploadSuccess = (res) => {//将返回的图片路径赋值给form.photos
  data.form.photos = res.data;
}

const update = () => {
  request.put('/member/update', data.form).then(res => {
    if (res.code === '200') {
      localStorage.removeItem('login-user');
      ElMessage.success("操作成功,请重新登录");
      // router.push('/login');
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}
.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>