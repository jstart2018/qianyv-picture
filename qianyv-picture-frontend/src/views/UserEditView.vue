<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import * as userApi from '@/api/userController'
import UserAvatar from '@/components/UserAvatar.vue'
import AvatarCropper from '@/components/AvatarCropper.vue'

const route = useRoute()
const router = useRouter()

// 用户ID
const userId = ref<string | number>('')

// 用户完整信息（只读展示）
const userInfo = ref<API.UserInfoVO | null>(null)

// 可编辑的表单数据（只包含 UserEditDTO 的字段）
const editForm = ref<API.UserEditDTO>({
  nickname: '',
  gender: undefined,
  introduction: '',
})

// 加载状态
const loading = ref(false)
const submitting = ref(false)
// 编辑模式状态
const isEditing = ref(false)
// 头像上传状态
const uploadingAvatar = ref(false)
// 文件输入框引用
const avatarFileInput = ref<HTMLInputElement | null>(null)
// 头像裁剪弹窗状态
const showCropper = ref(false)
const cropperFile = ref<File | null>(null)

// 修改密码弹窗状态
const showPasswordModal = ref(false)
// 密码修改表单
const passwordForm = ref({
  type: 'email', // email 或 phone
  code: '',
  newPassword: '',
  confirmPassword: '',
})
// 验证码发送状态
const sendingCode = ref(false)
// 倒计时状态
const countdown = ref(0)
// 密码修改提交状态
const changingPassword = ref(false)

