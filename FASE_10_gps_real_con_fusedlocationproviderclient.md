# RunningVoiceCoach — Fase 10 — GPS real con FusedLocationProviderClient

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Reemplazar el simulador por datos reales de ubicación, distancia y ritmo.

---

## Tareas de esta fase

- Implementar LocationTracker.
- Pedir permisos de ubicación en tiempo de ejecución.
- Iniciar y detener seguimiento.
- Calcular distancia acumulada.
- Calcular velocidad actual.
- Calcular ritmo actual.
- Exponer datos mediante StateFlow.
- Ignorar puntos de baja precisión.
- Integrar GPS en ActiveRunScreen.

---

## Prompt para Codex

```text
Implementa LocationTracker usando FusedLocationProviderClient.

Debe:
- pedir permisos de ubicación en tiempo de ejecución
- iniciar seguimiento de ubicación
- detener seguimiento
- calcular distancia acumulada
- calcular velocidad actual
- calcular ritmo actual
- exponer los datos mediante StateFlow
- ignorar puntos de baja precisión

Integralo con ActiveRunScreen reemplazando el simulador cuando GPS esté activado.

Mantené una opción de modo simulador para pruebas.

No implementes todavía Foreground Service.
```

---

## Criterios de aceptación

- La app solicita permisos correctamente.
- La ubicación se actualiza en carrera activa.
- Distancia y ritmo se calculan de forma razonable.
- Los puntos de baja precisión se descartan.
- El simulador sigue disponible para pruebas.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
