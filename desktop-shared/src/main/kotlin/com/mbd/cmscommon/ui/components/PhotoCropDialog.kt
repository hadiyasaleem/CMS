package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.ModTrack
import kotlin.math.max
import kotlin.math.roundToInt

private const val CROP_VIEWPORT_DP = 240
private const val CROP_OUTPUT_PX = 480

/**
 * A minimal circular crop tool: drag to reposition, slider to zoom, then "Use photo" bakes the
 * currently-framed region into a fixed-size square [ImageBitmap] via [Canvas.drawImageRect] --
 * a Compose UI API implemented identically on Android and Desktop, so this whole dialog (and the
 * crop math) is platform-agnostic. Only encoding the result to upload bytes is platform-specific.
 */
@Composable
fun PhotoCropDialog(source: ImageBitmap, onCancel: () -> Unit, onCropped: (ImageBitmap) -> Unit) {
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val viewportPx = with(density) { CROP_VIEWPORT_DP.dp.toPx() }
    val coverScale = max(viewportPx / source.width, viewportPx / source.height)
    val displayWidthPx = source.width * coverScale * zoom
    val displayHeightPx = source.height * coverScale * zoom

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Crop photo") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(CROP_VIEWPORT_DP.dp)
                        .clip(CircleShape)
                        .background(ModTrack)
                        .border(1.dp, ModTrack, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                offset += drag
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = source,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = with(density) {
                            Modifier
                                .width(displayWidthPx.toDp())
                                .height(displayHeightPx.toDp())
                                .graphicsLayer(translationX = offset.x, translationY = offset.y)
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Drag to reposition, use the slider to zoom.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..3f)
            }
        },
        confirmButton = {
            TextButton(onClick = { onCropped(cropToSquare(source, zoom, offset, viewportPx, CROP_OUTPUT_PX)) }) {
                Text("Use photo")
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}

private fun cropToSquare(source: ImageBitmap, zoom: Float, offsetPx: Offset, viewportPx: Float, outputPx: Int): ImageBitmap {
    val coverScale = max(viewportPx / source.width, viewportPx / source.height)
    val effectiveScale = coverScale * zoom
    val visibleSourcePx = viewportPx / effectiveScale

    val centerX = source.width / 2f - offsetPx.x / effectiveScale
    val centerY = source.height / 2f - offsetPx.y / effectiveScale
    val half = visibleSourcePx / 2f

    val cropSize = visibleSourcePx.roundToInt().coerceAtLeast(1).coerceAtMost(max(1, minOf(source.width, source.height)))
    val srcX = (centerX - half).roundToInt().coerceIn(0, (source.width - cropSize).coerceAtLeast(0))
    val srcY = (centerY - half).roundToInt().coerceIn(0, (source.height - cropSize).coerceAtLeast(0))

    val target = ImageBitmap(outputPx, outputPx)
    Canvas(target).drawImageRect(
        image = source,
        srcOffset = IntOffset(srcX, srcY),
        srcSize = IntSize(cropSize, cropSize),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(outputPx, outputPx),
        paint = Paint(),
    )
    return target
}
