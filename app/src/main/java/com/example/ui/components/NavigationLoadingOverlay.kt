package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImage

/**
 * Lightweight, CLOUDIHUB-branded full-screen loading state shown while a
 * newly-selected tab/screen is preparing its content (data + interactive
 * state) in the background. Kept visually consistent with [CloudeHubSplashScreen]
 * (same palette, same logo) but intentionally simple/cheap to draw since this
 * can appear on every single navigation tap.
 */
@Composable
fun CloudihubNavigationLoadingOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "NavLoaderInfinite")

    // Gentle pulse on the logo only - single cheap animated float, no path work.
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE0F2FE),
                        Color(0xFFF1F5F9)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand logo (same asset as splash screen, smaller here since this
            // shows briefly and frequently rather than once per app launch)
            AsyncImage(
                model = com.example.R.drawable.cloudihub_logo_1784004021392,
                contentDescription = "CLOUDIHUB",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(72.dp)
                    .alpha(pulseAlpha)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CLOUDIHUB",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 3.sp,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Standard indeterminate spinner - already animates itself, no
            // extra manual rotation logic needed.
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF38BDF8),
                strokeWidth = 2.dp,
                trackColor = Color(0xFFE0F2FE)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "loading",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
