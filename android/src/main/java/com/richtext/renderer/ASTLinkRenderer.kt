package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.LinkSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE

class ASTLinkRenderer(
  private val config: ASTRendererConfig,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    val url = node.getAttribute("url") ?: return

    factory.renderWithSpan(builder, { factory.renderChildren(node, builder, onLinkPress) }) { start, end, blockStyle ->
      builder.setSpan(
        LinkSpan(url, onLinkPress, config.style, blockStyle, factory.context),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }
}
