package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode

class LineBreakRenderer : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    builder.append("\n")
  }
}
