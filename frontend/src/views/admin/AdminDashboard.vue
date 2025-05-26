<template>
  <div class="admin-dashboard">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>管理员控制台 - 数据统计</h2>
          <div class="header-actions">
            <el-button type="primary" @click="refreshData">刷新数据</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 顶部概览卡片 -->
        <div class="overview-cards">
          <el-row :gutter="20">
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <el-card shadow="hover" class="overview-card users-card">
                <template #header>
                  <div class="card-header-small">
                    <span>用户总数</span>
                  </div>
                </template>
                <div class="overview-value">
                  <el-statistic :value="overview.totalUsers">
                    <template #suffix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-statistic>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <el-card shadow="hover" class="overview-card posts-card">
                <template #header>
                  <div class="card-header-small">
                    <span>帖子总数</span>
                  </div>
                </template>
                <div class="overview-value">
                  <el-statistic :value="overview.totalPosts">
                    <template #suffix>
                      <el-icon><Document /></el-icon>
                    </template>
                  </el-statistic>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <el-card shadow="hover" class="overview-card comments-card">
                <template #header>
                  <div class="card-header-small">
                    <span>评论总数</span>
                  </div>
                </template>
                <div class="overview-value">
                  <el-statistic :value="overview.totalComments">
                    <template #suffix>
                      <el-icon><ChatDotRound /></el-icon>
                    </template>
                  </el-statistic>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <el-card shadow="hover" class="overview-card groups-card">
                <template #header>
                  <div class="card-header-small">
                    <span>群组总数</span>
                  </div>
                </template>
                <div class="overview-value">
                  <el-statistic :value="overview.totalGroups">
                    <template #suffix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-statistic>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 用户数据部分 -->
        <div class="data-section">
          <h3>用户数据</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <div class="chart-container">
                <h4>用户状态分布</h4>
                <div ref="userStatusChartRef" class="chart"></div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <div class="chart-container">
                <h4>最近7天注册用户</h4>
                <div ref="userRegistrationChartRef" class="chart"></div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 帖子数据部分 -->
        <div class="data-section">
          <h3>帖子数据</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <div class="chart-container">
                <h4>帖子状态分布</h4>
                <div ref="postStatusChartRef" class="chart"></div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <div class="chart-container">
                <h4>最近7天发帖趋势</h4>
                <div ref="postTrendChartRef" class="chart"></div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 群组数据部分 -->
        <div class="data-section">
          <h3>群组数据</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <div class="chart-container">
                <h4>群组状态分布</h4>
                <div ref="groupStatusChartRef" class="chart"></div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <div class="chart-container">
                <h4>最近7天创建群组</h4>
                <div ref="groupTrendChartRef" class="chart"></div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Document, ChatDotRound, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { 
  BarChart, 
  LineChart, 
  PieChart 
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { getAdminOverview, getAdminUserStats, getAdminPostStats, getAdminGroupStats } from '@/api/adminDashboard'
import { useAuthStore } from '@/store/auth'
import { useUiStore } from '@/store/ui'

// 注册必须的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  TransformComponent,
  BarChart,
  LineChart,
  PieChart,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer
])

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUiStore()
const loading = ref(false)

// 图表引用
const userStatusChartRef = ref<HTMLElement | null>(null)
const userRegistrationChartRef = ref<HTMLElement | null>(null)
const postStatusChartRef = ref<HTMLElement | null>(null)
const postTrendChartRef = ref<HTMLElement | null>(null)
const groupStatusChartRef = ref<HTMLElement | null>(null)
const groupTrendChartRef = ref<HTMLElement | null>(null)

// 图表实例
let userStatusChart: echarts.ECharts | null = null
let userRegistrationChart: echarts.ECharts | null = null
let postStatusChart: echarts.ECharts | null = null
let postTrendChart: echarts.ECharts | null = null
let groupStatusChart: echarts.ECharts | null = null
let groupTrendChart: echarts.ECharts | null = null

// 数据
const overview = reactive({
  totalUsers: 0,
  totalPosts: 0,
  totalComments: 0,
  totalGroups: 0
})

const userStats = reactive({
  statusCounts: [] as any[],
  registrationTrend: [] as any[]
})

const postStats = reactive({
  statusCounts: [] as any[],
  postTrend: [] as any[]
})

const groupStats = reactive({
  statusCounts: [] as any[],
  groupTrend: [] as any[]
})

// 获取当前 ECharts 主题
function getEchartsTheme() {
  // 优先 data-theme 属性
  if (document.documentElement.getAttribute('data-theme') === 'dark') return 'dark'
  // 兼容 class 方案
  if (document.documentElement.classList.contains('dark-theme')) return 'dark'
  return 'default'
}

