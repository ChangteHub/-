import { useState, useEffect } from 'react'
import { NavBar, Form, Input, Button, Toast, ImageUploader } from 'antd-mobile'
import { useNavigate } from 'react-router-dom'
import { LeftOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import type { ImageUploadItem } from 'antd-mobile/es/components/image-uploader'
import { commonApi, verificationApi } from '../../services/api'
import { useStore } from '../../store/useStore'
import './Verification.css'

export default function VerificationPage() {
  const navigate = useNavigate()
  const { user, showLogin, authChecked } = useStore()
  const [submitted, setSubmitted] = useState(false)
  const [fileList, setFileList] = useState<ImageUploadItem[]>([])
  const [realName, setRealName] = useState('')
  const [studentId, setStudentId] = useState('')
  const [college, setCollege] = useState('')
  const [enrollYear, setEnrollYear] = useState('')
  const [loading, setLoading] = useState(false)
  const [verifyStatus, setVerifyStatus] = useState<string>('')

  useEffect(() => {
    if (!authChecked) return
    if (!user) {
      showLogin()
      return
    }
    const checkStatus = async () => {
      try {
        const res: any = await verificationApi.getStatus()
        if (res?.status && res.status !== 'none') {
          setVerifyStatus(res.status)
          setSubmitted(true)
        }
      } catch {
        // ignore - user hasn't submitted yet
      }
    }
    checkStatus()
  }, [user, authChecked])

  const uploadImage = async (file: File): Promise<ImageUploadItem> => {
    const res: any = await commonApi.uploadImage(file)
    return { url: res.url }
  }

  const handleSubmit = async () => {
    if (!realName.trim()) {
      Toast.show({ content: '请输入真实姓名', position: 'center' })
      return
    }
    if (!studentId.trim()) {
      Toast.show({ content: '请输入学号', position: 'center' })
      return
    }
    if (fileList.length === 0) {
      Toast.show({ content: '请上传学生证照片', position: 'center' })
      return
    }

    setLoading(true)
    try {
      await verificationApi.submit({
        realName,
        studentId,
        college,
        enrollYear: enrollYear ? parseInt(enrollYear) : undefined,
        studentCardUrl: fileList[0].url,
      })
      Toast.show({ content: '提交成功，预计1-2个工作日审核', position: 'center' })
      setVerifyStatus('pending')
      setSubmitted(true)
    } catch (error: any) {
      Toast.show({ content: error.message || '提交失败', position: 'center' })
    } finally {
      setLoading(false)
    }
  }

  if (submitted) {
    return (
      <div className="page-wrapper verify-page" data-od-id="verify-page">
        <NavBar
          onBack={() => navigate(-1)}
          backArrow={<LeftOutlined />}
          className="page-navbar"
        >
          实名认证
        </NavBar>
        <div className="verify-success">
          <div className="verify-success-icon">
            <SafetyCertificateOutlined />
          </div>
          <h3>认证申请已提交</h3>
          {verifyStatus === 'pending' && <p>审核中，请耐心等待</p>}
          {verifyStatus === 'approved' && <p style={{ color: 'var(--success)' }}>已认证通过</p>}
          {verifyStatus === 'rejected' && <p style={{ color: 'var(--danger)' }}>认证未通过，请修改信息后重新提交</p>}
          {!verifyStatus && <p>预计1-2个工作日内完成审核</p>}
          <p className="verify-success-hint">审核通过后，你的个人主页将显示认证标识</p>
          {verifyStatus === 'rejected' && (
            <Button color="primary" block style={{ marginBottom: 8 }} onClick={() => setSubmitted(false)}>
              重新提交认证
            </Button>
          )}
          <Button color="primary" block onClick={() => navigate('/profile')}>
            返回个人中心
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="page-wrapper verify-page" data-od-id="verify-page">
      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutlined />}
        className="page-navbar"
      >
        实名认证
      </NavBar>

      <div className="verify-banner">
        <SafetyCertificateOutlined className="verify-banner-icon" />
        <div className="verify-banner-text">
          <h3>实名认证</h3>
          <p>完成认证后可获得更多信任，提升交易成功率</p>
        </div>
      </div>

      <Form
        layout="horizontal"
        className="verify-form"
        footer={
          <Button block color="primary" size="large" onClick={handleSubmit} loading={loading}>
            提交认证
          </Button>
        }
      >
        <Form.Item label="真实姓名" required>
          <Input
            placeholder="请输入身份证姓名"
            value={realName}
            onChange={setRealName}
          />
        </Form.Item>

        <Form.Item label="学号" required>
          <Input
            placeholder="请输入学号"
            value={studentId}
            onChange={setStudentId}
          />
        </Form.Item>

        <Form.Item label="学院">
          <Input
            placeholder="请输入所在学院"
            value={college}
            onChange={setCollege}
          />
        </Form.Item>

        <Form.Item label="入学年份">
          <Input
            placeholder="如 2023"
            type="number"
            value={enrollYear}
            onChange={setEnrollYear}
          />
        </Form.Item>

        <Form.Item label="学生证照片" required className="verify-upload-item">
          <ImageUploader
            value={fileList}
            onChange={setFileList}
            upload={uploadImage}
            maxCount={1}
            className="verify-uploader"
          />
        </Form.Item>
      </Form>

      <div className="verify-notice">
        <p className="verify-notice-title">认证说明</p>
        <ul className="verify-notice-list">
          <li>仅限西南科技大学在校学生进行认证</li>
          <li>请确保填写的信息与学生证一致</li>
          <li>学生证照片需清晰可辨，包含姓名和学号</li>
          <li>认证信息仅用于身份验证，不会公开展示</li>
        </ul>
      </div>
    </div>
  )
}
