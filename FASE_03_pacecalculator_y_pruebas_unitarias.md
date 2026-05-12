# RunningVoiceCoach — Fase 03 — PaceCalculator y pruebas unitarias

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Implementar los cálculos de ritmo, ritmo promedio, formato min/km y comparación contra objetivo.

---

## Tareas de esta fase

- Crear PaceCalculator.
- Calcular ritmo desde velocidad en m/s.
- Calcular ritmo promedio desde distancia y duración.
- Formatear ritmo como mm:ss /km.
- Comparar ritmo actual contra ritmo objetivo con tolerancia.
- Agregar tests unitarios.

---

## Prompt para Codex

```text
Implementa PaceCalculator.

Debe incluir:
- calculatePaceSecondsPerKm(speedMetersPerSecond: Double): Int?
- calculateAveragePaceSecondsPerKm(distanceMeters: Double, durationSeconds: Long): Int?
- formatPace(secondsPerKm: Int?): String
- comparePace(current: Int?, target: Int?, tolerance: Int): PaceStatus

Reglas:
- Si velocidad o distancia son inválidas, devolver null.
- El formato debe ser mm:ss /km.
- Si current es menor que target por más de tolerance, el corredor va más rápido.
- Si current es mayor que target por más de tolerance, el corredor va más lento.
- Si está dentro de la tolerancia, devolver WITHIN_TARGET.
- Si no hay ritmo objetivo, devolver NO_TARGET.

Incluye pruebas unitarias para:
- velocidad válida
- velocidad cero
- distancia cero
- ritmo más rápido
- ritmo más lento
- ritmo dentro de tolerancia
- formato mm:ss /km
```

---

## Criterios de aceptación

- Todos los tests pasan.
- PaceCalculator no depende de Android.
- Los casos inválidos devuelven null o NO_TARGET según corresponda.
- El formato de ritmo es consistente.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
