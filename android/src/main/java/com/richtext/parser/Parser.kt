package com.richtext.parser

import android.util.Log

class Parser {
  companion object {
    init {
      try {
        System.loadLibrary("react_codegen_RichTextViewSpec")
      } catch (e: UnsatisfiedLinkError) {
        Log.e("MarkdownParser", "Failed to load native library", e)
      }
    }

    @JvmStatic
    private external fun nativeParseMarkdown(markdown: String): MarkdownASTNode?

    /**
     * Shared parser instance. Parser is stateless and thread-safe, so it can be reused
     * across all RichTextView instances to avoid unnecessary allocations.
     */
    val shared: Parser = Parser()
  }

  fun parseMarkdown(markdown: String): MarkdownASTNode? {
    if (markdown.isBlank()) {
      return null
    }

    try {
      val ast = nativeParseMarkdown(markdown)

      if (ast != null) {
        return ast
      } else {
        Log.w("MarkdownParser", "Native parser returned null")
        return null
      }
    } catch (e: Exception) {
      Log.e("MarkdownParser", "MD4C parsing failed: ${e.message}", e)
      return null
    }
  }
}
