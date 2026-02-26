<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/userController'

const router = useRouter()
const userStore = useUserStore()

// 登录方式：'code' - 验证码登录, 'password' - 账号密码登录
const loginType = ref<'code' | 'password'>('code')

const account = ref('')
const code = ref('')
const password = ref('')
const countdown = ref(0)
const loading = ref(false)
const error = ref('')

// 发送验证码
async function sendVerifyCode() {
  if (!account.value) {
    error.value = '请输入邮箱或手机号'
    return
  }

  if (countdown.value > 0) return

  try {
    loading.value = true
    error.value = ''

    // 判断是邮箱还是手机号
    const isEmail = account.value.includes('@')
    await userApi.sendCode(isEmail ? { email: account.value } : { phone: account.value })

    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e: any) {
    error.value = e?.message || '发送验证码失败'
  } finally {
    loading.value = false
  }
}

// 登录或注册
async function doLogin() {
  if (!account.value || !password.value) {
    error.value = '请填写完整信息'
    return
  }

  try {
    loading.value = true
    error.value = ''

    if (loginType.value === 'code') {
      // 验证码登录
      await userApi.loginWithCode({
        emailOrPhone: account.value,
        code: password.value,
      })
    } else {
      // 账号密码登录
      await userApi.loginWithPassword({
        account: account.value,
        password: password.value,
      })
    }

    // 登录成功后获取用户信息
    await userStore.fetchUser()
    router.push('/')
  } catch (e: any) {
    error.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-view">
    <!-- 底层背景卡片 -->
    <div class="background-card">
      <img
        src="https://qianyv-picture-1344284634.cos.ap-guangzhou.myqcloud.com/public/1986719802620301314/public/1986719802620301314/2026-02-21_7264290ca04b_t.png"
        alt="背景"
        class="bg-image"
      />
    </div>

    <!-- 顶层登录表单卡片 -->
    <div class="login-card">
      <!-- 登录方式切换 -->
      <div class="login-type-switch">
        <button :class="['type-btn', { active: loginType === 'code' }]" @click="loginType = 'code'">
          验证码登录
        </button>
        <button
          :class="['type-btn', { active: loginType === 'password' }]"
          @click="loginType = 'password'"
        >
          账号密码登录
        </button>
      </div>

      <div class="login-form">
        <!-- 账号/邮箱/手机号 -->
        <div class="form-item">
          <input
            v-model="account"
            type="text"
            :placeholder="loginType === 'code' ? '请输入邮箱或手机号' : '请输入账号'"
            @keyup.enter="doLogin"
          />
        </div>

        <!-- 验证码登录：验证码 + 发送按钮 -->
        <div v-if="loginType === 'code'" class="form-item">
          <div class="code-input-box">
            <input
              v-model="password"
              type="text"
              placeholder="请输入验证码"
              @keyup.enter="doLogin"
            />
            <button class="code-btn" :disabled="countdown > 0 || !account" @click="sendVerifyCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </div>
        </div>

        <!-- 账号密码登录：密码框 -->
        <div v-else class="form-item">
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="doLogin"
          />
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>

        <button class="submit-btn" :disabled="loading" @click="doLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-view {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

/* 底层背景卡片 */
.background-card {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.bg-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

/* 顶层登录表单卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  /* 白色半透明，透明度0.3 */
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 32px 36px;
}

/* 登录方式切换 */
.login-type-switch {
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 4px;
}

.type-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: #666;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-btn.active {
  background: rgba(0, 0, 0, 0.08);
  color: #000;
  box-shadow: none;
}

.type-btn:not(.active):hover {
  background: rgba(0, 0, 0, 0.05);
  color: #000;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid rgba(26, 160, 193, 0.25);
  border-radius: 12px;
  font-size: 15px;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.15);
  color: #333;
}

.form-item input:focus {
  border-color: rgba(0, 0, 0, 0.7);
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.15);
}

.form-item input::placeholder {
  color: #000;
}

.code-input-box {
  display: flex;
  gap: 12px;
}

.code-input-box input {
  flex: 1;
  background: rgba(255, 255, 255, 0.15);
}

.code-btn {
  padding: 14px 20px;
  background: transparent;
  color: #000;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.code-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.3);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  color: #e74c3c;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: rgba(231, 76, 60, 0.1);
  border-radius: 10px;
  border: 1px solid rgba(231, 76, 60, 0.2);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: transparent;
  color: #000;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 4px;
}

.submit-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.05);
  border-color: rgba(0, 0, 0, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
