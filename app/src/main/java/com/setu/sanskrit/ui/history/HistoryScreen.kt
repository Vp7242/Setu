package com.setu.sanskrit.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setu.sanskrit.domain.model.TranslationResult
import com.setu.sanskrit.ui.components.SetuHeader
import com.setu.sanskrit.ui.theme.ScriptStyle

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpen: (TranslationResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        SetuHeader("History", "Your saved translations")
        if (items.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryCard(item, onClick = { onOpen(item) }, onDelete = { viewModel.delete(item.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(item: TranslationResult, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 8.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(item.source, style = ScriptStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text(
                    item.translation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                SuggestionChip(onClick = onClick, label = { Text(item.language.label) })
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            "Nothing here yet. Translate a verse and it will be saved automatically.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
