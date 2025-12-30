package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.EmphasisSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE

class ASTEmphasisRenderer(
  private val config: ASTRendererConfig,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    factory.renderWithSpan(builder, { factory.renderChildren(node, builder, onLinkPress) }) { start, end, blockStyle ->
      builder.setSpan(
        EmphasisSpan(config.style, blockStyle),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }
}
