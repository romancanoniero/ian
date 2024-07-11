package com.iyr.ian.utils

import android.content.Context
import android.text.SpannedString
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat

class HTMLExtensions

fun Context.getHtmlStyledText(
    @StringRes htmlStringRes: Int,
    vararg args: Any
): CharSequence {

    // paso 0 - Codificar marcadores de posición de cadena
    val escapedArgs = args.map {
        if (it is String) TextUtils.htmlEncode(it) else it
    }.toTypedArray()

    // paso 1 - getText()
    val styledString = getText(htmlStringRes)

    // paso 2 - toHtml()
    val spannedString = SpannedString(styledString)
    val htmlString = HtmlCompat.toHtml(
        spannedString,
        HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL
    )
        .substringBeforeLast('>')
        .plus(">")

    // paso 3 - Cadena.formato()
    val dynamicStyledString =
        String.format(htmlString, *escapedArgs)

    // paso 4 - fromHtml()
    return HtmlCompat.fromHtml(
        dynamicStyledString,
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )
        .removeSuffix("\n") //fromHtml() agrega una nueva línea en el fin
}
