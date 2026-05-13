package com.otero.runningvoicecoach.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class BottomTab {
    HOME,
    ROUTINES,
    PROGRESS,
    HEALTH,
    PROFILE
}

@Composable
fun RunnersBottomBar(
    selected: BottomTab,
    onHome: () -> Unit,
    onRoutines: () -> Unit,
    onProgress: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
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
            BottomItem("⌂", "Inicio", selected == BottomTab.HOME, onHome)
            BottomItem("🏃", "Rutinas", selected == BottomTab.ROUTINES, onRoutines)
            BottomItem("▥", "Progreso", selected == BottomTab.PROGRESS, onProgress)
            BottomItem("❤", "Salud", selected == BottomTab.HEALTH, onHealth)
            BottomItem("☻", "Perfil", selected == BottomTab.PROFILE, onProfile)
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
    val color = if (selected) Color(0xFF006DE5) else Color(0xFF577095)
    Surface(onClick = onClick, color = Color.Transparent) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge, color = color)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
