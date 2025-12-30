package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.styles.ParagraphStyle
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import com.richtext.utils.applyMarginBottom
import com.richtext.utils.containsBlockImage
import com.richtext.utils.createLineHeightSpan
import com.richtext.utils.getMarginBottomForASTParagraph

class ASTParagraphRenderer(
  private val config: ASTRendererConfig,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    // When inside a block element, render content without paragraph-specific spans
    // The parent block element (blockquote, list, etc.) handles spacing and styling
    if (factory.blockStyleContext.isInsideBlockElement()) {
      renderParagraphContent(node, builder, onLinkPress, factory)
      return
    }

    renderTopLevelParagraph(node, builder, onLinkPress, factory)
  }

  // ============================================================================
  // Styling and Spacing
  // ============================================================================

  /**
   * Renders a top-level paragraph with all paragraph-specific styling:
   * - Line height (skipped for paragraphs containing block images)
   * - Margin bottom (calculated based on paragraph content)
   */
  private fun renderTopLevelParagraph(
    paragraph: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    val start = builder.length
    val paragraphStyle = config.style.getParagraphStyle()
    factory.blockStyleContext.setParagraphStyle(paragraphStyle)

    try {
      factory.renderChildren(paragraph, builder, onLinkPress)
    } finally {
      factory.blockStyleContext.clearBlockStyle()
    }

    val end = builder.length
    val contentLength = end - start
    if (contentLength > 0) {
      applyLineHeight(builder, paragraph, paragraphStyle, start, end)
      applyParagraphMarginBottom(builder, paragraph, paragraphStyle, start)
    }
  }

  /**
   * Applies line height to the paragraph, skipping it if the paragraph contains block images.
   * This prevents unwanted spacing above block images.
   */
  private fun applyLineHeight(
    builder: SpannableStringBuilder,
    paragraph: MarkdownASTNode,
    paragraphStyle: ParagraphStyle,
    start: Int,
    end: Int,
  ) {
    // Skip lineHeight for paragraphs containing block images to prevent unwanted spacing above image
    if (!paragraph.containsBlockImage()) {
      builder.setSpan(
        createLineHeightSpan(paragraphStyle.lineHeight),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }

  /**
   * Applies margin bottom spacing to the paragraph.
   * The margin value is calculated based on paragraph content (e.g., uses image margin if paragraph contains only an image).
   */
  private fun applyParagraphMarginBottom(
    builder: SpannableStringBuilder,
    paragraph: MarkdownASTNode,
    paragraphStyle: ParagraphStyle,
    start: Int,
  ) {
    val marginBottom = getMarginBottomForASTParagraph(paragraph, paragraphStyle, config.style)
    applyMarginBottom(builder, start, marginBottom)
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Renders paragraph content (children + newline) without applying paragraph-specific spans.
   * Used when paragraph is inside a block element that handles its own spacing.
   */
  private fun renderParagraphContent(
    paragraph: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    factory.renderChildren(paragraph, builder, onLinkPress)
    builder.append("\n")
  }
}
