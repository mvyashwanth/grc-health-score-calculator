import time
from flask import Blueprint, jsonify

health_bp = Blueprint('health', __name__)

START_TIME = time.time()

@health_bp.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status":         "ok",
        "model":          "llama-3.3-70b-versatile",
        "uptime_seconds": round(time.time() - START_TIME, 2)
    })