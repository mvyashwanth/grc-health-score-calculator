import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './services/AuthContext'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import RecordList from './pages/RecordList'
import RecordDetail from './pages/RecordDetail'
import RecordForm from './pages/RecordForm'
import Layout from './components/Layout'

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="flex items-center justify-center h-screen text-gray-500">Loading...</div>
  return user ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="records" element={<RecordList />} />
            <Route path="records/new" element={<RecordForm />} />
            <Route path="records/:id" element={<RecordDetail />} />
            <Route path="records/:id/edit" element={<RecordForm />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
