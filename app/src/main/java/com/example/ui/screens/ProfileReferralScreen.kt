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

// --- Profile tab: Refer-a-friend sub-screen + its QR code view ---
// --- SUB-SCREEN: INVITE FRIEND ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferScreen(viewModel: CloudihubViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val inviteLink = "https://cloudihub.com/invite/aaliyah_71"
    var showQrSheet by remember { mutableStateOf(false) }
    val referPagerState = rememberPagerState(pageCount = { 2 })
    val referScope = rememberCoroutineScope()
    val selectedTab = referPagerState.currentPage

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

    // Re-key animation each time this composable enters composition so it restarts from frame 0 and freezes on last frame
    val playKey = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        // Toolbar (Seamless background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.activeProfilePage = "main" },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Invite Friend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Main Top Lottie Animation Container (300x300dp) - Freezes on last frame
                key(playKey) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DotLottieAnimation(
                            source = DotLottieSource.Url("https://lottie.host/ad77edea-d745-4c02-9a37-18b18633daca/fhDPaqcl5V.lottie"),
                            autoplay = true,
                            loop = false,
                            modifier = Modifier.size(280.dp)
                        )
                    }
                }

                // Referral Earn Balance Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .border(1.5.dp, Color(0xFFA7F3D0), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "Wallet",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Referral Wallet Balance",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = viewModel.userBalance,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Text(
                                    text = "${viewModel.referralCount} Invites",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.addReferralReward()
                                    Toast.makeText(context, "Referral success! +$5.00 added to wallet balance!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Text("Simulate Invite (+$5.00)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.showPrimeBadgesDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFD97706))
                            ) {
                                Text("Buy Prime Badges", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Row: Clean, NO CIRCLES / NO BORDER CONTAINERS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Copy Link Action (Clean Aesthetic Icon)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(inviteLink))
                                Toast.makeText(context, "Invite link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Copy Link",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }

                    // 2. QR Code Action (Lottie)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showQrSheet = true
                            }
                            .padding(12.dp)
                    ) {
                        DotLottieAnimation(
                            source = DotLottieSource.Url("https://lottie.host/3d35cfc5-5c28-46d5-8f17-326b5cfa85c3/f7QNSkcuIe.lottie"),
                            autoplay = true,
                            loop = true,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "QR Code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }

                    // 3. Share Action (Lottie from Video Playback Share Button)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Join Cloudihub")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join Cloudihub for high-speed cloud storage and video streaming: $inviteLink")
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Invite Link"))
                            }
                            .padding(12.dp)
                    ) {
                        DotLottieAnimation(
                            source = DotLottieSource.Url("https://lottie.host/0753d442-1101-40ac-a517-f21d4be97ef8/D1QEnYRtVw.lottie"),
                            autoplay = true,
                            loop = true,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Detailed Modern Instructions & Features Card (NO EMOJIS)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Referral Program Overview",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Invite friends to expand your personal storage quota and access shared high-speed media streaming features.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Program Instructions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val instructions = listOf(
                            "Step 1: Copy your referral link or display the QR code using the action buttons above.",
                            "Step 2: Share the link or QR code with friends, family, or colleagues.",
                            "Step 3: Once your invitee registers a Cloudihub account using your link, verified quota bonus is credited automatically.",
                            "Step 4: You receive 5 GB of extra cloud storage for each verified referral, up to 100 GB total bonus."
                        )

                        instructions.forEach { item ->
                            Text(
                                text = item,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Technical Details & Rules",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val details = listOf(
                            "Quota Credit: Instant allocation upon unique device registration.",
                            "Bandwidth Tier: Unlocks high-priority network nodes for video streaming and file transfers.",
                            "Privacy Protection: Invitee data remains private and strictly end-to-end encrypted.",
                            "Expiration: Bonus cloud storage allocated via referral is permanent and never expires."
                        )

                        details.forEach { detail ->
                            Text(
                                text = detail,
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- NEW BOTTOM SECTION: MY FRIENDS & TOP REFERRERS TABBED SLIDER ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Small dushor/grey switcher buttons with icons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .padding(3.dp)
                        ) {
                            // Tab 1: My Friends
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTab == 0) Color(0xFFCBD5E1) else Color.Transparent
                                    )
                                    .clickable {
                                        referScope.launch { referPagerState.animateScrollToPage(0) }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = "My Friends",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "My Friends",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }

                            // Tab 2: Top Referrers
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTab == 1) Color(0xFFCBD5E1) else Color.Transparent
                                    )
                                    .clickable {
                                        referScope.launch { referPagerState.animateScrollToPage(1) }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Top Referrers",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Top Referrers",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalPager(
                            state = referPagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            if (page == 0) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val myFriends = listOf(
                                        Triple("Sadia Islam", "https://i.postimg.cc/1zmgjdRt/f6d0aee22a954d7db19f3c210f9d876e.jpg", "Joined 2 days ago"),
                                        Triple("Tanvir Ahmed", "https://i.postimg.cc/s2xM1LhJ/avatar1.jpg", "Joined 5 days ago"),
                                        Triple("Nabil Chowdhury", "https://i.postimg.cc/44yJ3mpt/avatar2.jpg", "Joined 1 week ago")
                                    )

                                    Text(
                                        text = "Invited Friends (${myFriends.size})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    myFriends.forEachIndexed { index, friend ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = friend.second,
                                                contentDescription = friend.first,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = friend.first,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = friend.third,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Color(0xFFECFDF5))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "+5 GB Active",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF059669)
                                                )
                                            }
                                        }
                                        if (index < myFriends.size - 1) {
                                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                        }
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val topReferrers = listOf(
                                        Quadruple("#1", "Zubair Rahman", "48 Invites • 240 GB Earned", Color(0xFFF59E0B)),
                                        Quadruple("#2", "Fariha Chowdhury", "35 Invites • 175 GB Earned", Color(0xFF94A3B8)),
                                        Quadruple("#3", "Mahmudul Hasan", "28 Invites • 140 GB Earned", Color(0xFFD97706)),
                                        Quadruple("#4", "Anika Tabassum", "21 Invites • 105 GB Earned", Color(0xFF64748B))
                                    )

                                    Text(
                                        text = "Global Leaderboard",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    topReferrers.forEachIndexed { index, user ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(user.fourth.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = user.first,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = user.fourth
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = user.second,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = user.third,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0284C7),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        if (index < topReferrers.size - 1) {
                                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

    // Dynamic Sliding Bottom Sheet Modal for QR Code with Glowing Blue Shadow
    if (showQrSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQrSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scan QR Code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(
                        onClick = { showQrSheet = false },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Sheet",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Highlighted floating QR code with glowing blue shadow effect
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0xFF0284C7),
                            ambientColor = Color(0xFF0284C7)
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF0F9FF), Color.White)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VisualQRCode(modifier = Modifier.size(170.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "How to Scan & Claim Bonus",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val scanSteps = listOf(
                    "Step 1: Open the Camera application or any QR Code scanner on another mobile device.",
                    "Step 2: Position the camera lens over the highlighted QR code above.",
                    "Step 3: Tap the detected link banner to accept the invite and instantly claim 5 GB bonus cloud storage."
                )

                scanSteps.forEach { step ->
                    Text(
                        text = step,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showQrSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Close", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun VisualQRCode(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(160.dp)
            .background(Color.White)
            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(5) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(5) { colIndex ->
                        val isCorner = (rowIndex == 0 && colIndex == 0) || 
                                       (rowIndex == 0 && colIndex == 4) || 
                                       (rowIndex == 4 && colIndex == 0)
                        val isCenterDot = rowIndex == 2 && colIndex == 2
                        val isRandomDot = (rowIndex + colIndex) % 3 == 0
                        
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(if (isCorner) 6.dp else 2.dp))
                                .background(
                                    if (isCorner) Color(0xFF0F172A)
                                    else if (isCenterDot) Color(0xFF0284C7)
                                    else if (isRandomDot) Color(0xFF334155)
                                    else Color(0xFFF1F5F9)
                                )
                        ) {
                            if (isCorner) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.Center)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .align(Alignment.Center)
                                            .background(Color(0xFF0F172A), CircleShape)
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

// --- SUB-SCREEN: OFFLINE DOWNLOADS SCREEN ---
// Handled by com.example.ui.screens.DownloadsScreen.kt

// --- SLIDING BOTTOM SHEETS (ModalBottomSheet with native drag handles to slide down & close) ---
