package com.example.gymfitness.presentation.screen.leaderboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymfitness.domain.models.LeaderboardEntry
import com.example.gymfitness.domain.models.LeaderboardPeriod
import com.example.gymfitness.domain.models.LeaderboardUiState
import com.example.gymfitness.presentation.components.BaseCard
import com.example.gymfitness.presentation.components.GhostButton
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.viewmodel.LeaderboardViewModel
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.delay

import androidx.navigation.NavController
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.components.CodeChip

@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val friendCode by viewModel.friendCode.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = PageBg,
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        when (uiState) {
            is LeaderboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SunsetOrange)
                }
            }
            is LeaderboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text((uiState as LeaderboardUiState.Error).message, color = Color.Red)
                }
            }
            is LeaderboardUiState.Success -> {
                val state = uiState as LeaderboardUiState.Success
                val maxPoints = state.entries.maxOfOrNull { it.weeklyPoints } ?: 1
                
                if (state.entries.size <= 1) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding()
                    ) {
                        item {
                            LeaderboardHeader(
                                friendCount = 0,
                                friendCode = friendCode,
                                navController = navController
                            )
                        }
                        item {
                            Spacer(Modifier.height(32.dp))
                            BaseCard(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("👋", fontSize = 48.sp)
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "Squad is Empty",
                                        style = Typography.titleLarge,
                                        color = InkBlack
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Add friends to see how you rank. Share your squad code or add theirs to start competing!",
                                        style = Typography.bodyMedium,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    if (friendCode.isNotEmpty() && friendCode != "------") {
                                        CodeChip(code = friendCode)
                                        Spacer(Modifier.height(16.dp))
                                    }
                                    PrimaryButton(
                                        text = "Add Friends",
                                        onClick = { navController.navigate("friend_code") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(32.dp))
                            YourStatsCard(entry = state.currentUserEntry)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding()
                    ) {
                        item {
                            LeaderboardHeader(
                                friendCount = state.entries.size,
                                friendCode = friendCode,
                                navController = navController
                            )
                        }
                        item {
                            PeriodTabRow(
                                selected = selectedPeriod,
                                onSelect = { period ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.fetchLeaderboard(period)
                                }
                            )
                        }
                        item {
                            Top3Podium(
                                top3 = state.entries.take(3),
                                haptic = haptic
                            )
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                        itemsIndexed(state.entries.drop(3)) { index, entry ->
                            LeaderboardListItem(
                                entry = entry,
                                rank = index + 4,
                                maxPoints = maxPoints,
                                onTap = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            YourStatsCard(entry = state.currentUserEntry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardHeader(
    friendCount: Int,
    friendCode: String,
    navController: NavController
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Leaderboard",
                style = Typography.displayLarge,
                color = InkBlack
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$friendCount friends competing",
                style = Typography.bodyMedium,
                color = TextMuted
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (friendCode.isNotEmpty() && friendCode != "------") {
                CodeChip(code = friendCode)
            }
            GhostButton(
                text = "+ Add",
                onClick = { navController.navigate("friend_code") },
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

@Composable
fun PeriodTabRow(selected: LeaderboardPeriod, onSelect: (LeaderboardPeriod) -> Unit) {
    val periods = LeaderboardPeriod.values()
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceAlt)
            .padding(4.dp)
    ) {
        periods.forEach { period ->
            val isActive = period == selected
            val bgColor by animateColorAsState(
                targetValue = if (isActive) CardSurface else Color.Transparent,
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) SunsetOrange else TextMuted,
                label = "tab_text"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .clickable { onSelect(period) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (period) {
                        LeaderboardPeriod.WEEKLY -> "Weekly"
                        LeaderboardPeriod.MONTHLY -> "Monthly"
                        LeaderboardPeriod.ALL_TIME -> "All Time"
                    },
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
fun Top3Podium(top3: List<LeaderboardEntry>, haptic: HapticFeedback) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        triggered = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        val rank2 = top3.getOrNull(1)
        val rank1 = top3.getOrNull(0)
        val rank3 = top3.getOrNull(2)

        PodiumColumn(
            entry = rank2, rank = 2, targetHeight = 80, delayMs = 150, 
            triggered = triggered, haptic = haptic, modifier = Modifier.weight(1f)
        )
        PodiumColumn(
            entry = rank1, rank = 1, targetHeight = 120, delayMs = 300, 
            triggered = triggered, haptic = haptic, modifier = Modifier.weight(1f)
        )
        PodiumColumn(
            entry = rank3, rank = 3, targetHeight = 60, delayMs = 0, 
            triggered = triggered, haptic = haptic, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PodiumColumn(
    entry: LeaderboardEntry?, rank: Int, targetHeight: Int, 
    delayMs: Long, triggered: Boolean, haptic: HapticFeedback, modifier: Modifier
) {
    var animTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(triggered) {
        if (triggered) {
            delay(delayMs)
            animTrigger = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    val barHeight by animateIntAsState(
        targetValue = if (animTrigger) targetHeight else 0,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "podium_bar_$rank"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (rank == 1) {
            Text("👑", fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
        }
        val avatarSize = if (rank == 1) 64.dp else 48.dp
        val borderColor = SunsetOrange // Orange borders for top 3 as requested
        val avatarBg = OrangeTint

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(avatarBg)
                .border(2.dp, borderColor, CircleShape)
        ) {
            Text(
                text = entry?.avatarInitials ?: "?",
                style = Typography.titleMedium.copy(fontSize = if (rank == 1) 18.sp else 14.sp),
                color = SunsetOrange
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = entry?.displayName?.split(" ")?.firstOrNull() ?: "",
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = InkBlack,
            maxLines = 1
        )
        Text(
            text = "%,d".format(entry?.weeklyPoints ?: 0),
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = SunsetOrange
        )
        Spacer(Modifier.height(8.dp))

        val barColor = Brush.verticalGradient(listOf(SunsetOrange.copy(alpha=0.3f), Color.Transparent))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(barHeight.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(barColor)
                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        )
    }
}

@Composable
fun LeaderboardListItem(entry: LeaderboardEntry, rank: Int, maxPoints: Int, onTap: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((rank * 60).toLong())
        visible = true
    }
    val fillFraction by animateFloatAsState(
        targetValue = if (visible && maxPoints > 0) entry.weeklyPoints.toFloat() / maxPoints else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "fill"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (entry.isCurrentUser) OrangeTint.copy(alpha = 0.5f) else CardSurface)
            .border(
                width = 1.dp,
                color = if (entry.isCurrentUser) SunsetOrange.copy(alpha = 0.5f) else StrokeSoft,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onTap() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fillFraction)
                .background(SunsetOrange.copy(alpha = 0.05f))
        )
        if (entry.isCurrentUser) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(SunsetOrange)
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextMuted,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceAlt)
            ) {
                Text(
                    text = entry.avatarInitials,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkBlack
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.displayName,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = InkBlack
                    )
                    if (entry.isCurrentUser) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SunsetOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("YOU", style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "%,d steps · %d workout%s".format(entry.steps, entry.workoutsThisWeek, if (entry.workoutsThisWeek == 1) "" else "s"),
                    style = Typography.labelSmall,
                    color = TextMuted
                )
            }
            Text(
                text = "%,d".format(entry.weeklyPoints),
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InkBlack
            )
        }
    }
}

@Composable
fun YourStatsCard(entry: LeaderboardEntry?) {
    if (entry == null) return
    BaseCard(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                "YOUR STATS THIS WEEK",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = SunsetOrange
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(value = "%,d".format(entry.steps), label = "Steps", color = InkBlack)
                StatItem(value = "${entry.workoutsThisWeek}", label = "Workouts", color = InkBlack)
                StatItem(value = "%,d".format(entry.weeklyPoints), label = "Points", color = SunsetOrange)
                StatItem(value = "${entry.currentStreak} \uD83D\uDD25", label = "Streak", color = InkBlack)
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = value,
            transitionSpec = { slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut() },
            label = "stat_$label"
        ) { v ->
            Text(v, style = Typography.displayMedium, color = color)
        }
        Text(label, style = Typography.labelSmall, color = TextMuted)
    }
}
