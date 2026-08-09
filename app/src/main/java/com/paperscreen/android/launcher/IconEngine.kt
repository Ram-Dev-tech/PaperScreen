package com.paperscreen.android.launcher

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache

class IconEngine(private val context: Context) {

    // Cache to prevent lag in the App Drawer
    private val cache = LruCache<String, Bitmap>(150)

    // Normalize icon size to 56dp
    private val iconSizePx: Int = (56 * context.resources.displayMetrics.density).toInt()

    fun getExternalAppIcon(packageName: String): Bitmap? {
        val cached = cache.get(packageName)
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

        drawable.setBounds(0, 0, iconSizePx, iconSizePx)
        drawable.draw(canvas)

        cache.put(packageName, bitmap)
        return bitmap
    }
    
    fun clearCache() {
        cache.evictAll()
    }
}
