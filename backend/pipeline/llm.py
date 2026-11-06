"""One place that talks to the language model.

Groq, OpenRouter and Ollama all speak the same "OpenAI-compatible" protocol, so switching
provider is just environment variables (see README). No code changes needed.
"""
import os

from openai import OpenAI

# provider: (base_url, name of the env var holding the key, default model)
PROVIDERS = {
    "groq": ("https://api.groq.com/openai/v1", "GROQ_API_KEY", "llama-3.3-70b-versatile"),
    "openrouter": ("https://openrouter.ai/api/v1", "OPENROUTER_API_KEY", "meta-llama/llama-3.3-70b-instruct:free"),
    "ollama": ("http://localhost:11434/v1", "", "qwen2.5:14b"),  # runs on your own computer
}

PROVIDER = os.getenv("SETU_PROVIDER", "groq")
_base_url, _key_var, _default_model = PROVIDERS[PROVIDER]
MODEL = os.getenv("SETU_MODEL", _default_model)

_client = None


def chat(system: str, user: str, max_tokens: int = 2000) -> str:
    global _client
    if _client is None:
        key = os.getenv(_key_var) if _key_var else "ollama"
        if not key:
            raise RuntimeError(f"Set the {_key_var} environment variable before starting the server.")
        _client = OpenAI(base_url=_base_url, api_key=key)
    resp = _client.chat.completions.create(
        model=MODEL,
        max_tokens=max_tokens,
        temperature=0.2,
        messages=[{"role": "system", "content": system}, {"role": "user", "content": user}],
    )
    return resp.choices[0].message.content or ""
