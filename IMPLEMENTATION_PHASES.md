# RunningVoiceCoach - Fases de implementacion

Este archivo resume el orden de trabajo definido en `PLAN_CODEX_RUNNING_VOICE_COACH.md`.

## Estado actual

Fase 12 completada:

- Proyecto Android base creado.
- Modulo `app` configurado con Kotlin y Jetpack Compose.
- Navegacion Compose agregada.
- Pantallas placeholder agregadas:
  - `HomeScreen`
  - `WorkoutListScreen`
  - `WorkoutEditorScreen`
  - `ActiveRunScreen`
  - `RunSummaryScreen`
  - `SettingsScreen`
- Paquetes base creados:
  - `domain`
  - `data`
  - `location`
  - `voice`
  - `openai`
  - `ui`
- Modelos de dominio agregados para rutinas, bloques, sesiones y resultados por bloque.
- Rutina de ejemplo `Intervalos 5 x 1000` agregada y visible desde `WorkoutListScreen`.
- `PaceCalculator` agregado para calcular, formatear y comparar ritmos.
- `WorkoutEngine` agregado para calcular bloque actual, progreso, restantes, cambio de bloque y fin de entrenamiento.
- `AlertEngine` agregado para generar alertas de bloque, ritmo, proximidad de fin, kilometros y finalizacion.
- `LocalMessageProvider` agregado para generar mensajes locales breves sin OpenAI.
- `VoiceCoach` agregado con implementacion Android basada en TextToSpeech.
- Simulador de carrera sin GPS integrado en `ActiveRunScreen`.
- La pantalla de carrera muestra tiempo, distancia, ritmo actual, ritmo promedio, bloque actual, ritmo objetivo, diferencia respecto del objetivo, estado y progreso.
- La simulacion permite iniciar, pausar, reanudar y finalizar.
- La pantalla activa usa metricas grandes y estado guardable para el simulador.
- Dependencia `play-services-location` agregada.
- Permisos `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` declarados.
- `LocationTracker` agregado con `FusedLocationProviderClient`, `StateFlow`, filtro de precision y calculo de distancia/ritmo.
- `ActiveRunScreen` permite alternar entre simulador y GPS real, solicitando permisos en tiempo de ejecucion.
- `RunForegroundService` agregado con notificacion persistente para carreras con GPS.
- Permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION` y `POST_NOTIFICATIONS` declarados.
- La notificacion muestra tiempo, distancia y ritmo, y permite finalizar el servicio.
- UI actualizada con identidad visual Runners: logo, paleta azul/cian/naranja, Home renovado y listado de rutinas en tarjetas.
- Catalogo hardcodeado con 10 rutinas base: Inicio 3K, Caminata + Trote, 5K, 10K, intervalos, cuestas, fondo, recuperacion y preparacion 21K.
- Dependencia `androidx.datastore:datastore-preferences` agregada para persistencia local simple.
- `UserSettingsRepository` agregado para persistir voz activada/desactivada, OpenAI activado/desactivado, frecuencia minima entre avisos y tolerancia general de ritmo.
- `SettingsScreen` conectada a DataStore para guardar y recuperar configuracion al reiniciar la app.
- `RunHistoryRepository` agregado para persistir sesiones finalizadas.
- `RunSummaryScreen` muestra historial local de carreras finalizadas.
- `ActiveRunScreen` guarda la sesion al finalizar manualmente o al completar la rutina.
- La navegacion permite iniciar una rutina seleccionada desde el catalogo y recuperarla por `workoutPlanId`.
- Pruebas unitarias agregadas para calculo de ritmo, motor de entrenamiento, motor de alertas y mensajes locales.
- Validacion: `.\gradlew.bat testDebugUnitTest --no-daemon` pasa correctamente.
- Validacion: `.\gradlew.bat assembleDebug --no-daemon` compila correctamente.

Pendiente conocido:

- Persistencia de rutinas creadas por el usuario: el almacenamiento base esta listo, pero falta implementar el editor funcional de rutinas personalizadas.

## Proximas fases

1. Fase 13: OpenAI client.
2. Fase 14: integracion OpenAI con fallback local.
3. Fase 15: resumen final.
4. Fase 16: configuracion completa y criterios MVP.
5. Editor de rutinas personalizadas y persistencia de rutinas creadas.

## Regla de continuidad

Al acercarnos al 5% restante del limite semanal, cerrar la tarea activa en un punto consistente y dejar anotado el siguiente paso concreto.
