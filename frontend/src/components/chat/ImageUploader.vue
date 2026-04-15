<template>
    <div class="image-uploader">
        <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleFileChange"
        />
        <el-button type="text" class="upload-trigger" :loading="uploading" @click="triggerFileInput">
            <el-icon><Picture /></el-icon>
        </el-button>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { uploadApi } from '@/api/upload'

// 定义事件
const emit = defineEmits<{
    success: [imageUrl: string]
    error: [error: string]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

// 触发文件选择
const triggerFileInput = () => {
    if (!uploading.value) {
        fileInputRef.value?.click()
    }
}

// 处理文件选择
const handleFileChange = async (event: Event) => {
    const target = event.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file) return

    // 重置 input，允许再次选择同一文件
    target.value = ''

    // 检查文件类型
    if (!file.type.startsWith('image/')) {
        ElMessage.error('只能上传图片文件!')
        return
    }

    // 检查文件大小（限制为20MB）
    if (file.size / 1024 / 1024 > 20) {
        ElMessage.error('图片大小不能超过 20MB!')
        return
    }

    uploading.value = true
    try {
        const res = await uploadApi.uploadImage(file)
        if (res.data.code === 200) {
            ElMessage.success('图片上传成功')
            emit('success', res.data.data)
        } else {
            const msg = res.data.message || '图片上传失败'
            ElMessage.error(msg)
            emit('error', msg)
        }
    } catch (error: any) {
        console.error('图片上传失败:', error)
        let errorMessage = '图片上传失败'
        if (error?.response?.status === 413) {
            errorMessage = '图片文件过大'
        } else if (error?.response?.status === 415) {
            errorMessage = '不支持的图片格式'
        } else if (error?.message) {
            errorMessage = error.message
        }
        ElMessage.error(errorMessage)
        emit('error', errorMessage)
    } finally {
        uploading.value = false
    }
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
