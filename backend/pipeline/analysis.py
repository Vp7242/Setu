"""Sandhi splitting, lemmatisation and grammar: the step that happens BEFORE the language model.

Analysers, tried in this order (set SETU_ANALYZER to force one: ps | byt5 | parser):
  1. process-sanskrit  (default). Handles verse too: gives every word a lemma, case/number or
                       tense/person, and Monier-Williams data. Works offline after
                       `update-ps-database`. Its database is licensed CC BY-NC (non-commercial).
  2. byt5              Same library, but with the neural ByT5-Sanskrit segmenter (Nehrdich et al.,
                       2024). Needs `pip install -r requirements-byt5.txt` and downloads about 2 GB
                       from Hugging Face on first use. Opt in with SETU_ANALYZER=byt5.
  3. sanskrit_parser   Fallback. Good on simple sentences, split-only on verse.
If every analyser fails, the pipeline still works with the language model alone.
"""
import os
import re

from indic_transliteration import sanscript

from .analysis_parser import analyze_parser

_CASE = {"Nom": "nominative", "Acc": "accusative", "Ins": "instrumental", "Dat": "dative",
         "Abl": "ablative", "Gen": "genitive", "Loc": "locative", "Voc": "vocative"}
_NUM = {"Sg": "singular", "Du": "dual", "Pl": "plural"}
# the library uses the Sanskrit order: prathama = 3rd person, madhyama = 2nd, uttama = 1st
_PERSON = {"First": "3rd person", "Second": "2nd person", "Third": "1st person"}

_ps_ready = None  # None = not tried yet


def _dev(iast: str) -> str:
    return sanscript.transliterate(iast, sanscript.IAST, sanscript.DEVANAGARI)


def _forms(tags) -> str:
    """[('Loc','Sg'), ...] -> 'locative singular / ...' (also handles person+number for verbs)."""
    out = []
    for t in tags or []:
        a, b = (list(t) + ["", ""])[:2]
        if a in _CASE:
            out.append(f"{_CASE[a]} {_NUM.get(b, b)}")
        elif a in _PERSON:
            out.append(f"{_PERSON[a]} {_NUM.get(b, b)}")
        else:
            out.append(" ".join(x for x in (a, b) if x))
    return " / ".join(out)


def _candidate(entry) -> dict:
    lemma, desc, tags = entry[0], entry[1], entry[2]
    if str(desc).startswith("indeclinable"):  # case/number tags are meaningless for these
        return {"root": _dev(lemma), "grammar": str(desc)}
    return {"root": _dev(lemma), "grammar": f"{desc}; {_forms(tags)}".strip("; ")}


def _analyze_ps(text: str, byt5: bool):
    global _ps_ready
    if byt5:
        from process_sanskrit.functions.processBYT5 import processBYT5 as run
    else:
        import process_sanskrit as ps
        run = ps.process
    iast = sanscript.transliterate(re.sub(r"[।॥\n]+", " ", text), sanscript.DEVANAGARI, sanscript.IAST)
    entries = run(iast.strip())
    if not entries:
        return None

    # entries for one written word sit next to each other; group them by the surface form (index 4)
    groups = []
    for e in entries:
        if not isinstance(e, (list, tuple)) or len(e) < 5:
            continue
        if groups and groups[-1][0] == e[4]:
            groups[-1][1].append(e)
        else:
            groups.append((e[4], [e]))
    if not groups:
        return None

    words = []
    for surface, cands in groups:
        main = _candidate(cands[0])
        alts = [f'{c["root"]} ({c["grammar"]})' for c in map(_candidate, cands[1:3])]
        words.append({"word": _dev(surface), "root": main["root"], "grammar": main["grammar"], "alternatives": alts})
    return {"split": " ".join(w["word"] for w in words), "words": words, "parsed": True}


def analyze(text: str, lang: str = "en"):
    """Return {"split", "words": [{word, root, grammar, alternatives?}], "parsed"} or None."""
    mode = os.getenv("SETU_ANALYZER", "auto")
    if mode in ("auto", "ps", "byt5"):
        try:
            result = _analyze_ps(text, byt5=(mode == "byt5"))
            if result:
                return result
        except Exception as e:  # missing database, no GPU/torch, no internet for the model ...
            print(f"[setu] process-sanskrit unavailable ({type(e).__name__}: {str(e)[:120]}); using fallback")
    return analyze_parser(text, lang)
