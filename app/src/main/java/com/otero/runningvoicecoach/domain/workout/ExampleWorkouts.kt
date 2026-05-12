package com.otero.runningvoicecoach.domain.workout

import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.model.WorkoutStep

data class RoutinePreset(
    val id: Int,
    val workoutPlan: WorkoutPlan,
    val nivel: String,
    val duracion: String,
    val objetivo: String,
    val detalle: List<String>,
    val idealPara: String
)

object ExampleWorkouts {
    val simulacionRitmo7Min = WorkoutPlan(
        id = "simulacion-ritmo-7min",
        name = "Simulacion ritmo 7:00",
        description = "Prueba de avisos con objetivo 7:00 min/km y tolerancia 10%",
        steps = listOf(
            timeStep(
                id = "steady-7min",
                name = "Ritmo 7:00",
                type = StepType.EASY,
                seconds = 900.0,
                paceSecondsPerKm = 420,
                toleranceSeconds = 42
            )
        )
    )

    val intervalos5x1000 = WorkoutPlan(
        id = "intervalos-5x1000",
        name = "Intervalos 5 x 1000",
        description = "Entrenamiento de intervalos para 10K",
        steps = buildList {
            add(timeStep("warmup", "Entrada en calor", StepType.WARMUP, 600.0))
            repeat(5) { index ->
                val number = index + 1
                add(distanceStep("interval-$number", "Intervalo $number", StepType.INTERVAL, 1000.0, 330, 10))
                if (number < 5) {
                    add(timeStep("recovery-$number", "Recuperacion $number", StepType.RECOVERY, 120.0, 420, 25))
                }
            }
            add(timeStep("cooldown", "Vuelta a la calma", StepType.COOLDOWN, 600.0))
        }
    )

