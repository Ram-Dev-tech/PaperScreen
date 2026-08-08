package com.paperscreen.android.icon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.LruCache

class IconEngine(private val context: Context) {

    // Cache to prevent lag in the App Drawer
    private val cache = LruCache<String, Bitmap>(150)

    // Normalize icon size to 56dp
    private val iconSizePx: Int = (56 * context.resources.displayMetrics.density).toInt()

    fun getPaperIcon(packageName: String, isDarkTheme: Boolean = false): Bitmap? {
        val cacheKey = "${packageName}_$isDarkTheme"
        val cached = cache.get(cacheKey)
        if (cached != null) {
            return cached
        }

        val pm = context.packageManager
        val drawable: Drawable = try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }

        val bitmap = Bitmap.createBitmap(iconSizePx, iconSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = if (isDarkTheme) Color.parseColor("#444444") else Color.parseColor("#D8D6CF")
        val fgColor = if (isDarkTheme) Color.parseColor("#D8D6CF") else Color.parseColor("#444444")

        val rDiff = (Color.red(bgColor) - Color.red(fgColor)) / 255f
        val gDiff = (Color.green(bgColor) - Color.green(fgColor)) / 255f
        val bDiff = (Color.blue(bgColor) - Color.blue(fgColor)) / 255f

        // Map intensity of original icon to the range between fgColor and bgColor
        val matrix = floatArrayOf(
            0.2126f * rDiff, 0.7152f * rDiff, 0.0722f * rDiff, 0f, Color.red(fgColor).toFloat(),
            0.2126f * gDiff, 0.7152f * gDiff, 0.0722f * gDiff, 0f, Color.green(fgColor).toFloat(),
            0.2126f * bDiff, 0.7152f * bDiff, 0.0722f * bDiff, 0f, Color.blue(fgColor).toFloat(),
            0f, 0f, 0f, 1f, 0f
        )

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(matrix))
        }

        drawable.setBounds(0, 0, iconSizePx, iconSizePx)

        // Draw the drawable to the canvas with the ColorMatrix applied
        canvas.saveLayer(0f, 0f, iconSizePx.toFloat(), iconSizePx.toFloat(), paint)
        drawable.draw(canvas)
        canvas.restore()

        cache.put(cacheKey, bitmap)
        return bitmap
    }
    
    fun clearCache() {
        cache.evictAll()
    }
}
