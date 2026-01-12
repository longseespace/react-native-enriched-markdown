package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.TextSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE

class TextRenderer : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val content = node.content
    if (content.isEmpty()) return

    val blockType = factory.blockStyleContext.currentBlockType

    // Skip TextSpan for paragraph text - it will use TextView's default style
    // Only apply TextSpan for headings, blockquotes, code blocks, lists where style differs
    if (blockType == BlockType.PARAGRAPH) {
      builder.append(content)
      return
    }

    factory.renderWithSpan(builder, { builder.append(content) }) { start, end, blockStyle ->
      builder.setSpan(
        TextSpan(blockStyle, factory.context),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }
}
