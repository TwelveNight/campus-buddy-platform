<template>
    <div class="image-uploader">
        <el-upload v-model:file-list="fileList" :action="uploadUrl" list-type="picture-card" :limit="maxCount"
            :on-preview="handlePreview" :on-remove="handleRemove" :on-success="handleSuccess" :on-error="handleError"
            :before-upload="beforeUpload" :headers="headers" multiple>
            <el-icon>
                <Plus />
            </el-icon>
            <template #tip>
                <div class="upload-tip">
                    {{ tip || `点击上传图片，${maxSize}MB以内，最多${maxCount}张` }}
                </div>
            </template>
        </el-upload>

        <el-dialog v-model="dialogVisible">
            <img w-full :src="dialogImageUrl" alt="Preview Image" />
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/auth'
import { uploadApi } from '../../api/upload'

// 定义组件的属性
const props = defineProps({
    value: {
        type: Array,
        default: () => []
    },
    uploadUrl: {
        type: String,
        default: '/api/upload/image' // 默认上传地址
    },
    maxSize: {
        type: Number,
        default: 20 // 默认最大20MB
    },
    maxCount: {
        type: Number,
        default: 5 // 默认最多5张
    },
    tip: {
        type: String,
        default: ''
    },
    acceptTypes: {
        type: Array,
        default: () => ['image/jpeg', 'image/png', 'image/gif']
    },
    useDirectUpload: {
        type: Boolean,
        default: false // 是否使用自定义上传方法而不是action
    }
})

// 定义组件的事件
const emit = defineEmits(['update:value', 'upload-success', 'upload-error', 'remove'])

// 使用身份验证存储
const authStore = useAuthStore()

// 上传的文件列表
const fileList = ref<any[]>([])

// 图片预览相关状态
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

// 上传请求头
const headers = computed(() => {
    return {
        Authorization: `Bearer ${authStore.token}`
    }
})

// 初始化文件列表
watch(() => props.value, (newVal) => {
    if (newVal && Array.isArray(newVal)) {
        fileList.value = newVal.map((url, index) => ({
            name: `图片 ${index + 1}`,
            url
        }))
    } else {
        fileList.value = []
    }
}, { immediate: true })

// 预览图片
const handlePreview = (file: any) => {
    dialogImageUrl.value = file.url
    dialogVisible.value = true
}

// 移除图片
const handleRemove = (file: any, fileList: any[]) => {
    const urls = fileList.map(item => item.url || item.response?.data)
    emit('update:value', urls)
    emit('remove', file, fileList)
}

// 自定义上传方法
const handleCustomUpload = async (file: File) => {
    try {
        const response = await uploadApi.uploadImage(file)
        const dataObj = response && typeof response.data === 'object' ? response.data as any : null
        if (dataObj && typeof dataObj.code === 'number' && dataObj.code === 200) {
            handleSuccess({ data: dataObj }, file, fileList.value)
        } else {
            handleError(new Error((dataObj && dataObj.message) || '上传失败'), file)
        }
    } catch (error: any) {
        handleError(error, file)
    }
}

// 上传成功
const handleSuccess = (response: any, file: any, fileList: any[]) => {
    let dataObj = response && response.data ? response.data : response
    if (dataObj && dataObj.code === 200) {
        const imageUrl = dataObj.data
        file.url = imageUrl // 确保图片能回显
        // 单图上传时直接 emit 字符串，否则 emit 数组
        if (props.maxCount === 1) {
            emit('update:value', imageUrl)
        } else {
            const urls = fileList.map(item => item.url || (item.response && item.response.data && item.response.data.data))
            emit('update:value', urls)
        }
        emit('upload-success', imageUrl, file, fileList)
    } else {
        ElMessage.error((dataObj && dataObj.message) || '上传失败')
        emit('upload-error', dataObj, file)
    }
}

// 上传失败
const handleError = (error: any, file: any) => {
    ElMessage.error('上传失败，请稍后重试')
    emit('upload-error', error, file)
}

// 上传前检查
const beforeUpload = (file: any) => {
    // 检查文件类型
    if (props.acceptTypes.length > 0 && !props.acceptTypes.includes(file.type)) {
        ElMessage.error(`只能上传 ${props.acceptTypes.join(', ')} 格式的文件`)
        return false
    }

    // 检查文件大小
    const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
    if (!isLtMaxSize) {
        ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
        return false
    }

    // 如果使用自定义上传
    if (props.useDirectUpload) {
        handleCustomUpload(file)
        return false // 阻止默认上传
    }

    return true
}
</script>

<style scoped>
.image-uploader {
    margin-bottom: 16px;
}

.upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
}
</style>
