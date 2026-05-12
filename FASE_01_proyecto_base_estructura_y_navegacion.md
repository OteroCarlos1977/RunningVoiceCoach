# RunningVoiceCoach — Fase 01 — Proyecto base, estructura y navegación

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Crear el proyecto base Android con navegación y pantallas placeholder, sin GPS ni OpenAI.

---

## Tareas de esta fase

- Crear app Android nativa en Kotlin.
- Usar Jetpack Compose.
- Crear paquetes: domain, data, location, voice, openai, ui.
- Crear navegación básica.
- Crear pantallas placeholder: HomeScreen, WorkoutListScreen, WorkoutEditorScreen, ActiveRunScreen, RunSummaryScreen, SettingsScreen.
- No implementar GPS, TextToSpeech ni OpenAI todavía.

---

## Prompt para Codex

```text
Crea una app Android nativa en Kotlin con Jetpack Compose llamada RunningVoiceCoach.

La app debe tener navegación básica y estas pantallas placeholder:
- HomeScreen
- WorkoutListScreen
- WorkoutEditorScreen
- ActiveRunScreen
- RunSummaryScreen
- SettingsScreen

Usá una arquitectura simple por paquetes:
- domain
- data
- location
- voice
- openai
- ui

No implementes GPS ni OpenAI todavía.
Solo crea la estructura del proyecto, navegación y pantallas básicas.

Asegurate de que el proyecto compile antes de terminar.
```

---

## Criterios de aceptación

- La app compila.
- La pantalla inicial muestra accesos a Nueva carrera, Rutinas, Historial y Configuración.
- La navegación entre pantallas placeholder funciona.
- No hay dependencias innecesarias de GPS, OpenAI o TextToSpeech todavía.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
