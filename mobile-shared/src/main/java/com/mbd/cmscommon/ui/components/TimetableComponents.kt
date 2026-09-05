package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GridCell(
    val title: String,
    val subtitle: String,
    val meta: String,
    val isBreak: Boolean = false,
    val isAlert: Boolean = false,
)

data class GridRow(
    val key: String,
    val label: String,
    val sublabel: String = "",
    val cells: Map<String, GridCell?>,
)

private val DEFAULT_LABEL_W = 96.dp
private val DEFAULT_SLOT_W = 120.dp

@Composable
fun TimetableGrid(
    timeSlots: List<String>,
    rows: List<GridRow>,
    modifier: Modifier = Modifier,
    identityHeader: String = "DAY",
    labelWidth: Dp = DEFAULT_LABEL_W,
    slotWidth: Dp = DEFAULT_SLOT_W,
    onCellClick: ((String, String) -> Unit)? = null,
) {
    val hScroll = rememberScrollState()

    CmsCard(modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.horizontalScroll(hScroll).background(CmsTheme.colors.ink).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(labelWidth)) {
                    Text(identityHeader, color = CmsTheme.colors.onInk, style = CmsTextStyles.eyebrow)
                }
                timeSlots.forEach { slot ->
                    Box(Modifier.width(slotWidth)) {
                        Text(slot, color = CmsTheme.colors.onInk, style = CmsTextStyles.eyebrow)
                    }
                }
            }
            HorizontalDivider(thickness = 2.dp, color = CmsTheme.colors.rule)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.horizontalScroll(hScroll),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.width(labelWidth)) {
                        Text(
                            row.label,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        if (row.sublabel.isNotBlank()) {
                            Text(row.sublabel, color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    timeSlots.forEach { slot ->
                        GridCellBox(
                            cell = row.cells[slot],
                            width = slotWidth,
                            onClick = onCellClick?.let { { it(row.key, slot) } },
                        )
                    }
                }
                HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.35f))
            }
        }
    }
}

@Composable
fun GridCellBox(cell: GridCell?, width: Dp, onClick: (() -> Unit)? = null) {
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = Modifier.width(width).heightIn(min = 60.dp).padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        when {
            cell != null && cell.isBreak -> {
                Box(
                    Modifier.fillMaxWidth().heightIn(min = 44.dp).then(clickMod).background(CmsTheme.colors.track),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("BREAK", color = CmsTheme.colors.muted, style = MaterialTheme.typography.labelSmall)
                }
            }
            cell != null -> {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(clickMod)
                        .then(if (cell.isAlert) Modifier.background(CmsTheme.colors.redTint) else Modifier)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    Text(
                        cell.title,
                        color = if (cell.isAlert) CmsTheme.colors.accent else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        cell.subtitle,
                        color = CmsTheme.colors.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (cell.meta.isNotEmpty()) {
                        Text(
                            cell.meta,
                            color = CmsTheme.colors.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            onClick != null -> {
                Box(
                    Modifier.fillMaxWidth().heightIn(min = 44.dp).then(clickMod)
                        .border(1.dp, CmsTheme.colors.rule.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = CmsTheme.colors.faint, style = MaterialTheme.typography.titleMedium)
                }
            }
            else -> {
                Box(Modifier.fillMaxWidth().heightIn(min = 44.dp))
            }
        }
    }
}

@Composable
fun CmsChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) CmsTheme.colors.ink else MaterialTheme.colorScheme.surfaceContainerLowest
    val fg = if (selected) CmsTheme.colors.onInk else MaterialTheme.colorScheme.onSurface
    val border = if (selected) null else BorderStroke(2.dp, CmsTheme.colors.rule)

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RectangleShape,
        color = bg,
        contentColor = fg,
        border = border,
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
    }
}

/** A compact "chip that opens a dropdown menu" filter, e.g. for a horizontally-scrollable SHOW row. */
@Composable
fun DropdownChip(
    selectedLabel: String?,
    emptyLabel: String,
    options: List<CmsEntityOption>,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = selectedLabel != null

    Box(modifier) {
        Surface(
            modifier = Modifier.clickable(enabled = enabled) { expanded = true },
            shape = RectangleShape,
            color = if (selected) CmsTheme.colors.ink else MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = if (selected) CmsTheme.colors.onInk else MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
            border = if (selected) null else BorderStroke(2.dp, CmsTheme.colors.rule),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    selectedLabel ?: emptyLabel,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 240.dp)) {
            DropdownMenuItem(text = { Text(emptyLabel) }, onClick = { onSelected(null); expanded = false })
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.label) }, onClick = { onSelected(option.id); expanded = false })
            }
        }
    }
}

@Composable
fun CapacityBar(count: Int, max: Int, modifier: Modifier = Modifier) {
    val fraction = (count.toFloat() / max).coerceIn(0f, 1f)
    val color = when {
        fraction >= 1f -> CmsTheme.colors.accent
        fraction >= 0.8f -> CmsTheme.colors.warn
        else -> CmsTheme.colors.ink
    }

    Column(modifier.fillMaxWidth()) {
        Row {
            Text("ENROLLMENT", modifier = Modifier.weight(1f), color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
            Text("$count / $max seats", color = color, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).background(CmsTheme.colors.track)) {
            Box(Modifier.fillMaxWidth(fraction).height(6.dp).background(color))
        }
    }
}
