import axios from 'axios'

const API = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// Attach JWT token to every request
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Handle 401 globally
API.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// Auth
export const login = (data) => API.post('/auth/login', data)
export const register = (data) => API.post('/auth/register', data)

// Health Records
export const getRecords = (page = 0, size = 10) =>
  API.get(`/health-records?page=${page}&size=${size}`)
export const getRecord = (id) => API.get(`/health-records/${id}`)
export const createRecord = (data) => API.post('/health-records', data)
export const updateRecord = (id, data) => API.put(`/health-records/${id}`, data)
export const deleteRecord = (id) => API.delete(`/health-records/${id}`)
export const searchRecords = (q, page = 0) =>
  API.get(`/health-records/search?q=${q}&page=${page}`)
export const getStats = () => API.get('/health-records/stats')
export const exportCsv = () =>
  API.get('/health-records/export', { responseType: 'blob' })

// AI
export const aiRecommend = (id) => API.post(`/health-records/${id}/ai/recommend`)
export const aiReport = (id) => API.post(`/health-records/${id}/ai/report`)
