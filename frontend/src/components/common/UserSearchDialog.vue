<template>
  <div class="user-search-dialog">
    <el-dialog 
      v-model="dialogVisible" 
      title="搜索用户" 
      width="500px" 
      align-center
      :close-on-click-modal="false"
      @closed="resetSearch"
    >
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="输入用户昵称、学号或手机号搜索"
          clearable
          prefix-icon="Search"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        
        <div class="search-results" v-loading="loading">
          <el-empty v-if="!loading && searchResults.length === 0 && searchKeyword" description="未找到相关用户" />
          <el-empty v-else-if="!loading && searchResults.length === 0 && !searchKeyword" description="请输入关键词搜索用户" />
          
          <div v-for="user in searchResults" :key="user.userId" class="user-item">
            <div class="user-info" @click="viewUserProfile(user.userId)">
              <el-avatar :size="40" :src="user.avatarUrl || defaultAvatar"></el-avatar>
              <div class="user-details">
                <div class="user-name">{{ user.nickname || user.username }}</div>
                <div class="user-meta" v-if="user.major || user.grade">
                  {{ user.major }} {{ user.grade }}
                </div>
              </div>
            </div>
            <div class="user-actions">
              <!-- 自己不显示任何按钮 -->
              <template v-if="user.userId === currentUserId">
                <el-tag type="info" size="small">
                  <el-icon><UserFilled /></el-icon> 这是您自己
                </el-tag>
              </template>
              
              <!-- 已是好友显示私信按钮 -->
              <template v-else-if="isFriend(user.userId)">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleAction('message', user)"
                  plain
                >
                  <el-icon><ChatDotRound /></el-icon> 私信
                </el-button>
                <el-tag type="success" size="small">
                  <el-icon><Check /></el-icon> 已是好友
                </el-tag>
              </template>
              
              <!-- 已发送申请等待处理 -->
              <template v-else-if="getRequestStatus(user.userId) === 'PENDING_OUTGOING'">
                <el-tag type="warning" size="small">
                  <el-icon><Timer /></el-icon> 已发送申请，等待处理
                </el-tag>
              </template>
              
              <!-- 收到对方申请等待自己处理 -->
              <template v-else-if="getRequestStatus(user.userId) === 'PENDING_INCOMING'">
                <el-button 
                  type="warning" 
                  size="small" 
                  @click="handleFriendRequest(user.userId)"
                  plain
                >
                  <el-icon><Timer /></el-icon> 收到申请，去处理
                </el-button>
              </template>
              
              <!-- 其他情况显示加好友按钮 -->
              <template v-else>
                <el-button 
                  type="success" 
                  size="small" 
                  @click="handleAction('friend', user)"
                  plain
                >
                  <el-icon><Plus /></el-icon> 加好友
                </el-button>
              </template>
            </div>
          </div>
          
          <div class="pagination" v-if="searchResults.length > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :current-page="currentPage"
              :page-size="pageSize"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, defineExpose, defineEmits, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { ChatDotRound, Plus, Check, Search, Timer, UserFilled } from '@element-plus/icons-vue';
import { searchUsers, getUserInfo } from '../../api/user';
import { applyFriend, checkFriendStatus } from '../../api/friend';
import { useAuthStore } from '../../store/auth';

const emit = defineEmits(['select-user', 'message-user', 'add-friend']);
const router = useRouter();
const authStore = useAuthStore();

// 状态
const dialogVisible = ref(false);
const searchKeyword = ref('');
const searchResults = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const friendIds = ref<number[]>([]); // 存储当前用户的好友ID列表
const friendStatusMap = ref<Record<number, boolean>>({}); // 记录每个用户的好友状态
const requestStatusMap = ref<Record<number, string>>({}); // 记录好友申请状态

// 当前登录用户ID
const currentUserId = computed(() => authStore.user?.userId);

// 打开对话框
const open = (options?: { friendIds?: number[] }) => {
  dialogVisible.value = true;
  if (options?.friendIds) {
    friendIds.value = options.friendIds;
  }
};

// 检查单个用户是否为好友
const checkIsFriend = async (userId: number) => {
  // 如果是自己，直接返回false
  if (userId === currentUserId.value) return false;
  
  if (friendStatusMap.value[userId] !== undefined) return friendStatusMap.value[userId];
  try {
    const res = await checkFriendStatus(userId);
    if (res.data && typeof res.data.data?.isFriend === 'boolean') {
      friendStatusMap.value[userId] = res.data.data.isFriend;
      // 保存申请状态
      if (res.data.data.requestStatus) {
        requestStatusMap.value[userId] = res.data.data.requestStatus;
      }
      return res.data.data.isFriend;
    }
  } catch {}
  friendStatusMap.value[userId] = false;
  return false;
};

