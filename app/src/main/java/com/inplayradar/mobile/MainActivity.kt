package com.inplayradar.mobile

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {
    private lateinit var drawer: DrawerLayout
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var web: WebView
    private val homeUrl = "http://inplayradar.com/login/"
    private var zoom = 100

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        drawer = DrawerLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setScrimColor(0x99000000.toInt())
        }

        refresh = SwipeRefreshLayout(this)
        web = WebView(this).apply {
            setBackgroundColor(Color.WHITE)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false

            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                WebViewCompat.addDocumentStartJavaScript(
                    this,
                    """
                    (function() {
                        var css = '.ir-skip-link { display: none !important; }';
                        function addStyle() {
                            if (!document.documentElement) return false;
                            if (!document.getElementById('irSkipLinkHideStyle')) {
                                var style = document.createElement('style');
                                style.id = 'irSkipLinkHideStyle';
                                style.textContent = css;
                                document.documentElement.appendChild(style);
                            }
                            return true;
                        }
                        if (!addStyle()) {
                            var observer = new MutationObserver(function() {
                                if (addStyle()) observer.disconnect();
                            });
                            observer.observe(document, { childList: true, subtree: true });
                        }
                    })();
                    """.trimIndent(),
                    setOf("*")
                )
            }

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    refresh.isRefreshing = false
                }

                override fun onReceivedError(
                    view: WebView?, 
                    request: WebResourceRequest?, 
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) refresh.isRefreshing = false
                }
            }
            webChromeClient = WebChromeClient()
        }

        refresh.setColorSchemeResources(android.R.color.darker_gray)
        refresh.setOnChildScrollUpCallback { _, _ -> web.scrollY > 0 }
        refresh.setOnRefreshListener { web.reload() }
        refresh.addView(web, ViewGroup.LayoutParams(-1, -1))
        drawer.addView(refresh, DrawerLayout.LayoutParams(-1, -1))

        val panel = buildDrawer()
        val panelParams = DrawerLayout.LayoutParams(dp(288), -1).apply {
            gravity = Gravity.START
        }
        drawer.addView(panel, panelParams)

        setContentView(drawer)
        web.loadUrl(homeUrl)
    }

    private fun buildDrawer(): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(42), dp(16), dp(20))
            setBackgroundColor(Color.rgb(248, 248, 248))
            elevation = dp(12).toFloat()
        }

        fun item(label: String, action: () -> Unit) {
            val row = TextView(this).apply {
                text = label
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(dp(12), dp(16), dp(12), dp(16))
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    action()
                    drawer.closeDrawer(Gravity.START)
                }
            }
            box.addView(row, LinearLayout.LayoutParams(-1, -2))
        }

        item("Ana Sayfa") { web.loadUrl(homeUrl) }
        item("Yakınlaştırma") {
            val levels = arrayOf("40%", "50%", "60%", "70%", "80%", "90%", "100%", "110%", "120%", "130%", "140%", "150%", "160%", "170%", "180%", "190%", "200%")
            AlertDialog.Builder(this)
                .setTitle("Yakınlaştırma")
                .setItems(levels) { _, which ->
                    zoom = levels[which].removeSuffix("%").toInt()
                    web.evaluateJavascript("document.documentElement.style.zoom='${zoom}%'", null)
                }
                .show()
        }
        item("Tam ekran") {
            val immersive =
                (window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == 0
            window.decorView.systemUiVisibility = if (immersive) {
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }
        item("Ayarlar") {
            AlertDialog.Builder(this)
                .setTitle("Ayarlar")
                .setMessage("Yeni ayarlar sonraki sürümlerde buraya eklenebilir.")
                .setPositiveButton("Tamam", null)
                .show()
        }
        item("Hakkında") {
            AlertDialog.Builder(this)
                .setTitle("Inplay Radar")
                .setMessage("Sürüm 1.0.0")
                .setPositiveButton("Tamam", null)
                .show()
        }
        return box
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    @Deprecated("Deprecated in Android API; retained for backward compatibility.")
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START)
        } else {
            super.onBackPressed()
        }
    }
}
