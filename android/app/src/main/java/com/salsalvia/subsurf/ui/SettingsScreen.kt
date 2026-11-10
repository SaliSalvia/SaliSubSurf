package com.salsalvia.subsurf.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salsalvia.subsurf.AppViewModel
import com.salsalvia.subsurf.R
import com.salsalvia.subsurf.UiState

private fun validUrl(url: String): Boolean {
    val u = url.trim()
    return (u.startsWith("http://") || u.startsWith("https://")) && u.length > 8
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, state: UiState) {
    var url by remember(state.apiBaseUrl) { mutableStateOf(state.apiBaseUrl) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testOk by remember { mutableStateOf<Boolean?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.settings_title)) })

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(
                title = stringResource(R.string.settings_api_url),
                icon = Icons.Filled.Settings,
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        testOk = null
                        testResult = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.settings_api_url_hint)) },
                    singleLine = true,
                    isError = url.isNotBlank() && !validUrl(url),
                    trailingIcon = {
                        when {
                            url.isBlank() -> {}
                            validUrl(url) -> Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            else -> Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
                if (url.isNotBlank() && !validUrl(url)) {
                    Text(
                        stringResource(R.string.url_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Text(
                    stringResource(R.string.settings_current_url, state.apiBaseUrl.ifBlank { "—" }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            testResult = null
                            testOk = null
                            vm.saveBaseUrl(url.trim())
                        },
                        enabled = validUrl(url),
                    ) { Text(stringResource(R.string.settings_save)) }

                    OutlinedButton(onClick = {
                        testResult = null
                        testOk = null
                        vm.saveBaseUrl(url.trim())
                        vm.testConnection { ok, info ->
                            testOk = ok
                            testResult = info
                        }
                    }) { Text(stringResource(R.string.settings_test)) }
                }
                val ok = testOk
                if (ok != null) {
                    StatusChip(
                        text = if (ok) {
                            stringResource(R.string.settings_test_ok, testResult ?: "")
                        } else {
                            stringResource(R.string.settings_test_fail)
                        },
                        kind = if (ok) ChipKind.Success else ChipKind.Error,
                    )
                    if (!ok && !testResult.isNullOrBlank()) {
                        Text(
                            testResult ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            SectionCard(
                title = stringResource(R.string.settings_about_title),
                icon = Icons.Filled.Info,
                iconTint = MaterialTheme.colorScheme.secondary,
            ) {
                Text(
                    stringResource(R.string.settings_about),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            stringResource(R.string.disclaimer),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }
        }
    }
}
