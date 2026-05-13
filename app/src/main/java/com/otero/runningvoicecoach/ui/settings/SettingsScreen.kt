package com.otero.runningvoicecoach.ui.settings

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

private val ProfileNavy = Color(0xFF06245A)
private val ProfileBlue = Color(0xFF006DE5)
private val ProfileSoft = Color(0xFFF7FAFF)
private val ProfileMuted = Color(0xFF577095)

@Composable
fun SettingsScreen(
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { UserSettingsRepository(context.applicationContext) }
    val settings by repository.settings.collectAsState(
        initial = com.otero.runningvoicecoach.data.settings.UserSettings()
    )
    val scope = rememberCoroutineScope()
    var apiKeyDraft by remember(settings.developmentOpenAiApiKey) {
        mutableStateOf(settings.developmentOpenAiApiKey)
    }

    AppScaffold(title = "Perfil", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileSoft)
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
                ProfileHeader()
                ProfileDataCard()
                Text("Configuracion del sistema", color = ProfileNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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

            ProfileBottomBar(
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
private fun ProfileHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(id = R.drawable.logo_home),
            contentDescription = "Runners",
            modifier = Modifier
                .width(230.dp)
                .height(82.dp),
            contentScale = ContentScale.Fit
        )
        Text("Perfil", color = ProfileNavy, fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
        Text("Tus datos y preferencias de entrenamiento.", color = ProfileMuted, fontSize = 18.sp)
    }
}

@Composable
private fun ProfileDataCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(72.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFEAF4FF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 36.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Runner", color = ProfileNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Datos personales pendientes de configurar.", color = ProfileMuted, fontSize = 14.sp)
                Text("Objetivo: mejorar constancia y ritmo", color = ProfileBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
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
    SectionCard {
        Text("API key de desarrollo", color = ProfileNavy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = if (hasSavedApiKey) "Hay una clave guardada localmente." else "Tambien puede venir desde OPENAI_API_KEY.",
            style = MaterialTheme.typography.bodySmall,
            color = ProfileMuted
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKeyDraft,
            onValueChange = onApiKeyChange,
            singleLine = true,
            label = { Text("OPENAI_API_KEY") },
            visualTransformation = PasswordVisualTransformation()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(modifier = Modifier.weight(1f), onClick = onClear) { Text("Borrar") }
            OutlinedButton(modifier = Modifier.weight(1f), onClick = onSave) { Text("Guardar") }
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
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = ProfileNavy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = ProfileMuted)
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
    SectionCard {
        Text(text = title, color = ProfileNavy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onDecrease) { Text("-") }
            Text(text = value, color = ProfileNavy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onIncrease) { Text("+") }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun ProfileBottomBar(
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
            BottomItem("❤", "Salud", false, onHealth)
            BottomItem("👤", "Perfil", true, onProfile)
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
            Text(icon, style = MaterialTheme.typography.titleLarge, color = if (selected) ProfileBlue else ProfileMuted)
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) ProfileBlue else ProfileMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
