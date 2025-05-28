<template>
    <div class="login-page">
        <div class="auth-container">
            <div class="auth-welcome">
                <h1>欢迎回到 <span class="highlight">学伴</span></h1>
                <p>校园互助与资源共享平台</p>
                <div class="auth-features">
                    <div class="feature">
                        <el-icon size="24">
                            <ChatLineRound />
                        </el-icon>
                        <span>互助合作</span>
                    </div>
                    <div class="feature">
                        <el-icon size="24">
                            <Share />
                        </el-icon>
                        <span>资源共享</span>
                    </div>
                    <div class="feature">
                        <el-icon size="24">
                            <Trophy />
                        </el-icon>
                        <span>共同成长</span>
                    </div>
                </div>
            </div>

            <el-card class="auth-card login-card">
                <div class="auth-header">
                    <h2>登录账号</h2>
                    <p>快速登录开始使用学伴平台</p>
                </div>

                <el-form :model="form" :rules="rules" ref="loginForm" @submit.prevent="onSubmit">
                    <el-form-item prop="username">
                        <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large">
                        </el-input>
                    </el-form-item>

                    <el-form-item prop="password">
                        <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                            size="large" show-password>
                        </el-input>
                    </el-form-item>

                    <div class="remember-me">
                        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                        <a href="#" class="forgot-password">忘记密码?</a>
                    </div>

                    <el-form-item>
                        <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn" round>
                            登录账号
                        </el-button>
                    </el-form-item>

                    <div class="auth-footer">
                        <span>还没有账号？</span>
                        <router-link to="/register" class="register-link">立即注册</router-link>
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
import { ChatLineRound, Share, Trophy } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()
const loginForm = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
    username: '',
    password: ''
})

const rules: FormRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度应为3至20个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度应为6至20个字符', trigger: 'blur' }
    ]
}

// 检查是否有保存的用户名密码
function checkSavedCredentials() {
    const savedUsername = localStorage.getItem('savedUsername')
    const savedPassword = localStorage.getItem('savedPassword')

    if (savedUsername && savedPassword) {
        form.username = savedUsername
        form.password = atob(savedPassword) // 使用Base64解码密码
        rememberMe.value = true
    }
}

// 初始化时检查保存的凭据
checkSavedCredentials()

async function onSubmit() {
    if (!loginForm.value) return

    try {
        // 表单验证前显示轻微的加载效果
        const valid = await new Promise<boolean>((resolve) => {
            loginForm.value!.validate((isValid: boolean) => {
                if (!isValid) {
                    // 自动聚焦到第一个错误字段
                    const firstErrorInput = document.querySelector('.el-form-item.is-error input')
                    if (firstErrorInput) {
                        (firstErrorInput as HTMLElement).focus()
                    }
                }
                resolve(isValid)
            })
        })

        if (valid) {
            loading.value = true
            try {
                // 保存用户名密码（如果勾选了记住我）
                if (rememberMe.value) {
                    localStorage.setItem('savedUsername', form.username)
                    localStorage.setItem('savedPassword', btoa(form.password)) // 使用Base64编码密码
                } else {
                    // 如果不记住，则清除保存的凭据
                    localStorage.removeItem('savedUsername')
                    localStorage.removeItem('savedPassword')
                }

                await auth.loginAction({
                    username: form.username,
                    password: form.password
                })

                // 登录成功后立即获取完整用户信息
                try {
                    await auth.fetchCurrentUser()
                } catch (fetchError) {
                    console.error('获取用户信息失败，但不影响登录流程', fetchError)
                }

                ElMessage({
                    message: '登录成功，欢迎回来！',
                    type: 'success',
                    duration: 2000,
                    showClose: true,
                    onClose: () => {
                        router.push('/')
                    }
                })

                // 添加短暂延迟以显示成功消息
                setTimeout(() => {
                    router.push('/')
                }, 1000)
            } catch (e: any) {
                ElMessage({
                    message: e.message || '登录失败，请检查用户名和密码',
                    type: 'error',
                    duration: 3000,
                    showClose: true
                })
                // 登录失败时自动聚焦用户名输入框
                const usernameInput = document.querySelector('.login-card input[type="text"]')
                if (usernameInput) {
                    (usernameInput as HTMLElement).focus()
                }
            } finally {
                loading.value = false
            }
        }
    } catch (error) {
        console.error('登录过程出错:', error)
        loading.value = false
    }
}
</script>

