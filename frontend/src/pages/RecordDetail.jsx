import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { getRecord, deleteRecord, aiRecommend, aiReport } from '../services/api'
import ScoreBadge from '../components/ScoreBadge'

export default function RecordDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState(null)
  const [loading, setLoading] = useState(true)
  const [aiLoading, setAiLoading] = useState({ recommend: false, report: false })

  useEffect(() => {
    getRecord(id)
      .then(res => setRecord(res.data))
      .catch(() => navigate('/records'))
      .finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!confirm('Delete this record?')) return
    await deleteRecord(id)
    navigate('/records')
  }

  const handleRecommend = async () => {
    setAiLoading(a => ({ ...a, recommend: true }))
    try {
      const res = await aiRecommend(id)
      setRecord(res.data)
    } catch (e) { alert('AI recommendation failed') }
    finally { setAiLoading(a => ({ ...a, recommend: false })) }
  }

  const handleReport = async () => {
    setAiLoading(a => ({ ...a, report: true }))
    try {
      const res = await aiReport(id)
      setRecord(res.data)
    } catch (e) { alert('AI report failed') }
    finally { setAiLoading(a => ({ ...a, report: false })) }
  }

  if (loading) return <div className="text-center py-16 text-gray-400">Loading...</div>
  if (!record) return null

  const metrics = [
    ['Age', record.age, 'years'],
    ['BMI', record.bmi, ''],
    ['Blood Pressure', record.bloodPressureSystolic && record.bloodPressureDiastolic
      ? `${record.bloodPressureSystolic}/${record.bloodPressureDiastolic}` : '—', 'mmHg'],
    ['Cholesterol', record.cholesterol, 'mg/dL'],
    ['Blood Sugar', record.bloodSugar, 'mg/dL'],
    ['Exercise', record.exerciseHoursPerWeek, 'hrs/week'],
    ['Sleep', record.sleepHoursPerDay, 'hrs/day'],
    ['Smoking', record.smoking ? 'Yes 🚬' : 'No', ''],
    ['Alcohol', record.alcoholUnitsPerWeek, 'units/week'],
    ['Stress', record.stressLevel, '/10'],
  ]

  let recommendations = []
  if (record.aiRecommendations) {
    try {
      const parsed = typeof record.aiRecommendations === 'string'
        ? JSON.parse(record.aiRecommendations)
        : record.aiRecommendations
      recommendations = Array.isArray(parsed) ? parsed : []
    } catch { recommendations = [] }
  }

  let reportObj = null
  if (record.aiReport) {
    try {
      reportObj = typeof record.aiReport === 'string'
        ? JSON.parse(record.aiReport)
        : record.aiReport
      if (typeof reportObj === 'string') reportObj = null
    } catch { reportObj = null }
  }

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <Link to="/records" className="text-sm text-gray-400 hover:text-gray-600">← Back to Records</Link>
          <h1 className="text-2xl font-bold text-gray-900 mt-1">{record.title}</h1>
          <div className="mt-2"><ScoreBadge score={record.healthScore} size="lg" /></div>
        </div>
        <div className="flex gap-2">
          <Link to={`/records/${id}/edit`} className="btn-secondary text-sm">✏️ Edit</Link>
          <button onClick={handleDelete} className="btn-danger text-sm">🗑 Delete</button>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="card">
        <h2 className="font-semibold text-gray-800 mb-4">Health Metrics</h2>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          {metrics.map(([label, value, unit]) => (
            <div key={label} className="bg-surface rounded-lg p-3 text-center">
              <p className="text-xs text-gray-400 mb-1">{label}</p>
              <p className="font-bold text-gray-800">{value ?? '—'} <span className="text-xs font-normal text-gray-400">{unit}</span></p>
            </div>
          ))}
        </div>
      </div>

      {/* AI Description */}
      {record.aiDescription && (
        <div className="card border-l-4 border-primary">
          <div className="flex items-center gap-2 mb-2">
            <span>🤖</span>
            <h2 className="font-semibold text-gray-800">AI Health Description</h2>
            {record.isFallback && <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded-full">Fallback</span>}
          </div>
          <p className="text-gray-600 text-sm leading-relaxed">{record.aiDescription}</p>
        </div>
      )}

      {/* AI Buttons */}
      <div className="flex gap-3">
        <button onClick={handleRecommend} disabled={aiLoading.recommend}
          className="btn-primary text-sm disabled:opacity-60 flex items-center gap-2">
          {aiLoading.recommend ? '⏳ Generating...' : '💡 AI Recommendations'}
        </button>
        <button onClick={handleReport} disabled={aiLoading.report}
          className="btn-secondary text-sm disabled:opacity-60 flex items-center gap-2">
          {aiLoading.report ? '⏳ Generating...' : '📄 Generate Report'}
        </button>
      </div>

      {/* AI Recommendations */}
      {recommendations.length > 0 && (
        <div className="card">
          <h2 className="font-semibold text-gray-800 mb-4">💡 AI Recommendations</h2>
          <div className="space-y-3">
            {recommendations.map((rec, i) => (
              <div key={i} className="flex items-start gap-3 p-3 bg-surface rounded-lg">
                <span className={`text-xs font-bold px-2 py-1 rounded-full flex-shrink-0 ${
                  rec.priority === 'HIGH' ? 'bg-red-100 text-red-700' :
                  rec.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
                  'bg-green-100 text-green-700'
                }`}>{rec.priority}</span>
                <div>
                  <p className="text-xs font-semibold text-gray-400 uppercase">{rec.action_type}</p>
                  <p className="text-sm text-gray-700 mt-0.5">{rec.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* AI Report */}
      {reportObj && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-gray-800">📄 Health Report</h2>
            <span className={`text-xs font-bold px-3 py-1 rounded-full ${
              reportObj.risk_level === 'LOW' ? 'bg-green-100 text-green-700' :
              reportObj.risk_level === 'MODERATE' ? 'bg-yellow-100 text-yellow-700' :
              reportObj.risk_level === 'HIGH' ? 'bg-orange-100 text-orange-700' :
              'bg-red-100 text-red-700'
            }`}>Risk: {reportObj.risk_level}</span>
          </div>
          <p className="text-gray-600 text-sm italic mb-3">{reportObj.summary}</p>
          <p className="text-gray-700 text-sm leading-relaxed mb-4">{reportObj.overview}</p>

          {reportObj.key_findings?.length > 0 && (
            <div className="mb-4">
              <p className="text-xs font-semibold text-gray-500 uppercase mb-2">Key Findings</p>
              <ul className="space-y-1">
                {reportObj.key_findings.map((f, i) => (
                  <li key={i} className="text-sm text-gray-700 flex items-start gap-2">
                    <span className="text-primary mt-0.5">•</span>{f}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      <p className="text-xs text-gray-400">
        Created: {new Date(record.createdAt).toLocaleDateString()} |
        Updated: {new Date(record.updatedAt).toLocaleDateString()}
      </p>
    </div>
  )
}
