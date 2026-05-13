package com.otero.runningvoicecoach.ui.settings

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.settings.UserSettingsRepository
import com.otero.runningvoicecoach.ui.components.AppScaffold
import kotlinx.coroutines.launch

private val HealthNavy = Color(0xFF06245A)
private val HealthBlue = Color(0xFF006DE5)
private val HealthOrange = Color(0xFFFF6A00)
private val HealthTeal = Color(0xFF00A8A8)
private val HealthSoft = Color(0xFFF7FAFF)
private val HealthMuted = Color(0xFF577095)

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserSettingsRepository(context.applicationContext) }
    val settings by repository.settings.collectAsState(
        initial = com.otero.runningvoicecoach.data.settings.UserSettings()
    )
    val scope = rememberCoroutineScope()
    var apiKeyDraft by remember(settings.developmentOpenAiApiKey) {
        mutableStateOf(settings.developmentOpenAiApiKey)
    }

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
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "❤", title = "Ritmo cardiaco", value = "54", unit = "lpm", subtitle = "En reposo", status = "En rango saludable", accent = HealthOrange)
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "↻", title = "Recuperacion", value = "82", unit = "%", subtitle = "Listo para entrenar", status = "Optima", accent = HealthTeal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "☾", title = "Sueño", value = "7 h 36", unit = "min", subtitle = "Calidad del sueño", status = "Buena", accent = HealthBlue)
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "💧", title = "Hidratacion", value = "1.8", unit = "L", subtitle = "Objetivo: 2.5 L", status = "72% del objetivo", accent = HealthBlue)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "🔥", title = "Calorias activas", value = "512", unit = "kcal", subtitle = "Hoy", status = "En progreso", accent = HealthOrange)
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "👟", title = "Pasos", value = "8,432", unit = "", subtitle = "Objetivo: 10,000", status = "84% del objetivo", accent = HealthTeal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "≋", title = "Oxigeno en sangre", value = "98", unit = "%", subtitle = "SpO2 promedio", status = "Excelente", accent = HealthTeal)
                    HealthMetricCard(modifier = Modifier.weight(1f), icon = "☯", title = "Estres", value = "32", unit = "Bajo", subtitle = "Nivel de estres", status = "Bajo control", accent = Color(0xFF8A4BE8))
                }
                DailyAdviceCard()
                Text("Configuracion de entrenamiento", color = HealthNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                SettingsSwitchCard(
                    title = "Voz",
                    subtitle = "Reproducir alertas por TextToSpeech",
                    checked = settings.voiceEnabled,
                    onCheckedChange = { enabled -> scope.launch { repository.setVoiceEnabled(enabled) } }
                )
                SettingsSwitchCard(
                    title = "OpenAI",
                    subtitle = "Opcional. Si falla, la app usa mensajes locales",
                    checked = settings.openAiEnabled,
                    onCheckedChange = { enabled -> scope.launch { repository.setOpenAiEnabled(enabled) } }
                )
                DevelopmentApiKeyCard(
                    apiKeyDraft = apiKeyDraft,
                    hasSavedApiKey = settings.developmentOpenAiApiKey.isNotBlank(),
                    onApiKeyChange = { apiKeyDraft = it },
                    onSave = { scope.launch { repository.setDevelopmentOpenAiApiKey(apiKeyDraft) } },
                    onClear = {
                        apiKeyDraft = ""
                        scope.launch { repository.setDevelopmentOpenAiApiKey("") }
                    }
                )
                NumericSettingCard(
                    title = "Frecuencia minima de avisos",
                    value = "${settings.minAlertIntervalSeconds} s",
                    onDecrease = { scope.launch { repository.setMinAlertIntervalSeconds(settings.minAlertIntervalSeconds - 5) } },
                    onIncrease = { scope.launch { repository.setMinAlertIntervalSeconds(settings.minAlertIntervalSeconds + 5) } }
                )
                NumericSettingCard(
                    title = "Tolerancia general de ritmo",
                    value = "${settings.generalPaceToleranceSeconds} s/km",
                    onDecrease = { scope.launch { repository.setGeneralPaceToleranceSeconds(settings.generalPaceToleranceSeconds - 5) } },
                    onIncrease = { scope.launch { repository.setGeneralPaceToleranceSeconds(settings.generalPaceToleranceSeconds + 5) } }
                )
            }
            HealthBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                onHome = onBack
            )
        }
    }
}

@Composable
private fun HealthHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo6),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .height(235.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.82f
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(235.dp)
                .background(Brush.horizontalGradient(listOf(HealthSoft, HealthSoft.copy(alpha = 0.82f), HealthSoft.copy(alpha = 0.08f))))
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_home),
                    contentDescription = "Runners",
                    modifier = Modifier
                        .width(235.dp)
                        .height(86.dp),
                    contentScale = ContentScale.Fit
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderCircle("♢")
                    HeaderCircle("⋮")
                }
            }
            Text("Salud y biometria", color = HealthNavy, fontSize = 38.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold)
            Text("Conoce tu cuerpo. Mejora tu rendimiento.", color = HealthMuted, fontSize = 18.sp)
        }
    }
}

@Composable
private fun HeaderCircle(text: String) {
    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.78f)) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = HealthNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HealthMetricCard(
    modifier: Modifier,
    icon: String,
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    status: String,
    accent: Color
) {
    Card(
        modifier = modifier.height(182.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
                    Box(contentAlignment = Alignment.Center) { Text(icon, color = accent, fontSize = 23.sp) }
                }
                Text("›", color = HealthNavy, fontSize = 28.sp)
            }
            Text(title, color = HealthNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = HealthNavy, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                if (unit.isNotBlank()) {
                    Text(" $unit", color = HealthNavy, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Text(subtitle, color = HealthMuted, fontSize = 13.sp, maxLines = 1)
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
private fun DailyAdviceCard() {
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
                Text("Tu recuperacion esta optima. Es un gran dia para entrenar.", color = HealthMuted, fontSize = 13.sp)
            }
            Text("›", color = HealthTeal, fontSize = 32.sp)
        }
    }
}

@Composable
private fun DevelopmentApiKeyCard(
    apiKeyDraft: String,
    hasSavedApiKey: Boolean,
    onApiKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "API key de desarrollo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (hasSavedApiKey) "Hay una clave guardada localmente." else "Tambien puede venir desde OPENAI_API_KEY.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = apiKeyDraft,
                onValueChange = onApiKeyChange,
                singleLine = true,
                label = { Text("OPENAI_API_KEY") },
                visualTransformation = PasswordVisualTransformation()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onClear
                ) {
                    Text("Borrar")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onSave
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun NumericSettingCard(
    title: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onDecrease) { Text("-") }
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onIncrease) { Text("+") }
            }
        }
    }
}

@Composable
private fun HealthBottomBar(
    modifier: Modifier,
    onHome: () -> Unit
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
            BottomItem("🏃", "Rutinas", false, {})
            BottomItem("▁▃▆", "Progreso", false, {})
            BottomItem("❤", "Salud", true, {})
            BottomItem("👤", "Perfil", false, {})
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
