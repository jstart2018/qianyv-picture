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
  padding: 40px 20px;
  width: 100%;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(26, 160, 193, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 32px;
  color: #1aa0c1;
  margin-bottom: 8px;
  font-weight: 700;
}

.login-header p {
  font-size: 15px;
  color: #2c5282;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item label {
  display: block;
  font-size: 14px;
  color: #1e3a5f;
  margin-bottom: 8px;
  font-weight: 600;
}

.form-item input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(26, 160, 193, 0.2);
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.9);
}

.form-item input:focus {
  border-color: #1aa0c1;
  box-shadow: 0 0 0 3px rgba(26, 160, 193, 0.15);
  background: white;
}

.code-input-box {
  display: flex;
  gap: 10px;
}

.code-input-box input {
  flex: 1;
}

.code-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #7bd3d8, #1aa0c1);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.3s;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(26, 160, 193, 0.4);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  color: #e74c3c;
  font-size: 14px;
  text-align: center;
  padding: 10px;
  background: rgba(231, 76, 60, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(231, 76, 60, 0.2);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1aa0c1, #7bd3d8);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(26, 160, 193, 0.5);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
