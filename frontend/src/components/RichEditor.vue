<template>
  <div class="markdown-editor">
    <el-form-item :label="label" :prop="prop" class="editor-form-item" :required="required">
      <div class="editor-container">
        <MdEditor
          v-model="valueContent"
          :style="{ height: height }"
          :placeholder="placeholder"
          :toolbars="['bold', 'italic', 'strikeThrough', 'title', 'quote', 'code', 'link', 'image', 'table', 'orderedList', 'unorderedList', 'task', 'preview', 'fullscreen']"
          @change="handleChange"
          @onUploadImg="onUploadImg"
          :preview-theme="previewTheme"
          :editor-theme="editorTheme"
          :showWordCount="false"
          class="md-editor-fullwidth rich-editor-dark"
        />
      </div>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { uploadApi } from '../api/upload'

interface Props {
  modelValue: string
  label?: string
  prop?: string
  placeholder?: string
  height?: string
  required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  label: '',
  prop: '',
  placeholder: '请输入内容...',
  height: '400px',
  required: false
})

const emit = defineEmits(['update:modelValue', 'change'])

// 编辑器内容
const valueContent = ref(props.modelValue || '')

// 获取当前主题模式
const isDarkMode = ref(false)

// 监听主题变化并更新编辑器主题
const updateTheme = () => {
  const htmlElement = document.documentElement
  isDarkMode.value = htmlElement.getAttribute('data-theme') === 'dark'
}

// 初始检查主题
updateTheme()

// 创建主题监听器
onMounted(() => {
  // 监听主题变化
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === 'data-theme') {
        updateTheme()
      }
    })
  })
  
  observer.observe(document.documentElement, { attributes: true })
})

// 主题设置 - 根据当前主题动态变化
const previewTheme = computed(() => isDarkMode.value ? 'github-dark' : 'vuepress')
const editorTheme = computed(() => isDarkMode.value ? 'dark' : 'light')

// 监听内容变化
const handleChange = (content: string) => {
  valueContent.value = content
  emit('update:modelValue', content)
  emit('change', content)
}

// 处理图片上传
const onUploadImg = async (files: Array<File>, callback: (urls: Array<string>) => void) => {
  // 调用后端上传接口，支持多图上传
  const urls: string[] = []
  for (const file of files) {
    try {
      const res = await uploadApi.uploadImage(file)
      // 兼容 axios 响应结构
      // res 可能是 AxiosResponse 或直接是 url 字符串
      if (res && res.data && typeof res.data === 'object' && (res.data as any).code === 200) {
        urls.push((res.data as any).data)
      } else if (typeof res === 'string') {
        urls.push(res)
      } else if (res && res.data && typeof res.data === 'string') {
        urls.push(res.data)
      } else {
        console.error('图片上传失败', res)
      }
    } catch (e) {
      console.error('图片上传失败', e)
    }
  }
  callback(urls)
}

// 监听父组件传入的值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue !== valueContent.value) {
    valueContent.value = newValue
  }
})

// 暴露给父组件的方法
defineExpose({
  // 设置内容
  setContent: (content: string) => {
    valueContent.value = content
  },
  // 获取内容
  getContent: () => {
    return valueContent.value
  },
  // 清空内容
  clear: () => {
    valueContent.value = ''
    emit('update:modelValue', '')
    emit('change', '')
  }
})
</script>

<style scoped>
.markdown-editor {
  margin-bottom: 20px;
}

.editor-container {
  z-index: 100;
  width: 100%;
}

.editor-form-item {
  width: 100%;
}

.text-count {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

:deep(.md-editor) {
  border-radius: 4px;
  width: 100%;
  min-height: 300px;
}

:deep(.md-editor-toolbar) {
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

:deep(.md-editor-input) {
  min-height: 300px;
}

:deep(.md-editor-preview) {
  min-height: 300px;
}

/* 暗色模式适配 */
[data-theme="dark"] :deep(.md-editor) {
  background-color: var(--dark-bg);
  color: var(--dark-text-primary);
  border-color: var(--dark-border-color);
}

[data-theme="dark"] :deep(.md-editor-toolbar) {
  background-color: var(--dark-card-bg);
  border-color: var(--dark-border-color);
}

[data-theme="dark"] :deep(.md-editor-toolbar svg) {
  color: var(--dark-text-primary);
  fill: var(--dark-text-primary);
}

[data-theme="dark"] :deep(.md-editor-toolbar .active svg) {
  color: var(--primary-color-dark);
  fill: var(--primary-color-dark);
}

[data-theme="dark"] :deep(.md-editor-input) {
  background-color: var(--dark-bg);
  color: var(--dark-text-primary);
}

[data-theme="dark"] :deep(.md-editor-content .show-html-type) {
  background-color: var(--dark-bg);
  color: var(--dark-text-primary);
}

[data-theme="dark"] .text-count {
  color: var(--dark-text-secondary);
}
</style>