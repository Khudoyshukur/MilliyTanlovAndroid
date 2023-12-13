package uz.androdev.milliytanlov.base

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.net.http.SslCertificate
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.androdev.milliytanlov.databinding.FrarmentBaseWebviewBinding
import uz.androdev.milliytanlov.databinding.LayoutWebViewBinding
import java.util.Arrays

/**
 * Created by: androdev
 * Date: 08-12-2023
 * Time: 3:39 PM
 * Email: Khudoyshukur.Juraev.001@mail.ru
 */

abstract class BaseWebViewFragment : BaseFragment<FrarmentBaseWebviewBinding>(
    FrarmentBaseWebviewBinding::inflate
) {
    private var webViewBinding: LayoutWebViewBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) { initialDelay() }
            runCatching { startProcessing() }
        }
    }

    private suspend fun startProcessing() {
        val link = withContext(Dispatchers.IO) { getPageUrl() }
        binding.bindWebView(link)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webViewBinding?.webView?.canGoBack() == true) {
                        webViewBinding?.webView?.goBack()
                    } else {
                        isEnabled = false
                        activity?.onBackPressedDispatcher?.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun FrarmentBaseWebviewBinding.bindWebView(link: String) {
        root.postDelayed({
            webViewBinding = LayoutWebViewBinding.bind(webViewStub.inflate())
            with(webViewBinding?.webView ?: return@postDelayed) {
                with(CookieManager.getInstance()) {
                    setAcceptCookie(true)
                    acceptCookie()
                    getCookie(link)
                }
                settings.javaScriptEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.databaseEnabled = true
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, progress: Int) {
                        loadingView.root.visibility = if (progress != 100) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView, request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        when {
                            url.startsWith("mailto:") -> {
                                try {
                                    startActivity(
                                        Intent(Intent.ACTION_VIEW, request.url)
                                    )
                                } catch (e: ActivityNotFoundException) {
                                    // ignore
                                }
                                return true
                            }
                        }

                        return false
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        try {
                            val certificates = getWebsiteCertificates()

                            val serverCertificate: SslCertificate = error!!.certificate
                            val serverBundle: Bundle = SslCertificate.saveState(serverCertificate)
                            for (appCertificate in certificates) {
                                if (TextUtils.equals(
                                        serverCertificate.toString(),
                                        appCertificate.toString()
                                    )
                                ) { // First fast check
                                    val appBundle: Bundle = SslCertificate.saveState(appCertificate)
                                    val keySet = appBundle.keySet()
                                    var matches = true
                                    for (key in keySet) {
                                        val serverObj = serverBundle[key]
                                        val appObj = appBundle[key]
                                        if (serverObj is ByteArray && appObj is ByteArray) {     // key "x509-certificate"
                                            if (!Arrays.equals(
                                                    serverObj as ByteArray?,
                                                    appObj as ByteArray?
                                                )
                                            ) {
                                                matches = false
                                                break
                                            }
                                        } else if (serverObj != null && serverObj != appObj) {
                                            matches = false
                                            break
                                        }
                                    }
                                    if (matches) {
                                        handler!!.proceed()
                                        return
                                    }
                                }
                            }

                            handler!!.cancel()
                            val message = "SSL Error " + error.primaryError
                        } catch (e: Exception) {
                            handler?.cancel()
                        }
                    }
                }
                loadUrl(link)
            }
        }, 200)
    }

    override fun onDestroyView() {
        webViewBinding?.let {
            it.webView.webChromeClient = WebChromeClient()
            it.webView.stopLoading()
        }
        webViewBinding = null
        super.onDestroyView()
    }

    abstract suspend fun getPageUrl(): String
    abstract suspend fun initialDelay()
    abstract fun getWebsiteCertificates(): List<SslCertificate>
}