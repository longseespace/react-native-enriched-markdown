package com.richtext.styles

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.PixelUtil

data class HeadingStyle(
  val fontSize: Float,
  val fontFamily: String?
)

class RichTextStyle(style: ReadableMap?) {
  private val headingStyles = mutableMapOf<Int, HeadingStyle>()

  init {
    style?.let { parseStyles(it) }
  }

  fun getHeadingFontSize(level: Int): Float {
    return headingStyles[level]?.fontSize ?: 32f
  }

  fun getHeadingFontFamily(level: Int): String? {
    return headingStyles[level]?.fontFamily
  }

  private fun parseStyles(style: ReadableMap) {
    (1..6).forEach { level ->
      val levelKey = "h$level"
      val levelStyle = style.getMap(levelKey)
      levelStyle?.let { map ->
        val fontSize = if (map.hasKey("fontSize") && !map.isNull("fontSize")) {
          PixelUtil.toPixelFromSP(map.getDouble("fontSize").toFloat())
        } else {
          32f
        }
        val fontFamily = map.getString("fontFamily")
        
        headingStyles[level] = HeadingStyle(fontSize, fontFamily)
      }
    }
  }
}

