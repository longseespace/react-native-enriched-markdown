package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.InlineCodeBackgroundSpan
import com.richtext.spans.InlineCodeSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE

class ASTCodeRenderer(
  private val config: ASTRendererConfig,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    // For code nodes, content might be in node.content or in children (Text nodes)
    val codeText =
      if (node.content.isNotEmpty()) {
        node.content
      } else {
        // Collect text from children
        node.children.joinToString("") { it.content }
      }

    if (codeText.isEmpty()) return

    factory.renderWithSpan(builder, { builder.append(codeText) }) { start, end, blockStyle ->
      builder.setSpan(
        InlineCodeSpan(config.style, blockStyle),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
      builder.setSpan(
        InlineCodeBackgroundSpan(config.style),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }
}
