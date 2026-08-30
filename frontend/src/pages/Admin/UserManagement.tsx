import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Toast, Dialog } from 'antd-mobile'
import { adminApi } from '../../services/api'
import { USER_STATUS } from '../../constants'
import { formatDate, DEFAULT_AVATAR } from '../../utils/format'
import './UserManagement.css'

interface UserItem {
  id: string
  username: string
  nickname: string
  avatar: string
  phone: string
  school: string
  studentId: string
  bio: string
  status: number
  role: number
  productCount: number
  createdAt: string
}

export default function UserManagement() {
  const [searchParams] = useSearchParams()
  const [users, setUsers] = useState<UserItem[]>([])
  const [loading, setLoading] = useState(true)
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [searchedKeyword, setSearchedKeyword] = useState('') // 已提交搜索的关键词（筛选/翻页用它，避免未提交输入污染）
  const [statusFilter, setStatusFilter] = useState<number | undefined>(
    searchParams.get('status') ? Number(searchParams.get('status')) : undefined
  )
  const [actingId, setActingId] = useState<string | null>(null)
  const seqRef = useRef(0)

  useEffect(() => {
    loadUsers(pageNum, searchedKeyword)
  }, [pageNum, statusFilter, searchedKeyword])

  const loadUsers = async (page: number = pageNum, kw: string = keyword) => {
    const cur = ++seqRef.current // 序号保护：翻页/筛选/搜索竞态时旧响应不覆盖新结果
    try {
      setLoading(true)
      const data = await adminApi.getUsers({
        keyword: kw || undefined,
        status: statusFilter,
        pageNum: page,
        pageSize,
      })
      if (cur !== seqRef.current) return
      setUsers(data.list)
      setTotal(data.total)
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('Failed to load users:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }

  const handleSearch = () => {
    // 提交当前关键词：翻页/筛选统一使用已提交值
    setSearchedKeyword(keyword)
    setPageNum(1)
    loadUsers(1, keyword)
  }

  const handleToggleStatus = async (user: UserItem) => {
    if (actingId) return // 防连点
    if (user.role === 1) {
      Toast.show({ content: '不能操作管理员账号', icon: 'fail' })
      return
    }
    const newStatus = user.status === 0 ? 1 : 0
    const action = newStatus === 1 ? '禁用' : '启用'
    const confirmed = await Dialog.confirm({
      content: `确定要${action}用户 ${user.nickname} 吗？`,
    })
    if (!confirmed) return
    setActingId(user.id)
    try {
      await adminApi.updateUserStatus(user.id, newStatus)
      Toast.show({ content: '操作成功', icon: 'success' })
      loadUsers(pageNum)
    } catch (error) {
      console.error('Failed to update user status:', error)
      Toast.show({ content: '操作失败', icon: 'fail' })
    } finally {
      setActingId(null)
    }
  }

  return (
    <div className="user-management">
      <h1 className="page-title">用户管理</h1>

      <div className="toolbar">
        <div className="search-bar">
          <input
            type="text"
            placeholder="搜索用户名/昵称/学号"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
          <button onClick={handleSearch}>搜索</button>
        </div>
        <div className="filter-bar">
          <select
            value={statusFilter ?? ''}
            onChange={(e) => {
              setStatusFilter(e.target.value ? Number(e.target.value) : undefined)
              setPageNum(1)
            }}
          >
            <option value="">全部状态</option>
            <option value="0">正常</option>
            <option value="1">已禁用</option>
          </select>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>昵称</th>
              <th>学号</th>
              <th>手机号</th>
              <th>商品数</th>
              <th>状态</th>
              <th>角色</th>
              <th>注册时间</th>
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
            ) : users.length === 0 ? (
              <tr>
                <td colSpan={10} className="empty-cell">
                  暂无数据
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.username}</td>
                  <td>
                    <div className="user-info">
                      <img
                        src={user.avatar || DEFAULT_AVATAR}
                        alt="avatar"
                        className="user-avatar"
                      />
                      <span>{user.nickname}</span>
                    </div>
                  </td>
                  <td>{user.studentId || '-'}</td>
                  <td>{user.phone || '-'}</td>
                  <td>{user.productCount}</td>
                  <td>
                    <span
                      className={`status-tag ${USER_STATUS[user.status]?.className || ''}`}
                    >
                      {USER_STATUS[user.status]?.label || '未知'}
                    </span>
                  </td>
                  <td>
                    <span
                      className={`role-tag ${user.role === 1 ? 'role-admin' : 'role-user'}`}
                    >
                      {user.role === 1 ? '管理员' : '普通用户'}
                    </span>
                  </td>
                  <td>{formatDate(user.createdAt)}</td>
                  <td>
                    {user.role !== 1 && (
                      <button
                        className={`action-btn ${user.status === 0 ? 'btn-danger' : 'btn-success'}`}
                        onClick={() => handleToggleStatus(user)}
                        disabled={actingId === user.id}
                      >
                        {user.status === 0 ? '禁用' : '启用'}
                      </button>
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
    </div>
  )
}
