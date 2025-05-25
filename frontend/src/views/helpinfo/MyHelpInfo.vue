<template>
    <div class="my-helpinfo-page">
        <el-tabs v-model="activeTab" class="tabbed-section">
            <el-tab-pane label="我发布的互助任务" name="published">
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <h3 class="section-title">我发布的互助任务</h3>
                            <div>
                                <el-button type="primary" icon="Plus"
                                    @click="$router.push('/helpinfo/publish')">发布新互助</el-button>
                            </div>
                        </div>
                    </template>

                    <el-table v-loading="loading" :data="publishedList" row-key="infoId" style="width: 100%">
                        <el-table-column prop="title" label="标题" min-width="160">
                            <template #default="scope">
                                <router-link :to="`/helpinfo/${scope.row.infoId}`" class="title-link">
                                    {{ scope.row.title }}
                                </router-link>
                            </template>
                        </el-table-column>
                        <el-table-column prop="type" label="类型" width="120">
                            <template #default="scope">
                                <el-tag>{{ getTypeLabel(scope.row.type) }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="status" label="状态" width="120">
                            <template #default="scope">
                                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status)
                                    }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="createdAt" label="发布时间" width="180">
                            <template #default="scope">
                                {{ formatDate(scope.row.createdAt) }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="viewCount" label="浏览数" width="80" align="center"></el-table-column>
                        <el-table-column label="操作" width="180" fixed="right">
                            <template #default="scope">
                                <el-button-group>
                                    <el-button type="primary" size="small" link
                                        @click="$router.push(`/helpinfo/${scope.row.infoId}`)">
                                        查看
                                    </el-button>
                                    <el-button type="danger" size="small" link @click="confirmDelete(scope.row.infoId)"
                                        v-if="scope.row.status === 'OPEN'">
                                        删除
                                    </el-button>
                                    <el-button
                                        v-if="scope.row.status === 'RESOLVED' && scope.row.acceptedApplicationId && scope.row.canPublisherReview === true"
                                        type="success" size="small" link @click="openReviewDialog(scope.row)">
                                        评价帮助者
                                    </el-button>
                                    <el-tag
                                        v-else-if="scope.row.status === 'RESOLVED' && scope.row.acceptedApplicationId && scope.row.publisherHasReviewed === true"
                                        type="info" size="small">
                                        已评价
                                    </el-tag>
                                </el-button-group>
                            </template>
                        </el-table-column>
                    </el-table>

                    <div class="empty-block" v-if="publishedList.length === 0 && !loading">
                        <el-empty description="您还没有发布任何互助任务"></el-empty>
                    </div>

                    <div class="pagination-container" v-if="publishedList.length > 0">
                        <el-pagination v-model:current-page="publishedPage" v-model:page-size="publishedPageSize"
                            :page-sizes="[5, 10, 20]" :total="publishedTotal"
                            layout="total, sizes, prev, pager, next, jumper"
                            @size-change="handleSizeChange('published')"
                            @current-change="handleCurrentChange('published')" />
                    </div>
                </el-card>
            </el-tab-pane>

            <el-tab-pane label="我申请的互助" name="applied">
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <h3 class="section-title">我申请的互助</h3>
                        </div>
                    </template>

                    <el-table v-loading="loading" :data="appliedList" row-key="id" style="width: 100%">
                        <el-table-column prop="helpInfo.title" label="互助任务标题" min-width="160">
                            <template #default="scope">
                                <router-link :to="`/helpinfo/${scope.row.infoId}`" class="title-link">
                                    {{ scope.row.helpInfo?.title }}
                                </router-link>
                            </template>
                        </el-table-column>
                        <el-table-column prop="message" label="申请消息" min-width="200"
                            :show-overflow-tooltip="true"></el-table-column>
                        <el-table-column prop="status" label="申请状态" width="120">
                            <template #default="scope">
                                <el-tag :type="getApplicationStatusType(scope.row.status)">
                                    {{ getApplicationStatusLabel(scope.row.status) }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="createdAt" label="申请时间" width="180">
                            <template #default="scope">
                                {{ formatDate(scope.row.createdAt) }}
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="180" fixed="right">
                            <template #default="scope">
                                <el-button-group>
                                    <el-button type="primary" size="small" link
                                        @click="$router.push(`/helpinfo/${scope.row.infoId}`)">
                                        查看
                                    </el-button>
                                    <el-button
                                        v-if="scope.row.status === 'ACCEPTED' && scope.row.helpInfo?.status === 'RESOLVED' && scope.row.canHelperReview === true"
                                        type="success" size="small" link @click="openReviewPublisherDialog(scope.row)">
                                        评价发布者
                                    </el-button>
                                    <el-tag
                                        v-else-if="scope.row.status === 'ACCEPTED' && scope.row.helpInfo?.status === 'RESOLVED' && scope.row.helperHasReviewed === true"
                                        type="info" size="small">
                                        已评价
                                    </el-tag>
                                </el-button-group>
                            </template>
                        </el-table-column>
                    </el-table>

                    <div class="empty-block" v-if="appliedList.length === 0 && !loading">
                        <el-empty description="您还没有申请过任何互助"></el-empty>
                    </div>

                    <div class="pagination-container" v-if="appliedList.length > 0">
                        <el-pagination v-model:current-page="appliedPage" v-model:page-size="appliedPageSize"
                            :page-sizes="[5, 10, 20]" :total="appliedTotal"
                            layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange('applied')"
                            @current-change="handleCurrentChange('applied')" />
                    </div>
                </el-card>
            </el-tab-pane>
        </el-tabs>
        <ReviewDialog v-if="reviewDialogVisible" :visible="reviewDialogVisible"
            :help-info-id="currentSelectedInfo?.helpInfoId" :reviewed-user-id="currentSelectedInfo?.reviewedUserId"
            :reviewer-user-id="currentSelectedInfo?.reviewerUserId"
            :review-type="currentSelectedInfo?.reviewType || 'PUBLISHER_TO_HELPER'"
            :title="currentSelectedInfo?.title || '评价帮助者'" @update:visible="reviewDialogVisible = $event"
            @review-submitted="handleReviewSubmitted" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { fetchHelpInfoList, deleteHelpInfo } from "../../api/helpinfo";
import { useApplicationStore } from "../../store/application";
import ReviewDialog from "../../components/form/ReviewDialog.vue";
import { canReview, getHelpInfoReviewStatus, getUserReviewStatus } from '../../api/review';
import { useAuthStore } from '../../store/auth';
import { getApplicationById } from '../../api/helpApplication';

const router = useRouter()
const loading = ref(false)
const activeTab = ref('published')

// 已发布互助信息
const publishedList = ref<any[]>([])
const publishedPage = ref(1)
const publishedPageSize = ref(10)
const publishedTotal = ref(0)

// 已申请互助信息（由store管理）
const appliedList = ref<any[]>([])
const appliedPage = ref(1)
const appliedPageSize = ref(10)
const appliedTotal = ref(0)

const applicationStore = useApplicationStore()
const authStore = useAuthStore()
const reviewDialogVisible = ref(false)
const currentSelectedInfo = ref<any>(null)

onMounted(() => {
    loadMyPublishedHelpInfo()
    loadMyApplications()
    initReviewStatus()
})

// 初始化评价状态（不再需要从本地存储恢复，因为使用后端API）
function initReviewStatus() {
    // 该函数保留但不执行任何操作，因为我们现在使用后端API获取评价状态
    console.log('评价状态初始化 - 现在使用后端API获取评价状态');
}

// 调试函数：在每次加载或更新数据后打印评价状态
function debugReviewStatus() {
    console.log('=== 评价状态调试信息 ===');

    // 查看已发布互助信息的评价状态
    console.log('已发布互助信息评价状态:');
    publishedList.value.forEach(item => {
        console.log(`互助ID: ${item.infoId}, 标题: ${item.title}, 状态: ${item.status}, 可评价: ${item.canPublisherReview}, 已评价: ${item.publisherHasReviewed}`);
    });

    // 查看已申请互助信息的评价状态
    console.log('已申请互助信息评价状态:');
    appliedList.value.forEach(item => {
        console.log(`申请ID: ${item.id}, 互助ID: ${item.infoId}, 状态: ${item.status}, 可评价: ${item.canHelperReview}, 已评价: ${item.helperHasReviewed}`);
    });

    console.log('=== 调试信息结束 ===');
}

// 获取我发布的互助信息
async function loadMyPublishedHelpInfo() {
    loading.value = true
    try {
        const res = await fetchHelpInfoList({
            page: publishedPage.value,
            size: publishedPageSize.value,
            publisherId: 'my' // 这里需要后端支持获取当前用户发布的互助信息
        })
        if (res.data.code === 200) {
            let records = res.data.data.records || res.data.data || [];

            // 处理每条记录的评价状态 - 使用后端API获取准确的评价状态
            for (const record of records) {
                if (record.status === 'RESOLVED' && record.acceptedApplicationId) {
                    try {
                        // 从后端获取用户对该互助信息的评价状态
                        const currentUserId = authStore.user?.userId;
                        if (!currentUserId) continue;

                        const statusRes = await getUserReviewStatus(currentUserId, record.infoId);
                        if (statusRes && statusRes.data) {
                            record.canPublisherReview = statusRes.data.canPublisherReview;
                            record.publisherHasReviewed = statusRes.data.publisherHasReviewed;
                        }
                    } catch (error) {
                        console.error('获取评价状态失败:', error);
                        // 出错时设置默认值
                        record.publisherHasReviewed = false;
                        record.canPublisherReview = false;
                    }
                } else {
                    // 非RESOLVED状态不能评价
                    record.canPublisherReview = false;
                }
            }

            publishedList.value = records;
            publishedTotal.value = res.data.data.total || records.length;

            console.log('加载的互助信息列表:', publishedList.value);
        } else {
            ElMessage.error(res.data.message || '获取数据失败')
        }
    } catch (e: any) {
        ElMessage.error(e.message || '获取数据失败')
    } finally {
        loading.value = false
        debugReviewStatus()
    }
}

// 获取我的申请列表（用store自动补全helpInfo）
async function loadMyApplications() {
    loading.value = true
    try {
        await applicationStore.fetchMyApplications()

        // 处理每条申请的评价状态
        const applications = [...applicationStore.myApplications];
        for (const app of applications) {
            if (app.status === 'ACCEPTED' && app.helpInfo?.status === 'RESOLVED' && app.infoId) {
                try {
                    // 从后端获取用户对该互助信息的评价状态
                    const currentUserId = authStore.user?.userId;
                    if (!currentUserId) continue;

                    const statusRes = await getUserReviewStatus(currentUserId, app.infoId);
                    if (statusRes && statusRes.data) {
                        app.canHelperReview = statusRes.data.canHelperReview;
                        app.helperHasReviewed = statusRes.data.helperHasReviewed;
                    }
                } catch (error) {
                    console.error('获取评价状态失败:', error);                        // 出错时设置默认值
                    app.helperHasReviewed = false;
                    app.canHelperReview = false;
                }
            } else {
                // 非ACCEPTED状态或互助非RESOLVED状态不能评价
                app.canHelperReview = false;
            }
        }

        appliedList.value = applications;
        appliedTotal.value = applications.length;

        console.log('加载的申请列表:', appliedList.value);
    } catch (e: any) {
        ElMessage.error(e.message || '获取数据失败')
    } finally {
        loading.value = false
        debugReviewStatus()
    }
}

// 确认删除互助信息
function confirmDelete(id: number) {
    ElMessageBox.confirm('确定要删除该互助信息吗？此操作不可恢复。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            const res = await deleteHelpInfo(id)
            if (res.data.code === 200) {
                ElMessage.success('删除成功')
                // 重新加载列表
                loadMyPublishedHelpInfo()
            } else {
                ElMessage.error(res.data.message || '删除失败')
            }
        } catch (e: any) {
            ElMessage.error(e.message || '删除失败')
        }
    }).catch(() => { })
}

