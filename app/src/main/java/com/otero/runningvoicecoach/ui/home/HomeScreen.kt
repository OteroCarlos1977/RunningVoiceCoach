package com.otero.runningvoicecoach.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.ui.components.AppScaffold

@Composable
fun HomeScreen(
    onNewRun: () -> Unit,
    onWorkouts: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    AppScaffold(title = "Runners") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.runners_logo),
                contentDescription = "Runners",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
            )

            Text(
                text = "Corre hoy,\nmejora siempre.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tu asistente para registrar, escuchar y ajustar cada entrenamiento.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                onClick = onNewRun
            ) {
                Text("Comenzar carrera")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = "🏃",
                    title = "Rutinas",
                    onClick = onWorkouts
                )
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = "📈",
                    title = "Progreso",
                    onClick = onHistory
                )
            }
            HomeActionCard(
                modifier = Modifier.fillMaxWidth(),
                icon = "⚙",
                title = "Configuracion",
                subtitle = "Voz, GPS y preferencias",
                onClick = onSettings
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Cada paso te impulsa",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                    )
                }
            }
        }
    }
}
