package com.richtext.renderer

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import org.commonmark.node.*
import com.richtext.theme.RichTextTheme

class CustomURLSpan(url: String, private val onLinkPress: ((String) -> Unit)?) : URLSpan(url) {
    override fun onClick(widget: android.view.View) {
        if (onLinkPress != null) {
            onLinkPress(url)
        } else {
            super.onClick(widget)
        }
    }
}

class Renderer {
    fun renderDocument(document: Document, theme: RichTextTheme, onLinkPress: ((String) -> Unit)? = null): SpannableString {
        val builder = SpannableStringBuilder()

        renderNode(document, builder, theme, onLinkPress)

        return SpannableString(builder)
    }

    private fun renderNode(
        node: Node,
        builder: SpannableStringBuilder,
        theme: RichTextTheme,
        onLinkPress: ((String) -> Unit)? = null
    ) {
        val renderer = NodeRendererFactory.getRenderer(node)
        renderer.render(node, builder, theme, onLinkPress)
    }
}
