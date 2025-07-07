package com.example.dtaquito.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class DiagonalBillarImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val path = Path()
    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 12f
        isAntiAlias = true
    }

    private val alphaPaint = Paint().apply {
        alpha = 180 // 70% de opacidad (255 * 0.7 ≈ 180)
        isAntiAlias = true
    }

    init {
        scaleType = ScaleType.CENTER_CROP
    }

    override fun onDraw(canvas: Canvas) {
        // Crear máscara triangular
        path.reset()
        path.moveTo(0f, 0f)
        path.lineTo(width.toFloat(), 0f)
        path.lineTo(0f, height.toFloat())
        path.close()

        // Guardar el estado del canvas
        val saveCount = canvas.save()

        // Recortar con la forma triangular
        canvas.clipPath(path)

        // Aplicar opacidad a la imagen
        canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), alphaPaint)
        super.onDraw(canvas)
        canvas.restore()

        // Restaurar el canvas original
        canvas.restoreToCount(saveCount)

        // Dibujar el borde
        canvas.drawPath(path, borderPaint)
    }
}