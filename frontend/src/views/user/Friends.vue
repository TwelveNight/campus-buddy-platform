<template>
  <div class="friends-list-container">
    <el-card class="friends-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="title">
            <el-icon><UserFilled /></el-icon>
            <h2>我的好友</h2>
          </div>
          <div class="actions">
            <el-button type="primary" @click="openUserSearch" plain>
              <el-icon><Plus /></el-icon>添加好友
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索好友"
          prefix-icon="Search"
          clearable
          @input="filterFriends"
        />
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="好友列表" name="friends"></el-tab-pane>
          <el-tab-pane label="好友申请" name="requests">
            <template #label>
              好友申请
              <el-badge :value="pendingRequestsCount" :hidden="pendingRequestsCount === 0" />
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 好友列表 -->
      <div v-if="activeTab === 'friends'" class="friends-list" v-loading="loading">
        <el-empty v-if="filteredFriends.length === 0 && !loading" description="暂无好友" />
        
        <div v-for="friend in filteredFriends" :key="friend.userId" class="friend-item">
          <div class="friend-info" @click="viewUserProfile(friend.friendId)">
            <el-avatar :size="50" :src="friend.avatarUrl || defaultAvatar"></el-avatar>
            <div class="friend-details">
              <div class="friend-name">{{ friend.nickname || friend.username }}</div>
              <div class="friend-meta" v-if="friend.major || friend.grade">
                {{ friend.major }} {{ friend.grade }}
              </div>
            </div>
          </div>
          <div class="friend-actions">
            <el-dropdown trigger="click">
              <el-button type="primary" size="small" plain>
                操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="sendMessage(friend)">
                    <el-icon><ChatDotRound /></el-icon> 发起私聊
                  </el-dropdown-item>
                  <el-dropdown-item @click="viewUserProfile(friend.friendId)">
                    <el-icon><View /></el-icon> 查看资料
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="confirmDeleteFriend(friend)" class="danger-item">
                    <el-icon><Delete /></el-icon> 删除好友
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <el-pagination
          v-if="totalFriends > pageSize"
          background
          layout="prev, pager, next"
          :total="totalFriends"
          :current-page="currentPage"
          :page-size="pageSize"
          @current-change="handlePageChange"
          class="pagination"
        />
      </div>
      
      <!-- 好友申请列表 -->
      <div v-else class="friend-requests" v-loading="requestsLoading">
        <el-empty v-if="friendRequests.length === 0 && !requestsLoading" description="暂无好友申请" />
        <div v-for="request in friendRequests" :key="request.requestId" class="request-item">
          <div class="request-info" @click="viewUserProfile(request.requester.userId)">
            <el-avatar :src="request.requester.avatarUrl || defaultAvatar" />
            <div class="request-details">
              <div class="request-name">{{ request.requester.nickname || request.requester.username }}</div>
              <div class="request-meta">
                <span>{{ formatTime(request.createdAt) }}</span>
              </div>
              <div class="request-message" v-if="request.requestMessage">{{ request.requestMessage }}</div>
            </div>
          </div>
          <div class="request-actions" v-if="request.status === 'PENDING'">
            <el-button size="small" type="primary" @click="acceptRequest(request)">接受</el-button>
            <el-button size="small" @click="rejectRequest(request)">拒绝</el-button>
          </div>
          <div class="request-actions" v-else>
            <el-tag v-if="request.status === 'ACCEPTED'" type="success">已同意</el-tag>
            <el-tag v-else-if="request.status === 'REJECTED'" type="info">已拒绝</el-tag>
          </div>
        </div>
        <el-pagination
          class="pagination"
          :current-page="requestsPage"
          :page-size="pageSize"
          :total="totalRequests"
          @current-change="handleRequestsPageChange"
        />
      </div>
    </el-card>
    
    <!-- 用户搜索对话框 -->
    <user-search-dialog 
      ref="userSearchDialog" 
      @message-user="sendMessage" 
      @add-friend="handleAddFriend"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UserFilled, Plus, ArrowDown, ChatDotRound, View, Delete, Search } from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';
import UserSearchDialog from '../../components/common/UserSearchDialog.vue';
import { getFriendList, getFriendRequests, acceptFriendRequest, rejectFriendRequest, deleteFriend } from '../../api/friend';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const router = useRouter();
const userSearchDialog = ref(null);

// 状态变量
const loading = ref(false);
const requestsLoading = ref(false);
const friends = ref<any[]>([]);
const friendRequests = ref<any[]>([]);
const searchKeyword = ref('');
const activeTab = ref('friends');
const currentPage = ref(1);
const requestsPage = ref(1);
const pageSize = ref(10);
const totalFriends = ref(0);
const totalRequests = ref(0);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 过滤后的好友列表
const filteredFriends = computed(() => {
  if (!searchKeyword.value) return friends.value;
  
  const keyword = searchKeyword.value.toLowerCase();
  return friends.value.filter(friend => {
    const nickname = (friend.nickname || '').toLowerCase();
    const username = (friend.username || '').toLowerCase();
    const major = (friend.major || '').toLowerCase();
    return nickname.includes(keyword) || username.includes(keyword) || major.includes(keyword);
  });
});

// 待处理的好友申请数量
const pendingRequestsCount = computed(() => {
  return friendRequests.value.filter(req => req.status === 'PENDING').length;
});

// 生命周期钩子
onMounted(() => {
  loadFriends();
  loadFriendRequests();
});

