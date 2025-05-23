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
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import ReviewList from '../components/ReviewList.vue';
import { getUserReviews } from '../api/review';
import { getUserById } from '../api/user';
import { Star, User, List } from '@element-plus/icons-vue';

const route = useRoute();
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



onMounted(() => {
  fetchUserInfo();
  fetchUserReviews();
});

// 监听路由参数变化，当用户ID改变时重新加载数据
watch(() => route.params.userId, (newUserId) => {
  console.log('路由参数userId发生变化：', newUserId);
  if (newUserId) {
    userId.value = Number(newUserId);
    fetchUserInfo();
    fetchUserReviews();
  }
}, { immediate: true });

// 监听用户ID变化，当用户ID改变时重新加载数据
watch(userId, (newId) => {
  console.log('用户ID变量发生变化：', newId);
  fetchUserInfo();
  fetchUserReviews();
});
</script>

<style scoped>
.user-profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 10px 60px;
}

.user-profile-card {
  border-radius: 18px;
  box-shadow: 0 2px 16px 0 rgba(60, 60, 60, 0.06);
  overflow: hidden;
}

.user-profile-header {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
  padding-bottom: 12px;
}

.user-profile-info {
  flex: 1;
  min-width: 200px;
}

.user-profile-info h2 {
  margin: 0 0 12px 0;
  font-size: 1.7rem;
  font-weight: 700;
  color: #222;
}

.user-basic-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 6px;
}

.user-credit {
  margin-top: 8px;
  min-width: 120px;
}

.credit-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}

.credit-value {
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
}

.credit-title {
  font-size: 13px;
  color: #909399;
}

.user-details-section {
  margin-top: 24px;
  background: #f8fafd;
  border-radius: 12px;
  padding: 18px 20px 10px 20px;
  box-shadow: 0 1px 6px 0 rgba(60, 60, 60, 0.04);
}

.details-row {
  margin-top: 10px;
}

.user-details-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  font-size: 17px;
  color: #409eff;
  font-weight: 600;
}

.detail-item {
  margin-bottom: 14px;
  line-height: 1.7;
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

.user-skills-section {
  margin-top: 22px;
}

.user-skills-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 16px;
  color: #67c23a;
  font-weight: 600;
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
</style>
