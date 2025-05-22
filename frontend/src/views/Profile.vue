<template>
    <div class="profile-page">
        <div class="profile-header">
            <h1>个人中心<span>完善您的个人信息，获得更好的校园互助体验</span></h1>
        </div>

        <el-row :gutter="24" class="profile-row">
            <!-- 左侧个人概览区域 -->
            <el-col :xs="24" :md="8" class="profile-sidebar">
                <el-card class="profile-card summary-card" shadow="hover">
                    <div class="user-summary">
                        <div class="avatar-container">
                            <AvatarUploader v-model="form.avatarUrl" :size="120" />
                        </div>
                        <h2>{{ form.nickname || form.username }}</h2>
                        <div class="user-credit">
                            <el-progress type="dashboard" :percentage="getCreditPercentage()" :color="creditColors"
                                :stroke-width="8">
                                <template #default>
                                    <div class="credit-label">
                                        <span class="credit-value">{{ form.creditScore }}</span>
                                        <span class="credit-title">信用积分</span>
                                    </div>
                                </template>
                            </el-progress>
                            <div class="credit-level">{{ getCreditLevel() }}</div>
                        </div>
                        <div class="user-stats">
                            <div class="stat-item">
                                <el-icon>
                                    <Medal />
                                </el-icon>
                                <span>注册用户</span>
                            </div>
                            <div class="stat-item">
                                <el-icon>
                                    <Calendar />
                                </el-icon>
                                <span>活跃用户</span>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧设置区域 -->
            <el-col :xs="24" :md="16" class="profile-content">
                <!-- 个人资料卡片 -->
                <el-card class="profile-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <el-icon>
                                <User />
                            </el-icon>
                            <h2>个人资料设置</h2>
                        </div>
                    </template>

                    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" v-loading="loading">
                        <el-form-item label="用户名">
                            <el-input v-model="form.username" disabled>
                                <template #prefix>
                                    <el-icon>
                                        <Avatar />
                                    </el-icon>
                                </template>
                            </el-input>
                            <div class="form-tips">用户名不可修改</div>
                        </el-form-item>

                        <el-form-item label="昵称" prop="nickname">
                            <el-input v-model="form.nickname" placeholder="请输入昵称">
                                <template #prefix>
                                    <el-icon>
                                        <UserFilled />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" @click="handleSubmit" :loading="loading" round>
                                <el-icon>
                                    <Check />
                                </el-icon>保存修改
                            </el-button>
                            <el-button @click="$router.push('/')" round>
                                <el-icon>
                                    <Back />
                                </el-icon>返回
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-card>

                <!-- 修改密码卡片 -->
                <el-card class="profile-card password-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <el-icon>
                                <Lock />
                            </el-icon>
                            <h2>修改密码</h2>
                        </div>
                    </template>

                    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="top">
                        <el-form-item label="当前密码" prop="oldPassword">
                            <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password>
                                <template #prefix>
                                    <el-icon>
                                        <Key />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item label="新密码" prop="newPassword">
                            <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password>
                                <template #prefix>
                                    <el-icon>
                                        <Lock />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item label="确认新密码" prop="confirmPassword">
                            <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码"
                                show-password>
                                <template #prefix>
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" @click="handleChangePassword" :loading="pwdLoading" round>
                                <el-icon>
                                    <RefreshRight />
                                </el-icon>修改密码
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useAuthStore } from '../store/auth'
import AvatarUploader from '../components/AvatarUploader.vue'
import { updateUserProfile, changePassword } from '../api/user'
import {
    User, UserFilled, Avatar, Key, Lock, Check,
    RefreshRight, Back, Medal, Calendar
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const loading = ref(false)
const pwdLoading = ref(false)

// 个人信息表单数据
const form = reactive({
    userId: 0,
    username: '',
    nickname: '',
    avatarUrl: '',
    creditScore: 0
})

// 修改密码表单数据
const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

// 积分相关的颜色渐变配置
const creditColors = [
    { color: '#f56c6c', percentage: 20 },
    { color: '#e6a23c', percentage: 40 },
    { color: '#5cb87a', percentage: 60 },
    { color: '#1989fa', percentage: 80 },
    { color: '#6f7ad3', percentage: 100 }
]

// 计算信用积分的百分比（假设最高分为100）
function getCreditPercentage() {
    const score = form.creditScore || 0
    // 假设最高分为100
    return Math.min(100, Math.max(0, score))
}

// 根据积分返回相应的信用等级
function getCreditLevel() {
    const score = form.creditScore || 0
    if (score >= 90) return '信用优秀'
    if (score >= 80) return '信用良好'
    if (score >= 70) return '信用标准'
    if (score >= 60) return '信用一般'
    return '信用待提升'
}

// 个人信息表单验证规则
const rules = {
    nickname: [
        { required: true, message: '昵称不能为空', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度应为2到20个字符', trigger: 'blur' }
    ]
}

// 修改密码表单验证规则
const pwdRules = {
    oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
            validator: (_: any, value: string, callback: any) => {
                if (value !== pwdForm.newPassword) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}

onMounted(async () => {
    if (!authStore.user) {
        try {
            await authStore.fetchCurrentUser()
        } catch (error) {
            ElMessage.error('获取用户信息失败，请重新登录')
            router.push('/login')
            return
        }
    }

    if (authStore.user) {
        form.userId = authStore.user.userId || 0
        form.username = authStore.user.username
        form.nickname = authStore.user.nickname
        form.avatarUrl = authStore.user.avatarUrl || ''
        form.creditScore = authStore.user.creditScore || 0
    }
})

// 保存个人信息
async function handleSubmit() {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                const res = await updateUserProfile({
                    nickname: form.nickname,
                    avatarUrl: form.avatarUrl
                })

                if (res.data.code === 200) {
                    ElMessage({
                        message: '个人信息更新成功',
                        type: 'success',
                        duration: 2000
                    })

                    // 更新存储中的用户信息
                    if (authStore.user) {
                        authStore.user.nickname = form.nickname
                        authStore.user.avatarUrl = form.avatarUrl
                    }
                } else {
                    ElMessage.error(res.data.message || '更新失败')
                }
            } catch (e: any) {
                ElMessage.error(e.message || '更新失败，请稍后重试')
            } finally {
                loading.value = false
            }
        }
    })
}

