package com.richtext.renderer

import android.text.SpannableStringBuilder
import com.richtext.parser.MarkdownASTNode
import com.richtext.spans.BaseListSpan
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

    ensureBlockStyle(listType, factory)

    if (listType == BlockStyleContext.ListType.ORDERED) {
      factory.blockStyleContext.incrementListItemNumber()
    }

    factory.renderChildren(node, builder, onLinkPress)

    val end = builder.length
    if (end == start || isWhitespaceOnly(builder, start, end)) return

    prepareListItemContent(builder, start)
    applyListSpan(builder, start, builder.length, listType, factory)
  }

  private fun ensureBlockStyle(
    listType: BlockStyleContext.ListType?,
    factory: RendererFactory,
  ) {
    if (factory.blockStyleContext.getBlockStyle() != null) return

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

  private fun prepareListItemContent(
    builder: SpannableStringBuilder,
    start: Int,
  ) {
    removeTrailingNewlines(builder, start)
    builder.append("\n")
  }

  private fun isWhitespaceOnly(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
  ): Boolean {
    for (i in start until end) {
      if (!builder[i].isWhitespace()) return false
    }
    return true
  }

  private fun removeTrailingNewlines(
    builder: SpannableStringBuilder,
    start: Int,
  ) {
    var contentEnd = builder.length
    while (contentEnd > start && builder[contentEnd - 1] == '\n') {
      contentEnd--
    }
    if (contentEnd < builder.length) {
      builder.delete(contentEnd, builder.length)
    }
  }

  private fun applyListSpan(
    builder: SpannableStringBuilder,
    start: Int,
    end: Int,
    listType: BlockStyleContext.ListType?,
    factory: RendererFactory,
  ) {
    val depth = factory.blockStyleContext.listDepth - 1
    createListSpan(listType, depth, factory)?.let { span ->
      builder.setSpan(span, start, end, SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE)
    }
  }

  private fun createListSpan(
    listType: BlockStyleContext.ListType?,
    depth: Int,
    factory: RendererFactory,
  ): BaseListSpan? =
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
        null
      }
    }
}
