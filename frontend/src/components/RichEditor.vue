<template>
    <div class="rich-editor">
        <el-form-item :label="label" :prop="prop" class="editor-form-item" :required="required">
            <div class="editor-container">
                <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" mode="default"
                    style="border-bottom: 1px solid #dcdfe6" />
                <Editor :style="editorStyle" v-model="valueHtml" :defaultConfig="editorConfig" mode="default"
                    @onCreated="handleCreated" @onChange="handleChange" />
            </div>
            <div class="text-count" v-if="showCount">
                {{ currentCount }}/{{ maxLength }}
            </div>
        </el-form-item>
    </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, computed, onBeforeUnmount, watch } from 'vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IEditorConfig, IToolbarConfig } from '@wangeditor/editor'

interface Props {
    modelValue: string
    label?: string
    prop?: string
    placeholder?: string
    height?: string
    maxLength?: number
    showCount?: boolean
    required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
    modelValue: '',
    label: '',
    prop: '',
    placeholder: '请输入内容...',
    height: '300px',
    maxLength: 1000,
    showCount: true,
    required: false
})

const emit = defineEmits(['update:modelValue', 'change'])

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

// 内容 HTML
const valueHtml = ref(props.modelValue)

// 工具栏配置
const toolbarConfig = {} as IToolbarConfig

// 编辑器配置
const editorConfig = computed<IEditorConfig>(() => ({
    placeholder: props.placeholder,
    maxLength: props.maxLength,
    MENU_CONF: {},
    customAlert: (info: string, type: string) => {
      // 你可以根据需要自定义弹窗，这里用 alert 兜底
      alert(`${type}: ${info}`)
    },
    scroll: true,
    readOnly: false,
    autoFocus: false
}))

// 编辑器样式
const editorStyle = computed(() => {
    return {
        height: props.height,
        overflowY: 'hidden'
    }
})

// 当前文字数量
const currentCount = ref(0)

// 组件销毁时，也必须销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

// 初始化编辑器
const handleCreated = (editor: any) => {
    editorRef.value = editor
    // 初始化文本计数
    if (props.modelValue) {
        currentCount.value = getTextCount(props.modelValue)
    }
}

// 监听内容变化
const handleChange = (editor: any) => {
    emit('update:modelValue', editor.getHtml())
    currentCount.value = getTextCount(editor.getHtml())
    emit('change', editor.getHtml())
}

// 获取文本长度（去除HTML标签后）
function getTextCount(html: string): number {
    if (!html) return 0

    // 创建临时元素解析HTML
    const temp = document.createElement('div')
    temp.innerHTML = html
    const text = temp.textContent || temp.innerText
    return text.length
}

// 监听父组件传入的值变化
watch(() => props.modelValue, (newValue) => {
    if (newValue !== valueHtml.value) {
        valueHtml.value = newValue
        currentCount.value = getTextCount(newValue)
    }
})
</script>

<style scoped>
.rich-editor {
    margin-bottom: 20px;
}

.editor-container {
    border: 1px solid #dcdfe6;
    z-index: 100;
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
</style>
