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
import { User, Lock, ChatLineRound, Share, Trophy } from '@element-plus/icons-vue'

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
    min-height: 550px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    background-color: var(--card-bg);
    transition: all 0.3s ease;
}

.auth-welcome {
    flex: 1;
    padding: 40px;
    background: linear-gradient(135deg, var(--primary-color) 0%, #2c65dd 100%);
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
}

.feature {
    display: flex;
    align-items: center;
    gap: 15px;
    font-size: 1.1rem;
}

.feature .el-icon {
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    padding: 8px;
}

.auth-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 20px;
    border: none;
    box-shadow: none;
}

.auth-header {
    text-align: center;
    margin-bottom: 30px;
}

.auth-header h2 {
    font-size: 1.8rem;
    margin-bottom: 10px;
    color: var(--text-primary);
}

.auth-header p {
    color: var(--text-secondary);
    font-size: 1rem;
}

:deep(.el-input__wrapper) {
    border-radius: 8px;
}

:deep(.el-input__inner) {
    height: 50px;
}

:deep(.el-form-item) {
    margin-bottom: 20px;
}

.remember-me {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    color: var(--text-regular);
}

.forgot-password {
    color: var(--primary-color);
    text-decoration: none;
    font-size: 0.9rem;
    transition: color 0.3s;
}

.forgot-password:hover {
    text-decoration: underline;
}

.submit-btn {
    width: 100%;
    height: 50px;
    font-size: 1.1rem;
    font-weight: 500;
    transition: all 0.3s ease;
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.auth-footer {
    text-align: center;
    margin-top: 20px;
    color: var(--text-regular);
}

.register-link {
    color: var(--primary-color);
    text-decoration: none;
    font-weight: 500;
    margin-left: 5px;
    transition: color 0.3s;
}

.register-link:hover {
    text-decoration: underline;
}

/* 响应式布局 */
@media (max-width: 992px) {
    .auth-container {
        width: 700px;
    }
    
    .auth-welcome h1 {
        font-size: 2rem;
    }
    
    .auth-welcome p {
        font-size: 1.1rem;
    }
}

@media (max-width: 768px) {
    .auth-container {
        flex-direction: column;
        width: 100%;
        max-width: 500px;
    }
    
    .auth-welcome {
        padding: 30px;
    }
    
    .auth-features {
        flex-direction: row;
        justify-content: space-around;
        margin-bottom: 10px;
    }
    
    .feature {
        flex-direction: column;
        text-align: center;
        gap: 10px;
    }
}

@media (max-width: 480px) {
    .login-page {
        padding: 10px;
    }
    
    .auth-welcome {
        padding: 20px;
    }
    
    .auth-welcome h1 {
        font-size: 1.5rem;
    }
    
    .auth-welcome p {
        font-size: 1rem;
        margin-bottom: 20px;
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
.dark-theme .login-page {
    background: linear-gradient(135deg, #1e1e20 0%, #2a2a2e 100%);
}
</style>
