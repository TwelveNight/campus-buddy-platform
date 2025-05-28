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
  padding: 30px 0 0 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.footer::before {
  content: '';
  display: block;
  position: absolute;
  top: 0; left: 0; right: 0; height: 2px; /* 减小高度 */
  background: linear-gradient(90deg, #409eff 0%, #8b5cf6 50%, #fbbf24 100%);
  filter: blur(3px); /* 减轻模糊效果 */
  opacity: 0.5; /* 降低不透明度 */
  z-index: 2;
}

@keyframes footer-glow {
  0% { filter: blur(3px) brightness(1); } /* 减轻模糊效果 */
  50% { filter: blur(5px) brightness(1.1); } /* 减轻模糊和亮度效果 */
  100% { filter: blur(3px) brightness(1); } /* 减轻模糊效果 */
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary);
  font-size: 15px;
  padding: 18px 0 8px 0;
  position: relative;
  z-index: 3;
}

.links {
  display: flex;
  gap: 28px;
}

.links a {
  color: var(--text-secondary);
  text-decoration: none;
  position: relative;
  padding: 5px 0;
  transition: color 0.3s, text-shadow 0.3s;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.links a::after {
  content: '';
  position: absolute;
  left: 0; bottom: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #409eff, #8b5cf6, #fbbf24);
  border-radius: 2px;
  transition: width 0.4s cubic-bezier(0.4,0,0.2,1);
  box-shadow: 0 2px 8px #409eff44;
}

.links a:hover {
  color: #409eff;
  text-shadow: 0 0 8px #409eff99, 0 0 16px #8b5cf6cc;
}

.links a:hover::after {
  width: 100%;
}

@media (max-width: 900px) {
  .container {
    max-width: 98vw;
    padding: 0 8px;
  }
  .footer-content {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  .links {
    gap: 16px;
  }
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
