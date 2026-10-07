package com.bragadev.fincheck.core.util.extensions

import android.content.Context
import android.content.Intent

/** Opens the Android share sheet (WhatsApp, e-mail...) with [text]. */
fun Context.shareText(text: String, chooserTitle: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(sendIntent, chooserTitle))
}
