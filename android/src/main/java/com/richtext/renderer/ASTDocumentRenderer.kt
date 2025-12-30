package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode

class ASTDocumentRenderer(
  private val config: ASTRendererConfig? = null,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    factory.renderChildren(node, builder, onLinkPress)
  }
}
