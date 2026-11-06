package com.setu.sanskrit.ui.translate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setu.sanskrit.domain.model.TargetLanguage
import com.setu.sanskrit.domain.model.TranslationResult
import com.setu.sanskrit.domain.model.WordAnalysis
import com.setu.sanskrit.ui.components.ConfidenceChip
import com.setu.sanskrit.ui.components.SectionCard
import com.setu.sanskrit.ui.components.SetuHeader
import com.setu.sanskrit.ui.theme.ScriptStyle
import com.setu.sanskrit.ui.theme.ScriptStyleLarge

private val PagePadding = Modifier.padding(horizontal = 20.dp)

@Composable
fun TranslateScreen(viewModel: TranslateViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::scan)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SetuHeader("सेतु", "Sanskrit to Marathi and English") }

        item {
            InputCard(
                text = state.input,
                scanning = state.scanning,
                onTextChange = viewModel::onInputChange,
                onScan = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onClear = viewModel::clear,
                modifier = PagePadding,
            )
        }
        item { LanguageSelector(state.language, viewModel::onLanguageChange, PagePadding) }
        item {
            val loading = state.status is TranslateStatus.Loading
            Button(
                onClick = viewModel::translate,
                enabled = state.input.isNotBlank() && !loading,
                shape = RoundedCornerShape(16.dp),
                modifier = PagePadding.fillMaxWidth().height(56.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Translating")
                } else {
                    Text("Translate", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        when (val status = state.status) {
            is TranslateStatus.Success -> resultItems(status.result)
            is TranslateStatus.Error -> item { ErrorCard(status.message, PagePadding) }
            TranslateStatus.Idle, TranslateStatus.Loading -> Unit
        }
    }
}

// ---------- Input ----------

@Composable
private fun InputCard(
    text: String,
    scanning: Boolean,
    onTextChange: (String) -> Unit,
    onScan: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    SectionCard(
        title = "Sanskrit text",
        modifier = modifier,
        trailing = { if (scanning) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) },
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("धर्मक्षेत्रे कुरुक्षेत्रे समवेता युयुत्सवः", style = ScriptStyle) },
            textStyle = ScriptStyle,
            minLines = 3,
            maxLines = 8,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = onScan, label = { Text("Scan image") },
                leadingIcon = { Icon(Icons.Filled.CameraAlt, null, Modifier.size(18.dp)) })
            AssistChip(onClick = { clipboard.getText()?.let { onTextChange(it.text) } }, label = { Text("Paste") },
                leadingIcon = { Icon(Icons.Filled.ContentPaste, null, Modifier.size(18.dp)) })
            if (text.isNotEmpty()) {
                AssistChip(onClick = onClear, label = { Text("Clear") },
                    leadingIcon = { Icon(Icons.Filled.Close, null, Modifier.size(18.dp)) })
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    selected: TargetLanguage,
    onSelect: (TargetLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        TargetLanguage.entries.forEachIndexed { index, lang ->
            SegmentedButton(
                selected = lang == selected,
                onClick = { onSelect(lang) },
                shape = SegmentedButtonDefaults.itemShape(index, TargetLanguage.entries.size),
            ) { Text(lang.label) }
        }
    }
}

// ---------- Results ----------

private fun LazyListScope.resultItems(r: TranslationResult) {
    item {
        val clipboard = LocalClipboardManager.current
        SectionCard(
            title = "Translation (${r.language.label})",
            emphasis = true,
            modifier = PagePadding,
            trailing = {
                IconButton(onClick = { clipboard.setText(AnnotatedString(r.translation)) }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy translation")
                }
            },
        ) {
            Text(r.translation, style = ScriptStyleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            ConfidenceChip(r.confidence)
        }
    }
    if (r.sandhiSplit.isNotBlank()) {
        item { SectionCard("Sandhi split", modifier = PagePadding) { Text(r.sandhiSplit, style = ScriptStyle) } }
    }
    if (r.words.isNotEmpty()) {
        item { Text("Word by word", style = MaterialTheme.typography.titleMedium, modifier = PagePadding) }
        items(r.words.size) { i -> WordRow(r.words[i], PagePadding) }
    }
}

@Composable
private fun WordRow(w: WordAnalysis, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Card(
        onClick = { open = !open },
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(w.word, style = ScriptStyle, modifier = Modifier.weight(0.45f))
                Text(w.meaning, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.55f))
            }
            if (open) {
                Spacer(Modifier.height(8.dp))
                if (w.root.isNotBlank()) DetailLine("Root", w.root)
                if (w.grammar.isNotBlank()) DetailLine("Grammar", w.grammar)
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.padding(top = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.width(72.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
