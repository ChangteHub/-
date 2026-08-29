import request from './request'
import type { User, RegisterData, LoginData, LoginResult } from './types'

export const authApi = {
  register: (data: RegisterData): Promise<User> =>
    request.post('/auth/register', data),
  login: (data: LoginData): Promise<LoginResult> =>
    request.post('/auth/login', data),
  getMe: (): Promise<User> => request.get('/auth/me'),
}
