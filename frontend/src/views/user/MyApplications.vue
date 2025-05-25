<template>
    <div class="my-applications-page">
        <h2 class="page-title">我的申请</h2>

        <el-tabs v-model="activeTab" class="application-tabs">
            <el-tab-pane label="我发出的申请" name="sent">
                <el-card v-loading="loading">
                    <div v-if="myApplications.length === 0 && !loading" class="empty-data">
                        <el-empty description="暂无发出的申请记录" />
                    </div>

                    <template v-else>
                        <el-tabs v-model="sentApplicationTab">
                            <el-tab-pane label="待处理" name="pending">
                                <application-list :applications="pendingApplications" type="sent" status="pending" />
                            </el-tab-pane>

                            <el-tab-pane label="已接受" name="accepted">
                                <application-list :applications="acceptedApplications" type="sent" status="accepted" />
                            </el-tab-pane>

                            <el-tab-pane label="已拒绝" name="rejected">
                                <application-list :applications="rejectedApplications" type="sent" status="rejected" />
                            </el-tab-pane>
                        </el-tabs>
                    </template>
                </el-card>
            </el-tab-pane>

            <el-tab-pane label="我收到的申请" name="received">
                <el-card v-loading="loading">
                    <div v-if="receivedApplications.length === 0 && !loading" class="empty-data">
                        <el-empty description="暂无收到的申请记录" />
                    </div>

                    <template v-else>
                        <el-tabs v-model="receivedApplicationTab">
                            <el-tab-pane label="待处理" name="pending">
                                <application-list 
                                    :applications="pendingReceivedApplications" 
                                    type="received" 
                                    status="pending" 
                                    @refresh="fetchData"
                                />
                            </el-tab-pane>

                            <el-tab-pane label="处理中" name="processing">
                                <application-list :applications="processingReceivedApplications" type="received"
                                    status="processing" @refresh="fetchData" />
                            </el-tab-pane>

                            <el-tab-pane label="已完成" name="completed">
                                <application-list :applications="completedReceivedApplications" type="received"
                                    status="completed" />
                            </el-tab-pane>
                        </el-tabs>
                    </template>
                </el-card>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useApplicationStore } from '../../store/application'
import { ElMessage } from 'element-plus'
import ApplicationList from '../../components/common/ApplicationList.vue'

const applicationStore = useApplicationStore()
const activeTab = ref('sent')
const sentApplicationTab = ref('pending')
const receivedApplicationTab = ref('pending')
const loading = computed(() => applicationStore.loading)

// 我发出的申请
const myApplications = computed(() => applicationStore.myApplications)
const pendingApplications = computed(() => applicationStore.pendingApplications)
const acceptedApplications = computed(() => applicationStore.acceptedApplications)
const rejectedApplications = computed(() => applicationStore.rejectedApplications)

// 我收到的申请
const receivedApplications = computed(() => applicationStore.receivedApplications)
const pendingReceivedApplications = computed(() => applicationStore.pendingReceivedApplications)
const processingReceivedApplications = computed(() => applicationStore.processingReceivedApplications)
const completedReceivedApplications = computed(() => applicationStore.completedReceivedApplications)

onMounted(() => {
    fetchData()
})

// 加载数据
async function fetchData() {
    try {
        await Promise.all([
            applicationStore.fetchMyApplications(),
            applicationStore.fetchReceivedApplications()
        ])
    } catch (e: any) {
        ElMessage.error(e.message || '加载数据失败')
    }
}
</script>

<style scoped>
.my-applications-page {
    max-width: 1000px;
    margin: 30px auto;
    padding: 0 20px;
}

.page-title {
    margin-bottom: 20px;
    font-size: 1.8rem;
    color: #303133;
}

.application-tabs {
    margin-top: 20px;
}

.empty-data {
    padding: 40px 0;
}
</style>
