package com.richtext

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.UIManagerHelper
import com.richtext.events.LinkPressEvent

/**
 * ViewManager for RichTextView component
 */
class RichTextViewManager : SimpleViewManager<RichTextView>() {

    private var reactContext: ThemedReactContext? = null

    override fun getName(): String = "RichTextView"

    override fun createViewInstance(reactContext: ThemedReactContext): RichTextView {
        this.reactContext = reactContext
        return RichTextView(reactContext)
    }

    // Basic props for testing
    @ReactProp(name = "markdown")
    fun setMarkdown(view: RichTextView?, markdown: String?) {
        // Set up link press callback BEFORE parsing markdown
        view?.setOnLinkPressCallback { url ->
            // Debug logging
            android.util.Log.d("RichTextViewManager", "Link pressed with URL: '$url'")
            // Emit onLinkPress event to React Native using Event class
            emitOnLinkPress(view, url)
        }
        
        // Parse and render markdown AFTER callback is set
        view?.setMarkdownContent(markdown ?: "No markdown content")
    }

    @ReactProp(name = "fontSize", defaultFloat = 16f)
    fun setFontSize(view: RichTextView?, fontSize: Float) {
        view?.textSize = fontSize
    }
    
    private fun emitOnLinkPress(view: RichTextView, url: String) {
        val surfaceId = UIManagerHelper.getSurfaceId(reactContext!!)
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext!!, view.id)
        val event = LinkPressEvent(surfaceId, view.id, url)
        
        android.util.Log.d("RichTextViewManager", "Emitting event with URL: '$url'")
        eventDispatcher?.dispatchEvent(event)
    }
}
