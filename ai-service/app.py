import os
<<<<<<< HEAD
import time
from flask import Flask, jsonify

print("🔥 Starting AI service...")

app = Flask(__name__)

START_TIME = time.time()

# ── Register blueprints ──────────────────────────────
try:
    from routes.health   import health_bp
    app.register_blueprint(health_bp)
    print("✅ health blueprint registered")
except Exception as e:
    print(f"❌ health blueprint failed: {e}")

try:
    from routes.describe import describe_bp
    app.register_blueprint(describe_bp)
    print("✅ describe blueprint registered")
except Exception as e:
    print(f"❌ describe blueprint failed: {e}")

try:
    from routes.recommend import recommend_bp
    app.register_blueprint(recommend_bp)
    print("✅ recommend blueprint registered")
except Exception as e:
    print(f"❌ recommend blueprint failed: {e}")

try:
    from routes.report import report_bp
    app.register_blueprint(report_bp)
    print("✅ report blueprint registered")
except Exception as e:
    print(f"❌ report blueprint failed: {e}")

# ── Rate limiting (optional) ─────────────────────────
try:
    from flask_limiter import Limiter
    from flask_limiter.util import get_remote_address
    limiter = Limiter(
        get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri="memory://"
    )
    print("✅ Rate limiter enabled")
except Exception as e:
    print(f"⚠️  Rate limiter skipped: {e}")

# ── Fallback root route ──────────────────────────────
@app.route('/')
def index():
    return jsonify({
        "service": "Tool-86 AI Service",
        "status":  "running",
        "routes": ["/health", "/describe", "/recommend", "/generate-report"]
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
=======
from flask import Flask, jsonify
from routes.describe import describe_bp
from routes.recommend import recommend_bp
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

# Register Blueprints
app.register_blueprint(describe_bp)
app.register_blueprint(recommend_bp)

# Health endpoint placeholder (Day 7 task)
@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy"}), 200

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port, debug=True)
>>>>>>> 4f7ca931deb325071b877936902f8f85bcb32df8
