<template>
    <div class="admin-page">
        <el-card>
            <template #header>
                <div class="card-header">
                    <h2>管理员控制台 - 互助信息管理</h2>
                </div>
            </template>

            <div class="filter-area">
                <el-form :inline="true" class="filter-form">
                    <el-form-item label="关键词">
                        <el-input v-model="filters.keyword" placeholder="标题/描述" clearable
                            @keyup.enter="handleSearch"></el-input>
                    </el-form-item>
                    <el-form-item label="类型">
                        <el-select v-model="filters.type" placeholder="类型" clearable>
                            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label"
                                :value="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="状态">
                        <el-select v-model="filters.status" placeholder="状态" clearable>
                            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label"
                                :value="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handleSearch">搜索</el-button>
                        <el-button @click="resetFilters">重置</el-button>
                    </el-form-item>
                </el-form>
            </div>

            <el-table v-loading="loading" :data="list" style="width: 100%" border>
                <el-table-column type="index" width="50" align="center"></el-table-column>
                <el-table-column prop="infoId" label="ID" width="80" align="center"></el-table-column>
                <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
                    <template #default="scope">
                        <router-link :to="`/helpinfo/${scope.row.infoId}`" class="info-title-link">
                            {{ scope.row.title }}
                        </router-link>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="120">
                    <template #default="scope">
                        <el-tag>{{ getTypeLabel(scope.row.type) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                        <el-tag :type="getStatusType(scope.row.status)">
                            {{ getStatusLabel(scope.row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="publisherName" label="发布者" width="120"></el-table-column>
                <el-table-column prop="createdAt" label="发布时间" width="180">
                    <template #default="scope">
                        {{ formatDate(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览量" width="80" align="center"></el-table-column>
                <el-table-column fixed="right" label="操作" width="220">
                    <template #default="scope">
                        <el-button-group>
                            <el-button type="info" size="small" link
                                @click="$router.push(`/helpinfo/${scope.row.infoId}`)">
                                查看
                            </el-button>
                            <el-button type="success" size="small" link v-if="scope.row.status === 'OPEN'"
                                @click="handleUpdateStatus(scope.row.infoId, 'IN_PROGRESS')">
                                标记处理中
                            </el-button>
                            <el-button type="warning" size="small" link
                                v-if="['OPEN', 'IN_PROGRESS'].includes(scope.row.status)"
                                @click="handleUpdateStatus(scope.row.infoId, 'RESOLVED')">
                                标记已解决
                            </el-button>
                            <el-button type="danger" size="small" link @click="confirmRemove(scope.row.infoId)">
                                删除
                            </el-button>
                        </el-button-group>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-area">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange">
                </el-pagination>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchHelpInfoList, deleteHelpInfo, updateHelpInfoStatus } from '../api/helpinfo'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const list = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 过滤条件
const filters = reactive({
    keyword: '',
    type: '',
    status: ''
})

// 类型选项
const typeOptions = [
    { value: 'COURSE_TUTORING', label: '课程辅导' },
    { value: 'SKILL_LEARNING', label: '技能学习' },
    { value: 'ITEM_LEND', label: '物品借用' },
    { value: 'ITEM_EXCHANGE', label: '物品交换' },
    { value: 'TEAM_UP', label: '组队合作' }
]

// 状态选项
const statusOptions = [
    { value: 'OPEN', label: '进行中' },
    { value: 'IN_PROGRESS', label: '处理中' },
    { value: 'RESOLVED', label: '已解决' },
    { value: 'CLOSED', label: '已关闭' },
    { value: 'EXPIRED', label: '已过期' }
]

onMounted(async () => {
    // 检查用户角色是否为管理员
    if (!authStore.user || !(authStore.user.roles || []).includes('ROLE_ADMIN')) {
        ElMessage.error('权限不足，请使用管理员账号登录')
        router.push('/')
        return
    }

    fetchList()
})

// 获取互助信息列表
async function fetchList() {
    loading.value = true

    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: filters.keyword || undefined,
            type: filters.type || undefined,
            status: filters.status || undefined
        }

        const res = await fetchHelpInfoList(params)

        if (res.data.code === 200) {
            list.value = res.data.data.records || res.data.data || []
            total.value = res.data.data.total || list.value.length
        } else {
            ElMessage.error(res.data.message || '获取列表失败')
        }
    } catch (error: any) {
        console.error('获取互助信息列表失败:', error)
        ElMessage.error('获取列表失败: ' + error.message)
    } finally {
        loading.value = false
    }
}

// 搜索
function handleSearch() {
    currentPage.value = 1
    fetchList()
}

// 重置过滤条件
function resetFilters() {
    Object.keys(filters).forEach(key => {
        filters[key as keyof typeof filters] = ''
    })
    currentPage.value = 1
    fetchList()
}

// 页码变化
function handleCurrentChange(val: number) {
    currentPage.value = val
    fetchList()
}

// 每页条数变化
function handleSizeChange(val: number) {
    pageSize.value = val
    currentPage.value = 1
    fetchList()
}

// 更新互助信息状态
async function handleUpdateStatus(id: number, status: string) {
    try {
        const res = await updateHelpInfoStatus(id, status)

        if (res.data.code === 200) {
            ElMessage.success(`状态已更新为${getStatusLabel(status)}`)
            fetchList()
        } else {
            ElMessage.error(res.data.message || '更新状态失败')
        }
    } catch (error: any) {
        ElMessage.error('更新状态失败: ' + error.message)
    }
}

// 确认删除
function confirmRemove(id: number) {
    ElMessageBox.confirm(
        '确定要删除这条互助信息吗？此操作不可恢复。',
        '警告',
        {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            const res = await deleteHelpInfo(id)

            if (res.data.code === 200) {
                ElMessage.success('删除成功')
                fetchList()
            } else {
                ElMessage.error(res.data.message || '删除失败')
            }
        } catch (error: any) {
            ElMessage.error('删除失败: ' + error.message)
        }
    }).catch(() => { })
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

// 获取类型标签
function getTypeLabel(type: string) {
    const map: Record<string, string> = {
        'COURSE_TUTORING': '课程辅导',
        'SKILL_LEARNING': '技能学习',
        'ITEM_LEND': '物品借用',
        'ITEM_EXCHANGE': '物品交换',
        'TEAM_UP': '组队合作'
    }
    return map[type] || type
}

// 获取状态标签
function getStatusLabel(status: string) {
    const map: Record<string, string> = {
        'OPEN': '进行中',
        'IN_PROGRESS': '处理中',
        'RESOLVED': '已解决',
        'CLOSED': '已关闭',
        'EXPIRED': '已过期'
    }
    return map[status] || status
}

// 获取状态类型颜色
function getStatusType(status: string) {
    const map: Record<string, string> = {
        'OPEN': 'success',
        'IN_PROGRESS': 'warning',
        'RESOLVED': 'info',
        'CLOSED': '',
        'EXPIRED': 'danger'
    }
    return map[status] || ''
}
</script>

<style scoped>
.admin-page {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.card-header h2 {
    margin: 0;
    color: #303133;
}

.filter-area {
    margin-bottom: 20px;
}

.filter-form {
    display: flex;
    flex-wrap: wrap;
}

.info-title-link {
    color: #409EFF;
    text-decoration: none;
}

.info-title-link:hover {
    text-decoration: underline;
}

.pagination-area {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
