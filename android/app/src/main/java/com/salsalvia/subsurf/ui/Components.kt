package com.salsalvia.subsurf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salsalvia.subsurf.ui.theme.MetricTextStyle

/**
 * Shared visual language for all screens. Every results/section card uses the
 * same header (tonal icon chip + title) so the app reads as one coherent tool.
 */

/** Card with a tonal icon chip + title header, used for every section. */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

/** Label/value row with tabular figures — the workhorse of the results screen. */
@Composable
fun MetricRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MetricTextStyle.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            color = valueColor,
        )
    }
}

enum class ChipKind { Success, Error, Warning, Neutral }

/** Compact status pill with a leading dot (success / error / warning / neutral). */
@Composable
fun StatusChip(text: String, kind: ChipKind) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (kind) {
        ChipKind.Success -> scheme.primaryContainer to scheme.onPrimaryContainer
        ChipKind.Error -> scheme.errorContainer to scheme.onErrorContainer
        ChipKind.Warning -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        ChipKind.Neutral -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
    Surface(shape = CircleShape, color = bg) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(fg))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
            )
        }
    }
}

/** Bullet line for warnings, caveats and provenance entries. */
@Composable
fun BulletLine(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", style = MaterialTheme.typography.bodyMedium, color = color)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
