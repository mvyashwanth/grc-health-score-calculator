import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getStats, getRecords, exportCsv } from '../services/api'
import ScoreBadge from '../components/ScoreBadge'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell
} from 'recharts'

const SCORE_BINS = [
  { label: '0-25', min: 0, max: 25 },
  { label: '26-50', min: 26, max: 50 },
  { label: '51-75', min: 51, max: 75 },
  { label: '76-100', min: 76, max: 100 },
]

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([getStats(), getRecords(0, 100)])
      .then(([statsRes, recRes]) => {
        setStats(statsRes.data)
        setRecords(recRes.data.content || [])
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const chartData = SCORE_BINS.map(bin => ({
    name: bin.label,
    count: records.filter(r => {
      const s = parseFloat(r.healthScore)
      return s >= bin.min && s <= bin.max
    }).length,
    fill: bin.min >= 76 ? '#2E7D32' : bin.min >= 51 ? '#1B4F8A' : bin.min >= 26 ? '#F57F17' : '#C62828'
  }))

  const handleExport = async () => {
    try {
      const res = await exportCsv()
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const a = document.createElement('a')
      a.href = url
      a.download = 'health_records.csv'
      a.click()
    } catch (e) {
      alert('Export failed')
    }
  }

  if (loading) return (
    <div className="space-y-4 animate-pulse">
      <div className="h-8 bg-gray-200 rounded w-48" />
      <div className="grid grid-cols-3 gap-4">
        {[1,2,3].map(i => <div key={i} className="h-32 bg-gray-200 rounded-xl" />)}
      </div>
    </div>
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-500 text-sm mt-1">Health score overview & analytics</p>
        </div>
        <div className="flex gap-3">
          <button onClick={handleExport} className="btn-secondary text-sm">
            📥 Export CSV
          </button>
          <Link to="/records/new" className="btn-primary text-sm">
            ➕ New Record
          </Link>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="card">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Total Records</p>
          <p className="text-4xl font-bold text-primary">{stats?.total ?? 0}</p>
          <p className="text-sm text-gray-500 mt-1">Health profiles tracked</p>
        </div>
        <div className="card">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Active Records</p>
          <p className="text-4xl font-bold text-secondary">{stats?.active ?? 0}</p>
          <p className="text-sm text-gray-500 mt-1">Currently monitored</p>
        </div>
        <div className="card">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Avg Health Score</p>
          <p className="text-4xl font-bold text-warning">
            {stats?.avgScore ? parseFloat(stats.avgScore).toFixed(1) : '—'}
          </p>
          <p className="text-sm text-gray-500 mt-1">Out of 100 points</p>
        </div>
      </div>

      {/* Chart + Recent */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Bar Chart */}
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-4">Score Distribution</h2>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={chartData} barCategoryGap="30%">
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="count" radius={[6,6,0,0]}>
                {chartData.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Recent Records */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-gray-800">Recent Records</h2>
            <Link to="/records" className="text-sm text-primary hover:underline">View all →</Link>
          </div>
          <div className="space-y-3">
            {records.slice(0, 6).map(r => (
              <Link key={r.id} to={`/records/${r.id}`}
                className="flex items-center justify-between py-2 border-b border-gray-50 hover:bg-gray-50 px-2 rounded-lg transition-colors">
                <div>
                  <p className="text-sm font-medium text-gray-800">{r.title}</p>
                  <p className="text-xs text-gray-400">Age {r.age}</p>
                </div>
                <ScoreBadge score={r.healthScore} size="sm" />
              </Link>
            ))}
            {records.length === 0 && (
              <p className="text-sm text-gray-400 text-center py-4">No records yet</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
