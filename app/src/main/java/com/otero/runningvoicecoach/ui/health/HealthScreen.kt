package com.otero.runningvoicecoach.ui.health

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.ui.components.AppScaffold

private val HealthNavy = Color(0xFF06245A)
private val HealthBlue = Color(0xFF006DE5)
private val HealthOrange = Color(0xFFFF6A00)
private val HealthTeal = Color(0xFF00A8A8)
private val HealthSoft = Color(0xFFF7FAFF)
private val HealthMuted = Color(0xFF577095)

@Composable
fun HealthScreen(
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    AppScaffold(title = "Salud", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HealthSoft)
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
                HealthHeader()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HealthDataCard(
                        modifier = Modifier.weight(1f),
                        icon = "⌖",
                        title = "Actividad GPS",
                        value = "Disponible",
                        subtitle = "Distancia, tiempo y ritmo durante carrera",
                        status = "Medible desde la app",
                        accent = HealthBlue
                    )
                    HealthDataCard(
                        modifier = Modifier.weight(1f),
                        icon = "🔥",
                        title = "Calorias",
                        value = "Estimadas",
                        subtitle = "Calculo aproximado por distancia",
                        status = "Sin sensor externo",
                        accent = HealthOrange
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HealthDataCard(
                        modifier = Modifier.weight(1f),
                        icon = "💧",
                        title = "Hidratacion",
                        value = "Manual",
                        subtitle = "Pendiente de carga del usuario",
                        status = "Proxima mejora",
                        accent = HealthBlue
                    )
                    HealthDataCard(
                        modifier = Modifier.weight(1f),
                        icon = "👟",
                        title = "Pasos",
                        value = "Sin datos",
                        subtitle = "Requiere sensor, Health Connect o permiso",
                        status = "No conectado",
                        accent = HealthTeal
                    )
                }
                DeviceNoticeCard()
                HealthAdviceCard()
            }

            HealthBottomBar(
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
private fun HealthHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(315.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo6),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.82f
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(HealthSoft, HealthSoft.copy(alpha = 0.82f), HealthSoft.copy(alpha = 0.08f))
                    )
                )
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(id = R.drawable.logo_home),
                contentDescription = "Runners",
                modifier = Modifier
                    .width(235.dp)
                    .height(86.dp),
                contentScale = ContentScale.Fit
            )
            Text("Salud", color = HealthNavy, fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
            Text("Solo datos medibles o registrables.", color = HealthMuted, fontSize = 18.sp)
        }
    }
}

@Composable
private fun HealthDataCard(
    modifier: Modifier,
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    status: String,
    accent: Color
) {
    Card(
        modifier = modifier.height(190.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) { Text(icon, color = accent, fontSize = 23.sp) }
            }
            Text(title, color = HealthNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(value, color = HealthNavy, fontSize = 26.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = HealthMuted, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2)
            Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.12f)) {
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    text = "●  $status",
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DeviceNoticeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Dispositivo de salud", color = HealthNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "No hay pulsera, reloj o fuente biometrica conectada. Por eso no se muestran pulsaciones, estres, oxigeno ni recuperacion.",
                color = HealthMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun HealthAdviceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFFFFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("♡", color = HealthTeal, fontSize = 34.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Consejo del dia", color = HealthNavy, fontWeight = FontWeight.Bold)
                Text("Usa esta pantalla solo para datos reales o cargados por el usuario.", color = HealthMuted, fontSize = 13.sp)
            }
            Text("›", color = HealthTeal, fontSize = 32.sp)
        }
    }
}

@Composable
private fun HealthBottomBar(
    modifier: Modifier,
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem("⌂", "Inicio", false, onHome)
            BottomItem("🏃", "Rutinas", false, onRoutines)
            BottomItem("▁▃▆", "Progreso", false, onProgress)
            BottomItem("❤", "Salud", true, onHealth)
            BottomItem("👤", "Perfil", false, onProfile)
        }
    }
}

@Composable
private fun BottomItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(icon, style = MaterialTheme.typography.titleLarge, color = if (selected) HealthBlue else HealthMuted)
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) HealthBlue else HealthMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
