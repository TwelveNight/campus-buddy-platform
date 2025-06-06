<template>
  <div class="group-list-page">
    <h1 class="page-title animated-title">
      <span class="title-text">学习小组</span>
      <div class="title-decoration"></div>
    </h1>

    <div class="group-actions">
      <el-button type="primary" @click="showCreateGroupDialog" class="create-btn glowing-btn">
        <el-icon class="rotating-icon">
          <Plus />
        </el-icon> 
        <span>创建小组</span>
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
          <el-card v-for="(group, index) in groups" :key="group.groupId" 
            class="group-card card-entrance"
            :style="{ '--animation-delay': `${index * 0.1}s` }"
            :class="{
              'group-card-disbanded': group.status === 'DISBANDED',
              'group-card-inactive': group.status === 'INACTIVE'
            }"
            @click="group.status === 'DISBANDED' ? null : goToGroupDetail(group.groupId)">
            <div class="group-avatar floating-avatar">
              <el-avatar :size="64" :src="group.avatar || group.avatarUrl || defaultAvatar">
                {{ group.name?.substring(0, 1) }}
              </el-avatar>
              <div class="avatar-glow"></div>
            </div>
            <div class="group-info">
              <h3 class="group-name">
                {{ group.name }}
                <el-tag v-if="group.status === 'DISBANDED'" type="danger" size="small" style="margin-left:8px;">已解散</el-tag>
                <el-tag v-else-if="group.status === 'INACTIVE'" type="warning" size="small" style="margin-left:8px;">已禁用</el-tag>
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
            </div>              <div class="group-actions">
                <el-button v-if="group.status === 'DISBANDED'" type="info" size="small" plain disabled class="action-btn">已解散</el-button>
                <el-button v-else-if="group.status === 'INACTIVE'" type="warning" size="small" plain class="action-btn inactive-btn">
                  <span>已禁用</span>
                </el-button>
                <template v-else>
                  <el-button v-if="!group.memberStatus" type="primary" size="small" plain @click.stop="handleJoinGroup(group)" class="action-btn pulse-btn">
                    <span>加入小组</span>
                  </el-button>
                  <el-button v-else-if="group.memberStatus === 'PENDING' || group.memberStatus === 'PENDING_APPROVAL'" type="warning" size="small" plain disabled class="action-btn">
                    <span>等待审批</span>
                  </el-button>
                  <el-button v-else-if="group.memberStatus === 'ACTIVE'" type="success" size="small" plain disabled class="action-btn">
                    <span>已加入</span>
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
          <el-card v-for="(group, index) in groups" :key="group.groupId" 
            class="group-card card-entrance"
            :style="{ '--animation-delay': `${index * 0.1}s` }"
            :class="{
              'group-card-disbanded': group.status === 'DISBANDED',
              'group-card-inactive': group.status === 'INACTIVE'
            }"
            @click="group.status === 'DISBANDED' ? null : goToGroupDetail(group.groupId)">
            <div class="group-avatar floating-avatar">
              <el-avatar :size="64" :src="group.avatar || group.avatarUrl || defaultAvatar">
                {{ group.name?.substring(0, 1) }}
              </el-avatar>
              <div class="avatar-glow"></div>
            </div>
            <div class="group-info">
              <h3 class="group-name">
                {{ group.name }}
                <el-tag v-if="group.status === 'DISBANDED'" type="danger" size="small" style="margin-left:8px;">已解散</el-tag>
                <el-tag v-else-if="group.status === 'INACTIVE'" type="warning" size="small" style="margin-left:8px;">已禁用</el-tag>
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
              <el-button v-if="group.status === 'DISBANDED'" type="info" size="small" plain disabled class="action-btn">已解散</el-button>
              <el-button v-else type="success" size="small" plain disabled class="action-btn creator-badge">
                <span>我创建的</span>
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
          <el-card v-for="(group, index) in groups" :key="group.groupId" 
            class="group-card card-entrance"
            :style="{ '--animation-delay': `${index * 0.1}s` }"
            :class="{'group-card-disbanded': group.status === 'DISBANDED', 'group-card-inactive': group.status === 'INACTIVE'}"
            @click="group.status === 'DISBANDED' ? null : goToGroupDetail(group.groupId)">
            <div class="group-avatar floating-avatar">
              <el-avatar :size="64" :src="group.avatar || group.avatarUrl || defaultAvatar">
                {{ group.name?.substring(0, 1) }}
              </el-avatar>
              <div class="avatar-glow"></div>
            </div>
            <div class="group-info">
              <h3 class="group-name">
                {{ group.name }}
                <el-tag v-if="group.status === 'DISBANDED'" type="danger" size="small" style="margin-left:8px;">已解散</el-tag>
                <el-tag v-else-if="group.status === 'INACTIVE'" type="warning" size="small" style="margin-left:8px;">已禁用</el-tag>
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
              <el-button v-if="group.status === 'DISBANDED'" type="info" size="small" plain disabled class="action-btn">已解散</el-button>
              <el-button v-else-if="group.status === 'INACTIVE'" type="warning" size="small" plain disabled class="action-btn">已禁用</el-button>
              <el-button v-else type="success" size="small" plain disabled class="action-btn member-badge">
                <span>已加入</span>
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
import ImageUploader from '../../components/form/ImageUploader.vue';
import AvatarUploader from '../../components/form/AvatarUploader.vue';
import { uploadApi } from '../../api/upload';

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
        // 修复：创建小组后直接进入详细页而不是预览页
        router.push(`/groups/${response.data.data}/detail`);
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
  animation: pageEnter 0.6s ease-out;
}

