<template>
    <div class="register-page">
        <el-card class="register-card">
            <template #header>
                <h2>注册</h2>
            </template>
            <el-form :model="form" :rules="rules" ref="registerForm" @submit.prevent="onSubmit">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="form.nickname" placeholder="请输入昵称"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">注册</el-button>
                </el-form-item>
                <div class="text-center">
                    <span>已有账号？</span>
                    <router-link to="/login">去登录</router-link>
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
const registerForm = ref<FormInstance>()
const loading = ref(false)

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
    min-height: calc(100vh - 200px);
}

.register-card {
    width: 400px;
}

.text-center {
    text-align: center;
    margin-top: 15px;
}
</style>
