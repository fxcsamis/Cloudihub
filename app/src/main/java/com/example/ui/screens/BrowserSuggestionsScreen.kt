package com.example.ui.screens
import com.example.ui.components.rememberIosStyleFlingBehavior

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



// --- Browser tab: search suggestions panel ---
enum class SuggestionCategory(val label: String) {
    ALL("All"),
    DOMAIN("🌐 Domains"),
    NEWS("📰 News"),
    TOPIC("🔥 Topics"),
    DIRECT("⚡ Direct")
}

data class BrowserSearchSuggestion(
    val title: String,
    val subtitle: String,
    val target: String,
    val category: SuggestionCategory,
    val icon: ImageVector
)

fun generateBrowserSuggestions(query: String): List<BrowserSearchSuggestion> {
    val trimmed = query.trim()
    val suggestions = mutableListOf<BrowserSearchSuggestion>()

    if (trimmed.isEmpty()) {
        // Trending & Popular Suggestions when search bar is clicked/empty
        suggestions.add(BrowserSearchSuggestion("Google Search", "google.com • Search the Web", "https://www.google.com", SuggestionCategory.DOMAIN, Icons.Default.Search))
        suggestions.add(BrowserSearchSuggestion("YouTube", "youtube.com • Watch Videos & Shorts", "https://m.youtube.com", SuggestionCategory.DOMAIN, Icons.Default.PlayArrow))
        suggestions.add(BrowserSearchSuggestion("Yahoo Portal", "yahoo.com • News, Mail & Search", "https://www.yahoo.com", SuggestionCategory.DOMAIN, Icons.Default.Public))
        suggestions.add(BrowserSearchSuggestion("BBC World News", "bbc.com • Breaking Global Headlines", "https://www.bbc.com/news", SuggestionCategory.NEWS, Icons.Default.Newspaper))
        suggestions.add(BrowserSearchSuggestion("Trending News Today", "Top global breaking headlines & live stories", "trending news today", SuggestionCategory.NEWS, Icons.Default.TrendingUp))
        suggestions.add(BrowserSearchSuggestion("ChatGPT AI", "chatgpt.com • Smart AI Assistant", "https://chatgpt.com", SuggestionCategory.TOPIC, Icons.Default.AutoAwesome))
        suggestions.add(BrowserSearchSuggestion("Wikipedia", "wikipedia.org • Free Online Encyclopedia", "https://www.wikipedia.org", SuggestionCategory.DOMAIN, Icons.Default.MenuBook))
        suggestions.add(BrowserSearchSuggestion("Amazon Shopping", "amazon.com • Online Store & Deals", "https://www.amazon.com", SuggestionCategory.TOPIC, Icons.Default.ShoppingCart))
        return suggestions
    }

    // 1. Direct Actions for typed query
    val isUrlLike = trimmed.contains(".") && !trimmed.contains(" ")
    if (isUrlLike) {
        val targetUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
        suggestions.add(
            BrowserSearchSuggestion(
                title = "Go to $trimmed",
                subtitle = "Open website directly",
                target = targetUrl,
                category = SuggestionCategory.DIRECT,
                icon = Icons.Default.Language
            )
        )
    }

    suggestions.add(
        BrowserSearchSuggestion(
            title = "Search Google for \"$trimmed\"",
            subtitle = "Google Search",
            target = "https://www.google.com/search?q=${Uri.encode(trimmed)}",
            category = SuggestionCategory.DIRECT,
            icon = Icons.Default.Search
        )
    )

    // 2. Predefined Database of Domain, News, and Topic items
    val database = listOf(
        // Y items (YouTube, Yahoo, Yahoo News, YouTube Music, Yahoo Finance, Y2Mate, Y Combinator, Yahoo Mail, Yandex, Yelp, Y8 Games)
        BrowserSearchSuggestion("YouTube", "youtube.com • Popular Video Platform", "https://m.youtube.com", SuggestionCategory.DOMAIN, Icons.Default.PlayArrow),
        BrowserSearchSuggestion("Yahoo!", "yahoo.com • Web Portal & Search Engine", "https://www.yahoo.com", SuggestionCategory.DOMAIN, Icons.Default.Public),
        BrowserSearchSuggestion("Yahoo News", "news.yahoo.com • Breaking News & Latest Headlines", "https://news.yahoo.com", SuggestionCategory.NEWS, Icons.Default.Newspaper),
        BrowserSearchSuggestion("YouTube Music", "music.youtube.com • Trending Songs & Playlists", "https://music.youtube.com", SuggestionCategory.TOPIC, Icons.Default.MusicNote),
        BrowserSearchSuggestion("Yahoo Finance", "finance.yahoo.com • Stock Market & Live Financial News", "https://finance.yahoo.com", SuggestionCategory.NEWS, Icons.Default.TrendingUp),
        BrowserSearchSuggestion("Y2Mate Video Downloader", "y2mate.is • Fast MP4/MP3 Converter", "https://y2mate.is", SuggestionCategory.TOPIC, Icons.Default.Download),
        BrowserSearchSuggestion("Y Combinator", "ycombinator.com • Startup News & Hacker News", "https://news.ycombinator.com", SuggestionCategory.NEWS, Icons.Default.Code),
        BrowserSearchSuggestion("Yahoo Mail", "mail.yahoo.com • Free Email & Messaging", "https://mail.yahoo.com", SuggestionCategory.DOMAIN, Icons.Default.Email),
        BrowserSearchSuggestion("Yandex Search", "yandex.com • Global Search Engine & Maps", "https://yandex.com", SuggestionCategory.DOMAIN, Icons.Default.Search),
        BrowserSearchSuggestion("Yelp", "yelp.com • Local Restaurants & Reviews", "https://www.yelp.com", SuggestionCategory.TOPIC, Icons.Default.Place),
        BrowserSearchSuggestion("Y8 Games", "y8.com • Free Online Browser Games", "https://www.y8.com", SuggestionCategory.TOPIC, Icons.Default.SportsEsports),

        // G items
        BrowserSearchSuggestion("Google", "google.com • Search Engine", "https://www.google.com", SuggestionCategory.DOMAIN, Icons.Default.Search),
        BrowserSearchSuggestion("Gmail", "mail.google.com • Google Email Service", "https://mail.google.com", SuggestionCategory.DOMAIN, Icons.Default.Email),
        BrowserSearchSuggestion("Google News", "news.google.com • Top Live World Headlines", "https://news.google.com", SuggestionCategory.NEWS, Icons.Default.Newspaper),
        BrowserSearchSuggestion("Google Maps", "maps.google.com • Navigation & Live Traffic", "https://maps.google.com", SuggestionCategory.TOPIC, Icons.Default.Map),
        BrowserSearchSuggestion("GitHub", "github.com • Software Code & Developer Projects", "https://github.com", SuggestionCategory.DOMAIN, Icons.Default.Code),

        // A items
        BrowserSearchSuggestion("Amazon", "amazon.com • World's Largest Online Store", "https://www.amazon.com", SuggestionCategory.DOMAIN, Icons.Default.ShoppingCart),
        BrowserSearchSuggestion("Apple", "apple.com • iPhones, Mac & iOS Updates", "https://www.apple.com", SuggestionCategory.DOMAIN, Icons.Default.PhoneIphone),
        BrowserSearchSuggestion("Al Jazeera News", "aljazeera.com • Live International Headlines", "https://www.aljazeera.com", SuggestionCategory.NEWS, Icons.Default.Newspaper),

        // F items
        BrowserSearchSuggestion("Facebook", "facebook.com • Social Media & Stories", "https://www.facebook.com", SuggestionCategory.DOMAIN, Icons.Default.People),
        BrowserSearchSuggestion("Fox News", "foxnews.com • U.S. & Politics Breaking News", "https://www.foxnews.com", SuggestionCategory.NEWS, Icons.Default.Newspaper),

        // N items
        BrowserSearchSuggestion("Netflix", "netflix.com • Watch Movies & TV Shows", "https://www.netflix.com", SuggestionCategory.DOMAIN, Icons.Default.Movie),
        BrowserSearchSuggestion("New York Times", "nytimes.com • World News & Editorial Articles", "https://www.nytimes.com", SuggestionCategory.NEWS, Icons.Default.Newspaper),

        // W items
        BrowserSearchSuggestion("Wikipedia", "wikipedia.org • Free Online Encyclopedia", "https://www.wikipedia.org", SuggestionCategory.DOMAIN, Icons.Default.MenuBook),
        BrowserSearchSuggestion("WhatsApp Web", "web.whatsapp.com • Instant Web Messaging", "https://web.whatsapp.com", SuggestionCategory.TOPIC, Icons.Default.Chat),
        BrowserSearchSuggestion("Weather Channel", "weather.com • Live Weather & Storm Forecast", "https://weather.com", SuggestionCategory.NEWS, Icons.Default.WbSunny),

        // C items
        BrowserSearchSuggestion("ChatGPT", "chatgpt.com • OpenAI Smart Conversational Assistant", "https://chatgpt.com", SuggestionCategory.TOPIC, Icons.Default.AutoAwesome),
        BrowserSearchSuggestion("CNN News", "cnn.com • Breaking News, Video & Live Audio", "https://www.cnn.com", SuggestionCategory.NEWS, Icons.Default.Newspaper),

        // S items
        BrowserSearchSuggestion("Spotify", "spotify.com • Listen to Music & Podcasts", "https://open.spotify.com", SuggestionCategory.DOMAIN, Icons.Default.MusicNote),
        BrowserSearchSuggestion("SoundCloud", "soundcloud.com • Free Audio & Tracks", "https://soundcloud.com", SuggestionCategory.TOPIC, Icons.Default.GraphicEq),

        // T items
        BrowserSearchSuggestion("X (formerly Twitter)", "x.com • Trending Social Posts & News", "https://x.com", SuggestionCategory.DOMAIN, Icons.Default.Tag),
        BrowserSearchSuggestion("TikTok", "tiktok.com • Short Videos & Trending Clips", "https://www.tiktok.com", SuggestionCategory.TOPIC, Icons.Default.Videocam)
    )

    // Filter database items by matching query
    val qLower = trimmed.lowercase()
    val matchedFromDb = database.filter {
        it.title.lowercase().contains(qLower) ||
        it.subtitle.lowercase().contains(qLower) ||
        it.target.lowercase().contains(qLower)
    }

    suggestions.addAll(matchedFromDb)

    // 3. Dynamic Topic / News / Domain Fallbacks if typed query is long or unique
    if (matchedFromDb.isEmpty() || trimmed.length >= 2) {
        val cleanQuery = trimmed.replace("https://", "").replace("http://", "").replace("www.", "")

        // Domain suggestion
        val domainTarget = if (cleanQuery.contains(".")) "https://$cleanQuery" else "https://www.$cleanQuery.com"
        val domainTitle = if (cleanQuery.contains(".")) cleanQuery else "$cleanQuery.com"
        if (suggestions.none { it.target.equals(domainTarget, ignoreCase = true) }) {
            suggestions.add(
                BrowserSearchSuggestion(
                    title = domainTitle,
                    subtitle = "Domain website • Visit $domainTitle",
                    target = domainTarget,
                    category = SuggestionCategory.DOMAIN,
                    icon = Icons.Default.Public
                )
            )
        }

        // News suggestion
        suggestions.add(
            BrowserSearchSuggestion(
                title = "$trimmed News & Updates",
                subtitle = "Latest breaking news & articles about $trimmed",
                target = "https://www.google.com/search?q=${Uri.encode("$trimmed news")}",
                category = SuggestionCategory.NEWS,
                icon = Icons.Default.Newspaper
            )
        )

        // Topic suggestion
        suggestions.add(
            BrowserSearchSuggestion(
                title = "$trimmed Trending Topics",
                subtitle = "Popular topics, videos & discussions for $trimmed",
                target = "https://www.google.com/search?q=${Uri.encode("$trimmed trending topics")}",
                category = SuggestionCategory.TOPIC,
                icon = Icons.Default.TrendingUp
            )
        )
    }

    return suggestions.distinctBy { it.title + it.target }
}

