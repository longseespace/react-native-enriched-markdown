package com.richtext.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.richtext.renderer.BlockStyle
import com.richtext.renderer.SpanStyleCache

class InlineCodeSpan(
  private val styleCache: SpanStyleCache,
  private val blockStyle: BlockStyle,
) : MetricAffectingSpan() {
  override fun updateDrawState(tp: TextPaint) {
    applyMonospacedFont(tp)
    tp.color = styleCache.codeColor
  }

  override fun updateMeasureState(tp: TextPaint) {
    applyMonospacedFont(tp)
  }

  private fun applyMonospacedFont(paint: TextPaint) {
    paint.textSize = blockStyle.fontSize * 0.85f
    val preservedStyle = (paint.typeface?.style ?: 0) and (Typeface.BOLD or Typeface.ITALIC)
    paint.typeface = SpanStyleCache.getMonospaceTypeface(preservedStyle)
  }
}
