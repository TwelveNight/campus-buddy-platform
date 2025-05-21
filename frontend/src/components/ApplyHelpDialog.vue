<template>
    <el-dialog :model-value="visible" title="申请帮助" width="500px" :close-on-click-modal="false"
        :before-close="handleClose">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="loading">
            <el-form-item label="申请消息" prop="message">
                <el-input v-model="form.message" type="textarea" :rows="4" placeholder="请描述您为何适合提供这个帮助，或您打算如何提供帮助"
                    maxlength="200" show-word-limit></el-input>
            </el-form-item>

            <el-form-item label="联系方式" prop="contactInfo">
                <el-input v-model="form.contactInfo" placeholder="可选，如微信号、QQ、电话等" maxlength="50"
                    show-word-limit></el-input>
                <div class="form-tips">发布者会通过此联系方式与您取得联系</div>
            </el-form-item>
        </el-form>

        <template #footer>
            <span class="dialog-footer">
                <el-button @click="handleClose">取消</el-button>
                <el-button type="primary" @click="submitForm" :loading="loading">提交申请</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue' // Removed defineEmits, defineProps
import type { PropType } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { submitApplication } from '../api/helpApplication'

const props = defineProps({
    helpInfoId: {
        type: Number as PropType<number>,
        required: true
    },
    visible: {
        type: Boolean,
        required: true
    }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const form = reactive({
    message: '',
    contactInfo: ''
})

// 表单验证规则
const rules: FormRules = {
    message: [
        { required: true, message: '请输入申请消息', trigger: 'blur' },
        { min: 10, max: 200, message: '消息长度应为10至200个字符', trigger: 'blur' }
    ]
}

// 提交表单
async function submitForm() {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                // 如果用户提供了联系方式，将它合并到申请消息中
                const messageWithContact = form.contactInfo ?
                    `${form.message}\n\n联系方式: ${form.contactInfo}` :
                    form.message

                await submitApplication(props.helpInfoId, {
                    message: messageWithContact
                })

                ElMessage.success('申请提交成功！')
                emit('success')
                handleClose()
            } catch (e: any) {
                // 显示具体的错误信息
                if (e.message && e.message.includes('不能申请自己')) {
                    ElMessage.error('您不能申请自己发布的互助信息')
                } else if (e.message && e.message.includes('已经申请')) {
                    ElMessage.error('您已经申请过该互助信息')
                } else if (e.message && e.message.includes('状态不允许')) {
                    ElMessage.error('该互助信息当前状态不允许申请')
                } else if (e.message && e.message.includes('请求格式错误')) {
                    ElMessage.error('提交数据格式错误，请检查填写内容后重试')
                    console.error('申请提交格式错误:', e) // 在控制台打印详细错误以便调试
                } else {
                    ElMessage.error(e.message || '申请提交失败，请稍后重试')
                    console.error('申请提交失败:', e) // 在控制台打印详细错误以便调试
                }
            } finally {
                loading.value = false
            }
        }
    })
}

// 关闭对话框
function handleClose() {
    // 重置表单
    form.message = ''
    form.contactInfo = ''
    if (formRef.value) {
        formRef.value.resetFields()
    }
    emit('update:visible', false)
}
</script>

<style scoped>
.form-tips {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}
</style>
