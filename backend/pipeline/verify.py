import os
import re

from .llm import chat


def score(source: str, translation: str, lang: str) -> float:
    """Second-opinion check: a separate model call rates faithfulness from 0 to 1.

    OFF by default because free tiers have small daily token limits. Turn on with SETU_VERIFY=1.
    This is an LLM judge, so treat it as a warning light, not proof. Replace it with
    back-translation + chrF++ / COMET once you have a test set.
    """
    if os.getenv("SETU_VERIFY", "0") != "1":
        return 0.0
    try:
        reply = chat(
            "You grade translations. Reply with only a number.",
            f"Sanskrit:\n{source}\n\nTranslation:\n{translation}\n\n"
            "How faithful and accurate is the translation? Reply with only a number from 0.0 to 1.0.",
            max_tokens=10,
        )
        m = re.search(r"[01](?:\.\d+)?", reply)
        return max(0.0, min(1.0, float(m.group()))) if m else 0.0
    except Exception:
        return 0.0
