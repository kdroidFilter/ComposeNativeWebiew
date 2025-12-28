package io.github.kdroidfilter.composewebview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    val webViewState = rememberWebViewState("https://google.com")
    var urlText by remember { mutableStateOf("https://google.com") }

    LaunchedEffect(webViewState.currentUrl) {
        webViewState.currentUrl?.let { urlText = it }
    }

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de navigation
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Barre d'URL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Boutons de navigation
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                NavIconButton(
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    onClick = { webViewState.goBack() },
                                    enabled = webViewState.canGoBack
                                )
                                NavIconButton(
                                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Avancer",
                                    onClick = { webViewState.goForward() },
                                    enabled = webViewState.canGoForward
                                )
                                NavIconButton(
                                    icon = Icons.Default.Refresh,
                                    contentDescription = "Recharger",
                                    onClick = { webViewState.reload() }
                                )
                                NavIconButton(
                                    icon = Icons.Default.Home,
                                    contentDescription = "Accueil",
                                    onClick = { webViewState.loadUrl("https://google.com") }
                                )
                            }

                            // Champ URL
                            OutlinedTextField(
                                value = urlText,
                                onValueChange = { urlText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .onKeyEvent { event ->
                                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                                            val url = if (!urlText.startsWith("http")) {
                                                "https://$urlText"
                                            } else urlText
                                            webViewState.loadUrl(url)
                                            true
                                        } else false
                                    },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                placeholder = { Text("Entrez une URL...") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        val url = if (!urlText.startsWith("http")) {
                                            "https://$urlText"
                                        } else urlText
                                        webViewState.loadUrl(url)
                                    }
                                )
                            )

                            // Bouton Go
                            Button(
                                onClick = {
                                    val url = if (!urlText.startsWith("http")) {
                                        "https://$urlText"
                                    } else urlText
                                    webViewState.loadUrl(url)
                                },
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Go")
                            }
                        }

                        // Indicateur de chargement
                        AnimatedVisibility(
                            visible = webViewState.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // WebView
                WryWebView(
                    state = webViewState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}