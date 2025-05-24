<template>
    <div class="group-files-tab">
        <div class="file-actions">
            <el-button type="primary" @click="showUploadDialog" :disabled="!canUploadFile">
                <el-icon>
                    <UploadFilled />
                </el-icon> 上传文件
            </el-button>
        </div>

        <div class="files-container" v-loading="loading">
            <el-empty v-if="files.length === 0 && !loading" description="暂无文件资源" />

            <el-table v-else :data="files" style="width: 100%"
                :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266' }">
                <el-table-column label="文件名" min-width="220">
                    <template #default="scope">
                        <div class="file-name-cell">
                            <el-icon :size="24" class="file-icon">
                                <Document v-if="isDocumentFile(scope.row.fileName)" />
                                <Picture v-else-if="isImageFile(scope.row.fileName)" />
                                <VideoPlay v-else-if="isVideoFile(scope.row.fileName)" />
                                <Files v-else />
                            </el-icon>
                            <span class="file-name">{{ scope.row.fileName }}</span>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="大小" width="120">
                    <template #default="scope">
                        {{ formatFileSize(scope.row.fileSize) }}
                    </template>
                </el-table-column>

                <el-table-column label="上传者" width="150">
                    <template #default="scope">
                        {{ scope.row.uploaderName }}
                    </template>
                </el-table-column>

                <el-table-column label="上传时间" width="180">
                    <template #default="scope">
                        {{ formatTime(scope.row.createdAt) }}
                    </template>
                </el-table-column>

                <el-table-column label="描述" min-width="200">
                    <template #default="scope">
                        {{ scope.row.description || '无' }}
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                        <el-button type="primary" size="small" @click="handleDownload(scope.row)" plain>
                            <el-icon>
                                <Download />
                            </el-icon> 下载
                        </el-button>
                        <el-dropdown v-if="canManageFile(scope.row)" trigger="click">
                            <el-button type="primary" size="small" plain>
                                <el-icon>
                                    <MoreFilled />
                                </el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="showEditFileDialog(scope.row)">
                                        <el-icon>
                                            <Edit />
                                        </el-icon> 编辑描述
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="confirmDeleteFile(scope.row)" class="danger-item">
                                        <el-icon>
                                            <Delete />
                                        </el-icon> 删除文件
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination">
                <el-pagination v-if="total > pageSize" v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" background />
            </div>
        </div>

        <!-- 上传文件对话框 -->
        <el-dialog v-model="uploadDialogVisible" title="上传文件" width="500px">
            <el-form :model="fileForm" :rules="fileRules" ref="fileFormRef" label-width="80px">
                <el-form-item label="文件" prop="file">
                    <el-upload class="file-uploader" drag :auto-upload="false" :limit="1" :on-change="handleFileChange"
                        :on-remove="handleFileRemove" :multiple="false" accept="*">
                        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                        <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
                        <template #tip>
                            <div class="el-upload__tip">
                                单个文件大小不超过50MB
                            </div>
                        </template>
                    </el-upload>
                </el-form-item>

                <el-form-item label="描述" prop="description">
                    <el-input v-model="fileForm.description" type="textarea" rows="3" placeholder="请输入文件描述（选填）" />
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="uploadDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="handleUploadFile" :loading="uploading">
                        上传
                    </el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 编辑文件描述对话框 -->
        <el-dialog v-model="editFileDialogVisible" title="编辑文件描述" width="500px">
            <el-form :model="fileForm" ref="editFileFormRef" label-width="80px">
                <el-form-item label="文件名">
                    <div>{{ editingFile?.fileName }}</div>
                </el-form-item>

                <el-form-item label="描述" prop="description">
                    <el-input v-model="fileForm.description" type="textarea" rows="3" placeholder="请输入文件描述（选填）" />
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editFileDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="handleUpdateFileInfo" :loading="submitting">
                        保存
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/store/auth';
import {
    getGroupFiles,
    uploadFile,
    updateFileInfo,
    deleteFile,
    downloadFileById
} from '@/api/groupFile';
import {
    Document,
    Picture,
    VideoPlay,
    Files,
    Edit,
    Delete,
    Download,
    MoreFilled,
    UploadFilled
} from '@element-plus/icons-vue';

