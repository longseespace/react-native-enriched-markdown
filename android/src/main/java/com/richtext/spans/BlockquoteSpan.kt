package com.richtext.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import com.richtext.renderer.BlockStyle
import com.richtext.styles.BlockquoteStyle
import com.richtext.styles.StyleConfig
import com.richtext.utils.applyBlockStyleFont
import com.richtext.utils.applyColorPreserving

/**
 * Span for rendering blockquotes with left border and indentation.
 * Each span draws borders for all nesting levels from 0 to depth (inclusive).
 */
class BlockquoteSpan(
  private val style: BlockquoteStyle,
  val depth: Int,
  private val context: Context? = null,
  private val richTextStyle: StyleConfig? = null,
) : MetricAffectingSpan(),
  LeadingMarginSpan {
  // ============================================================================
  // Properties
  // ============================================================================

  private val levelSpacing: Float = style.borderWidth + style.gapWidth
  private val blockStyle =
    BlockStyle(
      fontSize = style.fontSize,
      fontFamily = style.fontFamily,
      fontWeight = style.fontWeight,
      color = style.color,
    )

  // ============================================================================
  // MetricAffectingSpan Implementation
  // ============================================================================

  override fun updateMeasureState(tp: TextPaint) = applyTextStyle(tp)

  override fun updateDrawState(tp: TextPaint) = applyTextStyle(tp)

  // ============================================================================
  // LeadingMarginSpan Implementation
  // ============================================================================

  override fun getLeadingMargin(first: Boolean): Int = levelSpacing.toInt()

  override fun drawLeadingMargin(
    c: Canvas,
    p: Paint,
    x: Int,
    dir: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    text: CharSequence?,
    start: Int,
    end: Int,
    first: Boolean,
    layout: Layout?,
  ) {
    if (shouldSkipDrawing(text, start)) return

    val originalStyle = p.style
    val originalColor = p.color

    drawBackground(c, p, top, bottom, layout)
    drawBorders(c, p, top, bottom, x, dir, text, start, layout)

    p.style = originalStyle
    p.color = originalColor
  }

  // ============================================================================
  // Text Styling
  // ============================================================================

  /**
   * Applies blockquote text styling (font size, typeface, color) to the TextPaint.
   * Preserves existing bold/italic styles and inline element colors (strong, emphasis, links, code).
   */
  private fun applyTextStyle(tp: TextPaint) {
    if (context == null) return

    tp.textSize = blockStyle.fontSize
    preserveAndApplyTypeface(tp)
    applyColor(tp)
  }

  /**
   * Preserves bold/italic styles from strong/emphasis spans before applying blockquote font.
   * Android processes spans in order, and the blockquote span (applied to a larger range)
   * may be processed after strong/emphasis spans, overwriting their styles.
   */
  private fun preserveAndApplyTypeface(tp: TextPaint) {
    val preservedStyles = (tp.typeface?.style ?: Typeface.NORMAL) and (Typeface.BOLD or Typeface.ITALIC)
    tp.applyBlockStyleFont(blockStyle, context!!)

    if (preservedStyles != 0) {
      val blockquoteTypeface = tp.typeface ?: Typeface.DEFAULT
      val combinedStyle = blockquoteTypeface.style or preservedStyles
      tp.typeface = Typeface.create(blockquoteTypeface, combinedStyle)
    }
  }

  /**
   * Applies blockquote color, preserving colors from inline elements (strong, emphasis, links, code).
   * This ensures that inline formatting colors take precedence over the blockquote color.
   */
  private fun applyColor(tp: TextPaint) {
    if (richTextStyle != null) {
      tp.applyColorPreserving(blockStyle.color, *getColorsToPreserve().toIntArray())
    } else {
      tp.color = blockStyle.color
    }
  }

  /**
   * Collects colors from inline elements that should be preserved when applying blockquote color.
   * Returns a list of colors for strong, emphasis, links, and code elements.
   */
  private fun getColorsToPreserve(): List<Int> {
    if (richTextStyle == null) return emptyList()
    return buildList {
      richTextStyle.getStrongColor()?.takeIf { it != 0 }?.let { add(it) }
      richTextStyle.getEmphasisColor()?.takeIf { it != 0 }?.let { add(it) }
      richTextStyle.getLinkColor().takeIf { it != 0 }?.let { add(it) }
      richTextStyle
        .getCodeStyle()
        ?.color
        ?.takeIf { it != 0 }
        ?.let { add(it) }
    }
  }

  // ============================================================================
  // Border Drawing
  // ============================================================================

  /**
   * Draws the blockquote background and borders for all nesting levels.
   * Each level draws a vertical border line, creating a visual hierarchy for nested blockquotes.
   */
  private fun drawBorders(
    c: Canvas,
    p: Paint,
    top: Int,
    bottom: Int,
    x: Int,
    dir: Int,
    text: CharSequence?,
    start: Int,
    layout: Layout?,
  ) {
    p.style = Paint.Style.FILL
    p.color = style.borderColor

    val borderTop = top.toFloat()
    val borderBottom = calculateBorderBottom(bottom, text, start, layout)
    val containerLeft = layout?.getLineLeft(0) ?: 0f

    // Draw borders for all levels from 0 to depth (inclusive)
    // This creates the visual hierarchy: each nested level adds another border line
    for (level in 0..depth) {
      val borderX = containerLeft + (levelSpacing * level * dir)
      val borderRight = borderX + (style.borderWidth * dir)
      c.drawRect(borderX, borderTop, borderRight, borderBottom, p)
    }
  }

  /**
   * Draws the blockquote background color if configured.
   * The background spans the full width of the text container.
   */
  private fun drawBackground(
    c: Canvas,
    p: Paint,
    top: Int,
    bottom: Int,
    layout: Layout?,
  ) {
    val bgColor = style.backgroundColor ?: return
    if (bgColor == Color.TRANSPARENT || layout == null) return

    p.style = Paint.Style.FILL
    p.color = bgColor
    c.drawRect(0f, top.toFloat(), layout.width.toFloat(), bottom.toFloat(), p)
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Determines if this span should skip drawing because a deeper nested blockquote
   * is present at the same position. Only the deepest blockquote at each position draws borders.
   */
  private fun shouldSkipDrawing(
    text: CharSequence?,
    start: Int,
  ): Boolean {
    if (text !is Spanned) return false
    val maxDepth =
      text
        .getSpans(start, start + 1, BlockquoteSpan::class.java)
        .maxOfOrNull { it.depth } ?: -1
    return maxDepth > depth
  }

  /**
   * Calculates the bottom position for border drawing, extending to the next line
   * if it's part of the same blockquote. This creates continuous borders across line breaks.
   */
  private fun calculateBorderBottom(
    bottom: Int,
    text: CharSequence?,
    start: Int,
    layout: Layout?,
  ): Float {
    if (layout == null || text !is Spanned || start >= layout.text.length) {
      return bottom.toFloat()
    }

    val lineNumber = layout.getLineForOffset(start)
    if (lineNumber >= layout.lineCount - 1) {
      return bottom.toFloat()
    }

    if (!isNextLineSameBlockquote(text, layout, lineNumber)) {
      return bottom.toFloat()
    }

    // Extend border slightly to bridge the gap between lines
    val gap = layout.getLineTop(lineNumber + 1) - bottom
    return if (gap > 0 && gap < 1f) (bottom + gap).toFloat() else bottom.toFloat()
  }

  /**
   * Checks if the next line belongs to the same blockquote by checking if this span
   * is present at the start of the next line.
   */
  private fun isNextLineSameBlockquote(
    text: Spanned,
    layout: Layout,
    lineNumber: Int,
  ): Boolean {
    val nextLineStart = layout.getLineStart(lineNumber + 1)
    val nextLineEnd = layout.getLineEnd(lineNumber + 1)
    return text
      .getSpans(nextLineStart, nextLineEnd, BlockquoteSpan::class.java)
      .any { it == this }
  }
}
