<template>
  <div class="avatar-uploader">
    <!-- 头像显示区域 -->
    <div class="avatar-display" @click="showUploadDialog" :style="{ width: `${size}px`, height: `${size}px` }">
      <img v-if="modelValue" :src="modelValue" class="avatar" alt="头像" />
      <div v-else class="avatar-placeholder">
        <el-icon class="avatar-icon"><Plus /></el-icon>
        <span class="avatar-text">上传头像</span>
      </div>
      <div class="avatar-hover-effect">
        <el-icon><Camera /></el-icon>
        <span>更换头像</span>
      </div>
    </div>
    <div class="upload-tip" v-if="tip">{{ tip }}</div>

    <!-- 头像上传/裁剪对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="设置头像"
      width="520px"
      :close-on-click-modal="false"
      :destroy-on-close="true"
      :modal-append-to-body="true"
      :append-to-body="true"
      class="avatar-dialog"
    >
      <div class="cropper-container">
        <!-- 图片选择按钮 -->
        <div v-if="!cropperImage" class="image-selector">
          <el-upload
            class="image-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/jpeg,image/png,image/gif,image/webp"
            drag
          >
            <div class="upload-area">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span class="upload-text">点击或拖拽图片到此处上传</span>
              <span class="upload-hint">支持JPG、PNG、GIF、WebP格式，文件小于2MB</span>
            </div>
          </el-upload>
        </div>

        <!-- 裁剪区域 -->
        <div v-else class="cropper-area">
          <div class="cropper-preview-container">
            <VueCropper
              ref="cropperRef"
              :img="cropperImage"
              :autoCrop="true"
              :fixedBox="true"
              :centerBox="true"
              :autoCropWidth="200"
              :autoCropHeight="200"
              :outputSize="1"
              :outputType="cropperOptions.outputType"
              :info="true"
              :full="true"
              :canMove="true"
              :canMoveBox="true"
              :originalImg="true"
              mode="cover"
              :infoTrue="true"
              class="cropper"
            />
            <div class="preview-container">
              <div class="preview-label">预览效果</div>
              <div class="preview-box">
                <img :src="previewUrl" alt="头像预览" v-if="previewUrl" />
                <div class="preview-placeholder" v-else>
                  <el-icon><Camera /></el-icon>
                </div>
              </div>
            </div>
          </div>
          <div class="cropper-actions">
            <el-button-group>
              <el-tooltip content="向左旋转">
                <el-button @click="rotateLeft" type="default" size="small">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="向右旋转">
                <el-button @click="rotateRight" type="default" size="small">
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
            <el-button-group>
              <el-tooltip content="放大">
                <el-button @click="zoomIn" type="default" size="small">
                  <el-icon><ZoomIn /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="缩小">
                <el-button @click="zoomOut" type="default" size="small">
                  <el-icon><ZoomOut /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelCrop">取消</el-button>
          <el-button v-if="cropperImage" @click="reselect" plain>重新选择</el-button>
          <el-button v-if="cropperImage" type="primary" @click="confirmCrop" :loading="uploading">
            确认并上传
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { VueCropper } from 'vue-cropper';
import 'vue-cropper/dist/index.css';
import {
  Plus,
  Refresh,
  RefreshRight,
  ZoomIn,
  ZoomOut,
  Camera
} from '@element-plus/icons-vue';
import { useAuthStore } from '../store/auth';
import { uploadApi } from '../api/upload';

// 定义组件的属性
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 100
  },
  maxSize: {
    type: Number,
    default: 2 // 默认最大2MB
  },
  tip: {
    type: String,
    default: ''
  },
  isGroupAvatar: {
    type: Boolean,
    default: false // 默认为用户头像
  }
});

// 定义组件的事件
const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error']);

// 使用身份验证存储
const authStore = useAuthStore();

// 裁剪相关状态
const dialogVisible = ref(false);
const cropperRef = ref();
const cropperImage = ref('');
const cropperOptions = ref({
  outputType: 'jpeg' // 输出图片格式
});
const uploading = ref(false);
const previewUrl = ref(''); // 预览图片URL

// 显示上传对话框
const showUploadDialog = () => {
  dialogVisible.value = true;
};

// 更新预览图
const updatePreview = () => {
  if (cropperRef.value && typeof cropperRef.value.getCropData === 'function') {
    cropperRef.value.getCropData((data) => {
      previewUrl.value = data;
    });
  }
};

