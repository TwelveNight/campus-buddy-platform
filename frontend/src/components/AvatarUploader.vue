<template>
    <div class="avatar-uploader">
        <el-upload class="avatar-uploader-box" :show-file-list="false" :action="uploadUrl" :on-success="handleSuccess"
            :on-error="handleError" :before-upload="beforeUpload" :headers="headers">
            <el-avatar v-if="modelValue" :size="size" :src="modelValue" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon">
                <Plus />
            </el-icon>
        </el-upload>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    size: {
        type: Number,
        default: 100
    },
    uploadUrl: {
        type: String,
        default: '/api/upload/avatar'
    }
})

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error'])

// 使用身份验证存储
const authStore = useAuthStore()

// 上传请求头
const headers = computed(() => {
    return {
        Authorization: `Bearer ${authStore.token}`
    }
})

// 上传成功
const handleSuccess = (response: any) => {
    if (response.code === 200) {
        const avatarUrl = response.data
        emit('update:modelValue', avatarUrl)
        emit('upload-success', avatarUrl)
        ElMessage.success('头像上传成功')
    } else {
        ElMessage.error(response.message || '头像上传失败')
        emit('upload-error', response)
    }
}

// 上传失败
const handleError = (error: any) => {
    ElMessage.error('头像上传失败，请稍后重试')
    emit('upload-error', error)
}

// 上传前检查
const beforeUpload = (file: any) => {
    // 检查文件类型
    const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
    if (!isImage) {
        ElMessage.error('只能上传图片文件！')
        return false
    }

    // 检查文件大小 (最大 2MB)
    const isLt2M = file.size / 1024 / 1024 < 2
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return false
    }

    return true
}
</script>

<style scoped>
.avatar-uploader-box {
    text-align: center;
    cursor: pointer;
}

.avatar-uploader-box .avatar {
    display: block;
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    line-height: 100px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
}

.avatar-uploader-icon:hover {
    border-color: #409EFF;
}
</style>
