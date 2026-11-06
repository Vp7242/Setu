"""FALLBACK analyser: sandhi splitting + morphology using the offline `sanskrit_parser` library.

What it gives you:
  * splitting of sandhi and compounds, e.g.  रामोगच्छति -> रामः गच्छति
  * for sentences it can fully parse: root (lemma), case/number/gender/tense, and the
    kāraka relation (kartā, karma ...) of each word.

Limits (be aware):
  * The first-ranked split is not always the right one (scoring models are not installed).
  * Full grammatical parsing only succeeds for fairly simple, complete sentences. For verse
    it usually falls back to split-only, and the LLM supplies the grammar notes.
"""
import logging
import re

_parser = None

_EN = {
    # case
    "प्रथमाविभक्तिः": "nominative", "द्वितीयाविभक्तिः": "accusative", "तृतीयाविभक्तिः": "instrumental",
    "चतुर्थीविभक्तिः": "dative", "पञ्चमीविभक्तिः": "ablative", "षष्ठीविभक्तिः": "genitive",
    "सप्तमीविभक्तिः": "locative", "सम्बोधनविभक्तिः": "vocative", "संबोधनविभक्तिः": "vocative",
    # number
    "एकवचनम्": "singular", "द्विवचनम्": "dual", "बहुवचनम्": "plural",
    # gender
    "पुंल्लिङ्गम्": "masculine", "स्त्रीलिङ्गम्": "feminine", "नपुंसकलिङ्गम्": "neuter", "त्रिलिङ्गम्": "any gender",
    # verb
    "लट्": "present", "लङ्": "imperfect", "लृट्": "future", "लोट्": "imperative", "विधिलिङ्": "optative",
    "लिट्": "perfect", "लुङ्": "aorist",
    "प्रथमपुरुषः": "3rd person", "मध्यमपुरुषः": "2nd person", "उत्तमपुरुषः": "1st person",
    "कर्तरि": "active", "कर्मणि": "passive", "भावे": "impersonal",
    "परस्मैपदम्": "parasmaipada", "आत्मनेपदम्": "ātmanepada", "अव्ययम्": "indeclinable",
    # kāraka relations
    "कर्ता": "subject (kartā)", "कर्म": "object (karma)", "करणम्": "instrument (karaṇa)",
    "सम्प्रदानम्": "recipient (sampradāna)", "अपादानम्": "source (apādāna)", "अधिकरणम्": "location (adhikaraṇa)",
    "विशेषणम्": "modifier",
}
_HIDE = {"प्राथमिकः", "समासपूर्वपदनामपदम्"}  # technical tags that add noise


def _get_parser():
    global _parser
    if _parser is None:
        from sanskrit_parser import Parser
        _parser = Parser(input_encoding="devanagari", output_encoding="devanagari")
        # the library emits thousands of debug/info lines per request; quiet only its own loggers
        for name in list(logging.root.manager.loggerDict):
            if name.startswith("sanskrit_parser"):
                logging.getLogger(name).setLevel(logging.WARNING)
    return _parser


def _display(word: str) -> str:
    """Parser uses the internal pre-sandhi form (…स्); show it with a visarga (…ः)."""
    return re.sub(r"[सर]्$", "ः", word)


def _label(tags, relation, lang) -> str:
    parts = [t for t in tags if t not in _HIDE]
    if relation:
        parts.append(relation)
    if lang == "en":
        parts = [_EN.get(p, p) for p in parts]
    return ", ".join(parts)


def analyze_parser(text: str, lang: str = "en"):
    """Return {"split": str, "words": [{word, root, grammar}], "parsed": bool} or None on failure."""
    try:
        parser = _get_parser()
    except Exception:
        return None  # sanskrit_parser not installed -> pipeline falls back to the LLM alone

    words, lines, parsed = [], [], False
    for line in re.split(r"[\n।॥]+", text):
        line = line.strip()
        if not line:
            continue
        try:
            splits = parser.split(line, limit=3)
        except Exception:
            splits = None
        if not splits:  # unknown words: keep them as typed
            tokens = line.split()
            words += [{"word": w, "root": "", "grammar": ""} for w in tokens]
            lines.append(" ".join(tokens))
            continue

        best = splits[0]
        info = {}
        try:
            parses = best.parse(limit=1)
            if parses:
                for item in parses[0].serializable()["graph"]:
                    node = item.get("node", item)
                    info[_display(node["pada"])] = (
                        node.get("root", ""), node.get("tags", []), item.get("sambandha", ""))
                parsed = True
        except Exception:
            pass

        tokens = [_display(w.devanagari()) for w in best.split]
        for tok in tokens:
            root, tags, rel = info.get(tok, ("", [], ""))
            words.append({"word": tok, "root": root, "grammar": _label(tags, rel, lang)})
        lines.append(" ".join(tokens))

    return {"split": "\n".join(lines), "words": words, "parsed": parsed} if words else None