    val presets: List<RoutinePreset> = listOf(
        RoutinePreset(
            id = 1,
            workoutPlan = WorkoutPlan(
                id = "inicio-3k",
                name = "Inicio 3K",
                description = "Comenzar a moverse sin exigir demasiado el cuerpo.",
                steps = listOf(
                    timeStep("walk-start", "Caminata suave", StepType.WARMUP, 300.0, 720, 60),
                    *repeatWalkRun(
                        prefix = "walk-run",
                        repetitions = 5,
                        runSeconds = 60.0,
                        walkSeconds = 60.0
                    ).toTypedArray(),
                    timeStep("walk-end", "Caminata final", StepType.COOLDOWN, 300.0, 720, 60)
                )
            ),
            nivel = "Principiante",
            duracion = "20 min",
            objetivo = "Comenzar a moverse sin exigir demasiado el cuerpo.",
            detalle = listOf(
                "5 min de caminata suave",
                "10 min alternando 1 min de trote + 1 min de caminata",
                "5 min de caminata final para bajar pulsaciones"
            ),
            idealPara = "Usuarios que recien empiezan o vuelven despues de mucho tiempo"
        ),
        RoutinePreset(
            id = 2,
            workoutPlan = WorkoutPlan(
                id = "caminata-trote",
                name = "Caminata + Trote",
                description = "Mejorar la adaptacion cardiovascular.",
                steps = listOf(
                    timeStep("walk-fast", "Caminata rapida", StepType.WARMUP, 300.0, 660, 60),
                    *repeatWalkRun(
                        prefix = "two-one",
                        repetitions = 5,
                        runSeconds = 120.0,
                        walkSeconds = 60.0
                    ).toTypedArray(),
                    timeStep("walk-soft", "Caminata suave final", StepType.COOLDOWN, 300.0, 720, 60)
                )
            ),
            nivel = "Principiante",
            duracion = "25 min",
            objetivo = "Mejorar la adaptacion cardiovascular.",
            detalle = listOf(
                "5 min de caminata rapida",
                "15 min alternando 2 min de trote suave + 1 min de caminata",
                "5 min de caminata suave final"
            ),
            idealPara = "Usuarios que todavia no pueden correr varios minutos seguidos"
        ),
        RoutinePreset(
            id = 3,
            workoutPlan = WorkoutPlan(
                id = "5k-principiante",
                name = "5K Principiante",
                description = "Preparar al usuario para completar 5 km progresivamente.",
                steps = listOf(
                    timeStep("warmup", "Entrada en calor caminando", StepType.WARMUP, 300.0, 720, 60),
                    timeStep("easy-run", "Trote suave continuo", StepType.EASY, 1200.0, 420, 42),
                    timeStep("cooldown", "Vuelta a la calma", StepType.COOLDOWN, 300.0, 720, 60)
                )
            ),
            nivel = "Principiante",
            duracion = "30 min",
            objetivo = "Preparar al usuario para completar 5 km progresivamente.",
            detalle = listOf(
                "5 min de entrada en calor caminando",
                "20 min de trote suave continuo o alternado",
                "5 min de vuelta a la calma"
            ),
            idealPara = "Usuarios que ya toleran trotar entre 10 y 20 minutos"
        ),
        RoutinePreset(
            id = 4,
            workoutPlan = WorkoutPlan(
                id = "5k-intermedio",
                name = "5K Intermedio",
                description = "Mejorar ritmo y resistencia en distancias cortas.",
                steps = listOf(
                    timeStep("easy-start", "Trote suave", StepType.WARMUP, 300.0, 420, 42),
                    timeStep("steady", "Carrera continua comoda", StepType.EASY, 1200.0, 390, 35),
                    timeStep("strong", "Ritmo mas fuerte", StepType.TEMPO, 300.0, 360, 30),
                    timeStep("easy-end", "Trote suave final", StepType.COOLDOWN, 300.0, 480, 45)
                )
            ),
            nivel = "Intermedio",
            duracion = "35 min",
            objetivo = "Mejorar ritmo y resistencia en distancias cortas.",
            detalle = listOf(
                "5 min de trote suave",
                "20 min de carrera continua a ritmo comodo",
                "5 min de ritmo un poco mas fuerte",
                "5 min de trote suave o caminata final"
            ),
            idealPara = "Corredores que ya completan 5 km y quieren mejorar"
        ),
        RoutinePreset(
            id = 5,
            workoutPlan = WorkoutPlan(
                id = "10k-base",
                name = "10K Base",
                description = "Construir resistencia para llegar a 10 km.",
                steps = listOf(
                    timeStep("easy-start", "Trote suave", StepType.WARMUP, 600.0, 420, 42),
                    timeStep("steady", "Carrera continua comoda", StepType.EASY, 1500.0, 410, 40),
                    timeStep("moderate", "Trote moderado", StepType.TEMPO, 300.0, 390, 35),
                    timeStep("cooldown", "Trote muy suave", StepType.COOLDOWN, 300.0, 510, 60)
                )
            ),
            nivel = "Intermedio",
            duracion = "45 min",
            objetivo = "Construir resistencia para llegar a 10 km.",
            detalle = listOf(
                "10 min de trote suave",
                "25 min de carrera continua a ritmo comodo",
                "5 min de trote moderado",
                "5 min de caminata o trote muy suave"
            ),
            idealPara = "Usuarios que ya corren 5 km con comodidad"
        ),
        RoutinePreset(
            id = 6,
            workoutPlan = WorkoutPlan(
                id = "intervalos-velocidad",
                name = "Intervalos de Velocidad",
                description = "Mejorar velocidad, potencia y respuesta cardiovascular.",
                steps = buildList {
                    add(timeStep("warmup", "Trote suave", StepType.WARMUP, 480.0, 420, 42))
                    repeat(8) { index ->
                        val number = index + 1
                        add(timeStep("fast-$number", "Rapido $number", StepType.INTERVAL, 30.0, 300, 20))
                        add(timeStep("recovery-$number", "Recuperacion $number", StepType.RECOVERY, 90.0, 540, 60))
                    }
                    add(timeStep("cooldown", "Trote suave final", StepType.COOLDOWN, 360.0, 480, 50))
                }
            ),
            nivel = "Avanzado",
            duracion = "30 min",
            objetivo = "Mejorar velocidad, potencia y respuesta cardiovascular.",
            detalle = listOf(
                "8 min de trote suave",
                "8 repeticiones de 30 seg rapido + 90 seg suave o caminando",
                "6 min de trote suave final"
            ),
            idealPara = "Corredores que quieren bajar tiempos"
        ),
        RoutinePreset(
            id = 7,
            workoutPlan = WorkoutPlan(
                id = "series-cuesta",
                name = "Series en Cuesta",
                description = "Fortalecer piernas y mejorar potencia.",
                steps = buildList {
                    add(timeStep("warmup", "Trote suave", StepType.WARMUP, 600.0, 420, 42))
                    repeat(6) { index ->
                        val number = index + 1
                        add(timeStep("hill-$number", "Subida fuerte $number", StepType.INTERVAL, 45.0, 330, 25))
                        add(timeStep("down-$number", "Recuperacion bajando", StepType.RECOVERY, 120.0, 600, 60))
                    }
                    add(timeStep("cooldown", "Trote suave final", StepType.COOLDOWN, 600.0, 480, 50))
                }
            ),
            nivel = "Avanzado",
            duracion = "35 min",
            objetivo = "Fortalecer piernas y mejorar potencia.",
            detalle = listOf(
                "10 min de trote suave",
                "6 repeticiones en subida de 30 a 45 seg corriendo fuerte",
                "Bajar caminando o trotando suave como recuperacion",
                "10 min de trote suave final"
            ),
            idealPara = "Usuarios que quieren mejorar fuerza, tecnica y resistencia muscular"
        ),
        RoutinePreset(
            id = 8,
            workoutPlan = WorkoutPlan(
                id = "fondo-largo",
                name = "Fondo Largo",
                description = "Aumentar resistencia aerobica.",
                steps = listOf(
                    timeStep("easy-start", "Trote muy suave", StepType.WARMUP, 600.0, 480, 50),
                    timeStep("long-run", "Carrera continua comoda", StepType.EASY, 2400.0, 420, 42),
                    timeStep("cooldown", "Vuelta a la calma", StepType.COOLDOWN, 600.0, 540, 60)
                )
            ),
            nivel = "Intermedio",
            duracion = "60 min",
            objetivo = "Aumentar resistencia aerobica.",
            detalle = listOf(
                "10 min de trote muy suave",
                "40 min de carrera continua a ritmo comodo",
                "10 min de vuelta a la calma"
            ),
            idealPara = "Usuarios que preparan carreras de 10K o 21K"
        ),
        RoutinePreset(
            id = 9,
            workoutPlan = WorkoutPlan(
                id = "recuperacion-activa",
                name = "Recuperacion Activa",
                description = "Mover el cuerpo sin generar fatiga.",
                steps = listOf(
                    timeStep("walk-start", "Caminata suave", StepType.WARMUP, 300.0, 720, 60),
                    timeStep("light-run", "Trote liviano", StepType.EASY, 600.0, 540, 60),
                    timeStep("mobility", "Movilidad suave", StepType.COOLDOWN, 300.0, null, 60)
                )
            ),
            nivel = "Suave",
            duracion = "20 min",
            objetivo = "Mover el cuerpo sin generar fatiga.",
            detalle = listOf(
                "5 min de caminata suave",
                "10 min de trote muy liviano o caminata rapida",
                "5 min de movilidad o elongacion suave"
            ),
            idealPara = "Dia posterior a un entrenamiento fuerte"
        ),
        RoutinePreset(
            id = 10,
            workoutPlan = WorkoutPlan(
                id = "preparacion-21k",
                name = "Preparacion 21K",
                description = "Preparar al usuario para media maraton.",
                steps = listOf(
                    timeStep("warmup", "Trote suave", StepType.WARMUP, 600.0, 450, 45),
                    timeStep("controlled", "Carrera continua controlada", StepType.EASY, 1800.0, 410, 40),
                    timeStep("moderate", "Ritmo moderado", StepType.TEMPO, 300.0, 390, 35),
                    timeStep("cooldown", "Trote suave final", StepType.COOLDOWN, 300.0, 480, 50)
                )
            ),
            nivel = "Avanzado",
            duracion = "50 min",
            objetivo = "Preparar al usuario para media maraton.",
            detalle = listOf(
                "10 min de trote suave",
                "30 min de carrera continua a ritmo controlado",
                "5 min de ritmo moderado",
                "5 min de trote suave final"
            ),
            idealPara = "Corredores que ya dominan los 10K y quieren avanzar hacia 21K"
        )
    )