// 初始化图表
function initCharts() {
  const theme = getEchartsTheme()
  if (userStatusChartRef.value) {
    userStatusChart = echarts.init(userStatusChartRef.value, theme)
  }
  
  if (userRegistrationChartRef.value) {
    userRegistrationChart = echarts.init(userRegistrationChartRef.value, theme)
  }
  
  if (postStatusChartRef.value) {
    postStatusChart = echarts.init(postStatusChartRef.value, theme)
  }
  
  if (postTrendChartRef.value) {
    postTrendChart = echarts.init(postTrendChartRef.value, theme)
  }
  
  if (groupStatusChartRef.value) {
    groupStatusChart = echarts.init(groupStatusChartRef.value, theme)
  }
  
  if (groupTrendChartRef.value) {
    groupTrendChart = echarts.init(groupTrendChartRef.value, theme)
  }
}

// 更新图表数据
function updateCharts() {
  if (userStatusChart) {
    const statusMap: Record<string, string> = {
      'ACTIVE': '正常',
      'INACTIVE': '未激活',
      'BANNED': '已禁用'
    }
    
    const chartData = userStats.statusCounts.map(item => {
      return {
        name: statusMap[item.name] || item.name,
        value: item.value
      }
    })
    
    userStatusChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: chartData.map(item => item.name)
      },
      series: [
        {
          name: '用户状态',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '16',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData
        }
      ]
    })
  }
  
  if (userRegistrationChart) {
    const dates = userStats.registrationTrend.map(item => item.date)
    const counts = userStats.registrationTrend.map(item => item.count)
    
    userRegistrationChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: counts,
          type: 'bar',
          barWidth: '60%',
          itemStyle: {
            color: '#3a7afe'
          }
        }
      ]
    })
  }
  
  if (postStatusChart) {
    const statusMap: Record<string, string> = {
      'PUBLISHED': '正常',
      'INACTIVE': '未激活',
      'BLOCKED': '已屏蔽',
      'DELETED': '已删除'
    }
    
    const chartData = postStats.statusCounts.map(item => {
      return {
        name: statusMap[item.name] || item.name,
        value: item.value
      }
    })
    
    postStatusChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: chartData.map(item => item.name)
      },
      series: [
        {
          name: '帖子状态',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '16',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData
        }
      ]
    })
  }
  
  if (postTrendChart) {
    const dates = postStats.postTrend.map(item => item.date)
    const counts = postStats.postTrend.map(item => item.count)
    
    postTrendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: counts,
          type: 'line',
          smooth: true,
          areaStyle: {},
          itemStyle: {
            color: '#67c23a'
          }
        }
      ]
    })
  }
  
  if (groupStatusChart) {
    const statusMap: Record<string, string> = {
      'ACTIVE': '正常',
      'INACTIVE': '未激活',
      'DISBANDED': '已解散'
    }
    
    const chartData = groupStats.statusCounts.map(item => {
      return {
        name: statusMap[item.name] || item.name,
        value: item.value
      }
    })
    
    groupStatusChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: chartData.map(item => item.name)
      },
      series: [
        {
          name: '群组状态',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '16',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData
        }
      ]
    })
  }
  
  if (groupTrendChart) {
    const dates = groupStats.groupTrend.map(item => item.date)
    const counts = groupStats.groupTrend.map(item => item.count)
    
    groupTrendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: counts,
          type: 'line',
          smooth: true,
          areaStyle: {},
          itemStyle: {
            color: '#f56c6c'
          }
        }
      ]
    })
  }
}

// 监听暗色模式切换，自动切换 ECharts 主题
watch(
  () => document.documentElement.getAttribute('data-theme'),
  async (_newTheme) => { // 使用 _newTheme 表示该参数未使用
    // 销毁并延迟重建，避免 ECharts dom 冲突
    userStatusChart?.dispose()
    userRegistrationChart?.dispose()
    postStatusChart?.dispose()
    postTrendChart?.dispose()
    groupStatusChart?.dispose()
    groupTrendChart?.dispose()
    await nextTick()
    initCharts()
    updateCharts()
  }
)

// 窗口大小变化时重新调整图表大小
function handleResize() {
  userStatusChart?.resize()
  userRegistrationChart?.resize()
  postStatusChart?.resize()
  postTrendChart?.resize()
  groupStatusChart?.resize()
  groupTrendChart?.resize()
}

