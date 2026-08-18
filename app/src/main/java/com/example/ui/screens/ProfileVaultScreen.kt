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

// --- Profile tab: Private Vault (lock screens, folder management, unlocked vault view) ---
@Composable
fun PrivateVaultPasswordTypeDialog(viewModel: CloudihubViewModel) {
    val isDark = viewModel.isDarkTheme
    val textCol = if (isDark) Color.White else Color(0xFF0F172A)
    val descCol = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val accentCol = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedType by remember { mutableStateOf("PIN") }
    var inputVal by remember { mutableStateOf("") }
    var biometricEnabled by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    IOSBottomSheet(
        onDismissRequest = { viewModel.showPrivateVaultPasswordTypeDialog = false },
        viewModel = viewModel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instruction section with anim entry
            var showInstructions by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                showInstructions = true
            }

            AnimatedVisibility(
                visible = showInstructions,
                enter = expandVertically() + fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(accentCol.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = accentCol,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Setup Secure Private Vault",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textCol
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Encrypted local vault protect. Photos, documents, and recordings stored in this vault cannot be accessed without authorization.",
                        fontSize = 11.sp,
                        color = descCol,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Divider(color = borderCol, modifier = Modifier.padding(bottom = 16.dp))

            // Sleek narrow/small type selector box
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Lock Option",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = descCol,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (selectedType) {
                                "PIN" -> Icons.Default.Dialpad
                                "Password" -> Icons.Default.Password
                                else -> Icons.Default.Fingerprint
                            },
                            contentDescription = null,
                            tint = accentCol,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedType == "Biometric") "Biometric (Fingerprint)" else selectedType,
                            fontSize = 14.sp,
                            color = textCol,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (selectedType == "Biometric") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Recommend",
                                    color = Color(0xFF22C55E),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = descCol,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Dropdown Options
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                    ) {
                        val options = listOf("PIN", "Password", "Biometric")
                        options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedType = option
                                        inputVal = ""
                                        isExpanded = false
                                        errorMsg = null
                                        biometricEnabled = (option == "Biometric")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (option) {
                                            "PIN" -> Icons.Default.Dialpad
                                            "Password" -> Icons.Default.Password
                                            else -> Icons.Default.Fingerprint
                                        },
                                        contentDescription = null,
                                        tint = if (selectedType == option) accentCol else descCol,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (option == "Biometric") "Biometric (Fingerprint)" else option,
                                        fontSize = 13.sp,
                                        color = if (selectedType == option) accentCol else textCol,
                                        fontWeight = if (selectedType == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (option == "Biometric") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "Recommend",
                                                color = Color(0xFF22C55E),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                if (selectedType == option) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = accentCol,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input fields based on option selected
            AnimatedVisibility(
                visible = selectedType == "PIN" || selectedType == "Password",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            if (selectedType == "PIN") {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    inputVal = it
                                }
                            } else {
                                inputVal = it
                            }
                            errorMsg = null
                        },
                        label = { Text(if (selectedType == "PIN") "Set 6-Digit PIN" else "Set Master Password", color = descCol) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = accentCol) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedType == "PIN") KeyboardType.Number else KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = descCol
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textCol,
                            unfocusedTextColor = textCol,
                            focusedBorderColor = accentCol,
                            unfocusedBorderColor = borderCol
                        )
                    )

                    // No biometric toggle switch displayed here to ensure mutual exclusivity
                }
            }

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMsg ?: "", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.showPrivateVaultPasswordTypeDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textCol),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (selectedType == "Biometric") {
                            viewModel.savePrivateVaultSettings("Biometric", "", true)
                            viewModel.showPrivateVaultPasswordTypeDialog = false
                            Toast.makeText(context, "Secure Private Vault configured with Biometrics!", Toast.LENGTH_LONG).show()
                        } else {
                            if (selectedType == "PIN" && inputVal.length < 4) {
                                errorMsg = "PIN must be at least 4 digits"
                            } else if (selectedType == "Password" && inputVal.length < 4) {
                                errorMsg = "Password must be at least 4 characters"
                            } else {
                                viewModel.savePrivateVaultSettings(selectedType, inputVal, biometricEnabled)
                                viewModel.showPrivateVaultPasswordTypeDialog = false
                                Toast.makeText(context, "Secure Private Vault configured successfully!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accentCol)
                ) {
                    Text("Save Lock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PatternLockGrid(
    enteredPattern: List<Int>,
    onDotClicked: (Int) -> Unit,
    onClear: () -> Unit,
    isDark: Boolean,
    accentCol: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (enteredPattern.isEmpty()) "Tap dots to connect pattern" else "Pattern sequence: ${enteredPattern.joinToString(" ➔ ")}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentCol,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 3x3 Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            for (row in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (col in 0 until 3) {
                        val dotIndex = row * 3 + col + 1
                        val isSelected = enteredPattern.contains(dotIndex)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) accentCol.copy(alpha = 0.25f) else Color.Transparent
                                )
                                .border(
                                    2.dp,
                                    if (isSelected) accentCol else (if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                    CircleShape
                                )
                                .clickable { onDotClicked(dotIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) accentCol else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onClear) {
            Text("Clear Pattern", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PrivateVaultPasswordInputDialog(viewModel: CloudihubViewModel) {
    val isDark = viewModel.isDarkTheme
    val textCol = if (isDark) Color.White else Color(0xFF0F172A)
    val descCol = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val accentCol = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    var inputVal by remember { mutableStateOf("") }
    val enteredPattern = remember { mutableStateListOf<Int>() }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    IOSBottomSheet(
        onDismissRequest = { viewModel.showPrivateVaultPasswordInputDialog = false },
        viewModel = viewModel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Set Private Vault ${viewModel.tempPasswordType}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textCol
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Establish a master password/PIN/pattern. Do not share this lock credential.",
                fontSize = 12.sp,
                color = descCol
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Custom UI based on Type
            when (viewModel.tempPasswordType) {
                "PIN" -> {
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                inputVal = it
                                errorMsg = null
                            }
                        },
                        label = { Text("6-Digit PIN", color = descCol) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = accentCol) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textCol,
                            unfocusedTextColor = textCol,
                            focusedBorderColor = accentCol,
                            unfocusedBorderColor = borderCol
                        )
                    )
                }
                "Password" -> {
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            inputVal = it
                            errorMsg = null
                        },
                        label = { Text("Master Password", color = descCol) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = accentCol) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = descCol
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textCol,
                            unfocusedTextColor = textCol,
                            focusedBorderColor = accentCol,
                            unfocusedBorderColor = borderCol
                        )
                    )
                }
                "Pattern" -> {
                    PatternLockGrid(
                        enteredPattern = enteredPattern,
                        onDotClicked = { index ->
                            if (!enteredPattern.contains(index)) {
                                enteredPattern.add(index)
                                errorMsg = null
                            }
                        },
                        onClear = { enteredPattern.clear() },
                        isDark = isDark,
                        accentCol = accentCol
                    )
                }
            }

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        viewModel.showPrivateVaultPasswordInputDialog = false
                        viewModel.showPrivateVaultPasswordTypeDialog = true
                    }
                ) {
                    Text("Back", color = descCol, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val finalPassword = if (viewModel.tempPasswordType == "Pattern") {
                            enteredPattern.joinToString("-")
                        } else {
                            inputVal
                        }

                        if (viewModel.tempPasswordType == "PIN" && finalPassword.length < 4) {
                            errorMsg = "PIN must be at least 4 digits long."
                        } else if (viewModel.tempPasswordType == "Password" && finalPassword.length < 4) {
                            errorMsg = "Password must be at least 4 characters."
                        } else if (viewModel.tempPasswordType == "Pattern" && enteredPattern.size < 3) {
                            errorMsg = "Connect at least 3 dots for secure pattern."
                        } else {
                            viewModel.savePrivateVaultSettings(
                                type = viewModel.tempPasswordType,
                                passwordVal = finalPassword,
                                biometric = viewModel.tempBiometricEnabled
                            )
                            // Close dialogs and enter vault!
                            viewModel.showPrivateVaultPasswordInputDialog = false
                            viewModel.showPrivateVaultPasswordTypeDialog = false
                            viewModel.activeProfilePage = "private_vault"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentCol),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Open", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PrivateVaultUnlockDialog(viewModel: CloudihubViewModel) {
    val isDark = viewModel.isDarkTheme
    val textCol = if (isDark) Color.White else Color(0xFF0F172A)
    val descCol = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val accentCol = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    var inputVal by remember { mutableStateOf("") }
    val enteredPattern = remember { mutableStateListOf<Int>() }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Auto-trigger biometric lock on open if enabled!
    LaunchedEffect(Unit) {
        if (viewModel.privateVaultPasswordType == "Biometric" || viewModel.privateVaultBiometricEnabled) {
            viewModel.biometricAuthTarget = "private_vault"
            viewModel.showFingerprintAuth = true
            viewModel.showPrivateVaultUnlockDialog = false
        }
    }

    if (viewModel.privateVaultPasswordType == "Biometric") return

    LaunchedEffect(viewModel.activeProfilePage) {
        if (viewModel.activeProfilePage == "private_vault") {
            viewModel.showPrivateVaultUnlockDialog = false
        }
    }

    IOSBottomSheet(
        onDismissRequest = { viewModel.showPrivateVaultUnlockDialog = false },
        viewModel = viewModel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = accentCol,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Unlock Private Vault",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textCol
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Enter your ${viewModel.privateVaultPasswordType} to view private files",
                fontSize = 12.sp,
                color = descCol,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Input based on setup password type
            when (viewModel.privateVaultPasswordType) {
                "PIN" -> {
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                inputVal = it
                                errorMsg = null
                            }
                        },
                        label = { Text("Enter PIN", color = descCol) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = accentCol) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textCol,
                            unfocusedTextColor = textCol,
                            focusedBorderColor = accentCol,
                            unfocusedBorderColor = borderCol
                        )
                    )
                }
                "Password" -> {
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            inputVal = it
                            errorMsg = null
                        },
                        label = { Text("Enter Password", color = descCol) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = accentCol) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = descCol
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textCol,
                            unfocusedTextColor = textCol,
                            focusedBorderColor = accentCol,
                            unfocusedBorderColor = borderCol
                        )
                    )
                }
                "Biometric" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(accentCol.copy(alpha = 0.1f))
                                .clickable {
                                    viewModel.biometricAuthTarget = "private_vault"
                                    viewModel.showFingerprintAuth = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Scan Fingerprint",
                                tint = accentCol,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap fingerprint icon to unlock",
                            fontSize = 13.sp,
                            color = descCol,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                "Pattern" -> {
                    PatternLockGrid(
                        enteredPattern = enteredPattern,
                        onDotClicked = { index ->
                            if (!enteredPattern.contains(index)) {
                                enteredPattern.add(index)
                                errorMsg = null
                            }
                        },
                        onClear = { enteredPattern.clear() },
                        isDark = isDark,
                        accentCol = accentCol
                    )
                }
            }

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Retry Button if enabled
            if (viewModel.privateVaultBiometricEnabled && viewModel.privateVaultPasswordType != "Biometric") {
                TextButton(
                    onClick = {
                        viewModel.biometricAuthTarget = "private_vault"
                        viewModel.showFingerprintAuth = true
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, null, tint = accentCol, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Fingerprint Scanner", color = accentCol, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { viewModel.showPrivateVaultUnlockDialog = false }
                ) {
                    Text("Cancel", color = descCol, fontWeight = FontWeight.Bold)
                }

                if (viewModel.privateVaultPasswordType != "Biometric") {
                    Button(
                        onClick = {
                            val attempt = if (viewModel.privateVaultPasswordType == "Pattern") {
                                enteredPattern.joinToString("-")
                            } else {
                                inputVal
                            }

                            if (attempt == viewModel.privateVaultPassword) {
                                viewModel.showPrivateVaultUnlockDialog = false
                                viewModel.activeProfilePage = "private_vault"
                            } else {
                                errorMsg = "Verification failed! Incorrect lock credential."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentCol),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VaultLottieLoadingView(
    title: String = "Searching & Decrypting Vault Files...",
    subtitle: String = "Scanning local storage for encrypted items...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            DotLottieAnimation(
                source = DotLottieSource.Url("https://lottie.host/8de591c2-9e48-4634-b9a3-793c7ab3d0f2/7MenLSHWm0.lottie"),
                autoplay = true,
                loop = true,
                modifier = Modifier.size(240.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SelectVaultFilesDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<CloudihubViewModel.VaultItem>) -> Unit
) {
    val sampleAvailableFiles = remember {
        listOf(
            CloudihubViewModel.VaultItem(folderId = "sample", title = "Family_Vacation_2026.mp4", size = "45.2 MB", type = "Videos", date = "2026-07-24"),
            CloudihubViewModel.VaultItem(folderId = "sample", title = "Confidential_Client_Clip.mp4", size = "88.0 MB", type = "Videos", date = "2026-07-24"),
            CloudihubViewModel.VaultItem(folderId = "sample", title = "Private_Voice_Recording.m4a", size = "8.4 MB", type = "Audio", date = "2026-07-24"),
            CloudihubViewModel.VaultItem(folderId = "sample", title = "Bank_Account_Passcode.pdf", size = "2.1 MB", type = "Documents", date = "2026-07-24"),
            CloudihubViewModel.VaultItem(folderId = "sample", title = "Secret_Photo_Album.jpg", size = "5.6 MB", type = "Photos", date = "2026-07-24"),
            CloudihubViewModel.VaultItem(folderId = "sample", title = "Crypto_Vault_Backup.key", size = "12 KB", type = "Notes & Keys", date = "2026-07-24")
        )
    }

    val selectedIndices = remember { mutableStateListOf<Int>() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Files & Videos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Choose items to import into Private Vault",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    TextButton(
                        onClick = {
                            if (selectedIndices.size == sampleAvailableFiles.size) {
                                selectedIndices.clear()
                            } else {
                                selectedIndices.clear()
                                selectedIndices.addAll(sampleAvailableFiles.indices)
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedIndices.size == sampleAvailableFiles.size) "Clear" else "Select All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleAvailableFiles.size) { index ->
                        val item = sampleAvailableFiles[index]
                        val isChecked = selectedIndices.contains(index)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isChecked) Color(0xFFE0F2FE) else Color(0xFFF8FAFC))
                                .border(
                                    1.dp,
                                    if (isChecked) Color(0xFF0284C7) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isChecked) selectedIndices.remove(index)
                                    else selectedIndices.add(index)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIndices.add(index)
                                    else selectedIndices.remove(index)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.type} • ${item.size}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        enabled = selectedIndices.isNotEmpty(),
                        onClick = {
                            val items = selectedIndices.map { sampleAvailableFiles[it] }
                            onConfirm(items)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Confirm (${selectedIndices.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImportingToVaultDialog(itemCount: Int) {
    androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/8de591c2-9e48-4634-b9a3-793c7ab3d0f2/7MenLSHWm0.lottie"),
                    autoplay = true,
                    loop = true,
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Encrypting & Importing $itemCount File(s)...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Securing into local AES-256 encrypted Private Vault",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVaultFolderDialog(
    viewModel: CloudihubViewModel,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    val types = listOf("Photos", "Videos", "Audio", "Documents", "Notes & Keys")
    var selectedType by remember { mutableStateOf("Photos") }
    val isDark = viewModel.isDarkTheme

    val previewImageUrl = when (selectedType) {
        "Photos" -> viewModel.folderImagePhotos
        "Videos" -> viewModel.folderImageVideos
        "Audio" -> viewModel.folderImageAudio
        "Documents" -> viewModel.folderImageDocuments
        else -> viewModel.folderImageNotes
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create New Private Folder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = model = previewImageUrl,
                    contentDescription = "Folder Preview Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                placeholder = { Text("e.g. My Secret Vault") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0284C7),
                    unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Folder Type:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(types, key = { it.hashCode() }) { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                            labelColor = if (isDark) Color.White else Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    enabled = folderName.isNotBlank(),
                    onClick = {
                        viewModel.createNewVaultFolder(folderName.trim(), selectedType)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create Folder", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VaultFolderDetailView(
    folder: CloudihubViewModel.VaultFolder,
    viewModel: CloudihubViewModel
) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkTheme
    val textCol = if (isDark) Color.White else Color(0xFF0F172A)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val descCol = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val accentCol = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    var searchQuery by remember { mutableStateOf("") }
    var showSelectFilesModal by remember { mutableStateOf(false) }
    var isImportingFiles by remember { mutableStateOf(false) }
    var pendingSelectedFiles by remember { mutableStateOf<List<CloudihubViewModel.VaultItem>>(emptyList()) }
    var isFolderLoading by remember { mutableStateOf(true) }

    val folderItems = viewModel.vaultItems.filter {
        (it.folderId == folder.id || it.type == folder.type) &&
                (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
    }

    LaunchedEffect(folder.id) {
        isFolderLoading = true
        delay(1200)
        isFolderLoading = false
    }

    if (isFolderLoading) {
        VaultLottieLoadingView(
            title = "Loading ${folder.name}...",
            subtitle = "Decrypting files in folder...",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.selectedVaultFolder = null },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textCol)
                }

                Spacer(modifier = Modifier.width(12.dp))

                AsyncImage(
                    model = model = folder.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textCol,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${folderItems.size} encrypted ${folder.type.lowercase()} item(s)",
                        fontSize = 12.sp,
                        color = descCol
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search inside ${folder.name}...", fontSize = 13.sp, color = descCol) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = descCol) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = descCol)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = accentCol,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textCol,
                    unfocusedTextColor = textCol
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (folderItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = descCol.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No files in this folder yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = descCol
                        )
                        Text(
                            text = "Tap the + button below to import files or videos",
                            fontSize = 12.sp,
                            color = descCol.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(folderItems, key = { it.hashCode() }) { item ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentCol.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item.type) {
                                            "Photos" -> Icons.Default.Image
                                            "Videos" -> Icons.Default.PlayCircle
                                            "Audio" -> Icons.Default.Audiotrack
                                            "Documents" -> Icons.Default.Description
                                            else -> Icons.Default.Key
                                        },
                                        contentDescription = null,
                                        tint = accentCol,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.size} • Encrypted AES-256 • ${item.date}",
                                        fontSize = 11.sp,
                                        color = descCol
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "Opening decrypted preview for ${item.title}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Decrypted",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showSelectFilesModal = true },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) },
            text = { Text("Add ${folder.type}", fontWeight = FontWeight.Bold, color = Color.White) },
            containerColor = accentCol,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
        )

        if (showSelectFilesModal) {
            SelectVaultFilesDialog(
                onDismiss = { showSelectFilesModal = false },
                onConfirm = { items ->
                    showSelectFilesModal = false
                    pendingSelectedFiles = items.map { it.copy(folderId = folder.id, type = folder.type) }
                    isImportingFiles = true
                }
            )
        }

        if (isImportingFiles) {
            ImportingToVaultDialog(itemCount = pendingSelectedFiles.size)

            LaunchedEffect(isImportingFiles) {
                delay(2800)
                pendingSelectedFiles.forEach { item ->
                    viewModel.addVaultItemToFolder(item)
                }
                isImportingFiles = false
                Toast.makeText(
                    context,
                    "${pendingSelectedFiles.size} Item(s) successfully imported into ${folder.name}!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun PrivateVaultScreen(viewModel: CloudihubViewModel) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkTheme
    val bgGradient = if (isDark) {
        listOf(Color(0xFF0B1329), Color(0xFF1C2541))
    } else {
        listOf(Color(0xFFF0F6FF), Color(0xFFD6E4FF))
    }
    val textCol = if (isDark) Color.White else Color(0xFF0F172A)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val descCol = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val accentCol = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    var showMenu by remember { mutableStateOf(false) }
    var isVaultScanning by remember { mutableStateOf(true) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVaultScanning = true
        delay(1800)
        isVaultScanning = false
    }

    if (viewModel.selectedVaultFolder != null) {
        VaultFolderDetailView(
            folder = viewModel.selectedVaultFolder!!,
            viewModel = viewModel
        )
        return
    }

    if (isVaultScanning) {
        VaultLottieLoadingView(
            title = "Searching & Decrypting Vault Folders...",
            subtitle = "Scanning local encrypted vault database...",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(bgGradient)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.activeProfilePage = "main" },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = textCol
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Private Vault",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textCol
                        )
                        Text(
                            text = "End-to-end encrypted storage",
                            fontSize = 12.sp,
                            color = descCol
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = textCol
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(cardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rescan Vault Files", color = textCol, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Refresh, null, tint = accentCol) },
                            onClick = {
                                showMenu = false
                                isVaultScanning = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unhide Vault", color = textCol, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Visibility, null, tint = accentCol) },
                            onClick = {
                                showMenu = false
                                viewModel.updateVaultCardHidden(false)
                                viewModel.activeProfilePage = "main"
                                Toast.makeText(context, "Vault card is now visible on the Profile dashboard!", Toast.LENGTH_LONG).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change Lock Type", color = textCol) },
                            leadingIcon = { Icon(Icons.Default.Settings, null, tint = descCol) },
                            onClick = {
                                showMenu = false
                                viewModel.showPrivateVaultPasswordTypeDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Vault Help & FAQ", color = textCol) },
                            leadingIcon = { Icon(Icons.Default.Info, null, tint = descCol) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "All files are stored locally and encrypted.", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lock & Exit", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                viewModel.activeProfilePage = "main"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Folders Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Existing Folders
                items(viewModel.vaultFolders, key = { it.hashCode() }) { folder ->
                    val itemCount = viewModel.vaultItems.count { it.folderId == folder.id || it.type == folder.type }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable {
                                viewModel.selectedVaultFolder = folder
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = model = folder.imageUrl,
                                    contentDescription = folder.name,
                                    modifier = Modifier.size(90.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = folder.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$itemCount item(s)",
                                    fontSize = 11.sp,
                                    color = descCol
                                )
                            }
                        }
                    }
                }

                // More / Create New Folder Card using custom vector icon
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFE0F2FE).copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.5.dp, accentCol.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable {
                                showCreateFolderDialog = true
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Designed Folder Creation Icon Badge
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(accentCol.copy(alpha = 0.12f))
                                        .border(1.5.dp, accentCol.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(accentCol, Color(0xFF0284C7))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreateNewFolder,
                                            contentDescription = "Create New Folder",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Create Folder",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentCol,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap to add new",
                                    fontSize = 11.sp,
                                    color = descCol
                                )
                            }
                        }
                    }
                }
            }
        }

        // Create Folder Dialog
        if (showCreateFolderDialog) {
            CreateVaultFolderDialog(
                viewModel = viewModel,
                onDismiss = { showCreateFolderDialog = false }
            )
        }
    }
}

