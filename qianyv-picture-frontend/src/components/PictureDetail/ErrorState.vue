<script setup lang="ts">
interface Props {
  error: string
}

interface Emits {
  (e: 'retry'): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()
</script>

<template>
  <div class="error-container">
    <div class="error-icon">⚠️</div>
    <p class="error-text">{{ error }}</p>
    <button class="retry-btn" @click="emit('retry')">重试</button>
  </div>
</template>

<style scoped>
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 20px;
}

.error-icon {
  font-size: 64px;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-10px);
  }
  75% {
    transform: translateX(10px);
  }
}

.error-text {
  font-size: 18px;
  color: rgba(255, 100, 100, 0.9);
  margin: 0;
  text-align: center;
}

.retry-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, rgba(138, 180, 248, 0.9), rgba(107, 157, 232, 0.9));
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.3);
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(138, 180, 248, 0.4);
}

.retry-btn:active {
  transform: translateY(0);
}
</style>
