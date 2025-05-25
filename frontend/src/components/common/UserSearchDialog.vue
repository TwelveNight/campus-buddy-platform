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
              <el-button 
                type="primary" 
                size="small" 
                @click="handleAction('message', user)"
                plain
              >
                <el-icon><ChatDotRound /></el-icon> 私信
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="handleAction('friend', user)"
                plain
                v-if="!isFriend(user.userId)"
              >
                <el-icon><Plus /></el-icon> 加好友
              </el-button>
              <el-tag v-else type="success" size="small">
                <el-icon><Check /></el-icon> 已是好友
              </el-tag>
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
import { ref, defineExpose, defineEmits } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { ChatDotRound, Plus, Check, Search } from '@element-plus/icons-vue';
import { searchUsers } from '../../api/user';
import { applyFriend } from '../../api/friend';

const emit = defineEmits(['select-user', 'message-user', 'add-friend']);
const router = useRouter();

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

// 打开对话框
const open = (options?: { friendIds?: number[] }) => {
  dialogVisible.value = true;
  if (options?.friendIds) {
    friendIds.value = options.friendIds;
  }
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

// 检查是否为好友
const isFriend = (userId: number) => {
  return friendIds.value.includes(userId);
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
    const res = await applyFriend(user.userId);
    if (res.data.code === 200) {
      ElMessage.success(`已向${user.nickname || user.username}发送好友申请`);
      emit('add-friend', user);
    } else {
      ElMessage.error(res.data.message || '发送好友申请失败');
    }
  } catch (error) {
    console.error('添加好友出错:', error);
    ElMessage.error('发送好友申请失败，请稍后重试');
  }
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
