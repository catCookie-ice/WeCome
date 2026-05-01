<template>
  <div>
    <div class="card" style="margin-bottom: 10px;">
      <el-input v-model="data.searchName" :prefix-icon="Search" style="width: 300px; margin-right: 10px"
                placeholder="请输入活动名称"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>
    
    <div class="card">
      <div style="margin-bottom: 10px;">
        <el-button type="primary" @click="handleAdd">新增活动</el-button>
      </div>
      
      <el-table :data="data.tableData" v-loading="data.loading">
        <el-table-column label="序号" type="index" width="70"></el-table-column>
        <el-table-column label="活动名称" prop="activityName" width="180"></el-table-column>
        <el-table-column label="活动描述" prop="activityDesc" show-overflow-tooltip></el-table-column>
        <el-table-column label="活动时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.activityTime) }}
          </template>
        </el-table-column>
        <el-table-column label="招募开始时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.recruitStartTime) }}
          </template>
        </el-table-column>
        <el-table-column label="所需人数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.requiredPeople }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isExpired === 0" type="success">进行中</el-tag>
            <el-tag v-else type="info">已过期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="250">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" size="small" v-if="scope.row.isExpired === 0" 
                       @click="handleExpire(scope.row.id)">设为过期</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div style="margin-top: 10px;">
        <el-pagination 
          background 
          layout="prev, pager, next" 
          @current-change="handleCurrentChange"
          v-model:current-page="data.pageNum" 
          v-model:page-size="data.pageSize" 
          :total="data.total"
        />
      </div>
    </div>
  </div>

  <!-- 新增/编辑对话框 -->
  <el-dialog 
    v-model="data.formVisible" 
    :title="data.form.id ? '编辑活动' : '新增活动'" 
    width="40%"
    destroy-on-close
  >
    <el-form :model="data.form" ref="formRef" :rules="rules" label-width="120px">
      <el-form-item label="活动名称" prop="activityName">
        <el-input v-model="data.form.activityName" placeholder="请输入活动名称" />
      </el-form-item>
      <el-form-item label="活动描述" prop="activityDesc">
        <el-input 
          v-model="data.form.activityDesc" 
          type="textarea" 
          :rows="4"
          placeholder="请输入活动描述" 
        />
      </el-form-item>
      <el-form-item label="活动时间" prop="activityTime">
        <el-date-picker
          v-model="data.form.activityTime"
          type="datetime"
          placeholder="选择活动时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="招募开始时间" prop="recruitStartTime">
        <el-date-picker
          v-model="data.form.recruitStartTime"
          type="datetime"
          placeholder="选择招募开始时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="所需人数" prop="requiredPeople">
        <el-input-number 
          v-model="data.form.requiredPeople" 
          :min="1" 
          :max="1000"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const data = reactive({
  searchName: '',
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  loading: false
})

const formRef = ref()

const rules = reactive({
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityDesc: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  recruitStartTime: [{ required: true, message: '请选择招募开始时间', trigger: 'change' }],
  requiredPeople: [{ required: true, message: '请输入所需人数', trigger: 'blur' }]
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
    // 获取当前登录用户
    const user = JSON.parse(localStorage.getItem('login-user') || '{}')
    
    // 使用发布者专用接口,只查询当前发布者的活动
    const res = await request.get('/volunteer/activity/publisher/list', {
      params: {
        publisherId: user.id,
        pageNum: data.pageNum,
        pageSize: data.pageSize
      }
    })
    if (res.code === '200') {
      data.tableData = res.data.list || []
      data.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载失败', error)
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

// 新增
const handleAdd = () => {
  const user = JSON.parse(localStorage.getItem('login-user') || '{}')
  data.form = {
    id: 0,                  // 传入0让后端使用自增ID
    initiatorId: user.id,
    isExpired: 0,
    requiredPeople: 10
  }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = { ...row }
  data.formVisible = true
}

// 保存
const save = async () => {
  await formRef.value.validate()
  try {
    const user = JSON.parse(localStorage.getItem('login-user') || '{}')
    
    if (data.form.id) {
      // 编辑活动 - 使用发布者专用更新接口
      const res = await request.put('/volunteer/activity/publisher/update', data.form, {
        params: {
          publisherId: user.id,
          isAdmin: false
        }
      })
      if (res.code === '200') {
        ElMessage.success('保存成功')
        data.formVisible = false
        load()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    } else {
      // 新增活动 - 使用原有接口
      const res = await request.post('/volunteer/activity/add', data.form)
      if (res.code === '200') {
        ElMessage.success('保存成功')
        data.formVisible = false
        load()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 设为过期
const handleExpire = async (id) => {
  try {
    await ElMessageBox.confirm('确定将此活动设为过期吗?', '提示', { type: 'warning' })
    const res = await request.put(`/volunteer/activity/update/expired?arg0=${id}&arg1=1`)
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.log('取消操作')
  }
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
</style>
