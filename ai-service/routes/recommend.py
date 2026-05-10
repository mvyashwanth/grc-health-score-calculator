import os
import json
from datetime import datetime
from flask import Blueprint, jsonify, request

recommend_bp = Blueprint('recommend', __name__)

FALLBACK_RECS = [
    {"action_type": "EXERCISE",      "description": "Aim for 150 minutes of moderate aerobic activity weekly.", "priority": "HIGH"},
    {"action_type": "NUTRITION",     "description": "Eat a balanced diet rich in vegetables, fruits, and whole grains.", "priority": "MEDIUM"},
    {"action_type": "SLEEP",         "description": "Maintain 7-9 hours of quality sleep each night.", "priority": "MEDIUM"},
]

@recommend_bp.route('/recommend', methods=['POST'])
def recommend():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "Invalid JSON body"}), 400

    try:
        from services.groq_client import call_groq

        prompt = f"""You are a health improvement AI. Based on these patient metrics, generate exactly 3 actionable health recommendations.

Patient: Age {data.get('age')}, BMI {data.get('bmi')}, BP {data.get('blood_pressure_systolic')}/{data.get('blood_pressure_diastolic')},
Cholesterol {data.get('cholesterol')}, Blood Sugar {data.get('blood_sugar')}, Exercise {data.get('exercise_hours_per_week')} hrs/week,
Sleep {data.get('sleep_hours_per_day')} hrs/day, Smoking {data.get('smoking')}, Alcohol {data.get('alcohol_units_per_week')} units/week,
Stress {data.get('stress_level')}/10, Health Score {data.get('health_score')}/100.

Return ONLY a JSON array with exactly 3 objects. Each must have:
- "action_type": one of [EXERCISE, NUTRITION, SLEEP, MENTAL_HEALTH, MEDICAL, LIFESTYLE]
- "description": specific actionable recommendation (1-2 sentences)
- "priority": one of [HIGH, MEDIUM, LOW]

Return ONLY the JSON array, no other text."""

        result = call_groq([{"role": "user", "content": prompt}], temperature=0.5)
        is_fallback = False
        recommendations = FALLBACK_RECS

        if result:
            try:
                cleaned = result.strip().replace('```json', '').replace('```', '').strip()
                recommendations = json.loads(cleaned)
            except Exception:
                is_fallback = True
        else:
            is_fallback = True

        return jsonify({
            "recommendations": recommendations,
            "is_fallback":     is_fallback,
            "generated_at":    datetime.utcnow().isoformat()
        })

    except Exception as e:
        return jsonify({
            "recommendations": FALLBACK_RECS,
            "is_fallback":     True,
            "generated_at":    datetime.utcnow().isoformat()
        })