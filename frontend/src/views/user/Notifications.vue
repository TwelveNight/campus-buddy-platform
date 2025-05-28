<template>
    <div class="notification-page">
        <!-- <div class="notification-header-bar">
            <el-button v-if="isAdmin" type="primary" @click="showAnnouncementDialog = true">发布公告</el-button>
        </div> -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部通知" name="all">
                <NotificationCenter :key="'all'" :title="'全部通知'" :type="'all'" />
            </el-tab-pane>
            <el-tab-pane label="系统通知" name="system">
                <NotificationCenter :key="'system'" :title="'系统通知'" :type="'SYSTEM_ANNOUNCEMENT,SYSTEM_ACTIVITY,POST_STATUS,POST_DELETE,USER_STATUS_CHANGE,USER_ROLE_CHANGE,GROUP_STATUS_CHANGE,GROUP_DELETE,HELP_INFO_STATUS_CHANGE,HELP_INFO_DELETE'" />
            </el-tab-pane>
            <el-tab-pane label="互助通知" name="help">
                <NotificationCenter :key="'help'" :title="'互助通知'" :type="'HELP_NEW_APPLICATION,HELP_APPLICATION_ACCEPTED,HELP_APPLICATION_REJECTED,HELP_COMPLETED,HELP_NEW_REVIEW'" />
            </el-tab-pane>
            <el-tab-pane label="小组通知" name="group">
                <NotificationCenter :key="'group'" :title="'小组通知'" :type="'GROUP_JOIN_APPLICATION,GROUP_JOIN_APPROVED,GROUP_JOIN_REJECTED,GROUP_INVITATION,GROUP_ANNOUNCEMENT,GROUP_ADMIN_ASSIGNED'" />
            </el-tab-pane>
            <el-tab-pane label="好友通知" name="friend">
                <NotificationCenter :key="'friend'" :title="'好友通知'" :type="'FRIEND_REQUEST,FRIEND_REQUEST_ACCEPTED,FRIEND_REQUEST_REJECTED,FRIEND_REMOVED'" />
            </el-tab-pane>
        </el-tabs>
        <!-- 公告弹窗逻辑保留，但按钮已移除 -->
        <el-dialog v-model="showAnnouncementDialog" title="发布系统公告" width="400px">
            <el-form :model="announcementForm" label-width="70px">
                <el-form-item label="标题">
                    <el-input v-model="announcementForm.title" maxlength="30" show-word-limit />
                </el-form-item>
                <el-form-item label="内容">
                    <el-input type="textarea" v-model="announcementForm.content" maxlength="200" show-word-limit rows="4" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showAnnouncementDialog = false">取消</el-button>
                <el-button type="primary" @click="handlePublishAnnouncement" :loading="publishing">发布</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue' // 引入 watch
import { useRoute, useRouter } from 'vue-router' // 引入 useRouter
import NotificationCenter from '@/components/common/NotificationCenter.vue'
import { ElMessage } from 'element-plus'
import { sendSystemNotification } from '@/api/notification'
import { useAuthStore } from '@/store/auth' // 引入 auth store

const route = useRoute()
const router = useRouter() // 初始化 router
const authStore = useAuthStore() // 使用 auth store

const activeTab = ref('all')
const showAnnouncementDialog = ref(false)
const publishing = ref(false)
const announcementForm = ref({ title: '', content: '' })

// 从 store 获取 isAdmin 状态
const isAdmin = ref(authStore.isAdmin)

// 监听 store 中 isAdmin 状态的变化
watch(() => authStore.isAdmin, (newIsAdmin) => {
  isAdmin.value = newIsAdmin
})

// 监听路由参数，用于从 NavBar 打开弹窗
watch(() => route.query.action, (newAction) => {
  if (newAction === 'publish-announcement' && isAdmin.value) {
    showAnnouncementDialog.value = true
    // 可选：清除 query 参数，避免重复触发
    router.replace({ query: { ...route.query, action: undefined } })
  }
}, { immediate: true })


// 从路由参数获取初始标签页
const initActiveTab = () => {
    const tab = route.query.tab as string
    if (tab && ['all', 'system', 'help', 'group', 'friend'].includes(tab)) {
        activeTab.value = tab
    }
}

// 处理标签页变化
const handleTabChange = (tab: string) => {
    activeTab.value = tab
}

const handlePublishAnnouncement = async () => {
    if (!announcementForm.value.title || !announcementForm.value.content) {
        ElMessage.warning('请填写完整公告信息')
        return
    }
    publishing.value = true
    try {
        await sendSystemNotification({
            type: 'SYSTEM_ANNOUNCEMENT',
            title: announcementForm.value.title,
            content: announcementForm.value.content,
            recipientId: authStore.user?.userId // 添加当前用户ID作为接收者
        })
        ElMessage.success('公告发布成功')
        showAnnouncementDialog.value = false
        announcementForm.value = { title: '', content: '' }
    } catch (e) {
        ElMessage.error('公告发布失败')
    } finally {
        publishing.value = false
    }
}

// 初始化
initActiveTab()
</script>

