package com.setu.sanskrit.data

import com.setu.sanskrit.data.local.TranslationDao
import com.setu.sanskrit.data.local.TranslationEntity
import com.setu.sanskrit.data.remote.SetuApi
import com.setu.sanskrit.data.remote.TranslateRequest
import com.setu.sanskrit.domain.model.TargetLanguage
import com.setu.sanskrit.domain.model.TranslationResult
import com.setu.sanskrit.domain.model.WordAnalysis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

/** Single place that knows about network + database. The UI never touches them directly. */
class TranslationRepository(
    private val api: SetuApi,
    private val dao: TranslationDao,
    private val json: Json,
) {
    private val wordsSerializer = ListSerializer(WordAnalysis.serializer())

    val history: Flow<List<TranslationResult>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun translate(text: String, language: TargetLanguage): Result<TranslationResult> = try {
        val r = api.translate(TranslateRequest(text, language.code))
        val result = TranslationResult(
            source = text,
            normalized = r.normalized,
            sandhiSplit = r.sandhiSplit,
            translation = r.translation,
            language = language,
            words = r.words,
            confidence = r.confidence,
            createdAt = System.currentTimeMillis(),
        )
        val id = dao.insert(result.toEntity())
        Result.success(result.copy(id = id))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(Exception("Can't reach the translation server. Check that it is running and your connection is on."))
    } catch (e: HttpException) {
        val detail = runCatching {
            val body = e.response()?.errorBody()?.string() ?: ""
            json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.content
        }.getOrNull()
        val errorMsg = if (!detail.isNullOrBlank()) detail else "The server returned an error (${e.code()}). Try again in a moment."
        Result.failure(Exception(errorMsg))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun delete(id: Long) = dao.delete(id)

    // --- mapping between database rows and domain objects ---
    private fun TranslationResult.toEntity() = TranslationEntity(
        source = source, normalized = normalized, sandhiSplit = sandhiSplit,
        translation = translation, language = language.code,
        wordsJson = json.encodeToString(wordsSerializer, words),
        confidence = confidence, createdAt = createdAt,
    )

    private fun TranslationEntity.toDomain() = TranslationResult(
        id = id, source = source, normalized = normalized, sandhiSplit = sandhiSplit,
        translation = translation, language = TargetLanguage.fromCode(language),
        words = runCatching { json.decodeFromString(wordsSerializer, wordsJson) }.getOrDefault(emptyList()),
        confidence = confidence, createdAt = createdAt,
    )
}
