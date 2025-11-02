package com.richtext.renderer

import android.text.SpannableString
import android.text.SpannableStringBuilder
import com.richtext.styles.RichTextStyle
import org.commonmark.node.*

class Renderer {
    private var style: RichTextStyle? = null

    fun setStyle(style: RichTextStyle?) {
        this.style = style
    }

    fun renderDocument(document: Document, onLinkPress: ((String) -> Unit)? = null): SpannableString {
        val builder = SpannableStringBuilder()
        val config = RendererConfig(style)

        renderNode(document, builder, onLinkPress, config)

        return SpannableString(builder)
    }

    private fun renderNode(
        node: Node,
        builder: SpannableStringBuilder,
        onLinkPress: ((String) -> Unit)? = null,
        config: RendererConfig
    ) {
        val renderer = NodeRendererFactory.getRenderer(node, config)
        renderer.render(node, builder, onLinkPress)
    }
}
