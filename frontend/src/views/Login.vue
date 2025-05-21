<template>
    <div class="login-page">
        <el-card class="login-card">
            <template #header>
                <h2>登录</h2>
            </template>
            <el-form :model="form" :rules="rules" ref="loginForm" @submit.prevent="onSubmit">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">登录</el-button>
                </el-form-item>
                <div class="text-center">
                    <span>还没有账号？</span>
                    <router-link to="/register">去注册</router-link>
                </div>
            </el-form>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const loginForm = ref<FormInstance>()
const loading = ref(false)

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

async function onSubmit() {
    if (!loginForm.value) return

    await loginForm.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                await auth.loginAction({
                    username: form.username,
                    password: form.password
                })
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
    min-height: calc(100vh - 200px);
}

.login-card {
    width: 400px;
}

.text-center {
    text-align: center;
    margin-top: 15px;
}
</style>
