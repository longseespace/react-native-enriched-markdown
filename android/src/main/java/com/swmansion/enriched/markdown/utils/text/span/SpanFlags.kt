package com.swmansion.enriched.markdown.utils.text.span

import android.text.Spannable
import android.text.SpannableString

const val SPAN_FLAGS_EXCLUSIVE_EXCLUSIVE = SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE

// Ensure blockquote leading margins are applied before list spans sharing the same range.
const val SPAN_PRIORITY_BLOCKQUOTE = 1 shl Spannable.SPAN_PRIORITY_SHIFT
