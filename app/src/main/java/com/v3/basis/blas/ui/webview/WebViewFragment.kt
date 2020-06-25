package com.v3.basis.blas.ui.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.setBackPressedEvent
import kotlinx.android.synthetic.main.fragment_webview.*

class WebViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackPressedEvent {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                it.isEnabled = false
                requireActivity().onBackPressed()
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.webViewClient = BlasWebClient()

        // とりあえずBLAS777に遷移する
        webView.loadUrl("https://www.basis-service.com/blas777/users/login")
    }

    inner class BlasWebClient: WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            request?.also { view?.loadUrl(it.url.toString()) }
            return true
        }
    }
}
