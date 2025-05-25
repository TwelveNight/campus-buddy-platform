<template>
  <div class="group-list-page">
    <h1 class="page-title">学习小组</h1>

    <div class="group-actions">
      <el-button type="primary" @click="showCreateGroupDialog">
        <el-icon>
          <Plus />
        </el-icon> 创建小组
      </el-button>

      <div class="group-filters">
        <el-input v-model="searchKeyword" placeholder="搜索小组名称或描述" class="search-input" clearable
          @keyup.enter="loadGroups">
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
        </el-input>

        <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="loadGroups">
          <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
        </el-select>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="所有小组" name="all">
        <div class="group-grid" v-loading="loading">
          <el-empty v-if="groups.length === 0 && !loading" description="暂无小组" />
          <el-card v-for="group in groups" :key="group.groupId" 
            class="group-card"
            :class="{'group-card-disbanded': group.status === 'DISBANDED'}"
            @click="group.status === 'DISBANDED' ? null : goToGroupDetail(group.groupId)">
            <div class="group-avatar">
              <el-avatar :size="64" :src="group.avatar || group.avatarUrl || defaultAvatar">
                {{ group.name?.substring(0, 1) }}
              </el-avatar>
            </div>
            <div class="group-info">
              <h3 class="group-name">
                {{ group.name }}
                <el-tag v-if="group.status === 'DISBANDED'" type="danger" size="small" style="margin-left:8px;">已解散</el-tag>
                <el-tag v-else-if="group.joinType === 'PUBLIC'" type="success" size="small" style="margin-left:8px;">公开</el-tag>
                <el-tag v-else-if="group.joinType === 'APPROVAL'" type="warning" size="small" style="margin-left:8px;">需审批</el-tag>
              </h3>
              <div class="group-meta">
                <span>
                  <el-icon>
                    <User />
                  </el-icon> {{ group.memberCount }} 成员
                </span>
                <span>
                  <el-tag size="small">{{ group.category }}</el-tag>
                </span>
              </div>
              <!-- 创建者信息展示 -->
              <div class="group-creator" v-if="group.creator">
                <span style="color:#888;font-size:13px;">创建者：</span>
                <router-link :to="`/user/${group.creator.userId}`" class="creator-link" style="display:inline-flex;align-items:center;gap:6px;">
                  <el-avatar :size="22" :src="group.creator.avatarUrl || defaultAvatar" />
                  <span>{{ group.creator.nickname }}</span>
                </router-link>
              </div>
              <div v-else style="color:#bbb;font-size:12px;">（仅在小组详情页可见创建者）</div>
              <p class="group-description">{{ truncateText(group.description, 60) }}</p>
              <div class="group-tags">
                <el-tag v-for="tag in normalizeTags(group.tags)" :key="tag" type="info" class="tag">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="group-actions">
              <el-button v-if="group.status === 'DISBANDED'" type="info" size="small" plain disabled>已解散</el-button>
              <template v-else>
                <el-button v-if="!group.memberStatus" type="primary" size="small" plain @click.stop="handleJoinGroup(group)">
                  加入小组
                </el-button>
                <el-button v-else-if="group.memberStatus === 'PENDING' || group.memberStatus === 'PENDING_APPROVAL'" type="warning" size="small" plain disabled>
                  等待审批
                </el-button>
                <el-button v-else-if="group.memberStatus === 'ACTIVE'" type="success" size="small" plain disabled>
                  已加入
                </el-button>
              </template>
            </div>
          </el-card>
        </div>

        <div class="pagination">
          <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[8, 16, 24, 32]"
            layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
            @current-change="handleCurrentChange" background />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我创建的" name="created">
        <div class="group-grid" v-loading="loading">
          <el-empty v-if="groups.length === 0 && !loading" description="您暂未创建任何小组" />
          <el-card v-for="group in groups" :key="group.groupId" 
            class="group-card"
            :class="{'group-card-disbanded': group.status === 'DISBANDED'}"
            @click="group.status === 'DISBANDED' ? null : goToGroupDetail(group.groupId)">
            <div class="group-avatar">
              <el-avatar :size="64" :src="group.avatar || group.avatarUrl || defaultAvatar">
                {{ group.name?.substring(0, 1) }}
              </el-avatar>
            </div>
            <div class="group-info">
              <h3 class="group-name">
                {{ group.name }}
                <el-tag v-if="group.status === 'DISBANDED'" type="danger" size="small" style="margin-left:8px;">已解散</el-tag>
                <el-tag v-else-if="group.joinType === 'PUBLIC'" type="success" size="small" style="margin-left:8px;">公开</el-tag>
                <el-tag v-else-if="group.joinType === 'APPROVAL'" type="warning" size="small" style="margin-left:8px;">需审批</el-tag>
              </h3>
              <div class="group-meta">
                <span>
                  <el-icon>
                    <User />
                  </el-icon> {{ group.memberCount }} 成员
                </span>
                <span>
                  <el-tag size="small">{{ group.category }}</el-tag>
                </span>
              </div>
              <!-- 创建者信息展示 -->
              <div class="group-creator" v-if="group.creator">
                <span style="color:#888;font-size:13px;">创建者：</span>
                <router-link :to="`/user/${group.creator.userId}`" class="creator-link" style="display:inline-flex;align-items:center;gap:6px;">
                  <el-avatar :size="22" :src="group.creator.avatarUrl || defaultAvatar" />
                  <span>{{ group.creator.nickname }}</span>
                </router-link>
              </div>
              <p class="group-description">{{ truncateText(group.description, 60) }}</p>
              <div class="group-tags">
                <el-tag v-for="tag in normalizeTags(group.tags)" :key="tag" type="info" class="tag">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="group-actions">
              <el-button v-if="group.status === 'DISBANDED'" type="info" size="small" plain disabled>已解散</el-button>
              <el-button v-else type="success" size="small" plain disabled>
                我创建的
              </el-button>
            </div>
          </el-card>
        </div>

        <div class="pagination" v-if="activeTab === 'created' && groups.length > 0">
          <!-- 对于创建的小组，不需要分页，因为数据量通常较小 -->
        </div>
      </el-tab-pane>

      <el-tab-pane label="我加入的" name="joined">
        <div class="group-grid" v-loading="loading">
          <el-empty v-if="groups.length === 0 && !loading" description="您暂未加入任何小组" />
          <el-card v-for="group in groups" :key="group.groupId" 
            class="group-card"
            :class="{'group-card-disbanded': group.status === 'DISBANDED'}"
            @click="group.status === 'DISBANDED' ? null : goToGroupDetail(group.groupId)">
            <div class="group-avatar">
              <el-avatar :size="64" :src="group.avatar || group.avatarUrl || defaultAvatar">
                {{ group.name?.substring(0, 1) }}
              </el-avatar>
            </div>
            <div class="group-info">
              <h3 class="group-name">
                {{ group.name }}
                <el-tag v-if="group.status === 'DISBANDED'" type="danger" size="small" style="margin-left:8px;">已解散</el-tag>
                <el-tag v-else-if="group.joinType === 'PUBLIC'" type="success" size="small" style="margin-left:8px;">公开</el-tag>
                <el-tag v-else-if="group.joinType === 'APPROVAL'" type="warning" size="small" style="margin-left:8px;">需审批</el-tag>
              </h3>
              <div class="group-meta">
                <span>
                  <el-icon>
                    <User />
                  </el-icon> {{ group.memberCount }} 成员
                </span>
                <span>
                  <el-tag size="small">{{ group.category }}</el-tag>
                </span>
              </div>
              <!-- 创建者信息展示 -->
              <div class="group-creator" v-if ="group.creator" >
                <span style="color:#888;font-size:13px;">创建者：</span>
                <router-link :to="`/user/${group.creator.userId}`" class="creator-link" style="display:inline-flex;align-items:center;gap:6px;">
                  <el-avatar :size="22" :src="group.creator.avatarUrl || defaultAvatar" />
                  <span>{{ group.creator.nickname }}</span>
                </router-link>
              </div>
              <p class="group-description">{{ truncateText(group.description, 60) }}</p>
              <div class="group-tags">
                <el-tag v-for="tag in normalizeTags(group.tags)" :key="tag" type="info" class="tag">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="group-actions">
              <el-button v-if="group.status === 'DISBANDED'" type="info" size="small" plain disabled>已解散</el-button>
              <el-button v-else type="success" size="small" plain disabled>
                已加入
              </el-button>
            </div>
          </el-card>
        </div>

        <div class="pagination" v-if="activeTab === 'joined' && groups.length > 0">
          <!-- 对于加入的小组，不需要分页，因为数据量通常较小 -->
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建小组对话框 -->
    <el-dialog v-model="createGroupDialogVisible" title="创建学习小组" width="50%">
      <el-form :model="groupForm" :rules="groupRules" ref="groupFormRef" label-width="80px"
        @submit.prevent="handleCreateGroup">
        <el-form-item label="名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="请输入小组名称" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="groupForm.category" placeholder="选择小组分类">
            <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-select v-model="groupForm.tags" multiple filterable allow-create default-first-option
            placeholder="请输入标签（可多选，最多5个）" :max-collapse-tags="2">
            <el-option v-for="tag in commonTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>

        <el-form-item label="加入方式" prop="joinType">
          <el-radio-group v-model="groupForm.joinType">
            <el-radio label="PUBLIC">公开（任何人可加入）</el-radio>
            <el-radio label="APPROVAL">需审批（需管理员同意）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="groupForm.description" type="textarea" rows="4" placeholder="请输入小组描述" />
        </el-form-item>

        <el-form-item label="小组头像" prop="avatarUrl">
          <AvatarUploader v-model="groupForm.avatarUrl" :size="120" 
            @upload-success="handleAvatarUploadSuccess" tip="点击上传小组头像" :isGroupAvatar="true" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createGroupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateGroup" :loading="submitting">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, User } from '@element-plus/icons-vue';