// 分页大小改变
function handleSizeChange(type: 'published' | 'applied') {
    if (type === 'published') {
        publishedPage.value = 1
        loadMyPublishedHelpInfo()
    } else {
        appliedPage.value = 1
        loadMyApplications()
    }
}

// 页码改变
function handleCurrentChange(type: 'published' | 'applied') {
    if (type === 'published') {
        loadMyPublishedHelpInfo()
    } else {
        loadMyApplications()
    }
}

// 格式化日期
function formatDate(dateString: string | Date | number) {
    if (!dateString) return ''
    try {
        // 处理数字类型的时间戳（毫秒）
        const date = typeof dateString === 'number' ? new Date(dateString) :
            (typeof dateString === 'string' ? new Date(dateString) : dateString)
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        })
    } catch (error) {
        console.error('日期格式化错误:', error, dateString)
        return String(dateString)
    }
}

// 获取类型显示标签
function getTypeLabel(type: string) {
    const typeMap: Record<string, string> = {
        'COURSE_TUTORING': '课程辅导',
        'SKILL_LEARNING': '技能学习',
        'ITEM_LEND': '物品借用',
        'ITEM_EXCHANGE': '物品交换',
        'TEAM_UP': '组队合作'
    }
    return typeMap[type] || type
}

// 获取状态显示类型
function getStatusType(status: string) {
    const statusMap: Record<string, string> = {
        'OPEN': 'success',
        'IN_PROGRESS': 'warning',
        'RESOLVED': 'info',
        'CLOSED': '',
        'EXPIRED': 'danger'
    }
    return statusMap[status] || ''
}

