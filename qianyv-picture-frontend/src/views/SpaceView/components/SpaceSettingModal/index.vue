<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getSpaceInfo, editSpace, upgradeSpace } from '@/api/spaceController'

interface Props {
  show: boolean
  spaceId: number | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'updated'): void
}>()

// 空间详情
const spaceDetail = ref<any>(null)
const loading = ref(false)
const editingName = ref(false)
const newSpaceName = ref('')

// 升级弹窗
const showUpgrade = ref(false)
const upgradeOptions = ref<{ key: number; title: string; desc: string }[]>([])

// 空间级别映射
const levelMap: Record<number, { label: string; class: string }> = {
  0: { label: '普通版', class: 'level-0' },
  1: { label: '专业版', class: 'level-1' },
  2: { label: '旗舰版', class: 'level-2' },
}

// 获取空间详情
const fetchSpaceDetail = async () => {
  if (!props.spaceId) return

  loading.value = true
  try {
    const res = await getSpaceInfo({ spaceId: props.spaceId })
    if (res.data && res.data.code === 0 && res.data.data) {
      spaceDetail.value = res.data.data
      newSpaceName.value = res.data.data.spaceName || ''
    } else {
      message.error(res.data?.message || '获取空间信息失败')
    }
  } catch (err) {
    console.error('获取空间信息失败:', err)
    message.error('获取空间信息失败')
  } finally {
    loading.value = false
  }
}

// 监听弹窗显示
watch(
  () => props.show,
  (show) => {
    if (show && props.spaceId) {
      fetchSpaceDetail()
      editingName.value = false
    }
  },
)

// 关闭弹窗
const handleClose = () => {
  emit('close')
}

// 开始编辑名称
const startEditName = () => {
  newSpaceName.value = spaceDetail.value?.spaceName || ''
  editingName.value = true
}

// 取消编辑名称
const cancelEditName = () => {
  editingName.value = false
  newSpaceName.value = spaceDetail.value?.spaceName || ''
}

// 保存空间名称
const saveSpaceName = async () => {
  if (!props.spaceId) return
  const name = newSpaceName.value.trim()
  if (!name) {
    message.warning('空间名称不能为空')
    return
  }
  if (name === spaceDetail.value?.spaceName) {
    editingName.value = false
    return
  }

  try {
    const res = await editSpace({
      spaceId: props.spaceId,
      name: name,
    })
    if (res.data && res.data.code === 0) {
      message.success('空间名称修改成功')
      spaceDetail.value.spaceName = name
      editingName.value = false
      emit('updated')
    } else {
      message.error(res.data?.message || '修改失败')
    }
  } catch (err) {
    console.error('修改空间名称失败:', err)
    message.error('修改失败，请重试')
  }
}

// 打开升级弹窗
const openUpgradeModal = () => {
  if (!spaceDetail.value) return

  const currentLevel = spaceDetail.value.spaceLevel || 0
  upgradeOptions.value = [
    { key: 1, title: '专业版', desc: '更大容量，适合小团队使用' },
    { key: 2, title: '旗舰版', desc: '无限可能，适合企业级应用' },
  ].filter((opt) => opt.key > currentLevel)

  if (upgradeOptions.value.length === 0) {
    message.info('您已经是最高级别')
    return
  }

  showUpgrade.value = true
}

const closeUpgradeModal = () => {
  showUpgrade.value = false
}

// 处理升级
const handleUpgrade = async (level: number) => {
  if (!props.spaceId) return

  closeUpgradeModal()

  try {
    const res = await upgradeSpace({
      spaceId: props.spaceId,
      level: level,
    })
    if (res.data && res.data.code === 0) {
      message.success('升级成功')
      await fetchSpaceDetail()
      emit('updated')
    } else {
      message.error(res.data?.message || '升级失败')
    }
  } catch (err) {
    console.error('升级失败:', err)
    message.error('升级失败，请重试')
  }
}

// 格式化文件大小
const formatFileSize = (size?: number) => {
  if (!size) return '0 MB'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}
</script>

