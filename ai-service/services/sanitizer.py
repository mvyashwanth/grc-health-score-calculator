import re
from flask import request, jsonify
from functools import wraps

INJECTION_PATTERNS = [
    r'ignore previous instructions',
    r'ignore all instructions',
    r'you are now',
    r'forget everything',
    r'<script',
    r'javascript:',
    r'DROP TABLE',
    r'SELECT \* FROM',
    r'UNION SELECT',
    r'1=1',
]

def sanitize_string(value: str) -> str:
    """Strip HTML tags and dangerous characters."""
    # Remove HTML tags
    value = re.sub(r'<[^>]+>', '', value)
    # Strip leading/trailing whitespace
    return value.strip()

def detect_injection(data: dict) -> bool:
    """Detect prompt injection attempts in string fields."""
    for key, value in data.items():
        if isinstance(value, str):
            lower = value.lower()
            for pattern in INJECTION_PATTERNS:
                if re.search(pattern, lower, re.IGNORECASE):
                    return True
    return False

def sanitize_input(f):
    """Decorator to sanitize and validate input."""
    @wraps(f)
    def decorated(*args, **kwargs):
        data = request.get_json(silent=True)
        if not data:
            return jsonify({"error": "Invalid JSON body"}), 400
        
        # Sanitize string fields
        for key, value in data.items():
            if isinstance(value, str):
                data[key] = sanitize_string(value)
        
        # Detect injection
        if detect_injection(data):
            return jsonify({"error": "Invalid input detected"}), 400
        
        request.sanitized_data = data
        return f(*args, **kwargs)
    return decorated
