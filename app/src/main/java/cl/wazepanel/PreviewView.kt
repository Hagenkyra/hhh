package cl.wazepanel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

/** Dibujo a escala de la pantalla con las dos ventanas, para elegir la proporción a ojo. */
class PreviewView(context: Context) : View(context) {

    var layoutInfo: WinLayout? = null
        set(value) {
            field = value
            invalidate()
        }

    var name1 = "App 1"
    var name2 = "App 2"

    private val screen = WindowLauncher.screenSize(context)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 14f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val w = MeasureSpec.getSize(widthSpec)
        setMeasuredDimension(w, w * screen.y / screen.x)
    }

    override fun onDraw(canvas: Canvas) {
        val l = layoutInfo ?: return
        val k = width.toFloat() / screen.x

        fill.color = 0xFF263238.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fill)

        val (r1, r2) = WindowLauncher.split(WindowLauncher.area(context, l), l.split, l.vertical)
        draw(canvas, r1, k, 0xFF1E88E5.toInt(), name1)
        draw(canvas, r2, k, 0xFFD32F2F.toInt(), name2)
    }

    private fun draw(canvas: Canvas, r: Rect, k: Float, color: Int, label: String) {
        fill.color = color
        canvas.drawRect(r.left * k + 1f, r.top * k + 1f, r.right * k - 1f, r.bottom * k - 1f, fill)
        canvas.drawText(label, (r.left + r.right) / 2f * k, (r.top + r.bottom) / 2f * k, text)
    }
}
