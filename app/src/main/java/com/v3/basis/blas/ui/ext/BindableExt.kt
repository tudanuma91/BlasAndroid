package com.v3.basis.blas.ui.ext

import android.text.Html
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean

object CustomBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["hasNotUrl", "nowDownloading"], requireAll = false)
    fun ImageView.setVisibleTwoCondition(hasNotUrl: Boolean, nowDownloading: Boolean) {

        if (hasNotUrl.not() && nowDownloading.not()) {
            visibility = View.VISIBLE
        } else {
            visibility = View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["htmlText"], requireAll = false)
    fun WebView.setHtmlText(text: String) {

        this.loadDataWithBaseURL(null, text, "text/html", "utf-8", null)
//        this.text = Html.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    @JvmStatic
    @BindingAdapter(value = ["backColor"], requireAll = false)
    fun View.backColor(color: Int) {

        this.setBackgroundColor(color)
    }
}
