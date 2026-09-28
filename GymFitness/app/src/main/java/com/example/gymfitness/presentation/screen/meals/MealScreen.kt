package com.example.gymfitness.presentation.screen.meals

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.data.remote.api.toRotatedBitmap
import com.example.gymfitness.presentation.components.BaseCard
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.viewmodel.MealViewModel
import com.example.gymfitness.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalDate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MealScreen(navController: NavController, viewModel: MealViewModel = hiltViewModel()) {
    var isScannerActive by remember { mutableStateOf(false) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var showFoodLibrary by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val scannedResult by viewModel.scannedFood.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {
        if (isScannerActive) {
            val isAnalyzing by viewModel.isAnalyzing.collectAsState()
            val infiniteTransition = rememberInfiniteTransition(label = "scanner")
            val laserPosition by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laser"
            )

            Box(Modifier.fillMaxSize()) {
                if (cameraPermissionState.status.isGranted) {
                    CameraPreviewOverlay(
                        isFlashEnabled = isFlashEnabled,
                        isAnalyzingEnabled = !isAnalyzing && scannedResult == null,
                        onFrameCaptured = { bitmap ->
                            viewModel.identifyFoodWithFastAPI(bitmap)
                        },
                        onBarcodeDetected = { barcode ->
                            Log.d("SCANNER", "Barcode detected: $barcode")
                        }
                    )
                }
                
                // Camera Mask Overlay
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                ) {
                    drawRect(color = Color.Black.copy(alpha = 0.7f))
                    val sizePx = 280.dp.toPx()
                    val left = (size.width - sizePx) / 2
                    val top = (size.height - sizePx) / 2
                    val cornerRadiusPx = 32.dp.toPx()

                    // Clear the cutout
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )

                    // Draw the glowing neon border
                    drawRoundRect(
                        color = SunsetOrange,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Laser scanning line inside the cutout
                    val laserY = top + sizePx * laserPosition
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SunsetOrange.copy(alpha = 0.0f),
                                SunsetOrange,
                                SunsetOrange.copy(alpha = 0.0f)
                            ),
                            startY = laserY - 6.dp.toPx(),
                            endY = laserY + 6.dp.toPx()
                        ),
                        start = androidx.compose.ui.geometry.Offset(left + 12.dp.toPx(), laserY),
                        end = androidx.compose.ui.geometry.Offset(left + sizePx - 12.dp.toPx(), laserY),
                        strokeWidth = 4.dp.toPx()
                    )
                }

                // Instructions and analyzing feedback
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 370.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Align food or barcode inside the frame",
                        color = Color.White,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (isAnalyzing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = SunsetOrange,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Analyzing item...",
                                color = SunsetOrange,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isScannerActive = false },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkBlack)
                    }

                    IconButton(
                        onClick = { isFlashEnabled = !isFlashEnabled },
                        modifier = Modifier.background(if (isFlashEnabled) SunsetOrange else Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash Toggle",
                            tint = if (isFlashEnabled) Color.White else InkBlack
                        )
                    }
                }
            }
        } else {
            // MAIN UI
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomNavBar(navController = navController) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    val todayMeals by viewModel.todayMeals.collectAsState()
                    val totalCalories = todayMeals.sumOf { it.calories.toDouble() }.toInt()

                    MainHeader(onScanClick = { 
                        if (cameraPermissionState.status.isGranted) {
                            isScannerActive = true 
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    })
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    DateSelector()
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    BaseCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Intake", style = Typography.bodyMedium, color = TextMuted)
                                Text("$totalCalories kcal", style = Typography.displayMedium, color = InkBlack)
                            }
                            Button(
                                onClick = { showFoodLibrary = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Manually")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Today's Meals", style = Typography.titleLarge, color = InkBlack)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (todayMeals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("No meals logged yet", style = Typography.bodyLarge, color = TextMuted)
                        }
                    } else {
                        todayMeals.forEach { meal ->
                            MealCard(meal)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // Scan Result Overlay
        AnimatedVisibility(
            visible = scannedResult != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .navigationBarsPadding(),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            scannedResult?.let { food ->
                ResultPopup(
                    food = food,
                    onAdd = { calculatedMeal ->
                        viewModel.saveMealToRoom(calculatedMeal)
                        viewModel.clearResult()
                        isScannerActive = false
                    },
                    onCancel = { viewModel.clearResult() }
                )
            }
        }

        if (showFoodLibrary) {
            ModalBottomSheet(
                onDismissRequest = { showFoodLibrary = false },
                sheetState = sheetState,
                containerColor = CardSurface
            ) {
                FoodLibraryList(viewModel = viewModel, onDismiss = { showFoodLibrary = false })
            }
        }
    }
}

@Composable
fun ResultPopup(
    food: MealEntity,
    onAdd: (MealEntity) -> Unit,
    onCancel: () -> Unit
) {
    var quantityGrams by remember { mutableStateOf(100f) }
    var quantityText by remember { mutableStateOf("100") }
    var selectedMealType by remember { mutableStateOf(food.mealType.lowercase()) }

    val multiplier = (quantityGrams / 100f).coerceAtLeast(0.01f)
    val calculatedCalories = (food.calories * multiplier).toInt()
    val calculatedProtein = food.proteinG * multiplier
    val calculatedCarbs = food.carbsG * multiplier
    val calculatedFat = food.fatG * multiplier

    val foodIcon = when (food.name.lowercase()) {
        "egg" -> "🍳"
        "chicken" -> "🍗"
        "milk" -> "🥛"
        "broccoli" -> "🥦"
        "avocado" -> "🥑"
        "salmon" -> "🐟"
        "rice" -> "🍚"
        "apple" -> "🍎"
        "banana" -> "🍌"
        "oats" -> "🥣"
        else -> "🥗"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header: Food Icon, Name, Base Info, and Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(OrangeTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(foodIcon, fontSize = 28.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InkBlack
                    )
                    Text(
                        text = "Base: ${food.calories.toInt()} kcal / 100g",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceAlt, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = InkBlack, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = StrokeSoft)
            Spacer(Modifier.height(14.dp))

            // Quantity / Portion Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PORTION / QUANTITY",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted
                )
                Text(
                    text = "${quantityGrams.toInt()} grams",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SunsetOrange
                )
            }

            Spacer(Modifier.height(10.dp))

            // Quantity Stepper and Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus 25g button
                IconButton(
                    onClick = {
                        val newQty = (quantityGrams - 25f).coerceAtLeast(10f)
                        quantityGrams = newQty
                        quantityText = newQty.toInt().toString()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceAlt)
                        .border(1.dp, StrokeSoft, RoundedCornerShape(12.dp))
                ) {
                    Text("-25g", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = InkBlack)
                }

                // Quantity Editable TextField
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(4)
                        quantityText = filtered
                        val parsed = filtered.toFloatOrNull()
                        if (parsed != null && parsed > 0) {
                            quantityGrams = parsed
                        }
                    },
                    modifier = Modifier
                        .width(130.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        focusedBorderColor = SunsetOrange,
                        unfocusedBorderColor = StrokeSoft,
                        focusedTextColor = InkBlack,
                        unfocusedTextColor = InkBlack
                    ),
                    textStyle = Typography.titleMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    suffix = { Text("g", color = SunsetOrange, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Plus 25g button
                IconButton(
                    onClick = {
                        val newQty = (quantityGrams + 25f).coerceAtMost(2000f)
                        quantityGrams = newQty
                        quantityText = newQty.toInt().toString()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceAlt)
                        .border(1.dp, StrokeSoft, RoundedCornerShape(12.dp))
                ) {
                    Text("+25g", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = InkBlack)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quick Preset Chips (50g, 100g, 150g, 200g, 250g)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(50f, 100f, 150f, 200f, 250f).forEach { preset ->
                    val isSelected = (quantityGrams == preset)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SunsetOrange else SurfaceAlt)
                            .border(1.dp, if (isSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(8.dp))
                            .clickable {
                                quantityGrams = preset
                                quantityText = preset.toInt().toString()
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset.toInt()}g",
                            style = Typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                            color = if (isSelected) Color.White else InkBlack
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Live Calculated Macro Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceAlt)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroBadge(label = "Calories", value = "$calculatedCalories", unit = "kcal", highlight = true, modifier = Modifier.weight(1.2f))
                MacroBadge(label = "Protein", value = String.format("%.1f", calculatedProtein), unit = "g", modifier = Modifier.weight(1f))
                MacroBadge(label = "Carbs", value = String.format("%.1f", calculatedCarbs), unit = "g", modifier = Modifier.weight(1f))
                MacroBadge(label = "Fat", value = String.format("%.1f", calculatedFat), unit = "g", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(14.dp))

            // Meal Type Selection Chips
            Text(
                text = "LOG AS",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextMuted
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("breakfast", "lunch", "dinner", "snack").forEach { type ->
                    val isTypeSelected = (selectedMealType == type)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTypeSelected) OrangeTint else CardSurface)
                            .border(1.dp, if (isTypeSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(8.dp))
                            .clickable { selectedMealType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.replaceFirstChar { it.uppercase() },
                            color = if (isTypeSelected) SunsetOrange else InkBlack,
                            style = Typography.labelSmall.copy(fontWeight = if (isTypeSelected) FontWeight.Bold else FontWeight.Medium)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Add / Save Button
            PrimaryButton(
                text = "Log Meal • $calculatedCalories kcal",
                onClick = {
                    val finalMeal = food.copy(
                        name = food.name,
                        calories = calculatedCalories.toFloat(),
                        proteinG = calculatedProtein,
                        carbsG = calculatedCarbs,
                        fatG = calculatedFat,
                        mealType = selectedMealType
                    )
                    onAdd(finalMeal)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MacroBadge(
    label: String,
    value: String,
    unit: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (highlight) SunsetOrange else InkBlack
            )
            Spacer(Modifier.width(1.dp))
            Text(
                text = unit,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = if (highlight) SunsetOrange else TextMuted
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted
        )
    }
}

@Composable
fun MainHeader(onScanClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Nutrition Log", style = Typography.displayLarge, color = InkBlack)
        IconButton(
            onClick = onScanClick, 
            modifier = Modifier.size(50.dp).background(SunsetOrange, CircleShape)
        ) {
            Icon(Icons.Default.QrCodeScanner, null, tint = Color.White)
        }
    }
}

@Composable
fun MealCard(meal: MealEntity) {
    BaseCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(45.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceAlt),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(meal.mealType.lowercase()) {
                    "breakfast" -> "🍳"
                    "lunch" -> "🍲"
                    "dinner" -> "🥩"
                    else -> "🍎"
                }
                Text(icon, fontSize = 20.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.name, color = InkBlack, style = Typography.titleMedium)
                Text(meal.mealType.replaceFirstChar { it.uppercase() }, color = TextMuted, style = Typography.bodySmall)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${meal.calories.toInt()} kcal", color = SunsetOrange, style = Typography.titleMedium)
                Text("P:${meal.proteinG.toInt()}g C:${meal.carbsG.toInt()}g", color = TextMuted, style = Typography.bodySmall)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector() {
    val today = LocalDate.now()
    val weekDays = remember { (0..6).map { today.plusDays(it.toLong() - today.dayOfWeek.value + 1) } }
    var selectedDate by remember { mutableStateOf(today) }

    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        weekDays.forEach { date ->
            val isSelected = date == selectedDate
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) SunsetOrange else CardSurface)
                    .clickable { selectedDate = date },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = date.dayOfWeek.name.take(3),
                        color = if (isSelected) Color.White else TextMuted,
                        style = Typography.labelSmall
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (isSelected) Color.White else InkBlack,
                        style = Typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
fun FoodLibraryList(viewModel: MealViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var showCustomForm by remember { mutableStateOf(false) }
    var selectedResultForLogging by remember { mutableStateOf<com.example.gymfitness.data.remote.dto.NutrientDto?>(null) }

    // Custom Form states
    var customName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFats by remember { mutableStateOf("") }
    var customMealType by remember { mutableStateOf("breakfast") }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Log Meal", style = Typography.displayMedium, color = InkBlack)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = InkBlack)
            }
        }

        Spacer(Modifier.height(16.dp))

        PrimaryInputField(
            value = query,
            onValueChange = { query = it; viewModel.searchFood(it) },
            label = "Search food..."
        )

        Spacer(Modifier.height(20.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SunsetOrange)
            }
        } else {
            if (query.isNotBlank() && searchResults.isNotEmpty()) {
                searchResults.forEach { result ->
                    SearchResultItem(result, viewModel, onDismiss)
                }
            } else if (query.isNotBlank() && searchResults.isEmpty()) {
                Text("Food not found. Add custom food.", color = TextMuted)
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Add Custom Food", onClick = { customName = query; showCustomForm = true })
            } else {
                Text("Popular Foods", style = Typography.titleMedium, color = InkBlack)
                Spacer(Modifier.height(8.dp))
                val popular = listOf(
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Egg", 155.0, 13.0, 1.1, 11.0),
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Chicken", 165.0, 31.0, 0.0, 3.6),
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Broccoli", 34.0, 2.8, 7.0, 0.4)
                )
                popular.forEach { result ->
                    SearchResultItem(result, viewModel, onDismiss)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showCustomForm = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Or add custom food", color = SunsetOrange)
                }
            }
        }

        AnimatedVisibility(visible = showCustomForm) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                HorizontalDivider(color = StrokeSoft)
                Spacer(Modifier.height(16.dp))
                Text("Add Custom Food", style = Typography.titleLarge, color = InkBlack)
                Spacer(Modifier.height(16.dp))
                PrimaryInputField(customName, { customName = it }, "Food Name")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryInputField(customCalories, { customCalories = it }, "Calories (kcal)", Modifier.weight(1f))
                    PrimaryInputField(customProtein, { customProtein = it }, "Protein (g)", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryInputField(customCarbs, { customCarbs = it }, "Carbs (g)", Modifier.weight(1f))
                    PrimaryInputField(customFats, { customFats = it }, "Fats (g)", Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Save to Database & Log",
                    onClick = {
                        viewModel.addCustomFoodAndLog(
                            foodName = customName,
                            calories = customCalories.toDoubleOrNull() ?: 0.0,
                            protein = customProtein.toDoubleOrNull() ?: 0.0,
                            carbs = customCarbs.toDoubleOrNull() ?: 0.0,
                            fats = customFats.toDoubleOrNull() ?: 0.0,
                            mealType = customMealType
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SearchResultItem(result: com.example.gymfitness.data.remote.dto.NutrientDto, viewModel: MealViewModel, onDismiss: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var quantityGrams by remember { mutableStateOf(100f) }
    val multiplier = (quantityGrams / 100f).coerceAtLeast(0.01f)
    val scaledCalories = (result.calories * multiplier).toInt()
    val scaledProtein = result.proteinG * multiplier
    val scaledCarbs = result.carbsG * multiplier
    val scaledFats = result.fatsG * multiplier

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = if(expanded) OrangeTint else SurfaceAlt),
        border = BorderStroke(1.dp, if(expanded) SunsetOrange else StrokeSoft),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(result.foodName, color = InkBlack, style = Typography.titleMedium)
                    Text("P: ${String.format("%.1f", scaledProtein)}g | C: ${String.format("%.1f", scaledCarbs)}g | F: ${String.format("%.1f", scaledFats)}g", color = TextMuted, style = Typography.bodySmall)
                }
                Text("$scaledCalories kcal", color = SunsetOrange, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Portion (${quantityGrams.toInt()}g):", color = TextMuted, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(50f, 100f, 150f, 200f).forEach { q ->
                            val isSel = (quantityGrams == q)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) SunsetOrange else CardSurface)
                                    .border(1.dp, if (isSel) SunsetOrange else StrokeSoft, RoundedCornerShape(6.dp))
                                    .clickable { quantityGrams = q }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("${q.toInt()}g", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else InkBlack)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Log as:", color = TextMuted, style = Typography.labelSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("breakfast", "lunch", "dinner", "snack").forEach { type ->
                        Box(
                            modifier = Modifier.weight(1f).border(1.dp, SunsetOrange, RoundedCornerShape(8.dp)).clickable {
                                viewModel.logFoodAsMeal(result, type, quantityGrams)
                                onDismiss()
                            }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type.replaceFirstChar { it.uppercase() }, color = SunsetOrange, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewOverlay(
    isFlashEnabled: Boolean,
    isAnalyzingEnabled: Boolean,
    onFrameCaptured: (Bitmap) -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    val backgroundExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    val currentIsAnalyzingEnabled by rememberUpdatedState(isAnalyzingEnabled)

    DisposableEffect(Unit) {
        onDispose {
            backgroundExecutor.shutdown()
        }
    }

    LaunchedEffect(camera, isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            cameraProviderFuture.addListener({
                var lastAnalysisTime = 0L
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                    it.setAnalyzer(backgroundExecutor) { imageProxy ->
                        val currentTime = System.currentTimeMillis()
                        val shouldAnalyze = currentIsAnalyzingEnabled && (currentTime - lastAnalysisTime >= 2000)

                        if (!shouldAnalyze) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        lastAnalysisTime = currentTime

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.forEach { b -> b.rawValue?.let { v -> onBarcodeDetected(v) } }
                                }
                                .addOnCompleteListener {
                                    val bitmap = imageProxy.toRotatedBitmap()
                                    if (bitmap != null) onFrameCaptured(bitmap)
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                    if (camera?.cameraInfo?.hasFlashUnit() == true) {
                        camera?.cameraControl?.enableTorch(isFlashEnabled)
                    }
                } catch (e: Exception) {}
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}