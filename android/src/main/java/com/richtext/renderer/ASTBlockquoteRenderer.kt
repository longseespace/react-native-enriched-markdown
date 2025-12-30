package com.richtext.renderer

import android.text.SpannableStringBuilder
import android.text.style.LineHeightSpan
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.BlockquoteSpan
import com.richtext.spans.MarginBottomSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import com.richtext.utils.createLineHeightSpan

class ASTBlockquoteRenderer(
  private val config: ASTRendererConfig,
) : ASTNodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: ASTRendererFactory,
  ) {
    val start = builder.length
    val blockquoteStyle = config.style.getBlockquoteStyle()
    val currentDepth = factory.blockStyleContext.blockquoteDepth

    // Update context for nested rendering
    factory.blockStyleContext.blockquoteDepth = currentDepth + 1
    factory.blockStyleContext.setBlockquoteStyle(blockquoteStyle)

    try {
      factory.renderChildren(node, builder, onLinkPress)
    } finally {
      factory.blockStyleContext.clearBlockStyle()
      factory.blockStyleContext.blockquoteDepth = currentDepth
    }

    val end = builder.length
    val contentLength = end - start
    if (contentLength == 0) return

    applyStylingAndSpacing(builder, start, end, currentDepth, blockquoteStyle, factory)
  }

  // ============================================================================
  // Styling and Spacing
  // ============================================================================

  /**
   * Applies all styling and spacing for the blockquote:
   * - Blockquote span (for borders and text styling)
   * - Line height (excluding nested blockquotes)
   * - Nested margin bottom (if applicable)
   * - Top-level margin bottom (if applicable)
   */
  private fun applyStylingAndSpacing(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
    currentDepth: Int,
    blockquoteStyle: com.richtext.styles.BlockquoteStyle,
    factory: ASTRendererFactory,
  ) {
    val nestedRanges = collectNestedBlockquotes(builder, start, end, currentDepth)

    // Apply blockquote span to entire range (includes nested blockquotes for border rendering)
    builder.setSpan(
      BlockquoteSpan(blockquoteStyle, currentDepth, factory.context, config.style),
      start,
      end,
      SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
    )

    // Apply lineHeight to parent content, excluding nested blockquote ranges
    applySpansExcludingNested(
      builder,
      nestedRanges,
      start,
      end,
      createLineHeightSpan(blockquoteStyle.lineHeight),
    )

    // Apply nestedMarginBottom spacing if there are nested blockquotes
    if (blockquoteStyle.nestedMarginBottom > 0 && nestedRanges.isNotEmpty()) {
      val contentEnd = getContentEndExcludingLastNewline(builder, start, end)
      if (contentEnd > start) {
        applySpansExcludingNested(
          builder,
          nestedRanges,
          start,
          contentEnd,
          MarginBottomSpan(blockquoteStyle.nestedMarginBottom),
        )
      }
    }

    // Apply marginBottom for top-level blockquotes only
    if (currentDepth == 0 && blockquoteStyle.marginBottom > 0) {
      val spacerLocation = builder.length
      builder.append("\n")
      builder.setSpan(
        MarginBottomSpan(blockquoteStyle.marginBottom),
        spacerLocation,
        builder.length,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }

  // ============================================================================
  // Nested Blockquote Handling
  // ============================================================================

  /**
   * Collects ranges of nested blockquotes within the current blockquote.
   * Nested blockquotes are identified by having depth = currentDepth + 1
   * and being fully contained within the current range.
   */
  private fun collectNestedBlockquotes(
    builder: SpannableStringBuilder,
    rangeStart: Int,
    rangeEnd: Int,
    currentDepth: Int,
  ): List<Pair<Int, Int>> =
    builder
      .getSpans(rangeStart, rangeEnd, BlockquoteSpan::class.java)
      .filter { span ->
        val spanStart = builder.getSpanStart(span)
        val spanEnd = builder.getSpanEnd(span)
        span.depth == currentDepth + 1 &&
          spanStart >= rangeStart &&
          spanEnd <= rangeEnd &&
          spanStart > rangeStart
      }.map { span -> Pair(builder.getSpanStart(span), builder.getSpanEnd(span)) }

  /**
   * Applies a span to ranges within [start, end), excluding nested blockquote ranges.
   * This prevents conflicts between parent and nested blockquote spans.
   *
   * For example, if we have:
   *   [Parent start] ... [Nested start] ... [Nested end] ... [Parent end]
   * The span will be applied to:
   *   [Parent start] ... [Nested start] and [Nested end] ... [Parent end]
   */
  private fun applySpansExcludingNested(
    builder: SpannableStringBuilder,
    nestedRanges: List<Pair<Int, Int>>,
    start: Int,
    end: Int,
    span: LineHeightSpan,
  ) {
    val rangesToApply =
      if (nestedRanges.isEmpty()) {
        listOf(Pair(start, end))
      } else {
        buildList {
          var currentPos = start
          for ((nestedStart, nestedEnd) in nestedRanges.sortedBy { it.first }) {
            if (currentPos < nestedStart) {
              add(Pair(currentPos, nestedStart))
            }
            currentPos = nestedEnd
          }
          if (currentPos < end) {
            add(Pair(currentPos, end))
          }
        }
      }

    for ((rangeStart, rangeEnd) in rangesToApply) {
      builder.setSpan(span, rangeStart, rangeEnd, SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE)
    }
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Returns the end position excluding the last newline character if present.
   * Used to prevent applying spacing to trailing newlines.
   */
  private fun getContentEndExcludingLastNewline(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
  ): Int = if (end > start && builder[end - 1] == '\n') end - 1 else end
}
