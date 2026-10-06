package com.bubbleroute.game.core.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object ScoreFormatter {

    private val grouped = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    fun grouped(value: Int): String = grouped.format(value.toLong())
}
