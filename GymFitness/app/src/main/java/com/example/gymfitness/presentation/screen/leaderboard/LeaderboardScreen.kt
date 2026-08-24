package com.example.gymfitness.presentation.screen.leaderboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GroupAdd
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
import androidx.navigation.NavController
import com.example.gymfitness.domain.models.LeaderboardEntry
import com.example.gymfitness.domain.models.LeaderboardPeriod
import com.example.gymfitness.domain.models.LeaderboardUiState
import com.example.gymfitness.presentation.components.CodeChip
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.viewmodel.LeaderboardViewModel
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.delay

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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LimeGreen)
                }
            }
            is LeaderboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as LeaderboardUiState.Error).message,
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is LeaderboardUiState.Success -> {
                val state = uiState as LeaderboardUiState.Success
                val maxPoints = state.entries.maxOfOrNull { it.weeklyPoints }?.coerceAtLeast(1) ?: 1

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .statusBarsPadding()
                ) {
                    // Header with Squad Code & Add button
                    item {
                        LeaderboardHeader(
                            friendCount = state.entries.size,
                            friendCode = friendCode,
                            onAddFriend = { navController.navigate("friend_code") }
                        )
                    }

                    // Period Selector (Weekly, Monthly, All Time)
                    item {
                        PeriodTabRow(
                            selected = selectedPeriod,
                            onSelect = { period ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.fetchLeaderboard(period)
                            }
                        )
                    }

                    // Top 3 Podium or Empty State
                    if (state.entries.size <= 1 && state.entries.firstOrNull()?.weeklyPoints == 0) {
                        item {
                            EmptySquadCard(
                                friendCode = friendCode,
                                onAddClick = { navController.navigate("friend_code") }
                            )
                        }
                    } else {
                        item {
                            Top3Podium(
                                entries = state.entries,
                                haptic = haptic,
                                onInviteClick = { navController.navigate("friend_code") }
                            )
                        }
                    }

                    // User Stats Card (Weekly Steps, Workouts, Points, Streak)
                    item {
                        Spacer(Modifier.height(16.dp))
                        YourStatsCard(entry = state.currentUserEntry)
                    }

                    // Full Rankings Section
                    if (state.entries.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Squad Standings",
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${state.entries.size} Competing",
                                    fontWeight = FontWeight.Bold,
                                    color = LimeDeepDark,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        itemsIndexed(state.entries) { index, entry ->
                            LeaderboardListItem(
                                entry = entry,
                                rank = index + 1,
                                maxPoints = maxPoints,
                                onTap = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item {
                        Spacer(Modifier.height(40.dp))
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
    onAddFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Leaderboard",
                fontWeight = FontWeight.Black,
                color = OffWhite,
                fontSize = 26.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (friendCount <= 1) "Compete with friends & track rank" else "$friendCount athletes competing",
                color = TextMutedDark,
                fontSize = 13.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (friendCode.isNotEmpty() && friendCode != "------") {
                CodeChip(code = friendCode)
            }
            IconButton(
                onClick = onAddFriend,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, StrokeDark, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = "Add Friends",
                    tint = LimeGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PeriodTabRow(selected: LeaderboardPeriod, onSelect: (LeaderboardPeriod) -> Unit) {
    val periods = LeaderboardPeriod.values()
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, StrokeDark, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        periods.forEach { period ->
            val isActive = period == selected
            val bgColor by animateColorAsState(
                targetValue = if (isActive) LimeGreen else Color.Transparent,
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color(0xFF121212) else TextMutedDark,
                label = "tab_text"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onSelect(period) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (period) {
                        LeaderboardPeriod.WEEKLY -> "Weekly"
                        LeaderboardPeriod.MONTHLY -> "Monthly"
                        LeaderboardPeriod.ALL_TIME -> "All Time"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun Top3Podium(
    entries: List<LeaderboardEntry>,
    haptic: HapticFeedback,
    onInviteClick: () -> Unit
) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        triggered = true
    }

    val rank1 = entries.getOrNull(0)
    val rank2 = entries.getOrNull(1)
    val rank3 = entries.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place (Left)
        PodiumSlot(
            entry = rank2,
            rank = 2,
            podiumHeight = 85.dp,
            accentColor = Color(0xFFC0C0C0),
            triggered = triggered,
            delayMs = 150,
            haptic = haptic,
            onInviteClick = onInviteClick,
            modifier = Modifier.weight(1f)
        )

        // 1st Place (Center - Highest)
        PodiumSlot(
            entry = rank1,
            rank = 1,
            podiumHeight = 125.dp,
            accentColor = LimeGreen,
            triggered = triggered,
            delayMs = 300,
            haptic = haptic,
            onInviteClick = onInviteClick,
            modifier = Modifier.weight(1.1f)
        )

        // 3rd Place (Right)
        PodiumSlot(
            entry = rank3,
            rank = 3,
            podiumHeight = 65.dp,
            accentColor = Color(0xFFCD7F32),
            triggered = triggered,
            delayMs = 0,
            haptic = haptic,
            onInviteClick = onInviteClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PodiumSlot(
    entry: LeaderboardEntry?,
    rank: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    triggered: Boolean,
    delayMs: Long,
    haptic: HapticFeedback,
    onInviteClick: () -> Unit,
    modifier: Modifier
) {
    var animTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(triggered) {
        if (triggered) {
            delay(delayMs)
            animTrigger = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val animatedHeight by animateDpAsState(
        targetValue = if (animTrigger) podiumHeight else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 220f),
        label = "podium_anim_$rank"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (entry != null) {
            // Crown or Rank Badge for 1st
            if (rank == 1) {
                Text("👑", fontSize = 24.sp)
                Spacer(Modifier.height(2.dp))
            }

            // Avatar Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (rank == 1) 56.dp else 46.dp)
                    .clip(CircleShape)
                    .background(if (rank == 1) LimeTintDark else SurfaceAltDark)
                    .border(2.dp, accentColor, CircleShape)
            ) {
                Text(
                    text = entry.avatarInitials,
                    fontWeight = FontWeight.Black,
                    fontSize = if (rank == 1) 18.sp else 14.sp,
                    color = if (rank == 1) LimeGreen else OffWhite
                )
            }

            Spacer(Modifier.height(6.dp))

            // Display Name
            Text(
                text = entry.displayName.split(" ").firstOrNull() ?: "",
                fontWeight = FontWeight.Bold,
                color = OffWhite,
                fontSize = 13.sp,
                maxLines = 1
            )

            // Points
            Text(
                text = "${entry.weeklyPoints} pts",
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(6.dp))

            // Solid Podium Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accentColor.copy(alpha = if (rank == 1) 0.35f else 0.2f),
                                SurfaceDark
                            )
                        )
                    )
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.Black,
                    fontSize = if (rank == 1) 22.sp else 18.sp,
                    color = accentColor
                )
            }
        } else {
            // Empty Slot - Invite Friend Prompt
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, StrokeDark, CircleShape)
                    .clickable { onInviteClick() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = TextMutedDark, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.height(4.dp))
            Text("Invite", color = TextMutedDark, fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text("-", color = TextMutedDark, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(SurfaceDark)
                    .border(1.dp, StrokeDark, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("#$rank", color = StrokeDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun LeaderboardListItem(
    entry: LeaderboardEntry,
    rank: Int,
    maxPoints: Int,
    onTap: () -> Unit
) {
    val rankColor = when (rank) {
        1 -> LimeGreen
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextMutedDark
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) LimeTintDark else SurfaceDark
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(
                width = if (entry.isCurrentUser) 1.5.dp else 1.dp,
                color = if (entry.isCurrentUser) LimeGreen else StrokeDark,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onTap() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "$rank",
                fontWeight = FontWeight.Black,
                color = rankColor,
                fontSize = 15.sp,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.width(8.dp))

            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceAltDark)
                    .border(1.dp, if (entry.isCurrentUser) LimeGreen else StrokeDark, CircleShape)
            ) {
                Text(
                    text = entry.avatarInitials,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isCurrentUser) LimeGreen else OffWhite,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Name + Stats breakdown
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.displayName,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        fontSize = 14.sp
                    )
                    if (entry.isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LimeGreen)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("YOU", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF121212))
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${String.format("%,d", entry.steps)} steps • ${entry.workoutsThisWeek} workouts",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }

            // Total Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,d", entry.weeklyPoints),
                    fontWeight = FontWeight.Black,
                    color = if (entry.isCurrentUser) LimeGreen else OffWhite,
                    fontSize = 16.sp
                )
                Text(
                    text = "pts",
                    color = TextMutedDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun YourStatsCard(entry: LeaderboardEntry?) {
    if (entry == null) return

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, StrokeDark, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR STATS THIS WEEK",
                    fontWeight = FontWeight.Black,
                    color = LimeGreen,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                if (entry.currentStreak > 0) {
                    Surface(
                        color = LimeTintDark,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🔥 ${entry.currentStreak}d Streak",
                            color = LimeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatTile(label = "Steps", value = String.format("%,d", entry.steps), modifier = Modifier.weight(1f))
                StatTile(label = "Workouts", value = "${entry.workoutsThisWeek}", modifier = Modifier.weight(1f))
                StatTile(label = "Points", value = String.format("%,d", entry.weeklyPoints), highlight = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatTile(label: String, value: String, highlight: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = if (highlight) LimeGreen else OffWhite
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMutedDark
        )
    }
}

@Composable
fun EmptySquadCard(friendCode: String, onAddClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .border(1.dp, StrokeDark, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(LimeTintDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = LimeGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Squad Standings",
                fontWeight = FontWeight.Black,
                color = OffWhite,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Add friends to compete on weekly steps and workouts! Share your squad code or enter theirs.",
                color = TextMutedDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(18.dp))

            if (friendCode.isNotEmpty() && friendCode != "------") {
                CodeChip(code = friendCode)
                Spacer(Modifier.height(14.dp))
            }

            PrimaryButton(
                text = "Add / Join Friends ➔",
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
