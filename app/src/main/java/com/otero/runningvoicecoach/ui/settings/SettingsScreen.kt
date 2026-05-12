package com.otero.runningvoicecoach.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.data.settings.UserSettingsRepository
import com.otero.runningvoicecoach.ui.components.AppScaffold
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserSettingsRepository(context.applicationContext) }
    val settings by repository.settings.collectAsState(
        initial = com.otero.runningvoicecoach.data.settings.UserSettings()
    )
    val scope = rememberCoroutineScope()

    AppScaffold(
        title = "Configuracion",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Configuracion",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            SettingsSwitchCard(
                title = "Voz",
                subtitle = "Reproducir alertas por TextToSpeech",
                checked = settings.voiceEnabled,
                onCheckedChange = { enabled ->
                    scope.launch { repository.setVoiceEnabled(enabled) }
                }
            )
            SettingsSwitchCard(
                title = "OpenAI",
                subtitle = "Opcional. Todavia no integrado en carrera",
                checked = settings.openAiEnabled,
                onCheckedChange = { enabled ->
                    scope.launch { repository.setOpenAiEnabled(enabled) }
                }
            )
            NumericSettingCard(
                title = "Frecuencia minima de avisos",
                value = "${settings.minAlertIntervalSeconds} s",
                onDecrease = {
                    scope.launch { repository.setMinAlertIntervalSeconds(settings.minAlertIntervalSeconds - 5) }
                },
                onIncrease = {
                    scope.launch { repository.setMinAlertIntervalSeconds(settings.minAlertIntervalSeconds + 5) }
                }
            )
            NumericSettingCard(
                title = "Tolerancia general de ritmo",
                value = "${settings.generalPaceToleranceSeconds} s/km",
                onDecrease = {
                    scope.launch { repository.setGeneralPaceToleranceSeconds(settings.generalPaceToleranceSeconds - 5) }
                },
                onIncrease = {
                    scope.launch { repository.setGeneralPaceToleranceSeconds(settings.generalPaceToleranceSeconds + 5) }
                }
            )
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
