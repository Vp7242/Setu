"""Dictionary grounding from Monier-Williams (built by scripts/build_dictionary.py).

Given the analysed words, finds likely headwords and returns short English glosses. These are
handed to the language model as evidence so it relies on real entries rather than guessing.
"""
import sqlite3
from pathlib import Path

from indic_transliteration import sanscript

DB_PATH = Path(__file__).resolve().parent.parent / "data" / "mw.sqlite"
_db = None
_warned = False


def _connect():
    global _db, _warned
    if _db is None and DB_PATH.exists():
        _db = sqlite3.connect(DB_PATH, check_same_thread=False)
    if _db is None and not _warned:
        print("[setu] Dictionary not built. Run: python scripts/build_dictionary.py")
        _warned = True
    return _db


def _candidates(slp1: str, exact: bool):
    """Headword guesses. A lemma is tried as-is; an inflected form also loses its ending."""
    s = slp1.rstrip("Hs")
    c = [s]
    if not exact:
        c += [s[:-1], s[:-2], s[:-1] + "a", s[:-2] + "a", s[:-3] + "a"]
    seen = []
    for x in c:
        if len(x) >= 2 and x not in seen:
            seen.append(x)
    return seen


def lookup(words: list[dict]) -> dict[str, str]:
    """words: [{'word':..., 'root':...}] (Devanagari). Returns {word: 'gloss; gloss'}."""
    db = _connect()
    if db is None:
        return {}
    out = {}
    for w in words:
        source = w.get("root") or w["word"]
        try:
            slp1 = sanscript.transliterate(source, sanscript.DEVANAGARI, sanscript.SLP1)
        except Exception:
            continue
        for key in _candidates(slp1, exact=bool(w.get("root"))):
            rows = db.execute("SELECT gloss FROM entries WHERE key=? LIMIT 3", (key,)).fetchall()
            if rows:
                parts = [p.strip() for r in rows for p in r[0][:160].split(";") if len(p.strip()) > 3]
                if parts:
                    out[w["word"]] = "; ".join(parts[:4])
                break
    return out
