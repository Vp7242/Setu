import json

from .llm import chat

_LANG = {"mr": "Marathi (Devanagari script)", "en": "English"}

SYSTEM = """You are an expert Sanskrit philologist and translator.
Translate faithfully. Never invent meanings: if a word or passage is uncertain, say so in the
meaning field with "(uncertain)". Reply with JSON only, no markdown."""


def translate(text: str, lang: str, analysis: dict | None = None, glosses: dict | None = None) -> dict:
    hints = ""
    if analysis:
        rows = []
        for w in analysis["words"]:
            row = f'{w["word"]} | root: {w["root"] or "?"} | {w["grammar"] or "?"}'
            if w.get("alternatives"):
                row += " | other possible readings: " + "; ".join(w["alternatives"])
            rows.append(row)
        hints += ("Automatic sandhi split and grammar (from an automatic analyser; the first reading is its best guess, "
                  "but choose the reading that fits the context):\n" + "\n".join(rows) + "\n\n")
    if glosses:
        hints += ("Monier-Williams dictionary entries for likely headwords (use only if they fit the "
                  "context; entries can belong to a different word):\n"
                  + "\n".join(f"{w}: {g}" for w, g in glosses.items()) + "\n\n")

    prompt = f"""Sanskrit text:
{text}

{hints}Return JSON with exactly these keys:
{{"sandhi_split": "the text with sandhi and compounds split, words separated by spaces",
 "translation": "natural, faithful translation into {_LANG[lang]}",
 "words": [{{"word": "...", "root": "dhatu or stem", "grammar": "case/number/tense etc.", "meaning": "in {_LANG[lang]}"}}]}}
List the words in the same order as the sandhi split."""
    return _parse(chat(SYSTEM, prompt, max_tokens=2500))


def _parse(raw: str) -> dict:
    try:
        data = json.loads(raw[raw.index("{"): raw.rindex("}") + 1])
        return {
            "sandhi_split": data.get("sandhi_split", ""),
            "translation": data["translation"],
            "words": data.get("words", []),
        }
    except (ValueError, KeyError):
        return {"sandhi_split": "", "translation": raw.strip(), "words": []}