// 获取状态显示标签
function getStatusLabel(status: string) {
    const statusMap: Record<string, string> = {
        'OPEN': '进行中',
        'IN_PROGRESS': '处理中',
        'RESOLVED': '已解决',
        'CLOSED': '已关闭',
        'EXPIRED': '已过期'
    }
    return statusMap[status] || status
}

// 获取申请状态显示类型
function getApplicationStatusType(status: string) {
    const statusMap: Record<string, string> = {
        'PENDING': 'warning',
        'ACCEPTED': 'success',
        'REJECTED': 'info'
    }
    return statusMap[status] || ''
}

// 获取申请状态显示标签
function getApplicationStatusLabel(status: string) {
    const statusMap: Record<string, string> = {
        'PENDING': '待处理',
        'ACCEPTED': '已接受',
        'REJECTED': '已拒绝'
    }
    return statusMap[status] || status
}

// 判断是否显示“评价发布者”按钮
function canShowReviewPublisherBtn(row: any) {
    return (row.canHelperReview === true || row._canHelperReview === true) && !reviewedApplications.value[row.id]
}

// 打开评价帮助者弹窗
async function openReviewDialog(row: any) {
    // 从后端获取最新的评价状态
    const currentUserId = authStore.user?.userId;
    if (!currentUserId) {
        ElMessage.warning('用户未登录，无法评价');
        return;
    }

    try {
        const statusRes = await getUserReviewStatus(currentUserId, row.infoId);
        if (statusRes && statusRes.data) {
            // 如果后端返回不能评价或已评价，则拒绝评价
            if (!statusRes.data.canPublisherReview) {
                if (statusRes.data.publisherHasReviewed) {
                    ElMessage.warning('您已评价过该帮助者');
                } else {
                    ElMessage.warning('当前无法评价该帮助者');
                }
                return;
            }
        }
    } catch (error) {
        console.error('获取评价状态失败:', error);
        // 出错时使用本地状态判断
        if (row.publisherHasReviewed === true) {
            ElMessage.warning('您已评价过该帮助者');
            return;
        }

        if (row.canPublisherReview === false) {
            ElMessage.warning('当前无法评价该帮助者');
            return;
        }
    }

    // 确保状态为已解决且有已接受的申请
    if (row.status !== 'RESOLVED' || !row.acceptedApplicationId) {
        ElMessage.warning('当前互助状态不允许评价');
        return;
    }

    let helperId = null;
    const acceptedApplicationId = row.acceptedApplicationId || row.helpInfo?.acceptedApplicationId;
    if (acceptedApplicationId && Array.isArray(row.applications)) {
        const acceptedApp = row.applications.find((app: any) => app.id === acceptedApplicationId);
        if (acceptedApp) helperId = acceptedApp.applicantId;
    } else if (acceptedApplicationId && Array.isArray(applicationStore.myApplications)) {
        const acceptedApp = applicationStore.myApplications.find((app: any) => app.id === acceptedApplicationId);
        if (acceptedApp) helperId = acceptedApp.applicantId;
    }
    // 如果本地没有，尝试异步获取
    if (!helperId && acceptedApplicationId) {
        try {
            const res = await getApplicationById(acceptedApplicationId);
            if (res.data && res.data.data && res.data.data.applicantId) {
                helperId = res.data.data.applicantId;
            }
        } catch (e) {
            // 忽略，后面兜底
        }
    }
    // 兜底：兼容旧数据结构
    if (!helperId) {
        helperId = row.acceptedApplicantId || row.acceptedUserId || row.acceptedApplicant || row.helperId;
    }
    if (!helperId) {
        ElMessage.warning('无法获取帮助者信息，请稍后重试');
        return;
    }
    currentSelectedInfo.value = {
        helpInfoId: row.infoId,
        reviewedUserId: helperId,
        reviewerUserId: authStore.user?.userId,
        reviewType: 'PUBLISHER_TO_HELPER',
        title: '评价帮助者',
        applicationId: row.id
    }
    reviewDialogVisible.value = true
}

