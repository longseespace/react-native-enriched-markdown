package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.OrderedListSpan
import com.richtext.spans.UnorderedListSpan
import com.richtext.utils.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE

class ListItemRenderer(
  private val config: RendererConfig,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val start = builder.length
    val listType = factory.blockStyleContext.listType

    // Ensure block style (simplified)
    if (factory.blockStyleContext.getBlockStyle() == null) {
      when (listType) {
        BlockStyleContext.ListType.UNORDERED -> {
          factory.blockStyleContext.setUnorderedListStyle(config.style.getUnorderedListStyle())
        }

        BlockStyleContext.ListType.ORDERED -> {
          factory.blockStyleContext.setOrderedListStyle(config.style.getOrderedListStyle())
        }

        null -> {
          // Fallback: parent list renderer should have set block style
          factory.blockStyleContext.setParagraphStyle(config.style.getParagraphStyle())
        }
      }
    }

    // Increment counter for ordered lists
    if (listType == BlockStyleContext.ListType.ORDERED) {
      factory.blockStyleContext.incrementListItemNumber()
    }

    factory.renderChildren(node, builder, onLinkPress)

    val end = builder.length
    if (end == start || builder.substring(start, end).isBlank()) return

    // Prepare content: remove trailing newlines and add one
    var contentEnd = builder.length
    while (contentEnd > start && builder[contentEnd - 1] == '\n') {
      contentEnd--
    }
    if (contentEnd < builder.length) {
      builder.delete(contentEnd, builder.length)
    }
    builder.append("\n")

    // Apply list span
    val depth = factory.blockStyleContext.listDepth - 1
    val span =
      when (listType) {
        BlockStyleContext.ListType.UNORDERED -> {
          UnorderedListSpan(
            config.style.getUnorderedListStyle(),
            depth,
            factory.context,
            config.style,
          )
        }

        BlockStyleContext.ListType.ORDERED -> {
          OrderedListSpan(
            config.style.getOrderedListStyle(),
            depth,
            factory.context,
            config.style,
          ).apply {
            setItemNumber(factory.blockStyleContext.listItemNumber)
          }
        }

        null -> {
          return
        }
      }
    builder.setSpan(span, start, builder.length, SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE)
  }
}
