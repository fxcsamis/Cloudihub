package com.example.ui.screens

import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource

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

// --- Profile tab: small reusable pieces (stat card, menu item, theme toggle, signup banner) ---
@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val finalModifier = if (onClick != null) {
        modifier
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
    } else {
        modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
    }
    
    Card(
        modifier = finalModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F9FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector? = null,
    imageUrl: String? = null,
    lottieUrl: String? = null,
    title: String,
    subtitle: String,
    iconTint: Color = Color(0xFF475569),
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            if (!lottieUrl.isNullOrEmpty()) {
                // BUG FIX: this used to ignore the lottieUrl parameter entirely and
                // always show a generic loading spinner instead of the actual small
                // preview animation (e.g. the Invite Friend icon). Now it plays the
                // real animation the URL points to, same as the full-size version
                // used on the destination screen (frozen on its last frame once
                // done, so it reads as a static icon rather than a looping spinner).
                DotLottieAnimation(
                    source = DotLottieSource.Url(lottieUrl),
                    autoplay = true,
                    loop = false,
                    modifier = Modifier.size(36.dp)
                )
            } else if (!imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}

class WavyBannerShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val r = 24.dp.value * density.density // Corner radius
            val w = size.width
            val h = size.height
            val dip = 32.dp.value * density.density // Vertical shift for the curve
            
            // Top-left corner
            moveTo(0f, r)
            quadraticTo(0f, 0f, r, 0f)
            
            // Top edge to the start of the curve (around 68% of width)
            lineTo(w * 0.68f, 0f)
            
            // Elegant S-curve down to the lower level
            cubicTo(
                w * 0.74f, 0f,
                w * 0.76f, dip,
                w * 0.82f, dip
            )
            
            // Straight edge of the lower shelf to the top-right corner
            lineTo(w - r, dip)
            quadraticTo(w, dip, w, dip + r)
            
            // Right edge down to bottom-right corner
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h)
            
            // Bottom edge to bottom-left corner
            lineTo(r, h)
            quadraticTo(0f, h, 0f, h - r)
            
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CustomThemeToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offset by animateDpAsState(targetValue = if (isDark) 26.dp else 2.dp, label = "toggleOffset")
    val bgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val circleColor = if (isDark) Color(0xFF64748B) else Color(0xFFFBBF24)
    
    Box(
        modifier = modifier
            .width(54.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFE2E8F0), CircleShape)
            .clickable { onToggle() }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background icons
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = if (isDark) Color(0xFF64748B) else Color(0xFFFBBF24),
                modifier = Modifier.size(12.dp)
            )
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = null,
                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                modifier = Modifier.size(12.dp)
            )
        }
        
        // Sliding handle
        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(24.dp)
                .clip(CircleShape)
                .background(circleColor)
                .shadow(1.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.NightsStay else Icons.Default.WbSunny,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun AestheticBannerButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDark: Boolean,
    contentColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "buttonScale"
    )
    
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val buttonBg = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF0F9FF)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE0F2FE)
    val iconColor = if (isDark) Color.White else Color(0xFF0284C7)
    val textColor = if (isDark) Color.White else Color(0xFF0284C7)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(buttonBg)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun WavySignupBanner(
    viewModel: CloudihubViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme
    
    // Glowing sky color shimmer transition mimicking Gemini's premium glow in sky blue!
    val infiniteTransition = rememberInfiniteTransition(label = "SkyGlow")
    val glowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlowProgress"
    )
    
    // We animate the gradient positions to create a continuous fluid flash/glow flow in Sky Color
    val glowBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0284C7).copy(alpha = 0.2f),
            Color(0xFF38BDF8),
            Color(0xFF0EA5E9),
            Color(0xFF38BDF8),
            Color(0xFF0284C7).copy(alpha = 0.2f)
        ),
        start = Offset(glowProgress * 1000f - 500f, 0f),
        end = Offset(glowProgress * 1000f + 500f, 500f)
    )

    val cardBg = if (isDark) Color(0xFF0F172A).copy(alpha = 0.25f) else Color.White
    val contentColor = if (isDark) Color.White else Color(0xFF334155)
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFE2E8F0)
    
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.8.dp, glowBrush, RoundedCornerShape(24.dp))
            .shadow(if (isDark) 12.dp else 4.dp, RoundedCornerShape(24.dp), clip = false)
            .clickable {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTapTime < 500) {
                    tapCount += 1
                } else {
                    tapCount = 1
                }
                lastTapTime = currentTime
                if (tapCount == 3) {
                    tapCount = 0
                    if (viewModel.isVaultCardHidden) {
                        try {
                            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(100)
                            }
                        } catch (e: Exception) {}
                        
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
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
        ) {
            // Top row with Sign In on Left and 3 Tiny Badges on Right Edge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sign In Button (Pill shaped with accent outline)
                Box(
                    modifier = Modifier
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF0F9FF), CircleShape)
                        .border(1.5.dp, if (isDark) accentColor else Color(0xFFBAE6FD), CircleShape)
                        .clip(CircleShape)
                        .clickable { viewModel.showSignupScreen = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sign Up",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign up",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDark) contentColor else accentColor
                        )
                    }
                }
                
                // 3 Tiny Badges (Level, Prime, Balance) on top right edge
                UserProfileMetricsRow(viewModel = viewModel)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Horizontal divider line
            Divider(
                color = borderColor,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Bottom buttons (Aesthetic, left-aligned)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AestheticBannerButton(
                    icon = Icons.Default.FolderOpen,
                    text = "My Files",
                    onClick = { viewModel.activeProfilePage = "downloads" },
                    isDark = isDark,
                    contentColor = contentColor
                )
                
                AestheticBannerButton(
                    icon = Icons.Default.History,
                    text = "History",
                    onClick = { viewModel.showHistoryPopup = true },
                    isDark = isDark,
                    contentColor = contentColor
                )
                
                AestheticBannerButton(
                    icon = Icons.Default.QueueMusic,
                    text = "Playlist",
                    onClick = { viewModel.selectTab(NavigationTab.Music) },
                    isDark = isDark,
                    contentColor = contentColor
                )
            }
        }
    }
}


// =========================================================================
// BIO-AUTHENTICATOR & REMOTE DEVICE LOGOUT FLOW (AS REQUESTED)
// =========================================================================