// 处理文件选择
const handleFileChange = (file: any) => {
  const isImage = file.raw.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('请上传图片文件');
    return;
  }

  const isLtMaxSize = file.raw.size / 1024 / 1024 < props.maxSize;
  if (!isLtMaxSize) {
    ElMessage.error(`头像大小不能超过 ${props.maxSize}MB`);
    return;
  }

  // 设置裁剪图片
  const reader = new FileReader();
  reader.onload = (e) => {
    cropperImage.value = e.target?.result as string;

    // 根据图片类型设置输出格式
    if (file.raw.type === 'image/png') {
      cropperOptions.value.outputType = 'png';
    } else {
      cropperOptions.value.outputType = 'jpeg';
    }
    
    // 等待下一个tick后更新预览
    setTimeout(() => {
      updatePreview();
    }, 300);
  };
  reader.readAsDataURL(file.raw);
};

// 向左旋转
const rotateLeft = () => {
  if (cropperRef.value) {
    try {
      // 尝试使用不同的旋转方法，vue-cropper可能使用不同名称的方法
      if (typeof cropperRef.value.rotateLeft === 'function') {
        cropperRef.value.rotateLeft();
      } else if (typeof cropperRef.value.rotate === 'function') {
        cropperRef.value.rotate(-90); // 逆时针旋转90度
      } else {
        console.warn('裁剪组件不支持旋转操作');
      }
      updatePreview();
    } catch (e) {
      console.error('旋转操作出错:', e);
    }
  } else {
    console.warn('裁剪组件实例不存在');
  }
};

// 向右旋转
const rotateRight = () => {
  if (cropperRef.value) {
    try {
      // 尝试使用不同的旋转方法，vue-cropper可能使用不同名称的方法
      if (typeof cropperRef.value.rotateRight === 'function') {
        cropperRef.value.rotateRight();
      } else if (typeof cropperRef.value.rotate === 'function') {
        cropperRef.value.rotate(90); // 顺时针旋转90度
      } else {
        console.warn('裁剪组件不支持旋转操作');
      }
      updatePreview();
    } catch (e) {
      console.error('旋转操作出错:', e);
    }
  } else {
    console.warn('裁剪组件实例不存在');
  }
};

// 放大
const zoomIn = () => {
  if (cropperRef.value) {
    try {
      // 尝试使用不同的缩放方法
      if (typeof cropperRef.value.zoomIn === 'function') {
        cropperRef.value.zoomIn();
      } else if (typeof cropperRef.value.changeScale === 'function') {
        // 当前比例基础上放大20%
        cropperRef.value.changeScale(0.2);
      } else {
        console.warn('裁剪组件不支持放大操作');
      }
      updatePreview();
    } catch (e) {
      console.error('放大操作出错:', e);
    }
  } else {
    console.warn('裁剪组件实例不存在');
  }
};

// 缩小
const zoomOut = () => {
  if (cropperRef.value) {
    try {
      // 尝试使用不同的缩放方法
      if (typeof cropperRef.value.zoomOut === 'function') {
        cropperRef.value.zoomOut();
      } else if (typeof cropperRef.value.changeScale === 'function') {
        // 当前比例基础上缩小20%
        cropperRef.value.changeScale(-0.2);
      } else {
        console.warn('裁剪组件不支持缩小操作');
      }
      updatePreview();
    } catch (e) {
      console.error('缩小操作出错:', e);
    }
  } else {
    console.warn('裁剪组件实例不存在');
  }
};

// 取消裁剪
const cancelCrop = () => {
  dialogVisible.value = false;
  cropperImage.value = '';
  previewUrl.value = '';
};

// 重新选择图片
const reselect = () => {
  cropperImage.value = '';
  previewUrl.value = '';
};

