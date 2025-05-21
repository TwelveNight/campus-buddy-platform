<template>
    <nav class="navbar">
        <router-link to="/">首页</router-link>
        <router-link to="/helpinfo">互助信息</router-link>
        <router-link to="/login">登录</router-link>
        <router-link to="/register">注册</router-link>
    </nav>
</template>
<script setup lang="ts">
// TODO: 登录态切换
</script>
<style scoped>
.navbar {
    display: flex;
    gap: 20px;
    padding: 16px;
    background: #f5f5f5;
}
</style>
