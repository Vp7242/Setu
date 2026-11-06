package com.setu.sanskrit.domain.model

import kotlinx.serialization.Serializable

/** Languages the user can translate INTO. Add a new language here (and in the backend). */
enum class TargetLanguage(val code: String, val label: String) {
    MARATHI("mr", "मराठी"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: MARATHI
    }
}

/** One word of the sentence, explained. Also used directly as the API/DB format. */
@Serializable
data class WordAnalysis(
    val word: String,
    val root: String = "",
    val grammar: String = "",
    val meaning: String = "",
)

/** A finished translation, as shown on screen and stored in history. */
data class TranslationResult(
    val id: Long = 0,
    val source: String,
    val normalized: String,
    val sandhiSplit: String,
    val translation: String,
    val language: TargetLanguage,
    val words: List<WordAnalysis>,
    val confidence: Float,
    val createdAt: Long,
)
