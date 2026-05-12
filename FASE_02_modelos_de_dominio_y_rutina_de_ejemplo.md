# RunningVoiceCoach — Fase 02 — Modelos de dominio y rutina de ejemplo

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Crear los modelos que representan rutinas, bloques de entrenamiento, sesiones y resultados por bloque.

---

## Tareas de esta fase

- Crear WorkoutPlan, WorkoutStep, StepType, TargetType.
- Crear RunSession, RunStepResult, PaceStatus.
- Permitir bloques por tiempo o por distancia.
- Permitir ritmo objetivo opcional en segundos por kilómetro.
- Agregar tolerancia de ritmo por bloque.
- Agregar una rutina de ejemplo: Intervalos 5 x 1000.

---

## Prompt para Codex

```text
Agrega los modelos de datos para representar entrenamientos estructurados de running.

Crear:
- WorkoutPlan
- WorkoutStep
- StepType
- TargetType
- RunSession
- RunStepResult
- PaceStatus

Cada WorkoutStep debe permitir:
- nombre del bloque
- tipo de bloque
- objetivo por tiempo o distancia
- valor objetivo
- ritmo objetivo opcional en segundos por kilómetro
- tolerancia en segundos por kilómetro

Agrega una rutina de ejemplo llamada "Intervalos 5 x 1000".

La rutina debe incluir:
- entrada en calor
- 5 bloques de 1000 metros
- recuperaciones entre bloques
- vuelta a la calma

Asegurate de que los modelos estén en paquetes de dominio y no dependan de Android.
```

---

## Criterios de aceptación

- Los modelos compilan y están separados de la capa Android.
- La rutina de ejemplo se puede mostrar desde la UI.
- Los ritmos se expresan internamente como segundos por kilómetro.
- Los bloques pueden ser por tiempo o distancia.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
