import axios, { type InternalAxiosRequestConfig } from 'axios'
import { useAuth } from '../store/auth'

const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'
export const api = axios.create({ baseURL, timeout: 10000, headers: { 'Content-Type': 'application/json' } })

api.interceptors.request.use((config) => {
  const token = useAuth.getState().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use((response) => response, async (error: unknown) => {
  if (!axios.isAxiosError(error) || error.response?.status !== 401 || !error.config) throw error
  const request = error.config as InternalAxiosRequestConfig & { retried?: boolean }
  const auth = useAuth.getState()
  if (request.retried || request.url?.includes('/auth/') || !auth.refreshToken) throw error
  request.retried = true
  try {
    const response = await axios.post(`${baseURL}/auth/refresh`, { refreshToken: auth.refreshToken }, { timeout: 10000 })
    auth.setTokens(response.data.accessToken, response.data.refreshToken)
    request.headers.Authorization = `Bearer ${response.data.accessToken}`
    return api(request)
  } catch (refreshError) {
    auth.clearSession()
    throw refreshError
  }
})