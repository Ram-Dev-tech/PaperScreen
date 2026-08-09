package com.paperscreen.android.reader.parser

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

object HtmlUtils {
    fun stripHtml(html: String): AnnotatedString {
        val cleanHtml = html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>\\s*<p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n# ")
        val spanned = android.text.Html.fromHtml(cleanHtml, android.text.Html.FROM_HTML_MODE_COMPACT)
        return spannedToAnnotatedString(spanned)
    }

    fun spannedToAnnotatedString(spanned: Spanned): AnnotatedString {
        return buildAnnotatedString {
            append(spanned.toString())
            
            val spans = spanned.getSpans(0, spanned.length, Any::class.java)
            for (span in spans) {
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                
                when (span) {
                    is StyleSpan -> {
                        when (span.style) {
                            Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                            Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                        }
                    }
                    is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    is StrikethroughSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                    is RelativeSizeSpan -> addStyle(SpanStyle(fontSize = androidx.compose.ui.unit.TextUnit(span.sizeChange, androidx.compose.ui.unit.TextUnitType.Em)), start, end)
                }
            }
        }
    }
}
