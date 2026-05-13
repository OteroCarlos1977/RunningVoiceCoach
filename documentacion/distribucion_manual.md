# Distribucion manual

## Objetivo

Preparar versiones instalables fuera de Play Store para validar funcionalidad primero en un dispositivo propio y luego con un grupo reducido de corredores.

## Etapa 1: prueba personal

- Generar APK debug o release.
- Instalar en un dispositivo real.
- Validar navegacion principal, rutinas, editor, simulador, GPS, voz, finalizacion de actividad y pantalla de progreso.
- Registrar capturas y observaciones antes de pasar a testers.

## Etapa 2: grupo de 10 corredores

- Distribuir un APK release firmado.
- Pedir prueba en telefonos distintos y con distintos tamanos de pantalla.
- Relevar problemas de:
  - instalacion;
  - permisos;
  - GPS;
  - voz;
  - claridad de rutinas;
  - legibilidad de UI;
  - flujo de finalizar actividad.
- Convertir los comentarios en requisitos para la siguiente iteracion.

## Firma local

El repositorio no guarda claves ni contrasenas. Para generar un release firmado, crear localmente un archivo `keystore.properties` en la raiz:

```properties
storeFile=C:\\ruta\\runners-release.jks
storePassword=tu_password
keyAlias=runners
keyPassword=tu_password
```

Ese archivo esta ignorado por Git.

## Comandos

Generar APK debug:

```powershell
.\gradlew.bat assembleDebug
```

Generar APK release:

```powershell
.\gradlew.bat assembleRelease
```

APK debug:

```text
app/build/outputs/apk/debug/app-debug.apk
```

APK release:

```text
app/build/outputs/apk/release/app-release.apk
```

## Criterio para publicar al grupo

Antes de enviar a los 10 corredores:

- `assembleDebug` debe pasar.
- `testDebugUnitTest` debe pasar.
- La prueba personal debe completar al menos una rutina y una actividad simulada o real.
- Debe existir una lista corta de cambios de la version.
