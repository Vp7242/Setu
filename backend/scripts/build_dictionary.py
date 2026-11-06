"""One-time setup: builds data/mw.sqlite from the Monier-Williams Sanskrit-English dictionary.

    python scripts/build_dictionary.py

Source: Cologne Digital Sanskrit Dictionaries (sanskrit-lexicon project), ~50 MB download.
Output: backend/data/mw.sqlite (headword in SLP1 -> short English gloss).
"""
import re
import sqlite3
import sys
import urllib.request
from pathlib import Path

URL = "https://raw.githubusercontent.com/sanskrit-lexicon/csl-orig/master/v02/mw/mw.txt"
ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "mw.sqlite"
RAW = ROOT / "data" / "mw.txt"

HEAD = re.compile(r"<L>([^<]*)<pc>[^<]*<k1>([^<]*)<k2>")


def clean(text: str) -> str:
    """Turn a raw MW entry body into a short English gloss."""
    text = text.split("¦", 1)[-1]
    text = re.sub(r"<ls[^>]*>.*?</ls>", "", text)           # literary sources
    text = re.sub(r"<(s|s1|lang|gk|etym|ab|hom|lex)[^>]*>.*?</\1>", "", text)  # Sanskrit forms, abbreviations
    text = re.sub(r"<[^>]+>", "", text)                      # any remaining tags
    text = re.sub(r"\([^()]*\)|\[[^\[\]]*\]", "", text)      # parentheticals and brackets
    text = re.sub(r"\s+", " ", text).strip(" ;,.:-")
    return text[:220]


def main():
    RAW.parent.mkdir(exist_ok=True)
    if not RAW.exists():
        print("Downloading Monier-Williams (~50 MB)...")
        urllib.request.urlretrieve(URL, RAW)
    db = sqlite3.connect(OUT)
    db.execute("DROP TABLE IF EXISTS entries")
    db.execute("CREATE TABLE entries (key TEXT, gloss TEXT)")
    key, body, n = None, [], 0
    with open(RAW, encoding="utf-8") as f:
        for line in f:
            m = HEAD.match(line)
            if m:
                key, body = m.group(2), []
            elif line.startswith("<LEND>"):
                gloss = clean(" ".join(body)) if key else ""
                if len(gloss) > 2:
                    db.execute("INSERT INTO entries VALUES (?, ?)", (key, gloss))
                    n += 1
                key = None
            elif key is not None:
                body.append(line.strip())
    db.execute("CREATE INDEX idx_key ON entries(key)")
    db.commit()
    db.close()
    print(f"Done: {n} entries -> {OUT}")


if __name__ == "__main__":
    sys.exit(main())
