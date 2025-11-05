package com.richtext.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.richtext.styles.RichTextStyle
import com.richtext.utils.applyColorPreserving

class RichTextEmphasisSpan(
  private val style: RichTextStyle
) : MetricAffectingSpan() {

  override fun updateDrawState(tp: TextPaint) {
    applyEmphasisStyle(tp)
    applyEmphasisColor(tp)
  }

  override fun updateMeasureState(tp: TextPaint) {
    applyEmphasisStyle(tp)
  }

  private fun applyEmphasisStyle(tp: TextPaint) {
    val currentTypeface = tp.typeface ?: Typeface.DEFAULT
    val currentStyle = currentTypeface.style
    
    // Skip if already italic
    if ((currentStyle and Typeface.ITALIC) != 0) {
      return
    }
    
    val italicTypeface = Typeface.create(currentTypeface, Typeface.ITALIC)
    tp.typeface = italicTypeface
  }

  private fun applyEmphasisColor(tp: TextPaint) {
    // Preserve link color - don't override if link span was already applied
    tp.applyColorPreserving(style.getEmphasisColor(), style.getLinkColor())
  }
}

