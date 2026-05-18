package com.otero.runningvoicecoach.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar

private val HomeNavy = Color(0xFF06245A)
private val HomeBlue = Color(0xFF006DE5)
private val HomeCyan = Color(0xFF16C7E8)
private val HomeOrange = Color(0xFFFF6A00)
private val HomeSoft = Color(0xFFF7FAFF)
private val HomeTextMuted = Color(0xFF577095)

@Composable
fun HomeScreen(
    onNewRun: () -> Unit,
    onWorkouts: () -> Unit,
    onHistory: () -> Unit,
    onRecentActivity: () -> Unit,
    onHealth: () -> Unit,
    onSettings: () -> Unit
) {
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeaderRow()
                Greeting()
                GoalHero()

                SectionHeader(title = "Resumen de hoy", action = "Ver actividad", onAction = onRecentActivity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "⌖",
                        label = "DISTANCIA",
                        value = "0.0",
                        unit = "km",
                        footer = "Meta: 10.00 km",
                        accent = HomeBlue
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "◴",
                        label = "RITMO PROM.",
                        value = "--:--",
                        unit = "min/km",
                        footer = "Sin actividad",
                        accent = HomeBlue
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
                        value = "0",
                        unit = "kcal",
                        footer = "Hoy",
                        accent = HomeOrange
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = "♡",
                        label = "TIEMPO ACTIVO",
                        value = "00:00",
                        unit = "min",
                        footer = "Meta: 60 min",
                        accent = Color(0xFF00A8A8)
                    )
                }

                BestPerformanceCard()

                SectionHeader(title = "Proxima rutina", action = "Ver plan")
                NextRoutineCard(onStart = onNewRun)
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
            text = "Listo para superar tus limites hoy.",
            style = MaterialTheme.typography.titleMedium,
            color = HomeTextMuted
        )
    }
}

@Composable
private fun GoalHero() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fondo6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.72f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.86f),
                                Color.White.copy(alpha = 0.18f)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HOY",
                        style = MaterialTheme.typography.labelLarge,
                        color = HomeNavy.copy(alpha = 0.74f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tu meta diaria",
                        style = MaterialTheme.typography.titleMedium,
                        color = HomeTextMuted
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "7.36",
                            style = MaterialTheme.typography.displayLarge,
                            color = HomeNavy,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp, bottom = 10.dp),
                            text = "km",
                            style = MaterialTheme.typography.headlineMedium,
                            color = HomeNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { 0.73f },
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = HomeOrange,
                        trackColor = Color(0xFFE1EAF5)
                    )
                    Text(
                        text = "73% de 10 km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HomeTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = HomeNavy,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$action  >",
            modifier = if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier,
            style = MaterialTheme.typography.titleSmall,
            color = HomeBlue,
            fontWeight = FontWeight.Bold
        )
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(158.dp),
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
                Text(text = value, style = MaterialTheme.typography.headlineLarge, color = HomeNavy, fontWeight = FontWeight.Bold)
                Text(text = unit, style = MaterialTheme.typography.bodyMedium, color = HomeTextMuted)
            }
            Text(text = footer, style = MaterialTheme.typography.bodySmall, color = accent)
        }
    }
}

@Composable
private fun BestPerformanceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Resumen Mejor Tiempo", style = MaterialTheme.typography.titleMedium, color = HomeNavy, fontWeight = FontWeight.Bold)
                Text("Tus mejores datos de todas las practicas.", style = MaterialTheme.typography.bodySmall, color = HomeTextMuted)
                Text("6:10 min/km", style = MaterialTheme.typography.headlineMedium, color = HomeNavy, fontWeight = FontWeight.Bold)
                Text("Mejor ritmo promedio", style = MaterialTheme.typography.bodySmall, color = HomeTextMuted)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                BestMetric(label = "5K", value = "31:20")
                BestMetric(label = "Dist.", value = "7.36 km")
                BestMetric(label = "Activo", value = "45:28")
            }
        }
    }
}

@Composable
private fun BestMetric(
    label: String,
    value: String
) {
    Surface(
        color = Color(0xFFEAF4FF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = HomeBlue, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.labelLarge, color = HomeNavy, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NextRoutineCard(onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HomeNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = HomeBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⌁", style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "5K Intermedio",
                    color = Color.White,
                    fontSize = 19.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "35 min  |  Intermedio",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = HomeBlue),
                contentPadding = PaddingValues(horizontal = 14.dp),
                onClick = onStart
            ) {
                Text("Iniciar", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
