<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <span>© 2025 学伴校园互助与资源共享平台</span>
        <div class="links">
          <a href="#">关于我们</a>
          <a href="#">使用帮助</a>
          <a href="#">隐私政策</a>
          <a href="#">联系我们</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
// 无需任何逻辑
</script>

<style scoped>
.footer {
  background-color: var(--card-bg);
  padding: 30px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary);
  font-size: 14px;
}

.links {
  display: flex;
  gap: 20px;
}

.links a {
  color: var(--text-secondary);
  text-decoration: none;
  position: relative;
  padding: 5px 0;
  transition: color 0.3s;
}

.links a:hover {
  color: var(--primary-color);
}

/* 添加美观的下划线效果 */
.links a::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: 0;
  left: 0;
  background-color: var(--primary-color);
  transition: width 0.3s ease;
}

.links a:hover::after {
  width: 100%;
}

/* 暗色模式适配 */
[data-theme="dark"] .footer {
  background-color: var(--dark-card-bg);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .footer-content {
  color: var(--dark-text-secondary);
}

[data-theme="dark"] .links a {
  color: var(--dark-text-secondary);
}

[data-theme="dark"] .links a:hover {
  color: var(--primary-color-dark);
}

[data-theme="dark"] .links a::after {
  background-color: var(--primary-color-dark);
}
</style>
