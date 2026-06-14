package com.otero.runningvoicecoach.ui.activeRun

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.session.RunActivityRepository
import com.otero.runningvoicecoach.data.session.RunHistoryRepository
import com.otero.runningvoicecoach.data.session.RunSessionSummary
import com.otero.runningvoicecoach.data.session.RunStepSummary
import com.otero.runningvoicecoach.data.settings.UserSettingsRepository
import com.otero.runningvoicecoach.data.workout.CustomWorkoutRepository
import com.otero.runningvoicecoach.domain.activity.RunTelemetryRecorder
import com.otero.runningvoicecoach.domain.alert.AlertEngine
import com.otero.runningvoicecoach.domain.alert.AlertPriority
import com.otero.runningvoicecoach.domain.alert.LocalMessageProvider
import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.model.RunPauseSegment
import com.otero.runningvoicecoach.domain.model.RunRoutePoint
import com.otero.runningvoicecoach.domain.model.RunSession
import com.otero.runningvoicecoach.domain.model.RunSessionStatus
import com.otero.runningvoicecoach.domain.model.RunStepResult
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.domain.workout.ExampleWorkouts
import com.otero.runningvoicecoach.domain.workout.WorkoutEngine
import com.otero.runningvoicecoach.domain.workout.WorkoutEngineState
import com.otero.runningvoicecoach.location.LocationTracker
import com.otero.runningvoicecoach.location.RunForegroundService
import com.otero.runningvoicecoach.location.RunLocationState
import com.otero.runningvoicecoach.openai.OpenAIClient
import com.otero.runningvoicecoach.openai.RunningAlertContext
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.voice.AndroidVoiceCoach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ActiveRunScreen(
    workoutPlanId: String?,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val customWorkoutRepository = remember { CustomWorkoutRepository(context.applicationContext) }
    val customWorkouts by customWorkoutRepository.workouts.collectAsState(initial = emptyList())
    val workoutPlan = remember(workoutPlanId, customWorkouts) {
        customWorkouts.firstOrNull { it.id == workoutPlanId } ?: ExampleWorkouts.findById(workoutPlanId)
    }
    val workoutEngine = remember { WorkoutEngine() }
    val alertEngine = remember { AlertEngine() }
    val voiceCoach = remember { AndroidVoiceCoach(context) }
    val locationTracker = remember { LocationTracker(context.applicationContext) }
    val historyRepository = remember { RunHistoryRepository(context.applicationContext) }
    val activityRepository = remember { RunActivityRepository(context.applicationContext) }
    val settingsRepository = remember { UserSettingsRepository(context.applicationContext) }
    val telemetryRecorder = remember { RunTelemetryRecorder() }
    val userSettings by settingsRepository.settings.collectAsState(
        initial = com.otero.runningvoicecoach.data.settings.UserSettings()
    )
    val openAIClient = remember(userSettings.developmentOpenAiApiKey) {
        OpenAIClient(apiKey = userSettings.developmentOpenAiApiKey.ifBlank { com.otero.runningvoicecoach.BuildConfig.OPENAI_API_KEY })
    }
    val coroutineScope = rememberCoroutineScope()
    val locationState by locationTracker.state.collectAsState()
    val latestLocationState by rememberUpdatedState(locationState)

    val useGpsMode = true
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var isFinished by rememberSaveable { mutableStateOf(false) }
    var showFinishConfirmation by rememberSaveable { mutableStateOf(false) }
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var totalDurationSeconds by rememberSaveable { mutableLongStateOf(0L) }
    var stepDurationSeconds by rememberSaveable { mutableLongStateOf(0L) }
    var totalDistanceMeters by rememberSaveable { mutableDoubleStateOf(0.0) }
    var stepDistanceMeters by rememberSaveable { mutableDoubleStateOf(0.0) }
    var lastGpsTotalDistanceMeters by rememberSaveable { mutableDoubleStateOf(0.0) }
    var sessionStartedAtMillis by rememberSaveable { mutableLongStateOf(0L) }
    var activePauseStartedAtMillis by rememberSaveable { mutableLongStateOf(0L) }
    var activePauseStartedAtElapsedSeconds by rememberSaveable { mutableLongStateOf(0L) }
    var currentPaceSecondsPerKm by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedBackgroundIndex by rememberSaveable { mutableIntStateOf(0) }
    val completedStepSummaries = remember { mutableStateListOf<RunStepSummary>() }
    val pauseSegments = remember { mutableStateListOf<RunPauseSegment>() }
    var engineState by remember(workoutPlan.id) {
        mutableStateOf(
            workoutEngine.evaluate(
                workoutPlan = workoutPlan,
                currentStepIndex = currentStepIndex,
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                stepDistanceMeters = stepDistanceMeters,
                stepDurationSeconds = stepDurationSeconds,
                currentPaceSecondsPerKm = currentPaceSecondsPerKm,
                paceToleranceSeconds = userSettings.generalPaceToleranceSeconds
            )
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationTracker.reset()
            lastGpsTotalDistanceMeters = 0.0
            if (sessionStartedAtMillis == 0L) {
                sessionStartedAtMillis = System.currentTimeMillis()
            }
            isRunning = true
            isPaused = false
            isFinished = false
            locationTracker.start()
            RunForegroundService.start(context)
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Notification permission is best-effort; the foreground service still owns location tracking.
    }

    DisposableEffect(Unit) {
        onDispose {
            locationTracker.stop()
            RunForegroundService.stop(context)
            voiceCoach.shutdown()
        }
    }

    LaunchedEffect(isRunning, isPaused, isFinished, currentStepIndex) {
        while (isRunning && !isPaused && !isFinished) {
            delay(1_000L)

            val nextPace = if (useGpsMode) {
                latestLocationState.currentPaceSecondsPerKm
            } else {
                simulatedPaceFor(totalDurationSeconds)
            }
            val metersThisSecond = if (useGpsMode) {
                val gpsDistance = latestLocationState.totalDistanceMeters
                val gpsDelta = (gpsDistance - lastGpsTotalDistanceMeters)
                    .coerceAtLeast(0.0)
                lastGpsTotalDistanceMeters = gpsDistance
                gpsDelta
            } else {
                METERS_PER_KILOMETER / (nextPace ?: DEFAULT_SIMULATED_PACE_SECONDS_PER_KM)
            }

            totalDurationSeconds += 1L
            stepDurationSeconds += 1L
            totalDistanceMeters += metersThisSecond
            stepDistanceMeters += metersThisSecond
            currentPaceSecondsPerKm = nextPace
            telemetryRecorder.recordSample(
                elapsedSeconds = totalDurationSeconds,
                totalDistanceMeters = totalDistanceMeters,
                speedKmh = speedKmhFor(nextPace, metersThisSecond),
                routePoint = routePointFor(
                    locationState = latestLocationState,
                    elapsedSeconds = totalDurationSeconds,
                    totalDistanceMeters = totalDistanceMeters,
                    useGpsMode = useGpsMode
                )
            )

            val nextState = workoutEngine.evaluate(
                workoutPlan = workoutPlan,
                currentStepIndex = currentStepIndex,
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                stepDistanceMeters = stepDistanceMeters,
                stepDurationSeconds = stepDurationSeconds,
                currentPaceSecondsPerKm = currentPaceSecondsPerKm,
                paceToleranceSeconds = userSettings.generalPaceToleranceSeconds
            )
            engineState = nextState

            val alerts = alertEngine.evaluate(
                state = nextState,
                totalDistanceMeters = totalDistanceMeters,
                nowMillis = totalDurationSeconds * 1_000L,
                minPaceAlertIntervalMillisOverride = userSettings.minAlertIntervalSeconds * 1_000L
            )
            alerts.forEachIndexed { index, alert ->
                if (userSettings.voiceEnabled) {
                    coroutineScope.launch {
                        val message = if (userSettings.openAiEnabled) {
                            runCatching {
                                openAIClient.generateRunningMessage(
                                    RunningAlertContext.fromAlertEvent(
                                        alertEvent = alert,
                                        targetPaceSecondsPerKm = nextState.currentStep?.targetPaceSecondsPerKm,
                                        currentPaceSecondsPerKm = currentPaceSecondsPerKm
                                    )
                                )
                            }.getOrElse {
                                LocalMessageProvider.messageFor(alert)
                            }
                        } else {
                            LocalMessageProvider.messageFor(alert)
                        }

                        voiceCoach.speak(
                            message = message,
                            flush = index == 0 && alert.priority == AlertPriority.HIGH
                        )
                    }
                }
            }

            if (nextState.isWorkoutFinished) {
                appendStepSummaryIfNeeded(
                    summaries = completedStepSummaries,
                    state = nextState,
                    stepDistanceMeters = stepDistanceMeters,
                    stepDurationSeconds = stepDurationSeconds
                )
                val sessionId = UUID.randomUUID().toString()
                val finishedAtMillis = System.currentTimeMillis()
                historyRepository.saveSession(
                    RunSessionSummary(
                        id = sessionId,
                        workoutName = workoutPlan.name,
                        finishedAtMillis = finishedAtMillis,
                        totalDistanceMeters = totalDistanceMeters,
                        totalDurationSeconds = totalDurationSeconds,
                        averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
                            distanceMeters = totalDistanceMeters,
                            durationSeconds = totalDurationSeconds
                        ),
                        stepSummaries = completedStepSummaries.toList()
                    )
                )
                activityRepository.saveSession(
                    buildRunSession(
                        id = sessionId,
                        workoutPlanId = workoutPlan.id,
                        workoutName = workoutPlan.name,
                        startedAtMillis = sessionStartedAtMillis,
                        finishedAtMillis = finishedAtMillis,
                        totalDistanceMeters = totalDistanceMeters,
                        totalDurationSeconds = totalDurationSeconds,
                        stepSummaries = completedStepSummaries,
                        pauseSegments = pauseSegments,
                        telemetryRecorder = telemetryRecorder
                    )
                )
                isFinished = true
                isRunning = false
                onFinish()
            } else if (nextState.shouldMoveToNextStep) {
                appendStepSummaryIfNeeded(
                    summaries = completedStepSummaries,
                    state = nextState,
                    stepDistanceMeters = stepDistanceMeters,
                    stepDurationSeconds = stepDurationSeconds
                )
                currentStepIndex += 1
                stepDurationSeconds = 0L
                stepDistanceMeters = 0.0
            }
        }
    }

    fun finishCurrentRun() {
        voiceCoach.stop()
        locationTracker.stop()
        RunForegroundService.stop(context)
        isRunning = false
        isFinished = true
        showFinishConfirmation = false
        coroutineScope.launch {
            if (activePauseStartedAtMillis > 0L) {
                pauseSegments += RunPauseSegment(
                    startedAtMillis = activePauseStartedAtMillis,
                    endedAtMillis = System.currentTimeMillis(),
                    startedAtElapsedSeconds = activePauseStartedAtElapsedSeconds,
                    endedAtElapsedSeconds = totalDurationSeconds
                )
                activePauseStartedAtMillis = 0L
                activePauseStartedAtElapsedSeconds = 0L
            }
            appendStepSummaryIfNeeded(
                summaries = completedStepSummaries,
                state = engineState,
                stepDistanceMeters = stepDistanceMeters,
                stepDurationSeconds = stepDurationSeconds
            )
            val sessionId = UUID.randomUUID().toString()
            val finishedAtMillis = System.currentTimeMillis()
            historyRepository.saveSession(
                RunSessionSummary(
                    id = sessionId,
                    workoutName = workoutPlan.name,
                    finishedAtMillis = finishedAtMillis,
                    totalDistanceMeters = totalDistanceMeters,
                    totalDurationSeconds = totalDurationSeconds,
                    averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
                        distanceMeters = totalDistanceMeters,
                        durationSeconds = totalDurationSeconds
                    ),
                    stepSummaries = completedStepSummaries.toList()
                )
            )
            activityRepository.saveSession(
                buildRunSession(
                    id = sessionId,
                    workoutPlanId = workoutPlan.id,
                    workoutName = workoutPlan.name,
                    startedAtMillis = sessionStartedAtMillis,
                    finishedAtMillis = finishedAtMillis,
                    totalDistanceMeters = totalDistanceMeters,
                    totalDurationSeconds = totalDurationSeconds,
                    stepSummaries = completedStepSummaries,
                    pauseSegments = pauseSegments,
                    telemetryRecorder = telemetryRecorder
                )
            )
            onFinish()
        }
    }

    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text("Finalizar actividad") },
            text = {
                Text(
                    text = "Se guardara la actividad actual con ${formatDuration(totalDurationSeconds)} y ${formatDistance(totalDistanceMeters)}."
                )
            },
            confirmButton = {
                TextButton(onClick = ::finishCurrentRun) {
                    Text("Finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) {
                    Text("Seguir corriendo")
                }
            }
        )
    }

    AppScaffold(title = "Carrera activa", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = activityBackgrounds[selectedBackgroundIndex].resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 1f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.02f),
                                Color.Black.copy(alpha = 0.08f),
                                Color(0xF2052145)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 34.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.86f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("●", color = Color(0xFF12B76A), fontSize = 15.sp)
                                Text("GPS", color = Color(0xFF06245A), fontSize = 17.sp, fontWeight = FontWeight.Medium)
                                Text("▁▃▆", color = Color(0xFF12B76A), fontSize = 16.sp)
                                Text("❤ 128", color = Color(0xFF06245A), fontSize = 15.sp)
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            onClick = onBack
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                text = "‹",
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.logo_home),
                        contentDescription = "Runners",
                        modifier = Modifier
                            .width(220.dp)
                            .height(78.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.2f".format(totalDistanceMeters / METERS_PER_KILOMETER),
                        color = Color.White,
                        fontSize = 78.sp,
                        lineHeight = 82.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DISTANCIA (km)",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 17.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.padding(top = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(modifier = Modifier.size(9.dp), shape = CircleShape, color = Color.White) {}
                        Surface(modifier = Modifier.size(9.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.34f)) {}
                        Surface(modifier = Modifier.size(9.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.34f)) {}
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (useGpsMode && locationState.lastError != null) {
                        Text(
                            text = locationState.lastError.orEmpty(),
                            color = Color(0xFFFFD0D0),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ActivityMetric("TIEMPO", formatDuration(totalDurationSeconds))
                            ActivityDivider()
                            ActivityMetric("RITMO", PaceCalculator.formatPace(currentPaceSecondsPerKm), "min/km")
                            ActivityDivider()
                            ActivityMetric("CALORIAS", estimatedCalories(totalDistanceMeters).toString(), "kcal")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ActivityMetric("FRECUENCIA", "❤ 128", "ppm")
                            ActivityDivider()
                            ActivityMetric(
                                "RITMO PROM.",
                                PaceCalculator.formatPace(
                                    PaceCalculator.calculateAveragePaceSecondsPerKm(
                                        distanceMeters = totalDistanceMeters,
                                        durationSeconds = totalDurationSeconds
                                    )
                                ),
                                "min/km"
                            )
                            ActivityDivider()
                            ActivityMetric("DESNIVEL", "320", "m")
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.13f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(engineState.currentStep?.name ?: "Finalizado", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(engineState.paceStatus.displayName(), color = paceStatusColor(engineState.paceStatus), fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { engineState.stepProgressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = Color(0xFF006DE5),
                                trackColor = Color.White.copy(alpha = 0.20f)
                            )
                            Text(
                                text = "Objetivo ${PaceCalculator.formatPace(engineState.currentStep?.targetPaceSecondsPerKm)} · ${formatPaceDifference(engineState.paceDifferenceSeconds)}",
                                color = Color.White.copy(alpha = 0.76f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActivityControlButton(
                            label = "▣",
                            container = Color.White,
                            content = Color(0xFF006DE5),
                            size = 76,
                            onClick = {
                                selectedBackgroundIndex = (selectedBackgroundIndex + 1) % activityBackgrounds.size
                            }
                        )
                        ActivityControlButton(
                            label = if (isRunning && !isPaused) "Ⅱ" else "▶",
                            container = Color(0xFF006DE5),
                            content = Color.White,
                            size = 112,
                            onClick = {
                                if (isFinished) {
                                    currentStepIndex = 0
                                    totalDurationSeconds = 0L
                                    stepDurationSeconds = 0L
                                    totalDistanceMeters = 0.0
                                    stepDistanceMeters = 0.0
                                    lastGpsTotalDistanceMeters = 0.0
                                    sessionStartedAtMillis = 0L
                                    activePauseStartedAtMillis = 0L
                                    activePauseStartedAtElapsedSeconds = 0L
                                    currentPaceSecondsPerKm = null
                                    completedStepSummaries.clear()
                                    pauseSegments.clear()
                                    telemetryRecorder.reset()
                                    alertEngine.reset()
                                    locationTracker.reset()
                                    isFinished = false
                                }
                                if (isRunning) {
                                    val nextPaused = !isPaused
                                    if (nextPaused) {
                                        activePauseStartedAtMillis = System.currentTimeMillis()
                                        activePauseStartedAtElapsedSeconds = totalDurationSeconds
                                    } else if (activePauseStartedAtMillis > 0L) {
                                        pauseSegments += RunPauseSegment(
                                            startedAtMillis = activePauseStartedAtMillis,
                                            endedAtMillis = System.currentTimeMillis(),
                                            startedAtElapsedSeconds = activePauseStartedAtElapsedSeconds,
                                            endedAtElapsedSeconds = totalDurationSeconds
                                        )
                                        activePauseStartedAtMillis = 0L
                                        activePauseStartedAtElapsedSeconds = 0L
                                    }
                                    isPaused = nextPaused
                                    if (isPaused) {
                                        voiceCoach.stop()
                                        if (useGpsMode) {
                                            locationTracker.stop()
                                            RunForegroundService.stop(context)
                                        }
                                    } else if (useGpsMode && locationTracker.hasLocationPermission()) {
                                        locationTracker.start()
                                        RunForegroundService.start(context)
                                    }
                                } else if (useGpsMode) {
                                    if (locationTracker.hasLocationPermission()) {
                                        locationTracker.start()
                                        RunForegroundService.start(context)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        if (sessionStartedAtMillis == 0L) {
                                            sessionStartedAtMillis = System.currentTimeMillis()
                                        }
                                        isRunning = true
                                        isPaused = false
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                } else {
                                    if (sessionStartedAtMillis == 0L) {
                                        sessionStartedAtMillis = System.currentTimeMillis()
                                    }
                                    isRunning = true
                                    isPaused = false
                                }
                            }
                        )
                        ActivityControlButton(
                            label = "■",
                            container = Color.White,
                            content = Color(0xFFFF4B0B),
                            size = 76,
                            onClick = {
                                if (totalDurationSeconds > 0L || totalDistanceMeters > 0.0) {
                                    showFinishConfirmation = true
                                }
                            }
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = Color.White.copy(alpha = 0.94f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⌃", color = Color(0xFF06245A), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Desliza hacia arriba para ver mas", color = Color(0xFF06245A), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityMetric(
    label: String,
    value: String,
    unit: String? = null
) {
    Column(
        modifier = Modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(value, color = Color.White, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium)
        if (unit != null) {
            Text(unit, color = Color.White.copy(alpha = 0.62f), fontSize = 13.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun ActivityDivider() {
    Spacer(
        modifier = Modifier
            .width(1.dp)
            .height(66.dp)
            .background(Color.White.copy(alpha = 0.36f))
    )
}

@Composable
private fun ActivityControlButton(
    label: String,
    container: Color,
    content: Color,
    size: Int,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        onClick = onClick
    ) {
        Text(label, fontSize = if (size > 90) 50.sp else 32.sp, fontWeight = FontWeight.Bold)
    }
}

private fun estimatedCalories(distanceMeters: Double): Int {
    return (distanceMeters / METERS_PER_KILOMETER * 70.0).toInt().coerceAtLeast(0)
}

private fun paceStatusColor(status: PaceStatus): Color {
    return when (status) {
        PaceStatus.WITHIN_TARGET -> Color(0xFF12B76A)
        PaceStatus.TOO_FAST -> Color(0xFFFFC857)
        PaceStatus.TOO_SLOW -> Color(0xFFFF6A00)
        PaceStatus.NO_TARGET -> Color.White.copy(alpha = 0.72f)
    }
}

@Composable
private fun ActivityBackgroundSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Fondo de actividad", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activityBackgrounds.forEachIndexed { index, background ->
                val borderColor = if (index == selectedIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
                Image(
                    painter = painterResource(id = background.resId),
                    contentDescription = background.label,
                    modifier = Modifier
                        .size(width = 82.dp, height = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onSelect(index) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun SourceModeRow(
    useGpsMode: Boolean,
    isRunning: Boolean,
    onUseGpsModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Fuente de datos", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (useGpsMode) "GPS real" else "Simulador",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Switch(
            checked = useGpsMode,
            onCheckedChange = onUseGpsModeChange,
            enabled = !isRunning
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (large) 6.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f)
            )
            Text(
                text = value,
                style = if (large) {
                    MaterialTheme.typography.displaySmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
                color = if (large) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

private fun simulatedPaceFor(totalDurationSeconds: Long): Int {
    return when ((totalDurationSeconds / 30L) % 3L) {
        0L -> 420
        1L -> 360
        else -> 480
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private fun formatDistance(distanceMeters: Double): String {
    return "%.2f km".format(distanceMeters / METERS_PER_KILOMETER)
}

private fun formatPaceDifference(differenceSeconds: Int?): String {
    if (differenceSeconds == null) {
        return "Sin objetivo"
    }

    return when {
        differenceSeconds < 0 -> "${-differenceSeconds}s rapido"
        differenceSeconds > 0 -> "${differenceSeconds}s lento"
        else -> "En objetivo"
    }
}

private fun startButtonLabel(
    isRunning: Boolean,
    isPaused: Boolean,
    isFinished: Boolean
): String {
    return when {
        isFinished -> "Reiniciar"
        isRunning && !isPaused -> "Corriendo"
        isPaused -> "Reanudar"
        else -> "Iniciar"
    }
}

private fun PaceStatus.displayName(): String {
    return when (this) {
        PaceStatus.WITHIN_TARGET -> "En objetivo"
        PaceStatus.TOO_FAST -> "Rapido"
        PaceStatus.TOO_SLOW -> "Lento"
        PaceStatus.NO_TARGET -> "Sin objetivo"
    }
}

private fun appendStepSummaryIfNeeded(
    summaries: MutableList<RunStepSummary>,
    state: WorkoutEngineState,
    stepDistanceMeters: Double,
    stepDurationSeconds: Long
) {
    val step = state.currentStep ?: return
    if (stepDurationSeconds <= 0L && stepDistanceMeters <= 0.0) {
        return
    }
    if (summaries.size > state.currentStepIndex) {
        return
    }

    val averagePace = PaceCalculator.calculateAveragePaceSecondsPerKm(
        distanceMeters = stepDistanceMeters,
        durationSeconds = stepDurationSeconds
    )

    summaries.add(
        RunStepSummary(
            stepName = step.name,
            distanceMeters = stepDistanceMeters,
            durationSeconds = stepDurationSeconds,
            averagePaceSecondsPerKm = averagePace,
            paceStatus = PaceCalculator.comparePace(
                current = averagePace,
                target = step.targetPaceSecondsPerKm,
                tolerance = step.paceToleranceSeconds
            )
        )
    )
}

private fun buildRunSession(
    id: String,
    workoutPlanId: String,
    workoutName: String,
    startedAtMillis: Long,
    finishedAtMillis: Long,
    totalDistanceMeters: Double,
    totalDurationSeconds: Long,
    stepSummaries: List<RunStepSummary>,
    pauseSegments: List<RunPauseSegment>,
    telemetryRecorder: RunTelemetryRecorder
): RunSession {
    val telemetry = telemetryRecorder.snapshot(
        totalDistanceMeters = totalDistanceMeters,
        totalDurationSeconds = totalDurationSeconds
    )
    val pausedDurationSeconds = pauseSegments.sumOf { it.durationSeconds ?: 0L }

    return RunSession(
        id = id,
        workoutPlanId = workoutPlanId,
        workoutName = workoutName,
        startTimeMillis = startedAtMillis.takeIf { it > 0L }
            ?: (finishedAtMillis - (totalDurationSeconds + pausedDurationSeconds) * 1_000L),
        endTimeMillis = finishedAtMillis,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationSeconds = totalDurationSeconds,
        averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
            distanceMeters = totalDistanceMeters,
            durationSeconds = totalDurationSeconds
        ),
        stepResults = stepSummaries.mapIndexed { index, summary ->
            RunStepResult(
                stepId = "step-$index",
                stepName = summary.stepName,
                distanceMeters = summary.distanceMeters,
                durationSeconds = summary.durationSeconds,
                averagePaceSecondsPerKm = summary.averagePaceSecondsPerKm,
                targetPaceSecondsPerKm = null,
                complianceStatus = summary.paceStatus,
                stepIndex = index
            )
        },
        status = RunSessionStatus.FINISHED,
        elapsedDurationSeconds = totalDurationSeconds + pausedDurationSeconds,
        pausedDurationSeconds = pausedDurationSeconds,
        averageSpeedKmh = telemetry.averageSpeedKmh,
        maxSpeedKmh = telemetry.maxSpeedKmh,
        estimatedCalories = estimatedCalories(totalDistanceMeters),
        kilometerSplits = telemetry.kilometerSplits,
        routePoints = telemetry.routePoints,
        pauseSegments = pauseSegments.toList()
    )
}

private fun routePointFor(
    locationState: RunLocationState,
    elapsedSeconds: Long,
    totalDistanceMeters: Double,
    useGpsMode: Boolean
): RunRoutePoint? {
    if (!useGpsMode) {
        return null
    }

    val latitude = locationState.latitude ?: return null
    val longitude = locationState.longitude ?: return null

    return RunRoutePoint(
        latitude = latitude,
        longitude = longitude,
        recordedAtMillis = locationState.timestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
        elapsedSeconds = elapsedSeconds,
        distanceMeters = totalDistanceMeters,
        accuracyMeters = locationState.accuracyMeters,
        altitudeMeters = locationState.altitudeMeters,
        speedMetersPerSecond = locationState.speedMetersPerSecond
    )
}

private fun speedKmhFor(paceSecondsPerKm: Int?, metersThisSecond: Double): Double? {
    if (paceSecondsPerKm != null && paceSecondsPerKm > 0) {
        return SECONDS_PER_HOUR / paceSecondsPerKm
    }
    if (metersThisSecond > 0.0) {
        return metersThisSecond * 3.6
    }
    return null
}

private data class ActivityBackground(
    val label: String,
    val resId: Int
)

private val activityBackgrounds = listOf(
    ActivityBackground("Fondo 1", R.drawable.fondo1),
    ActivityBackground("Fondo 2", R.drawable.fondo2),
    ActivityBackground("Fondo 3", R.drawable.fondo3),
    ActivityBackground("Fondo 4", R.drawable.fondo4),
    ActivityBackground("Fondo 5", R.drawable.fondo5),
    ActivityBackground("Fondo 6", R.drawable.fondo6)
)

private const val METERS_PER_KILOMETER = 1000.0
private const val DEFAULT_SIMULATED_PACE_SECONDS_PER_KM = 420
private const val SECONDS_PER_HOUR = 3600.0