// 确认裁剪并上传
const confirmCrop = () => {
  if (!cropperRef.value) return;
  
  uploading.value = true;

  // 使用回调方式获取裁剪数据
  // 这里我们确保 getCropData 被正确调用并接受一个回调函数
  if (typeof cropperRef.value.getCropData !== 'function') {
    ElMessage.error('裁剪组件不支持获取裁剪数据');
    uploading.value = false;
    return;
  }

  cropperRef.value.getCropData((base64Data: string) => {
    if (!base64Data) {
      ElMessage.error('获取裁剪图片失败');
      uploading.value = false;
      return;
    }

    try {
      // 将Base64转换为Blob
      const byteString = atob(base64Data.split(',')[1]);
      const mimeString = base64Data.split(',')[0].split(':')[1].split(';')[0];
      const ab = new ArrayBuffer(byteString.length);
      const ia = new Uint8Array(ab);

      for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
      }

      const blob = new Blob([ab], { type: mimeString });
      const file = new File([blob], `avatar.${cropperOptions.value.outputType}`, { type: mimeString });

      // 上传头像前关闭对话框，优化用户体验
      dialogVisible.value = false;
      
      // 根据类型选择不同的上传API
      const uploadPromise = props.isGroupAvatar 
        ? uploadApi.uploadGroupAvatar(file) 
        : uploadApi.uploadAvatar(file);
        
      // 上传头像
      uploadPromise.then((response: any) => {
        // 确保响应有效
        if (!response) {
          throw new Error('上传头像失败：服务器没有响应');
        }
        
        // 尝试从不同格式的响应中提取URL
        let avatarUrl = '';
        
        try {
          // 检查响应结构
          if (typeof response === 'string') {
            // 直接返回字符串URL
            avatarUrl = response;
          } else if (response.data) {
            const data = response.data;
            
            if (typeof data === 'string') {
              // 响应数据直接是字符串URL
              avatarUrl = data;
            } else if (data && data.data && typeof data.data === 'string') {
              // 嵌套的data字段
              avatarUrl = data.data;
            } else if (data && data.url && typeof data.url === 'string') {
              // url字段
              avatarUrl = data.url;
            }
          }
          
          console.log('头像上传响应:', response);
          console.log('解析的头像URL:', avatarUrl);
        } catch (e) {
          console.error('解析头像URL时出错:', e);
        }
        
        if (avatarUrl) {
          // 不在这里显示成功消息，留给父组件处理
          
          // 直接使用返回的URL，这会触发父组件的更新
          emit('update:modelValue', avatarUrl);
          emit('upload-success', avatarUrl, file);
        } else {
          console.error('无法从响应中解析头像URL:', response);
          throw new Error('上传头像失败：服务器未返回有效的头像URL');
        }
      }).catch(uploadError => {
        console.error('头像上传API错误:', uploadError);
        ElMessage.error(uploadError.message || '上传失败，请稍后重试');
        emit('upload-error', uploadError, file);
      }).finally(() => {
        uploading.value = false;
        cropperImage.value = '';
      });
    } catch (error: any) {
      console.error('头像处理失败:', error);
      ElMessage.error(error.message || '头像处理失败，请稍后重试');
      emit('upload-error', error, null);
      
      // 出错时也关闭对话框
      dialogVisible.value = false;
      uploading.value = false;
      cropperImage.value = '';
    }
  });
};
</script>

<style scoped>
.avatar-uploader {
  text-align: center;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-display {
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.avatar-display:hover {
  border-color: var(--el-color-primary);
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
}

.avatar {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
  background-color: #f5f7fa;
  border-radius: 50%;
}

.avatar-hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
}

.avatar-display:hover .avatar-hover-effect {
  opacity: 1;
}

.avatar-hover-effect i {
  font-size: 24px;
  margin-bottom: 5px;
}

.avatar-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.avatar-text {
  font-size: 14px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

/* 裁剪对话框样式 */
.avatar-dialog {
  /* 确保对话框总是相对于视口居中，而不是父组件 */
  position: fixed !important;
  z-index: 2050 !important; /* 提高z-index，确保显示在其他元素之上 */
}

.avatar-dialog :deep(.el-dialog__body) {
  padding: 20px 25px;
}

/* 全局样式确保对话框始终居中 */
:deep(.el-overlay) {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 2000;
}

:deep(.el-overlay-dialog) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: auto;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-dialog :deep(.el-dialog) {
  /* 确保对话框居中显示 */
  position: relative !important; /* 使用relative使对话框能够自动居中 */
  margin: 0 auto !important;
  max-width: 95vw;
  max-height: 90vh;
  overflow: auto;
}

/* 全局覆盖层样式 */
:global(.el-overlay) {
  position: fixed !important;
  top: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  left: 0 !important;
  z-index: 2000 !important;
  height: 100vh !important;
  width: 100vw !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
}

/* 添加全局样式，确保对话框正确显示 */
:global(.el-dialog) {
  margin: 15vh auto !important;
  position: relative !important;
  max-width: 90vw !important;
}

:global(.el-dialog__wrapper) {
  position: fixed !important;
  top: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  left: 0 !important;
  overflow: auto !important;
  margin: 0 !important;
  z-index: 2001 !important;
  display: flex !important;
  flex-direction: column !important;
  justify-content: center !important;
}

.cropper-container {
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-selector {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-uploader {
  width: 100%;
}

.upload-area {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  background-color: var(--el-fill-color-lighter);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

.upload-area:hover {
  border-color: var(--el-color-primary);
  background-color: rgba(64, 158, 255, 0.05);
}

.upload-icon {
  font-size: 36px;
  color: var(--el-color-primary);
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.cropper-area {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cropper-preview-container {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.cropper {
  height: 320px;
  flex: 1;
}

.preview-container {
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.preview-box {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px solid var(--el-border-color);
  background-color: #f5f7fa;
}

.preview-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #ccc;
  font-size: 24px;
}

.cropper-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 576px) {
  :deep(.el-dialog) {
    /* 移动设备优化 */
    width: 90% !important;
    max-height: 85vh;
    margin: 15px auto !important;
  }

  .cropper-preview-container {
    flex-direction: column;
    align-items: center;
  }

  .preview-container {
    margin-top: 15px;
  }

  .cropper {
    height: 250px;
  }

  .cropper-actions {
    justify-content: center;
  }
}
</style>
