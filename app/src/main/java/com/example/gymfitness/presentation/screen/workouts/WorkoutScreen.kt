package com.example.gymfitness.presentation.screen.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.ui.theme.BgDark

@Composable
fun WorkoutScreen(navController: NavController) {

    var selectedTab by remember { mutableStateOf(0) }


    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController
            )
        },
        containerColor = BgDark
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
        ) {

            WorkoutHeader()

            Spacer(modifier = Modifier.height(20.dp))

            CreateWorkoutButton()

            Spacer(modifier = Modifier.height(24.dp))

            RecentHistoryHeader()

            Spacer(modifier = Modifier.height(12.dp))

            WorkoutCard(
                title = "Upper Body A",
                date = "Oct 24, 2023",
                exercises = "7 exercises",
                volume = "Total Volume: 13,450 lbs",
                tag = "COMPLETED"
            )

            Spacer(modifier = Modifier.height(16.dp))

            WorkoutCard(
                title = "Leg Day - Power",
                date = "Oct 22, 2023",
                exercises = "6 exercises",
                volume = "Total Volume: 18,900 lbs",
                tag = "COMPLETED"
            )

            Spacer(modifier = Modifier.height(16.dp))

            WorkoutCard(
                title = "Full Body Core",
                date = "Oct 20, 2023",
                exercises = "10 exercises",
                volume = "Total Volume: 9,400 lbs",
                tag = "COMPLETED"
            )

        }
    }
}

@Composable
fun WorkoutHeader() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Workouts",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CreateWorkoutButton() {

    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2BE070)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {

        Icon(Icons.Default.Add, contentDescription = "")

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            "Create Workout",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun RecentHistoryHeader() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            "Recent History",
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            "View All",
            color = Color(0xFF2BE070),
            fontSize = 14.sp
        )
    }
}

@Composable
fun WorkoutCard(
    title: String,
    date: String,
    exercises: String,
    volume: String,
    tag: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151718)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Column {

            Box {

                Image(
                    painter = painterResource(id = R.drawable._00_rog_zephyrus_s17),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2BE070))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        tag,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                WorkoutInfoRow(R.drawable.gym_1025878, date)

                WorkoutInfoRow(R.drawable.gym_1025878, exercises)

                WorkoutInfoRow(R.drawable.gym_1025878, volume)
            }
        }
    }
}

@Composable
fun WorkoutInfoRow(icon: Int, text: String) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = icon),
            contentDescription = "",
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text,
            color = Color.Gray,
            fontSize = 13.sp
        )
    }

    Spacer(modifier = Modifier.height(4.dp))
}