@Composable
fun BrowserSearchSuggestionsPanel(
    query: String,
    isIncognito: Boolean,
    onSelectSuggestion: (String) -> Unit,
    onPasteSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(SuggestionCategory.ALL) }
    val allSuggestions = remember(query) { generateBrowserSuggestions(query) }

    val filteredSuggestions = remember(allSuggestions, selectedCategory) {
        if (selectedCategory == SuggestionCategory.ALL) {
            allSuggestions
        } else {
            allSuggestions.filter { it.category == selectedCategory || it.category == SuggestionCategory.DIRECT }
        }
    }

    val panelBgColor = if (isIncognito) Color(0xFF1E1B4B) else Color(0xFFF8FAFC)
    val cardBgColor = if (isIncognito) Color(0xFF312E81) else Color.White
    val cardBorderColor = if (isIncognito) Color(0xFF4C1D95) else Color(0xFFE2E8F0)
    val titleTextColor = if (isIncognito) Color(0xFFF3E8FF) else Color(0xFF0F172A)
    val subtitleTextColor = if (isIncognito) Color(0xFFC084FC) else Color(0xFF64748B)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(panelBgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // --- CATEGORY FILTER CHIPS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SuggestionCategory.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                val chipBg = if (isSelected) {
                    if (isIncognito) Color(0xFF9333EA) else Color(0xFF0284C7)
                } else {
                    if (isIncognito) Color(0xFF312E81) else Color(0xFFE2E8F0)
                }
                val chipText = if (isSelected) Color.White else if (isIncognito) Color(0xFFE9D5FF) else Color(0xFF334155)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = chipBg,
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat.label,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = chipText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- SEARCH SUGGESTIONS LIST ---
        if (filteredSuggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No search suggestions found for \"$query\"",
                    fontSize = 12.5.sp,
                    color = subtitleTextColor,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                flingBehavior = rememberIosStyleFlingBehavior(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredSuggestions, key = { it.hashCode() }) { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBgColor,
                        border = BorderStroke(1.dp, cardBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSuggestion(suggestion.target)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            val iconBg = when (suggestion.category) {
                                SuggestionCategory.DIRECT -> if (isIncognito) Color(0xFF581C87) else Color(0xFFE0F2FE)
                                SuggestionCategory.DOMAIN -> if (isIncognito) Color(0xFF4C1D95) else Color(0xFFF0FDF4)
                                SuggestionCategory.NEWS -> if (isIncognito) Color(0xFF831843) else Color(0xFFFEF2F2)
                                SuggestionCategory.TOPIC -> if (isIncognito) Color(0xFF701A75) else Color(0xFFFFF7ED)
                                else -> if (isIncognito) Color(0xFF3B0764) else Color(0xFFF1F5F9)
                            }
                            val iconTint = when (suggestion.category) {
                                SuggestionCategory.DIRECT -> if (isIncognito) Color(0xFFC084FC) else Color(0xFF0284C7)
                                SuggestionCategory.DOMAIN -> if (isIncognito) Color(0xFF4ADE80) else Color(0xFF16A34A)
                                SuggestionCategory.NEWS -> if (isIncognito) Color(0xFFF43F5E) else Color(0xFFE11D48)
                                SuggestionCategory.TOPIC -> if (isIncognito) Color(0xFFF97316) else Color(0xFFEA580C)
                                else -> if (isIncognito) Color(0xFFA855F7) else Color(0xFF64748B)
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = suggestion.icon,
                                    contentDescription = suggestion.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Text Column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = suggestion.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = suggestion.subtitle,
                                    fontSize = 11.sp,
                                    color = subtitleTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Fill / Paste arrow icon button to append to search bar
                            IconButton(
                                onClick = {
                                    onPasteSuggestion(suggestion.title)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallMade,
                                    contentDescription = "Paste to search bar",
                                    tint = subtitleTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


