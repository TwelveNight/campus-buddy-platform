<template>
  <div class="notification-center">
    <div class="notification-header">
      <h2>{{ title }}</h2>
      <div class="notification-actions">
        <el-button type="text" @click="markAllAsRead" :disabled="loading || notifications.length === 0">
          全部标记为已读
        </el-button>
      </div>
    </div>

    <el-divider />

    <div class="notification-list">
      <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
      
      <el-skeleton :rows="3" animated v-if="loading" :count="3" />
      
      <template v-else>
        <div 
          v-for="notification in notifications" 
          :key="notification.notificationId"
          class="notification-item"
          :class="{ 'notification-unread': !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-avatar">
            <el-avatar 
              :size="40" 
              :src="notification.senderAvatar || defaultAvatar" 
              :icon="notification.senderAvatar ? '' : 'Bell'"
            />
            <div v-if="!notification.isRead" class="unread-dot"></div>
          </div>
          
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-body">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
          </div>
          
          <div class="notification-actions">
            <el-dropdown trigger="click" @command="handleCommand($event, notification)">
              <span class="el-dropdown-link">
                <el-icon><MoreFilled /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="markAsRead" v-if="!notification.isRead">
                    标记为已读
                  </el-dropdown-item>
                  <el-dropdown-item command="delete">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
    </div>

    <div class="notification-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled, Bell } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { getNotifications, markNotificationAsRead, markAllNotificationsAsRead, deleteNotification } from '@/api/notification'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  title: {
    type: String,
    default: '消息通知'
  },
  type: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const notifications = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultAvatar = ref('/avatar-placeholder.png')

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...(props.type ? { type: props.type } : {})
    }
    
    console.log('NotificationCenter - fetchNotifications params:', params)
    const res = await getNotifications(params)
    console.log('NotificationCenter - API response:', res)
    
    if (res.code === 200) {
      notifications.value = res.data.records || []
      total.value = res.data.total || 0
      console.log('NotificationCenter - parsed notifications:', notifications.value)
      console.log('NotificationCenter - total:', total.value)
    }
  } catch (error) {
    console.error('获取通知列表失败', error)
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

// 处理通知点击
const handleNotificationClick = (notification: any) => {
  // 如果未读，标记为已读
  if (!notification.isRead) {
    markAsRead(notification.notificationId)
  }
  
  // 如果有相关链接，跳转
  if (notification.relatedLink) {
    router.push(notification.relatedLink)
  }
}

// 标记为已读
const markAsRead = async (notificationId: number) => {
  try {
    const res = await markNotificationAsRead(notificationId)
    if (res.code === 200) {
      // 更新本地状态
      const index = notifications.value.findIndex(item => item.notificationId === notificationId)
      if (index !== -1) {
        notifications.value[index].isRead = true
      }
    }
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 标记全部为已读
const markAllAsRead = async () => {
  try {
    const res = await markAllNotificationsAsRead()
    if (res.code === 200) {
      // 更新本地状态
      notifications.value.forEach(item => {
        item.isRead = true
      })
      ElMessage.success('已将全部通知标记为已读')
    }
  } catch (error) {
    console.error('标记全部已读失败', error)
    ElMessage.error('操作失败')
  }
}

// 删除通知
const handleDelete = async (notificationId: number) => {
  try {
    const res = await deleteNotification(notificationId)
    if (res.code === 200) {
      // 更新本地状态
      notifications.value = notifications.value.filter(item => item.notificationId !== notificationId)
      ElMessage.success('删除成功')
      
      // 如果删除后当前页没有数据且不是第一页，则回到上一页
      if (notifications.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        fetchNotifications()
      }
    }
  } catch (error) {
    console.error('删除通知失败', error)
    ElMessage.error('删除失败')
  }
}

// 处理下拉菜单命令
const handleCommand = (command: string, notification: any) => {
  if (command === 'markAsRead') {
    markAsRead(notification.notificationId)
  } else if (command === 'delete') {
    ElMessageBox.confirm(
      '确定要删除这条通知吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      handleDelete(notification.notificationId)
    }).catch(() => {})
  }
}

// 处理分页变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchNotifications()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchNotifications()
}

// 监听类型变化
watch(() => props.type, () => {
  currentPage.value = 1
  fetchNotifications()
})

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notification-center {
  padding: 20px;
  background-color: var(--el-bg-color);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  position: relative;
  background-color: var(--el-bg-color-page);
  cursor: pointer;
}

.notification-item:hover {
  background-color: var(--el-fill-color-light);
}

.notification-unread {
  background-color: var(--el-color-primary-light-9);
}

.notification-avatar {
  margin-right: 15px;
  position: relative;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--el-color-danger);
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--el-text-color-primary);
}

.notification-body {
  color: var(--el-text-color-regular);
  margin-bottom: 5px;
  font-size: 14px;
}

.notification-time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.notification-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.notification-actions {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>
