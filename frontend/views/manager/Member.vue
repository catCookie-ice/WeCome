<template>

    <div>
        <div class="card" style="margin-bottom: 10px;">
            <el-input v-model="data.nickname" :prefix-icon="Search" style="width: 300px; margin-right: 10px"
                      placeholder="请输入用户昵称"></el-input>
            <el-button type="primary" style="margin-left: 10px;" @click="load">查询</el-button>
            <el-button type="info" @click="reset">重置</el-button>
        </div>
        <div class="card">
            <div style="margin-bottom: 10px;">
                <el-button type="primary" @click="handlerAdd">新增用户</el-button>
            </div>
            <div style="margin-bottom: 10px;">
                <el-table :data="data.tableData">
                    <el-table-column label="序号" prop="id" width="70"></el-table-column>
                    <el-table-column label="用户头像">
                        <template #default="scope">
                            <el-image v-if="scope.row.photos" :src="scope.row.photos" :preview-src-list="[scope.row.photos]" style="width: 40px;height: 40px;border-radius: 4px;"></el-image>
                        </template>
                    </el-table-column>
                    <el-table-column label="用户账号" prop="uname"></el-table-column>
                    <el-table-column label="用户昵称" prop="nickname"></el-table-column>
                    <el-table-column label="用户手机号" prop="phone"></el-table-column>
                    <el-table-column label="用户注册日期" prop="createtime"></el-table-column>
                    <el-table-column label="操作" align="center" width="160">
                        <template #default="scope">
                            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                            <el-button type="danger" size="small" @click="handleDel(scope.row.id)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="card">
                <el-pagination background layout="prev, pager,next" @current-change="handelCurrentChange"
                               v-model:current-page="data.pageNum" v-model:page-size="data.pageSize" :total="data.total"/>
            </div>
        </div>
    </div>

    <!--模态框-->
    <el-dialog width="35%" v-model="data.formVisible" title="用户信息" destory-on-close>
        <el-form :model="data.form" ref="formRef" :rules="rules" label-width="100px" label-position="right" style="padding-right: 40px">
            <el-form-item label="用户账号">
                <el-input v-model="data.form.uname" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户密码">
                <el-input show-password v-model="data.form.upass" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户头像">
                <el-upload action="http://localhost:8080/files/upload" list-type="picture" :on-success="handleImgUploadSuccess">
                    <el-button type="primary">上传图片</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="用户昵称">
                <el-input v-model="data.form.nickname" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="用户手机号">
                <el-input v-model="data.form.phone" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
        </template>
    </el-dialog>

</template>


<script setup>
    import {reactive, ref} from "vue";
    import {Search} from "@element-plus/icons-vue";
    import request from "@/utils/request.js";
    import {ElMessage} from "element-plus";

    //初始值
    const data = reactive({
        nickname: '',  //初始搜索条件是''
        tableData: [],
        pageNum: 1, //当前页码
        pageSize: 8, //每页个数
        total: 0,
        formVisible: false //新增框不显示
    })


    const formRef = ref()

    const rules = reactive({
        uname: [
            {required: true, message: '用户账号不能为空', trigger: 'blur'}
        ],
        upass: [
            {required: true, message: '用户密码不能为空', trigger: 'blur'}
        ]
    })


    //请求数据
    const load = () => {
        //发送了一个Get请求，查询房源信息
        request.get("/member/selectPage", {
            params: {
                pageNum: data.pageNum, //当前页码
                pageSize: data.pageSize, //每页大小
                nickname: data.nickname, //查询条件
            }
        }).then(res => {
            console.log("分页数据:" + JSON.stringify(res.data));
            data.tableData = res.data?.list || [];
            data.total = res.data?.total || 0;
        })
    };

    //翻页
    const handelCurrentChange = (pageNum) => {
        // 当翻页的时候重新加载数据即可
        load()
    };

    //重置
    const reset = () => {
        data.nickname = '';
        load()
    };

    /**
     * 01-调用方法获取后台数据,视图打开自动调用该方法，加载房源信息
     */
    load();

    //点击新增
    const handlerAdd = () => {
        data.form = {};
        data.formVisible = true; //打开对话框
    };

    // 保存数据到后台
    const save = () => {
        request.request({
            url: data.form.id ? '/member/update' : '/member/add',
            method: data.form.id ? 'PUT' : 'POST',
            data: data.form
        }).then(res => {
            if (res.code === '200') {
                load()    // 重新获取数据
                data.formVisible = false  // 关闭弹窗
                ElMessage.success("操作成功")
            } else {
                ElMessage.error(res.msg)
            }
        })
    }

    //点击编辑
    const handleEdit = (row) => {
        data.form = JSON.parse(JSON.stringify(row));
        data.formVisible = true;
    }

    //删除
    const handleDel = (id) => {
        ElMessageBox.confirm('您确认删除吗？', '删除确认', {type: 'warning'}).then(res => {
            request.delete('/member/delete/' + id).then(res => {
                if (res.code === '200') {
                    load()    // 重新获取数据
                    ElMessage.success("操作成功")
                } else {
                    ElMessage.error(res.msg)
                }
            })
        }).catch(res => {
        })
    }

    //文件上传成功之后，回显图片地址
    const handleImgUploadSuccess = (res) => {
        console.log(res.data);
        data.form.photos = res.data
    }
</script>