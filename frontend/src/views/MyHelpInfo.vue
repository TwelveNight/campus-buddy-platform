<template>
    <div class="my-helpinfo-page">
        <el-tabs v-model="activeTab" class="tabbed-section">
            <el-tab-pane label="我发布的互助信息" name="published">
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <h3>我发布的互助信息</h3>
                            <el-button type="primary" @click="$router.push('/helpinfo/publish')">发布新互助</el-button>
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
                        <el-table-column label="操作" width="150" fixed="right">
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
                                </el-button-group>
                            </template>
                        </el-table-column>
                    </el-table>

                    <div class="empty-block" v-if="publishedList.length === 0 && !loading">
                        <el-empty description="您还没有发布任何互助信息"></el-empty>
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
                        <h3>我申请的互助</h3>
                    </template>

                    <el-table v-loading="loading" :data="appliedList" row-key="id" style="width: 100%">
                        <el-table-column prop="helpInfo.title" label="互助信息标题" min-width="160">
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
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchHelpInfoList, deleteHelpInfo } from '../api/helpinfo'
import { getMyApplications } from '../api/helpApplication'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('published')

// 已发布互助信息
const publishedList = ref<any[]>([])
const publishedPage = ref(1)
const publishedPageSize = ref(10)
const publishedTotal = ref(0)

// 已申请互助信息
const appliedList = ref<any[]>([])
const appliedPage = ref(1)
const appliedPageSize = ref(10)
const appliedTotal = ref(0)

onMounted(() => {
    loadMyPublishedHelpInfo()
    loadMyApplications()
})

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
            publishedList.value = res.data.data.records || res.data.data || []
            publishedTotal.value = res.data.data.total || publishedList.value.length
        } else {
            ElMessage.error(res.data.message || '获取数据失败')
        }
    } catch (e: any) {
        ElMessage.error(e.message || '获取数据失败')
    } finally {
        loading.value = false
    }
}

// 获取我的申请列表
async function loadMyApplications() {
    loading.value = true
    try {
        const res = await getMyApplications()
        if (res.data.code === 200) {
            appliedList.value = res.data.data.records || res.data.data || []
            appliedTotal.value = res.data.data.total || appliedList.value.length
        } else {
            ElMessage.error(res.data.message || '获取数据失败')
        }
    } catch (e: any) {
        ElMessage.error(e.message || '获取数据失败')
    } finally {
        loading.value = false
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
function formatDate(dateString: string | Date) {
    if (!dateString) return ''
    try {
        const date = typeof dateString === 'string' ? new Date(dateString) : dateString
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        })
    } catch (error) {
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
</script>

<style scoped>
.my-helpinfo-page {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
}

.tabbed-section {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-header h3 {
    margin: 0;
}

.title-link {
    color: #409EFF;
    text-decoration: none;
}

.title-link:hover {
    text-decoration: underline;
}

.empty-block {
    padding: 30px 0;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