/* 页面进入动画 */
@keyframes pageEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 炫酷的页面标题动画 */
.animated-title {
  position: relative;
  text-align: center;
  margin-bottom: 40px;
  overflow: hidden;
}

.title-text {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleFloat 3s ease-in-out infinite;
  display: inline-block;
}

.title-decoration {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #667eea, #764ba2, transparent);
  border-radius: 2px;
  animation: decorationGlow 2s ease-in-out infinite alternate;
}

@keyframes titleFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
}

@keyframes decorationGlow {
  from { 
    box-shadow: 0 0 5px rgba(102, 126, 234, 0.5);
    transform: translateX(-50%) scaleX(1);
  }
  to { 
    box-shadow: 0 0 20px rgba(118, 75, 162, 0.8);
    transform: translateX(-50%) scaleX(1.2);
  }
}

/* 创建按钮发光效果 */
.create-btn {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.create-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.create-btn:hover::before {
  left: 100%;
}

/* 旋转图标动画 */
.rotating-icon {
  transition: transform 0.3s ease;
}

.create-btn:hover .rotating-icon {
  transform: rotate(180deg);
}

.page-title {
  font-size: 2rem;
  margin-bottom: 24px;
  text-align: center;
  font-weight: 600;
}

.group-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.group-filters {
  display: flex;
  gap: 16px;
  flex: 1;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.search-input {
  max-width: 280px;
}

.group-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

/* 小组卡片进入动画 */
.card-entrance {
  animation: cardSlideIn 0.6s ease-out forwards;
  animation-delay: var(--animation-delay, 0s);
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}

@keyframes cardSlideIn {
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.group-card {
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  padding: 24px;
  position: relative;
  background: linear-gradient(135deg, #fff 0%, #f8f9ff 100%);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.group-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.group-card:hover::before {
  opacity: 1;
}

.group-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 
    0 20px 40px rgba(102, 126, 234, 0.15),
    0 0 0 1px rgba(102, 126, 234, 0.1);
}

/* 头像浮动效果 */
.floating-avatar {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.floating-avatar .el-avatar {
  transition: all 0.3s ease;
  border: 3px solid rgba(102, 126, 234, 0.1);
  animation: avatarFloat 4s ease-in-out infinite;
}

.group-card:hover .floating-avatar .el-avatar {
  transform: scale(1.1);
  border-color: rgba(102, 126, 234, 0.3);
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.group-card:hover .avatar-glow {
  opacity: 1;
  animation: glowPulse 1.5s ease-in-out infinite;
}

@keyframes avatarFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
}

@keyframes glowPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.3; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.1; }
}

.group-card-disbanded {
  opacity: 0.6;
  cursor: default;
  position: relative;
  overflow: hidden;
}

.group-card-disbanded::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 10px,
    rgba(0, 0, 0, 0.03) 10px,
    rgba(0, 0, 0, 0.03) 20px
  );
  pointer-events: none;
}

.group-card-disbanded:hover {
  transform: none;
  box-shadow: var(--el-box-shadow-light);
}

.group-card-inactive {
  opacity: 0.85;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.08) 0%, rgba(255, 235, 59, 0.05) 100%);
  border-left: 4px solid #f39c12;
  position: relative;
}

.group-card-inactive::before {
  background: linear-gradient(135deg, rgba(243, 156, 18, 0.05) 0%, rgba(255, 193, 7, 0.05) 100%);
}

.group-card-inactive:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 
    0 15px 35px rgba(243, 156, 18, 0.2),
    0 0 0 1px rgba(243, 156, 18, 0.1);
}

/* 按钮动画效果 */
.action-btn {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border-radius: 20px;
}

.pulse-btn {
  animation: buttonPulse 2s ease-in-out infinite;
}

.pulse-btn:hover {
  animation: none;
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.creator-badge {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border: none;
  color: white;
}

.member-badge {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  color: white;
}

@keyframes buttonPulse {
  0%, 100% { 
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.4);
  }
  50% { 
    box-shadow: 0 0 0 10px rgba(102, 126, 234, 0);
  }
}

