package com.richtext.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import com.richtext.styles.StyleConfig
import com.richtext.utils.applyTypefacePreserving

class HeadingSpan(
  private val level: Int,
  private val style: StyleConfig,
) : AbsoluteSizeSpan(style.getHeadingFontSize(level).toInt()) {
  // Use cached typeface from StyleConfig instead of computing it lazily
  // This avoids recreating the same typeface for each span instance
  private val cachedTypeface: Typeface? = style.getHeadingTypeface(level)

  override fun updateDrawState(tp: TextPaint) {
    super.updateDrawState(tp)
    cachedTypeface?.let { headingTypeface ->
      tp.applyTypefacePreserving(headingTypeface, Typeface.BOLD, Typeface.ITALIC)
    }
  }

  override fun updateMeasureState(tp: TextPaint) {
    super.updateMeasureState(tp)
    cachedTypeface?.let { headingTypeface ->
      tp.applyTypefacePreserving(headingTypeface, Typeface.BOLD, Typeface.ITALIC)
    }
  }
}
