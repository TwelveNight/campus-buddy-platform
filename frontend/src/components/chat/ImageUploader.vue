<template>
    <div class="image-uploader">
        <el-upload
            ref="uploadRef"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleSuccess"
            :on-error="handleError"
            accept="image/*"
            :disabled="uploading"
        >
            <el-button type="text" class="upload-trigger" :loading="uploading">
                <el-icon><Picture /></el-icon>
            </el-button>
        </el-upload>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

// 定义事件
const emit = defineEmits<{
    success: [imageUrl: string]
    error: [error: string]
}>()

const authStore = useAuthStore()
const uploadRef = ref()
const uploading = ref(false)

// 上传配置
const uploadAction = computed(() => {
    return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/upload/image`
})

const uploadHeaders = computed(() => {
    const token = authStore.token
    return token ? { 'Authorization': `Bearer ${token}` } : {}
})

// 上传前验证
const beforeUpload = (file: File) => {
    // 检查文件类型
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
    }

    // 检查文件大小（限制为5MB）
    const isLt5M = file.size / 1024 / 1024 < 5
    if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
    }

    uploading.value = true
    return true
}

// 上传成功
const handleSuccess = (response: any) => {
    uploading.value = false
    
    if (response.code === 200) {
        ElMessage.success('图片上传成功')
        emit('success', response.data)
    } else {
        ElMessage.error(response.message || '图片上传失败')
        emit('error', response.message || '图片上传失败')
    }
}

// 上传失败
const handleError = (error: any) => {
    uploading.value = false
    console.error('图片上传失败:', error)
    
    let errorMessage = '图片上传失败'
    if (error.status === 413) {
        errorMessage = '图片文件过大'
    } else if (error.status === 415) {
        errorMessage = '不支持的图片格式'
    } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message
    }
    
    ElMessage.error(errorMessage)
    emit('error', errorMessage)
}
</script>

<style scoped>
.image-uploader {
    display: inline-block;
}

.upload-trigger {
    font-size: 18px;
    color: var(--text-secondary, #909399);
    padding: 8px;
    margin: 0;
}

.upload-trigger:hover {
    color: var(--primary-color, #409eff);
}

.upload-trigger:disabled {
    color: var(--text-placeholder, #c0c4cc);
    cursor: not-allowed;
}

/* 暗色主题适配 */
[data-theme="dark"] .upload-trigger {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .upload-trigger:hover {
    color: var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] .upload-trigger:disabled {
    color: var(--dark-text-placeholder, #8d9095);
}
</style>