// 修改密码
async function handleChangePassword() {
    if (!pwdFormRef.value) return

    await pwdFormRef.value.validate(async (valid) => {
        if (valid) {
            pwdLoading.value = true
            try {
                const res = await changePassword({
                    oldPassword: pwdForm.oldPassword,
                    newPassword: pwdForm.newPassword
                })

                if (res.data.code === 200) {
                    ElMessage({
                        message: '密码修改成功，请重新登录',
                        type: 'success',
                        duration: 2000
                    })

                    // 清空表单
                    pwdForm.oldPassword = ''
                    pwdForm.newPassword = ''
                    pwdForm.confirmPassword = ''

                    // 短暂延迟后退出登录
                    setTimeout(() => {
                        authStore.logout()
                        router.push('/login')
                    }, 1500)
                } else {
                    ElMessage.error(res.data.message || '密码修改失败')
                }
            } catch (e: any) {
                ElMessage.error(e.message || '密码修改失败，请稍后重试')
            } finally {
                pwdLoading.value = false
            }
        }
    })
}
</script>

<style scoped>
.profile-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px 60px;
}

.profile-header {
    text-align: center;
    margin: 30px 0 40px;
}

.profile-header h1 {
    font-size: 2.2rem;
    color: var(--text-primary);
    font-weight: 700;
}

.profile-header h1 span {
    display: block;
    font-size: 1rem;
    color: var(--text-secondary);
    font-weight: 400;
    margin-top: 8px;
}

.profile-row {
    margin-bottom: 40px;
}

.profile-card {
    border-radius: 16px !important;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08) !important;
    transition: transform 0.3s, box-shadow 0.3s;
    overflow: visible;
    margin-bottom: 24px;
}

.profile-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-bottom: 16px;
}

.card-header :deep(svg) {
    font-size: 20px;
    color: var(--primary-color);
}

.card-header h2 {
    margin: 0;
    font-size: 1.4rem;
    color: var(--text-primary);
}

/* 用户概要信息卡片 */
.summary-card {
    height: 100%;
    min-height: 450px;
}

.user-summary {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
}

.avatar-container {
    margin-bottom: 24px;
    position: relative;
}

.user-summary h2 {
    font-size: 1.5rem;
    margin: 0 0 24px;
    color: var(--text-primary);
}

.user-credit {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 24px;
}

.credit-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    line-height: 1.2;
}

.credit-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--primary-color);
}

.credit-title {
    font-size: 12px;
    color: var(--text-secondary);
}

.credit-level {
    margin-top: 8px;
    font-size: 16px;
    color: var(--text-primary);
    font-weight: 500;
}

.user-stats {
    width: 100%;
    display: flex;
    justify-content: space-around;
    margin-top: 16px;
    padding: 16px 0;
    border-top: 1px dashed #eee;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.stat-item :deep(svg) {
    font-size: 24px;
    color: var(--accent-color);
}

.stat-item span {
    font-size: 14px;
    color: var(--text-secondary);
}

/* 表单样式 */
.form-tips {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 5px;
    opacity: 0.7;
}

/* 密码卡片间距 */
.password-card {
    margin-top: 32px;
}

:deep(.el-input-group__prepend) {
    background-color: transparent;
}

:deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--text-primary);
}

:deep(.el-button) {
    transition: all 0.3s;
}

:deep(.el-input__inner) {
    border-radius: 8px;
}

:deep(.el-form-item__error) {
    margin-top: 4px;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .profile-header h1 {
        font-size: 1.8rem;
    }

    .profile-content {
        margin-top: 24px;
    }

    .user-summary {
        padding: 10px 0;
    }
}
</style>
