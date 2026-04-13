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

                <!-- 登录方式切换 Tab -->
                <el-tabs v-model="loginType" class="login-tabs">
                    <!-- ===== 密码登录 Tab ===== -->
                    <el-tab-pane label="密码登录" name="PASSWORD">
                        <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef"
                            @submit.prevent="onSubmitPassword">
                            <el-form-item prop="username">
                                <el-input v-model="pwdForm.username" placeholder="请输入用户名或邮箱"
                                    prefix-icon="User" size="large">
                                </el-input>
                            </el-form-item>

                            <el-form-item prop="password">
                                <el-input v-model="pwdForm.password" type="password" placeholder="请输入密码"
                                    prefix-icon="Lock" size="large" show-password>
                                </el-input>
                            </el-form-item>

                            <div class="remember-me">
                                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                                <a href="#" class="forgot-password">忘记密码?</a>
                            </div>

                            <el-form-item>
                                <el-button type="primary" native-type="submit" :loading="loading"
                                    class="submit-btn" round>
                                    登录账号
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </el-tab-pane>

                    <!-- ===== 验证码登录 Tab ===== -->
                    <el-tab-pane label="邮箱验证码登录" name="CODE">
                        <el-form :model="codeForm" :rules="codeRules" ref="codeFormRef"
                            @submit.prevent="onSubmitCode">
                            <el-form-item prop="email">
                                <el-input v-model="codeForm.email" placeholder="请输入绑定的邮箱"
                                    prefix-icon="Message" size="large">
                                </el-input>
                            </el-form-item>

                            <el-form-item prop="code">
                                <div class="code-row">
                                    <el-input v-model="codeForm.code" placeholder="请输入6位验证码"
                                        prefix-icon="Key" size="large" maxlength="6" style="flex:1">
                                    </el-input>
                                    <el-button class="send-code-btn" size="large" :disabled="codeCooldown > 0"
                                        @click="sendLoginCode" :loading="sendingCode">
                                        {{ codeCooldown > 0 ? `${codeCooldown}s 后重发` : '发送验证码' }}
                                    </el-button>
                                </div>
                            </el-form-item>

                            <el-form-item>
                                <el-button type="primary" native-type="submit" :loading="loading"
                                    class="submit-btn" round>
                                    登录账号
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </el-tab-pane>
                </el-tabs>

                <div class="auth-footer">
                    <span>还没有账号？</span>
                    <router-link to="/register" class="register-link">立即注册</router-link>
                </div>
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
import { sendEmailCode } from '../../api/user'
import { ChatLineRound, Share, Trophy } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const rememberMe = ref(false)
const loginType = ref<'PASSWORD' | 'CODE'>('PASSWORD')

// ---- 密码登录表单 ----
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ username: '', password: '' })
const pwdRules: FormRules = {
    username: [
        { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度应为6至20个字符', trigger: 'blur' }
    ]
}

// ---- 验证码登录表单 ----
const codeFormRef = ref<FormInstance>()
const codeForm = reactive({ email: '', code: '' })
const codeRules: FormRules = {
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    code: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码为6位数字', trigger: 'blur' }
    ]
}

// ---- 发送验证码冷却 ----
const codeCooldown = ref(0)
const sendingCode = ref(false)
let cooldownTimer: ReturnType<typeof setInterval> | null = null

async function sendLoginCode() {
    if (!codeForm.email) {
        ElMessage.warning('请先填写邮箱')
        return
    }
    sendingCode.value = true
    try {
        const res = await sendEmailCode({ email: codeForm.email, codeType: 'LOGIN' })
        if (res.data?.code === 200) {
            ElMessage.success('验证码已发送，请查收邮件')
            startCooldown()
        } else {
            ElMessage.error(res.data?.message || '发送失败，请稍后重试')
        }
    } catch (e: any) {
        ElMessage.error(e?.response?.data?.message || '发送失败，请检查邮箱地址')
    } finally {
        sendingCode.value = false
    }
}

function startCooldown(seconds = 60) {
    codeCooldown.value = seconds
    if (cooldownTimer) clearInterval(cooldownTimer)
    cooldownTimer = setInterval(() => {
        codeCooldown.value--
        if (codeCooldown.value <= 0 && cooldownTimer) {
            clearInterval(cooldownTimer)
            cooldownTimer = null
        }
    }, 1000)
}

// ---- 初始化「记住我」 ----
function checkSavedCredentials() {
    const savedUsername = localStorage.getItem('savedUsername')
    const savedPassword = localStorage.getItem('savedPassword')
    if (savedUsername && savedPassword) {
        pwdForm.username = savedUsername
        pwdForm.password = atob(savedPassword)
        rememberMe.value = true
    }
}
checkSavedCredentials()

// ---- 通用登录完成处理 ----
async function afterLogin() {
    try {
        await auth.fetchCurrentUser()
    } catch (_) { /* 非致命，忽略 */ }
    ElMessage({ message: '登录成功，欢迎回来！', type: 'success', duration: 1500 })
    setTimeout(() => router.push('/'), 800)
}

// ---- 密码登录提交 ----
async function onSubmitPassword() {
    const valid = await pwdFormRef.value?.validate().catch(() => false)
    if (!valid) return
    loading.value = true
    try {
        if (rememberMe.value) {
            localStorage.setItem('savedUsername', pwdForm.username)
            localStorage.setItem('savedPassword', btoa(pwdForm.password))
        } else {
            localStorage.removeItem('savedUsername')
            localStorage.removeItem('savedPassword')
        }
        // 判断输入是否为邮箱格式
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(pwdForm.username)
        await auth.loginAction({
            loginType: 'PASSWORD',
            ...(isEmail
                ? { email: pwdForm.username }
                : { username: pwdForm.username }),
            password: pwdForm.password
        } as any)
        await afterLogin()
    } catch (e: any) {
        ElMessage.error(e.message || '登录失败，请检查账号和密码')
    } finally {
        loading.value = false
    }
}

// ---- 验证码登录提交 ----
async function onSubmitCode() {
    const valid = await codeFormRef.value?.validate().catch(() => false)
    if (!valid) return
    loading.value = true
    try {
        await auth.loginAction({
            loginType: 'CODE',
            email: codeForm.email,
            code: codeForm.code
        } as any)
        await afterLogin()
    } catch (e: any) {
        ElMessage.error(e.message || '验证码登录失败，请重试')
    } finally {
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
    margin-bottom: 20px;
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

.login-tabs {
    margin-bottom: 10px;
}

.code-row {
    display: flex;
    gap: 10px;
    width: 100%;
}

.send-code-btn {
    white-space: nowrap;
    min-width: 120px;
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
        gap: 5px;
    }

    .feature {
        font-size: 0.9rem;
    }

    .auth-header h2 {
        font-size: 1.5rem;
    }

    .auth-header p {
        font-size: 0.9rem;
    }

    .submit-btn {
        height: 45px;
        font-size: 1rem;
    }

    .send-code-btn {
        min-width: 100px;
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
