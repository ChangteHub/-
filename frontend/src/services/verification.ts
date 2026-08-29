import request from './request'

export const verificationApi = {
  submit: (data: {
    realName: string
    studentId: string
    college: string
    enrollYear?: number
    studentCardUrl: string
  }): Promise<void> => request.post('/verification', data),
  getStatus: (): Promise<any> => request.get('/verification/status'),
}
