package com.swmansion.enriched.markdown.renderer

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.swmansion.enriched.markdown.utils.text.span.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.CodeHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxTheme

internal class CodeSyntaxHighlighter {
  private val githubDarkTheme =
    SyntaxTheme(
      key = "github-dark",
      code = 0xC9D1D9,
      keyword = 0xFF7B72,
      string = 0xA5D6FF,
      literal = 0x79C0FF,
      comment = 0x8B949E,
      metadata = 0xD2A8FF,
      multilineComment = 0x8B949E,
      punctuation = 0xC9D1D9,
      mark = 0xC9D1D9,
    )

  private val highlighters = mutableMapOf<SyntaxLanguage, Highlights>()

  fun apply(
    builder: SpannableStringBuilder,
    contentStart: Int,
    contentEnd: Int,
    languageRaw: String?,
  ) {
    if (contentEnd <= contentStart) return

    val code = builder.subSequence(contentStart, contentEnd).toString()
    if (code.isBlank()) return

    val language = normalizeLanguage(languageRaw)
    val highlights =
      try {
        val highlighter =
          highlighters.getOrPut(language) {
            Highlights
              .Builder()
              .language(language)
              .theme(githubDarkTheme)
              .build()
          }
        highlighter.setCode(code)
        highlighter.getHighlights()
      } catch (_: Throwable) {
        emptyList()
      }

    if (highlights.isEmpty()) return

    applyColorHighlights(builder, contentStart, contentEnd, highlights)
  }

  private fun applyColorHighlights(
    builder: SpannableStringBuilder,
    contentStart: Int,
    contentEnd: Int,
    highlights: List<CodeHighlight>,
  ) {
    for (highlight in highlights) {
      val colorHighlight = highlight as? ColorHighlight ?: continue
      val localStart = colorHighlight.location.start
      val localEnd = colorHighlight.location.end

      if (localEnd <= localStart) continue

      val start = (contentStart + localStart).coerceIn(contentStart, contentEnd)
      val end = (contentStart + localEnd).coerceIn(contentStart, contentEnd)
      if (end <= start) continue

      builder.setSpan(
        ForegroundColorSpan(toArgb(colorHighlight.rgb)),
        start,
        end,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }

  private fun normalizeLanguage(rawLanguage: String?): SyntaxLanguage {
    val normalized = rawLanguage?.trim()?.lowercase().orEmpty()
    if (normalized.isEmpty()) return SyntaxLanguage.DEFAULT

    return when (normalized) {
      "js", "jsx", "mjs", "cjs" -> SyntaxLanguage.JAVASCRIPT
      "ts", "tsx" -> SyntaxLanguage.TYPESCRIPT
      "sh", "shell", "bash", "zsh" -> SyntaxLanguage.SHELL
      "c++", "cc", "cxx" -> SyntaxLanguage.CPP
      "c#", "cs", "csharp" -> SyntaxLanguage.CSHARP
      "kt", "kts" -> SyntaxLanguage.KOTLIN
      "py" -> SyntaxLanguage.PYTHON
      "rb" -> SyntaxLanguage.RUBY
      "rs" -> SyntaxLanguage.RUST
      "golang" -> SyntaxLanguage.GO
      "coffee" -> SyntaxLanguage.COFFEESCRIPT
      "pl" -> SyntaxLanguage.PERL
      else -> SyntaxLanguage.getByName(normalized) ?: SyntaxLanguage.DEFAULT
    }
  }

  private fun toArgb(rgb: Int): Int {
    val hasAlpha = (rgb ushr 24) != 0
    return if (hasAlpha) {
      rgb
    } else {
      rgb or (0xFF shl 24)
    }
  }
}

