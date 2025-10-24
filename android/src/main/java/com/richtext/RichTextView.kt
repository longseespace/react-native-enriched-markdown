package com.richtext

import android.content.Context
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.richtext.parser.Parser
import com.richtext.renderer.Renderer
import com.richtext.theme.RichTextTheme

class RichTextView : AppCompatTextView {

    private val parser = Parser()
    private val renderer = Renderer()
    private var theme = RichTextTheme.defaultTheme()
    private var onLinkPressCallback: ((String) -> Unit)? = null
    
    constructor(context: Context) : super(context) {
        prepareComponent()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        prepareComponent()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        prepareComponent()
    }

    private fun prepareComponent() {
        // Initialize the component with basic TextView setup
        text = "RichTextView - Ready for markdown!"
        textSize = 16f
        setTextColor(Color.BLACK)
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun setMarkdownContent(markdown: String) {
        try {
            val document = parser.parseMarkdown(markdown)
            if (document != null) {
                val styledText = renderer.renderDocument(document, theme, onLinkPressCallback)
                setText(styledText)
            } else {
                text = "Error parsing markdown - Document is null"
            }
        } catch (e: Exception) {
            text = "Error: ${e.message}"
        }
    }

    fun updateTheme(newTheme: RichTextTheme) {
        theme = newTheme
        if (text.isNotEmpty()) {
            val markdown = text.toString()
            setMarkdownContent(markdown)
        }
    }

    fun setOnLinkPressCallback(callback: (String) -> Unit) {
        onLinkPressCallback = callback
    }

    fun emitOnLinkPress(url: String) {
        val context = this.context as? com.facebook.react.bridge.ReactContext ?: return
        val surfaceId = com.facebook.react.uimanager.UIManagerHelper.getSurfaceId(context)
        val dispatcher = com.facebook.react.uimanager.UIManagerHelper.getEventDispatcherForReactTag(context, id)
        
        dispatcher?.dispatchEvent(
            com.richtext.events.LinkPressEvent(
                surfaceId,
                id,
                url
            )
        )
    }
}
