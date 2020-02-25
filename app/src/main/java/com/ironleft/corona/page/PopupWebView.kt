package com.ironleft.corona.page
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.ironleft.corona.PageID
import com.ironleft.corona.PageParam
import com.ironleft.corona.R

import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.skeleton.rx.RxPageFragment
import kotlinx.android.synthetic.main.popup_webview.*
import kotlin.Exception


class PopupWebView : RxPageFragment() {

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.popup_webview
    private var pageUrl:String? = null
    private var pageContent:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun setParam(param: Map<String, Any?>): PageFragment {
        pageUrl =  param[PageParam.PAGE_URL] as? String?
        pageContent =  param[PageParam.PAGE_CONTENT] as? String?
        return super.setParam(param)

    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreatedView() {
        super.onCreatedView()
        loadingBar.visibility = View.VISIBLE
        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            loadWithOverviewMode = true
            useWideViewPort = true
            @SuppressLint("ObsoleteSdkInt")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// html5에서 https 이미지 안나올때
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                view ?: return
                loadingBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                view ?: return
                loadingBar.visibility = View.GONE
            }
            @SuppressLint("DefaultLocale")
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                webView.loadUrl(url)
                return true
            }
        }
        context?.let {  webView.addJavascriptInterface(WebAppInterface(it), "") }

    }
    inner class WebAppInterface(private val mContext: Context) {
        /** Show a toast from the web page  */
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        if(pageContent == null || pageContent == "") pageUrl?.let { webView.loadUrl(it) }
        else pageContent?.let { webView.loadDataWithBaseURL(null, it, "text/HTML", "UTF-8", null)}
    }

    override fun onSubscribe() {
        super.onSubscribe()
        btnBack.clicks().subscribe {
            PagePresenter.getInstance<PageID>().goBack()
        }.apply { disposables.add(this) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            webView.stopLoading()
            webView.webViewClient = null
            webView.clearCache(true)
            webView.clearHistory()
        } catch ( e:Exception){

        }
        pageUrl = null
    }



}