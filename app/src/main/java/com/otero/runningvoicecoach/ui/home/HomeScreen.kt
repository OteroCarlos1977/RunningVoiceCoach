package com.otero.runningvoicecoach.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar
import com.otero.runningvoicecoach.ui.summary.demoRunSession

private val HomeNavy = Color(0xFF06245A)
private val HomeBlue = Color(0xFF006DE5)
private val HomeOrange = Color(0xFFFF6A00)
private val HomeSoft = Color(0xFFF7FAFF)
private val HomeTextMuted = Color(0xFF577095)
private val HomeTeal = Color(0xFF00A8A8)

@Composable
fun HomeScreen(
    onWorkouts: () -> Unit,
    onHistory: () -> Unit,
    onRecentActivity: () -> Unit,
    onDistanceSummary: () -> Unit,
    onPaceSummary: () -> Unit,
    onHealth: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { RunActivityRepository(context.applicationContext) }
    val storedActivities by repository.sessions.collectAsState(initial = emptyList())
    val activities = if (storedActivities.isEmpty()) listOf(remember { demoRunSession() }) else storedActivities
    val latestActivity = activities.first()
    val stats = remember(activities) { HomeStats.from(activities) }

    AppScaffold(title = "", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                HeaderRow()
                Greeting()
                LastActivityCard(activity = latestActivity, onClick = onRecentActivity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "⌖",
                        label = "DISTANCIA",
                        value = stats.totalDistance,
                        unit = "km",
                        footer = "${activities.size} actividades",
                        accent = HomeBlue,
                        onClick = onDistanceSummary
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "◴",
                        label = "RITMO PROM.",
                        value = stats.averagePace,
                        unit = "min/km",
                        footer = "Promedio general",
                        accent = HomeBlue,
                        onClick = onPaceSummary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "🔥",
                        label = "CALORIAS",
                        value = stats.totalCalories,
                        unit = "kcal",
                        footer = "Total estimado",
                        accent = HomeOrange
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "♡",
                        label = "TIEMPO ACTIVO",
                        value = stats.totalTime,
                        unit = "hh:mm",
                        footer = "Entrenando",
                        accent = HomeTeal
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            RunnersBottomBar(
                selected = BottomTab.HOME,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                onHome = {},
                onRoutines = onWorkouts,
                onProgress = onHistory,
                onHealth = onHealth,
                onProfile = onSettings
            )
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_home),
            contentDescription = "Runners",
            modifier = Modifier
                .width(252.dp)
                .height(82.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(28.dp))
    }
}

@Composable
private fun Greeting() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "¡Hola, Runner!",
            style = MaterialTheme.typography.displaySmall,
            color = HomeNavy,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tu actividad, tus datos, tu progreso.",
            style = MaterialTheme.typography.titleMedium,
            color = HomeTextMuted
        )
    }
}

@Composable
private fun LastActivityCard(activity: RunSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = HomeNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fondo6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.64f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                HomeNavy.copy(alpha = 0.96f),
                                HomeNavy.copy(alpha = 0.72f),
                                HomeNavy.copy(alpha = 0.22f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ULTIMA ACTIVIDAD", color = Color.White.copy(alpha = 0.74f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = activityTitle(activity),
                        color = Color.White,
                        fontSize = 32.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activity.workoutName.ifBlank { "Running" },
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    HeroMetric("%.2f".format(activity.distanceKilometers), "km")
                    HeroMetric(formatDuration(activity.totalDurationSeconds), "tiempo")
                    Text("Ver detalle  >", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(value: String, label: String) {
    Column {
        Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), color = Color.White.copy(alpha = 0.66f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SummaryCard(
    icon: String,
    label: String,
    value: String,
    unit: String,
    footer: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier
            .height(158.dp)
            .clickable(onClick = onClick)
    } else {
        modifier.height(158.dp)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineMedium, color = accent)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = HomeTextMuted, fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value, style = MaterialTheme.typography.headlineLarge, color = HomeNavy, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = unit, style = MaterialTheme.typography.bodyMedium, color = HomeTextMuted)
            }
            Text(text = footer, style = MaterialTheme.typography.bodySmall, color = accent, maxLines = 1)
        }
    }
}

private data class HomeStats(
    val totalDistance: String,
    val averagePace: String,
    val totalCalories: String,
    val totalTime: String
) {
    companion object {
        fun from(activities: List<RunSession>): HomeStats {
            val totalDistanceMeters = activities.sumOf { it.totalDistanceMeters }
            val totalDurationSeconds = activities.sumOf { it.totalDurationSeconds }
            val totalCalories = activities.sumOf { it.estimatedCalories ?: estimatedCalories(it.totalDistanceMeters) }

            return HomeStats(
                totalDistance = "%.1f".format(totalDistanceMeters / 1000.0),
                averagePace = PaceCalculator.formatPace(
                    PaceCalculator.calculateAveragePaceSecondsPerKm(
                        distanceMeters = totalDistanceMeters,
                        durationSeconds = totalDurationSeconds
                    )
                ),
                totalCalories = "%,d".format(totalCalories),
                totalTime = formatHoursMinutes(totalDurationSeconds)
            )
        }
    }
}

private fun activityTitle(activity: RunSession): String {
    val roundedKm = activity.distanceKilometers.toInt().coerceAtLeast(1)
    return "${roundedKm}K"
}

private fun estimatedCalories(distanceMeters: Double): Int {
    return (distanceMeters / 1000.0 * 70.0).toInt().coerceAtLeast(0)
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

private fun formatHoursMinutes(totalSeconds: Long): String {
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    return "%d:%02d".format(hours, minutes)
}