<style scoped>
.notification-page {
    height: 100%;
    padding: 20px;
    box-sizing: border-box;
    background-color: var(--background-color);
    transition: all 0.4s ease;
    animation: fadeIn 0.5s ease-out;
}

:deep(.el-tabs__header) {
    margin-bottom: 20px;
    border-bottom: 1px solid rgba(var(--el-color-primary-rgb), 0.1);
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 12px;
    padding: 8px 12px 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

:deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: rgba(var(--el-color-primary-rgb), 0.1);
}

:deep(.el-tabs__item) {
    transition: all 0.3s ease;
    height: 40px;
    line-height: 40px;
    font-size: 15px;
    color: var(--el-text-color-regular);
    position: relative;
}

:deep(.el-tabs__item:hover) {
    color: var(--el-color-primary);
}

:deep(.el-tabs__item.is-active) {
    color: var(--el-color-primary);
    font-weight: 600;
}

:deep(.el-tabs__item.is-active::after) {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 30px;
    height: 3px;
    border-radius: 3px;
    background: linear-gradient(90deg, var(--el-color-primary), var(--el-color-primary-light-3));
    transition: all 0.3s ease;
}

:deep(.el-tabs__content) {
    height: calc(100% - 65px);
    overflow-y: auto;
    padding: 4px;
}

:deep(.el-tab-pane) {
    height: 100%;
    padding: 4px;
}

/* 覆盖弹出对话框样式 */
:deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
    background: rgba(var(--el-color-primary-rgb), 0.05);
    padding: 16px 20px;
}

:deep(.el-dialog__title) {
    font-weight: 600;
    font-size: 18px;
    color: var(--el-text-color-primary);
}

:deep(.el-dialog__body) {
    padding: 24px 20px;
}

:deep(.el-dialog__footer) {
    padding: 16px 20px;
    border-top: 1px solid var(--el-border-color-lighter);
}

/* 暗色模式适配 */
[data-theme="dark"] .notification-page {
    background-color: var(--dark-bg, #18181a) !important;
    color: var(--dark-text-primary, #e5eaf3) !important;
}

[data-theme="dark"] :deep(.el-tabs__header) {
    background: rgba(36,41,61,0.82);
    border-bottom: 1px solid var(--dark-border-color, #333);
    box-shadow: 0 4px 16px rgba(0,0,0,0.18);
}

[data-theme="dark"] :deep(.el-tabs__nav-wrap::after) {
    background-color: var(--dark-border-color, #333);
}

[data-theme="dark"] :deep(.el-tabs__item) {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] :deep(.el-tabs__item.is-active) {
    color: var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] :deep(.el-tabs__item.is-active::after) {
    background: linear-gradient(90deg, var(--primary-color-dark, #60a9ff), #232326);
}

[data-theme="dark"] :deep(.el-tabs__content) {
    background: transparent !important;
}

[data-theme="dark"] :deep(.el-tab-pane) {
    background: transparent !important;
}

[data-theme="dark"] :deep(.el-dialog) {
    background: linear-gradient(135deg, rgba(36,41,61,0.95), rgba(45,55,72,0.92));
    border: 1.5px solid var(--dark-border-color, #333);
    color: var(--dark-text-primary, #e5eaf3);
    box-shadow: 0 8px 32px 0 rgba(0,0,0,0.28);
}

[data-theme="dark"] :deep(.el-dialog__header) {
    background: rgba(36,41,61,0.85);
    border-bottom: 1px solid var(--dark-border-color, #333);
}

[data-theme="dark"] :deep(.el-dialog__title) {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.el-dialog__body) {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.el-dialog__footer) {
    border-top: 1px solid var(--dark-border-color, #333);
}

[data-theme="dark"] :deep(.el-form-item__label) {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.el-input__wrapper) {
    background-color: rgba(36,41,61,0.6);
    box-shadow: 0 0 0 1px var(--dark-border-color, #333) inset;
}

[data-theme="dark"] :deep(.el-textarea__wrapper) {
    background-color: rgba(36,41,61,0.6);
    box-shadow: 0 0 0 1px var(--dark-border-color, #333) inset;
}

[data-theme="dark"] :deep(.el-input__inner),
[data-theme="dark"] :deep(.el-textarea__inner) {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.el-button--primary) {
    background: var(--primary-color-dark, #60a9ff);
    border-color: var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] :deep(.el-button--primary:hover) {
    background: var(--primary-color-dark-hover, #79b8ff);
    border-color: var(--primary-color-dark-hover, #79b8ff);
}

[data-theme="dark"] :deep(.el-divider) {
    border-color: var(--dark-border-color, #333) !important;
}

/* 过渡动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
    .notification-page {
        padding: 16px 12px;
    }
    
    :deep(.el-tabs__header) {
        padding: 6px 8px 0;
    }
    
    :deep(.el-tabs__item) {
        padding: 0 12px;
        font-size: 14px;
    }
    
    :deep(.el-dialog) {
        width: 90% !important;
        margin: 0 auto;
    }
}
</style>
