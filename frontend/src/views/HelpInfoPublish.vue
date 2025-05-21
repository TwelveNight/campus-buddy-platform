<template>
    <div class="helpinfo-publish-page">
        <el-card>
            <template #header>
                <h2>发布互助信息</h2>
            </template>

            <el-form :model="form" :rules="rules" ref="formRef" label-position="top" v-loading="loading">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入标题" maxlength="50" show-word-limit></el-input>
                </el-form-item>

                <el-form-item label="类型" prop="type">
                    <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                        <el-option label="课程辅导" value="COURSE_TUTORING"></el-option>
                        <el-option label="技能交换" value="SKILL_EXCHANGE"></el-option>
                        <el-option label="物品借用" value="ITEM_BORROW"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="描述" prop="description">
                    <el-input v-model="form.description" type="textarea" placeholder="请详细描述您的互助需求" :rows="6"
                        maxlength="500" show-word-limit></el-input>
                </el-form-item>

                <el-form-item label="悬赏金额">
                    <el-input-number v-model="form.rewardAmount" :min="0" :precision="2"
                        style="width: 100%"></el-input-number>
                    <div class="form-tips">可选，如需悬赏请输入金额</div>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="onSubmit" :loading="loading" style="width: 100%">发布</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useHelpInfoStore } from '../store/helpinfo'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const helpInfoStore = useHelpInfoStore()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const form = reactive({
    title: '',
    type: 'COURSE_TUTORING',
    description: '',
    rewardAmount: 0
})

// 表单验证规则
const rules: FormRules = {
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 3, max: 50, message: '标题长度应为3至50个字符', trigger: 'blur' }
    ],
    type: [
        { required: true, message: '请选择类型', trigger: 'change' }
    ],
    description: [
        { required: true, message: '请输入描述', trigger: 'blur' },
        { min: 10, max: 500, message: '描述长度应为10至500个字符', trigger: 'blur' }
    ]
}

// 提交表单
async function onSubmit() {
    if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                await helpInfoStore.publishInfo({
                    title: form.title,
                    type: form.type,
                    description: form.description,
                    rewardAmount: form.rewardAmount > 0 ? form.rewardAmount : null
                })

                ElMessage.success('发布成功!')
                router.push('/helpinfo')
            } catch (e: any) {
                ElMessage.error(e.message || '发布失败，请稍后重试')
            } finally {
                loading.value = false
            }
        }
    })
}
</script>

<style scoped>
.helpinfo-publish-page {
    max-width: 600px;
    margin: 30px auto;
    padding: 0 20px;
}

.form-tips {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}
</style>
