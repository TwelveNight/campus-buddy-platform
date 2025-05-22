<template>
    <div class="application-list">
        <div v-if="applications.length === 0" class="empty-list">
            <el-empty :description="`暂无${statusText}申请`" />
        </div>

        <div v-else>
            <el-table :data="applications" style="width: 100%">
                <!-- 互助信息标题 -->
                <el-table-column label="互助信息标题" min-width="180">
                    <template #default="scope">
                        <div class="helpinfo-column">
                            <template v-if="scope.row.helpInfo">
                                <template v-if="scope.row.helpInfo.title === '加载中...'">
                                    <el-skeleton :rows="1" animated style="width: 80px;" />
                                </template>
                                <template v-else-if="scope.row.helpInfo.title === '无法获取互助信息'">
                                    <span class="no-data">互助信息不存在或已删除</span>
                                </template>
                                <template v-else>
                                    <router-link :to="`/helpinfo/${scope.row.infoId || scope.row.helpInfo.id}`"
                                        class="title-link">
                                        {{ scope.row.helpInfo.title }}
                                    </router-link>
                                </template>
                            </template>
                            <template v-else>
                                <span class="no-data">互助信息不存在或已删除</span>
                            </template>
                        </div>
                    </template>
                </el-table-column>

                <!-- 发布者/申请者 -->
                <el-table-column :label="userColumnLabel" min-width="120">
                    <template #default="scope">
                        <div class="user-info">
                            <el-avatar :size="30" :src="getUserAvatar(scope)"></el-avatar>
                            <span>{{ getUserName(scope) }}</span>
                        </div>
                    </template>
                </el-table-column>

                <!-- 申请消息 -->
                <el-table-column label="申请消息" min-width="220">
                    <template #default="scope">
                        <div class="message-content" v-html="formatMessage(scope.row.message)">
                        </div>
                    </template>
                </el-table-column>

                <!-- 申请状态 -->
                <el-table-column label="状态" width="100">
                    <template #default="scope">
                        <el-tag :type="getApplicationStatusType(scope.row.status)">
                            {{ getApplicationStatusLabel(scope.row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>

                <!-- 申请时间 -->
                <el-table-column label="申请时间" width="180">
                    <template #default="scope">
                        {{ formatDate(scope.row.createdAt) }}
                    </template>
                </el-table-column>

                <!-- 操作 -->
                <el-table-column label="操作" width="160">
                    <template #default="scope">
                        <div class="action-buttons">
                            <template v-if="canReview(scope.row)">
                                <el-button size="small" type="primary" @click="handleReview(scope.row)">去评价</el-button>
                            </template>
                            <template
                                v-else-if="props.type === 'received' && scope.row.status?.toUpperCase() === 'PENDING'">
                                <el-button size="small" type="success" @click="handleAccept(scope.row)">接受</el-button>
                                <el-button size="small" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
                            </template>
                            <template v-else>
                                <span>-</span>
                            </template>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 评价弹窗 -->
        <ReviewDialog v-model:visible="reviewDialogVisible" :helpInfoId="selectedApplication?.infoId || 0"
            :reviewedUserId="getReviewedUserId()" :reviewerUserId="userId" @review-submitted="onReviewSubmitted" />
    </div>
</template>

<script setup lang="ts">
import { computed, defineProps, defineEmits, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import {
    acceptApplication,
    rejectApplication
} from '../api/helpApplication'
import { hasReviewed } from '../api/review'
import ReviewDialog from './ReviewDialog.vue'

const props = defineProps({
    applications: {
        type: Array,
        required: true
    },
    type: {
        type: String,
        required: true,
        validator: (value: string) => ['sent', 'received'].includes(value)
    },
    status: {
        type: String,
        required: true,
        validator: (value: string) => ['pending', 'accepted', 'rejected', 'processing', 'completed'].includes(value)
    }
})

const emit = defineEmits(['refresh'])

// 用户列展示标签
const userColumnLabel = computed(() => {
    return props.type === 'sent' ? '互助发布者' : '申请人'
})

// 状态文本
const statusText = computed(() => {
    const statusMap: Record<string, string> = {
        'pending': '待处理',
        'accepted': '已接受',
        'rejected': '已拒绝',
        'processing': '处理中',
        'completed': '已完成'
    }
    return statusMap[props.status] || props.status
})

// 是否显示操作按钮
const showActions = computed(() => {
    return (props.type === 'received' && (props.status === 'pending' || props.status === 'processing'))
})

// 获取用户头像
function getUserAvatar(scope: any) {
    const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

    try {
        if (props.type === 'sent') {
            // 获取发布者头像
            if (scope.row.helpInfo?.publisherAvatar) {
                return scope.row.helpInfo.publisherAvatar
            } else if (scope.row.helpInfo?.publisher?.avatar) {
                return scope.row.helpInfo.publisher.avatar
            }
        } else {
            // 获取申请者头像
            if (scope.row.applicantAvatar) {
                return scope.row.applicantAvatar
            }
        }
    } catch (error) {
        console.error('获取头像时出错:', error)
    }

    return defaultAvatar
}

// 获取用户名称
function getUserName(scope: any) {
    try {
        if (props.type === 'sent') {
            // 获取发布者名称
            if (scope.row.helpInfo?.publisherNickname) {
                return scope.row.helpInfo.publisherNickname
            } else if (scope.row.helpInfo?.publisher?.nickname) {
                return scope.row.helpInfo.publisher.nickname
            } else if (scope.row.helpInfo?.publisherId) {
                return '加载中...'
            }
        } else {
            // 获取申请者名称
            if (scope.row.applicantNickname) {
                return scope.row.applicantNickname
            } else if (scope.row.applicantId) {
                return '加载中...'
            }
        }
    } catch (error) {
        console.error('获取用户名称时出错:', error)
    }

    return '未知用户'
}

// 处理接受申请
async function handleAccept(application: any) {
    console.log('点击了接受', application);
    try {
        // 兼容 helpInfo 字段缺失，优先用 infoId
        const helpInfoId = application.infoId || (application.helpInfo && application.helpInfo.id);
        if (!helpInfoId) {
            throw new Error('互助信息不存在或已被删除')
        }
        const applicationId = application.applicationId || application.id;
        const res = await acceptApplication(helpInfoId, applicationId);
        if (res.data.code === 200) {
            ElMessage.success('已接受申请')
            emit('refresh')
        }
    } catch (e: any) {
        ElMessage.error(e.message || '操作失败')
        console.error('接受申请失败:', e);
    }
}

// 处理拒绝申请
async function handleReject(application: any) {
    console.log('点击了拒绝', application);
    try {
        // 兼容 helpInfo 字段缺失，优先用 infoId
        const helpInfoId = application.infoId || (application.helpInfo && application.helpInfo.id);
        if (!helpInfoId) {
            throw new Error('互助信息不存在或已被删除')
        }
        const applicationId = application.applicationId || application.id;
        const res = await rejectApplication(helpInfoId, applicationId);
        if (res.data.code === 200) {
            ElMessage.success('已拒绝申请')
            emit('refresh')
        }
    } catch (e: any) {
        ElMessage.error(e.message || '操作失败')
        console.error('拒绝申请失败:', e);
    }
}

// 格式化消息，将消息中的联系方式部分高亮显示
function formatMessage(message: string | undefined | null): string {
    if (!message) return ''

    try {
        // 如果消息中包含联系方式,使用HTML格式化显示
        const contactInfoMatch = message.match(/联系方式[:：](.+)$/m)
        if (contactInfoMatch) {
            const mainMessage = message.replace(/联系方式[:：](.+)$/m, '')
            return mainMessage + '<div class="contact-info">联系方式: ' + contactInfoMatch[1].trim() + '</div>'
        }

        return message.replace(/\n/g, '<br>')
    } catch (error) {
        console.error('格式化消息时出错:', error)
        return String(message) // 确保返回一个字符串
    }
}

// 格式化日期
function formatDate(dateString: string | Date | number) {
    if (!dateString) return ''
    try {
        // 处理数字类型的时间戳（毫秒）
        const date = typeof dateString === 'number' ? new Date(dateString) :
            (typeof dateString === 'string' ? new Date(dateString) : dateString)
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        })
    } catch (error) {
        console.error('日期格式化错误:', error, dateString)
        return String(dateString)
    }
}

// 获取申请状态类型
function getApplicationStatusType(status: string): string {
    const statusTypeMap: Record<string, string> = {
        'PENDING': 'warning',
        'ACCEPTED': 'success',
        'REJECTED': 'danger',
        'PROCESSING': 'info',
        'COMPLETED': 'success',
        'CANCELED': 'info'
    }
    return statusTypeMap[status?.toUpperCase()] || 'default'
}

// 获取申请状态标签
function getApplicationStatusLabel(status: string): string {
    const statusLabelMap: Record<string, string> = {
        'PENDING': '待处理',
        'ACCEPTED': '已接受',
        'REJECTED': '已拒绝',
        'PROCESSING': '处理中',
        'COMPLETED': '已完成',
        'CANCELED': '已取消'
    }
    return statusLabelMap[status?.toUpperCase()] || '未知状态'
}

// 打开评价弹窗
const reviewDialogVisible = ref(false)
const selectedApplication = ref<any>(null)
const authStore = useAuthStore()
const userId = computed(() => authStore.user?.userId || 0)

function openReviewDialog(application: any) {
    selectedApplication.value = application
    reviewDialogVisible.value = true
}

function getReviewedUserId() {
    if (!selectedApplication.value) return 0;
    
    if (props.type === 'sent') {
        // 如果是我发出的申请，评价对象是互助发布者
        const applicantId = selectedApplication.value.applicantId;
        return applicantId || 0;
    } else {
        // 如果是我收到的申请，评价对象是申请人
        const publisherId = selectedApplication.value.helpInfo?.publisherId;
        return publisherId || 0;
    }
}

async function handleReview(application: any) {
    try {
        const res = await hasReviewed(userId.value, application.infoId)
        // 兼容 axios 返回结构，后端直接返回true/false
        const reviewed = (res && typeof res.data === 'boolean') ? res.data : !!res
        if (!reviewed) {
            openReviewDialog(application)
        } else {
            ElMessage.warning('您已评价过该互助')
        }
    } catch (e) {
        ElMessage.error('校验评价状态失败')
    }
}

function onReviewSubmitted() {
    reviewDialogVisible.value = false
    emit('refresh')
}

// 评价按钮显示逻辑：互助已完成，且当前用户为申请人或发布人，且未评价
function canReview(row: any) {
    const statusCompleted = row.status?.toUpperCase() === 'COMPLETED';
    if (!statusCompleted || row.isReviewed) return false;
    // 当前登录用户
    const uid = userId.value;
    // 申请人或发布人都可评价
    return row.applicantId === uid || row.helpInfo?.publisherId === uid;
}
</script>

<style scoped>
.application-list {
    margin-top: 10px;
}

.empty-list {
    padding: 30px 0;
}

.helpinfo-column {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.loading-info {
    padding: 5px 0;
    width: 100%;
}

.title-link {
    color: #409EFF;
    text-decoration: none;
    font-weight: 500;
    cursor: pointer;
    display: inline-block;
    margin-bottom: 4px;
}

.title-link:hover {
    text-decoration: underline;
    color: #66b1ff;
}

.title-text {
    font-weight: 500;
    color: #303133;
}

.helpinfo-meta {
    display: flex;
    gap: 8px;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.message-content {
    white-space: pre-line;
    line-height: 1.5;
    color: #606266;
}

.contact-info {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
}

.action-buttons {
    display: flex;
    gap: 8px;
}

.no-data {
    color: #909399;
    font-style: italic;
}
</style>
