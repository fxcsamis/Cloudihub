package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.CloudMusicTrack
import com.example.ui.CloudihubViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.lazy.rememberLazyListState

// Beautiful static list of repeat-use high-quality imagery
private object MusicImages {
    const val COVER_LOFI = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500"
    const val COVER_ACOUSTIC = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
    const val COVER_ROCK = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=500"
    const val COVER_CONCERT = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500"
    const val COVER_ISLAMIC = "https://images.unsplash.com/photo-1564507592333-c60657eea523?w=500"
    const val COVER_ROMANTIC = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=500"

    const val ARTIST_ARIJIT = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=200"
    const val ARTIST_ATIF = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200"
    const val ARTIST_SHREYA = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200"
    const val ARTIST_JAMES = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200"
}

// Custom data classes for our rich screen items
private data class Singer(
    val name: String,
    val avatarUrl: String,
    val songs: List<CloudMusicTrack>
)

private data class BannerSlide(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val associateTrack: CloudMusicTrack
)

@Composable
fun MusicScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    // Current state from viewModel
    val currentTrack = viewModel.currentTrack
    val isPlaying = viewModel.isPlaying
    val progressSec = viewModel.currentTrackProgressSec
    val totalSec = currentTrack.durationSec
    val isDark = viewModel.isDarkTheme

    // --- NESTED SCROLL & COLLAPSIBLE ANIMATION STATE ---
    val density = LocalDensity.current
    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }
    var topBarHeightPx by remember { mutableStateOf(with(density) { 80.dp.toPx() }) }

    val nestedScrollConnection = remember(topBarHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = topBarOffsetHeightPx + delta
                topBarOffsetHeightPx = newOffset.coerceIn(-topBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    val topPaddingDp = with(density) { topBarHeightPx.toDp() + 12.dp }

    // Setup our data lists (Repeatable top tier images used beautifully!)
    val bannerSlides1 = remember {
        listOf(
            BannerSlide(
                title = "Acoustic Sunset Beats",
                subtitle = "Feel the breezy sunset melodies",
                imageUrl = MusicImages.COVER_CONCERT,
                associateTrack = CloudMusicTrack("b1", "Sunset Breeze", "Coastal Beats", "3:40", 220, MusicImages.COVER_CONCERT, "")
            ),
            BannerSlide(
                title = "Shojoni Retro Mix",
                subtitle = "Shibu & Tajwar hit 1M plays",
                imageUrl = MusicImages.COVER_ROMANTIC,
                associateTrack = CloudMusicTrack("b2", "Shojoni", "Shibu & Tajwar", "4:12", 252, MusicImages.COVER_ROMANTIC, "")
            ),
            BannerSlide(
                title = "Lofi Clouds & Rain",
                subtitle = "Relax, study or dream offline",
                imageUrl = MusicImages.COVER_LOFI,
                associateTrack = CloudMusicTrack("b3", "Nimbus Dream", "Ambient Sky", "4:45", 285, MusicImages.COVER_LOFI, "")
            )
        )
    }

    val bannerSlides2 = remember {
        listOf(
            BannerSlide(
                title = "Spiritual Peace Volume 1",
                subtitle = "Authentic Islamic Gazals",
                imageUrl = MusicImages.COVER_ISLAMIC,
                associateTrack = CloudMusicTrack("i1", "Hasbi Rabbi Jalallah", "Vocal Only", "4:15", 255, MusicImages.COVER_ISLAMIC, "")
            ),
            BannerSlide(
                title = "Monsoon Acoustic Moods",
                subtitle = "Cozy songs for rainy skies",
                imageUrl = MusicImages.COVER_ACOUSTIC,
                associateTrack = CloudMusicTrack("b4", "Rainy Horizon", "Acoustic Skies", "3:10", 190, MusicImages.COVER_ACOUSTIC, "")
            )
        )
    }

    val mixedPlaylists = remember {
        listOf(
            "Mixed for you" to listOf(
                CloudMusicTrack("p1", "My Mix 01", "JalRaj, Gajendra Verma", "3:45", 225, MusicImages.COVER_LOFI, ""),
                CloudMusicTrack("p2", "My Mix 03", "Sohan Ali, Atif", "4:10", 250, MusicImages.COVER_ACOUSTIC, ""),
                CloudMusicTrack("p3", "Chill Hits Sky", "Lofi Sky Beats", "3:12", 192, MusicImages.COVER_CONCERT, "")
            ),
            "Your Daily Discover" to listOf(
                CloudMusicTrack("p4", "Zara Zara (2020)", "JalRaj", "3:35", 215, MusicImages.COVER_ROMANTIC, ""),
                CloudMusicTrack("p5", "Maula Mere Maula", "Roop Kumar Rathod", "4:20", 260, MusicImages.COVER_CONCERT, ""),
                CloudMusicTrack("p6", "Chhor Denge", "Parampara Tandon", "3:30", 200, MusicImages.COVER_ACOUSTIC, "")
            ),
            "Bollywood & Indian Hits" to listOf(
                CloudMusicTrack("p7", "Haye Andar Andar Se Toota", "Vhan Muzic", "4:50", 290, MusicImages.COVER_ROCK, ""),
                CloudMusicTrack("p8", "Tum Hi Ho", "Arijit Singh", "4:22", 262, MusicImages.COVER_ROMANTIC, ""),
                CloudMusicTrack("p9", "Anbe", "James", "4:30", 270, MusicImages.COVER_CONCERT, "")
            )
        )
    }

    val singers = remember {
        listOf(
            Singer(
                name = "Arijit Singh",
                avatarUrl = MusicImages.ARTIST_ARIJIT,
                songs = listOf(
                    CloudMusicTrack("s1_1", "Tera Fitoor", "Arijit Singh", "4:05", 245, MusicImages.COVER_ROMANTIC, ""),
                    CloudMusicTrack("s1_2", "Tum Hi Ho", "Arijit Singh", "4:22", 262, MusicImages.COVER_LOFI, ""),
                    CloudMusicTrack("s1_3", "Kesariya", "Arijit Singh", "3:55", 235, MusicImages.COVER_ACOUSTIC, "")
                )
            ),
            Singer(
                name = "Atif Aslam",
                avatarUrl = MusicImages.ARTIST_ATIF,
                songs = listOf(
                    CloudMusicTrack("s2_1", "Dil Diyan Gallan", "Atif Aslam", "4:20", 260, MusicImages.COVER_CONCERT, ""),
                    CloudMusicTrack("s2_2", "Tajdar-e-Haram", "Atif Aslam", "5:45", 345, MusicImages.COVER_ISLAMIC, "")
                )
            ),
            Singer(
                name = "Shreya Ghoshal",
                avatarUrl = MusicImages.ARTIST_SHREYA,
                songs = listOf(
                    CloudMusicTrack("s3_1", "Sunn Raha Hai", "Shreya Ghoshal", "4:15", 255, MusicImages.COVER_ACOUSTIC, ""),
                    CloudMusicTrack("s3_2", "Manwa Laage", "Shreya Ghoshal", "3:50", 230, MusicImages.COVER_LOFI, "")
                )
            ),
            Singer(
                name = "James",
                avatarUrl = MusicImages.ARTIST_JAMES,
                songs = listOf(
                    CloudMusicTrack("s4_1", "Anbe (Nagar)", "James", "4:30", 270, MusicImages.COVER_ROCK, ""),
                    CloudMusicTrack("s4_2", "Taray Bhalobashi", "James", "5:05", 305, MusicImages.COVER_CONCERT, "")
                )
            )
        )
    }

    val islamicGozuls = remember {
        listOf(
            CloudMusicTrack("isl1", "Hasbi Rabbi Jalallah", "Vocal Only Gazal", "4:15", 255, MusicImages.COVER_ISLAMIC, ""),
            CloudMusicTrack("isl2", "Tawbah Breeze", "Islamic Breeze", "3:50", 230, MusicImages.COVER_ISLAMIC, ""),
            CloudMusicTrack("isl3", "Nabi Ji Nabi Ji", "Madina Echoes", "4:40", 280, MusicImages.COVER_ISLAMIC, ""),
            CloudMusicTrack("isl4", "Madinay Chalo", "Peace Voice", "5:10", 310, MusicImages.COVER_ISLAMIC, ""),
            CloudMusicTrack("isl5", "Rahman Ya Rahman", "Mishary Alafasy", "4:32", 272, MusicImages.COVER_ISLAMIC, "")
        )
    }

    // Active selected singer for song listings
    var selectedSinger by remember { mutableStateOf(singers[0]) }

    // Cinematic selected track for zoom-in play effect
    var zoomTrackId by remember { mutableStateOf<String?>(null) }

    // Auto-sliding page indicators
    var activeSlideIndex1 by remember { mutableStateOf(0) }
    var activeSlideIndex2 by remember { mutableStateOf(0) }

    // LaunchedEffect for Banner 1 auto rotation
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            activeSlideIndex1 = (activeSlideIndex1 + 1) % bannerSlides1.size
        }
    }

    // LaunchedEffect for Banner 2 auto rotation
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            activeSlideIndex2 = (activeSlideIndex2 + 1) % bannerSlides2.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .nestedScroll(nestedScrollConnection)
    ) {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topPaddingDp, bottom = 120.dp)
        ) {

        // --- 2. ACTIVE COMPACT PLAYER HEADER (IF PLAYING) ---
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Spinning album disk
                            val infiniteTransition = rememberInfiniteTransition(label = "ArtworkRotation")
                            val rotationAngle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(20000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "ArtworkAngle"
                            )
                            val activeRotation = if (isPlaying) rotationAngle else 0f

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(currentTrack.imageUrl),
                                    contentDescription = currentTrack.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .rotate(activeRotation)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTrack.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentTrack.artist,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Control buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.previousTrack() }) {
                                    Icon(Icons.Default.SkipPrevious, "Prev", tint = Color(0xFF64748B))
                                }
                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFE0F2FE), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color(0xFF0369A1)
                                    )
                                }
                                IconButton(onClick = { viewModel.nextTrack() }) {
                                    Icon(Icons.Default.SkipNext, "Next", tint = Color(0xFF64748B))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom high fidelity light slider
                        Slider(
                            value = if (totalSec > 0) progressSec.toFloat() / totalSec else 0f,
                            onValueChange = { /* Tap seek */ },
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFF0284C7),
                                inactiveTrackColor = Color(0xFFE2E8F0),
                                thumbColor = Color(0xFF0284C7)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- 3. AUTO-SLIDER BANNER 1 ---
        item {
            val slide = bannerSlides1[activeSlideIndex1]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.playTrack(slide.associateTrack) }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(slide.imageUrl),
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // High fidelity white gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "FEATURED DEEP CHILL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFBAE6FD),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = slide.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = slide.subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }

                // Active dot indicators
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bannerSlides1.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (i == activeSlideIndex1) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // --- 4. MULTIPLE REPEAT-USE PLAYLIST ROWS ---
        mixedPlaylists.forEach { (sectionTitle, tracks) ->
            item {
                Text(
                    text = sectionTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(tracks) { track ->
                        // Cinematic Animated Zoom Effect
                        val isZoomed = zoomTrackId == track.id
                        val scale by animateFloatAsState(
                            targetValue = if (isZoomed) 1.08f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "zoom"
                        )

                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .shadow(if (isZoomed) 8.dp else 2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    zoomTrackId = track.id
                                    viewModel.playTrack(track)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(track.imageUrl),
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = track.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = track.artist,
                                    fontSize = 11.sp,
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

        // --- 5. SINGERS AVATARS SECTION ---
        item {
            Text(
                text = "Featured Singers",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(start = 18.dp, top = 24.dp, bottom = 12.dp)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(singers) { singer ->
                    val isSelected = selectedSinger.name == singer.name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedSinger = singer }
                            .width(80.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0),
                                    shape = CircleShape
                                )
                                .padding(if (isSelected) 4.dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(singer.avatarUrl),
                                contentDescription = singer.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = singer.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0369A1) else Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- SINGER'S POPULAR SONGS LIST ---
        item {
            Text(
                text = "Popular Hits by ${selectedSinger.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        items(selectedSinger.songs) { song ->
            val isCurrent = currentTrack.id == song.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFFF0F9FF) else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .clickable { viewModel.playTrack(song) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(song.imageUrl),
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color(0xFF0369A1) else Color(0xFF1E293B)
                        )
                        Text(
                            text = song.artist,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (isCurrent && isPlaying) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Playing",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = song.duration,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // --- 6. MIDDLE AUTO-SLIDER BANNER 2 ---
        item {
            val slide = bannerSlides2[activeSlideIndex2]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .shadow(3.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.playTrack(slide.associateTrack) }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(slide.imageUrl),
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = slide.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = slide.subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }
        }

        // --- 7. ISLAMIC RELIGIOUS GOZULS SECTION (AT THE BOTTOM) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF059669)) // Warm Emerald theme indicator for Islamic tab
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Islamic Gozul & Ghazal Stream",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
            }
        }

        items(islamicGozuls) { gozul ->
            val isCurrent = currentTrack.id == gozul.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFFECFDF5) else Color.White // Emerald glow when selected
                )
            ) {
                Row(
                    modifier = Modifier
                        .clickable { viewModel.playTrack(gozul) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE6F4EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Gozul Note",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = gozul.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color(0xFF059669) else Color(0xFF1E293B)
                        )
                        Text(
                            text = gozul.artist,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (isCurrent && isPlaying) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Playing Gozul",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = gozul.duration,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }

        // --- ANIMATED FLOATING IMMERSIVE TOP BAR CONTAINER (Floats on top of feed) ---
        val fraction = if (topBarHeightPx > 0f) -topBarOffsetHeightPx / topBarHeightPx else 0f
        val translationY = with(density) { topBarOffsetHeightPx.toDp() }
        
        val alpha = if (fraction >= 0.90f) {
            val progress = (fraction - 0.90f) / 0.10f
            (1f - progress).coerceIn(0f, 1f)
        } else {
            1f
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = translationY)
                .clipToBounds()
                .statusBarsPadding()
                .onSizeChanged { size ->
                    topBarHeightPx = size.height.toFloat()
                }
                .graphicsLayer(alpha = alpha)
        ) {
            // Frosted blur backdrop layers
            if (fraction > 0f) {
                val blurColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.72f)
                val borderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.25f)
                val bgColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.25f)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(blurColor)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(
                            width = 0.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        )
                        .background(bgColor)
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "Cloud Sky Stream",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Fully Light & High Fidelity",
                        fontSize = 10.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