// 加载好友列表
const loadFriends = async () => {
  loading.value = true;
  try {
    const res = await getFriendList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    });
    
    if (res.data.code === 200) {
      // 拍平后端返回的好友数据结构，方便前端渲染
      friends.value = (res.data.data || []).map(item => ({
        ...item.friend,
        id: item.id,
        friendId: item.friendId
      }));
      totalFriends.value = friends.value.length || 0;
      console.log('好友列表:', friends.value);
      console.log('总好友数:', totalFriends.value);
    } else {
      ElMessage.error(res.data.message || '获取好友列表失败');
    }
  } catch (error) {
    console.error('加载好友列表出错:', error);
    ElMessage.error('获取好友列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 加载好友申请列表
const loadFriendRequests = async () => {
  requestsLoading.value = true;
  try {
    const res = await getFriendRequests({
      page: requestsPage.value,
      size: pageSize.value
    });
    if (res.data.code === 200) {
      console.log('好友申请列表:', res.data.data);
      friendRequests.value = res.data.data || [];
      totalRequests.value = res.data.data.total || 0;
    } else {
      ElMessage.error(res.data.message || '获取好友申请列表失败');
    }
  } catch (error) {
    console.error('加载好友申请列表出错:', error);
    ElMessage.error('获取好友申请列表失败，请稍后重试');
  } finally {
    requestsLoading.value = false;
  }
};

// 筛选好友
const filterFriends = () => {
  loadFriends();
};

// 标签页切换
const handleTabChange = (tab: string) => {
  if (tab === 'requests') {
    loadFriendRequests();
  } else {
    loadFriends();
  }
};

// 好友列表分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadFriends();
};

// 好友申请列表分页变化
const handleRequestsPageChange = (page: number) => {
  requestsPage.value = page;
  loadFriendRequests();
};

// 查看用户资料
const viewUserProfile = (userId: number) => {
  router.push(`/user/${userId}`);
};

// 发送私信
const sendMessage = (user: any) => {
  // 好友列表：friend.friendId
  if (user.friendId) {
    router.push(`/messages/${user.friendId}`)
    return
  }
  // 好友申请：requester.userId
  if (user.userId) {
    router.push(`/messages/${user.userId}`)
    return
  }
  // 兜底
  if (user.id) {
    router.push(`/messages/${user.id}`)
    return
  }
  ElMessage.error('无法识别用户ID，无法发起私聊');
};

// 打开用户搜索对话框
const openUserSearch = () => {
  const friendIds = friends.value.map(friend => friend.userId);
  userSearchDialog.value.open({ friendIds });
};

// 接受好友申请
const acceptRequest = async (request: any) => {
  try {
    const res = await acceptFriendRequest(request.requestId);
    if (res.data.code === 200) {
      ElMessage.success('已接受好友申请');
      loadFriendRequests();
      loadFriends();
    } else {
      ElMessage.error(res.data.message || '接受好友申请失败');
    }
  } catch (error) {
    console.error('接受好友申请出错:', error);
    ElMessage.error('接受好友申请失败，请稍后重试');
  }
};

// 拒绝好友申请
const rejectRequest = async (request: any) => {
  try {
    const res = await rejectFriendRequest(request.requestId);
    if (res.data.code === 200) {
      ElMessage.success('已拒绝好友申请');
      loadFriendRequests();
    } else {
      ElMessage.error(res.data.message || '拒绝好友申请失败');
    }
  } catch (error) {
    console.error('拒绝好友申请出错:', error);
    ElMessage.error('拒绝好友申请失败，请稍后重试');
  }
};

// 确认删除好友
const confirmDeleteFriend = (friend: any) => {
  ElMessageBox.confirm(
    `确定要删除好友"${friend.nickname || friend.username}"吗？`,
    '删除好友',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    handleDeleteFriend(friend.userId);
  }).catch(() => {});
};

// 删除好友
const handleDeleteFriend = async (userId: number) => {
  try {
    const res = await deleteFriend(userId);
    if (res.data.code === 200) {
      ElMessage.success('已删除好友');
      loadFriends();
    } else {
      ElMessage.error(res.data.message || '删除好友失败');
    }
  } catch (error) {
    console.error('删除好友出错:', error);
    ElMessage.error('删除好友失败，请稍后重试');
  }
};

// 添加好友后刷新申请列表
const handleAddFriend = () => {
  loadFriendRequests();
};

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow();
};
</script>

<style scoped>
.friends-list-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 10px 60px;
}

.friends-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.filter-section {
  margin-bottom: 20px;
}

.friend-item, .request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.friend-item:hover, .request-item:hover {
  background-color: #f9f9f9;
}

.friend-info, .request-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex: 1;
}

.friend-details, .request-details {
  margin-left: 15px;
}

.friend-name, .request-name {
  font-weight: 500;
  font-size: 16px;
  color: #333;
}

.friend-meta, .request-meta {
  font-size: 13px;
  color: #999;
  margin-top: 3px;
}

.request-message {
  font-size: 13px;
  color: #666;
  margin-top: 3px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-actions, .request-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.danger-item {
  color: #f56c6c;
}

/* 暗色模式适配 */
[data-theme="dark"] .friend-item, 
[data-theme="dark"] .request-item {
  border-bottom-color: #333;
}

[data-theme="dark"] .friend-item:hover, 
[data-theme="dark"] .request-item:hover {
  background-color: #2b2b2b;
}

[data-theme="dark"] .friend-name, 
[data-theme="dark"] .request-name {
  color: #e0e0e0;
}

[data-theme="dark"] .friend-meta, 
[data-theme="dark"] .request-meta {
  color: #aaa;
}

[data-theme="dark"] .request-message {
  color: #bbb;
}

[data-theme="dark"] .danger-item {
  color: #f78989;
}
</style>
