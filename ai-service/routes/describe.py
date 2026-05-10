<<<<<<< HEAD
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
=======
from flask import Blueprint, request, jsonify
from datetime import datetime, timezone
import json
import os
from services.groq_client import GroqClient

describe_bp = Blueprint('describe', __name__)
groq_client = GroqClient()

def load_prompt():
    prompt_path = os.path.join(os.path.dirname(__file__), '../prompts/primary_prompt.txt')
    with open(prompt_path, 'r') as f:
        return f.read()

@describe_bp.route('/describe', methods=['POST'])
def describe():
    """
    Day 3 Task: POST /describe 
    validates input, loads prompt, calls Groq, returns JSON with generated_at
    """
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid input, JSON payload required"}), 400

    prompt_template = load_prompt()
    prompt = prompt_template.replace('{input_data}', json.dumps(data))
    
    response_content = groq_client.generate_response(prompt, is_json=True)
    
    try:
        parsed_response = json.loads(response_content)
        parsed_response['generated_at'] = datetime.now(timezone.utc).isoformat()
        return jsonify(parsed_response), 200
    except Exception as e:
        return jsonify({"is_fallback": True, "error": "Failed to parse AI response"}), 500
>>>>>>> 4f7ca931deb325071b877936902f8f85bcb32df8
