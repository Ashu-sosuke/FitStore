package com.example.gymfitness.presentation.screen.meals

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.data.remote.api.toRotatedBitmap
import com.example.gymfitness.domain.models.PlannedMealItem
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
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MealScreen(navController: NavController, viewModel: MealViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var isScannerActive by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var showFoodLibrary by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Personalized Diet Plan, 1: Logged Meals Today

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val scannedResult by viewModel.scannedFood.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    val scanAlternatives by viewModel.scanAlternatives.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val personalizedPlan by viewModel.personalizedPlan.collectAsState()

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    isScannerActive = true
                    viewModel.analyzeCapturedBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("GALLERY", "Failed to decode image: ${e.message}")
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {
        if (isScannerActive) {
            // CAMERA SCANNER OVERLAY
            val infiniteTransition = rememberInfiniteTransition(label = "scanner")
            val laserPosition by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laser"
            )

            var triggerCapture by remember { mutableStateOf(false) }

            Box(Modifier.fillMaxSize()) {
                if (capturedBitmap != null) {
                    // Instantly display the saved/frozen photo
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Meal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (cameraPermissionState.status.isGranted) {
                    CameraCaptureOverlay(
                        isFlashEnabled = isFlashEnabled,
                        triggerCapture = triggerCapture,
                        onPhotoCaptured = { bitmap ->
                            triggerCapture = false
                            capturedBitmap = bitmap
                            viewModel.analyzeCapturedBitmap(bitmap)
                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("Camera permission is required to scan food items", color = Color.White, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { cameraPermissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }

                // Camera Cutout Mask & Laser line (drawn during viewfinder and background scan)
                if (scanError == null || isAnalyzing) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    ) {
                        drawRect(color = Color.Black.copy(alpha = if (capturedBitmap != null) 0.50f else 0.72f))
                        val sizePx = 290.dp.toPx()
                        val left = (size.width - sizePx) / 2
                        val top = (size.height - sizePx) / 2 - 40.dp.toPx()
                        val cornerRadiusPx = 32.dp.toPx()

                        // Cutout
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                        )

                        // Glowing border
                        drawRoundRect(
                            color = SunsetOrange,
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(sizePx, sizePx),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Laser line
                        val laserY = top + sizePx * laserPosition
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    SunsetOrange.copy(alpha = 0.0f),
                                    SunsetOrange,
                                    SunsetOrange.copy(alpha = 0.0f)
                                ),
                                startY = laserY - 8.dp.toPx(),
                                endY = laserY + 8.dp.toPx()
                            ),
                            start = androidx.compose.ui.geometry.Offset(left + 12.dp.toPx(), laserY),
                            end = androidx.compose.ui.geometry.Offset(left + sizePx - 12.dp.toPx(), laserY),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                }

                // Top Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isScannerActive = false
                            capturedBitmap = null
                            viewModel.clearResult()
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = { isFlashEnabled = !isFlashEnabled },
                            enabled = capturedBitmap == null,
                            modifier = Modifier.background(
                                if (isFlashEnabled) SunsetOrange else Color.Black.copy(alpha = 0.6f),
                                CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash Toggle",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Image", tint = Color.White)
                        }
                    }
                }

                // Instruction Callout (when camera preview is idle)
                if (capturedBitmap == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 280.dp)
                    ) {
                        Text(
                            text = "Aim at food & tap Camera Shutter to scan",
                            color = Color.White,
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Status or Bottom Controls Area
                if (capturedBitmap != null && isAnalyzing) {
                    // Scanning in Background Status Banner
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Black.copy(alpha = 0.88f),
                        border = BorderStroke(2.dp, SunsetOrange),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = SunsetOrange,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "⚡ AI SCANNING IN BACKGROUND",
                                    color = SunsetOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                "Image saved! AI Vision is analyzing your meal & calculating exact nutrition...",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = SunsetOrange,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                } else if (capturedBitmap != null && scanError != null && scannedResult == null) {
                    // Error Card with Retake & Search Options
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Black.copy(alpha = 0.92f),
                        border = BorderStroke(2.dp, ErrorRed),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Error",
                                tint = ErrorRed,
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                "Could Not Recognize Food",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = scanError ?: "The meal could not be identified with confidence. Ensure good lighting or search directly.",
                                color = TextMutedDark,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        capturedBitmap = null
                                        viewModel.clearScanError()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📸 Retake", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = {
                                        capturedBitmap = null
                                        isScannerActive = false
                                        viewModel.clearScanError()
                                        showFoodLibrary = true
                                    },
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🔍 Search", color = Color.White)
                                }
                            }
                        }
                    }
                } else if (capturedBitmap == null) {
                    // Live Camera Viewfinder Controls
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Gallery shortcut
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White)
                            }

                            // Large Shutter Button
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(4.dp, SunsetOrange, CircleShape)
                                    .clickable { triggerCapture = true }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(SunsetOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }

                            // Close scanner shortcut
                            IconButton(
                                onClick = {
                                    isScannerActive = false
                                    capturedBitmap = null
                                    viewModel.clearResult()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // MAIN DIET / MEAL SCREEN
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomNavBar(navController = navController) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val consumedCalories = todayMeals.sumOf { it.calories.toDouble() }.roundToInt()
                    val consumedProtein = todayMeals.sumOf { it.proteinG.toDouble() }.roundToInt()
                    val consumedCarbs = todayMeals.sumOf { it.carbsG.toDouble() }.roundToInt()
                    val consumedFats = todayMeals.sumOf { it.fatG.toDouble() }.roundToInt()

                    val targetCalories = (personalizedPlan?.targetCalories ?: userProfile?.dailyCalorieTarget ?: 2400.0).roundToInt()
                    val targetProtein = (personalizedPlan?.proteinTargetG ?: userProfile?.proteinTarget ?: 150.0).roundToInt()
                    val targetCarbs = (personalizedPlan?.carbsTargetG ?: userProfile?.carbsTarget ?: 250.0).roundToInt()
                    val targetFats = (personalizedPlan?.fatsTargetG ?: userProfile?.fatsTarget ?: 70.0).roundToInt()
                    val waterTarget = personalizedPlan?.waterTargetLiters ?: 3.0

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Nutrition & Diet", style = Typography.displayLarge, color = InkBlack)
                            Text("Certified IFCT 2017 & AI Meal Planner", style = Typography.bodySmall, color = TextMuted)
                        }

                        // Camera Scan Button
                        IconButton(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) {
                                    capturedBitmap = null
                                    viewModel.clearResult()
                                    viewModel.clearScanError()
                                    isScannerActive = true
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SunsetOrange)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Scan Food", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // DAILY CALORIE & MACRO TARGET PROGRESS HUB
                    DailyNutritionHubCard(
                        targetCalories = targetCalories,
                        consumedCalories = consumedCalories,
                        consumedProtein = consumedProtein,
                        targetProtein = targetProtein,
                        consumedCarbs = consumedCarbs,
                        targetCarbs = targetCarbs,
                        consumedFats = consumedFats,
                        targetFats = targetFats,
                        waterTargetLiters = waterTarget
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // TWO TABS: [ Personalized Diet Plan 🥗 ]  |  [ Logged Today 🍽️ ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceAlt)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) SunsetOrange else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Personalized Diet 🥗",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) Color.White else InkBlack
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) SunsetOrange else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Logged Today (${todayMeals.size}) 🍽️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) Color.White else InkBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // TAB 0: PERSONALIZED DIET PLAN BY NUTRITIONIST
                    if (selectedTab == 0) {
                        val plan = personalizedPlan
                        if (plan != null) {
                            // Plan Title & Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                border = BorderStroke(1.dp, StrokeSoft)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(plan.title, style = Typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = InkBlack)
                                            Text(plan.dietSummary, style = Typography.bodySmall, color = TextMuted)
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.regenerateMealPlan()
                                                Toast.makeText(context, "Regenerating your diet plan...", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate Plan", tint = SunsetOrange)
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(OrangeTint)
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("💡", fontSize = 16.sp)
                                            Spacer(Modifier.width(8.dp))
                                            Text(plan.nutritionistNotes, fontSize = 12.sp, color = InkBlack, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("Scheduled Daily Meals", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = InkBlack)
                            Spacer(Modifier.height(12.dp))

                            // Planned Meal Items
                            plan.meals.forEach { item ->
                                PlannedMealCard(
                                    item = item,
                                    onLogMeal = {
                                        viewModel.logPlannedMeal(item)
                                        Toast.makeText(context, "Logged ${item.mealType} to today's intake!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        } else {
                            // Empty Diet Plan Fallback
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                border = BorderStroke(1.dp, StrokeSoft)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🥗", fontSize = 42.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Generate Personalized Diet Plan", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = InkBlack)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Get a certified nutritionist meal plan tailored to your biometrics, diet preference, and fitness goal.", textAlign = TextAlign.Center, color = TextMuted, fontSize = 13.sp)
                                    Spacer(Modifier.height(18.dp))
                                    PrimaryButton(
                                        text = "Generate Nutrition Plan ⚡",
                                        onClick = {
                                            viewModel.regenerateMealPlan()
                                            Toast.makeText(context, "Crafting your diet plan...", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // TAB 1: LOGGED MEALS TODAY
                    if (selectedTab == 1) {
                        // Quick Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        capturedBitmap = null
                                        viewModel.clearResult()
                                        viewModel.clearScanError()
                                        isScannerActive = true
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Scan Food 📸", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { showFoodLibrary = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceAlt, contentColor = InkBlack),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, StrokeSoft)
                            ) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Search Food 🔍", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        if (todayMeals.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                border = BorderStroke(1.dp, StrokeSoft)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🍽️", fontSize = 40.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("No meals logged yet today", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = InkBlack)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Snap a picture of your meal or log directly from your Personalized Diet tab.", color = TextMuted, textAlign = TextAlign.Center, fontSize = 13.sp)
                                }
                            }
                        } else {
                            todayMeals.forEach { meal ->
                                MealCard(meal)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // COMPREHENSIVE QUANTITY SELECTION MODAL POPUP
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
                ComprehensiveQuantityPopup(
                    food = food,
                    alternatives = scanAlternatives,
                    onSelectAlternative = { altName -> viewModel.selectAlternativeFood(altName) },
                    onAdd = { scaledMeal, qtyGrams, mealType ->
                        viewModel.logScannedFood(scaledMeal, qtyGrams, mealType)
                        Toast.makeText(context, "Added ${scaledMeal.name} (${qtyGrams.toInt()}g) to today's log!", Toast.LENGTH_SHORT).show()
                        isScannerActive = false
                        capturedBitmap = null
                    },
                    onCancel = {
                        viewModel.clearResult()
                        capturedBitmap = null
                    }
                )
            }
        }

        // Food Library Modal Bottom Sheet
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

/**
 * CameraX Capture View that connects to ImageCapture and captures photos on shutter click.
 */
@Composable
fun CameraCaptureOverlay(
    isFlashEnabled: Boolean,
    triggerCapture: Boolean,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(camera, isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    LaunchedEffect(triggerCapture) {
        if (triggerCapture) {
            imageCapture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        val bitmap = imageProxy.toRotatedBitmap()
                        imageProxy.close()
                        if (bitmap != null) {
                            onPhotoCaptured(bitmap)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CAMERA", "Photo capture error: ${exception.message}", exception)
                    }
                }
            )
        }
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
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                    if (camera?.cameraInfo?.hasFlashUnit() == true) {
                        camera?.cameraControl?.enableTorch(isFlashEnabled)
                    }
                } catch (e: Exception) {
                    Log.e("CAMERA", "Binding failed: ${e.message}")
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Daily Nutrition Progress Card showing Calories, Macros, and Water Target
 */
@Composable
fun DailyNutritionHubCard(
    targetCalories: Int,
    consumedCalories: Int,
    consumedProtein: Int,
    targetProtein: Int,
    consumedCarbs: Int,
    targetCarbs: Int,
    consumedFats: Int,
    targetFats: Int,
    waterTargetLiters: Double
) {
    val remainingCalories = (targetCalories - consumedCalories).coerceAtLeast(0)
    val calProgress = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, StrokeSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main Calorie Counter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Calorie Budget", style = Typography.bodySmall, color = TextMuted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$consumedCalories", style = Typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold), color = SunsetOrange)
                        Text(" / $targetCalories kcal", style = Typography.titleMedium, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceAlt)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$remainingCalories", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = InkBlack)
                        Text("kcal left", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Main Calorie Progress Bar
            LinearProgressIndicator(
                progress = { calProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SunsetOrange,
                trackColor = StrokeSoft
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = StrokeSoft)
            Spacer(Modifier.height(14.dp))

            // Macro Targets Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroTargetPill(label = "Protein", consumed = consumedProtein, target = targetProtein, unit = "g", color = SunsetOrange, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                MacroTargetPill(label = "Carbs", consumed = consumedCarbs, target = targetCarbs, unit = "g", color = Color(0xFF00B0FF), modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                MacroTargetPill(label = "Fats", consumed = consumedFats, target = targetFats, unit = "g", color = Color(0xFFFFB300), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            // Water Target Chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE1F5FE))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💧", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Hydration Goal: ${waterTargetLiters}L pure water throughout the day",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0277BD)
                    )
                }
            }
        }
    }
}

@Composable
fun MacroTargetPill(
    label: String,
    consumed: Int,
    target: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceAlt)
            .padding(10.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("$consumed / $target$unit", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = StrokeSoft
        )
    }
}

/**
 * Planned Meal Item Card in the Personalized Nutritionist Diet Plan
 */
@Composable
fun PlannedMealCard(
    item: PlannedMealItem,
    onLogMeal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, StrokeSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badge & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.mealType.uppercase(),
                        color = SunsetOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "${item.calories.roundToInt()} kcal",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = SunsetOrange
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(item.title, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = InkBlack)
            Spacer(Modifier.height(4.dp))
            Text(item.foodDescription, style = Typography.bodyMedium, color = TextMuted)

            Spacer(Modifier.height(10.dp))

            // Macro details row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceAlt)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Portion: ${item.portionGrams.toInt()}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                Text("P: ${item.proteinG}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SunsetOrange)
                Text("C: ${item.carbsG}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00B0FF))
                Text("F: ${item.fatsG}g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
            }

            Spacer(Modifier.height(8.dp))

            // Nutritionist Pro Tip Callout
            Row(verticalAlignment = Alignment.Top) {
                Text("💡", fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Text(item.nutritionistTip, fontSize = 11.sp, color = TextMuted, lineHeight = 16.sp)
            }

            Spacer(Modifier.height(14.dp))

            // Quick One-Tap Log Button
            Button(
                onClick = onLogMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Log This Meal to Today's Intake", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Comprehensive Quantity Selection Bottom Sheet with Grams, Servings, Macro scaling, and Add button
 */
@Composable
fun ComprehensiveQuantityPopup(
    food: MealEntity,
    alternatives: List<String> = emptyList(),
    onSelectAlternative: (String) -> Unit = {},
    onAdd: (MealEntity, Float, String) -> Unit,
    onCancel: () -> Unit
) {
    var quantityGrams by remember { mutableFloatStateOf(150f) }
    var quantityText by remember { mutableStateOf("150") }
    var selectedMealType by remember { mutableStateOf(food.mealType.lowercase()) }

    val multiplier = (quantityGrams / 100f).coerceAtLeast(0.01f)
    val calculatedCalories = (food.calories * multiplier).roundToInt()
    val calculatedProtein = ((food.proteinG * multiplier) * 10).roundToInt() / 10f
    val calculatedCarbs = ((food.carbsG * multiplier) * 10).roundToInt() / 10f
    val calculatedFat = ((food.fatG * multiplier) * 10).roundToInt() / 10f

    val foodIcon = when {
        food.name.contains("egg", ignoreCase = true) -> "🍳"
        food.name.contains("chicken", ignoreCase = true) -> "🍗"
        food.name.contains("milk", ignoreCase = true) -> "🥛"
        food.name.contains("paneer", ignoreCase = true) -> "🧀"
        food.name.contains("rice", ignoreCase = true) || food.name.contains("biryani", ignoreCase = true) -> "🍚"
        food.name.contains("dal", ignoreCase = true) -> "🍲"
        food.name.contains("roti", ignoreCase = true) || food.name.contains("chapati", ignoreCase = true) || food.name.contains("paratha", ignoreCase = true) -> "🫓"
        food.name.contains("dosa", ignoreCase = true) || food.name.contains("idli", ignoreCase = true) -> "🥞"
        food.name.contains("samosa", ignoreCase = true) -> "🥟"
        food.name.contains("palak", ignoreCase = true) || food.name.contains("broccoli", ignoreCase = true) -> "🥦"
        food.name.contains("banana", ignoreCase = true) -> "🍌"
        else -> "🥗"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(1.dp, StrokeSoft)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Food Header
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
                        text = "Standard Ref: ${food.calories.toInt()} kcal / 100g",
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

            // Top Alternative Matches
            if (alternatives.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "SWITCH MATCH",
                    style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = TextMuted
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    alternatives.take(3).forEach { alt ->
                        val isSelected = food.name.equals(alt, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrangeTint else SurfaceAlt)
                                .border(1.dp, if (isSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(8.dp))
                                .clickable { onSelectAlternative(alt) }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alt,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = if (isSelected) SunsetOrange else InkBlack,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = StrokeSoft)
            Spacer(Modifier.height(14.dp))

            // Comprehensive Quantity Selector Header
            Text(
                text = "PORTION & QUANTITY",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextMuted
            )
            Spacer(Modifier.height(8.dp))

            // Step adjustment and Direct Number Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus 25g
                IconButton(
                    onClick = {
                        val newQty = (quantityGrams - 25f).coerceAtLeast(10f)
                        quantityGrams = newQty
                        quantityText = newQty.toInt().toString()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceAlt)
                        .border(1.dp, StrokeSoft, RoundedCornerShape(12.dp))
                ) {
                    Text("-25g", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = InkBlack)
                }

                // Number textfield
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
                        .height(50.dp),
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

                // Plus 25g
                IconButton(
                    onClick = {
                        val newQty = (quantityGrams + 25f).coerceAtMost(2000f)
                        quantityGrams = newQty
                        quantityText = newQty.toInt().toString()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceAlt)
                        .border(1.dp, StrokeSoft, RoundedCornerShape(12.dp))
                ) {
                    Text("+25g", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = InkBlack)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quick Preset Gram Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(50f, 100f, 150f, 200f, 250f, 300f).forEach { preset ->
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

            Spacer(Modifier.height(8.dp))

            // Common Household Serving Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("🥣 Bowl", 150f),
                    Pair("🍽️ Plate", 300f),
                    Pair("🫓 Roti/Pc", 50f),
                    Pair("🥛 Cup", 200f)
                ).forEach { (label, grams) ->
                    val isSel = (quantityGrams == grams)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) OrangeTint else CardSurface)
                            .border(1.dp, if (isSel) SunsetOrange else StrokeSoft, RoundedCornerShape(8.dp))
                            .clickable {
                                quantityGrams = grams
                                quantityText = grams.toInt().toString()
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) SunsetOrange else InkBlack
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Dynamic Scaled Macros Box
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
                MacroBadge(label = "Fats", value = String.format("%.1f", calculatedFat), unit = "g", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(14.dp))

            // Meal Type Chips
            Text(
                text = "LOG UNDER MEAL",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextMuted
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("breakfast", "🌅 Breakfast"),
                    Pair("lunch", "☀️ Lunch"),
                    Pair("snack", "🍎 Snack"),
                    Pair("dinner", "🌙 Dinner")
                ).forEach { (key, display) ->
                    val isTypeSelected = (selectedMealType == key)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTypeSelected) SunsetOrange else SurfaceAlt)
                            .border(1.dp, if (isTypeSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(8.dp))
                            .clickable { selectedMealType = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display,
                            color = if (isTypeSelected) Color.White else InkBlack,
                            style = Typography.labelSmall.copy(fontWeight = if (isTypeSelected) FontWeight.Bold else FontWeight.Medium)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ADD TO LOG BUTTON
            PrimaryButton(
                text = "Add to Meal Log • $calculatedCalories kcal",
                onClick = {
                    onAdd(food, quantityGrams, selectedMealType)
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

@Composable
fun FoodLibraryList(viewModel: MealViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var showCustomForm by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFats by remember { mutableStateOf("") }
    var customMealType by remember { mutableStateOf("lunch") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Search & Add Food", style = Typography.displayMedium, color = InkBlack)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = InkBlack)
            }
        }

        Spacer(Modifier.height(16.dp))

        PrimaryInputField(
            value = query,
            onValueChange = { query = it; viewModel.searchFood(it) },
            label = "Search Food (e.g. Chicken, Paneer, Rice, Dal)..."
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
                Text("Food not found. Add custom food to your shared database.", color = TextMuted)
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Add Custom Food", onClick = { customName = query; showCustomForm = true })
            } else {
                Text("Popular Verified Foods", style = Typography.titleMedium, color = InkBlack)
                Spacer(Modifier.height(8.dp))
                val popular = listOf(
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Egg (Boiled)", 155.0, 13.0, 1.1, 11.0),
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Chicken Breast", 165.0, 31.0, 0.0, 3.6),
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Paneer", 265.0, 18.3, 1.2, 20.8),
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Dal Tadka", 120.0, 6.0, 16.0, 3.5),
                    com.example.gymfitness.data.remote.dto.NutrientDto(null, "Brown Rice", 111.0, 2.6, 23.0, 0.9)
                )
                popular.forEach { result ->
                    SearchResultItem(result, viewModel, onDismiss)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showCustomForm = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Or add custom food profile", color = SunsetOrange)
                }
            }
        }

        AnimatedVisibility(visible = showCustomForm) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                HorizontalDivider(color = StrokeSoft)
                Spacer(Modifier.height(16.dp))
                Text("Add Custom Food Item", style = Typography.titleLarge, color = InkBlack)
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
    var quantityGrams by remember { mutableFloatStateOf(100f) }
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