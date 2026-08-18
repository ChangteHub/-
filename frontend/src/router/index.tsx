import { createBrowserRouter } from 'react-router-dom'
import App from '../App'
import ProtectedRoute from '../components/ProtectedRoute'
import HomePage from '../pages/Home'
import CategoryPage from '../pages/Category'
import PublishPage from '../pages/Publish'
import ProductDetailPage from '../pages/ProductDetail'
import SearchPage from '../pages/Search'
import ChatPage from '../pages/Chat'
import ChatDetailPage from '../pages/ChatDetail'
import ProfilePage from '../pages/Profile'
import MyPublishPage from '../pages/MyPublish'
import FavoritesPage from '../pages/Favorites'
import BrowsingHistoryPage from '../pages/BrowsingHistory'
import VerificationPage from '../pages/Verification'
import HelpCenterPage from '../pages/HelpCenter'
import SettingsPage from '../pages/Settings'
import EditProfilePage from '../pages/EditProfile'
import {
  AdminLayout,
  Dashboard,
  UserManagement,
  ProductManagement,
  VerificationReview,
  CategoryManagement,
} from '../pages/Admin'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'category', element: <CategoryPage /> },
      { path: 'publish', element: <ProtectedRoute><PublishPage /></ProtectedRoute> },
      { path: 'product/:id', element: <ProductDetailPage /> },
      { path: 'search', element: <SearchPage /> },
      { path: 'chat', element: <ProtectedRoute><ChatPage /></ProtectedRoute> },
      { path: 'chat/:id', element: <ProtectedRoute><ChatDetailPage /></ProtectedRoute> },
      { path: 'profile', element: <ProfilePage /> },
      { path: 'my-publish', element: <ProtectedRoute><MyPublishPage /></ProtectedRoute> },
      { path: 'favorites', element: <ProtectedRoute><FavoritesPage /></ProtectedRoute> },
      { path: 'history', element: <ProtectedRoute><BrowsingHistoryPage /></ProtectedRoute> },
      { path: 'verification', element: <ProtectedRoute><VerificationPage /></ProtectedRoute> },
      { path: 'help', element: <HelpCenterPage /> },
      { path: 'settings', element: <SettingsPage /> },
      { path: 'edit-profile', element: <ProtectedRoute><EditProfilePage /></ProtectedRoute> },
      // 404 兜底
      { path: '*', element: <NotFoundPage /> },
    ],
  },
  {
    path: '/admin',
    element: (
      <ProtectedRoute requiredRole={1}>
        <AdminLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Dashboard /> },
      { path: 'users', element: <UserManagement /> },
      { path: 'products', element: <ProductManagement /> },
      { path: 'verifications', element: <VerificationReview /> },
      { path: 'categories', element: <CategoryManagement /> },
    ],
  },
])

/** 404 兜底页 */
function NotFoundPage() {
  return (
    <div className="page-wrapper" style={{ padding: 60, textAlign: 'center' }}>
      <h2 style={{ marginBottom: 12 }}>页面不存在</h2>
      <p style={{ color: 'var(--text-muted)', marginBottom: 20 }}>你访问的页面不存在或已被移除</p>
      <a href="/" style={{ color: 'var(--primary)' }}>返回首页</a>
    </div>
  )
}
