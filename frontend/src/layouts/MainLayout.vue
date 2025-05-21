<template>
    <div>
        <NavBar />
        <main style="min-height: 60vh;">
            <router-view />
        </main>
        <Footer />
    </div>
</template>
<script setup lang="ts">
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
</script>
