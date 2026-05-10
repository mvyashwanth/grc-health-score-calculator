export default function ScoreBadge({ score, size = 'md' }) {
  const s = parseFloat(score) || 0

  const color =
    s >= 75 ? 'bg-green-100 text-green-800 border-green-200' :
    s >= 50 ? 'bg-blue-100 text-blue-800 border-blue-200' :
    s >= 30 ? 'bg-yellow-100 text-yellow-800 border-yellow-200' :
              'bg-red-100 text-red-800 border-red-200'

  const label =
    s >= 75 ? 'Excellent' :
    s >= 50 ? 'Good' :
    s >= 30 ? 'Fair' : 'Poor'

  const sizes = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-sm px-3 py-1',
    lg: 'text-2xl px-5 py-2 font-bold'
  }

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border font-semibold ${color} ${sizes[size]}`}>
      {size === 'lg' && <span>💚</span>}
      {s.toFixed(1)} — {label}
    </span>
  )
}
