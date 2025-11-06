<script setup lang="ts">
import { parseTags } from '@/utils/parse'

interface Props {
  tags: string
}

defineProps<Props>()
</script>

<template>
  <div class="tags-section" v-if="tags && parseTags(tags).length > 0">
    <div class="tags-header">
      <!-- 线条风格的标签图标 -->
      <svg class="tags-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path
          d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"
        ></path>
        <line x1="7" y1="7" x2="7.01" y2="7"></line>
      </svg>
      <span class="tags-title">相关标签</span>
    </div>
    <div class="tags-container">
      <span v-for="(tag, index) in parseTags(tags)" :key="index" class="tag-item">
        <!-- 青柠切片图标 -->
        <svg
          class="lime-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="2" x2="12" y2="22"></line>
          <line x1="2" y1="12" x2="22" y2="12"></line>
          <line x1="5.6" y1="5.6" x2="18.4" y2="18.4"></line>
          <line x1="5.6" y1="18.4" x2="18.4" y2="5.6"></line>
          <circle cx="12" cy="12" r="3"></circle>
        </svg>
        {{ tag }}
      </span>
    </div>
  </div>
</template>

<style scoped>
/* ========== 标签区域 ========== */
.tags-section {
  position: absolute;
  left: 100px;
  top: 730px;
  width: 12000px;
  background: transparent;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  border-radius: 0;
  padding: 20px 20px 24px 20px;
  box-shadow: none;
  border: none;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.tags-icon {
  width: 20px;
  height: 20px;
  color: rgba(138, 180, 248, 0.9);
}

.tags-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 0.5px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(138, 180, 248, 0.15), rgba(107, 157, 232, 0.12));
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 20px;
  font-size: 14px;
  color: rgba(138, 180, 248, 0.95);
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(138, 180, 248, 0.1);
}

.lime-icon {
  width: 14px;
  height: 14px;
  color: rgba(138, 180, 248, 0.8);
  transition: transform 0.3s ease;
}

.tag-item:hover {
  background: linear-gradient(135deg, rgba(138, 180, 248, 0.25), rgba(107, 157, 232, 0.2));
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.2);
}

.tag-item:hover .lime-icon {
  transform: rotate(90deg);
}

.tag-item:active {
  transform: translateY(0);
}
</style>
