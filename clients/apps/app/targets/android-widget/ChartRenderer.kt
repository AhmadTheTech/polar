package com.polarsource.Polar.widget

import android.graphics.*
import kotlin.math.max

object ChartRenderer {

    fun render(
        width: Int,
        height: Int,
        data: List<RevenueData>,
        lineColorHex: String = "#005FFF"
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        if (data.isEmpty()) return bitmap

        val maxValue = data.maxByOrNull { it.amount }?.amount ?: 1.0
        val yAxisMax = maxValue * 1.2
        val xScale = width.toFloat() / (data.size - 1).coerceAtLeast(1)
        val yScale = height.toFloat() / yAxisMax.toFloat()

        val linePaint = Paint().apply {
            color = Color.parseColor(lineColorHex)
            strokeWidth = 6f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        val fillPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor(lineColorHex),
                    Color.parseColor(lineColorHex).adjustAlpha(0.1f),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * xScale
            val y = height - (point.amount.toFloat() * yScale)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        val fillPath = Path(path)
        fillPath.lineTo((data.size - 1) * xScale, height.toFloat())
        fillPath.lineTo(0f, height.toFloat())
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)

        canvas.drawPath(path, linePaint)

        return bitmap
    }

    private fun Int.adjustAlpha(factor: Float): Int {
        val alpha = (Color.alpha(this) * factor).toInt()
        val red = Color.red(this)
        val green = Color.green(this)
        val blue = Color.blue(this)
        return Color.argb(alpha, red, green, blue)
    }
}
