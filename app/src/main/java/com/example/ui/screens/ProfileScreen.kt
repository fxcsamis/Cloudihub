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

// --- Profile tab: root screen + top-level tab content (this is the file MainActivity/other screens reference) ---
@Composable
fun ProfileScreen(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    if (viewModel.showCloudHubInProfile) {
        SitesScreen(viewModel = viewModel)
        return
    }

    when (viewModel.activeProfilePage) {
        "watch_later" -> WatchLaterScreen(viewModel = viewModel)
        "refer" -> ReferScreen(viewModel = viewModel)
        "downloads" -> DownloadsScreen(viewModel = viewModel)
        "linked_devices" -> LinkedDevicesScreen(viewModel = viewModel)
        "offline_folders" -> OfflineFoldersScreen(viewModel = viewModel)
        "private_vault" -> PrivateVaultScreen(viewModel = viewModel)
        "ai_settings" -> AiSettingsScreen(viewModel = viewModel)
        else -> MainProfileContent(viewModel = viewModel, modifier = modifier)
    }

    if (viewModel.showPrimeBadgesDialog) {
        ModernPrimeBadgesBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showPrimeBadgesDialog = false }
        )
    }

    // Modal Bottom Sheets (with drag handles that can be slid/dragged down to dismiss)
    if (viewModel.showHistoryPopup) {
        HistoryBottomSheet(viewModel = viewModel) { viewModel.showHistoryPopup = false }
    }
    if (viewModel.showFeedbackPopup) {
        FeedbackBottomSheet(viewModel = viewModel) { viewModel.showFeedbackPopup = false }
    }
    if (viewModel.showSubscriptionPopup) {
        SubscriptionBottomSheet(viewModel = viewModel) { viewModel.showSubscriptionPopup = false }
    }

    // --- PRIVATE VAULT SETUP & UNLOCK POPUPS (AS REQUESTED) ---
    if (viewModel.showPrivateVaultPasswordTypeDialog) {
        PrivateVaultPasswordTypeDialog(viewModel = viewModel)
    }

    if (viewModel.showPrivateVaultPasswordInputDialog) {
        PrivateVaultPasswordInputDialog(viewModel = viewModel)
    }

    if (viewModel.showPrivateVaultUnlockDialog) {
        PrivateVaultUnlockDialog(viewModel = viewModel)
    }
}

@Composable
fun ProfileAnimatedBackground(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgCloudAnim")

    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1"
    )

    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2"
    )

    val animalOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animal"
    )

    val cloudScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Clean White / Soft Sky White Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E293B)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF8FAFC),
                                Color(0xFFEFF6FF)
                            )
                        )
                    }
                )
        )

        // Soft ambient cloud glow circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = if (isDark) 0.12f else 0.15f),
                        Color.Transparent
                    )
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.2f, size.height * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF60A5FA).copy(alpha = if (isDark) 0.10f else 0.12f),
                        Color.Transparent
                    )
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.85f, size.height * 0.45f)
            )
        }

        // Floating Cloud 1 (Top Left)
        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)).copy(alpha = 0.14f),
            modifier = Modifier
                .size(110.dp)
                .offset(x = 10.dp, y = 40.dp + cloudOffset1.dp)
                .graphicsLayer {
                    scaleX = cloudScale
                    scaleY = cloudScale
                }
        )

        // Floating Cloud Animal - Bird/Sky Pet 1 (Top Right)
        Icon(
            imageVector = Icons.Default.CrueltyFree,
            contentDescription = null,
            tint = (if (isDark) Color(0xFF818CF8) else Color(0xFF0284C7)).copy(alpha = 0.12f),
            modifier = Modifier
                .size(65.dp)
                .offset(x = 280.dp, y = 60.dp + animalOffset.dp)
                .graphicsLayer {
                    scaleX = cloudScale
                    scaleY = cloudScale
                }
        )

        // Floating Cloud 2 (Top Right)
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = null,
            tint = (if (isDark) Color(0xFF818CF8) else Color(0xFF38BDF8)).copy(alpha = 0.15f),
            modifier = Modifier
                .size(120.dp)
                .offset(x = 240.dp, y = 140.dp + cloudOffset2.dp)
                .graphicsLayer {
                    scaleX = cloudScale
                    scaleY = cloudScale
                }
        )

        // Floating Cloud Animal - Pet 2 (Mid Left)
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            tint = (if (isDark) Color(0xFF34D399) else Color(0xFF0284C7)).copy(alpha = 0.10f),
            modifier = Modifier
                .size(50.dp)
                .offset(x = 25.dp, y = 280.dp - animalOffset.dp)
        )

        // Floating Cloud 3 - Sync Cloud (Mid Right)
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            tint = (if (isDark) Color(0xFF38BDF8) else Color(0xFF60A5FA)).copy(alpha = 0.13f),
            modifier = Modifier
                .size(90.dp)
                .offset(x = 270.dp, y = 380.dp - cloudOffset1.dp)
        )

        // Floating Sky Wind / Air (Bottom Left)
        Icon(
            imageVector = Icons.Default.Air,
            contentDescription = null,
            tint = (if (isDark) Color(0xFF94A3B8) else Color(0xFF0284C7)).copy(alpha = 0.11f),
            modifier = Modifier
                .size(75.dp)
                .offset(x = 15.dp, y = 520.dp + cloudOffset2.dp)
        )
    }
}

