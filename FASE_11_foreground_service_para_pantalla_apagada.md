# RunningVoiceCoach — Fase 11 — Foreground Service para pantalla apagada

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Mantener activa la carrera con pantalla apagada mediante servicio en primer plano.

---

## Tareas de esta fase

- Crear RunForegroundService.
- Declarar permisos en AndroidManifest.
- Usar foregroundServiceType location.
- Mostrar notificación persistente.
- Actualizar datos de carrera.
- Permitir finalizar desde la notificación.
- Manejar diferencias entre versiones recientes de Android.

---

## Prompt para Codex

```text
Agrega RunForegroundService.

Debe:
- mantener activa la carrera con pantalla apagada
- mostrar notificación persistente
- actualizar tiempo, distancia y ritmo
- permitir finalizar carrera desde la notificación
- declarar permisos necesarios en AndroidManifest
- usar foregroundServiceType="location"

Asegurate de manejar diferencias entre versiones recientes de Android.

La app debe seguir calculando distancia, ritmo, bloque actual y alertas aunque la pantalla esté apagada.
```

---

## Criterios de aceptación

- La carrera continúa con pantalla apagada.
- La notificación persistente aparece durante la actividad.
- Se puede finalizar desde la notificación.
- El GPS sigue activo dentro de las reglas de Android.
- La voz sigue funcionando si corresponde.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
