<template>
    <div class="home-page">
        <el-row :gutter="20">
            <el-col :span="24">
                <el-card class="welcome-card">
                    <template #header>
                        <div class="card-header">
                            <h2>欢迎来到"学伴"校园互助平台</h2>
                        </div>
                    </template>
                    <div class="card-content">
                        <p>在这里，你可以发布和查找各类互助信息，与校园内的同学们互相帮助。</p>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" class="module-row">
            <el-col :span="24">
                <h3>平台功能</h3>
            </el-col>
            <el-col :span="8">
                <el-card class="module-card" @click="navigateTo('/helpinfo')">
                    <el-icon class="module-icon">
                        <Connection />
                    </el-icon>
                    <h4>互助信息</h4>
                    <p>浏览和发布互助信息，包括课程辅导、技能交换、物品借用等</p>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card class="module-card disabled">
                    <el-icon class="module-icon">
                        <UserFilled />
                    </el-icon>
                    <h4>学习小组 <el-tag size="small">即将上线</el-tag></h4>
                    <p>创建或加入学习小组，进行协作学习和讨论</p>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card class="module-card disabled">
                    <el-icon class="module-icon">
                        <Files />
                    </el-icon>
                    <h4>资源共享 <el-tag size="small">即将上线</el-tag></h4>
                    <p>分享和获取学习资料、二手物品等校园资源</p>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Connection, UserFilled, Files } from '@element-plus/icons-vue'

const router = useRouter()

function navigateTo(path: string) {
    router.push(path)
}
</script>

<style scoped>
.home-page {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
}

.welcome-card {
    margin-bottom: 30px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.module-row {
    margin-top: 20px;
}

.module-card {
    height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    margin-bottom: 20px;
    cursor: pointer;
    transition: all 0.3s;
}

.module-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.module-card.disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.module-icon {
    font-size: 40px;
    margin-bottom: 10px;
    color: #409EFF;
}

h3 {
    margin-bottom: 20px;
    font-weight: 500;
}

h4 {
    margin: 10px 0;
}

p {
    color: #666;
    line-height: 1.5;
}
</style>