import {
  getGroups,
  getCreatedGroups,
  getJoinedGroups,
  createGroup,
  joinGroup
} from '@/api/group';
import { useAuthStore } from '@/store/auth';
import ImageUploader from '../components/ImageUploader.vue';
import AvatarUploader from '../components/AvatarUploader.vue';
import { uploadApi } from '../api/upload';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 数据状态
const loading = ref(false);
const submitting = ref(false);
const groups = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(16);
const searchKeyword = ref('');
const selectedCategory = ref('');
const activeTab = ref('all');
const createGroupDialogVisible = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 小组创建表单
const groupFormRef = ref(null);
const groupForm = ref({
  name: '',
  category: '',
  tags: [],
  joinType: 'PUBLIC',
  description: '',
  avatarUrl: '' // 只保留字符串字段
});

// 表单验证规则
const groupRules = {
  name: [
    { required: true, message: '请输入小组名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择小组分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入小组描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ]
};

// 选项数据
const categories = [
  '学科学习',
  '考研考证',
  '技能培训',
  '课程讨论',
  '竞赛组队',
  '职业发展',
  '兴趣爱好',
  '其他'
];

const commonTags = [
  '计算机',
  '数学',
  '英语',
  '物理',
  '化学',
  '生物',
  '经济',
  '管理',
  '文学',
  '历史',
  '哲学',
  '艺术',
  '体育',
  '编程',
  '设计',
  '演讲',
  '写作',
  '考研',
  '考证',
  '实习',
  '就业'
];

// 计算属性
const userJoinedGroups = ref([]);

// 兼容后端返回的tags为字符串的情况
const normalizeTags = (tags) => {
  if (Array.isArray(tags)) return tags;
  if (typeof tags === 'string') return tags.split(/[，,]/).map(t => t.trim()).filter(Boolean);
  return [];
};

// 生命周期钩子
// 监听路由变化，处理参数变化
watch(() => route.query, (newQuery) => {
  console.log('GroupList - 路由查询参数变化:', newQuery);
  handleRouteQuery();
}, { deep: true });

// 处理路由查询参数
const handleRouteQuery = () => {
  const { tab, action } = router.currentRoute.value.query;
  
  console.log('GroupList - 处理路由参数:', { tab, action });
  
  // 处理标签参数
  if (tab) {
    const tabValue = tab.toString();
    if (['all', 'joined', 'created'].includes(tabValue)) {
      activeTab.value = tabValue;
      console.log('GroupList - 设置活动标签:', activeTab.value);
    }
  }
  
  // 处理动作参数
  if (action === 'create') {
    console.log('GroupList - 显示创建小组对话框');
    createGroupDialogVisible.value = true;
  }
};

// 生命周期钩子
onMounted(() => {
  // 处理路由查询参数
  handleRouteQuery();

  // 加载小组列表
  loadGroups();

  // 加载用户已加入的小组（用于判断加入状态）
  if (authStore.isAuthenticated) {
    loadUserJoinedGroups();
  }
});

// 加载小组列表
const loadGroups = async () => {
  // 如果是"所有小组"标签页，确保先刷新用户已加入的小组信息
  if (activeTab.value === 'all' && authStore.isAuthenticated) {
    await loadUserJoinedGroups();
    console.log('已刷新用户加入的小组信息:', userJoinedGroups.value);
  }

  loading.value = true;
  try {
    let response;
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      category: selectedCategory.value || undefined,
      keyword: searchKeyword.value || undefined
    };

    if (activeTab.value === 'all') {
      response = await getGroups(params);
    } else if (activeTab.value === 'created') {
      response = await getCreatedGroups();
    } else if (activeTab.value === 'joined') {
      response = await getJoinedGroups();
    }

    if (response.data && response.data.code === 200) {
      if (activeTab.value === 'all') {
        groups.value = response.data.data.records || [];
        total.value = response.data.data.total || 0;
      } else {
        // 对于创建的和加入的小组，直接使用数组数据
        const groupList = response.data.data || [];
        groups.value = groupList;
        total.value = groupList.length;
      }
      
      console.log(`${activeTab.value} tab loaded:`, groups.value.length, 'groups');
    } else {
      ElMessage.error(response.data?.message || '加载小组列表失败');
      groups.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('加载小组列表失败:', error);
    ElMessage.error('加载小组列表失败，请稍后重试');
    groups.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 加载用户已加入的小组
const loadUserJoinedGroups = async () => {
  try {
    const response = await getJoinedGroups();
    if (response.data && response.data.code === 200) {
      userJoinedGroups.value = response.data.data || [];
      console.log('已加载用户加入的小组:', userJoinedGroups.value.length, '个');
      
      // 调试信息：打印每个已加入小组的ID
      if (userJoinedGroups.value.length > 0) {
        console.log('已加入小组ID列表:', userJoinedGroups.value.map(g => g.groupId));
      }
    }
  } catch (error) {
    console.error('加载用户已加入小组失败:', error);
    userJoinedGroups.value = []; // 确保在出错时重置数组
  }
};

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  loadGroups();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadGroups();
};

// 修改handleTabChange函数，确保在标签切换时更新相关数据
const handleTabChange = (tab) => {
  console.log('Tab changed to:', tab);
  // 重置页面状态
  currentPage.value = 1;
  groups.value = [];
  total.value = 0;
  
  // 对于created和joined标签页，清除搜索和分类筛选
  if (tab === 'created' || tab === 'joined') {
    searchKeyword.value = '';
    selectedCategory.value = '';
  }

  // 如果是切换到"所有小组"标签，确保刷新用户已加入的小组信息
  if (tab === 'all' && authStore.isAuthenticated) {
    // loadUserJoinedGroups会在loadGroups中调用
  }
  
  loadGroups();
};

// 截断文本
const truncateText = (text, length) => {
  if (!text) return '';
  return text.length > length ? text.substring(0, length) + '...' : text;
};

// 显示创建小组对话框
const showCreateGroupDialog = () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  createGroupDialogVisible.value = true;
};

// 处理小组头像上传成功
const handleAvatarUploadSuccess = (url) => {
  if (!url) {
    ElMessage.warning('头像URL为空，无法更新');
    return;
  }

  // 确保URL没有时间戳参数，避免重复添加
  const cleanUrl = url.split('?')[0];
  
  // 更新表单中的头像URL
  groupForm.value.avatarUrl = cleanUrl;
  
  ElMessage.success('小组头像上传成功');
};

// 创建小组
const handleCreateGroup = async () => {
  if (!groupFormRef.value) return;

  await groupFormRef.value.validate(async (valid) => {
    if (!valid) return;

    submitting.value = true;
    try {
      // 头像已经在上传成功时更新了，这里直接使用
      const avatarUrl = groupForm.value.avatarUrl;
      
      // 格式化数据以匹配后端期望的格式
      const createData = {
        name: groupForm.value.name,
        description: groupForm.value.description,
        category: groupForm.value.category,
        joinType: groupForm.value.joinType,
        tags: Array.isArray(groupForm.value.tags) 
          ? groupForm.value.tags.join(',') 
          : groupForm.value.tags,
        avatarUrl: avatarUrl,
        status: 'ACTIVE'
      };
      const response = await createGroup(createData);
      if (response.data && response.data.code === 200) {
        ElMessage.success('创建小组成功');
        createGroupDialogVisible.value = false;
        groupForm.value = {
          name: '',
          category: '',
          tags: [],
          joinType: 'PUBLIC',
          description: '',
          avatarUrl: ''
        };
        if (activeTab.value === 'created') {
          loadGroups();
        }
        router.push(`/groups/${response.data.data}`);
      } else {
        ElMessage.error(response.data?.message || '创建小组失败');
      }
    } catch (error) {
      console.error('创建小组失败:', error);
      ElMessage.error('创建小组失败，请稍后重试');
    } finally {
      submitting.value = false;
    }
  });
};

// 加入小组
const handleJoinGroup = async (group) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  try {
    const response = await joinGroup(group.groupId);
    if (response.data && response.data.code === 200) {
      // 更新加入状态
      await loadUserJoinedGroups();
      // 加入后强制刷新小组列表，确保审批状态及时更新
      await loadGroups();
      console.log('加入小组后更新状态:', userJoinedGroups.value);
      // 根据小组类型显示不同的提示信息
      if (group.joinType === 'APPROVAL') {
        ElMessage.info('小组需要审核，请等待管理员批准');
      } else {
        ElMessage.success('已成功加入小组');
        if (activeTab.value === 'joined') {
          loadGroups();
        }
      }
    } else {
      ElMessage.error(response.data?.message || '加入小组失败');
    }
  } catch (error) {
    console.error('加入小组失败:', error);
    ElMessage.error('加入小组失败，请稍后重试');
  }
};

// 跳转到小组详情页
const goToGroupDetail = (groupId) => {
  // 检查用户是否已经是小组成员
  const isMember = userJoinedGroups.value.some(g => g.groupId === groupId);
  
  // 如果是小组成员，直接跳转到详情页；否则跳转到预览页
  if (isMember) {
    router.push(`/groups/${groupId}/detail`);
  } else {
    router.push(`/groups/${groupId}`);
  }
};
</script>

<style scoped>
.group-list-page {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  color: #333;
}

.group-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.group-filters {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
}

.group-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.group-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.group-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.group-card-disbanded {
  background: #f5f5f5 !important;
  filter: grayscale(0.7);
  pointer-events: none;
  opacity: 0.7;
  border: 1.5px dashed #e57373;
}

.group-card-disbanded .el-tag[type="danger"] {
  background: #e57373;
  color: #fff;
  border: none;
}

.group-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.group-info {
  flex: 1;
}

.group-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.group-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.group-creator {
  margin-bottom: 10px;
}

.group-description {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 15px;
  min-height: 40px;
}

.group-tags {
  margin-bottom: 15px;
}

.group-card .group-actions {
  margin-top: auto;
  display: flex;
  justify-content: center;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
