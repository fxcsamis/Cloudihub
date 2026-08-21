package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import com.example.ui.components.LocalSharedTransitionScope
import com.example.ui.components.rememberIosStyleFlingBehavior
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

// --- Music tab: root screen (this is the file MainActivity/other screens reference) ---
data class SpeedDialItem(
    val id: String,
    val title: String,
    val imageUrl: String
)

data class PlaylistData(
    val id: String,
    val title: String,
    val creator: String,
    val year: String = "Playlist • 2026",
    val description: String = "No description",
    val tracksCount: String = "38 tracks",
    val images: List<String>,
    val tracks: List<MusicTrack>
)

val sampleBanglaPlaylist = PlaylistData(
    id = "p1",
    title = "bangla best",
    creator = "Bibhas Debnath",
    year = "Playlist • 2026",
    description = "No description",
    tracksCount = "38 tracks",
    images = listOf(
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
        "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400",
        "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400",
        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400"
    ),
    tracks = listOf(
        MusicTrack("t1", "Jiboner Ayna", "Parvez", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "4:12", "Romantic", "15M"),
        MusicTrack("t2", "Bolbona Go Ar Kono Din - 21", "Baul Sukumar", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", "5:04", "Folk", "28M"),
        MusicTrack("t3", "Aaina Mon Bhanga", "Zubeen Garg, Jeet Gannguli, & Priyo Chattopadhyay", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400", "4:45", "Melody", "42M"),
        MusicTrack("t4", "Chokhe Shanti Lage Amar", "Sathi Khan", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", "3:58", "Pop", "18M"),
        MusicTrack("t5", "Jeena Haraam", "Vishal Mishra", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400", "3:30", "Chill", "35M"),
        MusicTrack("t6", "Ruposh (Original Score)", "Wajhi Farooki", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", "4:20", "Romantic", "211M"),
        MusicTrack("t7", "Toota Jo Kabhi Tara", "Sachin-Jigar, Sumedha Karmahe & Atif Aslam", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400", "5:05", "Romantic", "150M"),
        MusicTrack("t8", "Anyayo", "Aneesh & Krtin Kay", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400", "3:15", "Chill", "2.6M"),
        MusicTrack("t9", "Kusu Kusu", "Zahrah S Khan & Dev Negi", "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=400", "3:45", "Party", "907M"),
        MusicTrack("t10", "Jhol", "Maanu & Annural Khalid", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "3:10", "Relax", "808M")
    )
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MusicScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme
    val context = LocalContext.current

    // Playlist overlay & search screen state
    var selectedPlaylist by remember { mutableStateOf<PlaylistData?>(null) }
    var isSearchScreenOpen by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isPlaylistOverlayOpen) {
        if (!viewModel.isPlaylistOverlayOpen) {
            selectedPlaylist = null
        }
    }

    BackHandler(enabled = isSearchScreenOpen || selectedPlaylist != null || viewModel.isFullMusicPlayerOpen || viewModel.isRecentMusicScreenOpen) {
        if (viewModel.isRecentMusicScreenOpen) {
            viewModel.isRecentMusicScreenOpen = false
        } else if (isSearchScreenOpen) {
            isSearchScreenOpen = false
        } else if (selectedPlaylist != null) {
            selectedPlaylist = null
            viewModel.isPlaylistOverlayOpen = false
        } else if (viewModel.isFullMusicPlayerOpen) {
            viewModel.isFullMusicPlayerOpen = false
        }
    }

    // Player States
    var currentTrack by remember {
        mutableStateOf(
            MusicTrack(
                id = "0",
                title = "Toota Jo Kabhi Tara",
                artist = "Sachin-Jigar, Sumedha Karmahe & Atif Aslam",
                albumArt = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=800",
                duration = "5:05",
                category = "Romantic",
                views = "150M"
            )
        )
    }
    var isPlaying by remember { mutableStateOf(false) }
    val isFullPlayerOpen = viewModel.isFullMusicPlayerOpen

    LaunchedEffect(selectedPlaylist) {
        viewModel.isPlaylistOverlayOpen = (selectedPlaylist != null)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.isFullMusicPlayerOpen = false
            viewModel.isPlaylistOverlayOpen = false
        }
    }
    var currentCategory by remember { mutableStateOf("All") }
    var musicSearchQuery by remember { mutableStateOf("") }

    // Marquee runner state for category filter chips in top bar
    val topCategoryLazyRowState = rememberLazyListState()
    val musicListState = rememberLazyListState()
    LaunchedEffect(topCategoryLazyRowState) {
        while (true) {
            when {
                topCategoryLazyRowState.isScrollInProgress -> {
                    while (topCategoryLazyRowState.isScrollInProgress) {
                        kotlinx.coroutines.delay(100)
                    }
                    kotlinx.coroutines.delay(2500)
                }
                // Main track list scrolling elsewhere - skip this step only, resume
                // immediately next tick once it settles (no artificial cooldown).
                musicListState.isScrollInProgress -> {
                    kotlinx.coroutines.delay(32)
                }
                else -> {
                    topCategoryLazyRowState.scrollBy(2.4f)
                    kotlinx.coroutines.delay(32)
                }
            }
        }
    }

    // Scroll state (declared above, alongside topCategoryLazyRowState, so the
    // auto-slide loop can reference it)

    // --- TOP BAR ENTRANCE & SCROLL ANIMATION STATE ---
    var isTopBarMounted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isTopBarMounted = true
    }

    val density = LocalDensity.current
    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }
    val maxUpOffsetPx = with(density) { (-10.dp).toPx() }

    val topBarNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = topBarOffsetHeightPx + delta
                topBarOffsetHeightPx = newOffset.coerceIn(maxUpOffsetPx, 0f)
                return Offset.Zero
            }
        }
    }

    val animatedEntranceY by animateFloatAsState(
        targetValue = if (isTopBarMounted) 0f else -100f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "musicTopBarEntranceY"
    )
    val animatedEntranceAlpha by animateFloatAsState(
        targetValue = if (isTopBarMounted) 1f else 0f,
        animationSpec = tween(400),
        label = "musicTopBarEntranceAlpha"
    )

    // Categories filter (matching screenshot)
    val categories = remember {
        listOf("All", "Relax", "Sleep", "Energize", "Sad", "Romance", "Chill", "Focus", "Workout", "Party")
    }

    // Speed Dial Items (3x3 grid matching screenshot)
    val speedDialItems = remember {
        listOf(
            SpeedDialItem("sd1", "Sunn Raha Hai", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400"),
            SpeedDialItem("sd2", "Bulleya (From \"Ae Dil...", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400"),
            SpeedDialItem("sd3", "Fakira 🎶 Tu puchh na...", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400"),
            SpeedDialItem("sd4", "Barbaad Reprise - Fe...", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400"),
            SpeedDialItem("sd5", "Finding Her", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400"),
            SpeedDialItem("sd6", "Bagdhara - Proticcho...", "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=400"),
            SpeedDialItem("sd7", "SHOJONI", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400"),
            SpeedDialItem("sd8", "Ishq de Fanniyar - Fe...", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400"),
            SpeedDialItem("sd9", "Arijit Special Mix", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400")
        )
    }

    // Quick Picks Tracks List
    val quickPicks = remember {
        listOf(
            MusicTrack("qp1", "Ruposh (Original Score)", "Wajhi Farooki • 211M plays", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", "4:20", "Romantic", "211M"),
            MusicTrack("qp2", "Anyayo", "Aneesh & Krtin Kay • 2.6M plays", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "3:15", "Chill", "2.6M"),
            MusicTrack("qp3", "Kusu Kusu", "Zahrah S Khan & Dev Negi • 907M plays", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", "3:45", "Party", "907M"),
            MusicTrack("qp4", "Jhol", "Maanu & Annural Khalid • 808M plays", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400", "3:10", "Relax", "808M"),
            MusicTrack("qp5", "no signal", "juggsi & kyra • 12M plays", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400", "2:50", "Chill", "12M"),
            MusicTrack("qp6", "Toota Jo Kabhi Tara", "Sachin-Jigar, Sumedha Karmahe & Atif Aslam • 150M plays", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", "5:05", "Romantic", "150M")
        )
    }

    // Long Listens Tracks
    val longListens = remember {
        listOf(
            MusicTrack("ll1", "1 Hour of Night Hindi Lofi Songs To Chill & Relax...", "viral vicky vlogs • 1:06:40", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", "1:06:40", "Relax", "500K"),
            MusicTrack("ll2", "Bairan | Jukebox | Amtee | Banjaare | Viral Har...", "AMTEE • 48:02", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "48:02", "Focus", "300K"),
            MusicTrack("ll3", "Best of Arijit Singh Mashup 2025 | O Sajni Re...", "Aftermorning • 35:30", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", "35:30", "Romantic", "1M")
        )
    }

    // Heard in Shorts Tracks
    val shortsTracks = remember {
        listOf(
            MusicTrack("hs1", "Traag (feat. Jozo & Kraantje Pappie)", "Bizzey • 652M plays", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400", "3:02", "Party", "652M"),
            MusicTrack("hs2", "Phagooner Mohonaye", "Various Artist • 78M plays", "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=400", "3:40", "Relax", "78M")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE), // Soft Cloud Sky Light
                        Color(0xFFF1F5F9), // Light Cloud Slate
                        Color(0xFFF8FAFC), // Pure Soft Cloud
                        Color(0xFFFFFFFF)  // Clean White
                    )
                )
            )
            .nestedScroll(topBarNestedScrollConnection)
    ) {
        // AMBIENT CLOUD THEME BLUR GLOWS
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-50).dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color(0xFFBAE6FD).copy(alpha = 0.50f))
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 160.dp)
                .clip(CircleShape)
                .background(Color(0xFFDDD6FE).copy(alpha = 0.45f))
                .blur(50.dp)
        )

        // 1. DYNAMIC CURVED GLASSY TOP BAR HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
                .graphicsLayer {
                    translationY = animatedEntranceY + topBarOffsetHeightPx
                    alpha = animatedEntranceAlpha
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                color = Color.White.copy(alpha = 0.70f),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.80f),
                                    Color.White.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.35f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(bottom = 12.dp)
                    ) {
                    // Title and action icons row with inline Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SimpMusic",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Good Morning",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Glassy Search Bar
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .padding(start = 10.dp, end = 6.dp)
                                .clickable { isSearchScreenOpen = true },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (musicSearchQuery.isEmpty()) "Search music..." else musicSearchQuery,
                                    color = if (musicSearchQuery.isEmpty()) Color(0xFF64748B) else Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = "Open Search",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        // Glassy Action Buttons (Notifications & History)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(34.dp)
                            ) {
                                IconButton(
                                    onClick = { Toast.makeText(context, "Notifications", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Listening History Button
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(34.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.isRecentMusicScreenOpen = true },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Recent Watched Music",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Continuous Auto-Running Marquee Filter Chips Row (Pauses on touch, resumes automatically)
                    LazyRow(
                        state = topCategoryLazyRowState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(count = Int.MAX_VALUE) { index ->
                            val category = categories[index % categories.size]
                            val isSelected = currentCategory == category
                            Surface(
                                modifier = Modifier.clickable { currentCategory = category },
                                shape = CircleShape,
                                color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.70f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF0284C7) else Color.White.copy(alpha = 0.90f)
                                ),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = category,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // MAIN SCROLLABLE YOUTUBE MUSIC CONTENT
        LazyColumn(
            state = musicListState,
            flingBehavior = rememberIosStyleFlingBehavior(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 160.dp, bottom = 140.dp)
        ) {
            // 4. QUICK PICKS SECTION (Horizontal Swipeable List)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick picks",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.clickable { }
                        ) {
                            Text(
                                text = "Play all",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Swipeable horizontal column pages for quick picks
                    val quickPickPages = quickPicks.chunked(4)
                    val pagerState = rememberPagerState(pageCount = { quickPickPages.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 16.dp
                    ) { pageIndex ->
                        val pageTracks = quickPickPages[pageIndex]
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pageTracks.forEach { track ->
                                val trackInteractionSource = remember { MutableInteractionSource() }
                                val trackIsPressed by trackInteractionSource.collectIsPressedAsState()
                                val trackScale by animateFloatAsState(if (trackIsPressed) 0.96f else 1.0f, label = "quickPickScale")
                                val isThisTrackPlaying = currentTrack?.id == track.id && isPlaying

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = trackScale
                                            scaleY = trackScale
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.88f),
                                    shadowElevation = 3.dp,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(interactionSource = trackInteractionSource, indication = null) {
                                                currentTrack = track
                                                isPlaying = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box {
                                                AsyncImage(
                                                    model = track.albumArt,
                                                    contentDescription = track.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                )
                                                if (isThisTrackPlaying) {
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        AnimatedEqualizerWave(tint = Color.White)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = track.title,
                                                        fontSize = 15.sp,
                                                        fontWeight = if (isThisTrackPlaying) FontWeight.Bold else FontWeight.SemiBold,
                                                        color = if (isThisTrackPlaying) Color(0xFF0284C7) else Color(0xFF0F172A),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    if (isThisTrackPlaying) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        AnimatedEqualizerWave(tint = Color(0xFF0284C7))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = track.artist,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 5. FROM THE COMMUNITY (2x2 Collage Playlist Cards)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "From the community",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            CommunityCollageCard(
                                title = "All Time Favourites",
                                subtitle = "Arunava Choudhury • 221K views",
                                images = listOf(
                                    "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=300",
                                    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300",
                                    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300",
                                    "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300"
                                ),
                                onClick = {
                                    selectedPlaylist = PlaylistData(
                                        id = "comm1",
                                        title = "All Time Favourites",
                                        creator = "Arunava Choudhury",
                                        year = "Playlist • 2026",
                                        description = "No description",
                                        tracksCount = "38 tracks",
                                        images = listOf(
                                            "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=300",
                                            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300",
                                            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300",
                                            "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300"
                                        ),
                                        tracks = sampleBanglaPlaylist.tracks
                                    )
                                }
                            )
                        }
                        item {
                            CommunityCollageCard(
                                title = "peace",
                                subtitle = "AYUSH PAUL • 1.1M views",
                                images = listOf(
                                    "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=300",
                                    "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300",
                                    "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300",
                                    "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300"
                                ),
                                onClick = {
                                    selectedPlaylist = PlaylistData(
                                        id = "comm2",
                                        title = "peace",
                                        creator = "AYUSH PAUL",
                                        year = "Playlist • 2026",
                                        description = "No description",
                                        tracksCount = "38 tracks",
                                        images = listOf(
                                            "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=300",
                                            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300",
                                            "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=300",
                                            "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300"
                                        ),
                                        tracks = sampleBanglaPlaylist.tracks
                                    )
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 6. FORGOTTEN FAVORITES (Hero Banner Card)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Forgotten favorites",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                currentTrack = MusicTrack(
                                    id = "ff1",
                                    title = "Die with a smile x awari song (Full Mashup)",
                                    artist = "Likey • 328K views",
                                    albumArt = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                                    duration = "4:30",
                                    category = "Mashup",
                                    views = "328K"
                                )
                                isPlaying = true
                            }
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                            contentDescription = "Forgotten Favorite",
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
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Die with a smile x awari song (Full Mashup)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Likey • 328K views",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 7. FRESH FINDS, OLD FAVORITES (Mix Cards)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fresh finds, old favorites",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            MixCard(
                                title = "Replay Mix",
                                subtitle = "Madhur Sharma, Tarun Sharma...",
                                imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400"
                            )
                        }
                        item {
                            MixCard(
                                title = "Archive Mix",
                                subtitle = "Sachet Parampara, Roop Kuma...",
                                imageUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 8. LONG LISTENS SECTION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Long listens",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        longListens.forEach { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        currentTrack = track
                                        isPlaying = true
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AsyncImage(
                                        model = track.albumArt,
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = track.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = track.artist,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 9. HEARD IN SHORTS SECTION
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Heard in Shorts",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.clickable { }
                        ) {
                            Text(
                                text = "Play all",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        shortsTracks.forEach { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        currentTrack = track
                                        isPlaying = true
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AsyncImage(
                                        model = track.albumArt,
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = track.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = track.artist,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // STICKY BOTTOM MINI PLAYER (Glassy White Cloud Style)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 60.dp, start = 8.dp, end = 8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clickable { viewModel.isFullMusicPlayerOpen = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = currentTrack.albumArt,
                            contentDescription = currentTrack.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentTrack.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentTrack.artist,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { isPlaying = !isPlaying }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FULL SCREEN PLAYER OVERLAY (Rich expanded player)
        AnimatedVisibility(
            visible = isFullPlayerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.zIndex(90f)
        ) {
            FullMusicPlayerOverlay(
                track = currentTrack,
                isPlaying = isPlaying,
                onClose = { viewModel.isFullMusicPlayerOpen = false },
                onTogglePlay = { isPlaying = !isPlaying },
                animatedVisibilityScope = this@AnimatedVisibility
            )
        }

        // PLAYLIST DETAIL OVERLAY (Matching Screenshot 2)
        AnimatedVisibility(
            visible = selectedPlaylist != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.zIndex(100f)
        ) {
            selectedPlaylist?.let { playlist ->
                PlaylistDetailOverlay(
                    playlist = playlist,
                    onClose = {
                        selectedPlaylist = null
                        viewModel.isPlaylistOverlayOpen = false
                    },
                    onTrackSelect = { track ->
                        currentTrack = track
                        isPlaying = true
                        viewModel.recordMusicTrackPlayed(track)
                    },
                    onSearchClick = {
                        isSearchScreenOpen = true
                    },
                    viewModel = viewModel
                )
            }
        }

        // FULL SEARCH SCREEN OVERLAY WITH SIDE SLIDE ANIMATION
        AnimatedVisibility(
            visible = isSearchScreenOpen,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 380, easing = EaseOutQuart)
            ) + fadeIn(animationSpec = tween(380)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 320, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(320)),
            modifier = Modifier.fillMaxSize().zIndex(150f)
        ) {
            MusicSearchScreenOverlay(
                onClose = { isSearchScreenOpen = false },
                onTrackSelect = { track ->
                    currentTrack = track
                    isPlaying = true
                    viewModel.recordMusicTrackPlayed(track)
                    isSearchScreenOpen = false
                },
                viewModel = viewModel
            )
        }

        // RECENT MUSIC HISTORY SCREEN OVERLAY
        AnimatedVisibility(
            visible = viewModel.isRecentMusicScreenOpen,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 380, easing = EaseOutQuart)
            ) + fadeIn(animationSpec = tween(380)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 320, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(320)),
            modifier = Modifier.fillMaxSize().zIndex(160f)
        ) {
            RecentMusicHistoryScreenOverlay(
                onClose = { viewModel.isRecentMusicScreenOpen = false },
                onTrackSelect = { track: MusicTrack ->
                    currentTrack = track
                    isPlaying = true
                    viewModel.recordMusicTrackPlayed(track)
                    viewModel.isRecentMusicScreenOpen = false
                },
                viewModel = viewModel
            )
        }
    }
}

// COMMUNITY COLLAGE CARD COMPONENT
