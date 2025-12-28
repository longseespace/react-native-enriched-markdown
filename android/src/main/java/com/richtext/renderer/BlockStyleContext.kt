package com.richtext.renderer

import com.richtext.styles.BlockquoteStyle
import com.richtext.styles.HeadingStyle
import com.richtext.styles.ParagraphStyle

enum class BlockType {
  NONE,
  PARAGRAPH,
  HEADING,
  BLOCKQUOTE,
  // TODO: Add when implementing:
  // LIST,
  // CODE_BLOCK,
}

data class BlockStyle(
  val fontSize: Float,
  val fontFamily: String,
  val fontWeight: String,
  val color: Int,
)

class BlockStyleContext {
  private var currentBlockType: BlockType = BlockType.NONE
  private var currentBlockStyle: BlockStyle? = null
  private var currentHeadingLevel: Int = 0
  var blockquoteDepth: Int = 0
  // TODO: Add listDepth and codeBlockDepth when implementing lists and code blocks
  // var listDepth: Int = 0
  // var codeBlockDepth: Int = 0

  /**
   * Returns true if we're inside a block element that should handle its own spacing
   * (e.g., blockquotes, lists, code blocks). Paragraphs inside these elements should
   * skip their own lineHeight and marginBottom spans.
   */
  fun isInsideBlockElement(): Boolean {
    return blockquoteDepth > 0
    // TODO: Add other block elements when implementing:
    // || listDepth > 0 || codeBlockDepth > 0
  }

  fun setParagraphStyle(style: ParagraphStyle) {
    currentBlockType = BlockType.PARAGRAPH
    currentHeadingLevel = 0
    currentBlockStyle =
      BlockStyle(
        fontSize = style.fontSize,
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight,
        color = style.color,
      )
  }

  fun setHeadingStyle(
    style: HeadingStyle,
    level: Int,
  ) {
    currentBlockType = BlockType.HEADING
    currentHeadingLevel = level
    currentBlockStyle =
      BlockStyle(
        fontSize = style.fontSize,
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight,
        color = style.color,
      )
  }

  fun setBlockquoteStyle(style: BlockquoteStyle) {
    currentBlockType = BlockType.BLOCKQUOTE
    currentHeadingLevel = 0
    currentBlockStyle =
      BlockStyle(
        fontSize = style.fontSize,
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight,
        color = style.color,
      )
  }

  fun getBlockStyle(): BlockStyle? = currentBlockStyle

  /**
   * Requires that a block style is set. Throws an exception if blockStyle is null.
   * This should never happen in normal rendering flow, as inline elements (text, links, etc.)
   * should always be rendered within a block context (paragraph, heading, or blockquote).
   *
   * @return The current block style, never null
   * @throws IllegalStateException if blockStyle is null
   */
  fun requireBlockStyle(): BlockStyle =
    currentBlockStyle
      ?: throw IllegalStateException(
        "BlockStyle is null. Inline renderers (Text, Link, Strong, Emphasis, Code) " +
          "must be rendered within a block context (Paragraph, Heading, or Blockquote).",
      )

  fun clearBlockStyle() {
    currentBlockType = BlockType.NONE
    currentBlockStyle = null
    currentHeadingLevel = 0
  }
}
