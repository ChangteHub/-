import { useState, useEffect } from 'react'
import { Toast, Dialog } from 'antd-mobile'
import { adminApi } from '../../services/api'
import './CategoryManagement.css'

interface CategoryItem {
  id: string
  name: string
  icon: string
  sort?: number // 与后端 Category.sort 对齐
  status?: number
  createdAt?: string
}

export default function CategoryManagement() {
  const [categories, setCategories] = useState<CategoryItem[]>([])
  const [loading, setLoading] = useState(true)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingCategory, setEditingCategory] = useState<CategoryItem | null>(
    null
  )
  const [formData, setFormData] = useState({
    name: '',
    icon: '',
    sort: 0,
  })

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      setLoading(true)
      const data = await adminApi.getCategories()
      setCategories(data)
    } catch (error) {
      console.error('Failed to load categories:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingCategory(null)
    setFormData({ name: '', icon: '', sort: 0 })
    setModalVisible(true)
  }

  const handleEdit = (category: CategoryItem) => {
    setEditingCategory(category)
    setFormData({
      name: category.name,
      icon: category.icon || '',
      sort: category.sort || 0,
    })
    setModalVisible(true)
  }

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      Toast.show({ content: '请输入分类名称', icon: 'fail' })
      return
    }

    try {
      if (editingCategory) {
        await adminApi.updateCategory(editingCategory.id, formData)
      } else {
        await adminApi.addCategory(formData)
      }
      Toast.show({ content: '保存成功', icon: 'success' })
      setModalVisible(false)
      loadCategories()
    } catch (error) {
      console.error('Failed to save category:', error)
      Toast.show({ content: '保存失败', icon: 'fail' })
    }
  }

  const handleDelete = async (category: CategoryItem) => {
    const confirmed = await Dialog.confirm({
      content: `确定要删除分类 "${category.name}" 吗？`,
    })
    if (!confirmed) return
    try {
      await adminApi.deleteCategory(category.id)
      Toast.show({ content: '删除成功', icon: 'success' })
      loadCategories()
    } catch (error: any) {
      console.error('Failed to delete category:', error)
      Toast.show({ content: error.message || '删除失败', icon: 'fail' })
    }
  }

  const handleToggleStatus = async (category: CategoryItem) => {
    const newStatus = category.status === 0 ? 1 : 0
    try {
      await adminApi.updateCategoryStatus(category.id, newStatus)
      Toast.show({ content: '操作成功', icon: 'success' })
      loadCategories()
    } catch (error) {
      console.error('Failed to update category status:', error)
      Toast.show({ content: '操作失败', icon: 'fail' })
    }
  }

  return (
    <div className="category-management">
      <div className="page-header">
        <h1 className="page-title">分类管理</h1>
        <button className="add-btn" onClick={handleAdd}>
          + 添加分类
        </button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>图标</th>
              <th>名称</th>
              <th>排序</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={7} className="loading-cell">
                  加载中...
                </td>
              </tr>
            ) : categories.length === 0 ? (
              <tr>
                <td colSpan={7} className="empty-cell">
                  暂无数据
                </td>
              </tr>
            ) : (
              categories.map((category) => (
                <tr key={category.id}>
                  <td>{category.id}</td>
                  <td>
                    <span className="category-icon">{category.icon || '📂'}</span>
                  </td>
                  <td>{category.name}</td>
                  <td>{category.sort}</td>
                  <td>
                    <span
                      className={`status-tag ${category.status === 0 ? 'status-active' : 'status-disabled'}`}
                    >
                      {category.status === 0 ? '启用' : '禁用'}
                    </span>
                  </td>
                  <td>
                    {category.createdAt
                      ? new Date(category.createdAt).toLocaleDateString('zh-CN')
                      : '-'}
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="action-btn btn-primary"
                        onClick={() => handleEdit(category)}
                      >
                        编辑
                      </button>
                      <button
                        className={`action-btn ${category.status === 0 ? 'btn-warning' : 'btn-success'}`}
                        onClick={() => handleToggleStatus(category)}
                      >
                        {category.status === 0 ? '禁用' : '启用'}
                      </button>
                      <button
                        className="action-btn btn-danger"
                        onClick={() => handleDelete(category)}
                      >
                        删除
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {modalVisible && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3>{editingCategory ? '编辑分类' : '添加分类'}</h3>
              <button
                className="modal-close"
                onClick={() => setModalVisible(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>分类名称 *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                  placeholder="请输入分类名称"
                />
              </div>
              <div className="form-group">
                <label>图标</label>
                <input
                  type="text"
                  value={formData.icon}
                  onChange={(e) =>
                    setFormData({ ...formData, icon: e.target.value })
                  }
                  placeholder="请输入图标名称（如：BookOutlined）"
                />
              </div>
              <div className="form-group">
                <label>排序</label>
                <input
                  type="number"
                  value={formData.sort}
                  onChange={(e) =>
                    setFormData({ ...formData, sort: Number(e.target.value) })
                  }
                  placeholder="请输入排序号"
                />
              </div>
            </div>
            <div className="modal-footer">
              <button
                className="modal-btn cancel"
                onClick={() => setModalVisible(false)}
              >
                取消
              </button>
              <button className="modal-btn confirm" onClick={handleSubmit}>
                确认
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
