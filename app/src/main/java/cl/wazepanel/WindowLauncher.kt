package cl.wazepanel

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast

/**
 * Abre dos apps en ventanas flotantes (freeform), cada una en un rectángulo exacto.
 * Requiere que el sistema tenga activado el modo freeform (ver README); si no, Android
 * ignora los rectángulos y abre las apps a pantalla completa.
 */
object WindowLauncher {

    fun screenSize(c: Context): Point {
        val p = Point()
        (c.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(p)
        return p
    }

    /** Rectángulo de la zona disponible, en píxeles. */
    fun area(c: Context, l: WinLayout): Rect {
        val s = screenSize(c)
        return Rect(s.x * l.left / 100, s.y * l.top / 100, s.x * l.right / 100, s.y * l.bottom / 100)
    }

    /** Divide la zona en dos rectángulos según el porcentaje de la primera app. */
    fun split(area: Rect, pct: Int, vertical: Boolean): Pair<Rect, Rect> =
        if (vertical) {
            val cut = area.top + area.height() * pct / 100
            Rect(area.left, area.top, area.right, cut) to Rect(area.left, cut, area.right, area.bottom)
        } else {
            val cut = area.left + area.width() * pct / 100
            Rect(area.left, area.top, cut, area.bottom) to Rect(cut, area.top, area.right, area.bottom)
        }

    fun launch(c: Context) {
        val l = Store.layout(c)
        val a = area(c, l)
        if (a.width() < 200 || a.height() < 200) {
            Toast.makeText(c, "La zona es muy pequeña: ajústala en Panel Waze", Toast.LENGTH_LONG).show()
            return
        }
        val (r1, r2) = split(a, l.split, l.vertical)
        start(c, l.pkg1, r1)
        // Pequeña pausa para que el sistema registre la primera ventana antes de la segunda.
        Handler(Looper.getMainLooper()).postDelayed({ start(c, l.pkg2, r2) }, 700)
    }

    private fun start(c: Context, pkg: String, bounds: Rect) {
        val intent = c.packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            Toast.makeText(c, "No encuentro la app $pkg", Toast.LENGTH_LONG).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            val options = ActivityOptions.makeBasic().setLaunchBounds(bounds)
            c.startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            Toast.makeText(c, "No pude abrir $pkg en ventana", Toast.LENGTH_LONG).show()
        }
    }
}
