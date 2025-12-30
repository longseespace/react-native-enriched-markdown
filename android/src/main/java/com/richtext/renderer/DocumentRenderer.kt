package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode

class DocumentRenderer(
  private val config: RendererConfig? = null,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    factory.renderChildren(node, builder, onLinkPress)
  }
}
