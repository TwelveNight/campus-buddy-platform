<template>
  <div class="notification-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部通知" name="all">
        <NotificationCenter :title="'全部通知'" :type="'all'" />
      </el-tab-pane>
      <el-tab-pane label="系统通知" name="system">
        <NotificationCenter :title="'系统通知'" :type="getSystemNotificationTypes()" />
      </el-tab-pane>
      <el-tab-pane label="互助通知" name="help">
        <NotificationCenter :title="'互助通知'" :type="getHelpNotificationTypes()" />
      </el-tab-pane>
      <el-tab-pane label="小组通知" name="group">
        <NotificationCenter :title="'小组通知'" :type="getGroupNotificationTypes()" />
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
  if (tab && ['all', 'system', 'help', 'group'].includes(tab)) {
    activeTab.value = tab
  }
}

// 处理标签页变化
const handleTabChange = (tab: string) => {
  activeTab.value = tab
}

// 获取系统通知类型
const getSystemNotificationTypes = () => {
  return 'SYSTEM_ANNOUNCEMENT,SYSTEM_ACTIVITY'
}

// 获取互助通知类型
const getHelpNotificationTypes = () => {
  return 'HELP_NEW_APPLICATION,HELP_APPLICATION_ACCEPTED,HELP_APPLICATION_REJECTED,HELP_COMPLETED,HELP_NEW_REVIEW'
}

// 获取小组通知类型
const getGroupNotificationTypes = () => {
  return 'GROUP_JOIN_APPROVED,GROUP_JOIN_REJECTED,GROUP_INVITATION,GROUP_ANNOUNCEMENT,GROUP_ADMIN_ASSIGNED'
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
