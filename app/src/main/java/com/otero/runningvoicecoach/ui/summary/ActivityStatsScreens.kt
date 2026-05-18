package com.otero.runningvoicecoach.ui.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.session.RunActivityRepository
import com.otero.runningvoicecoach.domain.model.RunSession
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val StatsNavy = Color(0xFF06245A)
private val StatsBlue = Color(0xFF006DE5)
private val StatsOrange = Color(0xFFFF6A00)
private val StatsSoft = Color(0xFFF7FAFF)
private val StatsMuted = Color(0xFF577095)

@Composable
fun ActivityDistanceSummaryScreen(
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    val activities = rememberActivities()
    val totalDistance = activities.sumOf { it.totalDistanceMeters } / 1000.0

    ActivityStatsScaffold(
        title = "Distancias",
        subtitle = "%.1f km acumulados".format(totalDistance),
        onHome = onHome,
        onRoutines = onRoutines,
        onProgress = onProgress,
        onHealth = onHealth,
        onProfile = onProfile
    ) {
        activities.forEach { activity ->
            ActivityDistanceRow(activity = activity)
        }
    }
}

@Composable
fun ActivityPaceSummaryScreen(
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    val activities = rememberActivities()

    ActivityStatsScaffold(
        title = "Ritmos y velocidades",
        subtitle = "Maxima, minima y promedio por actividad",
        onHome = onHome,
        onRoutines = onRoutines,
        onProgress = onProgress,
        onHealth = onHealth,
        onProfile = onProfile
    ) {
        activities.forEach { activity ->
            ActivityPaceRow(activity = activity)
        }
    }
}

@Composable
private fun rememberActivities(): List<RunSession> {
    val context = LocalContext.current
    val repository = remember { RunActivityRepository(context.applicationContext) }
    val storedActivities by repository.sessions.collectAsState(initial = emptyList())
    return if (storedActivities.isEmpty()) listOf(remember { demoRunSession() }) else storedActivities
}

@Composable
private fun ActivityStatsScaffold(
    title: String,
    subtitle: String,
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AppScaffold(title = title, showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StatsSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Header(title = title, subtitle = subtitle)
                content()
            }

            RunnersBottomBar(
                selected = BottomTab.HOME,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                onHome = onHome,
                onRoutines = onRoutines,
                onProgress = onProgress,
                onHealth = onHealth,
                onProfile = onProfile
            )
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(id = R.drawable.logo_home),
            contentDescription = "Runners",
            modifier = Modifier
                .width(210.dp)
                .height(72.dp),
            contentScale = ContentScale.Fit
        )
        Text(title, color = StatsNavy, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = StatsMuted, fontSize = 16.sp)
    }
}

@Composable
private fun ActivityDistanceRow(activity: RunSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(activity.workoutName.ifBlank { "Actividad" }, color = StatsNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatDate(activity.endTimeMillis ?: activity.startTimeMillis), color = StatsMuted, fontSize = 12.sp)
                Text(formatDuration(activity.totalDurationSeconds), color = StatsBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text("%.2f km".format(activity.distanceKilometers), color = StatsNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActivityPaceRow(activity: RunSession) {
    val minSpeed = activity.kilometerSplits.mapNotNull { it.averageSpeedKmh }.minOrNull()
    val maxSpeed = activity.maxSpeedKmh ?: activity.kilometerSplits.mapNotNull { it.averageSpeedKmh }.maxOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(activity.workoutName.ifBlank { "Actividad" }, color = StatsNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatDate(activity.endTimeMillis ?: activity.startTimeMillis), color = StatsMuted, fontSize = 12.sp)
                }
                Text("%.2f km".format(activity.distanceKilometers), color = StatsBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpeedStat(label = "Prom.", value = formatSpeed(activity.averageSpeedKmh))
                SpeedStat(label = "Max.", value = formatSpeed(maxSpeed), color = StatsOrange)
                SpeedStat(label = "Min.", value = formatSpeed(minSpeed), color = StatsMuted)
            }
        }
    }
}

@Composable
private fun SpeedStat(label: String, value: String, color: Color = StatsBlue) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = StatsMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatDate(timeMillis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timeMillis))
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun formatSpeed(speedKmh: Double?): String {
    return if (speedKmh == null) "0.0" else "%.1f".format(speedKmh)
}
