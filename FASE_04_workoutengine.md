# RunningVoiceCoach — Fase 04 — WorkoutEngine

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Implementar el motor que controla el bloque actual de la rutina y determina progreso, cambios y finalización.

---

## Tareas de esta fase

- Crear WorkoutEngine.
- Crear WorkoutEngineState.
- Controlar bloques por tiempo y por distancia.
- Calcular progreso del bloque.
- Calcular distancia o tiempo restante.
- Determinar si corresponde cambiar de bloque.
- Determinar si el entrenamiento terminó.
- Agregar pruebas unitarias.

---

## Prompt para Codex

```text
Implementa WorkoutEngine.

Debe recibir un WorkoutPlan y el estado actual de la carrera:
- distancia total
- tiempo total
- distancia dentro del bloque
- tiempo dentro del bloque
- ritmo actual

Debe devolver WorkoutEngineState con:
- bloque actual
- índice del bloque actual
- progreso del bloque
- distancia restante o tiempo restante
- estado de ritmo
- diferencia de ritmo en segundos
- si debe pasar al siguiente bloque
- si el entrenamiento terminó

Incluye pruebas unitarias para:
- bloque por tiempo no finalizado
- bloque por tiempo finalizado
- bloque por distancia no finalizado
- bloque por distancia finalizado
- entrenamiento completo
- ritmo rápido/lento/dentro del objetivo

No uses GPS real todavía. Esta fase debe ser pura lógica de dominio.
```

---

## Criterios de aceptación

- WorkoutEngine compila sin depender de Android.
- Los tests unitarios pasan.
- El motor identifica correctamente el bloque actual.
- El motor detecta final de bloque y final de entrenamiento.
- El estado de ritmo se calcula correctamente usando PaceCalculator.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
