<template>

    <div>
        <div class="card" style="margin-bottom: 10px;">
            <el-input v-model="data.title" :prefix-icon="Search" style="width: 300px; margin-right: 10px"
                      placeholder="请输入房源名称"></el-input>
            <el-button type="primary" style="margin-left: 10px;" @click="load">查询</el-button>
            <el-button type="info" @click="reset">重置</el-button>
        </div>
        <div class="card">
            <div style="margin-bottom: 10px;" v-if="user.role==='ADMIN'">
                <el-button type="primary" @click="handlerAdd">新增</el-button>
            </div>
            <div style="margin-bottom: 10px;">
                <el-table :data="data.tableData">
                    <el-table-column label="序号" prop="id" width="70"></el-table-column>
                    <el-table-column label="封面">
                        <template #default="scope">
                            <el-image v-if="scope.row.cover" :src="scope.row.cover"
                                      :preview-src-list="[scope.row.cover]"
                                      style="width: 40px;height: 40px;border-radius: 4px;"></el-image>
                        </template>
                    </el-table-column>
                    <el-table-column label="标题" prop="title"></el-table-column>
                    <el-table-column label="简介" prop="description"></el-table-column>
                    <el-table-column label="内容" prop="content">
                        <template #default="scope">
                            <el-button type="primary" class="el-icon-view" @click="view(scope.row.content,scope.row.id)">查看资讯内容
                            </el-button>
                        </template>
                    </el-table-column>
                    <el-table-column label="浏览次数" prop="count"></el-table-column>
                    <el-table-column label="创建时间" prop="createTime"></el-table-column>
                    <el-table-column label="操作" align="center" width="160" v-if="user.role==='ADMIN'">
                        <template #default="scope">
                            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                            <el-button type="danger" size="small" @click="handleDel(scope.row.id)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="card">
                <el-pagination background layout="prev, pager,next" @current-change="handelCurrentChange"
                               v-model:current-page="data.pageNum" v-model:page-size="data.pageSize"
                               :total="data.total"/>
            </div>
        </div>
    </div>

    <el-dialog width="35%" v-model="data.formVisible" title="资讯信息" destory-on-close>
        <el-form :model="data.form" ref="formRef" :rules="rules" label-width="100px" label-position="right"
                 style="padding-right: 40px">
            <el-form-item label="资讯标题">
                <el-input v-model="data.form.title" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="资讯简介">
                <el-input v-model="data.form.description" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="资讯内容">
                <el-input v-model="data.form.content" type="textarea" :rows="10" autocomplete="off"/>
            </el-form-item>
            <el-form-item label="资讯封面">
                <el-upload action="http://localhost:8080/files/upload" list-type="picture"
                           :on-success="handleImgUploadSuccess">
                    <el-button type="primary">上传图片</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="浏览次数">
                <el-input v-model="data.form.count" disabled autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
        </template>
    </el-dialog>

    <!--定义一个资讯详情的提示框-->
    <el-dialog width="60%" v-model="data.contentVisible" title="资讯详情" destory-on-close>
        <div>
            {{data.content}}
        </div>
    </el-dialog>

</template>


<script setup>
    import {reactive, ref} from "vue";
    import {Search} from "@element-plus/icons-vue";
    import request from "@/utils/request.js";
    import {ElMessage} from "element-plus";

    //初始值
    const data = reactive({
        title: '',  //初始搜索条件是''
        tableData: [],
        pageNum: 1, //当前页码
        pageSize: 8, //每页个数
        total: 0,
        formVisible: false, //新增框不显示
        contentVisible: false, //资讯详情框，默认隐藏的
        content: ''
    })

    const user = JSON.parse(localStorage.getItem('login-user') || '{}');

    const formRef = ref();

    const rules = reactive({
        title: [
            {required: true, message: '请输入资讯标题', trigger: 'blur'}
        ],
        description: [
            {required: true, message: '请输入资讯描述', trigger: 'blur'}
        ]
    });

    //打开资讯详情提示框
    const view = (content,id) => {
        //更新资讯的点击数
        request.request({
            url: '/news/updateCount',
            method: 'PUT',
            data: {"id": id}
        }).then(res => {
            if (res.code === '200') {
                data.content = content;
                //打开资讯详情框
                data.contentVisible = true;
                //重新查询
                load();
            } else {
                ElMessage.error(res.msg)
            }
        });
    };

    //请求数据
    const load = () => {
        //发送了一个Get请求，查询房源信息
        request.get("/news/selectPage", {
            params: {
                pageNum: data.pageNum, //当前页码
                pageSize: data.pageSize, //每页大小
                title: data.title, //查询条件
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
        data.title = '';
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
            url: data.form.id ? '/news/update' : '/news/add',
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
            request.delete('/news/delete/' + id).then(res => {
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
        data.form.cover = res.data
    }
</script>