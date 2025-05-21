<template>
    <div class="profile-page">
        <el-card>
            <template #header>
                <h2>个人信息设置</h2>
            </template>

            <el-form :model="form" :rules="rules" ref="formRef" label-position="right" label-width="120px"
                v-loading="loading">
                <el-form-item label="头像" prop="avatarUrl" class="avatar-form-item">
                    <AvatarUploader v-model="form.avatarUrl" :size="100" />
                </el-form-item>

                <el-form-item label="用户名">
                    <el-input v-model="form.username" disabled></el-input>
                    <div class="form-tips">用户名不可修改</div>
                </el-form-item>

                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="form.nickname" placeholder="请输入昵称"></el-input>
                </el-form-item>

                <el-form-item label="信用积分">
                    <el-input v-model="form.creditScore" disabled></el-input>
                    <div class="form-tips">信用积分由系统自动计算</div>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
                    <el-button @click="$router.push('/')">返回</el-button>
                </el-form-item>
            </el-form>

            <div class="change-password-section">
                <h3>修改密码</h3>
                <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="right" label-width="120px">
                    <el-form-item label="当前密码" prop="oldPassword">
                        <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码"
                            show-password></el-input>
                    </el-form-item>

                    <el-form-item label="新密码" prop="newPassword">
                        <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码"
                            show-password></el-input>
                    </el-form-item>

                    <el-form-item label="确认新密码" prop="confirmPassword">
                        <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码"
                            show-password></el-input>
                    </el-form-item>

                    <el-form-item>
                        <el-button type="primary" @click="handleChangePassword" :loading="pwdLoading">修改密码</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../store/auth'
import AvatarUploader from '../components/AvatarUploader.vue'
import { updateUserProfile, changePassword } from '../api/user'

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
            validator: (rule: any, value: string, callback: any) => {
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
                    ElMessage.success('个人信息更新成功')
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
                    ElMessage.success('密码修改成功，请重新登录')
                    // 清空表单
                    pwdForm.oldPassword = ''
                    pwdForm.newPassword = ''
                    pwdForm.confirmPassword = ''
                    // 退出登录
                    authStore.logout()
                    router.push('/login')
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
    max-width: 600px;
    margin: 30px auto;
    padding: 0 20px;
}

.avatar-form-item {
    margin-bottom: 30px;
}

.form-tips {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}

.change-password-section {
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
}

.change-password-section h3 {
    margin-bottom: 20px;
    color: #606266;
}
</style>
