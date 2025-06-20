<template>
    <div class="application-list">
        <div v-if="applications.length === 0" class="empty-list">
            <el-empty :description="`暂无${statusText}申请`" />
        </div>

        <div v-else>
            <el-table :data="applications" style="width: 100%">
                <!-- 互助任务标题 -->
                <el-table-column value="互助任务标题" min-width="180">
                    <template #default="scope">
                        <div class="helpinfo-column">
                            <template v-if="scope.row.helpInfo">
                                <template v-if="scope.row.helpInfo.title === '加载中...'">
                                    <el-skeleton :rows="1" animated style="width: 80px;" />
                                </template>
                                <template v-else-if="scope.row.helpInfo.title === '无法获取互助任务'">
                                    <span class="no-data">互助任务不存在或已删除</span>
                                </template>
                                <template v-else>
                                    <router-link :to="`/helpinfo/${scope.row.infoId || scope.row.helpInfo.id}`"
                                        class="title-link">
                                        {{ scope.row.helpInfo.title }}
                                    </router-link>
                                </template>
                            </template>
                            <template v-else>
                                <span class="no-data">互助任务不存在或已删除</span>
                            </template>
                        </div>
                    </template>
                </el-table-column>

                <!-- 发布者/申请者 -->
                <el-table-column :label="userColumnLabel" min-width="120">
                    <template #default="scope">
                        <div class="user-info">
                            <el-avatar :size="30" :src="getUserAvatar(scope)"></el-avatar>
                            <router-link :to="`/user/${getUserId(scope)}`" class="user-link">{{ getUserName(scope) }}</router-link>
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
                            <template
                                v-if="props.type === 'received' && scope.row.status?.toUpperCase() === 'PENDING'">
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

    </div>
</template>

<script setup lang="ts">
import { computed, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import {
    acceptApplication,
    rejectApplication
} from '../../api/helpApplication'

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

// 获取用户ID
function getUserId(scope: any) {
    try {
        if (props.type === 'sent') {
            // 获取发布者ID
            if (scope.row.helpInfo?.publisherId) {
                return scope.row.helpInfo.publisherId
            }
        } else {
            // 获取申请者ID
            if (scope.row.applicantId) {
                return scope.row.applicantId
            }
        }
    } catch (error) {
        console.error('获取用户ID时出错:', error)
    }

    return ''
}

// 处理接受申请
async function handleAccept(application: any) {
    console.log('点击了接受', application);
    try {
        // 兼容 helpInfo 字段缺失，优先用 infoId
        const helpInfoId = application.infoId || (application.helpInfo && application.helpInfo.id);
        if (!helpInfoId) {
            throw new Error('互助任务不存在或已被删除')
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
            throw new Error('互助任务不存在或已被删除')
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

.user-link {
    color: #409EFF;
    text-decoration: none;
    font-weight: 500;
    cursor: pointer;
}

.user-link:hover {
    text-decoration: underline;
    color: #66b1ff;
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
