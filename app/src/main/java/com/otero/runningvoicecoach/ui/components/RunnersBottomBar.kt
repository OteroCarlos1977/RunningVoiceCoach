package com.otero.runningvoicecoach.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val RunnersBlue = Color(0xFF005BFF)
val RunnersDarkBlue = Color(0xFF062A68)
val RunnersMutedBlue = Color(0xFF5E6F95)
val RunnersWhite = Color(0xFFFFFFFF)
val RunnersOrange = Color(0xFFFF5A1F)
val RunnersBackground = Color(0xFFF6F9FF)

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Inicio",
        icon = Icons.Outlined.Home,
        route = "inicio"
    ),
    BottomNavItem(
        title = "Rutinas",
        icon = Icons.AutoMirrored.Outlined.DirectionsRun,
        route = "rutinas"
    ),
    BottomNavItem(
        title = "Progreso",
        icon = Icons.Outlined.BarChart,
        route = "progreso"
    ),
    BottomNavItem(
        title = "Salud",
        icon = Icons.Outlined.FavoriteBorder,
        route = "salud"
    ),
    BottomNavItem(
        title = "Perfil",
        icon = Icons.Outlined.Person,
        route = "perfil"
    )
)

enum class BottomTab(val route: String) {
    HOME("inicio"),
    ROUTINES("rutinas"),
    PROGRESS("progreso"),
    HEALTH("salud"),
    PROFILE("perfil")
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
    val actions = mapOf(
        "inicio" to onHome,
        "rutinas" to onRoutines,
        "progreso" to onProgress,
        "salud" to onHealth,
        "perfil" to onProfile
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RunnersWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                BottomItem(
                    item = item,
                    selected = item.route == selected.route,
                    onClick = actions[item.route].orEmptyAction()
                )
            }
        }
    }
}

@Composable
private fun BottomItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) RunnersBlue else RunnersMutedBlue
    Surface(onClick = onClick, color = Color.Transparent) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(25.dp),
                tint = color
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun (() -> Unit)?.orEmptyAction(): () -> Unit = this ?: {}
