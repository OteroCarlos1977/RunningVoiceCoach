# RunningVoiceCoach — Fase 05 — AlertEngine

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Crear el motor que decide cuándo corresponde emitir una alerta durante la carrera.

---

## Tareas de esta fase

- Crear AlertEngine.
- Crear AlertEvent, AlertType y AlertPriority.
- Generar alertas de inicio/fin de bloque.
- Generar alertas por ritmo rápido, lento o correcto.
- Generar alertas por proximidad al final del bloque.
- Generar alerta por kilómetro completado.
- Aplicar regla de no repetir alertas constantemente.

---

## Prompt para Codex

```text
Implementa AlertEngine.

Debe generar alertas para:
- inicio de bloque
- fin de bloque
- ritmo demasiado rápido
- ritmo demasiado lento
- ritmo correcto
- faltan 200 metros
- faltan 60 segundos
- kilómetro completado
- entrenamiento finalizado

Reglas:
- No repetir alertas de ritmo antes de 30 segundos.
- Las alertas de cambio de bloque tienen prioridad alta.
- Las alertas de ritmo tienen prioridad normal.
- Las alertas informativas tienen prioridad baja.
- La alerta de entrenamiento finalizado tiene prioridad alta.

Crear:
- AlertEvent
- AlertType
- AlertPriority
- AlertEngine

Incluye pruebas unitarias para validar que no repite alertas antes del intervalo mínimo.
```

---

## Criterios de aceptación

- AlertEngine compila y está en capa de dominio.
- No repite alertas de ritmo antes de 30 segundos.
- Prioriza correctamente cambios de bloque y finalización.
- Puede generar alertas informativas sin saturar al corredor.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
