import { useState } from 'react'
import { NavBar, List, Switch, Dialog, Toast, Button } from 'antd-mobile'
import { useNavigate } from 'react-router-dom'
import {
  LeftOutlined,
  BellOutlined,
  LockOutlined,
  EyeOutlined,
  DeleteOutlined,
  InfoCircleOutlined,
  FileTextOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons'
import { useStore } from '../../stores/useStore'
import './Settings.css'

export default function SettingsPage() {
  const navigate = useNavigate()
  const { user, logout } = useStore()
  const [notifyEnabled, setNotifyEnabled] = useState(true)
  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('darkMode') === 'true'
  })

  const toggleDarkMode = (checked: boolean) => {
    setDarkMode(checked)
    localStorage.setItem('darkMode', String(checked))
    if (checked) {
      document.body.classList.add('dark-mode')
    } else {
      document.body.classList.remove('dark-mode')
    }
  }

  const handleClearCache = async () => {
    const confirmed = await Dialog.confirm({
      content: '确定清除本地缓存吗？',
    })
    if (confirmed) {
      Toast.show({ content: '缓存已清除', position: 'center' })
    }
  }

  const handleLogout = async () => {
    const confirmed = await Dialog.confirm({
      content: '确定退出登录吗？',
    })
    if (confirmed) {
      logout()
      Toast.show({ content: '已退出登录', position: 'center' })
      navigate('/profile')
    }
  }

  const handleDeleteAccount = async () => {
    const confirmed = await Dialog.confirm({
      content: '注销账号后数据将无法恢复，确定继续吗？',
    })
    if (confirmed) {
      Toast.show({ content: '账号注销功能暂未开放', position: 'center' })
    }
  }

  return (
    <div className="page-wrapper settings-page" data-od-id="settings-page">
      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutlined />}
        className="page-navbar"
      >
        设置
      </NavBar>

      <div className="settings-section">
        <div className="settings-section-title">通知设置</div>
        <List className="settings-list">
          <List.Item
            prefix={<BellOutlined className="settings-icon" />}
            extra={
              <Switch
                checked={notifyEnabled}
                onChange={setNotifyEnabled}
                style={{ '--checked-color': 'var(--primary)' }}
              />
            }
          >
            消息通知
          </List.Item>
          <List.Item
            prefix={<EyeOutlined className="settings-icon" />}
            extra={
              <Switch
                checked={darkMode}
                onChange={toggleDarkMode}
                style={{ '--checked-color': 'var(--primary)' }}
              />
            }
          >
            深色模式
          </List.Item>
        </List>
      </div>

      <div className="settings-section">
        <div className="settings-section-title">隐私与安全</div>
        <List className="settings-list">
          <List.Item
            prefix={<LockOutlined className="settings-icon" />}
            onClick={() => Toast.show({ content: '功能开发中', position: 'center' })}
            clickable
          >
            隐私设置
          </List.Item>
          <List.Item
            prefix={<DeleteOutlined className="settings-icon" />}
            onClick={handleClearCache}
            clickable
          >
            清除缓存
          </List.Item>
        </List>
      </div>

      <div className="settings-section">
        <div className="settings-section-title">关于</div>
        <List className="settings-list">
          <List.Item
            prefix={<InfoCircleOutlined className="settings-icon" />}
            extra="v1.0.0"
          >
            版本号
          </List.Item>
          <List.Item
            prefix={<FileTextOutlined className="settings-icon" />}
            onClick={() => Toast.show({ content: '功能开发中', position: 'center' })}
            clickable
          >
            用户协议
          </List.Item>
          <List.Item
            prefix={<FileTextOutlined className="settings-icon" />}
            onClick={() => Toast.show({ content: '功能开发中', position: 'center' })}
            clickable
          >
            隐私政策
          </List.Item>
          <List.Item
            prefix={<ExclamationCircleOutlined className="settings-icon" />}
            onClick={() => Toast.show({ content: '功能开发中', position: 'center' })}
            clickable
          >
            意见反馈
          </List.Item>
        </List>
      </div>

      {user && (
        <div className="settings-actions">
          <Button
            block
            fill="outline"
            color="danger"
            size="large"
            onClick={handleLogout}
          >
            退出登录
          </Button>
          <Button
            block
            fill="outline"
            color="danger"
            size="small"
            className="settings-delete-btn"
            onClick={handleDeleteAccount}
          >
            注销账号
          </Button>
        </div>
      )}
    </div>
  )
}
