package com.swmansion.enriched.markdown.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.TextPaint
import com.swmansion.enriched.markdown.renderer.BlockStyle
import com.swmansion.enriched.markdown.renderer.SpanStyleCache
import com.swmansion.enriched.markdown.styles.ListStyle

class OrderedListSpan(
  private val listStyle: ListStyle,
  depth: Int,
  context: Context,
  styleCache: SpanStyleCache,
) : BaseListSpan(
    depth = depth,
    context = context,
    styleCache = styleCache,
    blockStyle =
      BlockStyle(
        fontSize = listStyle.fontSize,
        fontFamily = listStyle.fontFamily,
        fontWeight = listStyle.fontWeight,
        color = listStyle.color,
      ),
    marginLeft = listStyle.marginLeft,
    gapWidth = listStyle.gapWidth,
  ) {
  companion object {
    private val sharedMarkerPaint = TextPaint().apply { isAntiAlias = true }
  }

  private val markerTypeface = SpanStyleCache.getTypefaceWithWeight(listStyle.fontFamily, listStyle.markerFontWeight)

  private fun configureMarkerPaint(): TextPaint =
    sharedMarkerPaint.apply {
      textSize = listStyle.fontSize
      color = listStyle.markerColor
      typeface = markerTypeface
    }

  var itemNumber: Int = 1
    private set

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
    val markerPaint = configureMarkerPaint()
    val text = "$itemNumber."
    val textWidth = markerPaint.measureText(text)

    // Indentation calculation based on depth and margin
    val textStartX = x + ((depth + 1) * marginLeft) * dir

    // Precise marker placement relative to the text start point
    val markerX = textStartX - (textWidth + (gapWidth / 4f)) * dir

    c.drawText(text, markerX, baseline.toFloat(), markerPaint)
  }

  fun setItemNumber(number: Int) {
    itemNumber = number
  }
}
