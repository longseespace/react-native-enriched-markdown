package com.richtext.renderer

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import org.commonmark.node.*
import com.richtext.theme.RichTextTheme

/**
 * Custom URLSpan that overrides onClick to call our callback instead of opening browser
 */
class CustomURLSpan(url: String, private val onLinkPress: ((String) -> Unit)?) : URLSpan(url) {
    override fun onClick(widget: android.view.View) {
        if (onLinkPress != null) {
            onLinkPress(url)
        } else {
            // Fallback to default URLSpan behavior (open browser)
            super.onClick(widget)
        }
    }
}

/**
 * Renders CommonMark Document to styled text using SpannableString
 */
class Renderer {
    
    /**
     * Render the CommonMark Document to styled text
     */
    fun renderDocument(document: Document, theme: RichTextTheme, onLinkPress: ((String) -> Unit)? = null): SpannableString {
        val builder = SpannableStringBuilder()
        
        renderNode(document, builder, theme, onLinkPress)
        
        return SpannableString(builder)
    }
    
    /**
     * Render a single CommonMark node
     */
    private fun renderNode(
        node: Node, 
        builder: SpannableStringBuilder, 
        theme: RichTextTheme, 
        onLinkPress: ((String) -> Unit)? = null
    ) {
        when (node) {
            is Document -> {
                // Render all children
                var child = node.firstChild
                while (child != null) {
                    renderNode(child, builder, theme, onLinkPress)
                    child = child.next
                }
            }
            
            is Paragraph -> {
                // Render paragraph content
                var child = node.firstChild
                while (child != null) {
                    renderNode(child, builder, theme, onLinkPress)
                    child = child.next
                }
                // Add line break after paragraph
                builder.append("\n")
            }
            
            is Heading -> {
                val start = builder.length
                // Render heading content
                var child = node.firstChild
                while (child != null) {
                    renderNode(child, builder, theme, onLinkPress)
                    child = child.next
                }
                
                // Apply heading styling if content was added
                val contentLength = builder.length - start
                if (contentLength > 0) {
                    val level = node.level
                    val scale = theme.headerConfig.scale
                    val isBold = theme.headerConfig.isBold
                    
                    // Apply bold style if needed
                    if (isBold) {
                        builder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            start + contentLength,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                builder.append("\n")
            }
            
            is Text -> {
                // Add text content
                val text = node.literal ?: ""
                builder.append(text)
            }
            
            is Link -> {
                val start = builder.length
                val url = node.destination ?: ""
                            
                // Render link content
                var child = node.firstChild
                while (child != null) {
                    renderNode(child, builder, theme, onLinkPress)
                    child = child.next
                }
                
                // Apply link styling if content was added
                val contentLength = builder.length - start
                
                if (contentLength > 0) {
                    builder.setSpan(
                        CustomURLSpan(url, onLinkPress),
                        start,
                        start + contentLength,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    
                    // Add underline
                    builder.setSpan(
                        UnderlineSpan(),
                        start,
                        start + contentLength,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            
            is HardLineBreak, is SoftLineBreak -> {
                builder.append("\n")
            }
            
            else -> {
                // Skip unsupported node types
                android.util.Log.w("Renderer", "Skipping unsupported CommonMark node type: ${node.javaClass.simpleName}")
            }
        }
    }
}