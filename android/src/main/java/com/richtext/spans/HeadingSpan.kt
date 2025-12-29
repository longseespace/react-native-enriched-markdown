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
  private val cachedTypeface: Typeface? by lazy {
    val fontFamily = style.getHeadingFontFamily(level)
    fontFamily.takeIf { it.isNotEmpty() }?.let {
      Typeface.create(it, Typeface.NORMAL)
    }
  }

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
