import os
from datetime import datetime
from flask import Blueprint, jsonify, request

describe_bp = Blueprint('describe', __name__)

FALLBACK = "Health analysis temporarily unavailable. Please try again later."

@describe_bp.route('/describe', methods=['POST'])
def describe():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "Invalid JSON body"}), 400

    try:
        from services.groq_client import call_groq

        prompt = f"""You are a medical health analyst AI. Given the following patient health metrics, 
provide a concise, professional 2-3 paragraph description of the patient's overall health status.

Patient Health Metrics:
- Age: {data.get('age', 'N/A')}
- BMI: {data.get('bmi', 'N/A')}
- Blood Pressure: {data.get('blood_pressure_systolic', 'N/A')}/{data.get('blood_pressure_diastolic', 'N/A')} mmHg
- Cholesterol: {data.get('cholesterol', 'N/A')} mg/dL
- Blood Sugar: {data.get('blood_sugar', 'N/A')} mg/dL
- Exercise: {data.get('exercise_hours_per_week', 'N/A')} hours/week
- Sleep: {data.get('sleep_hours_per_day', 'N/A')} hours/day
- Smoking: {data.get('smoking', False)}
- Alcohol: {data.get('alcohol_units_per_week', 'N/A')} units/week
- Stress Level: {data.get('stress_level', 'N/A')}/10
- Health Score: {data.get('health_score', 'N/A')}/100

Respond with a professional health summary under 200 words."""

        result = call_groq([{"role": "user", "content": prompt}], temperature=0.3)
        is_fallback = result is None

        return jsonify({
            "description":  result if result else FALLBACK,
            "is_fallback":  is_fallback,
            "generated_at": datetime.utcnow().isoformat()
        })

    except Exception as e:
        return jsonify({
            "description":  FALLBACK,
            "is_fallback":  True,
            "generated_at": datetime.utcnow().isoformat(),
            "error":        str(e)
        })