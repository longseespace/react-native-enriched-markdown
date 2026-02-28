package com.swmansion.enriched.markdown.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.LruCache
import com.agog.mathdisplay.MTFontManager
import com.agog.mathdisplay.parse.MTLineStyle
import com.agog.mathdisplay.parse.MTMathListBuilder
import com.agog.mathdisplay.parse.MTParseError
import com.agog.mathdisplay.parse.MTParseErrors
import com.agog.mathdisplay.render.MTTypesetter
import com.swmansion.enriched.markdown.parser.MarkdownASTNode
import com.swmansion.enriched.markdown.utils.text.span.SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class MathRenderer(
  private val config: RendererConfig,
  private val isDisplayMode: Boolean,
) : NodeRenderer {
  override fun render(
    node: MarkdownASTNode,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)?,
    onLinkLongPress: ((String) -> Unit)?,
    factory: RendererFactory,
  ) {
    val latex = extractLatex(node).trim()
    if (latex.isEmpty()) return

    val blockStyle =
      runCatching { factory.blockStyleContext.requireBlockStyle() }
        .getOrNull()
    val textColor = blockStyle?.color ?: Color.BLACK
    val fontSizePx = (blockStyle?.fontSize ?: config.style.paragraphStyle.fontSize).coerceAtLeast(12f)

    val bitmap = getOrRenderBitmap(factory, latex, textColor, fontSizePx)
    if (bitmap == null) {
      val fallback = if (isDisplayMode) "\$\$$latex\$\$" else "\$$latex\$"
      builder.append(fallback)
      return
    }

    var lineStart = -1
    if (isDisplayMode) {
      if (builder.isNotEmpty() && builder.last() != '\n') {
        builder.append("\n")
      }
      lineStart = builder.length
    }

    val markerStart = builder.length
    builder.append("\uFFFC")
    builder.setSpan(
      ImageSpan(
        factory.context,
        bitmap,
        if (isDisplayMode) DynamicDrawableSpan.ALIGN_BOTTOM else DynamicDrawableSpan.ALIGN_BASELINE,
      ),
      markerStart,
      markerStart + 1,
      SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
    )

    if (isDisplayMode) {
      builder.append("\n")
      builder.setSpan(
        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
        lineStart,
        builder.length,
        SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE,
      )
    }
  }

  private fun extractLatex(node: MarkdownASTNode): String {
    if (node.children.isEmpty()) {
      return node.content
    }
    val out = StringBuilder()
    appendText(node, out)
    return out.toString()
  }

  private fun appendText(
    node: MarkdownASTNode,
    out: StringBuilder,
  ) {
    if (node.content.isNotEmpty()) {
      out.append(node.content)
    }
    node.children.forEach { child -> appendText(child, out) }
  }

  private fun getOrRenderBitmap(
    factory: RendererFactory,
    latex: String,
    textColor: Int,
    fontSizePx: Float,
  ): Bitmap? {
    val cacheKey = "$latex|$textColor|$fontSizePx|${if (isDisplayMode) "d" else "i"}"
    synchronized(bitmapCache) {
      bitmapCache.get(cacheKey)?.let { return it }
    }

    val rendered = renderBitmap(factory, latex, textColor, fontSizePx) ?: return null
    synchronized(bitmapCache) {
      bitmapCache.put(cacheKey, rendered)
    }
    return rendered
  }

  private fun renderBitmap(
    factory: RendererFactory,
    latex: String,
    textColor: Int,
    fontSizePx: Float,
  ): Bitmap? =
    runOnMainThreadBlocking {
      runCatching {
        MTFontManager.Companion.setContext(factory.context)

        val parseError = MTParseError()
        val mathList = MTMathListBuilder.Factory.buildFromString(latex, parseError)
        if (mathList == null || parseError.errorcode != MTParseErrors.ErrorNone) {
          return@runCatching null
        }

        val lineStyle =
          if (isDisplayMode) {
            MTLineStyle.KMTLineStyleDisplay
          } else {
            MTLineStyle.KMTLineStyleText
          }

        val font =
          MTFontManager.Companion
            .defaultFont()
            ?.copyFontWithSize(fontSizePx.coerceAtLeast(12f))
            ?: return@runCatching null

        val display = MTTypesetter.Companion.createLineForMathList(mathList, font, lineStyle)
        display.textColor = textColor

        val density = factory.context.resources.displayMetrics.density
        val horizontalPaddingPx = if (isDisplayMode) 6f * density else 2f * density
        val verticalPaddingPx = if (isDisplayMode) 4f * density else 1f * density

        val rawWidth = ceil(display.width.toDouble()).toInt().coerceAtLeast(1)
        val rawHeight = ceil((display.ascent + display.descent).toDouble()).toInt().coerceAtLeast(1)
        val baseWidth = ceil((rawWidth + horizontalPaddingPx * 2f).toDouble()).toInt().coerceAtLeast(1)
        val baseHeight = ceil((rawHeight + verticalPaddingPx * 2f).toDouble()).toInt().coerceAtLeast(1)

        val maxBitmapWidth = min(MAX_BITMAP_WIDTH_PX, max(MIN_BITMAP_WIDTH_PX, factory.context.resources.displayMetrics.widthPixels * 2))
        val maxBitmapHeight = if (isDisplayMode) MAX_DISPLAY_BITMAP_HEIGHT_PX else MAX_INLINE_BITMAP_HEIGHT_PX

        var scale = 1f
        if (baseWidth > maxBitmapWidth) {
          scale = min(scale, maxBitmapWidth.toFloat() / baseWidth.toFloat())
        }
        if (baseHeight > maxBitmapHeight) {
          scale = min(scale, maxBitmapHeight.toFloat() / baseHeight.toFloat())
        }

        val basePixels = baseWidth.toLong() * baseHeight.toLong()
        if (basePixels > MAX_BITMAP_PIXELS) {
          val pixelScale = sqrt(MAX_BITMAP_PIXELS.toDouble() / basePixels.toDouble()).toFloat()
          scale = min(scale, pixelScale)
        }

        if (scale < MIN_RENDER_SCALE) {
          return@runCatching null
        }

        val bitmapWidth = max(1, ceil((baseWidth.toFloat() * scale).toDouble()).toInt())
        val bitmapHeight = max(1, ceil((baseHeight.toFloat() * scale).toDouble()).toInt())

        Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
          val canvas = Canvas(bitmap)
          canvas.save()
          canvas.translate(0f, bitmapHeight.toFloat())
          canvas.scale(scale, -scale)
          display.position.x = horizontalPaddingPx
          display.position.y = display.descent + verticalPaddingPx
          display.draw(canvas)
          canvas.restore()
        }
      }.getOrNull()
    }

  private fun <T> runOnMainThreadBlocking(block: () -> T?): T? {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      return block()
    }

    val resultRef = AtomicReference<T?>(null)
    val done = CountDownLatch(1)
    Handler(Looper.getMainLooper()).post {
      try {
        resultRef.set(block())
      } finally {
        done.countDown()
      }
    }

    val completed = runCatching { done.await(RENDER_TIMEOUT_MS, TimeUnit.MILLISECONDS) }.getOrDefault(false)
    if (!completed) {
      return null
    }

    return resultRef.get()
  }

  companion object {
    // 8 MB LRU cache for pre-rendered equations.
    private val bitmapCache =
      object : LruCache<String, Bitmap>(8 * 1024) {
        override fun sizeOf(
          key: String,
          value: Bitmap,
        ): Int = value.byteCount / 1024
      }

    private const val MIN_BITMAP_WIDTH_PX = 512
    private const val MAX_BITMAP_WIDTH_PX = 2048
    private const val MAX_DISPLAY_BITMAP_HEIGHT_PX = 1536
    private const val MAX_INLINE_BITMAP_HEIGHT_PX = 512
    private const val MAX_BITMAP_PIXELS = 2_000_000L
    private const val MIN_RENDER_SCALE = 0.2f
    private const val RENDER_TIMEOUT_MS = 2_000L
  }
}