// 刷新数据
async function refreshData() {
  await fetchData()
  ElMessage.success('数据已刷新')
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    // 获取概览数据
    const overviewRes = await getAdminOverview()
    if (overviewRes.data.code === 200) {
      const data = overviewRes.data.data
      overview.totalUsers = data.totalUsers
      overview.totalPosts = data.totalPosts
      overview.totalComments = data.totalComments
      overview.totalGroups = data.totalGroups
    } else {
      ElMessage.error(overviewRes.data.message || '获取概览数据失败')
    }
    
    // 获取用户统计数据
    const userStatsRes = await getAdminUserStats()
    if (userStatsRes.data.code === 200) {
      const data = userStatsRes.data.data
      userStats.statusCounts = data.statusCounts
      userStats.registrationTrend = data.registrationTrend
    } else {
      ElMessage.error(userStatsRes.data.message || '获取用户统计数据失败')
    }
    
    // 获取帖子统计数据
    const postStatsRes = await getAdminPostStats()
    if (postStatsRes.data.code === 200) {
      const data = postStatsRes.data.data
      postStats.statusCounts = data.statusCounts
      postStats.postTrend = data.postTrend
    } else {
      ElMessage.error(postStatsRes.data.message || '获取帖子统计数据失败')
    }
    
    // 获取群组统计数据
    const groupStatsRes = await getAdminGroupStats()
    if (groupStatsRes.data.code === 200) {
      const data = groupStatsRes.data.data
      groupStats.statusCounts = data.statusCounts
      groupStats.groupTrend = data.groupTrend
    } else {
      ElMessage.error(groupStatsRes.data.message || '获取群组统计数据失败')
    }
    
    // 更新图表
    updateCharts()
  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 检查用户角色是否为管理员
  try {
    const isAdmin = await authStore.checkAdminStatus()
    if (!isAdmin) {
      ElMessage.error('权限不足，请使用管理员账号登录')
      router.push('/')
      return
    }
  } catch (error) {
    console.error('管理员状态检查失败:', error)
    ElMessage.error('管理员权限验证失败，请重新登录')
    router.push('/')
    return
  }
  
  // 初始化图表
  initCharts()
  
  // 获取数据
  await fetchData()
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  userStatusChart?.dispose()
  userRegistrationChart?.dispose()
  postStatusChart?.dispose()
  postTrendChart?.dispose()
  groupStatusChart?.dispose()
  groupTrendChart?.dispose()
  
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 30px auto;
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

[data-theme="dark"] .card-header h2 {
  color: #fff;
}

.overview-cards {
  margin-bottom: 32px;
}

.overview-card {
  border-radius: 12px;
  transition: box-shadow 0.2s;
  background: #fff;
  border: 1px solid #f0f0f0;
}

.overview-card:hover {
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.08);
}

.card-header-small {
  font-size: 15px;
  font-weight: 500;
  color: #666;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-top: 10px;
}

.data-section {
  margin-bottom: 36px;
  background: #f8fafd;
  border-radius: 10px;
  padding: 18px 20px 10px 20px;
  box-shadow: 0 1px 6px 0 rgba(60, 60, 60, 0.04);
}

.data-section h3 {
  font-size: 18px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 18px;
}

.chart-container {
  background: #fff;
  border-radius: 10px;
  padding: 18px 10px 10px 10px;
  margin-bottom: 18px;
  min-height: 340px;
  box-shadow: 0 1px 6px 0 rgba(60, 60, 60, 0.04);
}

.chart-container h4 {
  font-size: 16px;
  color: #222;
  font-weight: 500;
  margin-bottom: 10px;
}

.chart {
  width: 100%;
  height: 260px;
}

/* 暗色模式适配 */
[data-theme="dark"] .el-card,
[data-theme="dark"] .overview-card {
  background: #232326 !important;
  border-color: #333 !important;
  color: #fff !important;
}
[data-theme="dark"] .overview-value {
  color: #6cb4ff;
}
[data-theme="dark"] .card-header-small {
  color: #bbb;
}
[data-theme="dark"] .data-section {
  background: #18181c !important;
  color: #fff !important;
  box-shadow: 0 1px 8px 0 rgba(0,0,0,0.18);
}
[data-theme="dark"] .data-section h3 {
  color: #6cb4ff;
}
[data-theme="dark"] .chart-container {
  background: #232326 !important;
  color: #fff !important;
  box-shadow: 0 1px 8px 0 rgba(0,0,0,0.18);
}
[data-theme="dark"] .chart-container h4 {
  color: #fff;
}
[data-theme="dark"] .el-statistic {
  color: #fff !important;
}
[data-theme="dark"] .el-row,
[data-theme="dark"] .el-col {
  background: transparent !important;
}
[data-theme="dark"] .chart {
  background: transparent !important;
}

/* ECharts 图表暗色适配建议：可在 updateCharts 里设置 theme: 'dark'，或在全局注册时设置。 */

/* 响应式适配 */
@media (max-width: 768px) {
  .overview-card {
    margin-bottom: 20px;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .chart {
    height: 200px;
  }
}
</style>
