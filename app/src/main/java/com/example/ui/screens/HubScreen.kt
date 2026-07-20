package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import com.example.ui.CloudVideo
import com.example.ui.CloudihubViewModel
import com.example.ui.SupportedMediaSite
import kotlin.math.absoluteValue
import kotlin.math.sin

private val cloudThemeColor = Color(0xFF0284C7)
private val cloudLightBg = Color(0xFFE0F2FE)
private val cloudTextDark = Color(0xFF0F172A)
private val cloudSubtitle = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSite by remember { mutableStateOf<SupportedMediaSite?>(null) }
    val gridState = rememberLazyGridState()
    val sites = viewModel.filteredMediaSites

    LaunchedEffect(Unit) {
        viewModel.loadSupportedMediaSites()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "CloudHubFloatingBg")
    val globalFloatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlobalFloatBg"
    )

    var isHeaderVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                isHeaderVisible = when {
                    index < previousIndex -> true
                    index > previousIndex -> false
                    offset < previousScrollOffset - 15 -> true
                    offset > previousScrollOffset + 15 -> false
                    else -> isHeaderVisible
                }
                previousIndex = index
                previousScrollOffset = offset
            }
    }

    val isAtTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0 }
    }

    Box(modifier = modifier.fillMaxSize()) {
        FloatingHubBackground(globalFloatOffset)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
                .statusBarsPadding()
                .background(Color.White.copy(alpha = if (isAtTop) 0.0f else 0.78f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            AnimatedVisibility(visible = isHeaderVisible || isAtTop) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Media Download Hub",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = cloudTextDark
                            )
                            Text(
                                text = "Search 1800+ resolver sources • personal / public / CC media",
                                fontSize = 12.sp,
                                color = cloudSubtitle,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshSupportedMediaSites() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(cloudLightBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh sites",
                                tint = cloudThemeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            OutlinedTextField(
                value = viewModel.mediaHubSearchQuery,
                onValueChange = { viewModel.updateMediaHubSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("media_site_search"),
                placeholder = { Text("Search supported sites, extractors, domains...", color = Color(0xFF94A3B8)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = cloudThemeColor) },
                trailingIcon = {
                    if (viewModel.isLoadingSupportedSites) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = cloudThemeColor)
                    } else {
                        Text("${sites.size}", color = cloudSubtitle, fontSize = 11.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.96f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
                    focusedBorderColor = cloudThemeColor,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedTextColor = cloudTextDark,
                    unfocusedTextColor = cloudTextDark,
                    cursorColor = cloudThemeColor
                )
            )
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 165.dp,
                bottom = 120.dp,
                start = 20.dp,
                end = 20.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            if (viewModel.supportedSitesError.isNotBlank()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    ComplianceInfoCard(message = viewModel.supportedSitesError)
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                ComplianceInfoCard(
                    message = "Only resolve media you own, public-domain/Creative Commons media, or sources that explicitly allow download/offline backup."
                )
            }

            items(sites, key = { it.id }) { site ->
                SocialCard(
                    site = site,
                    onClick = { selectedSite = site }
                )
            }
        }

        if (selectedSite != null) {
            HubPopupDialog(
                site = selectedSite!!,
                viewModel = viewModel,
                onDismiss = { selectedSite = null }
            )
        }
    }
}

@Composable
private fun FloatingHubBackground(globalFloatOffset: Float) {
    val floatingTags = listOf("MP4", "WEBM", "HLS", "DASH", "CC", "PD", "R2", "S3", "URL", "API", "1800+", "HUB")
    Box(modifier = Modifier.fillMaxSize()) {
        floatingTags.forEachIndexed { index, tag ->
            val angleOffset = index * 28f
            val xPosDp = (18 + (index * 31) % 330).dp
            val yBase = (80 + (index * 49) % 610).dp
            Box(
                modifier = Modifier
                    .offset(
                        x = xPosDp,
                        y = yBase + (sin(globalFloatOffset + angleOffset) * 14).dp
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(tag, color = Color.White.copy(alpha = 0.24f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ComplianceInfoCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = cloudThemeColor)
            Spacer(modifier = Modifier.width(10.dp))
            Text(message, fontSize = 12.sp, color = cloudSubtitle, lineHeight = 16.sp)
        }
    }
}

@Composable
fun SocialCard(
    site: SupportedMediaSite,
    onClick: () -> Unit
) {
    val brandColor = remember(site.name) { colorFromName(site.name) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(4.dp, RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp))
            .border(1.dp, brandColor.copy(alpha = 0.25f), RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp))
            .clip(RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp))
            .clickable { onClick() }
            .testTag("social_card_${site.name}"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFE2E8F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = site.logoUrl,
                        contentDescription = site.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = {
                            Icon(Icons.Default.Storage, contentDescription = site.name, tint = brandColor, modifier = Modifier.size(24.dp))
                        },
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = brandColor, strokeWidth = 2.dp)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(brandColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("READY", color = brandColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(site.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(site.domainHint, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.height(5.dp))
                Text(site.supportedFormats, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = brandColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubPopupDialog(
    site: SupportedMediaSite,
    viewModel: CloudihubViewModel,
    onDismiss: () -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val brandColor = remember(site.name) { colorFromName(site.name) }
    var selectedDownloadUrl by remember(site.id) { mutableStateOf("") }
    var selectedResolutionName by remember(site.id) { mutableStateOf("Best available") }
    var resolutionMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(site.id) {
        viewModel.clearMediaResolveState()
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cloudLightBg),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = site.logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = { Icon(Icons.Default.Link, contentDescription = null, tint = brandColor, modifier = Modifier.size(20.dp)) }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("${site.name} Portal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = cloudTextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(site.category, fontSize = 11.sp, color = cloudSubtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = cloudSubtitle, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it.trim() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hub_url_input"),
                    placeholder = { Text("Paste your personal/public/CC media URL...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = brandColor) },
                    trailingIcon = {
                        IconButton(onClick = { clipboardManager.getText()?.let { urlInput = it.text.trim() } }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste Clipboard", tint = brandColor, modifier = Modifier.size(20.dp))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = cloudTextDark,
                        unfocusedTextColor = cloudTextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = brandColor,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        cursorColor = brandColor
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = cloudTextDark)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cloudLightBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("How this works", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "1. Paste a media URL you own or have permission to archive.\n" +
                                "2. Backend returns metadata/stream URL only.\n" +
                                "3. Your phone streams/downloads directly; server does not proxy media bytes.",
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AnimatedVisibility(visible = viewModel.mediaResolveError.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(viewModel.mediaResolveError, color = Color(0xFF991B1B), fontSize = 12.sp)
                    }
                }

                val resolved = viewModel.lastResolvedMedia
                LaunchedEffect(resolved?.id, resolved?.videoStreamUrl) {
                    if (resolved != null) {
                        val first = resolved.resolutions.firstOrNull()
                        selectedResolutionName = first?.resolutionName ?: "Best available"
                        selectedDownloadUrl = first?.downloadUrl ?: resolved.videoStreamUrl
                    }
                }

                AnimatedVisibility(visible = resolved != null) {
                    resolved?.let { media ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF0F172A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = media.thumbnail.ifBlank { site.logoUrl },
                                            contentDescription = media.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                CircularProgressIndicator(color = cloudThemeColor, modifier = Modifier.size(26.dp))
                                            },
                                            error = {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
                                            }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.70f)),
                                                        startY = 80f
                                                    )
                                                )
                                        )
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                media.title.ifBlank { "Resolved Media" },
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "Duration: ${media.duration.ifBlank { "Unknown" }} • ${media.views.ifBlank { "Personal media" }}",
                                                color = Color.White.copy(alpha = 0.78f),
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Select resolution", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cloudSubtitle)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                                .clickable { resolutionMenuExpanded = true }
                                                .padding(horizontal = 14.dp, vertical = 13.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(selectedResolutionName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cloudTextDark)
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = cloudSubtitle)
                                        }

                                        DropdownMenu(
                                            expanded = resolutionMenuExpanded,
                                            onDismissRequest = { resolutionMenuExpanded = false },
                                            modifier = Modifier.background(Color.White)
                                        ) {
                                            media.resolutions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option.resolutionName, color = cloudTextDark) },
                                                    onClick = {
                                                        selectedResolutionName = option.resolutionName
                                                        selectedDownloadUrl = option.downloadUrl
                                                        resolutionMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.playVideo(
                                        CloudVideo(
                                            id = media.id.ifBlank { "preview_${System.currentTimeMillis()}" },
                                            title = media.title.ifBlank { "Preview Media" },
                                            duration = media.duration.ifBlank { "0:00" },
                                            creator = site.name,
                                            imageUrl = media.thumbnail.ifBlank { site.logoUrl },
                                            views = media.views.ifBlank { "Preview" },
                                            fileUrl = selectedDownloadUrl.ifBlank { media.videoStreamUrl },
                                            sizeMb = 0.0
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Play Preview Stream", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preview ready. Choose quality, then add to Download Hub.", color = Color(0xFF166534), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        val media = viewModel.lastResolvedMedia
                        if (media == null) {
                            viewModel.resolveMediaLinkForDownload(urlInput, site.name)
                        } else {
                            viewModel.downloadResolvedMedia(media, selectedDownloadUrl.ifBlank { media.videoStreamUrl }, site.name)
                        }
                    },
                    enabled = !viewModel.isResolvingMediaLink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("download_now_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cloudThemeColor)
                ) {
                    if (viewModel.isResolvingMediaLink) {
                        val infiniteLoading = rememberInfiniteTransition(label = "ExtractionLoading")
                        val spinAngle by infiniteLoading.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
                            label = "SpinLoading"
                        )
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.rotate(spinAngle))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resolving Preview...", fontWeight = FontWeight.Bold, color = Color.White)
                    } else if (viewModel.lastResolvedMedia == null) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resolve Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Selected Quality to Downloads", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

private fun colorFromName(name: String): Color {
    val palette = listOf(
        Color(0xFF0284C7),
        Color(0xFF7C3AED),
        Color(0xFF16A34A),
        Color(0xFFEA580C),
        Color(0xFFDB2777),
        Color(0xFF0891B2),
        Color(0xFF4F46E5),
        Color(0xFFDC2626)
    )
    return palette[name.hashCode().absoluteValue % palette.size]
}
