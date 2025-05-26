<template>
    <div class="notification-page">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部通知" name="all">
                <NotificationCenter :key="'all'" :title="'全部通知'" :type="'all'" />
            </el-tab-pane>
            <el-tab-pane label="系统通知" name="system">
                <NotificationCenter :key="'system'" :title="'系统通知'" :type="'SYSTEM_ANNOUNCEMENT,SYSTEM_ACTIVITY'" />
            </el-tab-pane>
            <el-tab-pane label="互助通知" name="help">
                <NotificationCenter :key="'help'" :title="'互助通知'" :type="'HELP_NEW_APPLICATION,HELP_APPLICATION_ACCEPTED,HELP_APPLICATION_REJECTED,HELP_COMPLETED,HELP_NEW_REVIEW'" />
            </el-tab-pane>
            <el-tab-pane label="小组通知" name="group">
                <NotificationCenter :key="'group'" :title="'小组通知'" :type="'GROUP_JOIN_APPLICATION,GROUP_JOIN_APPROVED,GROUP_JOIN_REJECTED,GROUP_INVITATION,GROUP_ANNOUNCEMENT,GROUP_ADMIN_ASSIGNED'" />
            </el-tab-pane>
            <el-tab-pane label="好友申请" name="friend">
                <NotificationCenter :key="'friend'" :title="'好友申请'" :type="'FRIEND_REQUEST,FRIEND_REQUEST_ACCEPTED,FRIEND_REQUEST_REJECTED'" />
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import NotificationCenter from '@/components/common/NotificationCenter.vue'

const route = useRoute()
const activeTab = ref('all')

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

:deep(.el-tabs__content) {
    height: calc(100% - 55px);
    overflow-y: auto;
}

:deep(.el-tab-pane) {
    height: 100%;
}
</style>