// 打开评价发布者弹窗（新加）
async function openReviewPublisherDialog(row: any) {
    // 从后端获取最新的评价状态
    const currentUserId = authStore.user?.userId;
    if (!currentUserId) {
        ElMessage.warning('用户未登录，无法评价');
        return;
    }

    try {
        const statusRes = await getUserReviewStatus(currentUserId, row.infoId);
        if (statusRes && statusRes.data) {
            // 如果后端返回不能评价或已评价，则拒绝评价
            if (!statusRes.data.canHelperReview) {
                if (statusRes.data.helperHasReviewed) {
                    ElMessage.warning('您已评价过该发布者');
                } else {
                    ElMessage.warning('当前无法评价该发布者');
                }
                return;
            }
        }
    } catch (error) {
        console.error('获取评价状态失败:', error);
        // 出错时使用本地状态判断
        if (row.helperHasReviewed === true) {
            ElMessage.warning('您已评价过该发布者');
            return;
        }

        if (row.canHelperReview === false) {
            ElMessage.warning('当前无法评价该发布者');
            return;
        }
    }

    // 确保申请状态为已接受且互助状态为已解决
    if (row.status !== 'ACCEPTED' || row.helpInfo?.status !== 'RESOLVED') {
        ElMessage.warning('当前互助状态不允许评价');
        return;
    }

    // 确保获取到有效的publisherId
    const publisherId = row.helpInfo?.publisherId;
    if (!publisherId) {
        ElMessage.warning('无法获取发布者信息，请稍后重试');
        return;
    }

    currentSelectedInfo.value = {
        helpInfoId: row.infoId,
        reviewedUserId: publisherId,
        reviewerUserId: authStore.user?.userId,
        reviewType: 'HELPER_TO_PUBLISHER',
        title: '评价发布者',
        applicationId: row.id
    }
    reviewDialogVisible.value = true
}

