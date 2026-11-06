import re
import unicodedata

from indic_transliteration import sanscript

_DEVANAGARI = re.compile(r"[\u0900-\u097F]")


def normalize(text: str) -> str:
    """Unicode-normalise, tidy punctuation, and turn IAST/Roman input into Devanagari."""
    text = text.replace("\ufffd", "").replace("\ufffe", "")
    text = unicodedata.normalize("NFC", text)
    if not _DEVANAGARI.search(text):  # looks like Roman transliteration
        text = sanscript.transliterate(text, sanscript.IAST, sanscript.DEVANAGARI)
    text = text.replace("\u200b", "").replace("॥", "॥ ").replace("।", "। ")
    return re.sub(r"[ \t]+", " ", text).strip()
