import { reactive } from 'vue'

interface UserInfo {
  id: number
  studentId: string
  nickname: string
  avatarUrl: string | null
  reputationScore: number
}

interface AuthState {
  isLoggedIn: boolean
  user: UserInfo | null
  accessToken: string
  refreshToken: string
}

export const authState = reactive<AuthState>({
  isLoggedIn: false,
  user: null,
  accessToken: '',
  refreshToken: ''
})

export function setAuth(token: string, refresh: string, user: UserInfo) {
  authState.accessToken = token
  authState.refreshToken = refresh
  authState.user = user
  authState.isLoggedIn = true
  localStorage.setItem('accessToken', token)
  localStorage.setItem('refreshToken', refresh)
  localStorage.setItem('user', JSON.stringify(user))
}

export function clearAuth() {
  authState.accessToken = ''
  authState.refreshToken = ''
  authState.user = null
  authState.isLoggedIn = false
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
}

export function restoreAuth() {
  const token = localStorage.getItem('accessToken')
  const refresh = localStorage.getItem('refreshToken')
  const user = localStorage.getItem('user')
  if (token && refresh && user) {
    authState.accessToken = token
    authState.refreshToken = refresh
    authState.user = JSON.parse(user)
    authState.isLoggedIn = true
  }
}