// 处理评价提交
async function handleReviewSubmitted() {
    reviewDialogVisible.value = false

    if (currentSelectedInfo.value) {
        if (currentSelectedInfo.value.reviewType === 'PUBLISHER_TO_HELPER' && currentSelectedInfo.value.helpInfoId) {
            // 重新加载发布的互助数据（从后端获取最新评价状态）
            await loadMyPublishedHelpInfo();
        } else if (currentSelectedInfo.value.reviewType === 'HELPER_TO_PUBLISHER' && currentSelectedInfo.value.helpInfoId) {
            // 重新加载申请的互助数据（从后端获取最新评价状态）
            // 先清空列表，确保UI显示加载状态
            appliedList.value = [];
            // 强制重新获取评价状态
            await loadMyApplications();

            // 立即更新当前项的评价状态，提供更好的用户体验
            if (currentSelectedInfo.value.applicationId) {
                const appId = currentSelectedInfo.value.applicationId;
                const updatedApp = appliedList.value.find(item => (item.id === appId || item.applicationId === appId));
                if (updatedApp) {
                    updatedApp.helperHasReviewed = true;
                    updatedApp.canHelperReview = false;
                }
            }
        }
    }

    currentSelectedInfo.value = null
}

// 监听弹窗关闭，重置 currentSelectedInfo，防止数据残留
watch(
    () => reviewDialogVisible.value,
    (val) => {
        if (!val) currentSelectedInfo.value = null
    }
)

// 监听Tab切换，重新加载对应数据
watch(
    () => activeTab.value,
    (newTab) => {
        if (newTab === 'published') {
            loadMyPublishedHelpInfo();
        } else if (newTab === 'applied') {
            loadMyApplications();
        }
    }
)
</script>

<style scoped>
.my-helpinfo-page {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
    animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.tabbed-section {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.el-tabs__nav-wrap::after {
    height: 1px;
    background-color: #ebeef5;
}

.el-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0,0.05);
    margin-bottom: 20px;
    transition: all 0.3s ease;
}

.el-card:hover {
    box-shadow: 0 4px 18px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
}

.section-title {
    margin: 0;
    font-size: 18px;
    color: #303133;
    font-weight: 600;
    position: relative;
    padding-left: 12px;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 16px;
    background-color: #409EFF;
    border-radius: 2px;
}

.title-link {
    color: #409EFF;
    text-decoration: none;
    font-weight: 500;
    transition: all 0.3s ease;
}

.title-link:hover {
    color: #66b1ff;
    text-decoration: underline;
}

.el-table {
    border-radius: 8px;
    overflow: hidden;
}

.empty-block {
    padding: 40px 0;
    background-color: #fafafa;
    border-radius: 8px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding: 10px 0;
}

.el-tag {
    border-radius: 4px;
    padding: 0 8px;
    height: 24px;
    line-height: 22px;
    font-size: 12px;
}

.el-button-group .el-button {
    margin-right: 8px;
}

.el-button-group .el-button:last-child {
    margin-right: 0;
}
</style>
