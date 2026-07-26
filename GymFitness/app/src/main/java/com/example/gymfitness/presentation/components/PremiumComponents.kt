package com.example.gymfitness.presentation.components

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gymfitness.presentation.state.DayStepEntry
import com.example.gymfitness.ui.theme.*
import java.time.LocalDate
import java.util.Locale

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = LimeGreen,
            contentColor = Color(0xFF121212)
        ),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = text,
            style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        )
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.2.dp, LimeGreen),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
        shape = CircleShape,
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = text,
            style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        )
    }
}

@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun CategoryBadge(
    text: String,
    colorTint: Color = LimeTintDark,
    textColor: Color = LimeGreen,
    modifier: Modifier = Modifier
) {
    Surface(
        color = colorTint,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun StatStrip(
    stats: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { label ->
            Surface(
                color = SurfaceAltDark,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = label,
                    style = Typography.labelSmall,
                    color = OffWhite,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun PrimaryInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMutedDark) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LimeGreen,
            unfocusedBorderColor = StrokeDark,
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceAltDark,
            cursorColor = LimeGreen,
            focusedTextColor = OffWhite,
            unfocusedTextColor = OffWhite
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun AvatarInitials(
    initials: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(LimeTintDark)
            .border(1.dp, LimeGreen.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = LimeGreen,
            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun PrimaryFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = LimeGreen,
        contentColor = Color(0xFF121212),
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        modifier = modifier.size(56.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Quick Add Workout or Meal")
    }
}

@Composable
fun CalorieRing(
    current: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "calorieProgress"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                drawCircle(
                    color = SurfaceAltDark,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = if (current > target && target > 0) WarningAmber else LimeGreen,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val remaining = (target - current).coerceAtLeast(0)
                Text(
                    text = String.format(Locale.getDefault(), "%,d", remaining),
                    style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = OffWhite
                )
                Text(
                    text = "kcal left",
                    style = Typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMutedDark
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(LimeGreen)
                )
                Text(
                    text = "Eaten: %,d kcal".format(current),
                    style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = OffWhite
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SurfaceAltDark)
                )
                Text(
                    text = "Target: %,d kcal".format(target),
                    style = Typography.bodySmall,
                    color = TextMutedDark
                )
            }
        }
    }
}

@Composable
fun MacroStatsRow(
    protein: Float,
    proteinTarget: Float,
    carbs: Float,
    carbsTarget: Float,
    fat: Float,
    fatsTarget: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            NutrientBar("Protein", protein, proteinTarget, LimeGreen)
        }
        Box(modifier = Modifier.weight(1f)) {
            NutrientBar("Carbs", carbs, carbsTarget, InfoBlue)
        }
        Box(modifier = Modifier.weight(1f)) {
            NutrientBar("Fats", fat, fatsTarget, WarningAmber)
        }
    }
}

@Composable
fun DailyProgressCard(
    caloriesEaten: Int,
    caloriesTarget: Int,
    protein: Float,
    proteinTarget: Float,
    carbs: Float,
    carbsTarget: Float,
    fat: Float,
    fatsTarget: Float,
    modifier: Modifier = Modifier
) {
    BaseCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Calorie Target",
                        style = Typography.labelSmall,
                        color = TextMutedDark
                    )
                    Text(
                        text = "Nutrition Overview",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OffWhite
                    )
                }
                val percentage = if (caloriesTarget > 0) {
                    ((caloriesEaten.toFloat() / caloriesTarget.toFloat()) * 100).toInt().coerceIn(0, 999)
                } else 0
                Surface(
                    color = LimeTintDark,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$percentage% Goal",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LimeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            CalorieRing(current = caloriesEaten, target = caloriesTarget)

            MacroStatsRow(
                protein = protein,
                proteinTarget = proteinTarget,
                carbs = carbs,
                carbsTarget = carbsTarget,
                fat = fat,
                fatsTarget = fatsTarget
            )
        }
    }
}

