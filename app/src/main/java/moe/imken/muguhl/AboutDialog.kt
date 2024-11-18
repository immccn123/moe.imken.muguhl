package moe.imken.muguhl

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutDialog(ctx: Context) : MaterialAlertDialogBuilder(ctx) {
    init {
        val htmlTextView = TextView(ctx).apply {
            val htmlContent = ctx.getString(R.string.about_dialog_content).trimIndent()
            text = fromHtmlCompat(htmlContent)
            movementMethod = LinkMovementMethod.getInstance()
            setPadding(120, 40, 120, 40)
        }
        setTitle(R.string.about_dialog_title)
        setView(htmlTextView)
        setPositiveButton(R.string.close) { dialog, _ ->
            dialog.dismiss()
        }
    }

    private fun fromHtmlCompat(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION") Html.fromHtml(html)
        }
    }
}
