package com.richtext.parser

import android.util.Log
import org.commonmark.node.Document
import org.commonmark.parser.Parser as CommonMarkParser

class Parser {
  private val parser = CommonMarkParser.builder().build()

  fun parseMarkdown(markdown: String): Document? {
    if (markdown.isBlank()) {
      return null
    }

    try {
      val document = parser.parse(markdown) as? Document

      if (document != null) {
        return document
      } else {
        Log.w("MarkdownParser", "Failed to cast parsed result to Document")
        return null
      }
    } catch (e: Exception) {
      Log.e("MarkdownParser", "CommonMark parsing failed: ${e.message}")
      return null
    }
  }

  companion object {
    /**
     * Shared parser instance. Parser is stateless and thread-safe, so it can be reused
     * across all RichTextView instances to avoid unnecessary allocations.
     */
    val shared: Parser = Parser()
  }
}
