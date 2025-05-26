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
    background-color: var(--el-bg-color);
}

/* .notification-header-bar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 10px;
} */

:deep(.el-tabs__content) {
    height: calc(100% - 55px);
    overflow-y: auto;
}

:deep(.el-tab-pane) {
    height: 100%;
}

/* 暗色模式适配 */
[data-theme="dark"] .notification-page {
    background-color: var(--dark-bg, #18181c) !important;
}

[data-theme="dark"] :deep(.el-tabs__header) {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-light);
}

[data-theme="dark"] :deep(.el-tabs__item) {
    color: var(--dark-text-regular);
}

[data-theme="dark"] :deep(.el-tabs__item.is-active) {
    color: var(--el-color-primary);
}

[data-theme="dark"] :deep(.el-tabs__nav-wrap::after) {
    background-color: var(--dark-border-lighter);
}

[data-theme="dark"] :deep(.el-tabs__content) {
    background-color: var(--dark-bg, #18181c) !important;
}

[data-theme="dark"] :deep(.el-tab-pane) {
    background-color: var(--dark-bg, #18181c) !important;
}
</style>
