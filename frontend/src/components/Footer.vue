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
    background-color: #f5f7fa;
    padding: 20px 0;
    margin-top: 40px;
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
  color: #606266;
  font-size: 14px;
}
.links {
  display: flex;
  gap: 15px;
}
.links a {
  color: #606266;
  text-decoration: none;
  transition: color 0.3s;
}
.links a:hover {
  color: #409EFF;
}
</style>