// 类型定义补充
interface FileData {
    id?: number;
    fileId?: number;
    fileName: string;
    size?: number;
    uploaderId?: number;
    uploaderName?: string;
    uploadTime?: string;
    description?: string;
}

// 属性定义
const props = defineProps({
    groupId: {
        type: [String, Number],
        required: true
    },
    userRole: {
        type: String,
        default: null
    }
});

// 数据状态
const authStore = useAuthStore();
const loading = ref(false);
const uploading = ref(false);
const submitting = ref(false);
const files = ref<FileData[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const uploadDialogVisible = ref(false);
const editFileDialogVisible = ref(false);
const fileFormRef = ref<any>(null);
const editFileFormRef = ref<any>(null);
const editingFile = ref<FileData | null>(null);

// 表单数据
const fileForm = ref<{
    file: File | null;
    description: string;
}>({
    file: null,
    description: ''
});

// 表单验证规则
const fileRules = {
    file: [
        { required: true, message: '请选择文件', trigger: 'change' }
    ]
};

// 计算属性
const canUploadFile = computed(() => {
    return props.userRole && ['CREATOR', 'ADMIN', 'MEMBER'].includes(props.userRole);
});

// 权限判断 - 是否可以管理文件（删除、编辑）
const canManageFile = (file: FileData): boolean => {
    if (!props.userRole) return false;

    // 创建者和管理员可以管理所有文件
    if (['CREATOR', 'ADMIN'].includes(props.userRole)) return true;

    // 普通成员只能管理自己上传的文件
    return props.userRole === 'MEMBER' && file.uploaderId === authStore.user?.userId;
};

// 生命周期钩子
onMounted(() => {
    loadFiles();
});

// 监视变量变化
watch(() => props.groupId, (newVal) => {
    if (newVal) {
        currentPage.value = 1;
        loadFiles();
    }
});

// 加载文件列表
const loadFiles = async () => {
    loading.value = true;
    try {
        const response = await getGroupFiles({ groupId: props.groupId });
        if (response.data && response.data.code === 200) {
            // 处理可能的分页数据结构
            if (response.data.data && response.data.data.records !== undefined) {
                // 服务器返回分页对象，取records字段
                files.value = response.data.data.records || [];
                total.value = response.data.data.total || 0;
            } else {
                // 服务器直接返回数组
                files.value = response.data.data || [];
                total.value = files.value.length;
            }
            console.log('文件列表加载成功:', files.value);
        } else {
            ElMessage.error(response.data?.message || '加载文件列表失败');
            files.value = [];
        }
    } catch (error) {
        console.error('加载文件列表失败:', error);
        ElMessage.error('加载文件列表失败，请稍后重试');
        files.value = [];
    } finally {
        loading.value = false;
    }
};

// 处理上传文件
const handleUploadFile = async () => {
    if (!fileForm.value.file) {
        ElMessage.warning('请选择要上传的文件');
        return;
    }

    const formData = new FormData();
    formData.append('file', fileForm.value.file);
    
    if (fileForm.value.description) {
        formData.append('description', fileForm.value.description);
    }
    
    uploading.value = true;
    try {
        const response = await uploadFile(props.groupId, fileForm.value.file, fileForm.value.description);
        if (response.data && response.data.code === 200) {
            ElMessage.success('文件上传成功');
            uploadDialogVisible.value = false;
            fileForm.value.file = null;
            fileForm.value.description = '';
            loadFiles();
        } else {
            ElMessage.error(response.data?.message || '文件上传失败');
        }
    } catch (error) {
        console.error('文件上传失败:', error);
        ElMessage.error('文件上传失败，请稍后重试');
    } finally {
        uploading.value = false;
    }
};

// 删除文件（合并了 handleDeleteFile 和 confirmDeleteFile 函数）
const confirmDeleteFile = (file: FileData) => {
    ElMessageBox.confirm(
        `确定要删除文件 "${file.fileName}" 吗？删除后将无法恢复。`,
        '删除文件',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            const fileId = file.id || file.fileId;
            if (fileId === undefined) {
                ElMessage.error('文件ID未定义');
                return;
            }
            
            const response = await deleteFile(fileId);
            if (response.data && response.data.code === 200) {
                ElMessage.success('文件已成功删除');
                loadFiles(); // 重新加载文件列表
            } else {
                ElMessage.error(response.data?.message || '删除文件失败');
            }
        } catch (error) {
            console.error('文件删除失败:', error);
            ElMessage.error('文件删除失败，请稍后重试');
        }
    }).catch(() => {
        // 取消操作
    });
};

