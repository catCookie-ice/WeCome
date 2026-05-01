<template>
    <div>
        <div class="card" style="margin-bottom: 10px;">
            <el-input v-model="data.content" :prefix-icon="Search" style="width: 300px; margin-right: 10px"
                      placeholder="请输入留言关键字"></el-input>
            <el-button type="primary" style="margin-left: 10px;" @click="load">查询</el-button>
            <el-button type="info" @click="reset">重置</el-button>
        </div>
        <div class="card">
            <div style="margin-bottom: 10px;">
                <el-button type="primary" @click="handlerAdd">我要留言</el-button>
            </div>
            <div style="margin-bottom: 10px;">
                <el-table :data="data.tableData">
                    <el-table-column label="序号" prop="id" width="70"></el-table-column>
                    <el-table-column label="留言人账号" prop="memberName"></el-table-column>
                    <el-table-column label="留言内容" prop="content"></el-table-column>
                    <el-table-column label="留言时间" prop="createTime"></el-table-column>
                    <el-table-column label="留言回复" prop="reply"></el-table-column>
                    <el-table-column label="留言回复时间" prop="replyTime"></el-table-column>
                    <el-table-column label="回复人" prop="replyName"></el-table-column>
                    <!--                    <el-table-column label="操作" align="center" width="160">-->
                    <!--                        <template #default="scope">-->
                    <!--                            <el-button type="primary" size="small" @click="handleReply(scope.row)">回复</el-button>-->
                    <!--                            <el-button type="danger" size="small" @click="handleDel(scope.row.id)">删除</el-button>-->
                    <!--                        </template>-->
                    <!--                    </el-table-column>-->
                </el-table>
            </div>
            <div class="card">
                <el-pagination background layout="prev, pager,next" @current-change="handelCurrentChange"
                               v-model:current-page="data.pageNum" v-model:page-size="data.pageSize"
                               :total="data.total"/>
            </div>
        </div>
    </div>

    <el-dialog width="35%" v-model="data.formVisible" title="留言信息" destory-on-close>
        <el-form :model="data.form" ref="formRef" :rules="rules" label-width="100px" label-position="right"
                 style="padding-right: 40px">
            <el-form-item label="留言内容">
                <el-input v-model="data.form.content" type="textarea" :rows="6" autocomplete="off"/>
            </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">留 言</el-button>
      </span>
        </template>
    </el-dialog>

</template>


<script setup>
    import {reactive, ref} from "vue";
    import {Search} from "@element-plus/icons-vue";
    import request from "@/utils/request.js";
    import {ElMessage} from "element-plus";

    //当前登录人的用户对象
    const user = JSON.parse(localStorage.getItem('login-user') || '{}');

    //初始值
    const data = reactive({
        content: '',  //初始搜索条件是''
        tableData: [],
        pageNum: 1, //当前页码
        pageSize: 8, //每页个数
        total: 0,
        formVisible: false
    });

    //点击编辑
    const handleReply = (row) => {
        data.form = JSON.parse(JSON.stringify(row));
        data.formVisible = true;
    };

    //点击新增
    const handlerAdd = () => {
        data.form = {};
        data.formVisible = true; //打开对话框
    };

    //请求数据
    const load = () => {
        //发送了一个Get请求，查询我的留言数据
        request.get("/leavemsg/myleavemsg", {
            params: {
                pageNum: data.pageNum, //当前页码
                pageSize: data.pageSize, //每页大小
                content: data.content, //查询条件
                memberId: user.id
            }
        }).then(res => {
            console.log("分页数据:" + JSON.stringify(res.data));
            data.tableData = res.data?.list || [];
            data.total = res.data?.total || 0;
        })
    };

    // 保存数据到后台
    const save = () => {
        request.request({
            url: '/leavemsg/save/' + user.id,
            method: 'POST',
            data: data.form
        }).then(res => {
            if (res.code === '200') {
                load()    // 重新获取数据
                data.formVisible = false; // 关闭弹窗
                ElMessage.success("操作成功");
            } else {
                ElMessage.error(res.msg)
            }
        })
    };


    //翻页
    const handelCurrentChange = (pageNum) => {
        // 当翻页的时候重新加载数据即可
        load()
    };

    //重置
    const reset = () => {
        data.content = '';
        load()
    };

    /**
     * 01-调用方法获取后台数据,视图打开自动调用该方法，加载房源信息
     */
    load();

</script>