package com.richtext

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.ViewProps
import com.facebook.react.uimanager.ViewDefaults
import com.richtext.events.LinkPressEvent

class RichTextViewManager : SimpleViewManager<RichTextView>() {

    private var reactContext: ThemedReactContext? = null

    override fun getName(): String = "RichTextView"

    override fun createViewInstance(reactContext: ThemedReactContext): RichTextView {
        this.reactContext = reactContext
        return RichTextView(reactContext)
    }

    @ReactProp(name = "markdown")
    fun setMarkdown(view: RichTextView?, markdown: String?) {
        view?.setOnLinkPressCallback { url ->
            emitOnLinkPress(view, url)
        }

        view?.setMarkdownContent(markdown ?: "No markdown content")
    }

    @ReactProp(name = "fontSize", defaultFloat = ViewDefaults.FONT_SIZE_SP)
    fun setFontSize(view: RichTextView?, fontSize: Float) {
        view?.setFontSize(fontSize)
    }

    @ReactProp(name = "fontFamily")
    fun setFontFamily(view: RichTextView?, family: String?) {
        view?.setFontFamily(family)
    }

    @ReactProp(name = ViewProps.COLOR, customType = "Color")
    fun setColor(view: RichTextView?, color: Int?) {
        view?.setColor(color)
    }

    @ReactProp(name = "fontWeight")
    fun setFontWeight(view: RichTextView?, weight: String?) {
        view?.setFontWeight(weight)
    }

    @ReactProp(name = "fontStyle")
    fun setFontStyle(view: RichTextView?, style: String?) {
        view?.setFontStyle(style)
    }

    override fun onAfterUpdateTransaction(view: RichTextView) {
        super.onAfterUpdateTransaction(view)
        view.updateTypeface()
    }

    private fun emitOnLinkPress(view: RichTextView, url: String) {
        val surfaceId = UIManagerHelper.getSurfaceId(reactContext!!)
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext!!, view.id)
        val event = LinkPressEvent(surfaceId, view.id, url)

        eventDispatcher?.dispatchEvent(event)
    }
}
