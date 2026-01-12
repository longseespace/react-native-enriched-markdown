package com.richtext.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.richtext.styles.StyleConfig

class HeadingSpan(
  val level: Int,
  style: StyleConfig,
) : MetricAffectingSpan() {
  private val fontSize: Float = style.getHeadingFontSize(level)
  private val color: Int = style.getHeadingStyle(level).color
  private val cachedTypeface: Typeface? = style.getHeadingTypeface(level)

  override fun updateDrawState(tp: TextPaint) {
    applyHeadingStyle(tp)
    tp.color = color
  }

  override fun updateMeasureState(tp: TextPaint) {
    applyHeadingStyle(tp)
  }

  private fun applyHeadingStyle(tp: TextPaint) {
    tp.textSize = fontSize
    cachedTypeface?.let { base ->
      val preserved = (tp.typeface?.style ?: 0) and (Typeface.BOLD or Typeface.ITALIC)
      tp.typeface = if (preserved != 0) Typeface.create(base, preserved) else base
    }
  }
}
