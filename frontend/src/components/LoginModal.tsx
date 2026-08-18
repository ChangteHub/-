import { useState } from 'react'
import { Modal, Input, Button, Toast } from 'antd-mobile'
import { useStore } from '../store/useStore'
import { authApi } from '../services/api'
import './LoginModal.css'

export default function LoginModal() {
  const { loginVisible, hideLogin, login } = useStore()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isRegister, setIsRegister] = useState(false)
  const [nickname, setNickname] = useState('')
  const [loading, setLoading] = useState(false)

  const handleLogin = async () => {
    if (!username.trim()) {
      Toast.show({ content: '请输入用户名', position: 'center' })
      return
    }
    if (!password.trim()) {
      Toast.show({ content: '请输入密码', position: 'center' })
      return
    }

    setLoading(true)
    try {
      await login(username, password)
      Toast.show({ content: '登录成功', position: 'center' })
      resetForm()
    } catch (error: any) {
      Toast.show({ content: error.message || '登录失败', position: 'center' })
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async () => {
    if (!username.trim()) {
      Toast.show({ content: '请输入用户名', position: 'center' })
      return
    }
    if (!password.trim()) {
      Toast.show({ content: '请输入密码', position: 'center' })
      return
    }
    if (!nickname.trim()) {
      Toast.show({ content: '请输入昵称', position: 'center' })
      return
    }

    setLoading(true)
    try {
      await authApi.register({ username, password, nickname })
      Toast.show({ content: '注册成功，正在自动登录...', position: 'center' })
      // 注册成功后自动登录，避免重复输入
      await login(username, password)
      setIsRegister(false)
      resetForm()
    } catch (error: any) {
      Toast.show({ content: error.message || '注册失败', position: 'center' })
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setUsername('')
    setPassword('')
    setNickname('')
  }

  return (
    <Modal
      visible={loginVisible}
      onClose={hideLogin}
      title={isRegister ? '注册' : '登录'}
      content={
        <div className="login-form">
          <div className="login-field">
            <label>用户名</label>
            <Input
              placeholder="请输入用户名/学号"
              value={username}
              onChange={setUsername}
            />
          </div>

          {isRegister && (
            <div className="login-field">
              <label>昵称</label>
              <Input
                placeholder="请输入昵称"
                value={nickname}
                onChange={setNickname}
              />
            </div>
          )}

          <div className="login-field">
            <label>密码</label>
            <Input
              placeholder="请输入密码"
              value={password}
              onChange={setPassword}
              type="password"
              onEnterPress={isRegister ? handleRegister : handleLogin}
            />
          </div>

          <Button
            block
            color="primary"
            size="large"
            onClick={isRegister ? handleRegister : handleLogin}
            loading={loading}
          >
            {isRegister ? '注册' : '登录'}
          </Button>

          <div className="login-switch">
            {isRegister ? (
              <span>
                已有账号？
                <a onClick={() => setIsRegister(false)}>立即登录</a>
              </span>
            ) : (
              <span>
                没有账号？
                <a onClick={() => setIsRegister(true)}>立即注册</a>
              </span>
            )}
          </div>
        </div>
      }
    />
  )
}