    val all: List<WorkoutPlan> = presets.map { it.workoutPlan } + simulacionRitmo7Min + intervalos5x1000

    fun findById(id: String?): WorkoutPlan {
        return all.firstOrNull { it.id == id } ?: simulacionRitmo7Min
    }

    private fun repeatWalkRun(
        prefix: String,
        repetitions: Int,
        runSeconds: Double,
        walkSeconds: Double
    ): List<WorkoutStep> {
        return buildList {
            repeat(repetitions) { index ->
                val number = index + 1
                add(timeStep("$prefix-run-$number", "Trote suave $number", StepType.EASY, runSeconds, 480, 50))
                add(timeStep("$prefix-walk-$number", "Caminata $number", StepType.RECOVERY, walkSeconds, 720, 60))
            }
        }
    }

    private fun timeStep(
        id: String,
        name: String,
        type: StepType,
        seconds: Double,
        paceSecondsPerKm: Int? = null,
        toleranceSeconds: Int = 15
    ): WorkoutStep {
        return WorkoutStep(
            id = id,
            name = name,
            type = type,
            targetType = TargetType.TIME_SECONDS,
            targetValue = seconds,
            targetPaceSecondsPerKm = paceSecondsPerKm,
            paceToleranceSeconds = toleranceSeconds
        )
    }

    private fun distanceStep(
        id: String,
        name: String,
        type: StepType,
        meters: Double,
        paceSecondsPerKm: Int?,
        toleranceSeconds: Int = 15
    ): WorkoutStep {
        return WorkoutStep(
            id = id,
            name = name,
            type = type,
            targetType = TargetType.DISTANCE_METERS,
            targetValue = meters,
            targetPaceSecondsPerKm = paceSecondsPerKm,
            paceToleranceSeconds = toleranceSeconds
        )
    }
}
