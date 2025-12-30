package com.richtext.spans

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * Empty span used to force TextView redraw when added/removed.
 * This is a lightweight way to trigger a redraw without modifying the text content.
 * Used when async images finish loading to update the display.
 */
class ForceRedrawSpan : MetricAffectingSpan() {
  override fun updateMeasureState(tp: TextPaint) {
    // Do nothing - we don't want to change measurements
  }

  override fun updateDrawState(tp: TextPaint?) {
    // Do nothing - we don't want to change appearance
  }
}