// 显示上传对话框
const showUploadDialog = () => {
    if (!canUploadFile.value) {
        ElMessage.warning('您没有上传文件的权限');
        return;
    }

    fileForm.value = {
        file: null,
        description: ''
    };

    uploadDialogVisible.value = true;
};

// 文件上传相关处理
const handleFileChange = (file: { raw: File }) => {
    fileForm.value.file = file.raw;
};

const handleFileRemove = () => {
    fileForm.value.file = null;
};

// 显示编辑文件对话框
const showEditFileDialog = (file: FileData) => {
    editingFile.value = file;
    fileForm.value.description = file.description || '';
    editFileDialogVisible.value = true;
};

// 更新文件信息
const handleUpdateFileInfo = async () => {
    if (!editingFile.value) return;

    submitting.value = true;
    try {
        const data = {
            description: fileForm.value.description
        };

        const response = await updateFileInfo(editingFile.value.id || 0, data);
        if (response.data && response.data.code === 200) {
            ElMessage.success('文件信息更新成功');
            editFileDialogVisible.value = false;
            loadFiles(); // 重新加载文件列表
        } else {
            ElMessage.error(response.data?.message || '文件信息更新失败');
        }
    } catch (error) {
        console.error('文件信息更新失败:', error);
        ElMessage.error('文件信息更新失败，请稍后重试');
    } finally {
        submitting.value = false;
    }
};

// 下载文件
const handleDownload = async (file: FileData) => {
    try {
        const fileId = file.id || file.fileId;
        if (fileId === undefined) {
            ElMessage.error('文件ID不存在，无法下载');
            return;
        }
        await downloadFileById(fileId, file.fileName);
        ElMessage.success('文件开始下载');
    } catch (error) {
        console.error('文件下载失败:', error);
        ElMessage.error('文件下载失败，请稍后重试');
    }
};

// 工具函数 - 格式化文件大小
const formatFileSize = (bytes: number | undefined): string => {
    if (!bytes || bytes === 0) return '0 B';

    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    let i = 0;
    let size = bytes;

    while (size >= 1024 && i < units.length - 1) {
        size /= 1024;
        i++;
    }

    return Math.round(size * 100) / 100 + ' ' + units[i];
};

// 工具函数 - 格式化时间
const formatTime = (timestamp: string | undefined): string => {
    if (!timestamp) return '';

    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN');
};

// 文件类型判断
const isDocumentFile = (fileName: string): boolean => {
    const docExts = ['.doc', '.docx', '.pdf', '.txt', '.xls', '.xlsx', '.ppt', '.pptx'];
    return docExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

const isImageFile = (fileName: string): boolean => {
    const imgExts = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg', '.webp'];
    return imgExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

const isVideoFile = (fileName: string): boolean => {
    const videoExts = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv', '.webm'];
    return videoExts.some(ext => fileName.toLowerCase().endsWith(ext));
};

// 处理分页大小变化
const handleSizeChange = (val: number) => {
    pageSize.value = val;
    currentPage.value = 1;
    loadFiles();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
    currentPage.value = val;
    loadFiles();
};
</script>

<style scoped>
.group-files-tab {
    padding: 20px 0;
}

.file-actions {
    margin-bottom: 20px;
    display: flex;
    justify-content: flex-end;
}

.files-container {
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.file-name-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.file-name {
    word-break: break-all;
}

.file-icon {
    flex-shrink: 0;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
}

.file-uploader {
    width: 100%;
}

/* 表格内的危险操作项 */
.danger-item {
    color: #f56c6c;
}

/* 上传组件样式修正 */
:deep(.el-upload-dragger) {
    width: 100%;
}
</style>
