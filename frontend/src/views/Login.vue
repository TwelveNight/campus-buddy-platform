<template>
    <div class="login-page">
        <div class="auth-container">
            <div class="auth-welcome">
                <h1>欢迎回到 <span class="highlight">学伴</span></h1>
                <p>校园互助与资源共享平台</p>
                <div class="auth-features">
                    <div class="feature">
                        <el-icon size="24"><ChatLineRound /></el-icon>
                        <span>互助合作</span>
                    </div>
                    <div class="feature">
                        <el-icon size="24"><Share /></el-icon>
                        <span>资源共享</span>
                    </div>
                    <div class="feature">
                        <el-icon size="24"><Trophy /></el-icon>
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
                        <el-input 
                            v-model="form.username" 
                            placeholder="请输入用户名"
                            prefix-icon="User"
                            size="large">
                        </el-input>
                    </el-form-item>
                    
                    <el-form-item prop="password">
                        <el-input 
                            v-model="form.password" 
                            type="password" 
                            placeholder="请输入密码"
                            prefix-icon="Lock"
                            size="large"
                            show-password>
                        </el-input>
                    </el-form-item>
                    
                    <div class="remember-me">
                        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                        <a href="#" class="forgot-password">忘记密码?</a>
                    </div>
                    
                    <el-form-item>
                        <el-button 
                            type="primary" 
                            native-type="submit" 
                            :loading="loading" 
                            class="submit-btn"
                            round>
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
import { useAuthStore } from '../store/auth'
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

    await loginForm.value.validate(async (valid) => {
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
                
                ElMessage.success('登录成功')
                router.push('/')
            } catch (e: any) {
                ElMessage.error(e.message || '登录失败，请检查用户名和密码')
            } finally {
                loading.value = false
            }
        }
    })
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
    background-color: #fff;
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

.remember-me {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.forgot-password {
    font-size: 0.9rem;
    color: var(--primary-color);
}

.auth-footer {
    text-align: center;
    margin-top: 20px;
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.register-link {
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
