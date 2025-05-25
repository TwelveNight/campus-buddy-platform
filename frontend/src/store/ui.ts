import { ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useUiStore = defineStore('ui', () => {
  // 全局加载状态
  const isLoading = ref(false)
  const loadingText = ref('')
  
  // 页面过渡动画状态
  const pageTransition = ref(true)
  
  // 设置加载状态
  const setLoading = (loading: boolean, text = '加载中...') => {
    isLoading.value = loading
    loadingText.value = text
  }
  
  // 显示加载动画并执行异步操作
  const withLoading = async <T>(promise: Promise<T>, text = '加载中...'): Promise<T> => {
    try {
      setLoading(true, text)
      return await promise
    } finally {
      setLoading(false)
    }
  }

  // 显示操作成功消息
  const showSuccess = (message: string) => {
    ElMessage({
      message,
      type: 'success',
      duration: 2000,
      showClose: true,
    })
  }

  // 显示操作错误消息
  const showError = (message: string) => {
    ElMessage({
      message,
      type: 'error',
      duration: 3000,
      showClose: true,
    })
  }

  // 表单验证辅助方法
  const validateForm = async (formRef: any): Promise<boolean> => {
    if (!formRef) return false
    
    return new Promise((resolve) => {
      formRef.validate((valid: boolean) => {
        if (!valid) {
          // 自动滚动到第一个错误字段
          const firstErrorField = document.querySelector('.el-form-item.is-error')
          if (firstErrorField) {
            firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' })
          }
          ElMessage.warning('请正确填写表单信息')
        }
        resolve(valid)
      })
    })
  }

  // 切换页面过渡动画
  const togglePageTransition = () => {
    pageTransition.value = !pageTransition.value
  }

  return {
    isLoading,
    loadingText,
    pageTransition,
    setLoading,
    withLoading,
    showSuccess,
    showError,
    validateForm,
    togglePageTransition
  }
})
