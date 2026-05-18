package com.otero.runningvoicecoach.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
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
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

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
    onOpenMap: () -> Unit,
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
                RoutePreviewCard(points = latestSession.routePoints, onOpenMap = onOpenMap)
                ActivityMetricGrid(session = latestSession)
                PaceAreaChartCard(session = latestSession)
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
private fun RoutePreviewCard(points: List<RunRoutePoint>, onOpenMap: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(enabled = points.size >= 2, onClick = onOpenMap),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recorrido GPS", color = ResultNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Ampliar  >", color = ResultBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
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
                RouteMapView(points = points, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun RouteMapView(points: List<RunRoutePoint>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val geoPoints = remember(points) { points.map { GeoPoint(it.latitude, it.longitude) } }

    AndroidView(
        modifier = modifier,
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 20.0
            }
        },
        update = { map ->
            map.overlays.clear()
            if (geoPoints.isNotEmpty()) {
                val route = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.rgb(0, 91, 255)
                    outlinePaint.strokeWidth = 8f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                }
                map.overlays.add(route)

                map.overlays.add(
                    Marker(map).apply {
                        position = geoPoints.first()
                        title = "Inicio"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                )
                map.overlays.add(
                    Marker(map).apply {
                        position = geoPoints.last()
                        title = "Fin"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                )

                map.controller.setZoom(15.0)
                map.controller.setCenter(geoPoints.first())
                map.post {
                    map.zoomToBoundingBox(geoPoints.toBoundingBox(), true, 64)
                    map.invalidate()
                }
            }
        }
    )
}

@Composable
private fun ActivityMetricGrid(session: RunSession) {
    val minSpeed = session.kilometerSplits.mapNotNull { it.averageSpeedKmh }.minOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Tiempo", value = formatDuration(session.totalDurationSeconds), accent = ResultBlue)
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Distancia", value = "%.2f km".format(session.distanceKilometers), accent = ResultBlue)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Ritmo prom.", value = PaceCalculator.formatPace(session.averagePaceSecondsPerKm), accent = ResultBlue)
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Vel. prom.", value = formatSpeed(session.averageSpeedKmh), accent = ResultBlue)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Vel. max.", value = formatSpeed(session.maxSpeedKmh), accent = ResultOrange)
            ResultMetricCard(modifier = Modifier.weight(1f), label = "Vel. min.", value = formatSpeed(minSpeed), accent = ResultMuted)
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
private fun PaceAreaChartCard(session: RunSession) {
    val samples = remember(session.routePoints, session.totalDurationSeconds) {
        buildTwoMinutePaceSamples(session.routePoints, session.totalDurationSeconds)
    }
    val values = samples.map { it.paceSecondsPerKm }
    val fastest = values.minOrNull()
    val slowest = values.maxOrNull()
    val average = session.averagePaceSecondsPerKm ?: values.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(268.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Variabilidad del ritmo", color = ResultNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("lecturas cada 2 min", color = ResultMuted, fontSize = 12.sp)
                }
                if (average != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatPaceCompact(average), color = ResultNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("media", color = ResultMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (samples.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay datos suficientes para graficar.", color = ResultMuted, fontSize = 13.sp)
                }
            } else if (fastest != null && slowest != null && average != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(146.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                            .height(146.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(formatPaceCompact(fastest), color = ResultMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(formatPaceCompact(average), color = ResultMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(formatPaceCompact(slowest), color = ResultMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    PaceAreaChart(samples = samples, modifier = Modifier.weight(1f).fillMaxSize())
                }
                Row(
                    modifier = Modifier.padding(start = 56.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${samples.first().elapsedMinutes} min", color = ResultMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("cada 2 min", color = ResultMuted, fontSize = 11.sp)
                    Text("${samples.last().elapsedMinutes} min", color = ResultMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay datos suficientes para graficar.", color = ResultMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PaceAreaChart(samples: List<PaceChartSample>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val values = samples.map { it.paceSecondsPerKm.toFloat() }
        if (values.size < 2) {
            return@Canvas
        }

        val minValue = values.minOrNull() ?: return@Canvas
        val maxValue = values.maxOrNull() ?: return@Canvas
        val range = max(maxValue - minValue, 1f)
        val leftPadding = 8.dp.toPx()
        val rightPadding = 10.dp.toPx()
        val topPadding = 12.dp.toPx()
        val bottomPadding = 20.dp.toPx()
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding
        val linePath = Path()
        val areaPath = Path()
        val points = mutableListOf<Offset>()

        drawRoundRect(color = Color(0xFFF3F7FF), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
        repeat(4) { index ->
            val y = topPadding + (index / 3f) * chartHeight
            drawLine(
                color = Color(0xFFDDE8F7),
                start = Offset(leftPadding, y),
                end = Offset(size.width - rightPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        samples.forEachIndexed { index, _ ->
            val x = leftPadding + (index.toFloat() / (samples.lastIndex).coerceAtLeast(1)) * chartWidth
            drawLine(
                color = Color(0xFFE6EEF9),
                start = Offset(x, topPadding),
                end = Offset(x, size.height - bottomPadding),
                strokeWidth = 1.dp.toPx()
            )
        }

        values.forEachIndexed { index, value ->
            val x = leftPadding + (index.toFloat() / (values.lastIndex).coerceAtLeast(1)) * chartWidth
            val y = topPadding + ((value - minValue) / range) * chartHeight
            points.add(Offset(x, y))
            if (index == 0) {
                linePath.moveTo(x, y)
                areaPath.moveTo(x, size.height - bottomPadding)
                areaPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                areaPath.lineTo(x, y)
            }
            if (index == values.lastIndex) {
                areaPath.lineTo(x, size.height - bottomPadding)
                areaPath.close()
            }
        }

        drawPath(path = areaPath, brush = Brush.verticalGradient(listOf(ResultBlue.copy(alpha = 0.36f), ResultBlue.copy(alpha = 0.04f))), style = Fill)
        drawPath(path = linePath, color = ResultBlue, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        points.forEach { point ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
            drawCircle(color = ResultBlue, radius = 3.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun PaceSplitsCard(splits: List<RunKilometerSplit>) {
    val fastest = splits.mapNotNull { split -> split.averagePaceSecondsPerKm?.let { split to it } }.minByOrNull { it.second }?.first
    val slowest = splits.mapNotNull { split -> split.averagePaceSecondsPerKm?.let { split to it } }.maxByOrNull { it.second }?.first
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
                    SplitRow(
                        split = split,
                        maxDurationSeconds = splits.maxOf { it.durationSeconds }.coerceAtLeast(1L),
                        marker = when (split) {
                            fastest -> "🐇"
                            slowest -> "🐢"
                            else -> null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitRow(split: RunKilometerSplit, maxDurationSeconds: Long, marker: String?) {
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
                text = buildString {
                    append(PaceCalculator.formatPace(split.averagePaceSecondsPerKm))
                    if (marker != null) append("  $marker")
                },
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

@Composable
fun ActivityMapScreen(
    onBack: () -> Unit,
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

    AppScaffold(title = "Recorrido", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ResultSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 30.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ResultHeader()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recorrido GPS", color = ResultNavy, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        modifier = Modifier.clickable(onClick = onBack),
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        shadowElevation = 5.dp
                    ) {
                        Text(
                            text = "Volver",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            color = ResultBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        if (latestSession.routePoints.size < 2) {
                            Text("Sin puntos GPS suficientes.", color = ResultMuted, modifier = Modifier.align(Alignment.Center))
                        } else {
                            RouteMapView(points = latestSession.routePoints, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
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

private data class PaceChartSample(
    val elapsedMinutes: Long,
    val paceSecondsPerKm: Int
)

private fun buildTwoMinutePaceSamples(points: List<RunRoutePoint>, totalDurationSeconds: Long): List<PaceChartSample> {
    if (points.size < 2 || totalDurationSeconds < TWO_MINUTES_SECONDS) {
        return emptyList()
    }

    val ordered = points.sortedBy { it.elapsedSeconds }
    return generateSequence(TWO_MINUTES_SECONDS) { it + TWO_MINUTES_SECONDS }
        .takeWhile { it <= totalDurationSeconds }
        .mapNotNull { endSeconds ->
            val startSeconds = endSeconds - TWO_MINUTES_SECONDS
            val startDistance = distanceAtSecond(ordered, startSeconds)
            val endDistance = distanceAtSecond(ordered, endSeconds)
            val distanceMeters = endDistance - startDistance
            if (distanceMeters < 20.0) {
                null
            } else {
                val paceSeconds = (TWO_MINUTES_SECONDS / (distanceMeters / 1000.0)).roundToInt()
                PaceChartSample(elapsedMinutes = endSeconds / 60L, paceSecondsPerKm = paceSeconds)
            }
        }
        .toList()
}

private fun distanceAtSecond(points: List<RunRoutePoint>, targetSeconds: Long): Double {
    val before = points.lastOrNull { it.elapsedSeconds <= targetSeconds } ?: return points.first().distanceMeters
    val after = points.firstOrNull { it.elapsedSeconds >= targetSeconds } ?: return points.last().distanceMeters
    if (after.elapsedSeconds == before.elapsedSeconds) {
        return before.distanceMeters
    }

    val progress = (targetSeconds - before.elapsedSeconds).toDouble() / (after.elapsedSeconds - before.elapsedSeconds).toDouble()
    return before.distanceMeters + (after.distanceMeters - before.distanceMeters) * progress.coerceIn(0.0, 1.0)
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

private fun formatPaceCompact(secondsPerKm: Int?): String {
    if (secondsPerKm == null || secondsPerKm < 0) {
        return "--:--"
    }

    val minutes = secondsPerKm / 60
    val seconds = secondsPerKm % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun List<GeoPoint>.toBoundingBox(): BoundingBox {
    return BoundingBox(
        maxOf { it.latitude },
        maxOf { it.longitude },
        minOf { it.latitude },
        minOf { it.longitude }
    )
}

private const val TWO_MINUTES_SECONDS = 120L
