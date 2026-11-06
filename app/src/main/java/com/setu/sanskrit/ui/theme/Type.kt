package com.setu.sanskrit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Serif for anything that is "the text" (Sanskrit, translation); sans for interface chrome. */
val SetuTypography = Typography().copy(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 52.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 26.sp),
)

/** For Sanskrit / Marathi passages. Tall line-height keeps Devanagari matras from colliding. */
val ScriptStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, lineHeight = 36.sp)
val ScriptStyleLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 24.sp, lineHeight = 40.sp)
