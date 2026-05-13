package com.otero.runningvoicecoach.ui.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.session.RunHistoryRepository
import com.otero.runningvoicecoach.data.session.RunSessionSummary
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar

private val DashNavy = Color(0xFF06245A)
private val DashBlue = Color(0xFF006DE5)
private val DashOrange = Color(0xFFFF6A00)
private val DashTeal = Color(0xFF00A8A8)
private val DashSoft = Color(0xFFF7FAFF)
private val DashMuted = Color(0xFF577095)

@Composable
fun RunSummaryScreen(
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { RunHistoryRepository(context.applicationContext) }
    val sessions by repository.sessions.collectAsState(initial = emptyList())
    val stats = remember(sessions) { DashboardStats.from(sessions) }

    AppScaffold(title = "Progreso", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DashSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardHeader()
                SegmentTabs()
                WeeklyDistanceCard(stats = stats)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    DashboardSmallCard(modifier = Modifier.weight(1f), icon = "⌖", title = "Distancia mensual", value = stats.monthDistance, unit = "km", trend = "▲ total")
                    DashboardSmallCard(modifier = Modifier.weight(1f), icon = "🔥", title = "Calorias", value = stats.calories, unit = "kcal", trend = "▲ estimado", accent = DashOrange)
                    DashboardSmallCard(modifier = Modifier.weight(1f), icon = "△", title = "Elevacion", value = "320", unit = "m", trend = "referencial", accent = DashTeal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    PaceEvolutionCard(modifier = Modifier.weight(1f), stats = stats)
                    GoalProgressCard(modifier = Modifier.weight(1f), stats = stats)
                }
                BottomStatsCard(stats = stats)
            }

            RunnersBottomBar(
                selected = BottomTab.PROGRESS,
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
private fun DashboardHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo6),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.86f
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(DashSoft, DashSoft.copy(alpha = 0.86f), DashSoft.copy(alpha = 0.04f))
                    )
                )
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_home),
                    contentDescription = "Runners",
                    modifier = Modifier
                        .width(210.dp)
                        .height(72.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text("¡Hola, Runner!", color = DashNavy, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text("Dashboard", color = DashNavy, fontSize = 50.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold)
            Text("Tu progreso, tu motivacion.", color = DashMuted, fontSize = 19.sp)
        }
    }
}

@Composable
private fun SegmentTabs() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Resumen", "Semanal", "Mensual", "Anual").forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    color = if (index == 0) DashBlue else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = if (index == 0) Color.White else DashNavy,
                            fontSize = 12.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyDistanceCard(stats: DashboardStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("👟  Distancia semanal", color = DashNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(stats.weekDistance, color = DashNavy, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text(" km", color = DashNavy, fontSize = 18.sp, modifier = Modifier.padding(bottom = 5.dp))
                }
                Text("▲ ${stats.sessionCount} actividades registradas", color = Color(0xFF12B76A), fontSize = 13.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                val labels = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM")
                stats.weekBars.forEachIndexed { index, value ->
                    BarColumn(label = labels[index], value = value)
                }
            }
        }
    }
}

@Composable
private fun BarColumn(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("%.1f".format(value * 8f), color = DashMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(90.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height((28 + value * 62).dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Brush.verticalGradient(listOf(DashBlue, Color(0xFF4DBBFF))))
            )
        }
        Text(label, color = DashMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DashboardSmallCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String,
    unit: String,
    trend: String,
    accent: Color = DashBlue
) {
    Card(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("$icon  $title", color = DashNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = DashNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(" $unit", color = DashNavy, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
            Text(trend, color = accent, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PaceEvolutionCard(modifier: Modifier, stats: DashboardStats) {
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("◴  Ritmo promedio (min/km)", color = DashNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(stats.bestPace, color = DashNavy, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                listOf(0.25f, 0.42f, 0.34f, 0.52f, 0.66f, 0.48f, 0.74f).forEachIndexed { index, value ->
                    Surface(
                        modifier = Modifier
                            .width(10.dp)
                            .height((34 + value * 70).dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (index > 4) DashOrange else DashBlue
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun GoalProgressCard(modifier: Modifier, stats: DashboardStats) {
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("◎  Progreso hacia tu meta", color = DashNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Box(contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(118.dp), shape = CircleShape, color = Color(0xFFE1EAF5)) {}
                Surface(modifier = Modifier.size(92.dp), shape = CircleShape, color = Color.White) {}
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.goalPercent}%", color = DashNavy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Completado", color = DashMuted, fontSize = 12.sp)
                }
            }
            Text("${stats.sessionCount} entrenamientos", color = DashBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomStatsCard(stats: DashboardStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            BottomStat("Actividades", stats.sessionCount.toString(), "▲ total")
            BottomStat("Tiempo total", stats.totalTime, "▲ acumulado")
            BottomStat("FC promedio", "128 lpm", "referencial")
        }
    }
}

@Composable
private fun BottomStat(label: String, value: String, trend: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = DashNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = DashNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(trend, color = Color(0xFF12B76A), fontSize = 10.sp)
    }
}

private data class DashboardStats(
    val sessionCount: Int,
    val weekDistance: String,
    val monthDistance: String,
    val calories: String,
    val bestPace: String,
    val totalTime: String,
    val goalPercent: Int,
    val weekBars: List<Float>
) {
    companion object {
        fun from(sessions: List<RunSessionSummary>): DashboardStats {
            val totalMeters = sessions.sumOf { it.totalDistanceMeters }
            val totalSeconds = sessions.sumOf { it.totalDurationSeconds }
            val bestPace = sessions.mapNotNull { it.averagePaceSecondsPerKm }.minOrNull()
            val fallbackBars = listOf(0.52f, 0.61f, 0.78f, 0.43f, 0.65f, 0.48f, 0.40f)
            val distanceKm = totalMeters / 1000.0
            val percent = ((distanceKm / 30.0) * 100).toInt().coerceIn(0, 100)

            return DashboardStats(
                sessionCount = sessions.size,
                weekDistance = "%.2f".format(if (sessions.isEmpty()) 38.75 else distanceKm),
                monthDistance = "%.1f".format(if (sessions.isEmpty()) 156.3 else distanceKm),
                calories = "%,d".format(((if (sessions.isEmpty()) 140600.0 else totalMeters) / 1000.0 * 70).toInt()),
                bestPace = PaceCalculator.formatPace(bestPace ?: 370),
                totalTime = formatDuration(if (sessions.isEmpty()) 46112L else totalSeconds),
                goalPercent = if (sessions.isEmpty()) 72 else percent,
                weekBars = fallbackBars
            )
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d:%02d".format(hours, minutes, seconds)
}
