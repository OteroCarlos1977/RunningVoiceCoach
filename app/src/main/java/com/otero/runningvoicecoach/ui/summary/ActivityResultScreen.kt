package com.otero.runningvoicecoach.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.session.RunActivityRepository
import com.otero.runningvoicecoach.domain.model.RunKilometerSplit
import com.otero.runningvoicecoach.domain.model.RunRoutePoint
import com.otero.runningvoicecoach.domain.model.RunSession
import com.otero.runningvoicecoach.domain.model.RunSessionStatus
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val ResultNavy = Color(0xFF06245A)
private val ResultBlue = Color(0xFF006DE5)
private val ResultOrange = Color(0xFFFF6A00)
private val ResultSoft = Color(0xFFF7FAFF)
private val ResultMuted = Color(0xFF577095)
private val ResultGreen = Color(0xFF12B76A)

@Composable
fun ActivityResultScreen(
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { RunActivityRepository(context.applicationContext) }
    val sessions by repository.sessions.collectAsState(initial = emptyList())
    val latestSession = sessions.firstOrNull() ?: remember { demoRunSession() }

    AppScaffold(title = "Actividad", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ResultSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 30.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ResultHeader()
                ActivityHeroCard(session = latestSession)
                RoutePreviewCard(points = latestSession.routePoints)
                ActivityMetricGrid(session = latestSession)
                PaceSplitsCard(splits = latestSession.kilometerSplits)
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
private fun ResultHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_home),
            contentDescription = "Runners",
            modifier = Modifier
                .width(190.dp)
                .height(66.dp),
            contentScale = ContentScale.Fit
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 5.dp
        ) {
            Text(
                text = "Guardada",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = ResultGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActivityHeroCard(session: RunSession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ResultNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fondo6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.42f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(ResultNavy.copy(alpha = 0.35f), ResultNavy.copy(alpha = 0.94f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Ultima actividad", color = Color.White.copy(alpha = 0.76f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(session.workoutName.ifBlank { "Running" }, color = Color.White, fontSize = 28.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
                    Text(formatDate(session.endTimeMillis ?: session.startTimeMillis), color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HeroMetric("%.2f".format(session.distanceKilometers), "km")
                    HeroMetric(formatDuration(session.totalDurationSeconds), "tiempo")
                    HeroMetric(PaceCalculator.formatPace(session.averagePaceSecondsPerKm), "ritmo")
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RoutePreviewCard(points: List<RunRoutePoint>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Recorrido GPS", color = ResultNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (points.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sin puntos GPS suficientes para dibujar el recorrido.",
                        color = ResultMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                RouteCanvas(points = points, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun RouteCanvas(points: List<RunRoutePoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLon = points.minOf { it.longitude }
        val maxLon = points.maxOf { it.longitude }
        val latRange = max(maxLat - minLat, 0.00001)
        val lonRange = max(maxLon - minLon, 0.00001)
        val padding = 16.dp.toPx()
        val drawableWidth = size.width - padding * 2
        val drawableHeight = size.height - padding * 2
        val path = Path()

        points.forEachIndexed { index, point ->
            val x = padding + ((point.longitude - minLon) / lonRange).toFloat() * drawableWidth
            val y = padding + (1f - ((point.latitude - minLat) / latRange).toFloat()) * drawableHeight
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawRoundRect(color = Color(0xFFEAF4FF), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()))
        drawPath(path = path, color = ResultBlue, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

        val start = points.first()
        val end = points.last()
        fun pointOffset(point: RunRoutePoint): Offset {
            val x = padding + ((point.longitude - minLon) / lonRange).toFloat() * drawableWidth
            val y = padding + (1f - ((point.latitude - minLat) / latRange).toFloat()) * drawableHeight
            return Offset(x, y)
        }
        drawCircle(color = ResultGreen, radius = 6.dp.toPx(), center = pointOffset(start))
        drawCircle(color = ResultOrange, radius = 6.dp.toPx(), center = pointOffset(end))
    }
}

@Composable
private fun ActivityMetricGrid(session: RunSession) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Vel. media", value = formatSpeed(session.averageSpeedKmh), accent = ResultBlue)
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Vel. max.", value = formatSpeed(session.maxSpeedKmh), accent = ResultOrange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Calorias", value = "${session.estimatedCalories ?: 0} kcal", accent = ResultOrange)
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Pausa", value = formatDuration(session.pausedDurationSeconds), accent = ResultMuted)
        }
    }
}

@Composable
private fun ResultMetricCard(modifier: Modifier, label: String, value: String, accent: Color) {
    Card(
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = ResultMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = ResultNavy, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun PaceSplitsCard(splits: List<RunKilometerSplit>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parciales por kilometro", color = ResultNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            if (splits.isEmpty()) {
                Text("No hay parciales suficientes.", color = ResultMuted, fontSize = 13.sp)
            } else {
                splits.take(12).forEach { split ->
                    SplitRow(split = split, maxDurationSeconds = splits.maxOf { it.durationSeconds }.coerceAtLeast(1L))
                }
            }
        }
    }
}

@Composable
private fun SplitRow(split: RunKilometerSplit, maxDurationSeconds: Long) {
    val barFraction = (split.durationSeconds.toFloat() / maxDurationSeconds.toFloat()).coerceIn(0.18f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("${split.kilometer}", color = ResultNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(30.dp)
                    .background(Color(0xFFE1EAF5), RoundedCornerShape(7.dp))
            )
            Text(
                text = PaceCalculator.formatPace(split.averagePaceSecondsPerKm),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp),
                color = ResultNavy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(formatSplitDistance(split.distanceMeters), color = ResultMuted, fontSize = 12.sp, modifier = Modifier.width(48.dp))
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
    return if (speedKmh == null) "0.0 km/h" else "%.1f km/h".format(speedKmh)
}

private fun formatSplitDistance(distanceMeters: Double): String {
    return if (distanceMeters >= 999.0) "1 km" else "${distanceMeters.toInt()} m"
}

private fun demoRunSession(): RunSession {
    val now = System.currentTimeMillis()
    return RunSession(
        id = "demo-actividad",
        workoutPlanId = "demo",
        workoutName = "10K prueba exterior",
        startTimeMillis = now - 4_145_000L,
        endTimeMillis = now,
        totalDistanceMeters = 10_050.0,
        totalDurationSeconds = 4_084L,
        averagePaceSecondsPerKm = 406,
        stepResults = emptyList(),
        status = RunSessionStatus.FINISHED,
        elapsedDurationSeconds = 4_145L,
        pausedDurationSeconds = 61L,
        averageSpeedKmh = 8.86,
        maxSpeedKmh = 11.1,
        estimatedCalories = 704,
        kilometerSplits = listOf(
            RunKilometerSplit(1, 1000.0, 386L, 386, 9.3),
            RunKilometerSplit(2, 1000.0, 397L, 397, 9.1),
            RunKilometerSplit(3, 1000.0, 402L, 402, 9.0),
            RunKilometerSplit(4, 1000.0, 407L, 407, 8.8),
            RunKilometerSplit(5, 1000.0, 405L, 405, 8.9),
            RunKilometerSplit(6, 1000.0, 411L, 411, 8.8),
            RunKilometerSplit(7, 1000.0, 408L, 408, 8.8),
            RunKilometerSplit(8, 1000.0, 399L, 399, 9.0),
            RunKilometerSplit(9, 1000.0, 429L, 429, 8.4),
            RunKilometerSplit(10, 1000.0, 438L, 438, 8.2),
            RunKilometerSplit(11, 50.0, 22L, 440, 8.1)
        ),
        routePoints = listOf(
            RunRoutePoint(-36.0900, -57.8050, now - 4_084_000L, 0L, 0.0),
            RunRoutePoint(-36.0888, -57.8040, now - 3_700_000L, 384L, 900.0),
            RunRoutePoint(-36.0876, -57.8028, now - 3_300_000L, 784L, 1_900.0),
            RunRoutePoint(-36.0868, -57.8010, now - 2_900_000L, 1_184L, 2_900.0),
            RunRoutePoint(-36.0878, -57.7994, now - 2_500_000L, 1_584L, 3_900.0),
            RunRoutePoint(-36.0895, -57.7988, now - 2_100_000L, 1_984L, 4_900.0),
            RunRoutePoint(-36.0910, -57.7996, now - 1_700_000L, 2_384L, 5_900.0),
            RunRoutePoint(-36.0918, -57.8016, now - 1_300_000L, 2_784L, 6_900.0),
            RunRoutePoint(-36.0907, -57.8034, now - 900_000L, 3_184L, 7_900.0),
            RunRoutePoint(-36.0892, -57.8048, now - 500_000L, 3_584L, 8_900.0),
            RunRoutePoint(-36.0900, -57.8050, now, 4_084L, 10_050.0)
        )
    )
}
