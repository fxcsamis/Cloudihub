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

// --- Profile tab: biometric auth dialog, linked devices, offline folders, iOS-style sheet ---
@Composable
fun BiometricAuthDialog(viewModel: CloudihubViewModel) {
    var isPulsing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    LaunchedEffect(Unit) {
        isPulsing = true
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { viewModel.showFingerprintAuth = false }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC).copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Pulse fingerprint icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                        .clickable {
                            // Simulate successful auth!
                            viewModel.showFingerprintAuth = false
                            viewModel.activeProfilePage = "linked_devices"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Scan Fingerprint",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Biometric Verification",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Touch and hold the fingerprint scanner or tap below to authenticate and manage active sessions securely.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.showFingerprintAuth = false
                        viewModel.activeProfilePage = "linked_devices"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Authenticate Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.showFingerprintAuth = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedDevicesScreen(viewModel: CloudihubViewModel) {
    val context = LocalContext.current
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var deviceToLogOut by remember { mutableStateOf<CloudihubViewModel.LinkedDevice?>(null) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.activeProfilePage = "main" },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Linked Active Devices",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        // Subheader intro banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "These devices are currently signed into your Cloudihub account. You can remotely revoke access to secure your cloud pipelines.",
                    fontSize = 12.sp,
                    color = Color(0xFF0369A1),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Devices List
        // Devices List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.linkedDevices) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .shadow(2.dp, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Hardware device picture
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(device.imageUrl),
                                    contentDescription = "Device Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = device.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (device.isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFDCFCE7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Current",
                                                color = Color(0xFF15803D),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                // OS and details badge
                                Text(
                                    text = device.os,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0284C7)
                                )
                                
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Text(
                                    text = "${device.lastActive} • ${device.location}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (!device.isCurrent) {
                            Button(
                                onClick = {
                                    deviceToLogOut = device
                                    newPassword = ""
                                    confirmPassword = ""
                                    showPasswordChangeDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEE2E2),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Logout",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // iOS-Style Bottom Sheet Verification for Logout / Password Change
    if (showPasswordChangeDialog && deviceToLogOut != null) {
        ModalBottomSheet(
            onDismissRequest = { showPasswordChangeDialog = false },
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .size(width = 44.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Security Verification",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "To securely sign out '${deviceToLogOut?.name}', please set a new account password. This ensures other active sessions are safely terminated and revoked.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                // Beautiful high-contrast device detail preview card as requested
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(deviceToLogOut?.imageUrl),
                                contentDescription = "Device Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = deviceToLogOut?.name ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = deviceToLogOut?.os ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${deviceToLogOut?.lastActive ?: ""} • ${deviceToLogOut?.location ?: ""}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Secure Password", fontSize = 12.sp) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Secure Password", fontSize = 12.sp) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showPasswordChangeDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // Perform Logout
                            viewModel.linkedDevices.removeIf { it.id == deviceToLogOut?.id }
                            showPasswordChangeDialog = false
                            Toast.makeText(context, "Password changed. Remote session revoked successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Change & Logout", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


// =========================================================================
// OFFLINE MODERN CLOUD FOLDERS (AS REQUESTED)
// =========================================================================

@Composable
fun ModernFolderIcon(
    baseColor: Color = Color(0xFFFBBF24), // Folder yellow/amber
    icon: ImageVector
) {
    Box(
        modifier = Modifier.size(54.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Drawing a real 3D looking folder!
        // 1. Folder Back Flap (Darker shade)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(baseColor.copy(alpha = 0.75f))
        )
        
        // 2. Folder Tab (Top Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp)
                .size(width = 24.dp, height = 14.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(baseColor.copy(alpha = 0.75f))
        )
        
        // 3. Papers peeking out (White sheet)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        )
        
        // 4. Folder Front Flap (Lighter shade, slanted slightly or overlapping)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(baseColor)
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(topStart = 4.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .shadow(2.dp, RoundedCornerShape(topStart = 4.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
        ) {
            // Little icon in center of front flap
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun OfflineFoldersScreen(viewModel: CloudihubViewModel) {
    val context = LocalContext.current
    var activeFolderTag by remember { mutableStateOf<String?>(null) } // null means showing folder grid, non-null shows files inside folder
    var activeFolderName by remember { mutableStateOf("") }

    // Mock folder files
    val portalBackups = listOf(
        "DriveBackup_2026_07.zip" to "1.2 GB",
        "Dropbox_Synced_Media.tar" to "850 MB",
        "OneDrive_Archived.zip" to "350 MB"
    )
    val tempCaches = listOf(
        "HlsChunk_Node_18204.log" to "32 MB",
        "HlsChunk_Node_18205.log" to "28 MB",
        "ProxyPipeline_Cache.bin" to "152 MB",
        "AestheticAmbiance_Index.json" to "12 KB"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (activeFolderTag != null) {
                        activeFolderTag = null
                    } else {
                        viewModel.activeProfilePage = "main"
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (activeFolderTag != null) activeFolderName else "Cloude Offline Hub",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        if (activeFolderTag == null) {
            // FOLDERS VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // Introductory banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CloudeHub high-speed offline directory. All folders are secure, sandboxed, and locally cached.",
                            fontSize = 12.sp,
                            color = Color(0xFF0284C7),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of folders with cloud-themed styling
                val folders = listOf(
                    Triple("Cloud Backups", "3 archives • 2.4 GB", "backup"),
                    Triple("Saved Streams", "${viewModel.downloadItems.filter { it.type == "Video" }.size} files • 1.8 GB", "video"),
                    Triple("Music & Melodies", "${viewModel.downloadItems.filter { it.type == "Music" }.size} tracks • 256 MB", "music"),
                    Triple("Private Keys & Certs", "${viewModel.downloadItems.filter { it.type == "Private" }.size} keys • 12 KB", "key"),
                    Triple("Temporary Cache Logs", "4 logs • 212 MB", "cache")
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(folders) { folder ->
                        val (name, desc, tag) = folder
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeFolderTag = tag
                                    activeFolderName = name
                                }
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (baseColor, icon) = when (tag) {
                                    "backup" -> Color(0xFFFBBF24) to Icons.Default.CloudDownload
                                    "video" -> Color(0xFF38BDF8) to Icons.Default.Movie
                                    "music" -> Color(0xFFF472B6) to Icons.Default.MusicNote
                                    "key" -> Color(0xFF34D399) to Icons.Default.Lock
                                    else -> Color(0xFFA78BFA) to Icons.Default.FolderOpen
                                }

                                ModernFolderIcon(
                                    baseColor = baseColor,
                                    icon = icon
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // INSIDE FOLDER VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Info description of active folder
                Text(
                    text = "Directory: storage/cloudehub/$activeFolderTag/",
                    fontSize = 11.sp,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val folderTag = activeFolderTag
                val itemsList = if (folderTag == "video") {
                    viewModel.downloadItems.filter { it.type == "Video" }
                } else if (folderTag == "music") {
                    viewModel.downloadItems.filter { it.type == "Music" }
                } else if (folderTag == "key") {
                    viewModel.downloadItems.filter { it.type == "Private" }
                } else {
                    emptyList()
                }

                if (folderTag == "backup" || folderTag == "cache") {
                    val staticList = if (folderTag == "backup") portalBackups else tempCaches
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(staticList) { item ->
                            val (filename, size) = item
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = filename,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "Local Cache Store",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                    Text(
                                        text = size,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else if (itemsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "This folder is empty.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(itemsList) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1E293B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Downloaded Stream",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.size,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.Bold
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

// ==========================================
// --- PRIVATE VAULT COMPOSABLES (M3) ---
// ==========================================

@Composable
fun IOSBottomSheet(
    onDismissRequest: () -> Unit,
    viewModel: CloudihubViewModel,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() },
            contentAlignment = Alignment.BottomCenter
        ) {
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                ) + fadeOut(),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent propagation */ }
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(), // Full width to touch/blend with screen edges perfectly
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), // Premium iOS-style rounded sheet
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkTheme) Color(0xFF0F172A) else Color.White),
                    border = BorderStroke(1.dp, if (viewModel.isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding() // CRITICAL: Smoothly pushes keyboard content up inside card
                            .padding(bottom = 16.dp) // Comfort space so fields are well above keyboard
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

