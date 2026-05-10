import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getRecords, searchRecords, deleteRecord } from '../services/api'
import ScoreBadge from '../components/ScoreBadge'

export default function RecordList() {
  const [records, setRecords] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  // Debounce search
  useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(search), 400)
    return () => clearTimeout(t)
  }, [search])

  const fetchRecords = useCallback(async () => {
    setLoading(true)
    try {
      const res = debouncedSearch
        ? await searchRecords(debouncedSearch, page)
        : await getRecords(page, 10)
      setRecords(res.data.content || [])
      setTotal(res.data.totalElements || 0)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [debouncedSearch, page])

  useEffect(() => { fetchRecords() }, [fetchRecords])

  const handleDelete = async (id, title) => {
    if (!confirm(`Delete record "${title}"?`)) return
    try {
      await deleteRecord(id)
      fetchRecords()
    } catch (e) {
      alert('Delete failed')
    }
  }

  const totalPages = Math.ceil(total / 10)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Health Records</h1>
          <p className="text-gray-500 text-sm">{total} total records</p>
        </div>
        <Link to="/records/new" className="btn-primary text-sm">➕ New Record</Link>
      </div>

      {/* Search bar */}
      <div className="card !p-4">
        <input
          type="text"
          className="input"
          placeholder="🔍 Search records by name..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0) }}
        />
      </div>

      {/* Table */}
      <div className="card !p-0 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-400">Loading...</div>
        ) : records.length === 0 ? (
          <div className="p-12 text-center">
            <p className="text-4xl mb-3">🩺</p>
            <p className="text-gray-500">No records found</p>
            <Link to="/records/new" className="btn-primary text-sm mt-4 inline-block">Create First Record</Link>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Name', 'Age', 'BMI', 'BP', 'Health Score', 'Status', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {records.map(r => (
                <tr key={r.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-900">
                    <Link to={`/records/${r.id}`} className="hover:text-primary">{r.title}</Link>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{r.age}</td>
                  <td className="px-4 py-3 text-gray-600">{r.bmi ?? '—'}</td>
                  <td className="px-4 py-3 text-gray-600">
                    {r.bloodPressureSystolic && r.bloodPressureDiastolic
                      ? `${r.bloodPressureSystolic}/${r.bloodPressureDiastolic}`
                      : '—'}
                  </td>
                  <td className="px-4 py-3"><ScoreBadge score={r.healthScore} size="sm" /></td>
                  <td className="px-4 py-3">
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      r.status === 'EXCELLENT' ? 'bg-green-100 text-green-700' :
                      r.status === 'GOOD' ? 'bg-blue-100 text-blue-700' :
                      r.status === 'FAIR' ? 'bg-yellow-100 text-yellow-700' :
                      'bg-red-100 text-red-700'
                    }`}>{r.status}</span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button onClick={() => navigate(`/records/${r.id}`)}
                        className="text-xs text-primary hover:underline">View</button>
                      <button onClick={() => navigate(`/records/${r.id}/edit`)}
                        className="text-xs text-gray-500 hover:underline">Edit</button>
                      <button onClick={() => handleDelete(r.id, r.title)}
                        className="text-xs text-red-500 hover:underline">Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <p className="text-sm text-gray-500">Page {page + 1} of {totalPages}</p>
            <div className="flex gap-2">
              <button onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0} className="btn-secondary text-xs disabled:opacity-40">← Prev</button>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1} className="btn-secondary text-xs disabled:opacity-40">Next →</button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
