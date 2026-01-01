package com.richtext.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.TextPaint
import com.richtext.renderer.BlockStyle
import com.richtext.styles.ListStyle
import com.richtext.styles.StyleConfig
import com.richtext.utils.applyBlockStyleFont

/**
 * Span for rendering unordered lists with bullet points and indentation.
 */
class UnorderedListSpan(
  private val style: ListStyle,
  depth: Int,
  context: Context? = null,
  richTextStyle: StyleConfig? = null,
) : BaseListSpan(
    depth = depth,
    context = context,
    richTextStyle = richTextStyle,
    blockStyle =
      BlockStyle(
        fontSize = style.fontSize,
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight,
        color = style.color,
      ),
    marginLeft = style.marginLeft,
    gapWidth = style.gapWidth,
  ) {
  private val markerWidth: Float = style.bulletSize

  override fun drawMarker(
    c: Canvas,
    p: Paint,
    x: Int,
    dir: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    layout: Layout?,
    start: Int,
  ) {
    p.style = Paint.Style.FILL
    p.color = style.bulletColor

    val depthOffset = depth * marginLeft
    val radius = markerWidth / 2f
    val bulletRightEdge = x + (depthOffset + marginLeft) * dir
    val bulletX = bulletRightEdge - radius * dir

    val textPaint =
      if (context != null) {
        TextPaint().apply {
          textSize = blockStyle.fontSize
          applyBlockStyleFont(blockStyle, context)
        }
      } else {
        TextPaint(p)
      }
    val fontMetrics = android.graphics.Paint.FontMetrics()
    textPaint.getFontMetrics(fontMetrics)
    val bulletY = baseline + (fontMetrics.ascent + fontMetrics.descent) / 2f

    c.drawCircle(bulletX, bulletY, radius, p)
  }
}
