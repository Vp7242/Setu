"""The translation pipeline. Each step is its own file, so you can swap or improve one at a time."""
from .analysis import analyze
from .dictionary import lookup
from .normalize import normalize
from .translator import translate
from .verify import score


def _merge(analysis, llm_words):
    """Analyser facts win for split/root/grammar; the LLM supplies meanings (and fills any gaps)."""
    by_word = {w.get("word"): w for w in llm_words}
    same_length = len(llm_words) == len(analysis["words"])
    merged = []
    for i, a in enumerate(analysis["words"]):
        llm = by_word.get(a["word"]) or (llm_words[i] if same_length else {})
        merged.append({
            "word": a["word"],
            "root": a["root"] or llm.get("root", ""),
            "grammar": a["grammar"] or llm.get("grammar", ""),
            "meaning": llm.get("meaning", ""),
        })
    return merged


def run_pipeline(text: str, lang: str) -> dict:
    clean = normalize(text)                                   # 1. clean text / Roman -> Devanagari
    analysis = analyze(clean, lang)                           # 2. sandhi split + morphology (offline)
    glosses = lookup(analysis["words"]) if analysis else {}   # 3. dictionary evidence
    out = translate(clean, lang, analysis, glosses)           # 4. LLM translation, grounded on 2 and 3
    if analysis:
        out["sandhi_split"] = analysis["split"]
        out["words"] = _merge(analysis, out["words"])
    out["normalized"] = clean
    out["confidence"] = score(clean, out["translation"], lang)  # 5. optional verification
    return out
