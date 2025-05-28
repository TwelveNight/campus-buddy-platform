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
import { useAuthStore } from '../../store/auth'
import {
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
/* 统一暗色模式适配 */
[data-theme="dark"] .register-page {
    background: linear-gradient(135deg, #1a1a1c 0%, #2d2d30 100%);
}

[data-theme="dark"] .auth-container {
    background: rgba(45, 45, 48, 0.95);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
    border-radius: 8px;
}

[data-theme="dark"] .auth-welcome {
    background: linear-gradient(135deg, #4a90e2 0%, #50a3a2 100%);
    color: #ffffff;
    border-radius: 8px 0 0 8px;
}

[data-theme="dark"] .auth-card {
    background: #2d2d30;
    box-shadow: none;
    border-radius: 0 8px 8px 0;
    border: 1px solid #404040;
}

[data-theme="dark"] .auth-header h2,
[data-theme="dark"] .auth-header p {
    color: #e5eaf3;
}

[data-theme="dark"] .feature .el-icon {
    color: #ffffff;
}

[data-theme="dark"] .submit-btn {
    background: linear-gradient(90deg, #4a90e2 0%, #50a3a2 100%);
    color: #fff;
    box-shadow: 0 2px 6px rgba(74, 144, 226, 0.3);
    border: none;
}

[data-theme="dark"] .submit-btn:hover {
    background: linear-gradient(90deg, #50a3a2 0%, #4a90e2 100%);
    box-shadow: 0 4px 12px rgba(80, 163, 162, 0.4);
    transform: translateY(-1px);
}

[data-theme="dark"] .login-link {
    color: #60a9ff;
}

[data-theme="dark"] .el-input__inner,
[data-theme="dark"] .el-input__wrapper {
    background: #404040 !important;
    color: #e5eaf3 !important;
    border-color: #555 !important;
}

[data-theme="dark"] .el-checkbox__input.is-checked .el-checkbox__inner {
    background: #4a90e2;
    border-color: #4a90e2;
}

[data-theme="dark"] .el-checkbox__label {
    color: #e5eaf3;
}

/* 亮色模式优化，保持简洁专业 */
.register-page {
    background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px;
}

.auth-container {
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 4px 12px rgba(74, 144, 226, 0.08);
    border-radius: 8px;
    display: flex;
    width: 1000px;
    overflow: hidden;
}

.auth-welcome {
    background: linear-gradient(135deg, #4a90e2 0%, #50a3a2 100%);
    color: #ffffff;
    border-radius: 8px 0 0 8px;
    flex: 1;
    padding: 60px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
}

.auth-card {
    background: #ffffff;
    box-shadow: none;
    border-radius: 0 8px 8px 0;
    border: 1px solid #e8e8e8;
    flex: 1;
    padding: 40px;
}

.auth-header h2,
.auth-header p {
    color: #333333;
}

.feature .el-icon {
    color: #ffffff;
}

.submit-btn {
    background: linear-gradient(90deg, #4a90e2 0%, #50a3a2 100%);
    color: #fff;
    box-shadow: 0 2px 6px rgba(74, 144, 226, 0.3);
    border: none;
    width: 100%;
    height: 48px;
    font-size: 1.1rem;
    letter-spacing: 0.5px;
    transition: all 0.2s ease;
}

.submit-btn:hover {
    background: linear-gradient(90deg, #50a3a2 0%, #4a90e2 100%);
    box-shadow: 0 4px 12px rgba(80, 163, 162, 0.4);
    transform: translateY(-1px);
}

.login-link {
    color: #4a90e2;
}

/* 其它细节优化，保持简洁专业 */
.auth-header {
    text-align: center;
    margin-bottom: 35px;
}

.auth-header h2 {
    font-size: 1.8rem;
    margin-bottom: 10px;
    font-weight: 600;
}

.auth-header p {
    font-size: 1rem;
    color: #666;
}

.feature {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 0.9rem;
    color: #ffffff;
    transition: transform 0.2s ease;
}

.feature:hover {
    transform: translateY(-2px);
}

.feature .el-icon {
    font-size: 24px;
    margin-bottom: 8px;
    transition: all 0.2s ease;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 50%;
    padding: 8px;
}

.feature:hover .el-icon {
    background: rgba(255, 255, 255, 0.25);
}

.auth-footer {
    text-align: center;
    margin-top: 20px;
    color: #666;
    font-size: 0.9rem;
}

.terms {
    margin-bottom: 20px;
}

.terms a {
    color: #4a90e2;
    text-decoration: none;
}

.terms a:hover {
    text-decoration: underline;
}

@media (max-width: 768px) {
    .auth-container {
        flex-direction: column;
        width: 100%;
        max-width: 450px;
    }
    
    .auth-welcome {
        padding: 40px 30px;
        border-radius: 8px 8px 0 0;
    }
    
    .auth-welcome h1 {
        font-size: 1.8rem;
    }
    
    .auth-card {
        border-radius: 0 0 8px 8px;
        padding: 30px;
    }
    
    .submit-btn {
        height: 44px;
        font-size: 1rem;
    }
}
</style>