@Composable
fun NutrientBar(
    label: String,
    value: Float,
    target: Float,
    color: Color = LimeGreen,
    unit: String = "g"
) {
    val icon = when (label.lowercase()) {
        "protein" -> "🍗"
        "carbs" -> "🍞"
        "fats" -> "🧀"
        else -> "📊"
    }

    val progress = if (target > 0) (value / target).coerceIn(0f, 1f) else 0f
    val barColor = if (target > 0 && value > target) WarningAmber else color

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceAltDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 12.sp)
                }
                Text(
                    text = label,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMutedDark
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(barColor, shape = CircleShape)
                )
            }

            Text(
                text = "${value.toInt()}g / ${target.toInt()}$unit",
                style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = OffWhite
            )
        }
    }
}

@Composable
fun CodeChip(
    code: String,
    modifier: Modifier = Modifier,
    onCopied: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Surface(
        color = LimeTintDark,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                clipboardManager.setText(AnnotatedString(code))
                if (onCopied != null) {
                    onCopied()
                } else {
                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Code:",
                style = Typography.labelSmall,
                color = LimeDeepDark
            )
            Text(
                text = code,
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LimeGreen,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy Friend Code",
                tint = LimeGreen,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun StepsBarChart(
    stepsData: List<Int>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val sampleDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val weeklySteps = stepsData.mapIndexed { idx, count ->
        DayStepEntry(today.minusDays((stepsData.size - 1 - idx).toLong()), sampleDays.getOrElse(idx) { "" }, count)
    }
    StepsBarChart(weeklySteps = weeklySteps, isHealthConnectGranted = true, modifier = modifier)
}

@Composable
fun StepsBarChart(
    weeklySteps: List<DayStepEntry>,
    isHealthConnectGranted: Boolean = true,
    isLoading: Boolean = false,
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isHealthConnectGranted && !isLoading) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Connect Health Connect to view weekly steps",
                style = Typography.bodyMedium,
                color = TextMutedDark
            )
            GhostButton(
                text = "Grant Health Permission",
                onClick = onConnectClick,
                modifier = Modifier.height(44.dp)
            )
        }
        return
    }

    if (isLoading) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(7) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(SurfaceAltDark.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp, 10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SurfaceAltDark.copy(alpha = 0.3f))
                    )
                }
            }
        }
        return
    }

    val today = LocalDate.now()
    val maxSteps = maxOf(weeklySteps.maxOfOrNull { it.steps } ?: 10000, 5000)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val displayList = if (weeklySteps.isNotEmpty()) weeklySteps else {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").mapIndexed { idx, label ->
                DayStepEntry(today.minusDays((6 - idx).toLong()), label, 0)
            }
        }

        displayList.forEachIndexed { index, entry ->
            val barHeightFraction = (entry.steps.toFloat() / maxSteps.toFloat()).coerceIn(0.08f, 1f)
            val isToday = entry.date == today
            val isSelected = selectedIndex == index
            val barColor = if (isToday) LimeGreen else if (isSelected) InfoBlue else SurfaceAltDark

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedIndex = if (isSelected) null else index }
            ) {
                if (isToday || isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) InfoBlue else LimeGreen)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%,d", entry.steps),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121212)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .fillMaxHeight(barHeightFraction - 0.12f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.dayLabel,
                    style = Typography.bodySmall,
                    color = if (isToday) OffWhite else TextMutedDark
                )
            }
        }
    }
}

