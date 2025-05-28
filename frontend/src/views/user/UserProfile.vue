<template>
  <div class="user-profile-page">
    <el-card class="user-profile-card" shadow="hover">
      <div class="user-profile-header">
        <el-avatar :size="100" :src="userInfo.avatarUrl || defaultAvatar" />
        <div class="user-profile-info">
          <h2>{{ userInfo.nickname || userInfo.username || ('用户#' + userInfo.userId) }}</h2>
          <div class="user-basic-info">
            <el-tag v-if="userInfo.gender" :type="getGenderTag(userInfo.gender)" size="small">
              {{ formatGender(userInfo.gender) }}
            </el-tag>
            <el-tag v-if="userInfo.major" type="info" size="small">{{ userInfo.major }}</el-tag>
            <el-tag v-if="userInfo.grade" type="info" size="small">{{ userInfo.grade }}</el-tag>
            <el-tag type="success" size="small">{{ formatDate(userInfo.createdAt) }} 加入</el-tag>
          </div>
          <!-- 新增操作按钮 -->
          <div class="user-profile-actions" v-if="showActionButtons">
            <el-button type="primary" size="small" @click="startPrivateChat" v-if="isFriend">
              <el-icon><ChatDotRound /></el-icon> 发私信
            </el-button>
            <el-button type="success" size="small" @click="addFriend" v-if="!isFriend && friendRequestStatus !== 'PENDING_OUTGOING'">
              <el-icon><Plus /></el-icon> 加好友
            </el-button>
            <el-tag v-else-if="isFriend" type="success" size="small">
              <el-icon><Check /></el-icon> 已是好友
            </el-tag>
            <el-tag v-else-if="friendRequestStatus === 'PENDING_OUTGOING'" type="info" size="small">
              <el-icon><Check /></el-icon> 已发送申请，等待对方处理
            </el-tag>
          </div>
        </div>
        <div class="user-credit">
          <el-progress type="dashboard" :percentage="userInfo.creditScore ?? 0" :color="creditColors" :stroke-width="8">
            <template #default>
              <div class="credit-label">
                <span class="credit-value">{{ userInfo.creditScore ?? 0 }}</span>
                <span class="credit-title">信誉分</span>
              </div>
            </template>
          </el-progress>
          <div class="credit-level">
            {{ creditStats.creditLevel }}
            <el-tag v-if="creditStats.trend !== '稳定'" :type="getTrendType(creditStats.trend)" effect="light" size="small">
              {{ creditStats.trend }}
              <el-icon v-if="creditStats.trend.includes('上升')"><CaretTop /></el-icon>
              <el-icon v-else-if="creditStats.trend.includes('下降')"><CaretBottom /></el-icon>
            </el-tag>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="user-details-section">
        <h3><el-icon>
            <User />
          </el-icon> 详细信息</h3>
        <el-row :gutter="24" class="details-row">
          <el-col :span="12">
            <div class="detail-item">
              <span class="detail-label">用户名</span>
              <span class="detail-value">{{ userInfo.username }}</span>
            </div>
            <div class="detail-item" v-if="userInfo.contactInfo">
              <span class="detail-label">联系方式</span>
              <span class="detail-value">{{ userInfo.contactInfo }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">注册时间</span>
              <span class="detail-value">{{ formatDateTime(userInfo.createdAt) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <span class="detail-label">账号状态</span>
              <el-tag :type="getStatusType(userInfo.status)">{{ formatStatus(userInfo.status) }}</el-tag>
            </div>
            <div class="detail-item" v-if="userInfo.major">
              <span class="detail-label">专业</span>
              <span class="detail-value">{{ userInfo.major }}</span>
            </div>
            <div class="detail-item" v-if="userInfo.grade">
              <span class="detail-label">年级</span>
              <span class="detail-value">{{ userInfo.grade }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="user-skills-section" v-if="parsedSkillTags.length > 0">
        <h3><el-icon>
            <List />
          </el-icon> 技能标签</h3>
        <div class="skill-tags">
          <el-tag v-for="(tag, index) in parsedSkillTags" :key="index" class="skill-tag" effect="light" type="primary">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="user-review-card" shadow="hover" style="margin-top: 24px;">
      <template #header>
        <div class="card-header">
          <el-icon>
            <Star />
          </el-icon>
          <h2>收到的评价</h2>
        </div>
      </template>
      <ReviewList :reviews="reviews" :loading="loading" :showFilter="false" :targetUserId="userId" :showReviewTarget="false" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import ReviewList from '../../components/common/ReviewList.vue';
import { getUserReviews, getUserCreditStats } from '../../api/review';
import { getUserById } from '../../api/user';
import { applyFriend, getFriendList, checkFriendStatus } from '../../api/friend';
import { Star, User, List, ChatDotRound, Plus, Check, CaretTop, CaretBottom } from '@element-plus/icons-vue';
import { useAuthStore } from '../../store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const userId = ref(Number(route.params.userId));
const loading = ref(false);
const userInfo = ref<any>({ nickname: '', username: '', userId: '', creditScore: 0, avatarUrl: '', createdAt: '', status: 'ACTIVE', skillTags: '' });
const reviews = ref<any[]>([]);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const creditColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
];

// 信用分统计信息
const creditStats = ref({
  creditScore: 0,
  creditLevel: '未评级',
  totalReviews: 0,
  averageScore: 0,
  recentReviews: 0,
  recentAverageScore: 0,
  trend: '稳定'
});

// 当前登录用户ID
const currentUserId = computed(() => authStore.user?.userId);
// 是否本人
const isSelf = computed(() => currentUserId.value === userId.value);
// 是否显示操作按钮
const showActionButtons = computed(() => !isSelf.value && !!currentUserId.value);
// 好友ID列表
const friendIds = ref<number[]>([]);
// 是否已是好友
const isFriend = ref(false);
// 好友请求状态
const friendRequestStatus = ref<string | null>(null);

// 检查是否为好友，并返回好友状态
const checkIsFriend = async () => {
  if (!currentUserId.value || !userId.value || isSelf.value) {
    isFriend.value = false;
    friendRequestStatus.value = null;
    return;
  }
  try {
    const res = await checkFriendStatus(userId.value);
    if (res.data && typeof res.data.data?.isFriend === 'boolean') {
      isFriend.value = res.data.data.isFriend;
      friendRequestStatus.value = res.data.data.requestStatus || null;
    } else {
      isFriend.value = false;
      friendRequestStatus.value = null;
    }
  } catch (e) {
    isFriend.value = false;
    friendRequestStatus.value = null;
  }
};

// 加好友
const addFriend = async () => {
  try {
    const res = await applyFriend(userId.value);
    if (res.data.code === 200) {
      ElMessage.success('好友申请已发送');
      await checkIsFriend();
    } else {
      // 针对已发送过申请的情况，后端会返回特定提示
      if (res.data.message && res.data.message.includes('已发送过好友申请')) {
        ElMessage.info('已发送过好友申请，请等待对方处理');
      } else if (res.data.message && res.data.message.includes('已经是好友')) {
        ElMessage.info('你们已经是好友，无需重复添加');
      } else {
        ElMessage.error(res.data.message || '发送好友申请失败');
      }
    }
  } catch (e: any) {
    // 针对后端异常返回重复申请等
    if (e?.response?.data?.message && e.response.data.message.includes('已发送过好友申请')) {
      ElMessage.info('已发送过好友申请，请等待对方处理');
    } else if (e?.response?.data?.message && e.response.data.message.includes('已经是好友')) {
      ElMessage.info('你们已经是好友，无需重复添加');
    } else {
      ElMessage.error('发送好友申请失败');
    }
  }
};
// 发起私聊
const startPrivateChat = () => {
  router.push(`/messages/${userId.value}`);
};
// 加载好友ID列表
const loadFriendIds = async () => {
  if (!currentUserId.value) return;
  try {
    const res = await getFriendList({ page: 1, size: 100 });
    if (res.data.code === 200) {
      friendIds.value = (res.data.data.records || []).map((f: any) => f.userId);
    }
  } catch (e) {}
};

onMounted(() => {
  fetchUserInfo();
  fetchUserReviews();
  fetchUserCreditStats();
  checkIsFriend();
});

// 解析技能标签
const parsedSkillTags = computed(() => {
  if (!userInfo.value.skillTags) return [];
  try {
    return JSON.parse(userInfo.value.skillTags);
  } catch (e) {
    return [];
  }
});

// 格式化性别显示
const formatGender = (gender: string) => {
  switch (gender) {
    case 'MALE': return '男';
    case 'FEMALE': return '女';
    default: return '保密';
  }
};

// 获取性别标签类型
const getGenderTag = (gender: string) => {
  switch (gender) {
    case 'MALE': return 'info';
    case 'FEMALE': return 'danger';
    default: return 'info';
  }
};

// 格式化日期（简短格式）
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`;
};

// 格式化日期时间（完整格式）
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 格式化状态显示
const formatStatus = (status: string) => {
  switch (status) {
    case 'ACTIVE': return '正常';
    case 'INACTIVE': return '未激活';
    case 'BANNED': return '已禁用';
    default: return '未知';
  }
};

// 获取状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success';
    case 'INACTIVE': return 'info';
    case 'BANNED': return 'danger';
    default: return 'info';
  }
};

// 获取趋势标签类型
const getTrendType = (trend: string) => {
  if (trend.includes('上升')) return 'success';
  if (trend.includes('下降')) return 'danger';
  return 'info';
};

async function fetchUserInfo() {
  try {
    const res = await getUserById(userId.value);
    if (res.data && res.data.data) {
      // 正确从 res.data.data 获取用户信息
      userInfo.value = { ...userInfo.value, ...res.data.data };
    } else if (res.data) {
      // 兼容直接返回数据的情况
      userInfo.value = { ...userInfo.value, ...res.data };
    }
    console.log('获取到的用户信息：', userInfo.value);
  } catch (e) {
    ElMessage.error('获取用户信息失败');
  }
}

async function fetchUserReviews() {
  loading.value = true;
  try {
    const res = await getUserReviews({ userId: userId.value, type: 'received', page: 1, size: 10 });
    console.log('获取到的评价数据：', res);
    
    if (res.data && Array.isArray(res.data.items)) {
      reviews.value = res.data.items;
    } else if (Array.isArray(res.data)) {
      reviews.value = res.data;
    } else {
      reviews.value = [];
    }
    
    // 确保数据中的评价都是针对当前用户的
    reviews.value = reviews.value.filter(review => review.reviewedUserId === userId.value);
    
    console.log('过滤后的收到的评价：', reviews.value);
  } catch (e) {
    console.error('获取评价失败：', e);
    ElMessage.error('获取评价失败');
  } finally {
    loading.value = false;
  }
}

// 监听路由参数变化，当用户ID改变时重新加载数据
watch(() => route.params.userId, (newUserId) => {
  console.log('路由参数userId发生变化：', newUserId);
  if (newUserId) {
    userId.value = Number(newUserId);
    fetchUserInfo();
    fetchUserReviews();
    fetchUserCreditStats();
    checkIsFriend();
  }
}, { immediate: true });

// 监听用户ID变化，当用户ID改变时重新加载数据
watch(userId, (newId) => {
  console.log('用户ID变量发生变化：', newId);
  fetchUserInfo();
  fetchUserReviews();
  fetchUserCreditStats();
  checkIsFriend();
});

// 获取用户信用分详细统计
async function fetchUserCreditStats() {
  if (!userId.value) return;
  
  try {
    const res = await getUserCreditStats(userId.value);
    if (res && res.data) {
      creditStats.value = res.data;
      // 更新用户信息中的信用分以确保显示一致
      userInfo.value.creditScore = creditStats.value.creditScore;
      console.log('获取到的信用分统计：', creditStats.value);
    }
  } catch (error) {
    console.error('获取信用分统计失败：', error);
  }
}
</script>

<style scoped>
.user-profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 10px 60px;
}

.user-profile-card {
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 2px 16px 0 rgba(60, 60, 60, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.user-profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-profile-info {
  flex: 1;
}

.user-profile-info h2 {
  font-size: 1.8rem;
  margin-bottom: 12px;
  font-weight: 600;
  color: #222;
}

.user-basic-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.user-profile-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.user-credit {
  text-align: center;
}

.credit-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}

.credit-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.credit-title {
  font-size: 14px;
  color: #909399;
}

.credit-level {
  margin-top: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #555;
}

.credit-level .el-tag {
  margin-left: 4px;
}

.user-details-section,
.user-skills-section {
  padding: 16px 0;
}

.user-details-section h3,
.user-skills-section h3 {
  font-size: 18px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.detail-label {
  font-weight: 600;
  color: #666;
  margin-right: 10px;
  min-width: 70px;
  display: inline-block;
}

.detail-value {
  color: #333;
  font-size: 15px;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tag {
  margin-right: 0;
  font-size: 14px;
  padding: 4px 14px;
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h2 {
  flex: 1;
}

/* 用户详情页暗色模式优化 */
[data-theme="dark"] .user-profile-page {
  background: transparent;
}

[data-theme="dark"] .user-profile-card {
  background: linear-gradient(135deg, rgba(36,41,61,0.92), rgba(45,55,72,0.88));
  box-shadow: 0 8px 32px rgba(0,0,0,0.28);
  border: 1px solid rgba(64,158,255,0.1);
}

[data-theme="dark"] .user-profile-header h2 {
  color: #e5eaf3;
}

[data-theme="dark"] .credit-value {
  color: #e5eaf3;
}

[data-theme="dark"] .credit-title {
  color: #a3a6ad;
}

[data-theme="dark"] .credit-level {
  color: #e5eaf3;
}

[data-theme="dark"] .detail-label {
  color: #a3a6ad;
}

[data-theme="dark"] .detail-value {
  color: #e5eaf3;
}

[data-theme="dark"] .user-skills-section h3,
[data-theme="dark"] .user-details-section h3 {
  color: #60a9ff;
}

[data-theme="dark"] .skill-tag {
  background: rgba(64, 158, 255, 0.15);
  color: #60a9ff;
  border-color: rgba(64, 158, 255, 0.3);
}

[data-theme="dark"] .skill-tag:hover {
  background: rgba(64, 158, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 3px 10px rgba(64, 158, 255, 0.2);
}

[data-theme="dark"] .el-divider {
  border-color: rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .user-profile-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .user-profile-info {
    width: 100%;
  }
  
  .user-basic-info,
  .user-profile-actions {
    justify-content: center;
  }
  
  .details-row {
    display: block;
  }
  
  .details-row .el-col {
    width: 100%;
    margin-bottom: 16px;
  }
}
</style>
