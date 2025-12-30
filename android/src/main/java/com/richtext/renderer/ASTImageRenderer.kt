package com.richtext.renderer

import android.content.Context
import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.ImageSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import com.richtext.utils.isInlineImage

class ASTImageRenderer(
  private val config: ASTRendererConfig,
  private val context: Context,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    val imageUrl = node.getAttribute("url") ?: return

    val isInline = builder.isInlineImage()
    val start = builder.length

    // Append object replacement character (U+FFFC) - Android requires text to attach spans to.
    // ImageSpan will replace this placeholder with the actual image during rendering.
    builder.append("\uFFFC")

    val end = builder.length
    val contentLength = end - start

    builder.setSpan(
      ImageSpan(context, imageUrl, config.style, isInline),
      start,
      end,
      SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
    )
    // Note: marginBottom for images is handled by ParagraphRenderer when the paragraph contains only an image
    // This ensures consistent spacing behavior and prevents paragraph's marginBottom from affecting images
  }
}