@Composable
fun MainProfileContent(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val storageInfo = viewModel.getDeviceStorageInfo()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLongPressOptions by remember { mutableStateOf(false) }
    var pressJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Clean White Cloud Background with Floating Clouds & Cloud Animals
        ProfileAnimatedBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // --- PROFILE HEADER & SIGNUP OPTION ---
        val isSigned = isGoogleSignedIn
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 1. TOP: PROFILE IMAGE
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clickable {
                            viewModel.showEditProfileScreen = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(
                                3.dp,
                                Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF9333EA))),
                                CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                if (viewModel.userProfileAvatar.isNotEmpty()) viewModel.userProfileAvatar
                                else if (isSigned && viewModel.signedInUserPhoto.isNotEmpty()) viewModel.signedInUserPhoto
                                else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300"
                            ),
                            contentDescription = "Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    // Small Rank Badge Overlay attached to Profile Logo
                    val activeBadge = viewModel.currentActiveBadge
                    if (activeBadge.imageUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(
                                    1.5.dp,
                                    Color(activeBadge.colorHex),
                                    CircleShape
                                )
                                .padding(2.dp)
                                .clickable {
                                    viewModel.showPrimeBadgesDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(activeBadge.imageUrl),
                                contentDescription = activeBadge.levelName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. NAME, USERNAME, EMAIL SECTION
                val displayName = viewModel.userProfileFullName.ifEmpty { 
                    viewModel.signedInUserName.ifEmpty { "Alex Skyward" } 
                }
                val displayUsername = viewModel.userProfileUsername.ifEmpty { 
                    if (viewModel.signedInUserName.isNotEmpty()) "@${viewModel.signedInUserName.lowercase().replace(" ", "_")}" else "@alexskyward" 
                }
                val displayEmail = viewModel.userProfileEmail.ifEmpty { 
                    viewModel.signedInUserEmail.ifEmpty { "alex.skyward@cloudihub.io" } 
                }

                Text(
                    text = displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (displayUsername.startsWith("@")) displayUsername else "@$displayUsername",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = displayEmail,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { viewModel.showEditProfileScreen = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profile & Info",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }

        // 3. BADGES SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSigned) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = "Badges",
                        tint = if (isSigned) Color(0xFFD97706) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BADGES & REWARDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSigned) Color(0xFF475569) else Color(0xFF64748B),
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = if (isSigned) "View Shop ➔" else "Unlock Tiers 🔒",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSigned) Color(0xFF0284C7) else Color(0xFF64748B),
                    modifier = Modifier.clickable {
                        if (isSigned) viewModel.showPrimeBadgesDialog = true
                        else viewModel.showSignupScreen = true
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isSigned) Color(0xFFE2E8F0) else Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                    .then(if (!isSigned) Modifier.graphicsLayer { alpha = 0.65f } else Modifier)
                    .clickable {
                        if (!isSigned) viewModel.showSignupScreen = true
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSigned) Color.White else Color(0xFFF8FAFC))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(viewModel.availablePrimeBadges) { badge ->
                            val isUnlocked = isSigned && viewModel.unlockedBadges.any { it.id == badge.id }
                            val isActive = isSigned && viewModel.userPrimeLevel == badge.levelName

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isActive) Color(badge.colorHex).copy(alpha = 0.15f)
                                        else if (isUnlocked) Color(0xFFF8FAFC)
                                        else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    width = if (isActive) 1.8.dp else 1.dp,
                                    color = if (isActive) Color(badge.colorHex)
                                            else if (isUnlocked) Color(badge.colorHex).copy(alpha = 0.4f)
                                            else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.clickable {
                                    if (isSigned) viewModel.showPrimeBadgesDialog = true
                                    else viewModel.showSignupScreen = true
                                }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = rememberAsyncImagePainter(badge.imageUrl),
                                            contentDescription = badge.badgeTitle,
                                            contentScale = ContentScale.Fit,
                                            colorFilter = if (!isSigned) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        if (!isUnlocked || !isSigned) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = badge.badgeTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSigned && isUnlocked) Color(0xFF0F172A) else Color(0xFF64748B)
                                    )

                                    Text(
                                        text = if (isActive) "ACTIVE"
                                               else if (isUnlocked) "UNLOCKED"
                                               else if (!isSigned) "LOCKED"
                                               else if (badge.price == 0.0) "FREE"
                                               else "$${badge.price}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isActive) Color(badge.colorHex)
                                                else if (isUnlocked) Color(0xFF059669)
                                                else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. LEVEL SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSigned) Icons.Default.Star else Icons.Default.Lock,
                        contentDescription = "Level",
                        tint = if (isSigned) Color(0xFF0284C7) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LEVEL & RANK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSigned) Color(0xFF475569) else Color(0xFF64748B),
                        letterSpacing = 0.8.sp
                    )
                }

                val activeBadge = viewModel.currentActiveBadge
                Text(
                    text = if (isSigned) activeBadge.levelName else "Locked 🔒",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSigned) Color(activeBadge.colorHex) else Color(0xFF64748B)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isSigned) Color(0xFFE2E8F0) else Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                    .then(if (!isSigned) Modifier.graphicsLayer { alpha = 0.65f } else Modifier)
                    .clickable {
                        if (!isSigned) viewModel.showSignupScreen = true
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSigned) Color.White else Color(0xFFF8FAFC))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val activeBadge = viewModel.currentActiveBadge
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSigned) Color(activeBadge.colorHex).copy(alpha = 0.12f)
                                        else Color(0xFFE2E8F0)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSigned) Color(activeBadge.colorHex).copy(alpha = 0.3f)
                                        else Color(0xFFCBD5E1),
                                        CircleShape
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(activeBadge.imageUrl),
                                    contentDescription = activeBadge.levelName,
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (!isSigned) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isSigned) "Level 5 • ${activeBadge.badgeTitle}" else "Level Locked • Sign In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSigned) Color(0xFF0F172A) else Color(0xFF64748B)
                                )
                                Text(
                                    text = if (isSigned) "XP Progress: 3,450 / 5,000 XP" else "Sign in to earn XP & unlock perks",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSigned) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSigned) Color(0xFFA7F3D0) else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable {
                                if (isSigned) viewModel.showPrimeBadgesDialog = true
                                else viewModel.showSignupScreen = true
                            }
                        ) {
                            Text(
                                text = if (isSigned) "Upgrade ⚡" else "Sign In 🔒",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSigned) Color(0xFF059669) else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = if (isSigned) 0.69f else 0f,
                        color = if (isSigned) Color(viewModel.currentActiveBadge.colorHex) else Color(0xFF94A3B8),
                        trackColor = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSigned) "Next Tier: Level 6 • Diamond Elite" else "Unlock ranks by signing in",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isSigned) "69%" else "0%",
                            fontSize = 11.sp,
                            color = if (isSigned) Color(viewModel.currentActiveBadge.colorHex) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PHYSICAL PHONE STORAGE QUOTA CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Device Storage",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CloudeHub Storage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F9FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${storageInfo.percentUsed}% Used",
                            color = Color(0xFF0284C7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = storageInfo.percentUsed / 100f,
                    color = Color(0xFF0284C7),
                    trackColor = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${storageInfo.usedGB} GB Used",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${storageInfo.totalGB} GB Total",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- STATS ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                title = "Offline Files",
                value = "${viewModel.downloadItems.size} files",
                icon = Icons.Default.CloudQueue,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.activeProfilePage = "offline_folders" }
            )

            ProfileStatCard(
                title = "Linked Devices",
                value = "3 Active",
                icon = Icons.Default.DeviceHub,
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.biometricAuthTarget = "linked_devices"
                    viewModel.showFingerprintAuth = true
                }
            )

            AnimatedVisibility(
                visible = !viewModel.isVaultCardHidden,
                enter = fadeIn(animationSpec = spring(dampingRatio = 0.62f, stiffness = 220f)) + 
                        expandHorizontally(expandFrom = Alignment.CenterHorizontally, animationSpec = spring(dampingRatio = 0.62f, stiffness = 220f)) + 
                        scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = 0.52f, stiffness = 160f)),
                exit = fadeOut(animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) + 
                       shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally, animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) + 
                       scaleOut(targetScale = 0.6f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)),
                modifier = if (viewModel.isVaultCardHidden) Modifier.width(0.dp) else Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown()
                                    pressJob?.cancel()
                                    pressJob = coroutineScope.launch {
                                        delay(3000)
                                        showLongPressOptions = true
                                        try {
                                            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                            } else {
                                                vibrator.vibrate(100)
                                            }
                                        } catch (e: Exception) {}
                                    }
                                    
                                    val up = waitForUpOrCancellation()
                                    pressJob?.cancel()
                                    if (up != null && !showLongPressOptions) {
                                        // Regular click!
                                        if (!viewModel.isPrivateVaultSetup) {
                                            viewModel.showPrivateVaultPasswordTypeDialog = true
                                        } else if (viewModel.privateVaultPasswordType == "Biometric" || viewModel.privateVaultBiometricEnabled) {
                                            viewModel.biometricAuthTarget = "private_vault"
                                            viewModel.showFingerprintAuth = true
                                        } else {
                                            viewModel.showPrivateVaultUnlockDialog = true
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ProfileStatCard(
                        title = "Private Vault",
                        value = if (viewModel.isPrivateVaultSetup) "Protected" else "Locked",
                        icon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = null
                    )

                    if (showLongPressOptions) {
                        androidx.compose.ui.window.Popup(
                            alignment = Alignment.Center,
                            onDismissRequest = { showLongPressOptions = false },
                            properties = androidx.compose.ui.window.PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(136.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Hide Vault?",
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { showLongPressOptions = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(28.dp)
                                        ) {
                                            Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                showLongPressOptions = false
                                                viewModel.updateVaultCardHidden(true)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Vault hidden! Tap 3 times on the Top Sign-In Card to unlock.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(28.dp)
                                        ) {
                                            Text("Hide", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- SERVICES SECTION ---
        Text(
            text = "PREMIUM UTILITIES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                ProfileMenuItem(
                    icon = if (isGoogleSignedIn) Icons.Default.Star else Icons.Default.Lock,
                    title = "Prime Level & Badges Shop",
                    subtitle = if (isGoogleSignedIn)
                        "Active: ${viewModel.userPrimeLevel} • Upgrade badges & unlock perks"
                    else
                        "🔒 Locked • Sign in to view level, balance & badges",
                    iconTint = if (isGoogleSignedIn) Color(0xFFD97706) else Color(0xFF64748B),
                    onClick = {
                        if (isGoogleSignedIn) {
                            viewModel.showPrimeBadgesDialog = true
                        } else {
                            viewModel.showSignupScreen = true
                            Toast.makeText(context, "Sign up/in required to access Level, Balance & Badges!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                ProfileMenuItem(
                    lottieUrl = LOTTIE_OVERLAY_1_URL,
                    title = "AI Copilot & Permissions",
                    subtitle = if (viewModel.isAiAssistantEnabled) "Active • Interactive mascot & auto-redirect controls" else "Disabled • Click to configure permissions",
                    onClick = { viewModel.activeProfilePage = "ai_settings" }
                )
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                ProfileMenuItem(
                    imageUrl = "https://i.postimg.cc/G2tMPzZm/Fast-Delivery-icon-concept-in-black-duo-line-color.jpg",
                    title = "Watch Later",
                    subtitle = "${viewModel.watchLaterVideos.size} saved videos queued",
                    onClick = { viewModel.activeProfilePage = "watch_later" }
                )
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                ProfileMenuItem(
                    lottieUrl = "https://lottie.host/ad77edea-d745-4c02-9a37-18b18633daca/fhDPaqcl5V.lottie",
                    title = "Invite Friend",
                    subtitle = "Invite friends & earn free storage",
                    onClick = { viewModel.activeProfilePage = "refer" }
                )
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                ProfileMenuItem(
                    icon = Icons.Default.CloudQueue,
                    title = "Downloads",
                    subtitle = "Manage downloaded videos, music, files & folders",
                    iconTint = Color(0xFF0284C7),
                    onClick = {
                        viewModel.showFullScreenDownloads = true
                        viewModel.activeProfilePage = "downloads"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PREFERENCES SECTION ---
        Text(
            text = "SYSTEM PREFERENCES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.DeviceHub,
                    title = "Cloud Services Hub",
                    subtitle = "Manage your active external cloud portals",
                    iconTint = Color(0xFF6366F1),
                    onClick = { viewModel.showCloudHubInProfile = true }
                )
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                ProfileMenuItem(
                    icon = Icons.Default.Refresh,
                    title = "Activity Logs History",
                    subtitle = "Review recently played tracks & visited pages",
                    iconTint = Color(0xFF0284C7),
                    onClick = { viewModel.showHistoryPopup = true }
                )
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Feedback & Rating",
                    subtitle = "Submit star rating & improve Cloudihub",
                    iconTint = Color(0xFFEC4899),
                    onClick = { viewModel.showFeedbackPopup = true }
                )
            }
        }

        // Show Logout button at the very bottom of profile screen without background
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(
            onClick = {
                if (isGoogleSignedIn) {
                    viewModel.signOutGoogle()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.showSignupScreen = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isGoogleSignedIn) Icons.Default.ExitToApp else Icons.Default.Login,
                    contentDescription = if (isGoogleSignedIn) "Logout" else "Login / Sign Up",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGoogleSignedIn) "Logout" else "Log In / Sign Up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
}

