import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code !== 0) {
      if (data.code === 1004 || data.code === 1005) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

export default request