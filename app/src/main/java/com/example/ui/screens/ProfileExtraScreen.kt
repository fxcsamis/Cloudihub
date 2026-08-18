package com.example.ui.screens

import com.example.ui.components.WaveBallLoaderHtmlView

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.ui.components.LOTTIE_OVERLAY_1_URL
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import com.example.ui.components.NavigationTab
import com.example.ui.CloudihubViewModel
import com.example.ui.components.CloudShape
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import androidx.compose.ui.window.Dialog

// --- Profile tab: Watch Later, AI Settings, profile metrics row, Prime badges sheet ---
@Composable
fun WatchLaterScreen(viewModel: CloudihubViewModel) {
    val watchLaterList = viewModel.watchLaterVideos
    val isDark = viewModel.isDarkTheme

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.activeProfilePage = "" },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Watch Later",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF9100).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${watchLaterList.size} Saved",
                    color = Color(0xFFFF9100),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (watchLaterList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://i.postimg.cc/G2tMPzZm/Fast-Delivery-icon-concept-in-black-duo-line-color.jpg",
                            contentDescription = "Watch Later Empty",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Watch Later Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the Watch Later icon on any video thumbnail on the Home screen to save videos here for later viewing.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(watchLaterList, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.playVideo(video) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 100.dp, height = 62.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = video.imageUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(video.duration, color = Color.White, fontSize = 9.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isDark) Color.White else Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = video.creator,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleWatchLater(video) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFEF4444),
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

// --- SUB-SCREEN: AI ASSISTANT CONTROL & PERMISSIONS ---
@Composable
fun AiSettingsScreen(viewModel: CloudihubViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9FF))
            .statusBarsPadding()
    ) {
        // Header Bar - Seamless background attached with page
        Surface(
            color = Color.Transparent,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { viewModel.activeProfilePage = "main" },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Copilot & Control Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Permissions & Smart Automation",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF6366F1))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (viewModel.isAiAssistantEnabled) "ONLINE" else "OFFLINE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card with Cloud Theme Banner & Description
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveBallLoaderHtmlView(
                                transparentBg = true,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI COPILOT CONTROL CENTER",
                                color = Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Smart Automation Engine",
                                color = Color(0xFF0F172A),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2FE))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("CLOUD THEME", color = Color(0xFF0369A1), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cloud Theme Description Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE0F2FE), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "• Dynamic Animated Mascot • Smart Site Domain Resolver • Auto Video Downloader",
                                color = Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask AI to visit sites by name (e.g. 'open daraz', 'facebook', 'search github') without full URLs!",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // MASTER SWITCH CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Assistant Power",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enable floating mascot & AI copilot across all screens",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Switch(
                        checked = viewModel.isAiAssistantEnabled,
                        onCheckedChange = { viewModel.isAiAssistantEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFDC2626)
                        )
                    )
                }
            }

            // CONDITIONAL SUB-TOGGLES (Visible only when Master AI Assistant Switch is ON)
            if (viewModel.isAiAssistantEnabled) {
                Text(
                    text = "OPTIONAL FEATURE SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column {
                        // Feature 1: Music Visualizer Effect (Defaulted to OFF)
                        AiToggleItem(
                            icon = Icons.Default.GraphicEq,
                            title = "Music Wave Effect",
                            description = "Play audio wave animation when streaming music or video. [Default OFF - Enable manually]",
                            checked = viewModel.isAiMusicEffectEnabled,
                            onCheckedChange = { viewModel.isAiMusicEffectEnabled = it },
                            iconColor = Color(0xFFEC4899)
                        )

                        Divider(color = Color(0xFFF1F5F9))

                        // Feature 2: Thinking Mode Effect
                        AiToggleItem(
                            icon = Icons.Default.Psychology,
                            title = "Thinking Mode Effect",
                            description = "Display animated visual state when AI is processing responses.",
                            checked = viewModel.isAiThinkingModeEnabled,
                            onCheckedChange = { viewModel.isAiThinkingModeEnabled = it },
                            iconColor = Color(0xFF8B5CF6)
                        )

                        Divider(color = Color(0xFFF1F5F9))

                        // Feature 3: Smart Web Redirect & Domain Navigation
                        AiToggleItem(
                            icon = Icons.Default.Language,
                            title = "Smart Web Domain Redirect",
                            description = "Auto-detect site names (e.g. Daraz, Facebook, Google) and redirect or search via Cloudihub Browser.",
                            checked = viewModel.isAiSiteNavigationEnabled,
                            onCheckedChange = { viewModel.isAiSiteNavigationEnabled = it },
                            iconColor = Color(0xFF6366F1)
                        )

                        Divider(color = Color(0xFFF1F5F9))

                        // Feature 4: AI Auto-Downloader Assistant
                        AiToggleItem(
                            icon = Icons.Default.Download,
                            title = "Auto Video Downloader Assistant",
                            description = "Provide clean video preview cards with resolution options (1080p, 720p, 480p, MP3) in AI chat.",
                            checked = viewModel.isAiAutoDownloaderEnabled,
                            onCheckedChange = { viewModel.isAiAutoDownloaderEnabled = it },
                            iconColor = Color(0xFF22C55E)
                        )

                        Divider(color = Color(0xFFF1F5F9))

                        // Feature 5: Domain Intent Verification
                        AiToggleItem(
                            icon = Icons.Default.VerifiedUser,
                            title = "Domain Intent Verification",
                            description = "Verify intent if asking for TikTok video download with a YouTube link provided.",
                            checked = viewModel.isAiIntentVerificationEnabled,
                            onCheckedChange = { viewModel.isAiIntentVerificationEnabled = it },
                            iconColor = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // Quick Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.isAiAssistantEnabled = true
                        viewModel.isAiMusicEffectEnabled = true
                        viewModel.isAiLinkDetectionEnabled = true
                        viewModel.isAiSiteNavigationEnabled = true
                        viewModel.isAiAutoDownloaderEnabled = true
                        viewModel.isAiIntentVerificationEnabled = true
                        Toast.makeText(context, "All AI permissions reset to Defaults", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("Reset Defaults", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AiToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconColor
            )
        )
    }
}

@Composable
fun UserProfileMetricsRow(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsStateWithLifecycle()
    if (!isGoogleSignedIn) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.clickable {
                    viewModel.showSignupScreen = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔒 Level & Balance Locked",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
        return
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Tiny Level Badge Icon
            Surface(
                shape = CircleShape,
                color = Color(0xFFF0F9FF),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Level: ${viewModel.userLevel}", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "User Level",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = viewModel.userLevel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // 2. Tiny Prime Badge Icon
            Surface(
                shape = CircleShape,
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.clickable {
                    viewModel.showPrimeBadgesDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Prime Level",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = viewModel.userPrimeLevel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }

            // 3. Tiny Balance Badge Icon
            Surface(
                shape = CircleShape,
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Balance: ${viewModel.userBalance}", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Balance",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = viewModel.userBalance,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }
        }
    }
}

data class BadgeCardInfo(
    val icon: String,
    val tag: String,
    val desc: String,
    val perks: List<String>
)

@Composable
fun ModernPrimeBadgesBottomSheet(
    viewModel: CloudihubViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val closeSheet = {
        isVisible = false
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(280)
            onDismiss()
        }
    }

    BackHandler(enabled = isVisible) {
        closeSheet()
    }

    var selectedBadgeId by remember { 
        mutableStateOf(
            viewModel.availablePrimeBadges.find { it.levelName == viewModel.userPrimeLevel }?.id 
                ?: viewModel.availablePrimeBadges.firstOrNull()?.id 
                ?: "b3"
        ) 
    }

    var selectedFilterCategory by remember { mutableStateOf("All Badges") }

    val filteredBadges = remember(selectedFilterCategory) {
        when (selectedFilterCategory) {
            "Popular" -> viewModel.availablePrimeBadges.filter { it.id in listOf("b1", "b2", "b3") }
            "VIP Tiers" -> viewModel.availablePrimeBadges.filter { it.id in listOf("b3", "b4", "b5") }
            else -> viewModel.availablePrimeBadges
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Scrim backdrop with fade animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { closeSheet() }
            )
        }

        // Sheet content with slide-up animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 280, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(250))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(enabled = false) {}, // Prevent closing when tapping inside sheet
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 90.dp)
                ) {
                    // Top Drag Indicator & Header Row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag Bar (Pull down or tap to dismiss sheet)
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .width(56.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCBD5E1))
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (dragAmount > 6f) {
                                            closeSheet()
                                        }
                                    }
                                }
                                .clickable { closeSheet() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Prime Badge",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Prime Badges Store",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Select & highlight cards to activate perks",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Filter Category Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All Badges", "Popular", "VIP Tiers").forEach { cat ->
                                val isCatSelected = (selectedFilterCategory == cat)
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { selectedFilterCategory = cat },
                                    label = {
                                        Text(
                                            text = cat,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCatSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0284C7),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF475569)
                                    ),
                                    border = null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Scrollable Cards List with Dynamic Highlighting
                    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                        initialPage = 0,
                        pageCount = { filteredBadges.size }
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 12.dp
                    ) { pageIndex ->
                        val badge = filteredBadges.getOrNull(pageIndex) ?: return@HorizontalPager
                        val isCardSelected = (selectedBadgeId == badge.id)
                        val isCardActive = (viewModel.userPrimeLevel == badge.levelName)

                        // Animated scale & border highlight
                        val scale by animateFloatAsState(
                            targetValue = if (isCardSelected) 1.02f else 0.97f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "cardScale"
                        )

                        val cardBorderColor by animateColorAsState(
                            targetValue = when {
                                isCardActive -> Color(0xFF10B981)
                                isCardSelected -> Color(0xFF0284C7)
                                else -> Color(0xFFE2E8F0)
                            },
                            label = "cardBorderColor"
                        )

                        val cardContainerColor by animateColorAsState(
                            targetValue = when {
                                isCardActive -> Color(0xFFECFDF5)
                                isCardSelected -> Color(0xFFF0F9FF)
                                else -> Color(0xFFFAFAFC)
                            },
                            label = "cardBgColor"
                        )

                        val cardInfo = when (badge.iconType) {
                            "Shield" -> BadgeCardInfo("🛡️", "STARTER PASS", "Essential cloud tools for fast browsing & file access.", listOf("Instant Cloud Sync", "1.2x Speed Download", "Ad-Lite Search", "Encrypted Vault"))
                            "Star" -> BadgeCardInfo("⭐", "MOST POPULAR", "Enhanced speed and storage for active power users.", listOf("1.5x Accelerated Speed", "20 GB Cloud Vault", "Auto Ad & Popup Blocker", "Silver Badge Ring"))
                            "Crown" -> BadgeCardInfo("👑", "BEST VALUE", "Ultimate cloud experience with turbo speed & VIP perks.", listOf("2x Turbo Speed Downloads", "Unlimited Encrypted Vault", "Golden Glowing Badge", "AI Summarizer"))
                            "Diamond" -> BadgeCardInfo("💎", "ULTIMATE VIP", "Unrestricted diamond power with ultra bandwidth.", listOf("4x Ultra Speed Pipeline", "Unlimited AI Copilot", "Diamond Glowing Avatar", "Zero Ads Guarantee"))
                            else -> BadgeCardInfo("🏆", "SUPREME CROWN", "Highest tier lifetime privilege pass with master badge.", listOf("Maximum Server Bandwidth", "Lifetime VIP All-Access", "Exclusive Crown Master", "24/7 Priority Support"))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Light Glow Aura Effect behind card (Bronze, Purple, Emerald, Azure, Inferno glowing lights)
                            val glowAlphaCenter = if (isCardSelected || isCardActive) 0.70f else 0.30f
                            val glowAlphaEdge = if (isCardSelected || isCardActive) 0.35f else 0.12f

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.96f)
                                    .height(290.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(badge.glowColorHex).copy(alpha = glowAlphaCenter),
                                                Color(badge.glowColorHex).copy(alpha = glowAlphaEdge),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(36.dp)
                                    )
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .border(
                                        width = if (isCardSelected || isCardActive) 2.5.dp else 1.dp,
                                        color = cardBorderColor,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable {
                                        selectedBadgeId = badge.id
                                    },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isCardSelected) 8.dp else 2.dp)
                            ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isCardSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFFCBD5E1).copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 20.dp, y = (-12).dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Tag Banner & Price Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = when {
                                                isCardActive -> Color(0xFF10B981)
                                                isCardSelected -> Color(0xFF0284C7)
                                                else -> Color(0xFFE2E8F0)
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                if (isCardActive || isCardSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = when {
                                                        isCardActive -> "ACTIVE PLAN ✓"
                                                        isCardSelected -> "SELECTED PLAN ★"
                                                        else -> cardInfo.tag
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isCardActive || isCardSelected) Color.White else Color(0xFF475569)
                                                )
                                            }
                                        }

                                        // Price Chip
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF0FDF4),
                                            border = BorderStroke(1.dp, Color(0xFF4ADE80))
                                        ) {
                                            Text(
                                                text = String.format(java.util.Locale.US, "$%.2f", badge.price),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Badge Image, Title & Level Name
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(Color(badge.colorHex).copy(alpha = 0.15f))
                                                .border(1.5.dp, Color(badge.colorHex).copy(alpha = 0.6f), CircleShape)
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = badge.imageUrl,
                                                contentDescription = badge.levelName,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = badge.badgeTitle,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = badge.levelName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(badge.colorHex)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = cardInfo.desc,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "HIGHLIGHTED PERKS & PRIVILEGES:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isCardSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                                        letterSpacing = 0.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        cardInfo.perks.forEach { perk ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Check",
                                                    tint = if (isCardSelected) Color(0xFF0284C7) else Color(0xFF10B981),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = perk,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF1E293B),
                                                    fontWeight = if (isCardSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicator Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(filteredBadges.size) { idx ->
                            val isCurrentPage = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(6.dp)
                                    .width(if (isCurrentPage) 22.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentPage) Color(0xFF0284C7) else Color(0xFFCBD5E1))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Section
                    val currentActiveBadge = filteredBadges.getOrNull(pagerState.currentPage)
                        ?: viewModel.availablePrimeBadges.find { it.id == selectedBadgeId }
                        ?: viewModel.availablePrimeBadges.first()

                    val isPageBadgeOwned = viewModel.unlockedBadges.any { it.id == currentActiveBadge.id }
                    val isPageBadgeActive = viewModel.userPrimeLevel == currentActiveBadge.levelName

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isPageBadgeActive) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFFECFDF5),
                                    disabledContentColor = Color(0xFF10B981)
                                )
                            ) {
                                Text("Active Badge Plan ✓", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        } else if (isPageBadgeOwned) {
                            Button(
                                onClick = {
                                    viewModel.userPrimeLevel = currentActiveBadge.levelName
                                    Toast.makeText(context, "Switched to ${currentActiveBadge.badgeTitle}!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("Activate ${currentActiveBadge.badgeTitle}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            val canAffordCredit = viewModel.userBalanceAmount >= currentActiveBadge.price

                            // Unlock with Credit
                            Button(
                                onClick = {
                                    if (viewModel.buyPrimeBadge(currentActiveBadge)) {
                                        Toast.makeText(context, "Unlocked ${currentActiveBadge.badgeTitle} with Credit!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Insufficient credit balance! Top up or invite friends.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = canAffordCredit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    disabledContainerColor = Color(0xFFE2E8F0)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Credit",
                                        tint = if (canAffordCredit) Color.White else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (canAffordCredit) "Unlock with Credit ($${String.format(java.util.Locale.US, "%.2f", currentActiveBadge.price)})" else "Low Credit Balance ($${String.format(java.util.Locale.US, "%.2f", viewModel.userBalanceAmount)})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canAffordCredit) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Pay Direct
                            OutlinedButton(
                                onClick = {
                                    if (!isPageBadgeOwned) {
                                        viewModel.unlockedBadges.add(currentActiveBadge)
                                        viewModel.userPrimeLevel = currentActiveBadge.levelName
                                        Toast.makeText(context, "Payment Successful! ${currentActiveBadge.badgeTitle} Unlocked!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF0284C7)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Pay",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, "Pay $%.2f to Unlock", currentActiveBadge.price),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7)
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

