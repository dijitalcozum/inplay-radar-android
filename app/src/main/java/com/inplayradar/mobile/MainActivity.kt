package com.inplayradar.mobile

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
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
    private lateinit var zoomPanel: LinearLayout
    private lateinit var zoomValue: TextView
    private lateinit var zoomSeekBar: SeekBar
    private val homeUrl = "http://inplayradar.com/login/"
    private var zoom = 100
    private val zoomHandler = Handler(Looper.getMainLooper())
    private val hideZoomPanel = Runnable {
        if (::zoomPanel.isInitialized) zoomPanel.visibility = View.GONE
    }

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
                    applyZoom()
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

        val content = FrameLayout(this)
        content.addView(refresh, FrameLayout.LayoutParams(-1, -1))
        drawer.addView(content, DrawerLayout.LayoutParams(-1, -1))

        val drawerPanel = buildDrawer()
        val drawerParams = DrawerLayout.LayoutParams(dp(288), -1).apply {
            gravity = Gravity.START
        }
        drawer.addView(drawerPanel, drawerParams)

        zoomPanel = buildZoomPanel()
        val zoomPanelParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            setMargins(dp(16), 0, dp(16), dp(24))
        }
        content.addView(zoomPanel, zoomPanelParams)
        ViewCompat.setOnApplyWindowInsetsListener(content) { _, insets ->
            val navigationBarHeight =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val params = zoomPanel.layoutParams as FrameLayout.LayoutParams
            params.bottomMargin = navigationBarHeight + dp(24)
            zoomPanel.layoutParams = params
            insets
        }
        ViewCompat.requestApplyInsets(content)

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
        item("Yakınlaştır") { showZoomPanel() }
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

    private fun buildZoomPanel(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), 0, dp(18), 0)
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
        }

        val pillColor = Color.rgb(250, 248, 253)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(pillColor)
                cornerRadius = dp(40).toFloat()
            }
            elevation = dp(8).toFloat()
        }

        zoomValue = TextView(this).apply {
            text = "$zoom%"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(25, 25, 25))
            setTypeface(null, Typeface.NORMAL)
        }
        header.addView(zoomValue, LinearLayout.LayoutParams(dp(74), dp(54)))

        val divider = View(this).apply {
            setBackgroundColor(Color.rgb(218, 215, 224))
        }
        header.addView(divider, LinearLayout.LayoutParams(dp(1), dp(54)))

        val reset = TextView(this).apply {
            text = "Sıfırla"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(66, 133, 244))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                setZoom(100)
                scheduleZoomPanelHide()
            }
        }
        header.addView(reset, LinearLayout.LayoutParams(dp(96), dp(54)))
        panel.addView(
            header,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(54)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            background = GradientDrawable().apply {
                setColor(pillColor)
                cornerRadius = dp(40).toFloat()
            }
            elevation = dp(8).toFloat()
        }

        fun zoomButton(label: String, change: Int): TextView =
            TextView(this).apply {
                text = label
                textSize = 29f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    setZoom((zoom + change).coerceIn(40, 200))
                    scheduleZoomPanelHide()
                }
            }

        controls.addView(zoomButton("−", -10), LinearLayout.LayoutParams(dp(48), dp(54)))

        zoomSeekBar = SeekBar(this).apply {
            max = 16
            progress = (zoom - 40) / 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        zoom = 40 + progress * 10
                        updateZoomDisplay()
                        applyZoom()
                        scheduleZoomPanelHide()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    zoomHandler.removeCallbacks(hideZoomPanel)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    scheduleZoomPanelHide()
                }
            })
        }
        controls.addView(zoomSeekBar, LinearLayout.LayoutParams(0, dp(54), 1f))
        controls.addView(zoomButton("+", 10), LinearLayout.LayoutParams(dp(48), dp(54)))
        panel.addView(
            controls,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
                topMargin = dp(8)
            }
        )

        return panel
    }

    private fun showZoomPanel() {
        zoomSeekBar.progress = (zoom - 40) / 10
        updateZoomDisplay()
        zoomPanel.visibility = View.VISIBLE
        scheduleZoomPanelHide()
    }

    private fun setZoom(value: Int) {
        zoom = value.coerceIn(40, 200)
        zoomSeekBar.progress = (zoom - 40) / 10
        updateZoomDisplay()
        applyZoom()
    }

    private fun updateZoomDisplay() {
        zoomValue.text = "$zoom%"
    }

    private fun applyZoom() {
        if (!::web.isInitialized) return
        web.evaluateJavascript(
            "document.documentElement.style.zoom='${zoom}%'",
            null
        )
    }

    private fun scheduleZoomPanelHide() {
        zoomHandler.removeCallbacks(hideZoomPanel)
        zoomHandler.postDelayed(hideZoomPanel, 3000)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    @Deprecated("Deprecated in Android API; retained for backward compatibility.")
    override fun onBackPressed() {
        if (::zoomPanel.isInitialized && zoomPanel.visibility == View.VISIBLE) {
            zoomHandler.removeCallbacks(hideZoomPanel)
            zoomPanel.visibility = View.GONE
        } else if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        zoomHandler.removeCallbacks(hideZoomPanel)
        super.onDestroy()
    }
}
