<template>
    <div class="helpinfo-list-page">
        <div class="page-header animate-enter">
            <h2 class="page-title"><span class="title-icon">📋</span>校园互助</h2>
            <el-button type="primary" @click="$router.push('/helpinfo/publish')" class="publish-btn">
                <i class="el-icon-plus"></i> 发布互助任务
            </el-button>
        </div>

        <el-card class="main-card animate-enter">
            <template #header>
                <div class="filter-section">
                    <div class="search-container">
                        <el-input v-model="searchKeyword" placeholder="搜索关键词" clearable class="search-input"
                            @keyup.enter="fetchData">
                            <template #prefix>
                                <el-icon>
                                    <search />
                                </el-icon>
                            </template>
                            <template #append>
                                <el-button @click="fetchData">搜索</el-button>
                            </template>
                        </el-input>
                    </div>

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
                        <router-link
                            v-if="scope.row.params && scope.row.params.publisherName"
                            :to="`/user/${scope.row.publisherId}`"
                            class="publisher-link"
                        >
                            {{ scope.row.params.publisherName }}
                        </router-link>
                        <router-link
                            v-else
                            :to="`/user/${scope.row.publisherId}`"
                            class="publisher-link"
                        >
                            用户{{ scope.row.publisherId }}
                        </router-link>
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
import { fetchHelpInfoList } from '../../api/helpinfo'
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
  padding: 20px;
  transition: all 0.3s ease;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 8px;
}

.page-title {
  font-size: 28px;
  margin: 0;
  display: flex;
  align-items: center;
  background: linear-gradient(45deg, var(--primary-color), var(--el-color-primary-light-3));
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 0 15px rgba(var(--el-color-primary-rgb), 0.25);
  position: relative;
}

.title-icon {
  margin-right: 8px;
  animation: float 3s ease-in-out infinite;
  position: relative;
}

.publish-btn {
  transform: translateY(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.2);
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  border: none;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(var(--el-color-primary-rgb), 0.3);
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary));
}

.main-card {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: 1px solid rgba(var(--el-color-primary-rgb), 0.1);
  animation: fadeIn 0.6s ease-out;
}

[data-theme="dark"] .main-card {
  background-color: rgba(30, 30, 30, 0.7) !important;
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 8px;
  align-items: center;
  background: linear-gradient(to right, rgba(var(--el-color-primary-rgb), 0.05), transparent);
  border-radius: 8px;
}

[data-theme="dark"] .filter-section {
  background: linear-gradient(to right, rgba(var(--el-color-primary-rgb), 0.1), rgba(30, 30, 30, 0.2));
}

.search-container {
  flex: 1;
  min-width: 280px;
}

.search-input {
  transition: all 0.3s ease;
}

.search-input:hover .el-input__inner,
.search-input:focus-within .el-input__inner {
  box-shadow: 0 0 0 2px rgba(var(--el-color-primary-rgb), 0.2);
}

.filter-select {
  min-width: 120px;
}

.info-table {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
}

[data-theme="dark"] .info-table {
  --el-table-bg-color: rgba(30, 30, 30, 0.6) !important;
  --el-table-border-color: rgba(255, 255, 255, 0.1) !important;
  --el-table-header-bg-color: rgba(40, 40, 40, 0.8) !important;
  --el-table-header-text-color: #ffffff !important;
  --el-table-row-hover-bg-color: rgba(var(--el-color-primary-rgb), 0.15) !important;
}

.title-link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  display: block;
  padding: 4px 0;
  position: relative;
}

.title-link:hover {
  color: var(--el-color-primary-light-3);
  transform: translateX(4px);
}

.title-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--el-color-primary), transparent);
  transition: width 0.3s ease;
}

.title-link:hover::after {
  width: 100%;
}

.publisher-link {
  color: var(--el-color-primary);
  text-decoration: none;
  transition: all 0.2s ease;
}

.publisher-link:hover {
  color: var(--el-color-primary-light-3);
  text-decoration: underline;
}

.animate-enter {
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .helpinfo-list-page {
    padding: 16px 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-container {
    width: 100%;
  }
  
  .filter-select {
    width: 100%;
  }
}
</style>
