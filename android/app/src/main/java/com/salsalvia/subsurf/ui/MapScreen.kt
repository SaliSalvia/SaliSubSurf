package com.salsalvia.subsurf.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salsalvia.subsurf.AppViewModel
import com.salsalvia.subsurf.R
import com.salsalvia.subsurf.UiState
import com.salsalvia.subsurf.map.RegionMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(vm: AppViewModel, state: UiState, onAnalyzed: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.map_title)) })

        Box(Modifier.weight(1f).fillMaxWidth()) {
            RegionMapView(
                vertices = state.vertices,
                onMapTap = { lat, lon -> vm.addVertex(lat, lon) },
                modifier = Modifier.fillMaxSize(),
            )

            ElevatedCard(Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp).size(18.dp),
                        )
                    }
                    Column {
                        Text(
                            stringResource(R.string.vertices_format, state.vertices.size),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (state.vertices.size >= 3) {
                            Text(
                                stringResource(R.string.area_format, vm.formatArea(state.vertices)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                stringResource(R.string.tap_to_add_vertex),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = state.busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Surface(tonalElevation = 3.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { vm.undo() },
                    enabled = state.vertices.isNotEmpty(),
                ) { Text(stringResource(R.string.action_undo)) }
                OutlinedButton(
                    onClick = { vm.clear() },
                    enabled = state.vertices.isNotEmpty(),
                ) { Text(stringResource(R.string.action_clear)) }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { vm.analyze() },
                    enabled = !state.busy && state.vertices.size >= 3,
                ) {
                    if (state.busy) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.size(8.dp))
                    }
                    Text(
                        stringResource(if (state.busy) R.string.analyzing else R.string.action_analyze),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }

    // Jump to the Results tab as soon as the server validation arrives.
    LaunchedEffect(state.validation) {
        if (state.validation != null && !state.busy) onAnalyzed()
    }
}
