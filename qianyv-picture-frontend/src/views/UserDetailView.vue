<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const userId = route.params.id
</script>

<template>
  <div class="user-detail-view">
    <div class="user-card">
      <h2>用户详情页</h2>
      <p>用户ID: {{ userId }}</p>
      <p class="hint">此页面为静态展示，详细功能待开发...</p>
    </div>
  </div>
</template>

<style scoped>
.user-detail-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px 20px;
  width: 100%;
}

.user-card {
  width: 100%;
  max-width: 600px;
  text-align: center;
  padding: 40px 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(26, 160, 193, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.user-card h2 {
  font-size: 32px;
  color: #1aa0c1;
  margin-bottom: 16px;
  font-weight: 700;
}

.user-card p {
  font-size: 18px;
  color: #2c5282;
  margin: 8px 0;
}

.user-card .hint {
  font-size: 14px;
  color: #5a7a95;
  margin-top: 20px;
}
</style>
