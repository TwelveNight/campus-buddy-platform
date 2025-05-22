<template>
    <div class="helpinfo-publish-page">
        <el-card>
            <template #header>
                <h2>{{ isEditMode ? '编辑互助信息' : '发布互助信息' }}</h2>
            </template>

            <el-form :model="form" :rules="rules" ref="formRef" label-position="top" v-loading="loading">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入标题" maxlength="50" show-word-limit></el-input>
                </el-form-item>

                <el-form-item label="类型" prop="type">
                    <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                        <el-option label="课程辅导" value="COURSE_TUTORING"></el-option>
                        <el-option label="技能学习" value="SKILL_LEARNING"></el-option>
                        <el-option label="物品借用" value="ITEM_LEND"></el-option>
                        <el-option label="物品交换" value="ITEM_EXCHANGE"></el-option>
                        <el-option label="组队合作" value="TEAM_UP"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="描述" prop="description">
                    <RichEditor v-model="form.description" placeholder="请详细描述您的互助需求" :maxLength="500" />
                </el-form-item>

                <el-form-item label="预期时间" prop="expectedTime">
                    <el-input v-model="form.expectedTime" placeholder="例如：周一至周五18:00-20:00，或具体日期"></el-input>
                </el-form-item>

                <el-form-item label="预期地点" prop="expectedLocation">
                    <el-input v-model="form.expectedLocation" placeholder="例如：图书馆、教学楼、线上会议等"></el-input>
                </el-form-item>

                <el-form-item label="联系方式" prop="contactMethod">
                    <el-input v-model="form.contactMethod" placeholder="请填写您的联系方式，如微信号、QQ、电话等"></el-input>
                </el-form-item>

                <el-form-item label="悬赏金额">
                    <el-input-number v-model="form.rewardAmount" :min="0" :precision="2"
                        style="width: 100%"></el-input-number>
                    <div class="form-tips">可选，如需悬赏请输入金额</div>
                </el-form-item>

                <el-form-item label="上传图片">
                    <ImageUploader v-model:value="form.imageUrlsList" />
                    <div class="form-tips">可选，上传相关图片，最多5张</div>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="onSubmit" :loading="loading" style="width: 100%">
                        {{ isEditMode ? '更新' : '发布' }}
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useHelpInfoStore } from '../store/helpinfo'
import { useAuthStore } from '../store/auth'
import { fetchHelpInfoDetail } from '../api/helpinfo'
import ImageUploader from '../components/ImageUploader.vue'
import RichEditor from '../components/RichEditor.vue'

const router = useRouter()
const route = useRoute()
const helpInfoStore = useHelpInfoStore()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const infoId = computed(() => route.params.id ? Number(route.params.id) : null)
const isEditMode = computed(() => !!infoId.value)

// 表单数据
const form = reactive({
    title: '',
    type: 'COURSE_TUTORING',
    description: '',
    expectedTime: '',
    expectedLocation: '',
    contactMethod: '',
    rewardAmount: 0,
    imageUrls: '',
    imageUrlsList: [] as string[]
})

// 表单验证规则
const rules: FormRules = {
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 2, max: 50, message: '标题长度应为2至50个字符', trigger: 'blur' }
    ],
    type: [
        { required: true, message: '请选择类型', trigger: 'change' }
    ],
    description: [
        { required: true, message: '请输入描述', trigger: 'blur' },
        { min: 10, max: 500, message: '描述长度应为10至500个字符', trigger: 'blur' }
    ],
    expectedTime: [
        { required: true, message: '请输入预期时间', trigger: 'blur' }
    ],
    expectedLocation: [
        { required: true, message: '请输入预期地点', trigger: 'blur' }
    ],
    contactMethod: [
        { required: true, message: '请输入联系方式', trigger: 'blur' }
    ]
}

// 加载数据
onMounted(async () => {
    if (isEditMode.value && infoId.value) {
        loading.value = true
        try {
            // 获取互助信息详情
            const res = await fetchHelpInfoDetail(infoId.value)
            if (res.data.code === 200) {
                const info = res.data.data
                
                // 填充表单数据
                form.title = info.title
                form.type = info.type
                form.description = info.description
                form.expectedTime = info.expectedTime
                form.expectedLocation = info.expectedLocation
                form.contactMethod = info.contactMethod
                form.rewardAmount = info.rewardAmount || 0
                
                // 解析图片URL列表
                if (info.imageUrls) {
                    try {
                        form.imageUrlsList = JSON.parse(info.imageUrls)
                    } catch (e) {
                        console.error('解析图片URL列表失败', e)
                    }
                }
            } else {
                ElMessage.error('加载数据失败')
                router.push('/helpinfo')
            }
        } catch (e: any) {
            ElMessage.error(e.message || '加载数据失败')
            router.push('/helpinfo')
        } finally {
            loading.value = false
        }
    }
})

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
                // 如果有图片URL列表，转换成JSON字符串
                if (form.imageUrlsList && form.imageUrlsList.length > 0) {
                    form.imageUrls = JSON.stringify(form.imageUrlsList);
                }

                const formData = {
                    title: form.title,
                    type: form.type,
                    description: form.description,
                    expectedTime: form.expectedTime,
                    expectedLocation: form.expectedLocation,
                    contactMethod: form.contactMethod,
                    rewardAmount: form.rewardAmount > 0 ? form.rewardAmount : null,
                    imageUrls: form.imageUrls || null
                };

                if (isEditMode.value && infoId.value) {
                    // 编辑现有互助信息
                    await helpInfoStore.updateInfo(infoId.value, formData)
                    ElMessage.success('更新成功!')
                    router.push(`/helpinfo/${infoId.value}`)
                } else {
                    // 发布新互助信息
                    await helpInfoStore.publishInfo(formData)
                    ElMessage.success('发布成功!')
                    router.push('/helpinfo')
                }
            } catch (e: any) {
                const action = isEditMode.value ? '更新' : '发布'
                ElMessage.error(e.message || `${action}失败，请稍后重试`)
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
