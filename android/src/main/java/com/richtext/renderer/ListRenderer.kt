package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.MarginBottomSpan
import com.richtext.spans.OrderedListSpan
import com.richtext.spans.UnorderedListSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import com.richtext.utils.createLineHeightSpan

/**
 * Unified renderer for both ordered and unordered lists.
 * Handles all list rendering logic including nesting, context management, and styling.
 */
class ListRenderer(
  private val config: RendererConfig,
  private val isOrdered: Boolean,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val start = builder.length
    val listType =
      if (isOrdered) {
        BlockStyleContext.ListType.ORDERED
      } else {
        BlockStyleContext.ListType.UNORDERED
      }

    // Get the list style and create context manager
    val listStyle: com.richtext.styles.BaseBlockStyle =
      if (isOrdered) {
        config.style.getOrderedListStyle()
      } else {
        config.style.getUnorderedListStyle()
      }

    val contextManager = ListContextManager(factory.blockStyleContext, config.style)
    val entryState = contextManager.enterList(listType, listStyle)

    // Ensure nested lists start on a new line (without spacing)
    if (entryState.previousDepth > 0 && builder.isNotEmpty() && builder.last() != '\n') {
      builder.append("\n")
    }

    try {
      factory.renderChildren(node, builder, onLinkPress)
    } finally {
      contextManager.exitList(entryState)
    }

    val end = builder.length
    if (end == start) return

    applyStylingAndSpacing(builder, start, end, entryState.previousDepth, listStyle)
  }

  private fun applyStylingAndSpacing(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
    currentDepth: Int,
    listStyle: com.richtext.styles.BaseBlockStyle,
  ) {
    builder.setSpan(
      createLineHeightSpan(listStyle.lineHeight),
      start,
      end,
      SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
    )

    if (currentDepth == 0 && listStyle.marginBottom > 0) {
      val spacerLocation = builder.length
      builder.append("\n")
      builder.setSpan(
        MarginBottomSpan(listStyle.marginBottom),
        spacerLocation,
        builder.length,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }
}
