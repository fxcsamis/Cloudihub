package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import com.example.ui.components.CloudShape
import com.example.ui.components.CloudSkyBackground
import com.example.ui.components.NavigationTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - 300f, translateAnim - 300f),
            end = Offset(translateAnim + 300f, translateAnim + 300f)
        )
    )
}

@Composable
fun ShimmerVideoCloudCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp, 28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .shimmer()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val videos = viewModel.videos
    val searchQuery = viewModel.searchQuery
    val activeDownloads by viewModel.downloads.collectAsState()
    val activeCount = activeDownloads.count { it.status == com.example.ui.DownloadStatus.DOWNLOADING || it.status == com.example.ui.DownloadStatus.QUEUED }

    // Floating categories
    val categories = listOf("All", "Rainclouds", "Infrastructure", "Sky Timelapse", "Edge Gaming", "Aesthetics")
    var selectedCategory by remember { mutableStateOf("All") }

    val lazyListState = rememberLazyListState()
    val isDark = viewModel.isDarkTheme
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchScreenOpen by remember { mutableStateOf(false) }
    var recentSearches by remember {
        mutableStateOf(listOf("Rainclouds", "Storm tracker", "Space timelapse", "Sky view", "Thunderstorm", "Rainbow"))
    }

    // --- NESTED SCROLL & COLLAPSIBLE ANIMATION STATE ---
    val density = LocalDensity.current
    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }
    var topBarHeightPx by remember { mutableStateOf(with(density) { 140.dp.toPx() }) }

    val isEmptyResults = videos.isEmpty() && !viewModel.isLoadingVideos

    LaunchedEffect(isEmptyResults) {
        if (isEmptyResults) {
            topBarOffsetHeightPx = 0f
        }
    }

    val nestedScrollConnection = remember(topBarHeightPx, isEmptyResults) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isEmptyResults) {
                    topBarOffsetHeightPx = 0f
                    return Offset.Zero
                }
                // Instantly update the top bar offset based on scroll gestures
                val delta = available.y
                val newOffset = topBarOffsetHeightPx + delta
                topBarOffsetHeightPx = newOffset.coerceIn(-topBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    val topPaddingDp = with(density) { topBarHeightPx.toDp() + 12.dp }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .nestedScroll(nestedScrollConnection)
    ) {
        if (isEmptyResults) {
            CloudSkyBackground(modifier = Modifier.fillMaxSize())
        }
        // --- VIDEO FEED / SHIMMER FEED WITH PULL TO REFRESH ---
        PullToRefreshBox(
            isRefreshing = viewModel.isLoadingVideos,
            onRefresh = { viewModel.loadHybridFeed() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(top = topPaddingDp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Content Feed Items: Loading, Empty, or Videos
                if (viewModel.isLoadingVideos) {
                    items(3) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ShimmerVideoCloudCard()
                        }
                    }
                } else if (videos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CloudShape())
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Empty Search",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No sky matches found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Try searching for Cloud, Rain or Space",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    items(videos, key = { it.id }) { video ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            VideoCloudCard(
                                video = video,
                                onDownloadClick = { viewModel.triggerVideoDownload(video) },
                                onPlayClick = { viewModel.playVideo(video) }
                            )
                        }
                    }
                }
            }
        }

        // --- ANIMATED FLOATING IMMERSIVE TOP BAR CONTAINER (Floats on top of feed) ---
        var startEntranceAnimation by remember { mutableStateOf(false) }
        val entranceProgress by animateFloatAsState(
            targetValue = if (startEntranceAnimation) 1f else 0f,
            animationSpec = tween(durationMillis = 1200, easing = EaseOutQuart),
            label = "TopBarEntrance"
        )
        LaunchedEffect(Unit) {
            startEntranceAnimation = true
        }

        val fraction = if (topBarHeightPx > 0f) -topBarOffsetHeightPx / topBarHeightPx else 0f
        val translationY = with(density) { topBarOffsetHeightPx.toDp() }
        val animatedOffsetY = translationY - (24 * (1f - entranceProgress)).dp
        
        // Mathematical fade and blur to guarantee zero lingering artifacts
        val baseAlpha = if (fraction >= 0.90f) {
            val progress = (fraction - 0.90f) / 0.10f
            (1f - progress).coerceIn(0f, 1f)
        } else {
            1f
        }
        val alpha = baseAlpha * entranceProgress

        // Start blurring gradually only after 40% has scrolled up/entered
        val blurRadius = if (fraction >= 0.40f) {
            val progress = (fraction - 0.40f) / 0.60f
            (progress * 12f).dp
        } else {
            0.dp
        }
        
        // Smoothly fade out the top bar's background color so it hides completely as it blurs and scrolls out
        val bgAlpha = if (fraction >= 0.40f) {
            val progress = (fraction - 0.40f) / 0.60f
            (1f - progress).coerceIn(0f, 1f)
        } else {
            1f
        }

        val topBarBgColor = if (isEmptyResults) {
            Color.Transparent
        } else if (isDark) {
            Color(0xFF0F172A).copy(alpha = bgAlpha * entranceProgress)
        } else {
            Color(0xFFF8FAFC).copy(alpha = bgAlpha * entranceProgress)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = animatedOffsetY)
                .clipToBounds() // Strictly clip within layout bounds to prevent any blur bleeding downwards
                .background(topBarBgColor)
                .statusBarsPadding() // Seamless immersive status bar matching
                .onSizeChanged { size ->
                    topBarHeightPx = size.height.toFloat()
                }
                .graphicsLayer(alpha = alpha)
                .let { modifier ->
                    if (blurRadius > 0.dp) modifier.blur(blurRadius) else modifier
                }
                .padding(bottom = 8.dp)
        ) {
            // 1. Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cloud-shaped Search Bar Container
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF1E293B) else Color.White)
                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                        .clickable { isSearchScreenOpen = true }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text("Search cloud files...", color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 14.sp)
                        } else {
                            Text(searchQuery, color = if (isDark) Color.White else Color(0xFF0F172A), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Reload / Refresh Feed Button
                IconButton(
                    onClick = { viewModel.loadHybridFeed() },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("reload_feed_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Feed",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Cute Download Icon with Active Badge
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.showDownloadHub = true },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("download_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloads",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (activeCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Voice Speech-to-Text Button
                IconButton(
                    onClick = { viewModel.startVoiceSearch() },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("voice_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice search",
                        tint = Color(0xFF0369A1),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Profile Logo Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBAE6FD))
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { viewModel.selectTab(NavigationTab.Profile) }
                        .testTag("profile_avatar_logo"),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = if (viewModel.isGoogleSignedIn && viewModel.signedInUserPhoto.isNotEmpty()) {
                        viewModel.signedInUserPhoto
                    } else {
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"
                    }
                    Image(
                        painter = rememberAsyncImagePainter(avatarUrl),
                        contentDescription = "Profile Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 2. Quick Category List
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { it }) { category ->
                    val isSelected = selectedCategory == category
                    val background = if (isSelected) Color(0xFF0284C7) else Color.White
                    val border = if (isSelected) Color.Transparent else Color(0xFFE2E8F0)
                    val textCol = if (isSelected) Color.White else Color(0xFF64748B)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(background)
                            .border(1.dp, border, RoundedCornerShape(16.dp))
                            .clickable { 
                                selectedCategory = category
                                if (category == "All") {
                                    viewModel.updateSearchQuery("")
                                } else {
                                    viewModel.updateSearchQuery(category)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textCol
                        )
                    }
                }
            }
        }

        // --- ANIMATED SEARCH SCREEN OVERLAY WITH SIDE SLIDE ANIMATION ---
        AnimatedVisibility(
            visible = isSearchScreenOpen,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 400, easing = EaseOutQuart)
            ) + fadeIn(animationSpec = tween(400)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 350, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(350)),
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            ) {
                CloudSkyBackground(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Top Search Row in Search Screen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isSearchScreenOpen = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }

                        // Search Input
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color.White)
                                .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                textStyle = TextStyle(
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (searchQuery.isNotEmpty()) {
                                            viewModel.triggerDoneSearchKeyboardAction(searchQuery)
                                            isSearchScreenOpen = false
                                            keyboardController?.hide()
                                        }
                                    }
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(if (isDark) Color.White else Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search sky and clouds...",
                                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Voice Button
                        IconButton(
                            onClick = { viewModel.startVoiceSearch() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice search",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Download Button
                        IconButton(
                            onClick = { viewModel.showDownloadHub = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Downloads",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Reload Button
                        IconButton(
                            onClick = { viewModel.loadHybridFeed() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Feed",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Profile Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFBAE6FD))
                                .border(1.dp, Color.White, CircleShape)
                                .clickable {
                                    isSearchScreenOpen = false
                                    viewModel.selectTab(NavigationTab.Profile)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarUrl = if (viewModel.isGoogleSignedIn && viewModel.signedInUserPhoto.isNotEmpty()) {
                                viewModel.signedInUserPhoto
                            } else {
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"
                            }
                            Image(
                                painter = rememberAsyncImagePainter(avatarUrl),
                                contentDescription = "Profile Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // Recent Searches Section
                        if (recentSearches.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                TextButton(
                                    onClick = { recentSearches = emptyList() }
                                ) {
                                    Text("Clear All", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                recentSearches.forEach { searchItem ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0).copy(alpha = 0.6f))
                                            .clickable {
                                                viewModel.updateSearchQuery(searchItem)
                                                isSearchScreenOpen = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History",
                                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = searchItem,
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                recentSearches = recentSearches.filter { it != searchItem }
                                            },
                                            modifier = Modifier.size(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Trending Searches Section
                        Text(
                            text = "Trending Searches",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val trendingSearches = listOf("Beautiful Aurora", "Nimbus Clouds", "Cosmic Stardust", "Lightning Strike", "Solar Eclipse")
                        trendingSearches.forEachIndexed { idx, trend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.5f))
                                    .clickable {
                                        if (!recentSearches.contains(trend)) {
                                            recentSearches = listOf(trend) + recentSearches
                                        }
                                        viewModel.updateSearchQuery(trend)
                                        isSearchScreenOpen = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (idx) {
                                                0 -> Color(0xFFEF4444)
                                                1 -> Color(0xFFF97316)
                                                2 -> Color(0xFFF59E0B)
                                                else -> if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (idx + 1).toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = trend,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Trending",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
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
fun VideoCloudCard(
    video: CloudVideo,
    onDownloadClick: () -> Unit,
    onPlayClick: (CloudVideo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            // Clicking the card area triggers direct video playback
            .clickable { onPlayClick(video) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // Beautiful Cloud-Shaped Thumbnail Background Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable { onPlayClick(video) },
                contentAlignment = Alignment.Center
            ) {
                // Background Cloud Shape decorative layer with a shadow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .shadow(elevation = 2.dp, shape = CloudShape(), clip = false)
                        .background(Color.White.copy(alpha = 0.92f), CloudShape())
                )

                // Actual video thumbnail image sitting inside, covering full width and height
                Image(
                    painter = rememberAsyncImagePainter(video.imageUrl),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )

                // Translucent Overlay glass shadow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.12f))
                )

                // Play Button floating in center
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Duration badge at the bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Video information and actions (Padded neatly at the bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = video.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = video.creator,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
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
                            text = video.views,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Cloud styled Download Button
                Button(
                    onClick = onDownloadClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F9FF),
                        contentColor = Color(0xFF0284C7)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .border(1.dp, Color(0xFFE0F2FE), RoundedCornerShape(16.dp))
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Video",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${video.sizeMb.toInt()}MB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
