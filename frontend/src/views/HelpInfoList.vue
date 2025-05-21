<template>
    <div class="helpinfo-list-page">
        <div class="page-header">
            <h2>互助信息列表</h2>
            <el-button type="primary" @click="$router.push('/helpinfo/publish')">发布互助信息</el-button>
        </div>

        <el-card>
            <template #header>
                <div class="filter-section">
                    <el-input v-model="searchKeyword" placeholder="搜索关键词" clearable style="width: 200px"
                        @keyup.enter="fetchData">
                        <template #prefix>
                            <el-icon>
                                <search />
                            </el-icon>
                        </template>
                    </el-input>

                    <el-select v-model="filterType" placeholder="类型" clearable style="width: 150px" @change="fetchData">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="课程辅导" value="COURSE_TUTORING"></el-option>
                        <el-option label="技能交换" value="SKILL_EXCHANGE"></el-option>
                        <el-option label="物品借用" value="ITEM_BORROW"></el-option>
                    </el-select>

                    <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 150px"
                        @change="fetchData">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="进行中" value="OPEN"></el-option>
                        <el-option label="处理中" value="IN_PROGRESS"></el-option>
                        <el-option label="已解决" value="RESOLVED"></el-option>
                        <el-option label="已关闭" value="CLOSED"></el-option>
                    </el-select>
                </div>
            </template>

            <el-table v-loading="loading" :data="list" style="width: 100%">
                <el-table-column prop="title" label="标题" min-width="200">
                    <template #default="scope">
                        <router-link :to="`/helpinfo/${scope.row.id}`" class="title-link">
                            {{ scope.row.title }}
                        </router-link>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="120"></el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                    <template #default="scope">
                        <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="publisherNickname" label="发布者" width="120"></el-table-column>
                <el-table-column prop="createdAt" label="发布时间" width="180"></el-table-column>
                <el-table-column prop="viewCount" label="浏览数" width="80" align="center"></el-table-column>
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
</script>

<style scoped>
.helpinfo-list-page {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.filter-section {
    display: flex;
    gap: 15px;
}

.title-link {
    color: #409EFF;
    text-decoration: none;
}

.title-link:hover {
    text-decoration: underline;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
