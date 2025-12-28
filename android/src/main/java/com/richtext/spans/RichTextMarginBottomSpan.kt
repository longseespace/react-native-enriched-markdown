package com.richtext.spans

import android.graphics.Paint
import android.text.style.LineHeightSpan

/**
 * Adds bottom margin to a block element (paragraphs/headings) using LineHeightSpan.
 *
 * For spacer lines (single newline), sets the line height to exactly marginBottom.
 * For regular lines, adds marginBottom only at paragraph boundaries to preserve lineHeight.
 *
 * @param marginBottom The margin in pixels to add below the block (must be > 0)
 */
class RichTextMarginBottomSpan(
  val marginBottom: Float,
) : LineHeightSpan {
  override fun chooseHeight(
    text: CharSequence,
    start: Int,
    end: Int,
    spanstartv: Int,
    lineHeight: Int,
    fm: Paint.FontMetricsInt,
  ) {
    if (end <= start || text[end - 1] != '\n') return

    val marginPixels = marginBottom.toInt()

    if (end - start == 1 && text[start] == '\n') {
      fm.top = 0
      fm.ascent = 0
      fm.descent = marginPixels
      fm.bottom = marginPixels
      return
    }

    // Only add spacing at paragraph boundaries (newline followed by non-newline content)
    // to prevent affecting lineHeight on every line
    if (end < text.length && text[end] != '\n') {
      fm.descent += marginPixels
      fm.bottom += marginPixels
    }
  }
}
