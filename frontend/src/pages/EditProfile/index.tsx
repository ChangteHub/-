import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { NavBar, Input, TextArea, Button, Toast, Image } from 'antd-mobile'
import { LeftOutline } from 'antd-mobile-icons'
import { userApi, commonApi } from '../../services/api'
import { useStore } from '../../store/useStore'
import { DEFAULT_AVATAR } from '../../utils/format'
import './EditProfile.css'

export default function EditProfilePage() {
  const navigate = useNavigate()
  const { user, checkAuth, showLogin, authChecked } = useStore()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [nickname, setNickname] = useState(user?.nickname || '')
  const [phone, setPhone] = useState(user?.phone || '')
  const [bio, setBio] = useState(user?.bio || '')
  const [avatar, setAvatar] = useState(user?.avatar || '')
  const [uploading, setUploading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!authChecked) return
    if (!user) {
      showLogin()
      return
    }
    // 登录态就绪后同步表单（冷启动直达时 user 为 null，需要回填）
    setNickname(user.nickname || '')
    setPhone(user.phone || '')
    setBio(user.bio || '')
    setAvatar(user.avatar || '')
  }, [user, authChecked])

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const res = await commonApi.uploadImage(file)
      if (res?.url) {
        setAvatar(res.url)
      }
    } catch (error) {
      Toast.show({ content: '头像上传失败', position: 'center' })
    } finally {
      setUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const handleSubmit = async () => {
    if (!nickname.trim()) {
      Toast.show({ content: '请输入昵称', position: 'center' })
      return
    }
    setSubmitting(true)
    try {
      await userApi.updateProfile({
        nickname: nickname.trim(),
        avatar,
        phone,
        bio,
      })
      await checkAuth()
      Toast.show({ content: '保存成功', position: 'center' })
      setTimeout(() => navigate(-1), 800)
    } catch (error) {
      Toast.show({ content: '保存失败', position: 'center' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page-wrapper edit-profile-page" data-od-id="edit-profile-page">
      <input
        type="file"
        ref={fileInputRef}
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleAvatarUpload}
      />

      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutline />}
        className="page-navbar"
      >
        编辑资料
      </NavBar>

      <div className="edit-profile-form">
        <div className="form-section">
          <div className="avatar-upload" onClick={() => fileInputRef.current?.click()}>
            <Image
              src={avatar || DEFAULT_AVATAR}
              width={80}
              height={80}
              fit="cover"
              style={{ borderRadius: '50%' }}
            />
            <div className="avatar-mask" style={{ opacity: uploading ? 1 : 0 }}>
              {uploading ? '上传中...' : '更换头像'}
            </div>
          </div>
        </div>

        <div className="form-row">
          <label htmlFor="edit-nickname">昵称</label>
          <Input
            id="edit-nickname"
            placeholder="请输入昵称"
            value={nickname}
            onChange={setNickname}
          />
        </div>

        <div className="form-row">
          <label htmlFor="edit-phone">手机号</label>
          <Input
            id="edit-phone"
            placeholder="请输入手机号"
            value={phone}
            onChange={setPhone}
            type="tel"
          />
        </div>

        <div className="form-row">
          <label htmlFor="edit-bio">个人简介</label>
          <TextArea
            id="edit-bio"
            placeholder="介绍一下自己吧"
            value={bio}
            onChange={setBio}
            rows={4}
            maxLength={200}
            showCount
          />
        </div>
      </div>

      <div className="edit-profile-footer">
        <Button
          block
          color="primary"
          size="large"
          onClick={handleSubmit}
          loading={submitting}
          disabled={uploading}
        >
          保存
        </Button>
      </div>
    </div>
  )
}
