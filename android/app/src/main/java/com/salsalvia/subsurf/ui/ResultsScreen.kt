package com.salsalvia.subsurf.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salsalvia.subsurf.AppViewModel
import com.salsalvia.subsurf.R
import com.salsalvia.subsurf.UiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(vm: AppViewModel, state: UiState) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.results_title)) })
        if (state.validation == null && state.catalog == null && state.terrain == null) {
            EmptyResults()
            return
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ValidationCard(state) }
            state.catalog?.let { catalog ->
                item { CatalogCard(catalog.totalMatched, catalog.products) }
                if (catalog.warnings.isNotEmpty()) item { WarningsCard(catalog.warnings) }
                if (catalog.provenance.isNotEmpty()) item { ProvenanceCard(catalog.provenance.map { it.provider }) }
            }
            item { TerrainCard(vm, state) }
            state.terrain?.let { terrain ->
                if (terrain.provenance.isNotEmpty()) {
                    item { ProvenanceCard(terrain.provenance.map { it.provider }) }
                }
                item { CaveatsCard(terrain.caveats) }
            }
            item { Spacer(Modifier.size(10.dp)) }
        }
    }
}

@Composable
private fun EmptyResults() {
    Box(
        Modifier.fillMaxSize().padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.results_empty_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.results_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    stringResource(R.string.disclaimer),
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ValidationCard(state: UiState) {
    val v = state.validation ?: return
    SectionCard(
        title = stringResource(R.string.validation_card),
        icon = Icons.Filled.CheckCircle,
        iconTint = if (v.valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    ) {
        StatusChip(
            text = stringResource(if (v.valid) R.string.valid_region else R.string.invalid_region),
            kind = if (v.valid) ChipKind.Success else ChipKind.Error,
        )
        if (!v.valid) {
            v.issues.forEach { issue ->
                BulletLine(issue, color = MaterialTheme.colorScheme.error)
            }
        }
        HorizontalDivider()
        MetricRow(stringResource(R.string.metric_vertices), v.vertexCount.toString())
        MetricRow(
            stringResource(R.string.metric_area),
            stringResource(R.string.area_hectares_format, fmt(v.areaHectares, 1)),
        )
        MetricRow(
            stringResource(R.string.metric_perimeter),
            stringResource(R.string.perimeter_format, fmt(v.perimeterM, 0)),
        )
    }
}

@Composable
private fun CatalogCard(totalMatched: Int, products: List<com.salsalvia.subsurf.data.ProductSummary>) {
    SectionCard(
        title = stringResource(R.string.catalog_card),
        icon = Icons.AutoMirrored.Filled.List,
    ) {
        StatusChip(
            text = stringResource(R.string.products_found, products.size) +
                " · " + stringResource(R.string.total_matched_format, totalMatched),
            kind = ChipKind.Neutral,
        )
        if (products.isEmpty()) {
            Text("—", style = MaterialTheme.typography.bodyMedium)
        }
        products.forEach { p ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        p.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        buildString {
                            append(p.datetime?.take(10) ?: "—")
                            p.cloudCoverPercent?.let {
                                append(" · ").append(stringResource(R.string.cloud_cover_format, fmt(it, 1)))
                            }
                            p.sizeMb?.let {
                                append(" · ").append(stringResource(R.string.size_format, fmt(it, 0)))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TerrainCard(vm: AppViewModel, state: UiState) {
    val t = state.terrain
    SectionCard(
        title = stringResource(R.string.terrain_card),
        icon = Icons.Filled.Place,
        iconTint = MaterialTheme.colorScheme.tertiary,
    ) {
        if (t == null) {
            Button(onClick = { vm.computeTerrain() }, enabled = !state.terrainBusy) {
                if (state.terrainBusy) {
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text(stringResource(if (state.terrainBusy) R.string.computing else R.string.compute_terrain))
            }
            return@Column
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    stringResource(
                        R.string.elevation_format,
                        fmt(t.elevation.minM, 0), fmt(t.elevation.maxM, 0),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    stringResource(R.string.slope_mean_format, fmt(t.slope.meanDeg, 1)) + " · " +
                        stringResource(R.string.slope_max_format, fmt(t.slope.maxDeg, 1)),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    stringResource(R.string.roughness_format, fmt(t.roughnessTriMean, 2)) + " · " +
                        stringResource(R.string.resolution_format, fmt(t.resolutionMPerPx, 1)),
                    style = MaterialTheme.typography.bodySmall,
                )
                t.dominantGradientAzimuthDeg?.let { az ->
                    Text(
                        stringResource(R.string.gradient_consistency_format, fmt(t.gradientConsistency ?: 0.0, 2)) +
                            " · " + com.salsalvia.subsurf.geo.GeoMath.compassLabel(az),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        t.surfaceAnomalyScore?.let { score ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.anomaly_score_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            fmt(score, 3),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    if (t.scoreComponents.isNotEmpty()) HorizontalDivider()
                    t.scoreComponents.forEach { (name, value) ->
                        Text(
                            "$name = ${fmt(value, 3)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningsCard(warnings: List<String>) {
    SectionCard(
        title = stringResource(R.string.warnings_title),
        icon = Icons.Filled.Warning,
        iconTint = MaterialTheme.colorScheme.tertiary,
    ) {
        warnings.forEach { BulletLine(it) }
    }
}

@Composable
private fun ProvenanceCard(providers: List<String>) {
    SectionCard(
        title = stringResource(R.string.provenance_title),
        icon = Icons.Filled.Info,
        iconTint = MaterialTheme.colorScheme.secondary,
    ) {
        providers.forEach { BulletLine(it) }
    }
}

@Composable
private fun CaveatsCard(caveats: List<String>) {
    SectionCard(
        title = stringResource(R.string.caveats_title),
        icon = Icons.Filled.Info,
        iconTint = MaterialTheme.colorScheme.outline,
    ) {
        caveats.forEach { BulletLine(it) }
    }
}

private fun fmt(v: Double, digits: Int): String =
    String.format(Locale.US, "%.${digits}f", v)
