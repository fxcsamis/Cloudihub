package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.example.ui.components.LocalSharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.scrollBy
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import coil.compose.rememberAsyncImagePainter
import com.example.ui.CloudihubViewModel
import com.example.ui.MusicTrack

// --- Music tab: playlist detail overlay ---
@Composable
fun PlaylistDetailOverlay(
    playlist: PlaylistData,
    onClose: () -> Unit,
    onTrackSelect: (MusicTrack) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: CloudihubViewModel
) {
    BackHandler(enabled = true) {
        onClose()
    }

    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), // Pure White Top
                        Color(0xFFF8FAFC), // Soft Off-White Cloud
                        Color(0xFFF1F5F9), // Light Cloud Smoke
                        Color(0xFFE2E8F0)  // Subtle Smoke Shade
                    )
                )
            )
    ) {
        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 0.dp, bottom = 120.dp)
        ) {
            // 1. EXTRA LARGE HERO COLLAGE BANNER (Seamessly Blended into Background with Animated White Smoke)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    // 2x2 Image Collage Banner (Edge-to-Edge)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.weight(1f)) {
                            Image(
                                painter = rememberAsyncImagePainter(playlist.images.getOrElse(0) { "" }),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            Image(
                                painter = rememberAsyncImagePainter(playlist.images.getOrElse(1) { "" }),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            Image(
                                painter = rememberAsyncImagePainter(playlist.images.getOrElse(2) { "" }),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            Image(
                                painter = rememberAsyncImagePainter(playlist.images.getOrElse(3) { "" }),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }

                    // SEAMLESS BACKGROUND BLEND GRADIENT OVERLAY
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.25f),
                                        Color.Transparent,
                                        Color(0xFFF8FAFC).copy(alpha = 0.70f),
                                        Color(0xFFF8FAFC)
                                    )
                                )
                            )
                    )

                    // LIVE ANIMATED WHITE SMOKE EFFECT FLOATING OVER BANNER
                    AnimatedWhiteSmokeOverlay(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Title, Creator & Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title & Creator
                    Text(
                        text = playlist.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playlist.creator,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = playlist.year,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Controls: Shuffle, Play, Download
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.90f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    Toast.makeText(context, "Shuffle mode enabled", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Play Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0F172A),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .height(52.dp)
                                .widthIn(min = 145.dp)
                                .clickable {
                                    if (playlist.tracks.isNotEmpty()) {
                                        onTrackSelect(playlist.tracks.first())
                                    }
                                }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.90f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    Toast.makeText(context, "Downloading playlist...", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Download",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Description and track count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playlist.description,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = playlist.tracksCount,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFCBD5E1).copy(alpha = 0.6f))
                }
            }

            // 2. TRACKS LIST (With Animated Equalizer Waves & Selection Feedback)
            items(playlist.tracks) { track ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.97f else 1.0f, label = "trackScale")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(interactionSource = interactionSource, indication = null) { onTrackSelect(track) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(track.albumArt),
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.artist,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { Toast.makeText(context, "Options for ${track.title}", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. TOP FLOATING GLASSY FROSTED NAVIGATION HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(20f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glassy Circular Back Button
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.80f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onClose() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Glassy Frosted Pill Container with Love, Search, 3-dot
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.80f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Love",
                            tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF0F172A),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    isLiked = !isLiked
                                    Toast.makeText(context, if (isLiked) "Added to Liked Playlists" else "Removed from Liked", Toast.LENGTH_SHORT).show()
                                }
                        )
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    onSearchClick()
                                }
                        )
                        var showPlaylistMenu by remember { mutableStateOf(false) }
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showPlaylistMenu = true }
                            )
                            DropdownMenu(
                                expanded = showPlaylistMenu,
                                onDismissRequest = { showPlaylistMenu = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Recently Played Music", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                        }
                                    },
                                    onClick = {
                                        showPlaylistMenu = false
                                        viewModel.isRecentMusicScreenOpen = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Shuffle Playlist", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                        }
                                    },
                                    onClick = {
                                        showPlaylistMenu = false
                                        Toast.makeText(context, "Shuffled playlist queue!", Toast.LENGTH_SHORT).show()
                                        playlist.tracks.shuffled().firstOrNull()?.let { onTrackSelect(it) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Equalizer & Sound FX", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                        }
                                    },
                                    onClick = {
                                        showPlaylistMenu = false
                                        Toast.makeText(context, "Equalizer mode: Sky Dynamic Boost active", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Clear Recent History", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFEF4444))
                                        }
                                    },
                                    onClick = {
                                        showPlaylistMenu = false
                                        viewModel.clearRecentMusicHistory()
                                        Toast.makeText(context, "Recent music history cleared", Toast.LENGTH_SHORT).show()
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

// FULL MUSIC SEARCH OVERLAY (Side-Slide Animation, Recent Searches, Live Query Filtering)