// 检查是否为好友（异步，优先本地缓存）
const isFriend = (userId: number) => {
  if (friendStatusMap.value[userId] !== undefined) return friendStatusMap.value[userId];
  // 触发异步检查
  checkIsFriend(userId);
  return false;
};

// 获取好友申请状态
const getRequestStatus = (userId: number) => {
  return requestStatusMap.value[userId] || 'NONE';
};

// 搜索用户
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  
  loading.value = true;
  try {
    const res = await searchUsers({
      keyword: searchKeyword.value,
      page: currentPage.value,
      size: pageSize.value
    });
    
    if (res.data.code === 200) {
      searchResults.value = res.data.data.records || [];
      total.value = res.data.data.total || 0;
      // 批量检查好友状态
      for (const user of searchResults.value) {
        checkIsFriend(user.userId);
      }
    } else {
      ElMessage.error(res.data.message || '搜索失败');
    }
  } catch (error) {
    console.error('搜索用户出错:', error);
    ElMessage.error('搜索用户失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  handleSearch();
};

// 查看用户资料
const viewUserProfile = (userId: number) => {
  router.push(`/user/${userId}`);
  dialogVisible.value = false;
};

// 操作处理
const handleAction = (type: 'message' | 'friend', user: any) => {
  if (type === 'message') {
    emit('message-user', user);
    router.push(`/messages/${user.userId}`);
    dialogVisible.value = false;
  } else if (type === 'friend') {
    addFriend(user);
  }
};

// 添加好友
const addFriend = async (user: any) => {
  try {
    // 先检查是否已经是好友或已发送申请
    const status = getRequestStatus(user.userId);
    if (status === 'PENDING_OUTGOING') {
      ElMessage.info(`已向${user.nickname || user.username}发送过好友申请，请等待对方处理`);
      return;
    }
    
    if (isFriend(user.userId)) {
      ElMessage.info(`你们已经是好友，无需重复添加`);
      return;
    }
    
    const res = await applyFriend(user.userId);
    if (res.data.code === 200) {
      ElMessage.success(`已向${user.nickname || user.username}发送好友申请`);
      // 更新申请状态
      requestStatusMap.value[user.userId] = 'PENDING_OUTGOING';
      emit('add-friend', user);
    } else {
      // 根据后端返回状态处理不同情况
      if (res.data.data && res.data.data.status) {
        const status = res.data.data.status;
        if (status === 'ALREADY_FRIEND') {
          friendStatusMap.value[user.userId] = true;
          ElMessage.info(`你们已经是好友，无需重复添加`);
        } else if (status === 'ALREADY_APPLIED') {
          requestStatusMap.value[user.userId] = 'PENDING_OUTGOING';
          ElMessage.info(`已向${user.nickname || user.username}发送过好友申请，请等待对方处理`);
        } else {
          ElMessage.error(res.data.message || '发送好友申请失败');
        }
      } else {
        ElMessage.error(res.data.message || '发送好友申请失败');
      }
    }
  } catch (error) {
    console.error('添加好友出错:', error);
    ElMessage.error('发送好友申请失败，请稍后重试');
  }
};

// 处理好友申请
const handleFriendRequest = (userId: number) => {
  // 跳转到好友管理页面处理申请
  router.push(`/friends?tab=requests`);
  dialogVisible.value = false;
};

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  searchResults.value = [];
  currentPage.value = 1;
};

// 暴露方法
defineExpose({
  open
});
</script>

<style scoped>
.search-container {
  padding: 10px 0;
}

.search-results {
  margin-top: 20px;
  min-height: 200px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.user-item:hover {
  background-color: #f9f9f9;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex: 1;
}

.user-details {
  margin-left: 12px;
}

.user-name {
  font-weight: 500;
  font-size: 15px;
  color: #333;
}

.user-meta {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}

.user-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 暗色模式适配 */
[data-theme="dark"] .user-item {
  border-bottom-color: #333;
}

[data-theme="dark"] .user-item:hover {
  background-color: #2b2b2b;
}

[data-theme="dark"] .user-name {
  color: #e0e0e0;
}

[data-theme="dark"] .user-meta {
  color: #aaa;
}
</style>
