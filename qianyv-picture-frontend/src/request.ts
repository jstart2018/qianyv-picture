import axios from 'axios'

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: '/api',
  timeout: 60000,
  withCredentials: true,
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response

    // 规范化后端返回的数据：把 data.data 中的数字字符串转换为数字，减少组件中手动 parseInt 的需求
    // 注意：ID 字段保持字符串类型，避免大数精度丢失（如 1976271616420237313）
    const normalizeNumericStrings = (obj: any) => {
      if (obj === null || obj === undefined) return
      if (Array.isArray(obj)) {
        for (let i = 0; i < obj.length; i++) {
          normalizeNumericStrings(obj[i])
        }
      } else if (typeof obj === 'object') {
        Object.keys(obj).forEach((key) => {
          const val = obj[key]
          if (val === null || val === undefined) return

          // 跳过所有 ID 相关字段，保持字符串类型避免精度丢失
          const isIdField = key.toLowerCase().includes('id')

          if (typeof val === 'string' && !isIdField) {
            // 仅在字符串完全为数字(可带小数)时转换，避免误转其他字符串
            if (/^-?\d+(\.\d+)?$/.test(val)) {
              const num = Number(val)
              // 仅当转换后为有效数字且在安全范围内时才替换
              if (!Number.isNaN(num) && Number.isSafeInteger(num)) {
                obj[key] = num
              }
              // 超出安全整数范围的保持字符串，避免精度丢失
            }
          } else if (typeof val === 'object') {
            normalizeNumericStrings(val)
          }
        })
      }
    }

    try {
      if (data && data.data) {
        normalizeNumericStrings(data.data)
      }
    } catch (e) {
      // 规范化失败不影响主流程，记录一次警告以便排查
      // eslint-disable-next-line no-console
      console.warn('response data normalization failed', e)
    }

    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        console.warn('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
