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
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private data class MenuConfig(
        val menuWidth: Int = 288,
        val menuHeight: Int = 0,
        val menuVerticalPosition: String = "top",
        val logoX: Int = 0,
        val logoY: Int = 0,
        val titleX: Int = 0,
        val titleY: Int = 0,
        val closeX: Int = 0,
        val closeY: Int = 0,
        val menuIconX: Int = 0,
        val menuIconY: Int = 0,
        val menuTextX: Int = 0,
        val menuTextY: Int = 0,
        val controlButtonX: Int = 0,
        val controlButtonY: Int = 0,
        val controlIconX: Int = 0,
        val controlIconY: Int = 0,
        val controlTextX: Int = 0,
        val controlTextY: Int = 0,
        val menuPadding: Int = 16,
        val headerHeight: Int = 62,
        val logoWidth: Int = 42,
        val logoHeight: Int = 42,
        val closeWidth: Int = 40,
        val closeHeight: Int = 48,
        val itemHeight: Int = 52,
        val itemSpacing: Int = 8,
        val iconAreaWidth: Int = 38,
        val iconSize: Int = 28,
        val fontSize: Int = 16,
        val controlHeight: Int = 78,
        val controlIconHeight: Int = 28,
        val controlLabelHeight: Int = 20,
        val cornerRadius: Int = 20,
        val itemCornerRadius: Int = 11,
        val controlCornerRadius: Int = 12,
        val menuTopPadding: Int = 24,
        val menuBottomPadding: Int = 12,
        val headerTitleSize: Int = 20,
        val headerTitleHeight: Int = 48,
        val itemPadding: Int = 10,
        val itemTextPadding: Int = 8,
        val controlPadding: Int = 7,
        val controlIconSize: Int = 24,
        val controlLabelSize: Int = 13,
        val controlTopMargin: Int = 2,
        val controlBottomMargin: Int = 8,
        val zoomPanelPadding: Int = 18,
        val zoomPanelBottom: Int = 32,
        val zoomHeaderHeight: Int = 48,
        val zoomValueWidth: Int = 68,
        val zoomValueTextSize: Int = 17,
        val zoomResetWidth: Int = 87,
        val zoomResetTextSize: Int = 16,
        val zoomControlHeight: Int = 48,
        val zoomButtonTextSize: Int = 29,
        val zoomMinusWidth: Int = 44,
        val zoomPlusWidth: Int = 48,
        val menuBorder: String = "#69707D",
        val dividerColor: String = "#707784",
        val selectedColor: String = "#0069E6",
        val controlBackground: String = "#3F434D",
        val controlBorder: String = "#69707D",
        val zoomBackground: String = "#FAF8FD",
        val zoomDivider: String = "#DAD7E0",
        val zoomTextColor: String = "#191919",
        val zoomResetColor: String = "#4285F4"
    )

    private var menuConfig = MenuConfig()
    private data class MenuTheme(
        val id: String,
        val name: String,
        val background: String,
        val border: String,
        val divider: String,
        val selected: String,
        val controlBackground: String,
        val controlBorder: String,
        val text: String,
        val icon: String,
        val secondaryText: String
    )

    private val menuThemes = listOf(
        MenuTheme(
            "minimal", "Kompakt Minimal",
            "#F8FAFC", "#E2E8F0", "#E9EEF4", "#E2E8F0",
            "#FFFFFF", "#E2E8F0", "#1E293B", "#475569", "#64748B"
        ),
        MenuTheme(
            "material_light", "Material Açık",
            "#FFFFFF", "#D9E0EA", "#E5EAF1", "#E8F0FE",
            "#F3F6FA", "#D9E0EA", "#202938", "#334155", "#64748B"
        ),
        MenuTheme(
            "glass", "Glassmorphism",
            "#E8F0F8", "#B8C9DA", "#C8D7E6", "#D1E4F8",
            "#DCE8F4", "#B8C9DA", "#26384D", "#3D6388", "#647D96"
        ),
        MenuTheme(
            "neon", "Neon Modern",
            "#101827", "#168BFF", "#263B58", "#073D75",
            "#172B45", "#168BFF", "#FFFFFF", "#55B7FF", "#A9C8E8"
        )
    )

    private var selectedMenuThemeId = "material_light"

    private fun saveMenuTheme(themeId: String) {
        selectedMenuThemeId = themeId
        getSharedPreferences("inplay_radar_preferences", MODE_PRIVATE)
            .edit()
            .putString("menu_theme_id", themeId)
            .apply()
    }

    private fun cfgColor(value: String): Int =
        try { Color.parseColor(value) } catch (_: Exception) { Color.GRAY }

    private lateinit var drawer: DrawerLayout
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var web: WebView
    private lateinit var zoomPanel: LinearLayout
    private lateinit var drawerPanel: LinearLayout
    private lateinit var contentContainer: FrameLayout
    private lateinit var zoomValue: TextView
    private lateinit var zoomSeekBar: SeekBar
    private val homeUrl = "https://inplayflux.com/live-scanner?page=live&lang=tr"
    private var zoom = 100
    private var customItemHeight = 52
    private var customItemSpacing = 8
    private val zoomHandler = Handler(Looper.getMainLooper())
    private val hideZoomPanel = Runnable {
        if (::zoomPanel.isInitialized) zoomPanel.visibility = View.GONE
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedThemeId = getSharedPreferences(
            "inplay_radar_preferences", MODE_PRIVATE
        ).getString("menu_theme_id", null)

        selectedMenuThemeId = savedThemeId
            ?.takeIf { id -> menuThemes.any { it.id == id } }
            ?: "material_light"

        val menuPreferences = getSharedPreferences(
            "inplay_radar_preferences", MODE_PRIVATE
        )
        customItemHeight = menuPreferences.getInt("custom_item_height", 52)
            .coerceIn(24, 100)
        customItemSpacing = menuPreferences.getInt("custom_item_spacing", 8)
            .coerceIn(0, 20)
        menuConfig = menuConfig.copy(
            itemHeight = customItemHeight,
            itemSpacing = customItemSpacing
        )

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
                        var hideWarningMessage = false;
                        try {
                            var savedHide = localStorage.getItem('ir_hide_warning_message');
                            if (savedHide !== null) {
                                hideWarningMessage = savedHide === 'true';
                            } else {
                                var legacyShow = localStorage.getItem('ir_show_warning_message');
                                if (legacyShow !== null) {
                                    hideWarningMessage = legacyShow !== 'true';
                                    localStorage.setItem('ir_hide_warning_message', hideWarningMessage ? 'true' : 'false');
                                }
                            }
                        } catch (e) {}
                        var css = '.ir-skip-link { display: none !important; }';
                        var targetSelector = 'section#uyariMesaji';
                        var observedTarget = null;

                        function addStyle() {
                            if (!document.documentElement) return false;
                            var style = document.getElementById('irSkipLinkHideStyle');
                            if (!style) {
                                style = document.createElement('style');
                                style.id = 'irSkipLinkHideStyle';
                                document.documentElement.appendChild(style);
                            }
                            var styleText = css + (hideWarningMessage ? ' section#uyariMesaji { display: none !important; }' : '');
                            if (style.textContent !== styleText) {
                                style.textContent = styleText;
                            }
                            return true;
                        }

                        function applyTargetVisibility(element) {
                            if (!element || !element.style) return;
                            var currentDisplay = element.style.getPropertyValue('display');
                            var currentPriority = element.style.getPropertyPriority('display');

                            if (hideWarningMessage) {
                                if (currentDisplay !== 'none' || currentPriority !== 'important') {
                                    element.style.setProperty('display', 'none', 'important');
                                }
                            } else if (currentDisplay === 'none' && currentPriority === 'important') {
                                element.style.removeProperty('display');
                            }
                        }

                        var targetObserver = new MutationObserver(function() {
                            applyTargetVisibility(observedTarget);
                        });

                        function watchTarget(element) {
                            if (!element) return;
                            if (observedTarget !== element) {
                                observedTarget = element;
                                targetObserver.disconnect();
                                targetObserver.observe(element, {
                                    attributes: true,
                                    attributeFilter: ['style', 'class']
                                });
                            }
                            applyTargetVisibility(element);
                        }

                        window.__irSetWarningHidden = function(hide) {
                            hideWarningMessage = !!hide;
                            try { localStorage.setItem('ir_hide_warning_message', hideWarningMessage ? 'true' : 'false'); } catch (e) {}
                            addStyle();
                            var target = document.querySelector(targetSelector);
                            if (target) watchTarget(target);
                        };

                        function inspectNode(node) {
                            if (!node || (node.nodeType !== 1 && node.nodeType !== 11)) return;
                            if (node.nodeType === 1 && node.matches(targetSelector)) {
                                watchTarget(node);
                            }
                            if (node.querySelector) {
                                var found = node.querySelector(targetSelector);
                                if (found) watchTarget(found);
                            }
                        }

                        addStyle();
                        var existingTarget = document.querySelector(targetSelector);
                        if (existingTarget) watchTarget(existingTarget);

                        var documentObserver = new MutationObserver(function(records) {
                            addStyle();
                            records.forEach(function(record) {
                                record.addedNodes.forEach(inspectNode);
                            });
                        });
                        documentObserver.observe(document, {
                            childList: true,
                            subtree: true
                        });
                    })();
                    """.trimIndent(),
                    setOf("*")
                )
            }

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    return request?.let { interceptMobileCardsScript(it) }
                }

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

        contentContainer = FrameLayout(this)
        contentContainer.addView(refresh, FrameLayout.LayoutParams(-1, -1))
        drawer.addView(contentContainer, DrawerLayout.LayoutParams(-1, -1))

        drawerPanel = buildDrawer()
        val drawerParams = DrawerLayout.LayoutParams(
            dp(menuConfig.menuWidth),
            if (menuConfig.menuHeight > 0) dp(menuConfig.menuHeight) else -1
        ).apply {
            gravity = Gravity.START or when (menuConfig.menuVerticalPosition.lowercase()) {
                "center" -> Gravity.CENTER_VERTICAL
                "bottom" -> Gravity.BOTTOM
                else -> Gravity.TOP
            }
        }
        drawer.addView(drawerPanel, drawerParams)

        zoomPanel = buildZoomPanel()
        val zoomPanelParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            setMargins(dp(16), 0, dp(16), dp(32))
        }
        contentContainer.addView(zoomPanel, zoomPanelParams)
        ViewCompat.setOnApplyWindowInsetsListener(contentContainer) { _, insets ->
            val navigationBarHeight =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val params = zoomPanel.layoutParams as FrameLayout.LayoutParams
            params.bottomMargin = navigationBarHeight + dp(32)
            zoomPanel.layoutParams = params
            insets
        }
        ViewCompat.requestApplyInsets(contentContainer)

        setContentView(drawer)
        web.loadUrl(homeUrl)
    }


    private fun interceptMobileCardsScript(
        request: WebResourceRequest
    ): WebResourceResponse? {
        val preferences = getSharedPreferences("inplay_radar_preferences", MODE_PRIVATE)
        if (!preferences.getBoolean("disable_auto_scroll", false)) return null

        val scriptPath = "/radarv8/js/mobileCards.v830454c1a4b8.js"
        if (!request.url.toString().contains(scriptPath)) return null

        return try {
            val connection = URL(request.url.toString()).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 10000
            connection.readTimeout = 15000

            request.requestHeaders.forEach { (name, value) ->
                if (!name.equals("Host", true) &&
                    !name.equals("Connection", true) &&
                    !name.equals("Accept-Encoding", true)
                ) {
                    connection.setRequestProperty(name, value)
                }
            }
            connection.setRequestProperty("Accept-Encoding", "identity")

            val cookie = CookieManager.getInstance().getCookie(request.url.toString())
            if (!cookie.isNullOrEmpty() &&
                request.requestHeaders.keys.none { it.equals("Cookie", true) }
            ) {
                connection.setRequestProperty("Cookie", cookie)
            }

            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                connection.disconnect()
                return null
            }

            val source = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val patchPattern = Regex("(syncSortOptions\\(\\);\\s*)restoreViewportAnchor\\(anchor\\);")
            val matches = patchPattern.findAll(source).count()
            if (matches != 1) {
                connection.disconnect()
                return null
            }

            val patched = patchPattern.replaceFirst(source, "$1")
            val responseHeaders = connection.headerFields
                .filterKeys { it != null }
                .filterKeys {
                    !it.equals("Content-Length", true) &&
                    !it.equals("Content-Encoding", true) &&
                    !it.equals("Transfer-Encoding", true) &&
                    !it.equals("Connection", true)
                }
                .mapKeys { it.key!! }
                .mapValues { (_, values) -> values.joinToString(",") }

            val mimeType = connection.contentType
                ?.substringBefore(";")
                ?: "application/javascript"
            val response = WebResourceResponse(
                mimeType,
                "UTF-8",
                statusCode,
                connection.responseMessage ?: "OK",
                responseHeaders,
                ByteArrayInputStream(patched.toByteArray(Charsets.UTF_8))
            )
            connection.disconnect()
            response
        } catch (_: Exception) {
            null
        }
    }

    private fun applyMenuConfig() {
        if (!::drawerPanel.isInitialized || !::contentContainer.isInitialized ||
            !::zoomPanel.isInitialized
        ) return

        val oldScrollY = (drawerPanel.getChildAt(2) as? ScrollView)?.scrollY ?: 0
        val rebuiltDrawer = buildDrawer()

        drawerPanel.setPadding(
            rebuiltDrawer.paddingLeft,
            rebuiltDrawer.paddingTop,
            rebuiltDrawer.paddingRight,
            rebuiltDrawer.paddingBottom
        )
        drawerPanel.background = rebuiltDrawer.background
        drawerPanel.elevation = rebuiltDrawer.elevation

        drawerPanel.removeAllViews()
        while (rebuiltDrawer.childCount > 0) {
            val child = rebuiltDrawer.getChildAt(0)
            val childParams = child.layoutParams
            rebuiltDrawer.removeViewAt(0)
            drawerPanel.addView(child, childParams)
        }

        drawerPanel.layoutParams = drawerPanel.layoutParams.apply {
            width = dp(menuConfig.menuWidth)
            height = if (menuConfig.menuHeight > 0) dp(menuConfig.menuHeight) else -1
        }

        (drawerPanel.getChildAt(2) as? ScrollView)?.post {
            (drawerPanel.getChildAt(2) as? ScrollView)?.scrollTo(0, oldScrollY)
        }

        val zoomWasVisible = zoomPanel.visibility == View.VISIBLE
        contentContainer.removeView(zoomPanel)
        zoomPanel = buildZoomPanel()
        if (zoomWasVisible) zoomPanel.visibility = View.VISIBLE

        val bottomInset = ViewCompat.getRootWindowInsets(contentContainer)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0

        contentContainer.addView(
            zoomPanel,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                setMargins(dp(16), 0, dp(16), bottomInset + dp(menuConfig.zoomPanelBottom))
            }
        )
    }

    private fun buildDrawer(): LinearLayout {
        val theme = menuThemes.firstOrNull { it.id == selectedMenuThemeId }
            ?: menuThemes.first()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(menuConfig.menuPadding), dp(menuConfig.menuTopPadding), dp(menuConfig.menuPadding), dp(menuConfig.menuBottomPadding))
            background = GradientDrawable().apply {
                setColor(cfgColor(theme.background))
                setStroke(dp(1), cfgColor(theme.border))
                cornerRadius = dp(menuConfig.cornerRadius).toFloat()
            }
            elevation = dp(menuConfig.controlCornerRadius).toFloat()
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), 0, dp(2), 0)
        }

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.ic_radar_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        header.addView(logo, LinearLayout.LayoutParams(dp(menuConfig.logoWidth), dp(menuConfig.logoHeight)).apply {
            leftMargin = dp(menuConfig.logoX)
            topMargin = dp(menuConfig.logoY)
        })

        val title = TextView(this).apply {
            text = "Inplay Radar"
            textSize = menuConfig.headerTitleSize.toFloat()
            setTextColor(cfgColor(theme.text))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, 0, 0)
        }
        header.addView(title, LinearLayout.LayoutParams(0, dp(menuConfig.headerTitleHeight), 1f).apply {
            leftMargin = dp(menuConfig.titleX)
            topMargin = dp(menuConfig.titleY)
        })

        val close = TextView(this).apply {
            text = "×"
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(cfgColor(theme.secondaryText))
            isClickable = true
            isFocusable = true
            setOnClickListener { drawer.closeDrawer(Gravity.START) }
        }
        header.addView(close, LinearLayout.LayoutParams(dp(menuConfig.closeWidth), dp(menuConfig.closeHeight)).apply {
            leftMargin = dp(menuConfig.closeX)
            topMargin = dp(menuConfig.closeY)
        })
        box.addView(header, LinearLayout.LayoutParams(-1, dp(menuConfig.headerHeight)))

        fun divider(): View = View(this).apply {
            setBackgroundColor(cfgColor(theme.divider))
        }

        box.addView(divider(), LinearLayout.LayoutParams(-1, dp(1)).apply {
            bottomMargin = dp(8)
        })

        val scrollView = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }

        val itemsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(
            itemsContainer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        box.addView(scrollView, LinearLayout.LayoutParams(-1, 0, 1f))

        val menuRows = linkedMapOf<String, LinearLayout>()
        var selectedMenu = "Ana Sayfa"

        fun itemBackground(selected: Boolean): GradientDrawable =
            GradientDrawable().apply {
                setColor(
                    if (selected) cfgColor(theme.selected) else Color.TRANSPARENT
                )
                cornerRadius = dp(menuConfig.itemCornerRadius).toFloat()
            }

        fun updateSelection(label: String) {
            selectedMenu = label
            menuRows.forEach { (name, row) ->
                row.background = itemBackground(name == selectedMenu)
            }
        }

        fun item(label: String, icon: String, action: () -> Unit) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(menuConfig.itemPadding), 0, dp(menuConfig.itemPadding), 0)
                background = itemBackground(label == selectedMenu)
                isClickable = true
                isFocusable = true
            }

            val iconView = TextView(this).apply {
                text = icon
                textSize = menuConfig.iconSize.toFloat()
                gravity = Gravity.CENTER
                setTextColor(cfgColor(theme.icon))
            }
            row.addView(iconView, LinearLayout.LayoutParams(dp(menuConfig.iconAreaWidth), -1).apply {
                leftMargin = dp(menuConfig.menuIconX)
                topMargin = dp(menuConfig.menuIconY)
            })

            val labelView = TextView(this).apply {
                text = label
                textSize = menuConfig.fontSize.toFloat()
                setTextColor(cfgColor(theme.text))
                setPadding(dp(menuConfig.itemTextPadding), 0, 0, 0)
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
            }
            row.addView(labelView, LinearLayout.LayoutParams(0, -1, 1f).apply {
                leftMargin = dp(menuConfig.menuTextX)
                topMargin = dp(menuConfig.menuTextY)
            })

            row.setOnClickListener {
                updateSelection(label)
                action()
                if (label != "Ayarlar") {
                    drawer.closeDrawer(Gravity.START)
                }
            }

            menuRows[label] = row
            itemsContainer.addView(
                row,
                LinearLayout.LayoutParams(-1, dp(menuConfig.itemHeight)).apply {
                    bottomMargin = dp(menuConfig.itemSpacing)
                }
            )
        }

        item("Ana Sayfa", "⌂") { web.loadUrl(homeUrl) }
        item("Canlı", "◉") {
            web.loadUrl("https://inplayflux.com/live-scanner?page=live")
        }
        item("Bitmiş", "✓") {
            web.loadUrl("https://inplayflux.com/live-scanner?page=finished")
        }
        item("Favorilerim", "☆") {
            web.loadUrl("https://inplayflux.com/live-scanner?page=favorim&lang=tr")
        }
        item("Stratejilerim", "⚙") {
            web.loadUrl("https://inplayflux.com/stratejiler")
        }
        item("Sinyaller", "⌁") {
            web.loadUrl("https://inplayflux.com/stratejiler/sinyaller")
        }
        item("SSS", "?") {
            web.loadUrl("https://inplayflux.com/page/how-to-use")
        }
        item("Ayarlar", "⚙") {
            AlertDialog.Builder(this)
                .setTitle("Ayarlar")
                .setItems(arrayOf("Menüyü Özelleştir", "İçerik Ayarları")) { _, which ->
                    when (which) {
                        0 -> showMenuCustomizationPanel()
                        1 -> showContentSettingsPanel()
                    }
                }
                .setNegativeButton("Kapat", null)
                .show()
        }
        item("Hakkında", "ⓘ") {
            AlertDialog.Builder(this)
                .setTitle("Inplay Radar")
                .setMessage("Sürüm 1.0.0")
                .setPositiveButton("Tamam", null)
                .show()
        }

        itemsContainer.addView(divider(), LinearLayout.LayoutParams(-1, dp(1)).apply {
            topMargin = dp(8)
            bottomMargin = dp(8)
        })

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        fun control(label: String, icon: String, action: () -> Unit) {
            val button = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(menuConfig.controlPadding), dp(4), dp(menuConfig.controlPadding))
                background = GradientDrawable().apply {
                    setColor(cfgColor(theme.controlBackground))
                    setStroke(dp(1), cfgColor(theme.controlBorder))
                    cornerRadius = dp(12).toFloat()
                }
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    action()
                    drawer.closeDrawer(Gravity.START)
                }
            }

            val iconView = TextView(this).apply {
                text = icon
                textSize = menuConfig.controlIconSize.toFloat()
                gravity = Gravity.CENTER
                setTextColor(cfgColor(theme.icon))
            }
            button.addView(iconView, LinearLayout.LayoutParams(-1, dp(menuConfig.controlIconHeight)).apply {
                leftMargin = dp(menuConfig.controlIconX)
                topMargin = dp(menuConfig.controlIconY)
            })

            val labelView = TextView(this).apply {
                text = label
                textSize = menuConfig.controlLabelSize.toFloat()
                gravity = Gravity.CENTER
                setTextColor(cfgColor(theme.text))
                maxLines = 1
            }
            button.addView(labelView, LinearLayout.LayoutParams(-1, dp(menuConfig.controlLabelHeight)).apply {
                leftMargin = dp(menuConfig.controlTextX)
                topMargin = dp(menuConfig.controlTextY)
            })

            controls.addView(
                button,
                LinearLayout.LayoutParams(0, dp(menuConfig.controlHeight), 1f).apply {
                    leftMargin = dp(menuConfig.controlButtonX + if (controls.childCount > 0) 8 else 0)
                    topMargin = dp(menuConfig.controlButtonY)
                }
            )
        }

        control("Yakınlaştır", "⌕") { showZoomPanel() }
        control("Tam Ekran Yap", "⛶") {
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

        itemsContainer.addView(controls, LinearLayout.LayoutParams(-1, dp(menuConfig.controlHeight)).apply {
            topMargin = dp(menuConfig.controlTopMargin)
            bottomMargin = dp(menuConfig.controlBottomMargin)
        })
        return box
    }

    private fun updateCustomMenuItems() {
        getSharedPreferences("inplay_radar_preferences", MODE_PRIVATE)
            .edit()
            .putInt("custom_item_height", customItemHeight)
            .putInt("custom_item_spacing", customItemSpacing)
            .apply()

        menuConfig = menuConfig.copy(
            itemHeight = customItemHeight,
            itemSpacing = customItemSpacing
        )
        applyMenuConfig()
    }

    private fun showContentSettingsPanel() {
        val preferences = getSharedPreferences("inplay_radar_preferences", MODE_PRIVATE)
        var changed = false

        if (!preferences.contains("hide_warning_message") &&
            preferences.contains("show_warning_message")
        ) {
            preferences.edit()
                .putBoolean(
                    "hide_warning_message",
                    !preferences.getBoolean("show_warning_message", false)
                )
                .apply()
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }

        fun addSetting(
            title: String,
            description: String,
            preferenceKey: String,
            defaultValue: Boolean
        ) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val textContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            val titleView = TextView(this).apply {
                text = title
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.rgb(30, 41, 59))
            }
            textContainer.addView(titleView)

            val descriptionView = TextView(this).apply {
                text = description
                textSize = 12f
                setTextColor(Color.rgb(100, 116, 139))
            }
            textContainer.addView(
                descriptionView,
                LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) }
            )

            val toggle = SwitchCompat(this).apply {
                isChecked = preferences.getBoolean(preferenceKey, defaultValue)
                setOnCheckedChangeListener { _, isChecked ->
                    preferences.edit().putBoolean(preferenceKey, isChecked).apply()
                    if (preferenceKey == "hide_warning_message" && ::web.isInitialized) {
                        web.evaluateJavascript("window.__irSetWarningHidden($isChecked)", null)
                    }
                    changed = true
                }
            }

            row.addView(textContainer, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(toggle, LinearLayout.LayoutParams(-2, -2).apply {
                leftMargin = dp(12)
            })
            panel.addView(row, LinearLayout.LayoutParams(-1, -2))
        }

        addSetting(
            "Otomatik Kaydırmayı Devre Dışı Bırak",
            "Sıralama değişirken sayfanın konumu korunur.",
            "disable_auto_scroll",
            false
        )

        val divider = View(this).apply {
            setBackgroundColor(Color.rgb(225, 231, 239))
        }
        panel.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).apply {
            topMargin = dp(16)
            bottomMargin = dp(16)
        })

        addSetting(
            "Uyarı Mesajını Gizle",
            "Açıkken uyarı mesajı gizlenir.",
            "hide_warning_message",
            false
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("İçerik Ayarları")
            .setView(panel)
            .setPositiveButton("Kapat", null)
            .create()

        dialog.setOnDismissListener {
            if (changed && ::web.isInitialized) web.reload()
        }
        dialog.show()
    }

    private fun showMenuCustomizationPanel() {
        customItemHeight = menuConfig.itemHeight
        customItemSpacing = menuConfig.itemSpacing

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.rgb(248, 250, 253))
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), Color.rgb(218, 226, 237))
            }
        }

        val title = TextView(this).apply {
            text = "Menüyü Özelleştir"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(30, 41, 59))
        }
        panel.addView(title, LinearLayout.LayoutParams(-1, dp(46)))

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(10))
        }
        panel.addView(tabs, LinearLayout.LayoutParams(-1, dp(54)))

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        panel.addView(
            content,
            LinearLayout.LayoutParams(-1, -2)
        )

        val dialog = AlertDialog.Builder(this)
            .setView(panel)
            .create()

        fun tabButton(label: String, selected: Boolean, action: () -> Unit): TextView {
            return TextView(this).apply {
                text = label
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(
                    if (selected) Color.WHITE else Color.rgb(71, 85, 105)
                )
                background = GradientDrawable().apply {
                    setColor(
                        if (selected) Color.rgb(45, 105, 190)
                        else Color.rgb(235, 240, 247)
                    )
                    cornerRadius = dp(11).toFloat()
                }
                isClickable = true
                isFocusable = true
                setOnClickListener { action() }
            }
        }

        var showMeasurements: () -> Unit = {}
        var showThemes: () -> Unit = {}

        fun renderTabs(selectedTab: String) {
            tabs.removeAllViews()

            tabs.addView(
                tabButton("Menü Ölçüleri", selectedTab == "sizes", showMeasurements),
                LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                    rightMargin = dp(5)
                }
            )
            tabs.addView(
                tabButton("Görünüm", selectedTab == "themes", showThemes),
                LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                    leftMargin = dp(5)
                }
            )
        }

        showMeasurements = {
            renderTabs("sizes")
            content.removeAllViews()

            fun sizeCard(
                label: String,
                minimum: Int,
                maximum: Int,
                getValue: () -> Int,
                change: (Int) -> Unit
            ): LinearLayout {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(13), dp(10), dp(13), dp(10))
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        cornerRadius = dp(14).toFloat()
                        setStroke(dp(1), Color.rgb(222, 229, 238))
                    }
                }

                val heading = TextView(this).apply {
                    text = label
                    textSize = 15f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.rgb(38, 50, 68))
                }
                card.addView(heading)

                val valueText = TextView(this).apply {
                    text = "${getValue()} dp"
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(Color.rgb(45, 105, 190))
                }
                card.addView(valueText, LinearLayout.LayoutParams(-1, dp(34)))

                val slider = SeekBar(this).apply {
                    max = maximum - minimum
                    progress = (getValue() - minimum).coerceIn(0, max)
                }
                card.addView(slider, LinearLayout.LayoutParams(-1, dp(38)))

                val buttons = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                }

                fun stepButton(symbol: String, amount: Int): TextView =
                    TextView(this).apply {
                        text = symbol
                        textSize = 22f
                        gravity = Gravity.CENTER
                        setTextColor(Color.WHITE)
                        background = GradientDrawable().apply {
                            setColor(Color.rgb(45, 105, 190))
                            cornerRadius = dp(9).toFloat()
                        }
                        isClickable = true
                        isFocusable = true
                        setOnClickListener {
                            val newProgress =
                                (slider.progress + amount).coerceIn(0, slider.max)
                            slider.progress = newProgress
                            change(minimum + newProgress)
                            valueText.text = "${getValue()} dp"
                        }
                    }

                buttons.addView(
                    stepButton("−", -1),
                    LinearLayout.LayoutParams(dp(48), dp(38)).apply {
                        rightMargin = dp(12)
                    }
                )
                buttons.addView(
                    stepButton("+", 1),
                    LinearLayout.LayoutParams(dp(48), dp(38))
                )
                card.addView(buttons)

                slider.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (!fromUser) return
                            change(minimum + progress)
                            valueText.text = "${getValue()} dp"
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                    }
                )

                return card
            }

            content.addView(
                sizeCard(
                    "Satır Yüksekliği", 24, 100,
                    { customItemHeight },
                    {
                        customItemHeight = it
                        updateCustomMenuItems()
                    }
                ),
                LinearLayout.LayoutParams(-1, dp(145)).apply {
                    bottomMargin = dp(10)
                }
            )

            content.addView(
                sizeCard(
                    "Satır Aralığı", 0, 20,
                    { customItemSpacing },
                    {
                        customItemSpacing = it
                        updateCustomMenuItems()
                    }
                ),
                LinearLayout.LayoutParams(-1, dp(145))
            )

            val reset = TextView(this).apply {
                text = "Ölçüleri Sıfırla"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    setColor(Color.rgb(45, 105, 190))
                    cornerRadius = dp(11).toFloat()
                }
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    customItemHeight = 52
                    customItemSpacing = 8
                    updateCustomMenuItems()
                    showMeasurements()
                }
            }
            content.addView(
                reset,
                LinearLayout.LayoutParams(-1, dp(42)).apply {
                    topMargin = dp(12)
                }
            )
        }

        showThemes = {
            renderTabs("themes")
            content.removeAllViews()

            val description = TextView(this).apply {
                text = "Menünün görünümünü seç. Seçimin cihazında saklanır."
                textSize = 12f
                setTextColor(Color.rgb(100, 116, 139))
                gravity = Gravity.CENTER
            }
            content.addView(description, LinearLayout.LayoutParams(-1, dp(34)))

            menuThemes.forEach { theme ->
                val selected = theme.id == selectedMenuThemeId

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(12), dp(9), dp(12), dp(9))
                    background = GradientDrawable().apply {
                        setColor(cfgColor(theme.background))
                        cornerRadius = dp(12).toFloat()
                        setStroke(
                            dp(if (selected) 2 else 1),
                            cfgColor(if (selected) theme.selected else theme.border)
                        )
                    }
                    isClickable = true
                    isFocusable = true
                }

                val preview = View(this).apply {
                    background = GradientDrawable().apply {
                        setColor(cfgColor(theme.selected))
                        cornerRadius = dp(7).toFloat()
                        setStroke(dp(1), cfgColor(theme.border))
                    }
                }
                card.addView(
                    preview,
                    LinearLayout.LayoutParams(dp(42), dp(34)).apply {
                        rightMargin = dp(12)
                    }
                )

                val name = TextView(this).apply {
                    text = theme.name
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(cfgColor(theme.text))
                }
                card.addView(name, LinearLayout.LayoutParams(0, -2, 1f))

                val mark = TextView(this).apply {
                    text = if (selected) "✓" else ""
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(cfgColor(theme.icon))
                }
                card.addView(mark, LinearLayout.LayoutParams(dp(28), dp(34)))

                card.setOnClickListener {
                    saveMenuTheme(theme.id)
                    applyMenuConfig()
                    showThemes()
                }

                content.addView(
                    card,
                    LinearLayout.LayoutParams(-1, dp(56)).apply {
                        bottomMargin = dp(7)
                    }
                )
            }
        }


        showMeasurements()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(dp(340), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun buildZoomPanel(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(menuConfig.zoomPanelPadding), 0, dp(menuConfig.zoomPanelPadding), 0)
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
        }

        val pillColor = cfgColor(menuConfig.zoomBackground)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(pillColor)
                cornerRadius = dp(menuConfig.cornerRadius + 22).toFloat()
            }
            elevation = dp(8).toFloat()
        }

        zoomValue = TextView(this).apply {
            text = "$zoom%"
            textSize = menuConfig.zoomValueTextSize.toFloat()
            gravity = Gravity.CENTER
            setTextColor(cfgColor(menuConfig.zoomTextColor))
            setTypeface(null, Typeface.NORMAL)
        }
        header.addView(zoomValue, LinearLayout.LayoutParams(dp(menuConfig.zoomValueWidth), dp(menuConfig.zoomHeaderHeight)))

        val divider = View(this).apply {
            setBackgroundColor(cfgColor(menuConfig.zoomDivider))
        }
        header.addView(divider, LinearLayout.LayoutParams(dp(1), dp(menuConfig.zoomHeaderHeight)))

        val reset = TextView(this).apply {
            text = "Sıfırla"
            textSize = menuConfig.zoomResetTextSize.toFloat()
            gravity = Gravity.CENTER
            setTextColor(cfgColor(menuConfig.zoomResetColor))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                setZoom(100)
                scheduleZoomPanelHide()
            }
        }
        header.addView(reset, LinearLayout.LayoutParams(dp(menuConfig.zoomResetWidth), dp(menuConfig.zoomHeaderHeight)))
        panel.addView(
            header,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(menuConfig.zoomHeaderHeight)).apply {
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
                textSize = menuConfig.zoomButtonTextSize.toFloat()
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    setZoom((zoom + change).coerceIn(40, 200))
                    scheduleZoomPanelHide()
                }
            }

        controls.addView(zoomButton("−", -10), LinearLayout.LayoutParams(dp(menuConfig.zoomMinusWidth), dp(menuConfig.zoomControlHeight)))

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
        controls.addView(zoomSeekBar, LinearLayout.LayoutParams(0, dp(menuConfig.zoomControlHeight), 1f))
        controls.addView(zoomButton("+", 10), LinearLayout.LayoutParams(dp(menuConfig.zoomPlusWidth), dp(menuConfig.zoomControlHeight + 6)))
        panel.addView(
            controls,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(menuConfig.zoomControlHeight)).apply {
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
        zoomHandler.postDelayed(hideZoomPanel, 5000)
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
