package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.ui.components.NavigationTab
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.CloudihubViewModel
import com.example.ui.components.CloudSkyBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.ByteArrayInputStream



// --- Browser tab: bookmark tiles + add-bookmark dialog ---
@Composable
fun BookmarkTile(
    bookmark: CloudihubViewModel.BrowserBookmark,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isIncognito: Boolean = false
) {
    // Elegant styling picked dynamically based on popular domain name as fallback
    val (bgColor, textColor, charLabel) = remember(bookmark.name) {
        val nameLower = bookmark.name.lowercase()
        when {
            nameLower.contains("google") -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "G")
            nameLower.contains("facebook") -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "F")
            nameLower.contains("youtube") -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "Y")
            nameLower.contains("wikipedia") -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), "W")
            nameLower.contains("amazon") -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "A")
            nameLower.contains("instagram") -> Triple(Color(0xFFFCE7F3), Color(0xFFBE185D), "I")
            nameLower.contains("linkedin") -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "L")
            else -> {
                val firstChar = bookmark.name.firstOrNull()?.uppercase() ?: "B"
                Triple(Color(0xFFF1F5F9), Color(0xFF475569), firstChar)
            }
        }
    }

    var imageLoadFailed by remember { mutableStateOf(false) }
    val faviconUrl = remember(bookmark.url) {
        "https://www.google.com/s2/favicons?sz=128&domain=${getDomainName(bookmark.url)}"
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = if (isIncognito) Color(0xFFC084FC) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(4.dp)
    ) {
        // Slim elegant delete button on top right
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9).copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete bookmark",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(7.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Colored logo circular container
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (imageLoadFailed) bgColor else Color(0xFFF8FAFC)), // clean modern backdrop for logo
                contentAlignment = Alignment.Center
            ) {
                if (!imageLoadFailed) {
                    AsyncImage(
                        model = faviconUrl,
                        contentDescription = "${bookmark.name} logo",
                        modifier = Modifier
                            .size(20.dp) // Compact scaling inside circle
                            .clip(CircleShape),
                        onSuccess = {
                            // Image loaded successfully!
                        },
                        onError = {
                            imageLoadFailed = true // Fall back to letter design
                        }
                    )
                } else {
                    Text(
                        text = charLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bookmark.name,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AddBookmarkTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isIncognito: Boolean = false
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = if (isIncognito) Color(0xFFC084FC) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant "+" circular container
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isIncognito) Color(0xFFF3E8FF) else Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Site",
                    tint = if (isIncognito) Color(0xFF9333EA) else Color(0xFF0284C7),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add Site",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isIncognito) Color(0xFF7E22CE) else Color(0xFF475569),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookmarkDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var animateShow by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // Synchronize with visible state
    LaunchedEffect(visible) {
        if (visible) {
            animateShow = true
            isVisible = true
        } else {
            isVisible = false
        }
    }

    // When exit animation finishes, hide from composition
    LaunchedEffect(isVisible) {
        if (!isVisible && animateShow) {
            delay(220)
            animateShow = false
        }
    }

    val scope = rememberCoroutineScope()
    val dismissWithAnimation = {
        scope.launch {
            isVisible = false
            delay(220)
            onDismiss()
        }
    }

    if (animateShow) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { dismissWithAnimation() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val backdropAlpha by animateFloatAsState(
                targetValue = if (isVisible) 0.5f else 0f,
                animationSpec = tween(durationMillis = 200, easing = EaseOutQuad),
                label = "BookmarkBackdropAlpha"
            )
            
            val scale by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0.82f,
                animationSpec = spring(
                    dampingRatio = 0.72f, // smooth, bouncy iOS feel
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "BookmarkContentScale"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 180),
                label = "BookmarkContentAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(Color.Black.copy(alpha = backdropAlpha))
                    }
                    .clickable { dismissWithAnimation() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .scale(scale)
                        .graphicsLayer(alpha = alpha)
                        .clickable(enabled = false) {} // Prevent click-through closing
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Add to Favorites",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = "Add your favorite website to the home tiles for super-fast cloud access.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 18.sp
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Site Name (e.g. Wikipedia)", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                cursorColor = Color(0xFF0284C7)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        val clipboardManager = LocalClipboardManager.current

                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            placeholder = { Text("Website URL (e.g. wikipedia.org)", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        clipboardManager.getText()?.let {
                                            url = it.text.trim()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste Clipboard",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                cursorColor = Color(0xFF0284C7)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (errorMsg.isNotEmpty()) {
                            Text(
                                text = errorMsg,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { dismissWithAnimation() }) {
                                Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (name.trim().isEmpty() || url.trim().isEmpty()) {
                                        errorMsg = "Please fill in all fields"
                                    } else {
                                        onAdd(name.trim(), url.trim())
                                        dismissWithAnimation()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add Favorite", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdultSiteBrandTile(
    name: String,
    url: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    isIncognito: Boolean = false
) {
    var imageLoadFailed by remember { mutableStateOf(false) }
    val faviconUrl = remember(url) {
        "https://www.google.com/s2/favicons?sz=128&domain=${getDomainName(url)}"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, if (isIncognito) Color(0xFFC084FC) else Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = faviconUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                onError = { imageLoadFailed = true }
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = name,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