<template>
  <Teleport to="body">
    <!-- 主设置弹窗 -->
    <Transition name="modal-fade">
      <div v-if="show" class="modal-overlay" @click="handleClose">
        <div class="modal-container" @click.stop>
          <!-- 头部 -->
          <div class="modal-header">
            <h3>空间设置</h3>
            <button class="close-btn" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
          </div>

          <!-- 内容 -->
          <div class="modal-body">
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>

            <div v-else-if="spaceDetail" class="space-info">
              <!-- 空间名称 -->
              <div class="info-item">
                <span class="info-label">空间名称</span>
                <div class="info-value name-edit-wrapper">
                  <template v-if="editingName">
                    <input
                      v-model="newSpaceName"
                      class="name-input"
                      maxlength="20"
                      @keyup.enter="saveSpaceName"
                    />
                    <button class="edit-btn save" @click="saveSpaceName">保存</button>
                    <button class="edit-btn cancel" @click="cancelEditName">取消</button>
                  </template>
                  <template v-else>
                    <span class="name-text">{{ spaceDetail.spaceName }}</span>
                    <button class="edit-btn" @click="startEditName">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                  </template>
                </div>
              </div>

              <!-- 空间级别 -->
              <div class="info-item">
                <span class="info-label">空间级别</span>
                <div class="level-wrapper">
                  <span class="level-badge" :class="levelMap[spaceDetail.spaceLevel]?.class">
                    {{ levelMap[spaceDetail.spaceLevel]?.label || '普通版' }}
                  </span>
                  <button
                    v-if="spaceDetail.spaceLevel < 2"
                    class="upgrade-btn"
                    @click="openUpgradeModal"
                  >
                    升级
                  </button>
                </div>
              </div>

              <!-- 容量信息 -->
              <div class="info-item capacity-item">
                <span class="info-label">存储容量</span>
                <div class="capacity-info">
                  <div class="capacity-bar">
                    <div
                      class="capacity-fill"
                      :style="{
                        width: `${Math.min((spaceDetail.totalSize / spaceDetail.maxSize) * 100, 100)}%`,
                      }"
                    ></div>
                  </div>
                  <span class="capacity-text">
                    {{ formatFileSize(spaceDetail.totalSize) }} /
                    {{ formatFileSize(spaceDetail.maxSize) }}
                  </span>
                </div>
              </div>

              <!-- 图片数量 -->
              <div class="info-item">
                <span class="info-label">图片数量</span>
                <span class="info-value">{{ spaceDetail.totalCount || 0 }} 张</span>
              </div>

              <!-- 成员数量 -->
              <div class="info-item">
                <span class="info-label">成员数量</span>
                <span class="info-value">{{ spaceDetail.memberCount || 1 }} 人</span>
              </div>

              <!-- 创建时间 -->
              <div class="info-item" v-if="spaceDetail.createTime">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ spaceDetail.createTime }}</span>
              </div>
            </div>

            <div v-else class="empty-state">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
              <p>暂无空间信息</p>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 升级弹窗 -->
    <Transition name="modal-fade">
      <div v-if="showUpgrade" class="modal-overlay" @click="closeUpgradeModal">
        <div class="upgrade-modal" @click.stop>
          <div class="upgrade-header">
            <h4>升级空间</h4>
            <button class="close-btn" @click="closeUpgradeModal">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
          </div>
          <div class="upgrade-options">
            <div
              v-for="opt in upgradeOptions"
              :key="opt.key"
              class="upgrade-option"
              @click="handleUpgrade(opt.key)"
            >
              <div class="option-title">{{ opt.title }}</div>
              <div class="option-desc">{{ opt.desc }}</div>
            </div>
          </div>
          <button class="cancel-btn" @click="closeUpgradeModal">取消</button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 480px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-bottom: 1px solid #e0f2fe;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #0369a1;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #64748b;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 1);
  color: #0369a1;
}

.close-btn svg {
  width: 18px;
  height: 18px;
}

.modal-body {
  padding: 24px;
  min-height: 200px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
  gap: 12px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #8ab4f8;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.space-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
}

.info-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 600;
}

.name-edit-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.name-input {
  padding: 6px 10px;
  border: 1px solid #8ab4f8;
  border-radius: 6px;
  font-size: 14px;
  width: 140px;
  outline: none;
}

.edit-btn {
  padding: 4px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  color: #0369a1;
}

.edit-btn svg {
  width: 16px;
  height: 16px;
}

.edit-btn.save {
  background: #8ab4f8;
  color: white;
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 12px;
}

.edit-btn.save:hover {
  background: #667eea;
}

.edit-btn.cancel {
  background: #e2e8f0;
  color: #64748b;
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 12px;
}

.edit-btn.cancel:hover {
  background: #cbd5e0;
}

.level-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.level-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.level-0 {
  background: #e0f2fe;
  color: #0369a1;
}

.level-1 {
  background: #fef3c7;
  color: #d97706;
}

.level-2 {
  background: #fce7f3;
  color: #db2777;
}

.upgrade-btn {
  padding: 4px 12px;
  background: linear-gradient(135deg, #8ab4f8 0%, #667eea 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.upgrade-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.4);
}

.capacity-item {
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.capacity-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.capacity-bar {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.capacity-fill {
  height: 100%;
  background: linear-gradient(90deg, #8ab4f8 0%, #667eea 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.capacity-text {
  font-size: 12px;
  color: #64748b;
  text-align: right;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #cbd5e0;
}

.empty-state svg {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* 升级弹窗样式 */
.upgrade-modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  padding: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: modalSlideIn 0.3s ease;
}

.upgrade-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.upgrade-header h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.upgrade-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.upgrade-option {
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.upgrade-option:hover {
  border-color: #8ab4f8;
  background: #f8fafc;
}

.option-title {
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 13px;
  color: #64748b;
}

.cancel-btn {
  width: 100%;
  padding: 10px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #e2e8f0;
}
</style>
