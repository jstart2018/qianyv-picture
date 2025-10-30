<script setup lang="ts">
// 团队空间页面，暂时展示占位内容
</script>

<template>
  <div class="space-view">
    <div class="placeholder-box">
      <h2>团队空间</h2>
      <p>团队协作空间正在开发中，敬请期待...</p>
    </div>
  </div>
</template>

<style scoped>
.space-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  width: 100%;
}

.placeholder-box {
  text-align: center;
  padding: 40px 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(26, 160, 193, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.placeholder-box h2 {
  font-size: 32px;
  color: #1aa0c1;
  margin-bottom: 12px;
  font-weight: 700;
}

.placeholder-box p {
  font-size: 18px;
  color: #2c5282;
}
</style>
