import os
import json
from datetime import datetime
from flask import Blueprint, jsonify, request

report_bp = Blueprint('report', __name__)

FALLBACK_REPORT = {
    "title":            "Health Assessment Report",
    "summary":          "Report generation temporarily unavailable.",
    "overview":         "Please try again later.",
    "key_findings":     [],
    "recommendations":  [],
    "risk_level":       "UNKNOWN"
}

@report_bp.route('/generate-report', methods=['POST'])
def generate_report():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "Invalid JSON body"}), 400

    try:
        from services.groq_client import call_groq

        prompt = f"""Generate a structured health report for this patient.

Age: {data.get('age')}, BMI: {data.get('bmi')}, BP: {data.get('blood_pressure_systolic')}/{data.get('blood_pressure_diastolic')},
Cholesterol: {data.get('cholesterol')}, Blood Sugar: {data.get('blood_sugar')},
Exercise: {data.get('exercise_hours_per_week')} hrs/week, Sleep: {data.get('sleep_hours_per_day')} hrs/day,
Smoking: {data.get('smoking')}, Alcohol: {data.get('alcohol_units_per_week')} units/week,
Stress: {data.get('stress_level')}/10, Health Score: {data.get('health_score')}/100.

Return ONLY this JSON format:
{{"title":"Health Assessment Report","summary":"one sentence","overview":"2-3 paragraphs","key_findings":["finding1","finding2","finding3"],"recommendations":["rec1","rec2","rec3"],"risk_level":"LOW or MODERATE or HIGH or CRITICAL"}}

Return ONLY the JSON, no markdown, no extra text."""

        result = call_groq([{"role": "user", "content": prompt}], temperature=0.3, max_tokens=1500)
        is_fallback = False
        report = FALLBACK_REPORT

        if result:
            try:
                cleaned = result.strip().replace('```json', '').replace('```', '').strip()
                report = json.loads(cleaned)
            except Exception:
                is_fallback = True
        else:
            is_fallback = True

        return jsonify({
            "report":       report,
            "is_fallback":  is_fallback,
            "generated_at": datetime.utcnow().isoformat()
        })

    except Exception as e:
        return jsonify({
            "report":       FALLBACK_REPORT,
            "is_fallback":  True,
            "generated_at": datetime.utcnow().isoformat()
        })