package com.richtext.spans

import android.text.style.URLSpan

class CallbackURLSpan(url: String, private val onLinkPress: ((String) -> Unit)?) : URLSpan(url) {
    override fun onClick(widget: android.view.View) {
        if (onLinkPress != null) {
            onLinkPress(url)
        } else {
            super.onClick(widget)
        }
    }
}
