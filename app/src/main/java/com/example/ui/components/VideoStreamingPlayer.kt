package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import java.io.File

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoStreamingPlayer(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val video = viewModel.playingVideo ?: return
    val streamUrl = viewModel.activeStreamingUrl
    val extractorMsg = viewModel.extractorModeMsg
    val isExtracting = viewModel.isExtracting

    var isPlayerExpanded by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackErrorMsg by remember { mutableStateOf<String?>(null) }

    // Custom Interactive States
    var isLiked by remember(video.id) { mutableStateOf(false) }
    var likeCount by remember(video.id) { mutableStateOf((800 + Math.random() * 500).toInt()) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    val comments = remember(video.id) {
        mutableStateListOf(
            "Wow! The Piped API-extraction system is incredibly fast.",
            "Visual resolution looks crisp and premium.",
            "Amazing cloudscapes. Perfect background for study sessions!",
            "Love the real-time stream extractor integration."
        )
    }
    var newCommentText by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Timer timeline calculation
    var progressSec by remember(video.id) { mutableStateOf(0) }
    val totalSeconds = remember(video) {
        val parts = video.duration.split(":")
        if (parts.size == 2) {
            val mins = parts[0].toIntOrNull() ?: 0
            val secs = parts[1].toIntOrNull() ?: 0
            mins * 60 + secs
        } else {
            300
        }
    }

    LaunchedEffect(isPlaying, streamUrl) {
        if (isPlaying && streamUrl.isNotEmpty()) {
            while (progressSec < totalSeconds) {
                delay(1000)
                progressSec++
            }
        }
    }

    val progressPercent = if (totalSeconds > 0) progressSec.toFloat() / totalSeconds else 0f

    // Draggable gesture to collapse/expand
    val draggableState = rememberDraggableState { delta ->
        if (delta > 20f && isPlayerExpanded) {
            isPlayerExpanded = false
        } else if (delta < -20f && !isPlayerExpanded) {
            isPlayerExpanded = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical
            )
    ) {
        if (!isPlayerExpanded) {
            // ==========================================
            // MODULE 5: SLIDING MINI-PLAYER VIEW (60dp)
            // ==========================================
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 86.dp) // Sits neatly above the bottom nav bar
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { isPlayerExpanded = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.96f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small thumbnail
                    Image(
                        painter = rememberAsyncImagePainter(video.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    // Text title / creator
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = video.creator,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Mini transport controls
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Mini Play/Pause",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.stopVideo() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Mini Close",
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // MODULE 4: EXPANDED FULL PLAYER SCREEN (REDESIGNED LIGHT THEME)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)) // Beautiful premium light background
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // --- TOP CHEVRON BAR ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlayerExpanded = false },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize Player",
                                tint = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NOW PLAYING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = video.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // --- CINEMATIC VIDEO VIEWPORT WITH ROUNDED CORNERS ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .aspectRatio(1.77f)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playbackErrorMsg != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Playback Error: $playbackErrorMsg",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        playbackErrorMsg = null
                                        viewModel.extractStreamAndPreparePlayer(video)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Retry Stream", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isExtracting || streamUrl.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFF0284C7))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = extractorMsg,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            ExoPlayerSurface(
                                streamUrl = streamUrl,
                                isPlaying = isPlaying,
                                onPlaybackError = { err -> playbackErrorMsg = err },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // --- VIDEO PROGRESS TIMELINE BAR ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Slider(
                            value = progressPercent,
                            onValueChange = { percent ->
                                progressSec = (percent * totalSeconds).toInt()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF0284C7),
                                activeTrackColor = Color(0xFF0284C7),
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(progressSec),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = video.duration,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // --- PLAYER CONTROLS PANEL ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { progressSec = (progressSec - 10).coerceAtLeast(0) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Massive central play button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7))
                                .clickable { isPlaying = !isPlaying },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play / Pause",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { progressSec = (progressSec + 10).coerceAtMost(totalSeconds) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Fast Forward 10s",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // --- SCROLLABLE INFORMATION, INTERACTIONS AND RELATED FEED ---
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title / Views metadata block
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = video.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = video.creator,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF94A3B8))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = video.views,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // Custom action feedback row (Like, Comment, Share, Download)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Like Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable {
                                            isLiked = !isLiked
                                            if (isLiked) likeCount++ else likeCount--
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color.Red else Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = likeCount.toString(),
                                        fontSize = 11.sp,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Share Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "Watch ${video.title} on Cloudihub Video Streamer!")
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Share via"))
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Share",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Save Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable {
                                            viewModel.triggerVideoDownload(video)
                                            Toast.makeText(context, "Cloud downloading started!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Save",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // --- NEW DIRECT INLINE DISCUSSION BOARD / COMMENT BOX (As requested!) ---
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Comment,
                                            contentDescription = "Comments",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Comments & Discussions (${comments.size})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Display list of comments directly inline (last 3 comments or all, styled beautifully)
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        comments.take(4).forEach { comment ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFF8FAFC))
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                // Small avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFBAE6FD)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "U",
                                                        color = Color(0xFF0284C7),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "@sky_gazer",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF64748B)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = comment,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Write Comment inline text box
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BasicTextField(
                                            value = newCommentText,
                                            onValueChange = { newCommentText = it },
                                            textStyle = TextStyle(
                                                color = Color(0xFF0F172A),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    contentAlignment = Alignment.CenterStart,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    if (newCommentText.isEmpty()) {
                                                        Text(
                                                            text = "Add to the discussion...",
                                                            color = Color(0xFF94A3B8),
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )

                                        IconButton(
                                            onClick = {
                                                if (newCommentText.trim().isNotEmpty()) {
                                                    comments.add(0, newCommentText.trim())
                                                    newCommentText = ""
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Post Comment",
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Related Feed Heading
                        item {
                            Text(
                                text = "Recommended Sights",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Related Videos Stream List
                        if (viewModel.relatedVideos.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No related nodes found",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            items(viewModel.relatedVideos, key = { it.id }) { relVideo ->
                                RelatedVideoCard(
                                    video = relVideo,
                                    onClick = {
                                        viewModel.playVideo(relVideo)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RelatedVideoCard(
    video: CloudVideo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(video.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 100.dp, height = 60.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = video.creator,
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = video.duration,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ExoPlayerSurface(
    streamUrl: String,
    isPlaying: Boolean,
    onPlaybackError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Remote direct MP4/WebM/signed CDN URLs render more reliably in Android WebView's
    // native HTML5 video pipeline on some devices where SurfaceView stays black
    // inside clipped Compose cards. Local files, HLS, and DASH still use Media3.
    if (shouldUseWebVideoSurface(streamUrl)) {
        WebVideoSurface(
            streamUrl = streamUrl,
            isPlaying = isPlaying,
            modifier = modifier
        )
        return
    }

    val exoPlayer = remember(streamUrl) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(30_000)
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to "Mozilla/5.0 (Android) CloudeHub/1.0 Media3",
                    "Accept" to "*/*",
                    "Connection" to "keep-alive"
                )
            )

        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val uri = resolvePlayableUri(streamUrl)
                val item = MediaItem.Builder()
                    .setUri(uri)
                    .setMimeType(inferMimeType(streamUrl))
                    .build()
                setMediaItem(item)
                prepare()
                playWhenReady = isPlaying
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        onPlaybackError(error.localizedMessage ?: error.message ?: "Playback source error")
                    }
                })
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        update = { it.player = exoPlayer },
        modifier = modifier
    )
}


@Composable
private fun WebVideoSurface(
    streamUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val html = remember(streamUrl, isPlaying) {
        val safeUrl = streamUrl
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
        val autoplay = if (isPlaying) "autoplay" else ""
        """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            html, body { margin:0; padding:0; width:100%; height:100%; background:#000; overflow:hidden; }
            video { width:100%; height:100%; background:#000; object-fit:contain; }
          </style>
        </head>
        <body>
          <video id="v" src="$safeUrl" controls playsinline webkit-playsinline $autoplay></video>
          <script>
            const v = document.getElementById('v');
            v.setAttribute('crossorigin', 'anonymous');
            ${if (isPlaying) "v.play().catch(()=>{});" else "v.pause();"}
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.BLACK)
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                loadDataWithBaseURL(streamUrl, html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(streamUrl, html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

private fun shouldUseWebVideoSurface(input: String): Boolean {
    val value = input.trim()
    val lower = value.substringBefore('?').lowercase()
    if (isLocalMediaPath(value)) return false
    if (lower.endsWith(".m3u8") || lower.endsWith(".mpd")) return false
    return value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)
}

private fun resolvePlayableUri(input: String): Uri {
    val value = input.trim()
    return when {
        value.startsWith("content://", ignoreCase = true) -> Uri.parse(value)
        value.startsWith("file://", ignoreCase = true) -> Uri.parse(value)
        value.startsWith("/storage/") || value.startsWith("/sdcard/") || value.startsWith("/") -> Uri.fromFile(File(value))
        value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true) -> Uri.parse(value)
        else -> Uri.fromFile(File(value))
    }
}

private fun isLocalMediaPath(input: String): Boolean {
    val value = input.trim()
    return value.startsWith("content://", ignoreCase = true) ||
        value.startsWith("file://", ignoreCase = true) ||
        value.startsWith("/storage/") ||
        value.startsWith("/sdcard/") ||
        !value.startsWith("http://", ignoreCase = true) && !value.startsWith("https://", ignoreCase = true)
}

private fun inferMimeType(input: String): String? {
    val clean = input.substringBefore('?').lowercase()
    return when {
        clean.endsWith(".m3u8") -> MimeTypes.APPLICATION_M3U8
        clean.endsWith(".mpd") -> MimeTypes.APPLICATION_MPD
        clean.endsWith(".mp4") || clean.endsWith(".m4v") -> MimeTypes.VIDEO_MP4
        clean.endsWith(".webm") -> MimeTypes.VIDEO_WEBM
        clean.endsWith(".mp3") -> MimeTypes.AUDIO_MPEG
        clean.endsWith(".m4a") -> MimeTypes.AUDIO_MP4
        else -> null
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
