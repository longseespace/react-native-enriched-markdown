package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.MarginBottomSpan
import com.richtext.spans.UnorderedListSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import com.richtext.utils.createLineHeightSpan

class UnorderedListRenderer(
  private val config: RendererConfig,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val start = builder.length
    val listStyle = config.style.getUnorderedListStyle()
    val currentDepth = factory.blockStyleContext.listDepth
    val parentListType = if (currentDepth > 0) factory.blockStyleContext.listType else null
    // Save parent list's item number to stack before resetting for nested list
    // This is needed even for unordered lists, as the parent might be ordered
    val isNestedInOrderedList = currentDepth > 0 && parentListType == BlockStyleContext.ListType.ORDERED
    if (isNestedInOrderedList) {
      factory.blockStyleContext.pushOrderedListItemNumber()
    }

    factory.blockStyleContext.listDepth = currentDepth + 1
    factory.blockStyleContext.setUnorderedListStyle(listStyle)
    factory.blockStyleContext.resetListItemNumber()

    // Ensure nested lists start on a new line (without spacing)
    if (currentDepth > 0 && builder.isNotEmpty() && builder.last() != '\n') {
      builder.append("\n")
    }

    try {
      factory.renderChildren(node, builder, onLinkPress)
    } finally {
      factory.blockStyleContext.clearListStyle()
      factory.blockStyleContext.listDepth = currentDepth
      // Restore parent list's item number from stack if parent was an ordered list
      if (isNestedInOrderedList) {
        factory.blockStyleContext.popOrderedListItemNumber()
      }
      restoreParentListContext(factory, parentListType)
    }

    val end = builder.length
    if (end == start) return

    applyStylingAndSpacing(builder, start, end, currentDepth, listStyle)
  }

  private fun restoreParentListContext(
    factory: RendererFactory,
    parentListType: BlockStyleContext.ListType?,
  ) {
    if (parentListType != null) {
      when (parentListType) {
        BlockStyleContext.ListType.UNORDERED -> {
          factory.blockStyleContext.setUnorderedListStyle(config.style.getUnorderedListStyle())
        }

        BlockStyleContext.ListType.ORDERED -> {
          factory.blockStyleContext.setOrderedListStyle(config.style.getOrderedListStyle())
        }

        null -> {}
      }
    }
  }

  private fun applyStylingAndSpacing(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
    currentDepth: Int,
    listStyle: com.richtext.styles.UnorderedListStyle,
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
