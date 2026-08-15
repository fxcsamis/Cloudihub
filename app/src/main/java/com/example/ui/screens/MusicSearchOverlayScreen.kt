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

// --- Music tab: full music search overlay ---
@Composable
fun MusicSearchScreenOverlay(
    onClose: () -> Unit,
    onTrackSelect: (MusicTrack) -> Unit,
    viewModel: CloudihubViewModel
) {
    BackHandler(enabled = true) {
        onClose()
    }

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val allTracks = remember {
        listOf(
            MusicTrack("t1", "Jiboner Ayna", "Parvez", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "4:12", "Romantic", "15M"),
            MusicTrack("t2", "Bolbona Go Ar Kono Din - 21", "Baul Sukumar", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400", "5:04", "Folk", "28M"),
            MusicTrack("t3", "Aaina Mon Bhanga", "Zubeen Garg, Jeet Gannguli & Priyo Chattopadhyay", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400", "4:45", "Melody", "42M"),
            MusicTrack("t4", "Chokhe Shanti Lage Amar", "Sathi Khan", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", "3:58", "Pop", "18M"),
            MusicTrack("t5", "Jeena Haraam", "Vishal Mishra", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400", "3:30", "Chill", "35M"),
            MusicTrack("t6", "Ruposh (Original Score)", "Wajhi Farooki", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400", "4:20", "Romantic", "211M"),
            MusicTrack("t7", "Toota Jo Kabhi Tara", "Sachin-Jigar, Sumedha Karmahe & Atif Aslam", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400", "5:05", "Romantic", "150M"),
            MusicTrack("t8", "Anyayo", "Aneesh & Krtin Kay", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400", "3:15", "Chill", "2.6M"),
            MusicTrack("t9", "Kusu Kusu", "Zahrah S Khan & Dev Negi", "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=400", "3:45", "Party", "907M"),
            MusicTrack("t10", "Jhol", "Maanu & Annural Khalid", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400", "3:10", "Relax", "808M"),
            MusicTrack("qp5", "no signal", "juggsi & kyra", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400", "2:50", "Chill", "12M"),
            MusicTrack("hs1", "Traag (feat. Jozo & Kraantje Pappie)", "Bizzey", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400", "3:02", "Party", "652M"),
            MusicTrack("hs2", "Phagooner Mohonaye", "Various Artist", "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=400", "3:40", "Relax", "78M")
        )
    }

    val filteredTracks = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE0F2FE),
                        Color(0xFFF1F5F9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Search Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        viewModel.addSearchQueryToHistory(searchQuery)
                                        keyboardController?.hide()
                                    }
                                }
                            ),
                            cursorBrush = SolidColor(Color(0xFF0284C7)),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search tracks, artists, albums...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.startVoiceSearch() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = Color(0xFF0284C7)
                                )
                            }
                        }
                    }
                }
            }

            // Search Content Body
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (searchQuery.isBlank()) {
                    // Recent Searches Section
                    if (viewModel.recentSearches.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recent Searches",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Clear All",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.clickable { viewModel.clearSearchHistory() }
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(viewModel.recentSearches) { historyQuery ->
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.clickable {
                                                searchQuery = historyQuery
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = historyQuery,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { viewModel.removeSearchQueryFromHistory(historyQuery) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Trending Music Searches Section
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Trending Music Searches",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val trending = listOf("Atif Aslam Hits", "Arijit Singh", "Lofi Sunset", "Bangla Acoustic", "Toota Jo Kabhi", "Coke Studio", "Party Beats")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(trending) { tag ->
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF0F9FF),
                                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                        modifier = Modifier.clickable {
                                            searchQuery = tag
                                            viewModel.addSearchQueryToHistory(tag)
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tag,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF0369A1)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Search Results List
                    if (filteredTracks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.MusicOff,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No songs found for \"$searchQuery\"",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "Found ${filteredTracks.size} tracks",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(filteredTracks) { track ->
                            Surface(
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSearchQueryToHistory(searchQuery)
                                        onTrackSelect(track)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(track.albumArt),
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${track.artist} • ${track.duration}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE0F2FE),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
