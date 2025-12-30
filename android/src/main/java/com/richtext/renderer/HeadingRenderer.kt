package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.HeadingSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import com.richtext.utils.applyMarginBottom
import com.richtext.utils.createLineHeightSpan

class HeadingRenderer(
  private val config: RendererConfig,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val level = node.getAttribute("level")?.toIntOrNull() ?: 1
    val start = builder.length

    val headingStyle = config.style.getHeadingStyle(level)
    factory.blockStyleContext.setHeadingStyle(headingStyle, level)

    try {
      factory.renderChildren(node, builder, onLinkPress)
    } finally {
      factory.blockStyleContext.clearBlockStyle()
    }

    val end = builder.length
    val contentLength = end - start
    if (contentLength > 0) {
      builder.setSpan(
        HeadingSpan(
          level,
          config.style,
        ),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )

      builder.setSpan(
        createLineHeightSpan(headingStyle.lineHeight),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )

      applyMarginBottom(builder, start, headingStyle.marginBottom)
    }
  }
}
