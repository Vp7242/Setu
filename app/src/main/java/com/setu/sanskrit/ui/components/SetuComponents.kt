package com.setu.sanskrit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.setu.sanskrit.ui.theme.*

/** Ink-coloured banner at the top of a screen. The turmeric dot echoes the string-hole of a palm-leaf manuscript. */
@Composable
fun SetuHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val ink = if (isSystemInDarkTheme()) NightInk else InkIndigo
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(ink)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineLarge, color = Turmeric)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(Turmeric))
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = Parchment.copy(alpha = 0.85f))
    }
}

/** Standard content card: title row + body. `emphasis` makes the main answer stand out. */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    emphasis: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasis) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (emphasis) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                trailing?.invoke()
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

/** Shows how much to trust the translation. Low scores say "check this with a scholar". */
@Composable
fun ConfidenceChip(confidence: Float, modifier: Modifier = Modifier) {
    if (confidence <= 0f) return
    val (label, color) = when {
        confidence >= 0.8f -> "High confidence" to MaterialTheme.colorScheme.secondary
        confidence >= 0.5f -> "Medium confidence" to MaterialTheme.colorScheme.tertiary
        else -> "Low confidence, please verify" to MaterialTheme.colorScheme.error
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            Text("$label (${(confidence * 100).toInt()}%)", style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}
