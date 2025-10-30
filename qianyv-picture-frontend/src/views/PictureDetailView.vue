<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const pictureId = ref<string>('')

onMounted(() => {
  pictureId.value = route.params.id as string
  console.log('图片ID:', pictureId.value)
  // TODO: 根据图片ID获取图片详情数据
})
</script>

<template>
  <div class="picture-detail-view">
    <div class="detail-container">
      <h1>图片详情页</h1>
      <p>图片 ID: {{ pictureId }}</p>
      <p class="placeholder-text">此页面正在开发中...</p>
    </div>
  </div>
</template>

<style scoped>
.picture-detail-view {
  width: 100%;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.detail-container {
  text-align: center;
  padding: 60px 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
}

.detail-container h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 16px;
}

.detail-container p {
  font-size: 18px;
  color: #4a5568;
  margin-bottom: 12px;
}

.placeholder-text {
  margin-top: 24px;
  font-style: italic;
  color: #a0aec0;
}
</style>
