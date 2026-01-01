package com.richtext.renderer

import com.richtext.styles.OrderedListStyle
import com.richtext.styles.StyleConfig
import com.richtext.styles.UnorderedListStyle

/**
 * Manages list context transitions (entering/exiting lists, handling nesting, etc.).
 * Centralizes the complex logic for managing list depth, item numbers, and parent context restoration.
 */
class ListContextManager(
  private val context: BlockStyleContext,
  private val styleConfig: StyleConfig,
) {
  /**
   * Data class to hold the state when entering a list, needed for proper restoration when exiting.
   */
  data class ListEntryState(
    val previousDepth: Int,
    val parentListType: BlockStyleContext.ListType?,
    val wasNestedInOrderedList: Boolean,
  )

  /**
   * Enters a list context. Handles:
   * - Saving parent list item numbers if nested in an ordered list
   * - Incrementing list depth
   * - Setting the appropriate list style
   * - Resetting item number for the new list
   *
   * @param listType The type of list being entered (ORDERED or UNORDERED)
   * @param style The style for the list being entered
   * @return State information needed for proper exit
   */
  fun enterList(
    listType: BlockStyleContext.ListType,
    style: Any,
  ): ListEntryState {
    val previousDepth = context.listDepth
    val isNested = previousDepth > 0
    val parentListType = if (isNested) context.listType else null
    val parentIsOrdered = parentListType == BlockStyleContext.ListType.ORDERED

    // Save parent list's item number to stack before resetting for nested list
    // This is needed even for unordered lists, as the parent might be ordered
    if (isNested && parentIsOrdered) {
      context.pushOrderedListItemNumber()
    }

    context.listDepth = previousDepth + 1
    when (listType) {
      BlockStyleContext.ListType.ORDERED -> {
        context.setOrderedListStyle(style as OrderedListStyle)
      }

      BlockStyleContext.ListType.UNORDERED -> {
        context.setUnorderedListStyle(style as UnorderedListStyle)
      }
    }
    context.resetListItemNumber()

    return ListEntryState(
      previousDepth = previousDepth,
      parentListType = parentListType,
      wasNestedInOrderedList = isNested && parentIsOrdered,
    )
  }

  /**
   * Exits a list context. Handles:
   * - Clearing list style (if top-level)
   * - Decrementing list depth
   * - Restoring parent list item numbers if needed
   * - Restoring parent list context
   *
   * @param entryState The state returned from enterList, containing information needed for restoration
   */
  fun exitList(entryState: ListEntryState) {
    context.clearListStyle()
    context.listDepth = entryState.previousDepth

    // Restore parent list's item number from stack if parent was an ordered list
    if (entryState.wasNestedInOrderedList) {
      context.popOrderedListItemNumber()
    }

    // Restore parent list context if we were nested
    if (entryState.previousDepth > 0) {
      restoreParentListContext(entryState.parentListType)
    }
  }

  /**
   * Restores the parent list context after exiting a nested list.
   *
   * @param parentListType The type of the parent list (null if no parent)
   */
  private fun restoreParentListContext(parentListType: BlockStyleContext.ListType?) {
    when (parentListType) {
      BlockStyleContext.ListType.UNORDERED -> {
        context.setUnorderedListStyle(styleConfig.getUnorderedListStyle())
      }

      BlockStyleContext.ListType.ORDERED -> {
        context.setOrderedListStyle(styleConfig.getOrderedListStyle())
      }

      null -> {
        // No parent list to restore
      }
    }
  }
}