/* 小组信息动画 */
.group-info {
  margin-top: 16px;
  flex: 1;
  animation: contentSlideUp 0.6s ease-out 0.2s both;
}

@keyframes contentSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.group-name {
  font-size: 1.4rem;
  margin-bottom: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  color: #2c3e50;
  transition: color 0.3s ease;
}

.group-card:hover .group-name {
  color: #667eea;
}

.group-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.group-creator {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.creator-link {
  color: var(--primary-color);
  font-size: 14px;
  text-decoration: none;
}

.creator-link:hover {
  color: var(--primary-color-light);
}

.group-description {
  margin: 12px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.group-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag {
  margin: 0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.tag:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.pagination {
  display: flex;
  justify-content: center;
  margin: 30px 0;
}

@media (max-width: 768px) {
  .group-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .group-filters {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    max-width: none;
    width: 100%;
  }

  .group-grid {
    grid-template-columns: 1fr;
  }
}

/* 学习小组列表卡片暗色模式优化 */
[data-theme="dark"] .group-list-page {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

[data-theme="dark"] .title-text {
  background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 50%, #2196f3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

[data-theme="dark"] .title-decoration {
  background: linear-gradient(90deg, transparent, #64b5f6, #2196f3, transparent);
}

[data-theme="dark"] .create-btn {
  background: linear-gradient(135deg, #64b5f6 0%, #2196f3 100%);
}

[data-theme="dark"] .create-btn:hover {
  box-shadow: 0 8px 25px rgba(100, 181, 246, 0.4);
}

[data-theme="dark"] .group-card {
  background: linear-gradient(135deg, rgba(36,41,61,0.95) 0%, rgba(22,33,62,0.95) 100%);
  border: 1px solid rgba(100, 181, 246, 0.1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

[data-theme="dark"] .group-card::before {
  background: linear-gradient(135deg, rgba(100, 181, 246, 0.05) 0%, rgba(33, 150, 243, 0.05) 100%);
}

[data-theme="dark"] .group-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 
    0 20px 40px rgba(100, 181, 246, 0.2),
    0 0 0 1px rgba(100, 181, 246, 0.15);
  border-color: rgba(100, 181, 246, 0.2);
}

[data-theme="dark"] .floating-avatar .el-avatar {
  border-color: rgba(100, 181, 246, 0.2);
}

[data-theme="dark"] .group-card:hover .floating-avatar .el-avatar {
  border-color: rgba(100, 181, 246, 0.4);
}

[data-theme="dark"] .avatar-glow {
  background: radial-gradient(circle, rgba(100, 181, 246, 0.15) 0%, transparent 70%);
}

[data-theme="dark"] .group-card-disbanded {
  background: linear-gradient(135deg, rgba(36,41,61,0.7) 0%, rgba(45,45,50,0.7) 100%);
  opacity: 0.6;
}

[data-theme="dark"] .group-card-disbanded::after {
  background: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 10px,
    rgba(255, 255, 255, 0.02) 10px,
    rgba(255, 255, 255, 0.02) 20px
  );
}

[data-theme="dark"] .group-card-inactive {
  background: linear-gradient(135deg, rgba(36,41,61,0.8) 0%, rgba(255, 193, 7, 0.08) 100%);
  border-left: 4px solid #e67e22;
  opacity: 0.85;
}

[data-theme="dark"] .group-card-inactive::before {
  background: linear-gradient(135deg, rgba(230, 126, 34, 0.05) 0%, rgba(255, 193, 7, 0.05) 100%);
}

[data-theme="dark"] .group-card-inactive:hover {
  box-shadow: 
    0 15px 35px rgba(230, 126, 34, 0.25),
    0 0 0 1px rgba(230, 126, 34, 0.15);
}

[data-theme="dark"] .group-name {
  color: #e5eaf3;
}

[data-theme="dark"] .group-card:hover .group-name {
  color: #64b5f6;
}

[data-theme="dark"] .group-description {
  color: #a3a6ad;
}

[data-theme="dark"] .group-meta {
  color: #a3a6ad;
}

[data-theme="dark"] .creator-link {
  color: #64b5f6;
}

[data-theme="dark"] .creator-link:hover {
  color: #42a5f5;
}

[data-theme="dark"] .group-tags .tag {
  background-color: rgba(100, 181, 246, 0.1);
  border-color: rgba(100, 181, 246, 0.2);
  color: #a3a6ad;
}

[data-theme="dark"] .tag:hover {
  box-shadow: 0 4px 12px rgba(100, 181, 246, 0.3);
  background-color: rgba(100, 181, 246, 0.15);
}

[data-theme="dark"] .pulse-btn:hover {
  box-shadow: 0 4px 15px rgba(100, 181, 246, 0.4);
}

[data-theme="dark"] .creator-badge {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
}

[data-theme="dark"] .member-badge {
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 100%);
}
</style>
