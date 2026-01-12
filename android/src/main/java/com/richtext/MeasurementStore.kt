package com.richtext

import android.content.Context
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.views.text.ReactTypefaceUtils.applyStyles
import com.facebook.react.views.text.ReactTypefaceUtils.parseFontWeight
import com.facebook.yoga.YogaMeasureMode
import com.facebook.yoga.YogaMeasureOutput
import com.richtext.styles.StyleConfig
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

object MeasurementStore {
  data class PaintParams(
    val typeface: Typeface,
    val fontSize: Float,
  )

  data class MeasurementParams(
    val initialized: Boolean,
    val cachedWidth: Float,
    val cachedSize: Long,
    val spannable: CharSequence?,
    val paintParams: PaintParams,
  )

  private val data = ConcurrentHashMap<Int, MeasurementParams>()

  fun store(
    id: Int,
    spannable: CharSequence?,
    paint: TextPaint,
  ): Boolean {
    val cachedWidth = data[id]?.cachedWidth ?: 0f
    val cachedSize = data[id]?.cachedSize ?: 0L
    val initialized = data[id]?.initialized ?: true

    val size = measure(cachedWidth, spannable, paint)
    val paintParams = PaintParams(paint.typeface ?: Typeface.DEFAULT, paint.textSize)

    data[id] = MeasurementParams(initialized, cachedWidth, size, spannable, paintParams)
    return cachedSize != size
  }

  fun release(id: Int) {
    data.remove(id)
  }

  private fun measure(
    maxWidth: Float,
    spannable: CharSequence?,
    paintParams: PaintParams,
  ): Long {
    val paint =
      TextPaint().apply {
        typeface = paintParams.typeface
        textSize = paintParams.fontSize
      }

    return measure(maxWidth, spannable, paint)
  }

  private fun measure(
    maxWidth: Float,
    spannable: CharSequence?,
    paint: TextPaint,
  ): Long {
    val text = spannable ?: ""
    val textLength = text.length
    val builder =
      StaticLayout.Builder
        .obtain(text, 0, textLength, paint, maxWidth.toInt().coerceAtLeast(1))
        .setIncludePad(true)
        .setLineSpacing(0f, 1f)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      builder.setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      builder.setUseLineSpacingFromFallbacks(true)
    }

    val staticLayout = builder.build()
    val heightInSP = PixelUtil.toDIPFromPixel(staticLayout.height.toFloat())
    val widthInSP = PixelUtil.toDIPFromPixel(maxWidth)
    return YogaMeasureOutput.make(widthInSP, heightInSP)
  }

  // Returns raw markdown for quick initial measurement, or "I" if no markdown
  private fun getInitialText(
    context: Context,
    props: ReadableMap?,
  ): CharSequence {
    val markdown = props?.getString("markdown")
    // If there is no markdown, assume text is one line, "I" is a good approximation of height
    if (markdown == null || markdown.isEmpty()) return "I"

    // Return raw markdown for quick initial estimation
    // The actual styled text will be used after the view renders and calls invalidateLayout()
    return markdown
  }

  private fun getInitialFontSize(
    context: Context,
    props: ReadableMap?,
  ): Float {
    val styleMap = props?.getMap("richTextStyle") ?: return ceil(PixelUtil.toPixelFromSP(16f))
    val styleConfig = StyleConfig(styleMap, context)
    return ceil(PixelUtil.toPixelFromSP(styleConfig.getParagraphStyle().fontSize))
  }

  // Called when view measurements are not available in the store.
  // Most likely first measurement, we can use raw markdown as no native state is set yet.
  private fun initialMeasure(
    context: Context,
    id: Int?,
    width: Float,
    props: ReadableMap?,
  ): Long {
    val text = getInitialText(context, props)
    val fontSize = getInitialFontSize(context, props)

    val styleMap = props?.getMap("richTextStyle")
    val fontFamily: String
    val fontWeight: Int

    if (styleMap != null) {
      val styleConfig = StyleConfig(styleMap, context)
      fontFamily = styleConfig.getParagraphStyle().fontFamily
      fontWeight = parseFontWeight(styleConfig.getParagraphStyle().fontWeight)
    } else {
      fontFamily = ""
      fontWeight = parseFontWeight(null)
    }

    val typeface = applyStyles(Typeface.DEFAULT, 0, fontWeight, fontFamily, context.assets)
    val paintParams = PaintParams(typeface, fontSize)
    val size = measure(width, text, paintParams)

    if (id != null) {
      data[id] = MeasurementParams(true, width, size, text, paintParams)
    }

    return size
  }

  fun getMeasureById(
    context: Context,
    id: Int?,
    width: Float,
    height: Float,
    heightMode: YogaMeasureMode?,
    props: ReadableMap?,
  ): Long {
    val size = getMeasureById(context, id, width, props)
    if (heightMode !== YogaMeasureMode.AT_MOST) {
      return size
    }

    val calculatedHeight = YogaMeasureOutput.getHeight(size)
    val atMostHeight = PixelUtil.toDIPFromPixel(height)
    val finalHeight = calculatedHeight.coerceAtMost(atMostHeight)
    return YogaMeasureOutput.make(YogaMeasureOutput.getWidth(size), finalHeight)
  }

  private fun getMeasureById(
    context: Context,
    id: Int?,
    width: Float,
    props: ReadableMap?,
  ): Long {
    val id = id ?: return initialMeasure(context, id, width, props)
    val value = data[id] ?: return initialMeasure(context, id, width, props)

    // First measure has to be done using initialMeasure.
    // That way it's free of any side effects and async initializations.
    if (!value.initialized) return initialMeasure(context, id, width, props)

    if (width == value.cachedWidth) {
      return value.cachedSize
    }

    val size = measure(width, value.spannable, value.paintParams)
    data[id] = MeasurementParams(true, width, size, value.spannable, value.paintParams)
    return size
  }
}
