package com.setu.sanskrit

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.setu.sanskrit.data.TranslationRepository
import com.setu.sanskrit.data.local.AppDatabase
import com.setu.sanskrit.data.remote.SetuApi
import com.setu.sanskrit.ocr.TextScanner
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Hand-written dependency container (no Hilt, so there is no "magic" to learn).
 * Everything the app needs is created here, once.
 */
class AppContainer(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val okHttp = OkHttpClient.Builder()
        .readTimeout(90, TimeUnit.SECONDS) // LLM calls can be slow
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val api: SetuApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SetuApi::class.java)

    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "setu.db").build()

    val repository = TranslationRepository(api, db.translationDao(), json)
    val scanner = TextScanner(context)
}
