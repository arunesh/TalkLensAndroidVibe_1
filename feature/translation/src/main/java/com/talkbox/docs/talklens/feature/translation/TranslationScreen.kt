package com.talkbox.docs.talklens.feature.translation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensCard
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensLoadingIndicator
import com.talkbox.docs.talklens.core.designsystem.component.TalkLensPrimaryButton
import com.talkbox.docs.talklens.core.designsystem.theme.SourceTextBackground
import com.talkbox.docs.talklens.core.designsystem.theme.TranslatedTextBackground
import com.talkbox.docs.talklens.core.model.Language
import kotlinx.coroutines.launch

/**
 * Translation screen for displaying and translating text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(
    sourceText: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TranslationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sourceLanguage by viewModel.sourceLanguage.collectAsStateWithLifecycle()
    val targetLanguage by viewModel.targetLanguage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Initialize with source text
    LaunchedEffect(sourceText) {
        if (uiState is TranslationUiState.Idle) {
            viewModel.setSourceText(sourceText)
            viewModel.translateText(sourceText)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageSelector(
                    selectedLanguage = sourceLanguage,
                    onLanguageSelected = viewModel::onSourceLanguageChanged,
                    label = "From",
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = viewModel::swapLanguages) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap languages"
                    )
                }

                LanguageSelector(
                    selectedLanguage = targetLanguage,
                    onLanguageSelected = viewModel::onTargetLanguageChanged,
                    label = "To",
                    modifier = Modifier.weight(1f)
                )
            }

            // Original Text
            TalkLensCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Original (${sourceLanguage.displayName})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = {
                                copyToClipboard(context, sourceText, "Original text")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Original text copied")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy original text"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sourceText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

            // Translation State
            when (val state = uiState) {
                is TranslationUiState.Translating -> {
                    TranslatingContent()
                }

                is TranslationUiState.Success -> {
                    TranslatedTextCard(
                        translatedText = state.translatedText.text,
                        targetLanguage = targetLanguage,
                        onCopy = {
                            copyToClipboard(context, state.translatedText.text, "Translated text")
                            scope.launch {
                                snackbarHostState.showSnackbar("Translated text copied")
                            }
                        },
                        onShare = {
                            shareText(
                                context = context,
                                text = "Original (${sourceLanguage.displayName}):\n$sourceText\n\n" +
                                        "Translation (${targetLanguage.displayName}):\n${state.translatedText.text}"
                            )
                        }
                    )
                }

                is TranslationUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = viewModel::retryTranslation
                    )
                }

                else -> {
                    // Idle or Ready state
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLanguage.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Language.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TranslatedTextCard(
    translatedText: String,
    targetLanguage: Language,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    TalkLensCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Translation (${targetLanguage.displayName})",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy translation"
                        )
                    }

                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share translation"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = translatedText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun TranslatingContent() {
    TalkLensCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        TalkLensLoadingIndicator(
            message = "Translating...",
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    TalkLensCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Translation Failed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            TalkLensPrimaryButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Copy text to clipboard
 */
private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

/**
 * Share text via Android share sheet
 */
private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share translation"))
}
