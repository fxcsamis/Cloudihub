package com.example.debug

import android.view.Choreographer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * Always-on-screen performance debug widget. A small floating badge shows the
 * live FPS at a glance; tapping it expands a panel with frame time, jank
 * count, memory usage, and the live/persisted error log from [DebugLog].
 *
 * This measures real device frame timing via [Choreographer] - the same
 * mechanism Android itself uses to schedule rendering - so the numbers here
 * reflect what's actually happening on screen, not a guess from reading code.
 * Frames slower than ~16.7ms (below 60fps) count as "jank"; the counter reset
 * button lets you zero it right before reproducing a specific stutter so the
 * count you see corresponds only to that action.
 */
@Composable
fun PerformanceDebugOverlay() {
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var fps by remember { mutableIntStateOf(0) }
    var frameTimeMs by remember { mutableStateOf(0f) }
    var jankCount by remember { mutableIntStateOf(0) }
    var usedMemoryMb by remember { mutableStateOf(0f) }
    var maxMemoryMb by remember { mutableStateOf(0f) }

    var badgeOffsetX by remember { mutableStateOf(0f) }
    var badgeOffsetY by remember { mutableStateOf(120f) }

    DisposableEffect(Unit) {
        DebugLog.loadPersistedCrash(context)

        var lastFrameTimeNanos = 0L
        var framesInWindow = 0
        var windowStartNanos = 0L

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTimeNanos != 0L) {
                    val deltaMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000f
                    frameTimeMs = deltaMs
                    // 16.7ms is the 60fps budget - anything slower than that
                    // is a dropped/janky frame the user can actually perceive.
                    if (deltaMs > 16.7f) jankCount++
                }
                lastFrameTimeNanos = frameTimeNanos

                framesInWindow++
                if (windowStartNanos == 0L) windowStartNanos = frameTimeNanos
                val elapsedMs = (frameTimeNanos - windowStartNanos) / 1_000_000
                if (elapsedMs >= 1000) {
                    fps = framesInWindow
                    framesInWindow = 0
                    windowStartNanos = frameTimeNanos

                    val runtime = Runtime.getRuntime()
                    usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
                    maxMemoryMb = runtime.maxMemory() / (1024f * 1024f)
                }

                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)

        onDispose {
            Choreographer.getInstance().removeFrameCallback(callback)
        }
    }

    val fpsColor = when {
        fps >= 55 -> Color(0xFF22C55E)
        fps >= 30 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(9999f)
    ) {
        // Small draggable badge - always visible, tap to expand full panel.
        Box(
            modifier = Modifier
                .padding(start = badgeOffsetX.dp, top = badgeOffsetY.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xE6111827))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        badgeOffsetX = (badgeOffsetX + dragAmount.x / 3f).coerceIn(0f, 260f)
                        badgeOffsetY = (badgeOffsetY + dragAmount.y / 3f).coerceIn(40f, 700f)
                    }
                }
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$fps",
                    color = fpsColor,
                    fontSize = 16.sp
                )
                Text(
                    text = "fps",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
        }
    }

    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            val logEntries by DebugLog.entries.collectAsState()
            val lastCrash by DebugLog.lastCrash.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .padding(16.dp)
            ) {
                Text("Performance Debug", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                DebugStatRow("FPS", "$fps", fpsColor)
                DebugStatRow(
                    "Frame time",
                    "${(frameTimeMs * 10).roundToInt() / 10f} ms",
                    if (frameTimeMs > 16.7f) Color(0xFFEF4444) else Color(0xFF22C55E)
                )
                DebugStatRow(
                    "Janky frames",
                    "$jankCount",
                    if (jankCount > 0) Color(0xFFF59E0B) else Color(0xFF22C55E)
                )
                DebugStatRow(
                    "Memory",
                    "${usedMemoryMb.roundToInt()} / ${maxMemoryMb.roundToInt()} MB",
                    if (maxMemoryMb > 0 && usedMemoryMb / maxMemoryMb > 0.8f) Color(0xFFEF4444) else Color(0xFF22C55E)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reset jank counter",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { jankCount = 0 }
                    )
                    Text(
                        text = "Close",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { expanded = false }
                    )
                }

                if (lastCrash != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Last crash", color = Color(0xFFEF4444), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastCrash ?: "",
                        color = Color(0xFFFCA5A5),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Text(
                        text = "Dismiss",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { DebugLog.clearPersistedCrash(context) }
                            .padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Live log", color = Color(0xFF94A3B8), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (logEntries.isEmpty()) {
                        item {
                            Text(
                                "No log entries yet.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        items(logEntries.asReversed()) { entry ->
                            Text(
                                text = entry,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp)
    }
}
