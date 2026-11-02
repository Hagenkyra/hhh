package cl.wazepanel

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

/** Pantalla de configuración: permisos, destinos y ventanas flotantes. */
class MainActivity : Activity() {

    private companion object {
        const val MAX_DESTS = 6
    }

    private val nameFields = mutableListOf<EditText>()
    private val targetFields = mutableListOf<EditText>()
    private lateinit var status: TextView
    private lateinit var preview: PreviewView
    private lateinit var app1Button: Button
    private lateinit var app2Button: Button
    private lateinit var lay: WinLayout

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lay = Store.layout(this)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }

        col.addView(TextView(this).apply {
            text = "Panel Waze"
            textSize = 26f
        })

        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, 8.dp(), 0, 8.dp())
        }
        col.addView(status)

        col.addView(Button(this).apply {
            text = "1. Conceder permiso de superposición"
            setOnClickListener { askOverlayPermission() }
        })

        col.addView(Button(this).apply {
            text = "Activar accesibilidad (para el botón Dividir)"
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@MainActivity, "Esta radio no tiene ajustes de accesibilidad", Toast.LENGTH_LONG).show()
                }
            }
        })

        addDestinations(col)

        col.addView(Button(this).apply {
            text = "2. Guardar y mostrar panel"
            setOnClickListener { saveAndShow() }
        })

        col.addView(Button(this).apply {
            text = "Ocultar panel"
            setOnClickListener {
                Store.setActive(this@MainActivity, false)
                stopService(Intent(this@MainActivity, OverlayService::class.java))
            }
        })

        addWindowSection(col)

        setContentView(ScrollView(this).apply { addView(col) })
    }

    private fun addDestinations(col: LinearLayout) {
        col.addView(TextView(this).apply {
            text = "Destinos. En «Destino» escribe: fav:Casa (favorito de Waze), " +
                "lat,lng (por ejemplo -33.4489,-70.6693) o una dirección o lugar."
            textSize = 14f
            setPadding(0, 12.dp(), 0, 8.dp())
        })

        val saved = Store.load(this)
        for (i in 0 until MAX_DESTS) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val name = EditText(this).apply {
                hint = "Nombre"
                setSingleLine()
                setText(saved.getOrNull(i)?.name ?: "")
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val target = EditText(this).apply {
                hint = "Destino"
                setSingleLine()
                setText(saved.getOrNull(i)?.target ?: "")
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            }
            nameFields.add(name)
            targetFields.add(target)
            row.addView(name)
            row.addView(target)
            col.addView(row)
        }
    }

    private fun addWindowSection(col: LinearLayout) {
        col.addView(TextView(this).apply {
            text = "Ventanas flotantes"
            textSize = 22f
            setPadding(0, 24.dp(), 0, 4.dp())
        })
        col.addView(TextView(this).apply {
            text = "Requiere activar el modo freeform por adb (ver README). " +
                "Elige las dos apps, la zona de pantalla donde caben y la proporción de la primera."
            textSize = 14f
        })

        app1Button = Button(this).apply {
            setOnClickListener {
                pickApp { pkg ->
                    lay = lay.copy(pkg1 = pkg)
                    commit()
                }
            }
        }
        app2Button = Button(this).apply {
            setOnClickListener {
                pickApp { pkg ->
                    lay = lay.copy(pkg2 = pkg)
                    commit()
                }
            }
        }
        col.addView(app1Button)
        col.addView(app2Button)

        preview = PreviewView(this)
        preview.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 8.dp(), 0, 8.dp()) }
        col.addView(preview)

        addSeek(col, "Primera app (porcentaje de la zona)", lay.split) { lay = lay.copy(split = it); commit() }
        col.addView(CheckBox(this).apply {
            text = "Apilar una sobre otra (en vez de lado a lado)"
            isChecked = lay.vertical
            setOnCheckedChangeListener { _, checked ->
                lay = lay.copy(vertical = checked)
                commit()
            }
        })

        addSeek(col, "Zona: borde izquierdo", lay.left) { lay = lay.copy(left = it); commit() }
        addSeek(col, "Zona: borde superior", lay.top) { lay = lay.copy(top = it); commit() }
        addSeek(col, "Zona: borde derecho", lay.right) { lay = lay.copy(right = it); commit() }
        addSeek(col, "Zona: borde inferior", lay.bottom) { lay = lay.copy(bottom = it); commit() }

        col.addView(Button(this).apply {
            text = "Probar ventanas ahora"
            setOnClickListener { WindowLauncher.launch(this@MainActivity) }
        })

        commit()
    }

    /** Guarda la disposición actual y refresca los botones y el dibujo. */
    private fun commit() {
        Store.saveLayout(this, lay)
        app1Button.text = "Primera app: ${appLabel(lay.pkg1)}"
        app2Button.text = "Segunda app: ${appLabel(lay.pkg2)}"
        preview.name1 = appLabel(lay.pkg1)
        preview.name2 = appLabel(lay.pkg2)
        preview.layoutInfo = lay
    }

    private fun addSeek(col: LinearLayout, title: String, value: Int, onChange: (Int) -> Unit) {
        val label = TextView(this).apply {
            text = "$title: $value%"
            setPadding(0, 8.dp(), 0, 0)
        }
        val bar = SeekBar(this).apply {
            max = 100
            progress = value
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, p: Int, fromUser: Boolean) {
                    label.text = "$title: $p%"
                    if (fromUser) onChange(p)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        col.addView(label)
        col.addView(bar)
    }

    private fun appLabel(pkg: String): String =
        try {
            packageManager.getApplicationInfo(pkg, 0).loadLabel(packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "$pkg (no instalada)"
        }

    private fun pickApp(onPicked: (String) -> Unit) {
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(main, 0)
            .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
            .distinctBy { it.second }
            .sortedBy { it.first.lowercase() }
        AlertDialog.Builder(this)
            .setTitle("Elige la app")
            .setItems(apps.map { it.first }.toTypedArray()) { _, i -> onPicked(apps[i].second) }
            .show()
    }

    override fun onResume() {
        super.onResume()
        status.text = if (Settings.canDrawOverlays(this)) {
            "Permiso de superposición: concedido"
        } else {
            "Permiso de superposición: falta concederlo"
        }
    }

    private fun askOverlayPermission() {
        try {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Esta radio no tiene esa pantalla. Por adb: adb shell appops set " +
                    "$packageName SYSTEM_ALERT_WINDOW allow",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveAndShow() {
        val list = nameFields.indices.mapNotNull { i ->
            val n = nameFields[i].text.toString().trim()
            val t = targetFields[i].text.toString().trim()
            if (n.isNotEmpty() && t.isNotEmpty()) Dest(n, t) else null
        }
        Store.save(this, list)

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Primero concede el permiso de superposición", Toast.LENGTH_LONG).show()
            return
        }

        Store.setActive(this, true)
        startForegroundService(
            Intent(this, OverlayService::class.java).setAction(OverlayService.ACTION_REFRESH)
        )
        Toast.makeText(this, "Panel activado. Abre Waze.", Toast.LENGTH_SHORT).show()
    }
}
