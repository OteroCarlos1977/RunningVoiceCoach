package com.otero.runningvoicecoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.navigation.RunningVoiceCoachNavHost
import com.otero.runningvoicecoach.ui.theme.RunningVoiceCoachTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RunningVoiceCoachTheme(darkTheme = false) {
                RunnersAppEntry()
            }
        }
    }
}

@Composable
private fun RunnersAppEntry() {
    var entryState by remember { mutableStateOf(EntryState.Brand) }

    LaunchedEffect(entryState) {
        when (entryState) {
            EntryState.Brand -> {
                delay(900L)
                entryState = EntryState.Welcome
            }
            EntryState.Loading -> {
                delay(1_500L)
                entryState = EntryState.App
            }
            EntryState.Welcome,
            EntryState.App -> Unit
        }
    }

    when (entryState) {
        EntryState.Brand -> RunnersBrandScreen()
        EntryState.Welcome -> RunnersWelcomeScreen(onStart = { entryState = EntryState.Loading })
        EntryState.Loading -> RunnersSplashScreen()
        EntryState.App -> RunningVoiceCoachNavHost()
    }
}

private enum class EntryState {
    Brand,
    Welcome,
    Loading,
    App
}

@Composable
private fun RunnersBrandScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFF)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_home),
            contentDescription = "Runners",
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(120.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun RunnersWelcomeScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFF))
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.86f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFF7FAFF),
                            Color(0xF2F7FAFF),
                            Color(0x66F7FAFF)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 54.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(36.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_home),
                    contentDescription = "Runners",
                    modifier = Modifier
                        .width(260.dp)
                        .height(92.dp),
                    contentScale = ContentScale.Fit
                )
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(
                        text = "Bienvenido,\nvamos a\ncorrer",
                        color = Color(0xFF06245A),
                        fontSize = 54.sp,
                        lineHeight = 58.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        modifier = Modifier
                            .width(72.dp)
                            .height(5.dp),
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFF6A00)
                    ) {}
                    Text(
                        text = "Cada paso te acerca\na tu mejor version.",
                        color = Color(0xFF24416A),
                        fontSize = 23.sp,
                        lineHeight = 30.sp
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006DE5)),
                    onClick = onStart
                ) {
                    Text("Comenzar   ›", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Iniciar sesion",
                    color = Color(0xFF006DE5),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                CardInfo()
            }
        }
    }
}

@Composable
private fun CardInfo() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.90f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Mas que correr", color = Color(0xFF06245A), fontSize = 18.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
            Text("Energia, enfoque y bienestar para tu dia a dia.", color = Color(0xFF24416A), fontSize = 13.sp)
        }
    }
}

@Composable
private fun RunnersSplashScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pantalla_binevenida),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xCC06162D),
                            Color(0xF206162D)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 44.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_home),
                contentDescription = "Runners",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = "Tu ritmo. Tu carrera. Tu progreso.",
                    style = MaterialTheme.typography.headlineLarge,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Preparando tu entrenamiento",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.78f)
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = Color.White.copy(alpha = 0.22f)
                )
            }
        }
    }
}
