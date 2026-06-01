package com.otero.runningvoicecoach.ui.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.workout.CustomWorkoutRepository
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.workout.ExampleWorkouts
import com.otero.runningvoicecoach.domain.workout.RoutinePreset
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.BottomTab
import com.otero.runningvoicecoach.ui.components.RunnersBottomBar

private val WorkoutsNavy = Color(0xFF06245A)
private val WorkoutsBlue = Color(0xFF006DE5)
private val WorkoutsOrange = Color(0xFFFF6A00)
private val WorkoutsSoft = Color(0xFFF7FAFF)
private val WorkoutsMuted = Color(0xFF577095)

@Composable
fun WorkoutListScreen(
    onBack: () -> Unit,
    onCreateWorkout: () -> Unit,
    onSelectWorkout: (String) -> Unit,
    onStartFreeRun: () -> Unit = { onSelectWorkout(ExampleWorkouts.freeRun.id) },
    routines: List<RoutinePreset> = ExampleWorkouts.presets,
    onHome: () -> Unit = onBack,
    onProgress: () -> Unit = {},
    onHealth: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { CustomWorkoutRepository(context.applicationContext) }
    val customWorkouts by repository.workouts.collectAsState(initial = emptyList())

    AppScaffold(title = "Rutinas", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WorkoutsSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WorkoutsHeader(onCreateWorkout = onCreateWorkout)
                FreeRunRow(onStartFreeRun = onStartFreeRun)

                if (customWorkouts.isNotEmpty()) {
                    Text(
                        text = "Tus rutinas",
                        style = MaterialTheme.typography.titleLarge,
                        color = WorkoutsNavy,
                        fontWeight = FontWeight.Bold
                    )
                    customWorkouts.forEach { workout ->
                        CustomWorkoutRow(workout = workout, onSelectWorkout = onSelectWorkout)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                routines.forEachIndexed { index, routine ->
                    RoutinePresetRow(
                        routine = routine,
                        index = index,
                        onSelectWorkout = onSelectWorkout
                    )
                }
            }

            RunnersBottomBar(
                selected = BottomTab.ROUTINES,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                onHome = onHome,
                onRoutines = {},
                onProgress = onProgress,
                onHealth = onHealth,
                onProfile = onProfile
            )
        }
    }
}

@Composable
private fun FreeRunRow(onStartFreeRun: () -> Unit) {
    RoutineRowShell(
        title = "Carrera libre",
        duration = "Sin limite",
        level = "Libre",
        accent = Color(0xFF12B76A),
        leading = "▶",
        onClick = onStartFreeRun
    )
}

@Composable
private fun WorkoutsHeader(onCreateWorkout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo6),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .height(230.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.82f
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            WorkoutsSoft,
                            WorkoutsSoft.copy(alpha = 0.82f),
                            WorkoutsSoft.copy(alpha = 0.10f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_home),
                contentDescription = "Runners",
                modifier = Modifier
                    .width(210.dp)
                    .height(72.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "Rutinas",
                color = WorkoutsNavy,
                fontSize = 43.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Elige tu entrenamiento y sigue avanzando.",
                color = WorkoutsMuted,
                fontSize = 18.sp,
                lineHeight = 22.sp
            )
            Button(
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoutsBlue),
                contentPadding = PaddingValues(horizontal = 16.dp),
                onClick = onCreateWorkout
            ) {
                Text("Rutina especial", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CustomWorkoutRow(
    workout: WorkoutPlan,
    onSelectWorkout: (String) -> Unit
) {
    RoutineRowShell(
        title = workout.name,
        duration = workout.estimatedDurationLabel(),
        level = "Personalizada",
        accent = WorkoutsBlue,
        leading = "★",
        onClick = { onSelectWorkout(workout.id) }
    )
}

@Composable
private fun RoutinePresetRow(
    routine: RoutinePreset,
    index: Int,
    onSelectWorkout: (String) -> Unit
) {
    RoutineRowShell(
        title = routine.workoutPlan.name,
        duration = routine.duracion,
        level = routine.nivel,
        accent = routine.nivel.accentColor(),
        leading = routine.leadingMark(index),
        onClick = { onSelectWorkout(routine.workoutPlan.id) }
    )
}

@Composable
private fun RoutineRowShell(
    title: String,
    duration: String,
    level: String,
    accent: Color,
    leading: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            onClick = onClick
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(accent)
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    RoutineMark(mark = leading, accent = accent)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            color = WorkoutsNavy,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "◷ $duration",
                                color = WorkoutsMuted,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "▁▃▆",
                                color = accent,
                                fontSize = 12.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LevelPill(level = level, accent = accent)
                        }
                    }
                    Text(
                        text = "›",
                        color = WorkoutsBlue,
                        fontSize = 36.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineMark(
    mark: String,
    accent: Color
) {
    Surface(
        modifier = Modifier.size(60.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFEAF4FF)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = mark,
                color = accent,
                fontSize = 24.sp,
                lineHeight = 26.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LevelPill(
    level: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
            text = level,
            color = accent,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun RoutinePreset.leadingMark(index: Int): String {
    return when {
        workoutPlan.name.contains("3K", ignoreCase = true) -> "3K"
        workoutPlan.name.contains("5K", ignoreCase = true) -> "5K"
        workoutPlan.name.contains("10K", ignoreCase = true) -> "10K"
        workoutPlan.name.contains("21K", ignoreCase = true) -> "21K"
        workoutPlan.name.contains("Caminata", ignoreCase = true) -> "👟"
        workoutPlan.name.contains("Velocidad", ignoreCase = true) -> "◴"
        workoutPlan.name.contains("Cuesta", ignoreCase = true) -> "↗"
        workoutPlan.name.contains("Fondo", ignoreCase = true) -> "〰"
        workoutPlan.name.contains("Recuper", ignoreCase = true) -> "♡"
        else -> "${index + 1}"
    }
}

private fun String.accentColor(): Color {
    return when (this) {
        "Avanzado" -> WorkoutsOrange
        "Suave" -> Color(0xFF00A8A8)
        "Intermedio" -> WorkoutsBlue
        else -> WorkoutsBlue
    }
}

private fun WorkoutPlan.estimatedDurationLabel(): String {
    val seconds = steps
        .filter { it.targetType == TargetType.TIME_SECONDS }
        .sumOf { it.targetValue }
        .toLong()

    return if (seconds > 0L) {
        "${seconds / 60L} min"
    } else {
        "Por distancia"
    }
}
