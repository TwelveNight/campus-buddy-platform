<template>
    <div class="register-page">
        <div class="auth-container">
            <div class="auth-welcome">
                <h1>加入 <span class="highlight">学伴</span></h1>
                <p>校园互助与资源共享平台</p>
                <div class="auth-features">
                    <div class="feature">
                        <el-icon size="24">
                            <Connection />
                        </el-icon>
                        <span>连接校园资源</span>
                    </div>
                    <div class="feature">
                        <el-icon size="24">
                            <UserFilled />
                        </el-icon>
                        <span>认识志同道合的朋友</span>
                    </div>
                    <div class="feature">
                        <el-icon size="24">
                            <School />
                        </el-icon>
                        <span>共同学习成长</span>
                    </div>
                </div>
            </div>

            <el-card class="auth-card register-card">
                <div class="auth-header">
                    <h2>创建新账号</h2>
                    <p>加入学伴，享受校园互助服务</p>
                </div>

                <el-form :model="form" :rules="rules" ref="registerForm" @submit.prevent="onSubmit">
                    <el-form-item prop="username">
                        <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large">
                        </el-input>
                    </el-form-item>

                    <el-form-item prop="nickname">
                        <el-input v-model="form.nickname" placeholder="请输入昵称" prefix-icon="EditPen" size="large">
                        </el-input>
                    </el-form-item>

                    <el-form-item prop="password">
                        <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                            size="large" show-password>
                        </el-input>
                    </el-form-item>

                    <el-form-item prop="confirmPassword">
                        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" prefix-icon="Key"
                            size="large" show-password>
                        </el-input>
                    </el-form-item>

                    <div class="terms">
                        <el-checkbox v-model="agreeTerms">我已阅读并同意<a href="#">服务条款</a>和<a href="#">隐私政策</a></el-checkbox>
                    </div>

                    <el-form-item>
                        <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn"
                            :disabled="!agreeTerms" round>
                            创建账号
                        </el-button>
                    </el-form-item>

                    <div class="auth-footer">
                        <span>已有账号？</span>
                        <router-link to="/login" class="login-link">立即登录</router-link>
                    </div>
                </el-form>
            </el-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../store/auth'
import {
    User,
    EditPen,
    Lock,
    Key,
    Connection,
    UserFilled,
    School
} from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()
const registerForm = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)

const form = reactive({
    username: '',
    nickname: '',
    password: '',
    confirmPassword: ''
})

const validatePass = (_rule: any, value: string, callback: any) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error('两次输入密码不一致'))
    } else {
        callback()
    }
}

const rules: FormRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度应为3至20个字符', trigger: 'blur' }
    ],
    nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 10, message: '长度应为2至10个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度应为6至20个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' }
    ]
}

async function onSubmit() {
    if (!registerForm.value) return

    await registerForm.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                await auth.registerAction({
                    username: form.username,
                    password: form.password,
                    nickname: form.nickname
                })
                ElMessage.success('注册成功，即将前往登录页')
                setTimeout(() => router.push('/login'), 1500)
            } catch (e: any) {
                ElMessage.error(e.message || '注册失败，请稍后再试')
            } finally {
                loading.value = false
            }
        }
    })
}
</script>

<style scoped>
.register-page {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
}

.auth-container {
    display: flex;
    width: 900px;
    min-height: 600px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    background-color: #fff;
}

.auth-welcome {
    flex: 1;
    padding: 40px;
    background: linear-gradient(135deg, var(--secondary-color) 0%, #3aa67c 100%);
    color: white;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.auth-welcome h1 {
    font-size: 2.4rem;
    margin-bottom: 10px;
    font-weight: 700;
}

.auth-welcome .highlight {
    color: #ffeb3b;
}

.auth-welcome p {
    font-size: 1.2rem;
    opacity: 0.9;
    margin-bottom: 40px;
}

.auth-features {
    display: flex;
    flex-direction: column;
    gap: 20px;
    margin-top: auto;
}

.feature {
    display: flex;
    align-items: center;
    gap: 15px;
    font-size: 1.1rem;
    opacity: 0.9;
}

.auth-card {
    flex: 1;
    border: none;
    box-shadow: none;
    padding: 20px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.auth-card :deep(.el-card__body) {
    padding: 30px;
}

.auth-header {
    text-align: center;
    margin-bottom: 30px;
}

.auth-header h2 {
    font-size: 1.8rem;
    color: var(--text-primary);
    margin-bottom: 10px;
}

.auth-header p {
    color: var(--text-secondary);
    font-size: 0.95rem;
}

.submit-btn {
    width: 100%;
    height: 50px;
    font-size: 1.1rem;
    margin-top: 10px;
}

.terms {
    margin-bottom: 20px;
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.terms a {
    color: var(--primary-color);
    text-decoration: none;
}

.auth-footer {
    text-align: center;
    margin-top: 20px;
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.login-link {
    color: var(--primary-color);
    font-weight: 500;
    margin-left: 5px;
}

/* 适配移动设备 */
@media (max-width: 768px) {
    .auth-container {
        flex-direction: column;
        width: 100%;
    }

    .auth-welcome {
        padding: 30px;
    }
}
</style>