// 性别选项
const genderOptions = [
  { label: '保密', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value = true
    // @ts-ignore - ID作为字符串传递，避免大整数精度丢失
    const res = await userApi.getById({ id: userId.value })
    if (res.data?.code === 0 && res.data?.data) {
      userInfo.value = res.data.data
      // 初始化可编辑表单数据
      editForm.value = {
        nickname: res.data.data.nickname || '',
        gender: undefined, // UserInfoVO 不包含 gender 字段，使用默认值
        introduction: res.data.data.introduction || '',
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 提交更新
const handleSubmit = async () => {
  try {
    // 校验昵称
    if (!editForm.value.nickname || editForm.value.nickname.trim() === '') {
      message.warning('昵称不能为空')
      return
    }

    submitting.value = true
    // @ts-ignore - ID作为字符串传递，避免大整数精度丢失
    const res = await userApi.update(
      { id: userId.value },
      {
        nickname: editForm.value.nickname.trim(),
        gender: editForm.value.gender,
        introduction: editForm.value.introduction?.trim() || '',
      },
    )

    if (res.data) {
      message.success('更新成功')
      // 更新成功后，退出编辑模式，继续在编辑页面
      isEditing.value = false
    } else {
      message.error('更新失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    message.error('更新失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  if (isEditing.value) {
    // 如果在编辑模式，重置表单数据后退出编辑
    editForm.value = {
      nickname: userInfo.value?.nickname || '',
      gender: undefined,
      introduction: userInfo.value?.introduction || '',
    }
    isEditing.value = false
  } else {
    // 如果不在编辑模式，直接返回
    router.back()
  }
}

// 进入编辑模式
const handleEdit = () => {
  isEditing.value = true
}

// 复制用户ID
const handleCopyId = async () => {
  try {
    await navigator.clipboard.writeText(String(userId.value))
    message.success('已复制用户ID')
  } catch (error) {
    console.error('复制失败:', error)
    message.error('复制失败')
  }
}

// 点击更换头像按钒
const handleAvatarClick = () => {
  avatarFileInput.value?.click()
}

// 处理头像文件选择 - 打开裁剪弹窗
const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }

  // 校验文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过5MB')
    return
  }

  // 打开裁剪弹窗
  cropperFile.value = file
  showCropper.value = true

  // 清空文件输入，允许上传相同文件
  if (target) {
    target.value = ''
  }
}

// 裁剪完成后上传头像
const handleCropConfirm = async (_blob: Blob, croppedFile: File) => {
  try {
    uploadingAvatar.value = true

    // 创建 FormData
    const formData = new FormData()
    formData.append('file', croppedFile)

    // 调用上传接口
    const res = await userApi.updateAvatar(formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (res.data?.code === 0 && res.data?.data) {
      message.success('头像更换成功')
      // 更新用户信息，刷新头像显示
      if (userInfo.value) {
        userInfo.value.avatar = res.data.data
      }
    } else {
      message.error('头像更换失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    message.error('上传失败，请稍后重试')
  } finally {
    uploadingAvatar.value = false
  }
}

// 裁剪取消
const handleCropCancel = () => {
  cropperFile.value = null
}

// 打开修改密码弹窗
const handleChangePasswordClick = () => {
  showPasswordModal.value = true
  // 重置表单
  passwordForm.value = {
    type: 'email',
    code: '',
    newPassword: '',
    confirmPassword: '',
  }
  countdown.value = 0
}

// 关闭弹窗
const handleClosePasswordModal = () => {
  showPasswordModal.value = false
  passwordForm.value = {
    type: 'email',
    code: '',
    newPassword: '',
    confirmPassword: '',
  }
  countdown.value = 0
}

// 发送验证码
const handleSendCode = async () => {
  try {
    // 检查是否已绑定
    const bindInfo =
      passwordForm.value.type === 'email' ? userInfo.value?.email : userInfo.value?.phone
    if (!bindInfo) {
      message.warning(
        `您未绑定${passwordForm.value.type === 'email' ? '邮箱' : '手机号'}，请选择其他方式`,
      )
      return
    }

    sendingCode.value = true
    const res = await userApi.sendPasswordCode({ type: passwordForm.value.type })

    if (res.data?.code === 0) {
      message.success('验证码已发送')
      // 开始倒计时60秒
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      message.error(res.data?.message || '发送失败')
    }
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    message.error(error?.response?.data?.message || '发送失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

// 校验密码格式
const validatePassword = (password: string): boolean => {
  // 6-16位，必须包含数字和字母
  if (password.length < 6 || password.length > 16) {
    message.warning('密码长度必须为6-16位')
    return false
  }
  if (!/\d/.test(password)) {
    message.warning('密码必须包含数字')
    return false
  }
  if (!/[a-zA-Z]/.test(password)) {
    message.warning('密码必须包含字母')
    return false
  }
  return true
}

// 提交密码修改
const handlePasswordSubmit = async () => {
  try {
    // 校验验证码
    if (!passwordForm.value.code || passwordForm.value.code.trim() === '') {
      message.warning('请输入验证码')
      return
    }

    // 校验新密码
    if (!passwordForm.value.newPassword || passwordForm.value.newPassword.trim() === '') {
      message.warning('请输入新密码')
      return
    }

    if (!validatePassword(passwordForm.value.newPassword)) {
      return
    }

    // 校验确认密码
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      message.warning('两次输入的密码不一致')
      return
    }

    changingPassword.value = true
    const res = await userApi.changePassword({
      type: passwordForm.value.type,
      code: passwordForm.value.code.trim(),
      newPassword: passwordForm.value.newPassword.trim(),
    })

    if (res.data?.code === 0) {
      message.success('密码修改成功')
      handleClosePasswordModal()
    } else {
      message.error(res.data?.message || '修改失败')
    }
  } catch (error: any) {
    console.error('修改密码失败:', error)
    message.error(error?.response?.data?.message || '修改失败，请稍后重试')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  userId.value = route.params.id as string
  if (userId.value) {
    fetchUserInfo()
  }
})
</script>

<template>
  <div class="user-edit-view">
    <div class="edit-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <a-spin size="large" tip="加载中..." />
      </div>

      <!-- 编辑表单 -->
      <div v-else-if="userInfo" class="edit-content">
        <!-- 左侧：头像和只读信息 -->
        <div class="left-section">
          <!-- 头像卡片 -->
          <div class="avatar-card">
            <UserAvatar :nickname="userInfo.nickname" :avatar="userInfo.avatar" :size="120" />
            <div class="avatar-info">
              <p class="avatar-tip">当前头像</p>
              <!-- 隐藏的文件输入框 -->
              <input
                ref="avatarFileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAvatarChange"
              />
              <button class="special-btn" :disabled="uploadingAvatar" @click="handleAvatarClick">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  class="btn-icon"
                >
                  <path
                    d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"
                  />
                  <circle cx="12" cy="13" r="4" />
                </svg>
                {{ uploadingAvatar ? '上传中...' : '更换头像' }}
              </button>
              <!-- 用户ID信息 -->
              <div class="user-id-section">
                <div class="user-id-label">你的ID</div>
                <div class="user-id-display">
                  <span class="user-id-value">{{ userId }}</span>
                  <button class="copy-btn" @click="handleCopyId" title="复制">
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      class="copy-icon"
                    >
                      <path
                        d="M16 4h2a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"
                      />
                      <rect x="8" y="2" width="8" height="4" rx="1" ry="1" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 只读信息卡片 -->
          <div class="readonly-card">
            <h3 class="card-title">账号信息</h3>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userInfo.email || '未绑定' }}</span>
              <button class="special-btn small" disabled>
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  class="btn-icon"
                >
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                修改
              </button>
            </div>
            <div class="info-item">
              <span class="info-label">手机</span>
              <span class="info-value">{{ userInfo.phone || '未绑定' }}</span>
              <button class="special-btn small" disabled>
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  class="btn-icon"
                >
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                修改
              </button>
            </div>
            <div class="info-item full">
              <button class="special-btn primary" @click="handleChangePasswordClick">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  class="btn-icon"
                >
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
                修改密码
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧：可编辑表单 -->
        <div class="right-section">
          <div class="form-card">
            <div class="form-card-header">
              <h3 class="card-title">基本资料</h3>
              <button v-if="!isEditing" class="edit-btn" @click="handleEdit">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  class="btn-icon"
                >
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                编辑
              </button>
            </div>

            <a-form :model="editForm" layout="vertical" class="edit-form">
              <!-- 昵称 -->
              <a-form-item label="昵称" required>
                <a-input
                  v-model:value="editForm.nickname"
                  placeholder="请输入昵称"
                  size="large"
                  :maxlength="20"
                  show-count
                  :disabled="!isEditing"
                />
              </a-form-item>

              <!-- 性别 -->
              <a-form-item label="性别">
                <a-radio-group v-model:value="editForm.gender" size="large" :disabled="!isEditing">
                  <a-radio-button
                    v-for="option in genderOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </a-radio-button>
                </a-radio-group>
              </a-form-item>

              <!-- 个人简介 -->
              <a-form-item label="个人简介">
                <a-textarea
                  v-model:value="editForm.introduction"
                  placeholder="介绍一下你自己吧~"
                  :rows="6"
                  :maxlength="200"
                  show-count
                  size="large"
                  :disabled="!isEditing"
                />
              </a-form-item>

              <!-- 操作按钮 -->
              <div v-if="isEditing" class="form-actions">
                <a-button size="large" @click="handleCancel">取消</a-button>
                <a-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
                  保存修改
                </a-button>
              </div>
            </a-form>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="showPasswordModal"
      title="修改密码"
      :footer="null"
      :width="500"
      @cancel="handleClosePasswordModal"
    >
      <div class="password-modal-content">
        <!-- 验证方式选择 -->
        <div class="form-section">
          <label class="form-label">验证方式</label>
          <a-radio-group v-model:value="passwordForm.type" size="large" class="verify-type-group">
            <a-radio-button value="email">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                class="type-icon"
              >
                <path
                  d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                />
                <polyline points="22,6 12,13 2,6" />
              </svg>
              邮箱验证
            </a-radio-button>
            <a-radio-button value="phone">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                class="type-icon"
              >
                <path
                  d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"
                />
              </svg>
              手机验证
            </a-radio-button>
          </a-radio-group>
          <p class="bind-hint">
            {{
              passwordForm.type === 'email'
                ? `当前邮箱：${userInfo?.email || '未绑定'}`
                : `当前手机：${userInfo?.phone || '未绑定'}`
            }}
          </p>
        </div>

        <!-- 验证码 -->
        <div class="form-section">
          <label class="form-label">验证码</label>
          <div class="code-input-group">
            <a-input
              v-model:value="passwordForm.code"
              placeholder="请输入验证码"
              size="large"
              :maxlength="6"
            />
            <a-button
              size="large"
              :disabled="sendingCode || countdown > 0"
              :loading="sendingCode"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </a-button>
          </div>
        </div>

        <!-- 新密码 -->
        <div class="form-section">
          <label class="form-label">新密码</label>
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入6-16位密码，必须包含数字和字母"
            size="large"
            :maxlength="16"
          />
        </div>

        <!-- 确认密码 -->
        <div class="form-section">
          <label class="form-label">确认密码</label>
          <a-input-password
            v-model:value="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
            size="large"
            :maxlength="16"
          />
        </div>

        <!-- 提示信息 -->
        <div class="password-tips">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            class="tip-icon"
          >
            <circle cx="12" cy="12" r="10" />
            <path d="M12 16v-4" />
            <path d="M12 8h.01" />
          </svg>
          <span>密码需为6-16位，并且必须包含数字和字母</span>
        </div>

        <!-- 操作按钒 -->
        <div class="modal-actions">
          <a-button size="large" @click="handleClosePasswordModal">取消</a-button>
          <a-button
            type="primary"
            size="large"
            :loading="changingPassword"
            @click="handlePasswordSubmit"
          >
            确认修改
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 头像裁剪弹窗 -->
    <AvatarCropper
      v-model:visible="showCropper"
      :image-file="cropperFile"
      @confirm="handleCropConfirm"
      @cancel="handleCropCancel"
    />
  </div>
</template>

<style scoped>
.user-edit-view {
  min-height: calc(100vh - 80px);
  padding: 20px 20px;
}

.edit-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 加载状态 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

/* 编辑内容区域 */
.edit-content {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
}

/* 左侧区域 */
.left-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 头像卡片 */
.avatar-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(26, 160, 193, 0.1);
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.avatar-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-tip {
  font-size: 14px;
  color: rgba(15, 43, 59, 0.6);
  margin: 0;
}

/* 用户ID信息区域 */
.user-id-section {
  width: 100%;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(26, 160, 193, 0.1);
}

.user-id-label {
  font-size: 12px;
  font-weight: 600;
  color: rgba(15, 43, 59, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.user-id-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(26, 160, 193, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(26, 160, 193, 0.15);
}

.user-id-value {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary);
  font-family: 'Courier New', monospace;
  word-break: break-all;
}

.copy-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 8px;
  border: 1px solid rgba(26, 160, 193, 0.3);
  background: rgba(255, 255, 255, 0.8);
  color: var(--primary);
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.copy-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: var(--primary);
  transform: scale(1.05);
}

.copy-btn:active {
  transform: scale(0.95);
}

.copy-icon {
  width: 16px;
  height: 16px;
}

/* 只读信息卡片 */
.readonly-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(26, 160, 193, 0.1);
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(26, 160, 193, 0.1);
}

.info-item.full {
  flex-direction: column;
  align-items: stretch;
  border-bottom: none;
  padding-top: 20px;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: rgba(15, 43, 59, 0.6);
  min-width: 60px;
}

.info-value {
  flex: 1;
  font-size: 15px;
  color: var(--text);
}

/* 特殊按针样式 */
.special-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(26, 160, 193, 0.3);
  background: rgba(255, 255, 255, 0.8);
  color: var(--primary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.special-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.95);
  border-color: var(--primary);
  transform: translateY(-1px);
}

.special-btn:active:not(:disabled) {
  transform: translateY(0);
}

.special-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.special-btn.small {
  padding: 6px 12px;
  font-size: 13px;
}

.special-btn.primary {
  width: 100%;
  justify-content: center;
  padding: 12px 20px;
  font-size: 15px;
}

.special-btn:disabled {
  opacity: 0.5;
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.disabled-tip {
  font-size: 12px;
  color: rgba(15, 43, 59, 0.4);
  margin-top: 4px;
}

/* 右侧表单区域 */
.right-section {
  display: flex;
  flex-direction: column;
}

.form-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(26, 160, 193, 0.1);
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.form-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(26, 160, 193, 0.3);
  background: rgba(255, 255, 255, 0.8);
  color: var(--primary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

.edit-btn:active {
  background: rgba(255, 255, 255, 0.7);
}

.edit-form {
  margin-top: 24px;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(26, 160, 193, 0.1);
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .edit-content {
    grid-template-columns: 1fr;
  }

  .left-section {
    flex-direction: row;
  }

  .avatar-card,
  .readonly-card {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .left-section {
    flex-direction: column;
  }

  .page-title {
    font-size: 24px;
  }

  .edit-container {
    padding: 20px 12px;
  }

  .form-card {
    padding: 24px 16px;
  }
}

/* 修改密码弹窗样式 */
.password-modal-content {
  padding: 20px 0;
}

.form-section {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 12px;
}

.verify-type-group {
  width: 100%;
  display: flex;
  gap: 12px;
}

.verify-type-group :deep(.ant-radio-button-wrapper) {
  flex: 1;
  text-align: center;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 12px;
  border: 1px solid rgba(26, 160, 193, 0.3);
  transition: all 0.3s ease;
}

.verify-type-group :deep(.ant-radio-button-wrapper:hover) {
  border-color: var(--primary);
}

.verify-type-group :deep(.ant-radio-button-wrapper-checked) {
  background: rgba(26, 160, 193, 0.1);
  border-color: var(--primary);
  color: var(--primary);
}

.type-icon {
  width: 18px;
  height: 18px;
}

.bind-hint {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(15, 43, 59, 0.6);
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input-group :deep(.ant-input) {
  flex: 1;
}

.code-input-group button {
  min-width: 120px;
}

.password-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(26, 160, 193, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(26, 160, 193, 0.15);
  margin-bottom: 24px;
}

.tip-icon {
  width: 20px;
  height: 20px;
  color: var(--primary);
  flex-shrink: 0;
}

.password-tips span {
  font-size: 13px;
  color: rgba(15, 43, 59, 0.7);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.modal-actions button {
  min-width: 100px;
}
</style>
