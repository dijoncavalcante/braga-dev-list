package com.bragadev.fincheck.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Widest a screen's content gets. Phones are narrower than this, so nothing changes on them;
 * tablets, foldables and landscape windows get a centered column instead of edge-to-edge content.
 */
val MaxContentWidth = 720.dp

/**
 * Centers the content in a column of at most [MaxContentWidth]. On large screens (where Android 16
 * also ignores the portrait lock) rows stay short enough to read, with an item's name and price
 * close together, instead of spanning the whole display.
 */
fun Modifier.adaptiveContentWidth(): Modifier = this
    .fillMaxWidth()
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = MaxContentWidth)
    .fillMaxWidth()
