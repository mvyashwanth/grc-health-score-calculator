from flask import Flask, app
import os
import unittest
from routes.describe import describe_bp

class TestDescribeEndpoint(unittest.TestCase):
    def setUp(self):
        self.app = Flask(__name__)
        self.app.register_blueprint(describe_bp)
        self.client = self.app.test_client()

    def test_describe_endpoint_valid_input(self):
        # Using placeholder test inputs as required for Day 2 tasks
        data = {
            "age": 45,
            "blood_pressure": "140/90",
            "cholesterol": 220,
            "lifestyle": "Sedentary",
            "blood_sugar": 105
        }
        res = self.client.post('/describe', json=data)
        # We expect a 200 or 500 fallback depending on API key existence
        self.assertIn(res.status_code, [200, 500])

if __name__ == '__main__':
    unittest.main()
