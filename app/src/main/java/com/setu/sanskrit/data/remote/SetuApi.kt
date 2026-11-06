package com.setu.sanskrit.data.remote

import com.setu.sanskrit.domain.model.WordAnalysis
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class TranslateRequest(
    val text: String,
    @SerialName("target_lang") val targetLang: String,
)

@Serializable
data class TranslateResponse(
    val normalized: String = "",
    @SerialName("sandhi_split") val sandhiSplit: String = "",
    val translation: String,
    val words: List<WordAnalysis> = emptyList(),
    val confidence: Float = 0f,
)

/** The only network endpoint the app uses. Mirrors backend/main.py. */
interface SetuApi {
    @POST("translate")
    suspend fun translate(@Body request: TranslateRequest): TranslateResponse
}
