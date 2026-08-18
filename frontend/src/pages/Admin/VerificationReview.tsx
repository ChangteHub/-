import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Toast } from 'antd-mobile'
import { adminApi } from '../../services/api'
import { formatDate } from '../../utils/format'
import './VerificationReview.css'

interface VerificationItem {
  id: string
  userId: string
  username: string
  nickname: string
  realName: string
  studentId: string
  college: string
  enrollYear: number
  studentCardUrl: string
  status: number
  rejectReason: string
  createdAt: string
}

const statusMap: Record<number, { label: string; className: string }> = {
  0: { label: '待审核', className: 'status-pending' },
  1: { label: '已通过', className: 'status-approved' },
  2: { label: '已拒绝', className: 'status-rejected' },
}

export default function VerificationReview() {
  const [searchParams] = useSearchParams()
  const [verifications, setVerifications] = useState<VerificationItem[]>([])
  const [loading, setLoading] = useState(true)
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize] = useState(10)
  const [statusFilter, setStatusFilter] = useState<number | undefined>(
    searchParams.get('status') ? Number(searchParams.get('status')) : undefined
  )
  const [reviewModal, setReviewModal] = useState<{
    id: string
    action: 'approve' | 'reject'
  } | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const seqRef = useRef(0)

  useEffect(() => {
    loadVerifications(pageNum)
  }, [pageNum, statusFilter])

  const loadVerifications = async (page: number = pageNum) => {
    const cur = ++seqRef.current // 序号保护：翻页/筛选竞态时旧响应不覆盖新结果
    try {
      setLoading(true)
      const data = await adminApi.getVerifications({
        status: statusFilter,
        pageNum: page,
        pageSize,
      })
      if (cur !== seqRef.current) return
      setVerifications(data.list)
      setTotal(data.total)
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('Failed to load verifications:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }

  const handleReview = async () => {
    if (!reviewModal || submitting) return // 防连点

    if (reviewModal.action === 'reject' && !rejectReason.trim()) {
      Toast.show({ content: '请输入拒绝原因', icon: 'fail' })
      return
    }

    setSubmitting(true)
    try {
      await adminApi.reviewVerification(reviewModal.id, {
        status: reviewModal.action === 'approve' ? 1 : 2,
        rejectReason:
          reviewModal.action === 'reject' ? rejectReason : undefined,
      })
      Toast.show({ content: '审核成功', icon: 'success' })
      setReviewModal(null)
      setRejectReason('')
      loadVerifications(pageNum)
    } catch (error) {
      console.error('Failed to review verification:', error)
      Toast.show({ content: '操作失败', icon: 'fail' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="verification-review">
      <h1 className="page-title">认证审核</h1>

      <div className="toolbar">
        <div className="filter-bar">
          <select
            value={statusFilter ?? ''}
            onChange={(e) => {
              setStatusFilter(e.target.value ? Number(e.target.value) : undefined)
              setPageNum(1)
            }}
          >
            <option value="">全部状态</option>
            <option value="0">待审核</option>
            <option value="1">已通过</option>
            <option value="2">已拒绝</option>
          </select>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户</th>
              <th>真实姓名</th>
              <th>学号</th>
              <th>学院</th>
              <th>入学年份</th>
              <th>学生证</th>
              <th>状态</th>
              <th>提交时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={10} className="loading-cell">
                  加载中...
                </td>
              </tr>
            ) : verifications.length === 0 ? (
              <tr>
                <td colSpan={10} className="empty-cell">
                  暂无数据
                </td>
              </tr>
            ) : (
              verifications.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>
                    <div className="user-info">
                      <span>{item.nickname}</span>
                      <span className="user-id">{item.username}</span>
                    </div>
                  </td>
                  <td>{item.realName}</td>
                  <td>{item.studentId}</td>
                  <td>{item.college || '-'}</td>
                  <td>{item.enrollYear || '-'}</td>
                  <td>
                    {item.studentCardUrl ? (
                      <a
                        href={item.studentCardUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="view-link"
                      >
                        查看
                      </a>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td>
                    <span
                      className={`status-tag ${statusMap[item.status]?.className || ''}`}
                    >
                      {statusMap[item.status]?.label || '未知'}
                    </span>
                  </td>
                  <td>{formatDate(item.createdAt)}</td>
                  <td>
                    {item.status === 0 && (
                      <div className="action-buttons">
                        <button
                          className="action-btn btn-success"
                          onClick={() =>
                            setReviewModal({ id: item.id, action: 'approve' })
                          }
                        >
                          通过
                        </button>
                        <button
                          className="action-btn btn-danger"
                          onClick={() =>
                            setReviewModal({ id: item.id, action: 'reject' })
                          }
                        >
                          拒绝
                        </button>
                      </div>
                    )}
                    {item.status === 2 && item.rejectReason && (
                      <div className="reject-reason" title={item.rejectReason}>
                        {item.rejectReason}
                      </div>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {total > pageSize && (
        <div className="pagination">
          <button
            disabled={pageNum <= 1}
            onClick={() => setPageNum(pageNum - 1)}
          >
            上一页
          </button>
          <span className="page-info">
            第 {pageNum} 页 / 共 {Math.ceil(total / pageSize)} 页
          </span>
          <button
            disabled={pageNum >= Math.ceil(total / pageSize)}
            onClick={() => setPageNum(pageNum + 1)}
          >
            下一页
          </button>
        </div>
      )}

      {reviewModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3>
                {reviewModal.action === 'approve' ? '确认通过' : '确认拒绝'}
              </h3>
              <button
                className="modal-close"
                onClick={() => {
                  setReviewModal(null)
                  setRejectReason('')
                }}
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              {reviewModal.action === 'approve' ? (
                <p>确定要通过该用户的实名认证吗？</p>
              ) : (
                <div className="reject-form">
                  <p>请输入拒绝原因：</p>
                  <textarea
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    placeholder="请输入拒绝原因..."
                    rows={4}
                  />
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button
                className="modal-btn cancel"
                onClick={() => {
                  setReviewModal(null)
                  setRejectReason('')
                }}
              >
                取消
              </button>
              <button
                className={`modal-btn confirm ${reviewModal.action === 'reject' ? 'danger' : 'success'}`}
                onClick={handleReview}
              >
                确认
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
