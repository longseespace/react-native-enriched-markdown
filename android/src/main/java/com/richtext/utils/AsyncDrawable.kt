package com.richtext.utils

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import java.net.URL
import java.util.concurrent.Executors

/**
 * Custom Drawable that loads images asynchronously from URLs.
 */
class AsyncDrawable(
  private val url: String,
) : Drawable() {
  internal var internalDrawable: Drawable = Color.TRANSPARENT.toDrawable()
  private val mainHandler = Handler(Looper.getMainLooper())
  private val executor = Executors.newSingleThreadExecutor()
  var isLoaded = false
  var onLoaded: (() -> Unit)? = null

  init {
    internalDrawable.bounds = bounds
    load()
  }

  private fun load() {
    executor.execute {
      try {
        isLoaded = false
        val inputStream = URL(url).openStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)

        mainHandler.post {
          if (bitmap != null) {
            val drawable = bitmap.toDrawable(Resources.getSystem())
            drawable.bounds = bounds
            internalDrawable = drawable
          }
          isLoaded = true
          onLoaded?.invoke()
        }
      } catch (e: Exception) {
        Log.e("AsyncDrawable", "Failed to load image from: $url", e)
        mainHandler.post {
          isLoaded = true
          onLoaded?.invoke()
        }
      }
    }
  }

  override fun draw(canvas: Canvas) {
    internalDrawable.draw(canvas)
  }

  override fun setAlpha(alpha: Int) {
    internalDrawable.alpha = alpha
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    internalDrawable.colorFilter = colorFilter
  }

  @Deprecated("Deprecated in Java")
  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  override fun setBounds(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    super.setBounds(left, top, right, bottom)
    internalDrawable.setBounds(left, top, right, bottom)
  }
}
