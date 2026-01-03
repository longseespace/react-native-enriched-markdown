package com.richtext.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.richtext.renderer.BlockStyle
import com.richtext.styles.StyleConfig
import com.richtext.utils.applyColorPreserving
import com.richtext.utils.getColorsToPreserveForInlineStyle

/**
 * A span that applies italic styling and optional color emphasis.
 * Handles nested 'strong' spans by preserving bold-italic hierarchy and colors.
 */
class EmphasisSpan(
  private val style: StyleConfig,
  private val blockStyle: BlockStyle,
) : MetricAffectingSpan() {
  override fun updateDrawState(tp: TextPaint) {
    applyEmphasisStyle(tp)
    applyEmphasisColor(tp)
  }

  override fun updateMeasureState(tp: TextPaint) {
    applyEmphasisStyle(tp)
  }

  private fun applyEmphasisStyle(tp: TextPaint) {
    val old = tp.typeface ?: Typeface.DEFAULT
    // Typeface.create handles merging styles automatically.
    // If the old typeface was BOLD, this results in BOLD_ITALIC.
    val combinedStyle = old.style or Typeface.ITALIC

    if (old.style != combinedStyle) {
      tp.typeface = Typeface.create(old, combinedStyle)
    }
  }

  private fun applyEmphasisColor(tp: TextPaint) {
    val configEmphasisColor = style.getEmphasisColor()

    // We only apply emphasis color if a specific color is configured
    // and if the current color is still the base block color (not already changed by Strong/Link).
    val colorToUse =
      if (tp.color == blockStyle.color) {
        configEmphasisColor ?: blockStyle.color
      } else {
        tp.color
      }

    // Apply color while protecting specific UI colors like Links or Code backgrounds.
    tp.applyColorPreserving(colorToUse, *getColorsToPreserveForInlineStyle(style))
  }
}
