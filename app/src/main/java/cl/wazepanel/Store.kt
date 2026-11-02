package cl.wazepanel

import android.content.Context

data class Dest(val name: String, val target: String)

/**
 * Disposición de las dos ventanas. Todos los valores son porcentajes de la pantalla:
 * el área (left, top, right, bottom) es la zona donde caben las dos apps, y `split` es el
 * porcentaje de esa zona que ocupa la primera app.
 */
data class WinLayout(
    val pkg1: String,
    val pkg2: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val split: Int,
    val vertical: Boolean
)

/** Guarda destinos, posición del panel y si el panel debe arrancar con la radio. */
object Store {
    private const val PREFS = "waze_panel"
    private const val KEY_DESTS = "dests"
    private const val KEY_X = "x"
    private const val KEY_Y = "y"
    private const val KEY_ACTIVE = "active"

    val defaults = listOf(
        Dest("Casa", "fav:Casa"),
        Dest("Trabajo", "fav:Trabajo")
    )

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun clean(s: String) = s.replace('\t', ' ').replace('\n', ' ').trim()

    fun load(c: Context): List<Dest> {
        val raw = prefs(c).getString(KEY_DESTS, null) ?: return defaults
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                Dest(parts[0], parts[1])
            } else null
        }
    }

    fun save(c: Context, list: List<Dest>) {
        val raw = list.joinToString("\n") { "${clean(it.name)}\t${clean(it.target)}" }
        prefs(c).edit().putString(KEY_DESTS, raw).apply()
    }

    /** Posición guardada del panel, o null si el usuario aún no lo ha movido. */
    fun position(c: Context): Pair<Int, Int>? {
        val p = prefs(c)
        return if (p.contains(KEY_X) && p.contains(KEY_Y)) p.getInt(KEY_X, 0) to p.getInt(KEY_Y, 0) else null
    }

    fun savePosition(c: Context, x: Int, y: Int) {
        prefs(c).edit().putInt(KEY_X, x).putInt(KEY_Y, y).apply()
    }

    fun isActive(c: Context) = prefs(c).getBoolean(KEY_ACTIVE, false)

    fun setActive(c: Context, active: Boolean) {
        prefs(c).edit().putBoolean(KEY_ACTIVE, active).apply()
    }

    /** YouTube ReVanced si está instalado; si no, el YouTube normal. */
    private fun defaultVideoApp(c: Context): String {
        val revanced = "app.revanced.android.youtube"
        return try {
            c.packageManager.getPackageInfo(revanced, 0)
            revanced
        } catch (e: Exception) {
            "com.google.android.youtube"
        }
    }

    fun layout(c: Context): WinLayout {
        val p = prefs(c)
        return WinLayout(
            pkg1 = p.getString("pkg1", "com.waze") ?: "com.waze",
            pkg2 = p.getString("pkg2", null) ?: defaultVideoApp(c),
            left = p.getInt("aL", 33),
            top = p.getInt("aT", 10),
            right = p.getInt("aR", 98),
            bottom = p.getInt("aB", 85),
            split = p.getInt("split", 60),
            vertical = p.getBoolean("vert", false)
        )
    }

    fun saveLayout(c: Context, l: WinLayout) {
        prefs(c).edit()
            .putString("pkg1", l.pkg1)
            .putString("pkg2", l.pkg2)
            .putInt("aL", l.left)
            .putInt("aT", l.top)
            .putInt("aR", l.right)
            .putInt("aB", l.bottom)
            .putInt("split", l.split)
            .putBoolean("vert", l.vertical)
            .apply()
    }
}
