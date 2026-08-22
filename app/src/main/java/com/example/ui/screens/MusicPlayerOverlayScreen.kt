package com.example.ui.screens
import com.example.ui.components.rememberIosStyleFlingBehavior

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
import coil.compose.AsyncImage
import com.example.ui.CloudihubViewModel
import com.example.ui.MusicTrack

// --- Music tab: full-screen player overlay + smoke/equalizer effects ---
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullMusicPlayerOverlay(
    track: MusicTrack,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onSelectTrack: (MusicTrack) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    androidx.activity.compose.BackHandler(enabled = true) {
        onClose()
    }
    val context = LocalContext.current
    var activeTrack by remember(track) { mutableStateOf(track) }
    var isLoved by remember { mutableStateOf(false) }
    var isArtistFollowed by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0.12f) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showArtistInfoDialog by remember { mutableStateOf(false) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var playerDragOffsetY by remember { mutableFloatStateOf(0f) }

    val lazyListState = rememberLazyListState()
    val isScrolledDown by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 100
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.isAppearanceLightStatusBars = true
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    val popularSongs = remember(activeTrack) {
        listOf(
            MusicTrack("p_1", "Toota Jo Kabhi Tara", "Sachin-Jigar, Sumedha Karmahe & Atif Aslam", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", "5:05", "Romantic", "150M"),
            MusicTrack("p_2", "Ruposh (Original Score)", "Wajhi Farooki & Atif Aslam", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "4:20", "Romantic", "211M"),
            MusicTrack("p_3", "Jeena Haraam", "Vishal Mishra & Atif Aslam", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400", "3:30", "Chill", "210M"),
            MusicTrack("p_4", "Aaina Mon Bhanga", "Zubeen Garg, Jeet Gannguli & Atif Aslam", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400", "4:45", "Melody", "95M"),
            MusicTrack("p_5", "Jiboner Ayna", "Parvez & Atif Aslam", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", "4:12", "Romantic", "115M"),
            MusicTrack("p_6", "Jhol", "Maanu, Annural Khalid & Atif Aslam", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", "3:10", "Relax", "808M")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = playerDragOffsetY
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        // Full screen White Smoke Background Effect
        AnimatedWhiteSmokeOverlay(
            modifier = Modifier.fillMaxSize()
        )

        // STICKY TOP MINI MUSIC BAR (Appears smoothly when scrolled down)
        AnimatedVisibility(
            visible = isScrolledDown,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(20f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color.White.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(0)
                                }
                            }
                    ) {
                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Collapse",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        AsyncImage(
                            model = activeTrack.albumArt,
                            contentDescription = activeTrack.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeTrack.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activeTrack.artist,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(
                            onClick = { Toast.makeText(context, "Casting music", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // MAIN SCROLLABLE CONTENT (PLAYER VIEW + ARTIST PROFILE & HITS)
        LazyColumn(
            flingBehavior = rememberIosStyleFlingBehavior(),
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ITEM 0: MAIN FULL MUSIC PLAYER VIEW (Fills 100% height so item 1 is hidden below fold)
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // TOP HEADER (Collapse & Cast - only when at top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (playerDragOffsetY > 120f) {
                                            onClose()
                                        }
                                        playerDragOffsetY = 0f
                                    },
                                    onDragCancel = { playerDragOffsetY = 0f },
                                    onVerticalDrag = { change, dragAmount ->
                                        if (dragAmount > 0 || playerDragOffsetY > 0) {
                                            playerDragOffsetY = (playerDragOffsetY + dragAmount).coerceAtLeast(0f)
                                            change.consume()
                                        }
                                    }
                                )
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Collapse",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Playing from Playlist",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )

                        IconButton(
                            onClick = { Toast.makeText(context, "Casting music", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // ALBUM ARTWORK BANNER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(vertical = 8.dp)
                            .aspectRatio(1f)
                            .shadow(14.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        AsyncImage(
                            model = activeTrack.albumArt,
                            contentDescription = activeTrack.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.08f),
                                            Color(0xFFF1F5F9).copy(alpha = 0.40f)
                                        )
                                    )
                                )
                        )
                    }

                    // TITLE & ARTIST DETAILS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeTrack.title,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeTrack.artist,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // PROGRESS BAR & TIMESTAMPS
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF0F172A),
                                activeTrackColor = Color(0xFF0F172A),
                                inactiveTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0:42",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = activeTrack.duration,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // PLAYBACK CONTROLS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0F172A),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(62.dp)
                                .clickable { onTogglePlay() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // BOTTOM UTILITY TOOLBAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showArtistInfoDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = { showLyricsSheet = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Lyrics",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = { isLoved = !isLoved },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLoved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Love",
                                    tint = if (isLoved) Color(0xFFEF4444) else Color(0xFF475569),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    sleepTimerMinutes = if (sleepTimerMinutes == 0) 15 else 0
                                    val msg = if (sleepTimerMinutes > 0) "Timer: 15m" else "Timer Off"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Sleep Timer",
                                    tint = if (sleepTimerMinutes > 0) Color(0xFF0284C7) else Color(0xFF475569),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Queue",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // ITEM 1: ARTIST PROFILE SECTION
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Artist Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                AsyncImage(
                                    model = activeTrack.albumArt,
                                    contentDescription = activeTrack.artist,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFF0284C7), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0284C7))
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VERIFIED ARTIST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeTrack.artist,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "18.5M Monthly Listeners",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = {
                                    isArtistFollowed = !isArtistFollowed
                                    val msg = if (isArtistFollowed) "Following ${activeTrack.artist}" else "Unfollowed ${activeTrack.artist}"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isArtistFollowed) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                                    contentColor = if (isArtistFollowed) Color(0xFF0F172A) else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (isArtistFollowed) "Following" else "+ Follow",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Artist Bio Snippet
                        Text(
                            text = "About the Artist",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeTrack.artist} is one of South Asia's most iconic playback singers & music composers, best known for soulful acoustic performances, chart-topping romantic anthems, and over 20 billion streams worldwide.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ITEM 2: POPULAR SONGS BY THIS ARTIST
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Songs by Artist",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "See All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Showing full discography", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    popularSongs.forEachIndexed { index, popTrack ->
                        val isThisActive = activeTrack.title == popTrack.title
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isThisActive) Color(0xFFF0F9FF) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isThisActive) Color(0xFFBAE6FD) else Color(0xFFF1F5F9)
                            ),
                            shadowElevation = if (isThisActive) 4.dp else 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    activeTrack = popTrack
                                    onSelectTrack(popTrack)
                                    if (!isPlaying) onTogglePlay()
                                    Toast.makeText(context, "Playing ${popTrack.title}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = popTrack.albumArt,
                                    contentDescription = popTrack.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = popTrack.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isThisActive) Color(0xFF0284C7) else Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${popTrack.views} plays • ${popTrack.duration}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (isThisActive) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isThisActive && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = if (isThisActive) Color.White else Color(0xFF0F172A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ITEM 3: FEATURED ALBUMS ROW (Edge-To-Edge horizontal scroll)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                ) {
                    Text(
                        text = "Featured Albums & Singles",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        flingBehavior = rememberIosStyleFlingBehavior(),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(sampleBanglaPlaylist.tracks.take(4), key = { it.hashCode() }) { albumTrack ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable {
                                        activeTrack = albumTrack
                                        onSelectTrack(albumTrack)
                                        if (!isPlaying) onTogglePlay()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    AsyncImage(
                                        model = albumTrack.albumArt,
                                        contentDescription = albumTrack.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = albumTrack.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = albumTrack.category,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM SPACING FOR NAV PADDING
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        // ARTIST DETAILS POPUP DIALOG
        if (showArtistInfoDialog) {
            AlertDialog(
                onDismissRequest = { showArtistInfoDialog = false },
                title = {
                    Text(
                        text = track.artist,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "${track.title} • Track Details",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0284C7)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Monthly Listeners: 3.2M",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${track.artist} is a renowned playback singer and music producer. Known for chart-topping romantic melodies, acoustic live performances, and viral music worldwide.",
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showArtistInfoDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // DYNAMIC LYRICS BOTTOM SHEET POPUP OVERLAY
        AnimatedVisibility(
            visible = showLyricsSheet,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350, easing = FastOutLinearInEasing)) + fadeOut(animationSpec = tween(250)),
            modifier = Modifier.zIndex(50f)
        ) {
            DynamicLyricsSheet(
                track = track,
                onClose = { showLyricsSheet = false }
            )
        }
    }
}

// ANIMATED WHITE SMOKE OVERLAY FOR HERO BANNER
@Composable
fun AnimatedWhiteSmokeOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WhiteSmokeAnimation")

    val smokeOffset1 by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smokeOffset1"
    )

    val smokeOffset2 by infiniteTransition.animateFloat(
        initialValue = 25f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(8500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smokeOffset2"
    )

    val smokeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smokeAlpha"
    )

    val smokeScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smokeScale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Floating Smoke Cloud Layer 1
        Box(
            modifier = Modifier
                .fillMaxWidth(1.2f)
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .offset(x = smokeOffset1.dp, y = (smokeOffset2 / 2).dp)
                .graphicsLayer {
                    scaleX = smokeScale
                    scaleY = smokeScale
                    alpha = smokeAlpha
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.90f),
                            Color(0xFFF1F5F9).copy(alpha = 0.60f),
                            Color.Transparent
                        )
                    )
                )
                .blur(48.dp)
        )

        // Floating Smoke Cloud Layer 2
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(160.dp)
                .align(Alignment.Center)
                .offset(x = (-smokeOffset2 * 1.5f).dp, y = smokeOffset1.dp)
                .graphicsLayer {
                    scaleX = 1.1f / smokeScale
                    alpha = smokeAlpha * 0.8f
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE2E8F0).copy(alpha = 0.50f),
                            Color.Transparent
                        )
                    )
                )
                .blur(38.dp)
        )

        // Floating Smoke Particle Layer 3
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .offset(x = smokeOffset1.dp, y = smokeOffset2.dp)
                .graphicsLayer {
                    scaleX = smokeScale
                    alpha = smokeAlpha * 0.5f
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )
    }
}

// ANIMATED EQUALIZER WAVEFORM INDICATOR
@Composable
fun AnimatedEqualizerWave(
    tint: Color = Color(0xFF0284C7),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Equalizer")
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(580, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = modifier.size(width = 16.dp, height = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(bar1Height)
                .background(tint, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(bar2Height)
                .background(tint, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(bar3Height)
                .background(tint, RoundedCornerShape(1.dp))
        )
    }
}

// PLAYLIST DETAIL OVERLAY (White Smoke Theme, Seamless Blend Large Banner, Animated Smoke & Frosted Glassy Controls)
