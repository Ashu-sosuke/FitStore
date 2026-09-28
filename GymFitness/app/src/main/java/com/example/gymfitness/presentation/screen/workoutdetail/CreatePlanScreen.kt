package com.example.gymfitness.presentation.screen.workoutdetail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.CategoryBadge
import com.example.gymfitness.presentation.components.GhostButton
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.presentation.viewmodel.WorkoutViewModel
import com.example.gymfitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePlanScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(true) }
    var planName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("5-Day Classic Split (Bro Split)") }
    var selectedDuration by remember { mutableStateOf("4 Week Program") }
    var intensity by remember { mutableStateOf("Advanced") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val splitTemplates = listOf(
        "5-Day Classic Split (Bro Split)",
        "Push Pull Legs (PPL)",
        "Upper / Lower Split (4 Days)",
        "Full Body Compound Split (3 Days)"
    )
    val durations = listOf("1 Week Split", "4 Week Program", "8 Week Program", "12 Week Program")
    val intensities = listOf("Beginner", "Moderate", "Advanced")

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                navController.popBackStack()
            },
            sheetState = sheetState,
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = StrokeDark) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Create Workout Split",
                    style = Typography.displayLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Black),
                    color = OffWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure a complete multi-day program covering all mandatory muscle groups.",
                    style = Typography.bodyMedium,
                    color = TextMutedDark
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                Text("Split Program Name", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                PrimaryInputField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = "e.g. 4-Week Hypertrophy Program",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))
                
                Text("Complete Split Architecture (All Muscles Included)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    splitTemplates.forEach { template ->
                        val isSel = selectedTemplate == template
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) LimeTintDark else SurfaceAltDark)
                                .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(12.dp))
                                .clickable { selectedTemplate = template }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FitnessCenter,
                                        contentDescription = null,
                                        tint = if (isSel) LimeGreen else TextMutedDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = template,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) LimeGreen else OffWhite,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = when {
                                                template.contains("Bro") -> "Chest • Back • Shoulders • Arms • Legs & Core"
                                                template.contains("Push") -> "Push (Chest/Delts/Triceps) • Pull (Back/Biceps) • Legs & Abs"
                                                template.contains("Upper") -> "Upper Body Power/Hypertrophy • Lower Body & Core"
                                                else -> "Full Body Compound Exercises A, B & C"
                                            },
                                            fontSize = 11.sp,
                                            color = TextMutedDark
                                        )
                                    }
                                }

                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = LimeGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Program Duration", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { dur ->
                        val isSel = selectedDuration == dur
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedDuration = dur },
                            label = { Text(dur, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LimeGreen,
                                selectedLabelColor = Color(0xFF121212),
                                containerColor = SurfaceAltDark,
                                labelColor = OffWhite
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Intensity Level", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    intensities.forEach { lvl ->
                        val isSel = intensity == lvl
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) LimeGreen else SurfaceAltDark)
                                .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(8.dp))
                                .clickable { intensity = lvl },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lvl,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF121212) else OffWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Full-body mandatory coverage confirmation banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = LimeTintDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Mandatory full-body muscle coverage: Chest, Back, Shoulders, Arms, Quads, Hamstrings, Calves & Abs included.",
                            color = OffWhite,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Save Workout Split Program 💾",
                    onClick = {
                        val finalName = planName.ifBlank { "$selectedTemplate ($selectedDuration)" }
                        viewModel.createMultiDaySplitProgram(
                            programName = finalName,
                            splitTemplate = selectedTemplate,
                            durationWeeks = selectedDuration,
                            intensity = intensity,
                            onSuccess = {
                                Toast.makeText(context, "Full split '$finalName' saved to library!", Toast.LENGTH_LONG).show()
                                showSheet = false
                                navController.popBackStack()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                GhostButton(
                    text = "Cancel",
                    onClick = {
                        showSheet = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize())
    }
}