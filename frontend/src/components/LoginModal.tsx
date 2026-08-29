import { useState } from 'react'
import { Modal, Input, Button, Toast } from 'antd-mobile'
import { EyeOutline, EyeInvisibleOutline } from 'antd-mobile-icons'
import { useStore } from '../stores/useStore'
import { authApi } from '../services/api'
import './LoginModal.css'

export default function LoginModal() {
  const { loginVisible, hideLogin, login } = useStore()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isRegister, setIsRegister] = useState(false)
  const [nickname, setNickname] = useState('')
  const [loading, setLoading] = useState(false)
  const [showPwd, setShowPwd] = useState(false)

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
    setShowPwd(false)
  }

  const handleClose = () => {
    hideLogin()
    setIsRegister(false)
    resetForm()
  }

  return (
    <Modal
      visible={loginVisible}
      onClose={handleClose}
      showCloseButton
      bodyStyle={{ borderRadius: 'var(--radius-xl)' }}
      content={
        <div className="login-form">
          {/* 品牌标识 */}
          <div className="login-brand" aria-hidden="true">
            <span className="login-logo">校园</span>
            <h2 className="login-title">{isRegister ? '创建账号' : '欢迎回来'}</h2>
            <p className="login-subtitle">{isRegister ? '加入校园二手交易' : '登录后开始淘好物'}</p>
          </div>

          <div className="login-field">
            <label>用户名</label>
            <Input
              placeholder="请输入用户名/学号"
              value={username}
              onChange={setUsername}
              clearable
            />
          </div>

          {isRegister && (
            <div className="login-field">
              <label>昵称</label>
              <Input
                placeholder="请输入昵称"
                value={nickname}
                onChange={setNickname}
                clearable
              />
            </div>
          )}

          <div className="login-field">
            <label>密码</label>
            <div className="login-password">
              <Input
                placeholder="请输入密码"
                value={password}
                onChange={setPassword}
                type={showPwd ? 'text' : 'password'}
                onEnterPress={isRegister ? handleRegister : handleLogin}
              />
              <button
                type="button"
                className="pwd-toggle"
                onClick={() => setShowPwd((v) => !v)}
                aria-label={showPwd ? '隐藏密码' : '显示密码'}
              >
                {showPwd ? <EyeOutline /> : <EyeInvisibleOutline />}
              </button>
            </div>
          </div>

          <Button
            block
            size="large"
            className="login-submit"
            onClick={isRegister ? handleRegister : handleLogin}
            loading={loading}
          >
            {isRegister ? '注册并登录' : '登录'}
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
