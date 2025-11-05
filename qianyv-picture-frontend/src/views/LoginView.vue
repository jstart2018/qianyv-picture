<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/userController'

const router = useRouter()
const userStore = useUserStore()

const account = ref('')
const code = ref('')
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
  if (!account.value || !code.value) {
    error.value = '请填写完整信息'
    return
  }

  try {
    loading.value = true
    error.value = ''
    await userApi.loginWithCode({
      emailOrPhone: account.value,
      code: code.value,
    })

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
    <div class="login-card">
      <div class="login-header">
        <h1>欢迎来到千语壁纸</h1>
        <p>登录或注册，享受更多功能</p>
      </div>

      <div class="login-form">
        <div class="form-item">
          <label>邮箱 / 手机号</label>
          <input
            v-model="account"
            type="text"
            placeholder="请输入邮箱或手机号"
            @keyup.enter="doLogin"
          />
        </div>

        <div class="form-item">
          <label>验证码</label>
          <div class="code-input-box">
            <input v-model="code" type="text" placeholder="请输入验证码" @keyup.enter="doLogin" />
            <button class="code-btn" :disabled="countdown > 0" @click="sendVerifyCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </div>
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>

        <button class="submit-btn" :disabled="loading" @click="doLogin">
          {{ loading ? '登录中...' : '登录 / 注册' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  padding: var(--spacing-xl) var(--spacing-lg);
  width: 100%;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  backdrop-filter: var(--blur-lg);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: var(--spacing-xl);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.login-header h1 {
  font-size: var(--font-size-4xl);
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
  font-weight: var(--font-weight-bold);
}

.login-header p {
  font-size: 15px;
  color: var(--text-secondary);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-item label {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  font-weight: var(--font-weight-semibold);
}

.form-item input {
  width: 100%;
  padding: 12px var(--spacing-md);
  border: 1px solid rgba(26, 160, 193, 0.2);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  outline: none;
  transition: var(--transition-all);
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.9);
}

.form-item input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(26, 160, 193, 0.15);
  background: var(--bg-primary);
}

.code-input-box {
  display: flex;
  gap: 10px;
}

.code-input-box input {
  flex: 1;
}

.code-btn {
  padding: 12px var(--spacing-lg);
  background: var(--gradient-primary);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
  transition: var(--transition-all);
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-primary);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  color: var(--color-error);
  font-size: var(--font-size-sm);
  text-align: center;
  padding: 10px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: var(--gradient-primary);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  transition: var(--transition-all);
  margin-top: var(--spacing-sm);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
