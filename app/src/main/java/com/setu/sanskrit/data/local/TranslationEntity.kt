package com.setu.sanskrit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val normalized: String,
    val sandhiSplit: String,
    val translation: String,
    val language: String,
    val wordsJson: String,
    val confidence: Float,
    val createdAt: Long,
)
