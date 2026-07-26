package com.example.gymfitness.presentation.screen.social

import android.content.ClipData
import android.content.ClipboardManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymfitness.domain.models.FriendCodeUiState
import com.example.gymfitness.domain.models.FriendEntry
import com.example.gymfitness.presentation.components.BaseCard
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.presentation.viewmodel.FriendCodeViewModel
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FriendCodeScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onCompare: (friendId: String) -> Unit,
    viewModel: FriendCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val codeInput by viewModel.addCodeInput.collectAsStateWithLifecycle()
    val addResult by viewModel.addResult.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboard = context.getSystemService(ClipboardManager::class.java)

    LaunchedEffect(addResult) {
        when (addResult) {
            is FriendCodeViewModel.AddFriendResult.Success -> {
                val vibrator = context.getSystemService(Vibrator::class.java)
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0,50,30,80), -1))
            }
            is FriendCodeViewModel.AddFriendResult.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) { viewModel.loadData(currentUserId) }

    Scaffold(containerColor = PageBg) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).clickable { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack() 
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Your Squad", style = Typography.displayLarge, color = SunsetOrange)
                }
                Text(
                    "Share your code or enter a friend's to connect",
                    style = Typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(Modifier.height(24.dp))
            }

            when (uiState) {
                is FriendCodeUiState.Success -> {
                    val state = uiState as FriendCodeUiState.Success
                    item {
                        YourCodeCard(
                            code = state.currentUserCode,
                            onCopy = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                clipboard?.setPrimaryClip(ClipData.newPlainText("FitStore Code", state.currentUserCode))
                            }
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                    item {
                        Text("ADD A FRIEND", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(Modifier.height(12.dp))
                        AddFriendRow(
                            value = codeInput,
                            onValueChange = viewModel::onCodeInputChange,
                            isLoading = addResult is FriendCodeViewModel.AddFriendResult.Loading,
                            onAdd = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.addFriendByCode(codeInput, currentUserId)
                            }
                        )
                        if (addResult is FriendCodeViewModel.AddFriendResult.Error) {
                            Text(
                                (addResult as FriendCodeViewModel.AddFriendResult.Error).message,
                                style = Typography.labelSmall, color = Color.Red,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                    item {
                        Text("SQUAD", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextMuted, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(Modifier.height(12.dp))
                    }
                    itemsIndexed(state.friends) { index, friend ->
                        FriendListItem(friend = friend, onCompare = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCompare(friend.userId)
                        })
                        Spacer(Modifier.height(8.dp))
                    }
                }
                is FriendCodeUiState.Loading -> item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = SunsetOrange) } }
                is FriendCodeUiState.Error   -> item { Text((uiState as FriendCodeUiState.Error).message, color = Color.Red, modifier = Modifier.padding(24.dp)) }
            }
        }
    }
}

@Composable
fun YourCodeCard(code: String, onCopy: () -> Unit) {
    var isCopied by remember { mutableStateOf(false) }
    var codeScale by remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()
    
    val scaleAnim by animateFloatAsState(
        targetValue = codeScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "scale"
    )

    Box(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().scale(scaleAnim)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
            drawRoundRect(color = StrokeSoft, style = stroke, cornerRadius = CornerRadius(16.dp.toPx()))
        }
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardSurface).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "YOUR UNIQUE FRIEND CODE",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = TextMuted
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                code.forEachIndexed { i, char ->
                    var charVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { delay(i * 80L + 100L); charVisible = true }
                    AnimatedVisibility(
                        visible = charVisible,
                        enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn(tween(300))
                    ) {
                        Text(
                            text = char.toString(),
                            style = Typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp),
                            color = SunsetOrange
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(OrangeTint)
                    .border(0.5.dp, StrokeSoft, RoundedCornerShape(50))
                    .clickable { 
                        scope.launch {
                            codeScale = 0.94f
                            delay(100)
                            codeScale = 1f
                            isCopied = true
                            delay(1500)
                            isCopied = false
                        }
                        onCopy() 
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isCopied) "Copied! ✓" else "📋 Copy Code",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = SunsetOrange
                )
            }
        }
    }
}

@Composable
fun AddFriendRow(value: String, onValueChange: (String) -> Unit, isLoading: Boolean, onAdd: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value, 
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = SunsetOrange,
                unfocusedBorderColor = StrokeSoft,
                focusedTextColor = InkBlack,
                unfocusedTextColor = InkBlack
            ),
            textStyle = Typography.bodyLarge.copy(letterSpacing = 2.sp),
            singleLine = true,
            placeholder = { Text("6-DIGIT CODE", style = Typography.labelMedium.copy(letterSpacing = 2.sp), color = TextMuted) }
        )
        Spacer(Modifier.width(12.dp))
        Button(
            onClick = onAdd,
            enabled = !isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = Color.White, disabledContainerColor = SunsetOrange.copy(alpha=0.3f)),
            modifier = Modifier.height(56.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Add", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun FriendListItem(friend: FriendEntry, onCompare: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(12.dp)).background(CardSurface).clickable { onCompare() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(friend.avatarColor)) {
            Text(friend.avatarInitials, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.displayName, style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = InkBlack)
            Text("Streak: ${friend.currentStreak} 🔥", style = Typography.labelSmall, color = TextMuted)
        }
        if (friend.isOnline) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
        }
    }
}
