import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      const error = new Error(body.message || '请求失败')
      error.code = body.code
      error.response = response
      return Promise.reject(error)
    }
    return body
  },
  (error) => {
    const data = error?.response?.data
    const message =
      data?.message ||
      (error?.code === 'ECONNABORTED' ? '请求超时，请稍后重试' : null) ||
      (error?.message === 'Network Error' ? '无法连接后端，请确认服务已启动（8080）' : null) ||
      error?.message ||
      '网络请求失败'
    const wrapped = new Error(message)
    wrapped.code = data?.code
    wrapped.response = error?.response
    return Promise.reject(wrapped)
  }
)

export default http
