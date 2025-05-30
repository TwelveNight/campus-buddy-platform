<template>
  <div class="mobile-bottom-nav" v-if="isMobile && shouldShowBottomNav">
    <div class="bottom-nav-container">
      <div
        v-for="(item, index) in navItems"
        :key="index"
        class="nav-item"
        :class="{ active: isActive(item.path), disabled: item.disabled }"
        @click="handleNavClick(item)"
      >
        <!-- 图标和徽章 -->
        <div class="nav-icon">
          <el-badge v-if="item.badge" :value="item.badge" :max="99" type="danger">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </el-badge>
          <el-icon v-else>
            <component :is="item.icon" />
          </el-icon>
        </div>
        
        <!-- 文字标签 - 简化显示 -->
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>
    <div class="safe-area-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { useMobileNav } from './useMobileNav'
const { navItems, isMobile, shouldShowBottomNav, isActive, handleNavClick } = useMobileNav()
</script>

<style scoped>
/* 基础样式 */
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: var(--card-bg, #ffffff);
  border-top: 1px solid var(--border-lighter, rgba(0,0,0,0.07));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
  display: none;
}

@media (max-width: 768px) {
  .mobile-bottom-nav {
    display: block;
  }
}

/* 容器布局 */
.bottom-nav-container {
  display: flex;
  padding: 4px 0;
  width: 100%;
  justify-content: space-around;
  align-items: center;
}

/* 导航项 */
.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 0;
  cursor: pointer;
  position: relative;
  margin: 0 2px;
  min-height: 56px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.nav-item:active {
  opacity: 0.7;
}

/* 图标样式 */
.nav-icon {
  margin-bottom: 4px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon .el-icon {
  font-size: 22px;
  transition: transform 0.2s ease;
}

/* 活跃状态 */
.nav-item.active {
  color: var(--primary-color, #409EFF);
}

.nav-item.active .nav-icon .el-icon {
  transform: scale(1.1);
}

/* 文字标签 - 重要修复 */
.nav-label {
  font-size: 10px;
  line-height: 1;
  font-weight: 500;
  text-align: center;
  max-width: 100%;
  display: block;
  white-space: nowrap;
  overflow: visible;
  padding: 0 2px;
  margin-top: 2px;
}

.nav-item.active .nav-label {
  font-weight: 600;
}

/* 禁用状态 */
.nav-item.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* 徽章样式 */
:deep(.el-badge__content) {
  font-size: 10px;
  height: 16px;
  min-width: 16px;
  line-height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  font-weight: bold;
  transform: translate(50%, -50%) scale(0.9);
}

/* 安全区域 */
.safe-area-placeholder {
  height: env(safe-area-inset-bottom, 0px);
  background-color: var(--card-bg, #ffffff);
  min-height: 6px;
}

/* 暗黑模式 */
[data-theme="dark"] .mobile-bottom-nav,
[data-theme="dark"] .safe-area-placeholder {
  background-color: var(--dark-card-bg, #1e1e1e);
  border-top-color: var(--dark-border-color, #333);
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.25);
}

[data-theme="dark"] .nav-item {
  color: var(--dark-text-secondary, #aaa);
}

[data-theme="dark"] .nav-item.active {
  color: var(--primary-color-dark, #79bbff);
}

/* 响应式调整 */
@media (max-width: 320px) {
  /* 特小屏幕 - iPhone 5/SE 等 */
  .nav-label {
    font-size: 9px;
  }
  
  .nav-icon .el-icon {
    font-size: 20px;
  }
  
  .nav-item {
    padding: 4px 0;
    min-height: 52px;
  }
}

@media (min-width: 375px) and (max-width: 390px) {
  /* iPhone X/11/12/13 */
  .nav-label {
    font-size: 10px;
  }
}

@media (min-width: 391px) and (max-width: 480px) {
  /* 稍大的手机 */
  .nav-icon .el-icon {
    font-size: 24px;
  }
  
  .nav-label {
    margin-top: 3px;
  }
}

@media (min-width: 481px) and (max-width: 768px) {
  /* 平板/大屏手机 */
  .nav-icon .el-icon {
    font-size: 24px;
  }
  
  .nav-label {
    font-size: 11px;
    margin-top: 4px;
  }
  
  .nav-item {
    min-height: 60px;
  }
}

/* 横屏模式 */
@media (orientation: landscape) and (max-height: 500px) {
  .nav-item {
    min-height: 46px;
    flex-direction: row;
    justify-content: center;
    padding: 4px;
  }
  
  .nav-icon {
    margin-bottom: 0;
    margin-right: 6px;
  }
  
  .nav-icon .el-icon {
    font-size: 18px;
  }
  
  .nav-label {
    font-size: 10px;
  }
}

/* 无动画模式 */
@media (prefers-reduced-motion: reduce) {
  .nav-item, 
  .nav-icon .el-icon {
    transition: none;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .mobile-bottom-nav {
    border-top-width: 2px;
  }
  
  .nav-item.active {
    outline: 2px solid var(--primary-color, #409EFF);
  }
  
  .nav-label {
    font-weight: 700;
  }
}
</style>
