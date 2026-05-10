import os
<<<<<<< HEAD
import time
import logging
from groq import Groq

logger = logging.getLogger(__name__)

client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def call_groq(messages: list, temperature: float = 0.3, max_tokens: int = 1000) -> str:
    """Call Groq API with 3-retry backoff. Returns None on all failures."""
    for attempt in range(3):
        try:
            response = client.chat.completions.create(
                model="llama-3.3-70b-versatile",
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens
            )
            return response.choices[0].message.content
        except Exception as e:
            logger.error(f"Groq call failed (attempt {attempt + 1}/3): {e}")
            if attempt < 2:
                time.sleep(2 ** attempt)  # exponential backoff
    return None
=======
import json
from groq import Groq

class GroqClient:
    def __init__(self):
        # AI Developer 2 builds this, but we need it for Day 3 & 4 testing
        self.api_key = os.getenv("GROQ_API_KEY", "")
        if self.api_key:
            self.client = Groq(api_key=self.api_key)
        else:
            self.client = None
        self.model = "llama-3.3-70b-versatile"

    def generate_response(self, prompt, is_json=False):
        if not self.client:
            return json.dumps({"error": "No Groq API key set."}) if is_json else "Error: No API key."
        
        try:
            chat_completion = self.client.chat.completions.create(
                messages=[{"role": "user", "content": prompt}],
                model=self.model,
                temperature=0.3, # 0.3 for factual as per Day 9 hint
                response_format={"type": "json_object"} if is_json else None
            )
            return chat_completion.choices[0].message.content
        except Exception as e:
            print(f"Groq API Error: {e}")
            if is_json:
                return json.dumps({"is_fallback": True, "error": str(e)})
            return "Fallback error response."
>>>>>>> 4f7ca931deb325071b877936902f8f85bcb32df8