@Composable
fun HeartRateLineChart(
    heartRateData: List<Int>,
    modifier: Modifier = Modifier
) {
    if (heartRateData.isEmpty()) return
    val maxHr = heartRateData.maxOrNull() ?: 140
    val minHr = heartRateData.minOrNull() ?: 60
    val range = (maxHr - minHr).coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (heartRateData.size - 1).coerceAtLeast(1)

        val points = heartRateData.mapIndexed { index, hr ->
            val x = index * spacing
            val fraction = (hr - minHr).toFloat() / range.toFloat()
            val y = height - (fraction * (height - 30.dp.toPx()) + 15.dp.toPx())
            androidx.compose.ui.geometry.Offset(x, y)
        }

        val fillPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(width, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(LimeGreen.copy(alpha = 0.3f), Color.Transparent),
                startY = points.minOf { it.y },
                endY = height
            )
        )

        val strokePath = androidx.compose.ui.graphics.Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = strokePath,
            color = LimeGreen,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        val peakIndex = heartRateData.indexOf(maxHr)
        if (peakIndex in points.indices) {
            val peakPoint = points[peakIndex]
            drawCircle(
                color = LimeGreen,
                radius = 6.dp.toPx(),
                center = peakPoint
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = peakPoint
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search workouts...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = TextMutedDark) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedDark) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LimeGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceAltDark,
            cursorColor = LimeGreen,
            focusedTextColor = OffWhite,
            unfocusedTextColor = OffWhite
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun WorkoutFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) LimeGreen else SurfaceAltDark,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF121212) else OffWhite,
            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SmallStatCard(
    label: String,
    value: String,
    subValue: String,
    isGranted: Boolean = true,
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon = when (label.lowercase()) {
        "sleep" -> "💤"
        "steps" -> "👣"
        else -> "📊"
    }
    BaseCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(LimeTintDark),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 12.sp)
            }
            Text(label, style = Typography.bodySmall, color = TextMutedDark)
        }
        Spacer(Modifier.height(12.dp))

        if (!isGranted && (value == "0" || value == "0h 0m")) {
            Text(
                text = "Connect Health",
                style = Typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = WarningAmber
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Grant permission",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = LimeGreen,
                modifier = Modifier.clickable { onConnectClick() }
            )
        } else {
            Text(
                text = value,
                style = Typography.displayLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = OffWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(subValue, style = Typography.bodySmall, color = LimeGreen)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalorieRing() {
    GymFitnessTheme {
        Box(modifier = Modifier.background(PageBg).padding(16.dp)) {
            CalorieRing(current = 1450, target = 2200)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMacroStatsRow() {
    GymFitnessTheme {
        Box(modifier = Modifier.background(PageBg).padding(16.dp)) {
            MacroStatsRow(
                protein = 110f,
                proteinTarget = 140f,
                carbs = 180f,
                carbsTarget = 220f,
                fat = 50f,
                fatsTarget = 65f
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDailyProgressCard() {
    GymFitnessTheme {
        Box(modifier = Modifier.background(PageBg).padding(16.dp)) {
            DailyProgressCard(
                caloriesEaten = 1450,
                caloriesTarget = 2200,
                protein = 110f,
                proteinTarget = 140f,
                carbs = 180f,
                carbsTarget = 220f,
                fat = 50f,
                fatsTarget = 65f
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCodeChip() {
    GymFitnessTheme {
        Box(modifier = Modifier.background(PageBg).padding(16.dp)) {
            CodeChip(code = "D06523")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStepsBarChart() {
    GymFitnessTheme {
        Box(modifier = Modifier.background(PageBg).padding(16.dp)) {
            val sampleData = listOf(
                DayStepEntry(LocalDate.now().minusDays(6), "Mon", 6200),
                DayStepEntry(LocalDate.now().minusDays(5), "Tue", 7800),
                DayStepEntry(LocalDate.now().minusDays(4), "Wed", 8500),
                DayStepEntry(LocalDate.now().minusDays(3), "Thu", 5100),
                DayStepEntry(LocalDate.now().minusDays(2), "Fri", 9400),
                DayStepEntry(LocalDate.now().minusDays(1), "Sat", 11200),
                DayStepEntry(LocalDate.now(), "Sun", 8450)
            )
            BaseCard {
                StepsBarChart(weeklySteps = sampleData, isHealthConnectGranted = true)
            }
        }
    }
}
