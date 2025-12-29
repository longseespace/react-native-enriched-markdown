package com.richtext.renderer

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import com.richtext.styles.StyleConfig
import org.commonmark.node.Document
import org.commonmark.node.Node

class Renderer {
  private var style: StyleConfig? = null
  private var lastConfiguredStyle: StyleConfig? = null
  private var lastConfiguredContext: Context? = null
  private lateinit var rendererFactory: RendererFactory

  fun configure(
    style: StyleConfig,
    context: Context,
  ) {
    // Only reconfigure if style or context changed
    if (lastConfiguredStyle !== style || lastConfiguredContext !== context) {
      this.style = style
      lastConfiguredStyle = style
      lastConfiguredContext = context
      val config = RendererConfig(style)
      rendererFactory = RendererFactory(config, context)
    }
  }

  fun renderDocument(
    document: Document,
    onLinkPress: ((String) -> Unit)? = null,
  ): SpannableString {
    val builder = SpannableStringBuilder()
    requireNotNull(style) {
      "richTextStyle should always be provided from JS side with defaults."
    }

    renderNode(document, builder, onLinkPress, rendererFactory)

    return SpannableString(builder)
  }

  private fun renderNode(
    node: Node,
    builder: SpannableStringBuilder,
    onLinkPress: ((String) -> Unit)? = null,
    factory: RendererFactory,
  ) {
    val renderer = factory.getRenderer(node)
    renderer.render(node, builder, onLinkPress, factory)
  }
}
