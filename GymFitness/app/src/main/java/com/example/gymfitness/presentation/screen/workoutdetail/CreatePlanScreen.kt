package com.example.gymfitness.presentation.screen.workoutdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.GhostButton
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(navController: NavController) {
    var showSheet by remember { mutableStateOf(true) }
    var planName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                navController.popBackStack()
            },
            sheetState = sheetState,
            containerColor = CardSurface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = StrokeSoft) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Create New Plan",
                    style = Typography.displayLarge,
                    color = InkBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set your target area, intensity, and duration.",
                    style = Typography.bodyLarge,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                PrimaryInputField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = "Plan Name (e.g., Bro Split)"
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                // Target Area Dropdown (mock)
                PrimaryInputField(
                    value = "Chest & Triceps",
                    onValueChange = { },
                    label = "Target Area"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PrimaryInputField(
                            value = "Advanced",
                            onValueChange = { },
                            label = "Intensity"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PrimaryInputField(
                            value = "45 mins",
                            onValueChange = { },
                            label = "Duration"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                PrimaryButton(
                    text = "Save Plan",
                    onClick = {
                        showSheet = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GhostButton(
                    text = "Cancel",
                    onClick = {
                        showSheet = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        // Fallback empty screen while popping back
        Box(modifier = Modifier.fillMaxSize())
    }
}