<style scoped>
.login-page {
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
    padding: 20px;
}

.auth-container {
    display: flex;
    width: 1000px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.auth-welcome {
    flex: 1;
    background: linear-gradient(135deg, #4a90e2 0%, #50a3a2 100%);
    color: white;
    padding: 60px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
}

.auth-welcome h1 {
    font-size: 2.4rem;
    margin-bottom: 15px;
}

.highlight {
    font-weight: 600;
    color: #ffffff;
}

.auth-features {
    margin-top: 30px;
    display: flex;
    justify-content: space-around;
    width: 100%;
    gap: 20px;
}

.feature {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 1rem;
    transition: transform 0.2s ease;
}

.feature:hover {
    transform: translateY(-2px);
}

.feature .el-icon {
    background: rgba(255, 255, 255, 0.15);
    border-radius: 50%;
    padding: 8px;
    margin-bottom: 10px;
    font-size: 1.2rem;
    transition: background-color 0.2s ease;
}

.feature:hover .el-icon {
    background: rgba(255, 255, 255, 0.25);
}

.auth-card {
    flex: 1;
    border: none;
    border-radius: 0;
    box-shadow: none;
    padding: 40px;
}

.auth-header {
    text-align: center;
    margin-bottom: 35px;
}

.auth-header h2 {
    font-size: 2rem;
    color: #333;
    margin-bottom: 10px;
}

.auth-header p {
    color: #888;
    font-size: 1rem;
}

.remember-me {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}

.forgot-password {
    color: #409eff;
    text-decoration: none;
}

.forgot-password:hover {
    text-decoration: underline;
}

.submit-btn {
    width: 100%;
    height: 48px;
    font-size: 1.1rem;
    letter-spacing: 0.5px;
    transition: all 0.2s ease;
}

.submit-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.auth-footer {
    text-align: center;
    margin-top: 20px;
    color: #888;
    font-size: 0.9rem;
}

.register-link {
    color: #409eff;
    font-weight: 500;
    margin-left: 5px;
}

/* 适配移动设备 */
@media (max-width: 768px) {
    .auth-container {
        flex-direction: column;
        width: 100%;
        max-width: 450px;
    }

    .auth-welcome {
        padding: 40px 30px;
    }

    .auth-welcome h1 {
        font-size: 2rem;
    }

    .auth-card {
        padding: 30px;
    }

    .auth-features {
        margin-top: 20px;
        margin-bottom: 20px;
    }

    .auth-header h2 {
        font-size: 1.5rem;
    }

    .auth-features {
        gap: 5px;
    }

    .feature {
        font-size: 0.9rem;
    }

    .feature .el-icon {
        padding: 5px;
    }

    .auth-header h2 {
        font-size: 1.5rem;
    }

    .auth-header p {
        font-size: 0.9rem;
    }

    :deep(.el-input__inner) {
        height: 45px;
    }

    .submit-btn {
        height: 45px;
        font-size: 1rem;
    }
}

/* 暗色主题适配 */
[data-theme="dark"] .login-page {
    background: linear-gradient(135deg, #1a1a1c 0%, #2d2d30 100%);
}

[data-theme="dark"] .auth-container {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .auth-welcome {
    background: linear-gradient(135deg, #4a90e2 0%, #50a3a2 100%);
}

[data-theme="dark"] .auth-card {
    background-color: #2d2d30;
    color: #e5eaf3;
}

[data-theme="dark"] .auth-header h2 {
    color: #e5eaf3;
}

[data-theme="dark"] .auth-header p {
    color: #a3a6ad;
}

[data-theme="dark"] .el-checkbox__label {
    color: #e5eaf3;
}

[data-theme="dark"] .auth-footer {
    color: #a3a6ad;
}

[data-theme="dark"] .forgot-password,
[data-theme="dark"] .register-link {
    color: #60a9ff;
}
</style>
