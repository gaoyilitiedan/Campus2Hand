import request from './request'

export interface LoginRequest {
  studentId: string
  password: string
}

export interface RegisterRequest {
  studentId: string
  nickname: string
  password: string
  email: string
  campus: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresAt: string
  user: {
    id: number
    studentId: string
    nickname: string
    avatarUrl: string | null
    reputationScore: number
  }
}

export function login(data: LoginRequest): Promise<LoginResponse> {
  return request.post('/auth/login', data).then(res => res.data)
}

export function register(data: RegisterRequest): Promise<void> {
  return request.post('/auth/register', data).then(res => res.data)
}

export function refreshToken(refreshToken: string): Promise<LoginResponse> {
  return request.post('/auth/refresh', { refreshToken }).then(res => res.data)
}

export function getProfile(): Promise<any> {
  return request.get('/users/me').then(res => res.data)
}