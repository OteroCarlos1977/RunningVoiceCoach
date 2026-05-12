# RunningVoiceCoach — Fase 08 — Simulador de carrera

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Probar toda la lógica de entrenamiento, alertas y voz sin GPS real.

---

## Tareas de esta fase

- Crear un simulador que emita tiempo, distancia y ritmo.
- Actualizar datos cada segundo.
- Integrar WorkoutEngine, AlertEngine, LocalMessageProvider y VoiceCoach.
- Mostrar datos simulados en ActiveRunScreen.
- Permitir iniciar, pausar, reanudar y finalizar simulación.

---

## Prompt para Codex

```text
Antes de usar GPS real, crea un simulador de carrera.

Debe generar datos simulados de:
- tiempo
- distancia
- ritmo actual
- bloque actual

Integrar:
- WorkoutEngine
- AlertEngine
- LocalMessageProvider
- VoiceCoach

La pantalla ActiveRunScreen debe mostrar datos simulados actualizados cada segundo.

Debe permitir:
- iniciar
- pausar
- reanudar
- finalizar

Esto permite probar la lógica sin salir a correr.
No agregues GPS real todavía.
```

---

## Criterios de aceptación

- La simulación actualiza datos cada segundo.
- ActiveRunScreen muestra bloque actual y ritmo.
- Las alertas se generan durante la simulación.
- La voz se reproduce cuando corresponde.
- La carrera simulada puede finalizar correctamente.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
