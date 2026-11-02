# Panel Waze

Panel flotante con atajos que se dibuja encima de Waze (Android 9, minSdk 26).

## Cómo compilar

1. Abre esta carpeta con **Android Studio** (File → Open). Él descarga Gradle y el SDK que falten.
2. Build → Build APK(s). El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.
3. Pásalo a la radio (pendrive o adb) e instálalo.

## Compilar sin Android Studio (GitHub Actions, gratis)

1. Crea una cuenta en github.com y un repositorio nuevo (rama `main`).
2. Sube todo el contenido de esta carpeta, incluida la carpeta oculta `.github`.
3. Ve a la pestaña **Actions** → **Build APK** → **Run workflow**.
4. Al terminar (unos 5 minutos), abre la ejecución y descarga **PanelWaze-apk** (viene dentro de un zip con `app-debug.apk`).

## Primer uso en la radio

1. Abre **Panel Waze**.
2. Toca **Conceder permiso de superposición** y activa el permiso.
   Si la radio no tiene esa pantalla, por adb:
   `adb shell appops set cl.wazepanel SYSTEM_ALERT_WINDOW allow`
3. Completa tus destinos y toca **Guardar y mostrar panel**.
4. Abre Waze: el panel queda encima del mapa.

## Tipos de destino

| Escribes | Qué hace |
|---|---|
| `fav:Casa` | Navega a tu favorito de Waze con ese nombre (el nombre debe ser idéntico al guardado en Waze) |
| `-33.4489,-70.6693` | Navega a esas coordenadas |
| `Costanera Center` | Busca esa dirección o lugar y navega |

## Botón «Dividir» (pantalla dividida)

1. En Panel Waze toca **Activar accesibilidad** y enciende el servicio «Panel Waze».
2. Abre una app (por ejemplo YouTube) y toca **Dividir** en el panel.
3. Si la radio soporta pantalla dividida, la app pasa a media pantalla y eliges la segunda (Waze).
   Si no la soporta, aparece el aviso «La radio no permitió dividir la pantalla»: el fabricante la desactivó
   y solo se puede forzar con adb o root.

## Ventanas flotantes (Waze + YouTube con proporción a elección)

El botón **Ventanas** del panel (y «Probar ventanas ahora» en la app) abre dos apps cada una en un
rectángulo exacto. En la app eliges las dos apps, la zona de la pantalla donde caben (por defecto,
el área derecha, sin el panel de clima ni la barra del A/C) y el porcentaje de la primera
(por ejemplo, Waze 60% y YouTube 40%). El dibujo a escala te muestra cómo quedará.

**Requisito: activar el modo freeform en la radio** (una sola vez, desde el notebook con adb):

    adb shell settings put global force_resizable_activities 1
    adb shell settings put global enable_freeform_support 1

y reiniciar la radio. Para deshacerlo, repite los comandos con `0` en vez de `1` y reinicia.

Notas:
- Si las apps ya estaban abiertas, ciérralas antes de tocar **Ventanas**: Android aplica el
  rectángulo solo cuando abre la ventana.
- Si el sistema de la radio no soporta freeform, las apps se abrirán a pantalla completa
  (no se daña nada: deshaz los ajustes y listo).
- Si YouTube en tu radio es otra app (distinto paquete), elígela en «Segunda app».

## Uso del panel

- Botón **WAZE** (arriba): tocar contrae o expande; arrastrar mueve el panel (recuerda la posición).
- Botón **Abrir Waze**: trae Waze al frente.
- Si el panel estaba activo, vuelve a aparecer solo al encender la radio.
