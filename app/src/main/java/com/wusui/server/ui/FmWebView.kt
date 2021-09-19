package com.wusui.server.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.wusui.server.databinding.FragmentWebviewBinding
import com.wusui.server.model.WikiModel
import solid.ren.skinlibrary.base.SkinBaseFragment

class FmWebView : SkinBaseFragment() {
    private var _binding: FragmentWebviewBinding? = null
    private val binding get() = _binding!!
    val wikiModel: WikiModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        wikiModel.setDefaultValue()
        val webView: WebView = binding.webview
        webView.settings.javaScriptEnabled = true
        webView.canGoBack()
        webView.canGoForward()
        val webSettings = webView.settings
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)
        webSettings.displayZoomControls = false
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.builtInZoomControls = true
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSettings.defaultTextEncodingName = "UTF-8"
//        webSettings.userAgentString =
//            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063"
        webView.webViewClient = webViewClient
        webView.webChromeClient = webChromeClient
        wikiModel.wiki.observe(viewLifecycleOwner, {
            val url = it.getOrNull()
            if (url != null) {
                webView.loadUrl(url)
            } else {
                webView.loadUrl("https://terraria.fandom.com/zh/wiki/Terraria_Wiki")
            }
        })
    }

    private val webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

        }
    }

    private val webChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
        }

        @SuppressLint("SetTextI18n")
        override fun onProgressChanged(view: WebView?, newProgress: Int) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}