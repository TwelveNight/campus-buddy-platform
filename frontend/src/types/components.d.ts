// 为 AvatarUploader 组件添加显式类型声明
declare module '@/components/form/AvatarUploader.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 为相对路径导入添加声明
declare module '../../components/form/AvatarUploader.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
