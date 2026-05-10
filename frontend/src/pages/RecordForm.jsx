import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { createRecord, updateRecord, getRecord } from '../services/api'

const FIELDS = [
  { name: 'title', label: 'Full Name / Title', type: 'text', required: true, col: 2 },
  { name: 'age', label: 'Age', type: 'number', min: 0, max: 150, required: true },
  { name: 'bmi', label: 'BMI', type: 'number', step: '0.1', min: 0 },
  { name: 'bloodPressureSystolic', label: 'Blood Pressure Systolic', type: 'number', min: 0 },
  { name: 'bloodPressureDiastolic', label: 'Blood Pressure Diastolic', type: 'number', min: 0 },
  { name: 'cholesterol', label: 'Cholesterol (mg/dL)', type: 'number', min: 0 },
  { name: 'bloodSugar', label: 'Blood Sugar (mg/dL)', type: 'number', step: '0.1', min: 0 },
  { name: 'exerciseHoursPerWeek', label: 'Exercise (hrs/week)', type: 'number', step: '0.5', min: 0 },
  { name: 'sleepHoursPerDay', label: 'Sleep (hrs/day)', type: 'number', step: '0.5', min: 0, max: 24 },
  { name: 'alcoholUnitsPerWeek', label: 'Alcohol (units/week)', type: 'number', min: 0 },
  { name: 'stressLevel', label: 'Stress Level (1-10)', type: 'number', min: 1, max: 10 },
]

const DEFAULTS = {
  title: '', age: '', bmi: '', bloodPressureSystolic: '', bloodPressureDiastolic: '',
  cholesterol: '', bloodSugar: '', exerciseHoursPerWeek: '', sleepHoursPerDay: '',
  smoking: false, alcoholUnitsPerWeek: '', stressLevel: ''
}

export default function RecordForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState(DEFAULTS)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) {
      getRecord(id).then(res => {
        const r = res.data
        setForm({
          title: r.title || '', age: r.age || '', bmi: r.bmi || '',
          bloodPressureSystolic: r.bloodPressureSystolic || '',
          bloodPressureDiastolic: r.bloodPressureDiastolic || '',
          cholesterol: r.cholesterol || '', bloodSugar: r.bloodSugar || '',
          exerciseHoursPerWeek: r.exerciseHoursPerWeek || '',
          sleepHoursPerDay: r.sleepHoursPerDay || '',
          smoking: r.smoking || false,
          alcoholUnitsPerWeek: r.alcoholUnitsPerWeek || '',
          stressLevel: r.stressLevel || ''
        })
      })
    }
  }, [id, isEdit])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    const payload = { ...form }
    // Convert empty strings to null
    Object.keys(payload).forEach(k => {
      if (payload[k] === '') payload[k] = null
    })
    try {
      if (isEdit) {
        await updateRecord(id, payload)
        navigate(`/records/${id}`)
      } else {
        const res = await createRecord(payload)
        navigate(`/records/${res.data.id}`)
      }
    } catch (err) {
      const data = err.response?.data
      setError(data?.message || JSON.stringify(data?.fieldErrors) || 'Save failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <Link to="/records" className="text-sm text-gray-400 hover:text-gray-600">← Back</Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-1">
          {isEdit ? 'Edit Health Record' : 'New Health Record'}
        </h1>
        <p className="text-gray-500 text-sm mt-1">
          Fill in the health metrics below. Health score will be calculated automatically.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="card space-y-4">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">{error}</div>
        )}

        <div className="grid grid-cols-2 gap-4">
          {FIELDS.map(f => (
            <div key={f.name} className={f.col === 2 ? 'col-span-2' : ''}>
              <label className="label">{f.label}{f.required && ' *'}</label>
              <input
                type={f.type}
                className="input"
                value={form[f.name]}
                onChange={(e) => setForm({ ...form, [f.name]: e.target.value })}
                required={f.required}
                min={f.min}
                max={f.max}
                step={f.step}
              />
            </div>
          ))}

          {/* Smoking toggle */}
          <div className="col-span-2">
            <label className="flex items-center gap-3 cursor-pointer">
              <div
                onClick={() => setForm(f => ({ ...f, smoking: !f.smoking }))}
                className={`w-12 h-6 rounded-full transition-colors relative ${form.smoking ? 'bg-red-500' : 'bg-gray-300'}`}
              >
                <div className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${form.smoking ? 'translate-x-6' : 'translate-x-0.5'}`} />
              </div>
              <span className="label !mb-0">Smoking {form.smoking ? '🚬 Yes' : 'No'}</span>
            </label>
          </div>
        </div>

        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={loading} className="btn-primary disabled:opacity-60">
            {loading ? 'Saving...' : isEdit ? 'Update Record' : 'Create Record'}
          </button>
          <Link to="/records" className="btn-secondary">Cancel</Link>
        </div>
      </form>
    </div>
  )
}
