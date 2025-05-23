<template>
    <div class="helpinfo-list-page">
        <div class="page-header animate-enter">
            <h2 class="page-title"><span class="title-icon">📋</span>互助信息列表</h2>
            <el-button type="primary" @click="$router.push('/helpinfo/publish')" class="publish-btn">
                <i class="el-icon-plus"></i> 发布互助信息
            </el-button>
        </div>

        <el-card class="main-card animate-enter">
            <template #header>
                <div class="filter-section">
                    <el-input v-model="searchKeyword" placeholder="搜索关键词" clearable class="search-input"
                        @keyup.enter="fetchData">
                        <template #prefix>
                            <el-icon>
                                <search />
                            </el-icon>
                        </template>
                    </el-input>

                    <el-select v-model="filterType" placeholder="类型" clearable class="filter-select"
                        @change="fetchData">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="课程辅导" value="COURSE_TUTORING"></el-option>
                        <el-option label="技能学习" value="SKILL_LEARNING"></el-option>
                        <el-option label="物品借用" value="ITEM_LEND"></el-option>
                        <el-option label="物品交换" value="ITEM_EXCHANGE"></el-option>
                        <el-option label="组队合作" value="TEAM_UP"></el-option>
                    </el-select>

                    <el-select v-model="filterStatus" placeholder="状态" clearable class="filter-select"
                        @change="fetchData">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="进行中" value="OPEN"></el-option>
                        <el-option label="处理中" value="IN_PROGRESS"></el-option>
                        <el-option label="已解决" value="RESOLVED"></el-option>
                        <el-option label="已关闭" value="CLOSED"></el-option>
                    </el-select>
                </div>
            </template>

            <el-table v-loading="loading" :data="list" style="width: 100%" row-key="infoId" class="info-table">
                <el-table-column prop="title" label="标题" min-width="180">
                    <template #default="scope">
                        <router-link :to="`/helpinfo/${scope.row.infoId}`" class="title-link">
                            {{ scope.row.title }}
                        </router-link>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="100">
                    <template #default="scope">
                        <el-tag>{{ getTypeLabel(scope.row.type) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                        <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="expectedTime" label="时间" width="120"
                    :show-overflow-tooltip="true"></el-table-column>
                <el-table-column prop="expectedLocation" label="地点" width="120"
                    :show-overflow-tooltip="true"></el-table-column>
                <el-table-column prop="publisherId" label="发布者" width="100">
                    <template #default="scope">
                        <span v-if="scope.row.params && scope.row.params.publisherName">{{
                            scope.row.params.publisherName }}</span>
                        <span v-else>用户{{ scope.row.publisherId }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="发布时间" width="180">
                    <template #default="scope">
                        {{ formatDate(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览数" width="80" align="center"></el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                    <template #default="scope">
                        <el-button type="primary" size="small" link
                            @click="$router.push(`/helpinfo/${scope.row.infoId}`)">
                            查看详情
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchHelpInfoList } from '../api/helpinfo'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const list = ref<any[]>([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

onMounted(() => {
    fetchData()
})

async function fetchData() {
    loading.value = true
    try {
        const res = await fetchHelpInfoList({
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchKeyword.value || undefined,
            type: filterType.value || undefined,
            status: filterStatus.value || undefined
        })
        if (res.data.code === 200) {
            list.value = res.data.data.records || res.data.data || []
            total.value = res.data.data.total || list.value.length
        } else {
            ElMessage.error(res.data.message || '获取数据失败')
        }
    } catch (e: any) {
        ElMessage.error(e.message || '获取数据失败')
    } finally {
        loading.value = false
    }
}

function handleSizeChange(val: number) {
    pageSize.value = val
    currentPage.value = 1
    fetchData()
}

function handleCurrentChange(val: number) {
    currentPage.value = val
    fetchData()
}

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
</script>

<style scoped>
.helpinfo-list-page {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
    animation: fadeIn 0.5s ease-out;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
}

.page-title {
    font-size: 1.8rem;
    color: #303133;
    font-weight: 600;
    position: relative;
    padding-left: 15px;
    margin: 0;
    border-left: 4px solid #409EFF;
    display: flex;
    align-items: center;
}

.title-icon {
    margin-right: 8px;
    font-size: 1.5rem;
}

.main-card {
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.main-card:hover {
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.filter-section {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    align-items: center;
}

.search-input {
    width: 220px;
    min-width: 200px;
}

.filter-select {
    width: 150px;
    min-width: 120px;
}

.info-table {
    border-radius: 8px;
    overflow: hidden;
}

.info-table :deep(th) {
    background-color: #f5f7fa;
    font-weight: 600;
    color: #303133;
}

.info-table :deep(tr:hover) {
    background-color: #f0f9ff;
}

.info-table :deep(.el-table__cell) {
    padding: 12px 0;
}

.title-link {
    color: #409EFF;
    text-decoration: none;
    transition: all 0.2s;
    font-weight: 500;
    display: inline-block;
}

.title-link:hover {
    text-decoration: underline;
    color: #337ecc;
    transform: translateX(2px);
}

.pagination-container {
    margin-top: 25px;
    display: flex;
    justify-content: flex-end;
}

/* 按钮样式优化 */
.publish-btn {
    border-radius: 20px;
    padding: 10px 20px;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.publish-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 操作按钮样式 */
.info-table :deep(.el-button--small.is-link) {
    font-weight: 500;
    transition: all 0.2s;
}

.info-table :deep(.el-button--small.is-link:hover) {
    transform: scale(1.05);
}

/* 标签样式优化 */
.info-table :deep(.el-tag) {
    border-radius: 16px;
    padding: 0 10px;
    height: 26px;
    line-height: 26px;
    font-size: 12px;
    font-weight: 500;
    margin: 2px;
}

/* 动画 */
.animate-enter {
    animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
    0% {
        opacity: 0;
        transform: translateY(10px);
    }
    100% {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .helpinfo-list-page {
        padding: 0 15px;
    }
    
    .page-header {
        flex-direction: column;
        gap: 15px;
        align-items: stretch;
        text-align: center;
    }
    
    .filter-section {
        flex-direction: column;
        gap: 10px;
    }
    
    .search-input,
    .filter-select {
        width: 100%;
    }
    
    .pagination-container {
        justify-content: center;
    }
}
</style>
