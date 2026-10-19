package com.generated.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private List<SiteModel> sites;
    private FrameLayout webViewContainer;
    private List<WebView> webViews;
    private List<LinearLayout> tabViews;
    private LinearLayout tabsLayout;
    private HorizontalScrollView tabsScroll;
    private LinearLayout topToolbar;
    
    private EditText urlInput;
    private ProgressBar progressBar;
    private ImageButton btnBack, btnForward, btnRefresh, btnSettings;

    private int currentIndex = 0;
    private boolean isToolbarVisible = true;
    private float lastScrollY = 0;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "BrowserPrefs";
    private static final String KEY_DESKTOP_MODE = "desktop_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        initSites();
        buildUI();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String url = data.toString();
                if (webViews != null && !webViews.isEmpty()) {
                    webViews.get(currentIndex).loadUrl(url);
                }
            }
        }
    }

    private void initSites() {
        sites = new ArrayList<>();
        sites.add(new SiteModel("מתמחים", "https://mitmachim.top"));
        sites.add(new SiteModel("OgenPlay", "https://ogenplay.com"));
        sites.add(new SiteModel("App Factory", "https://app-factory-nt5l.onrender.com"));
        sites.add(new SiteModel("JTech Forums", "https://jtechforums.org"));
        sites.add(new SiteModel("גיטהוב", "https://github.com"));
        sites.add(new SiteModel("Nigun", "https://nigun.pages.dev"));
        sites.add(new SiteModel("גוגל", "https://www.google.com"));
    }

    private void buildUI() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(0xFFFAFAFA);

        // Top Toolbar Container
        topToolbar = new LinearLayout(this);
        topToolbar.setOrientation(LinearLayout.VERTICAL);
        topToolbar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        topToolbar.setBackgroundResource(android.R.drawable.screen_background_light_transparent);

        // 1. Tabs Bar
        tabsScroll = new HorizontalScrollView(this);
        tabsScroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabsScroll.setHorizontalScrollBarEnabled(false);
        tabsScroll.setPadding(8, 8, 8, 8);
        tabsScroll.setBackgroundColor(0xFFEEEEEE);

        tabsLayout = new LinearLayout(this);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabsScroll.addView(tabsLayout);
        topToolbar.addView(tabsScroll);

        // 2. Navigation & Address Bar
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 56));
        navBar.setGravity(Gravity.CENTER_VERTICAL);
        navBar.setPadding(12, 4, 12, 4);
        navBar.setBackgroundColor(0xFFFFFFFF);

        btnBack = createToolbarButton(android.R.drawable.ic_menu_revert, getString(R.string.back));
        btnForward = createToolbarButton(android.R.drawable.ic_menu_send, getString(R.string.forward));
        btnRefresh = createToolbarButton(android.R.drawable.ic_menu_rotate, getString(R.string.refresh));

        btnBack.setOnClickListener(v -> {
            WebView wv = webViews.get(currentIndex);
            if (wv.canGoBack()) wv.goBack();
        });
        btnForward.setOnClickListener(v -> {
            WebView wv = webViews.get(currentIndex);
            if (wv.canGoForward()) wv.goForward();
        });
        btnRefresh.setOnClickListener(v -> webViews.get(currentIndex).reload());

        // URL Input Bar
        urlInput = new EditText(this);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
                0, 40, 1f);
        urlParams.setMargins(8, 0, 8, 0);
        urlInput.setLayoutParams(urlParams);
        urlInput.setHint(R.string.url_hint);
        urlInput.setTextSize(14);
        urlInput.setSingleLine(true);
        urlInput.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlInput.setPadding(24, 0, 24, 0);
        urlInput.setBackgroundResource(R.drawable.bg_search_bar);

        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = urlInput.getText().toString().trim();
                if (!query.isEmpty()) {
                    String targetUrl;
                    if (query.startsWith("http://") || query.startsWith("https://")) {
                        targetUrl = query;
                    } else if (query.contains(".") && !query.contains(" ")) {
                        targetUrl = "https://" + query;
                    } else {
                        targetUrl = "https://www.google.com/search?q=" + Uri.encode(query);
                    }
                    webViews.get(currentIndex).loadUrl(targetUrl);
                }
                return true;
            }
            return false;
        });

        btnSettings = createToolbarButton(android.R.drawable.ic_menu_manage, getString(R.string.settings));
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        navBar.addView(btnBack);
        navBar.addView(btnForward);
        navBar.addView(btnRefresh);
        navBar.addView(urlInput);
        navBar.addView(btnSettings);

        topToolbar.addView(navBar);

        // Progress Bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 6));
        progressBar.setMax(100);
        progressBar.setProgress(0);
        topToolbar.addView(progressBar);

        rootLayout.addView(topToolbar);

        // WebView Container
        webViewContainer = new FrameLayout(this);
        webViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        rootLayout.addView(webViewContainer);

        setContentView(rootLayout);

        // Initialize WebViews and Tabs
        initWebViewsAndTabs();
    }

    private ImageButton createToolbarButton(int iconRes, String contentDesc) {
        ImageButton btn = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
        params.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(params);
        btn.setImageResource(iconRes);
        btn.setContentDescription(contentDesc);
        btn.setBackgroundColor(0x00000000);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return btn;
    }

    private void initWebViewsAndTabs() {
        webViews = new ArrayList<>();
        tabViews = new ArrayList<>();

        boolean desktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false);

        for (int i = 0; i < sites.size(); i++) {
            SiteModel site = sites.get(i);

            // Create WebView
            WebView wv = new WebView(this);
            wv.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            
            WebSettings settings = wv.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            
            // Ultra-fast loading configurations
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            settings.setAppCacheEnabled(true);
            settings.setLoadsImagesAutomatically(true);
            settings.setBlockNetworkImage(false);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

            if (desktopMode) {
                settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            }

            final int index = i;
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        view.loadUrl(url);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if (index == currentIndex) {
                        urlInput.setText(url);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // Speed up future loads by enabling default cache mode after first load
                    view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                    if (index == currentIndex) {
                        urlInput.setText(url);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

            wv.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (index == currentIndex) {
                        progressBar.setProgress(newProgress);
                        if (newProgress == 100) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            // Scroll listener for hiding/showing top toolbar and pull-to-refresh
            wv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (index != currentIndex) return;

                // Pull to refresh trigger: when scrolled to the very top and pulling further down
                if (scrollY <= 0 && oldScrollY <= 0) {
                    if (v.getScrollY() < -30) {
                        wv.reload();
                    }
                }

                float diff = scrollY - lastScrollY;
                if (diff > 15 && isToolbarVisible && scrollY > 100) {
                    hideToolbar();
                } else if (diff < -15 && !isToolbarVisible) {
                    showToolbar();
                }
                if (scrollY > 10 || scrollY < oldScrollY) {
                    lastScrollY = scrollY;
                }
            });

            wv.loadUrl(site.getUrl());
            webViews.add(wv);
            webViewContainer.addView(wv);
            if (i != 0) {
                wv.setVisibility(View.GONE);
            }

            // Create Tab View
            LinearLayout tabView = new LinearLayout(this);
            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, 40);
            tabParams.setMargins(4, 4, 4, 4);
            tabView.setLayoutParams(tabParams);
            tabView.setGravity(Gravity.CENTER);
            tabView.setPadding(16, 0, 16, 0);
            tabView.setBackgroundResource(R.drawable.bg_tab);
            tabView.setSelected(i == 0);

            TextView tvTitle = new TextView(this);
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvTitle.setText(site.getTitle());
            tvTitle.setTextSize(13);
            tvTitle.setTextColor(0xFF333333);

            tabView.addView(tvTitle);
            tabView.setOnClickListener(v -> switchTab(index));

            tabsLayout.addView(tabView);
            tabViews.add(tabView);
        }
    }

    private void switchTab(int index) {
        if (index < 0 || index >= sites.size()) return;
        
        webViews.get(currentIndex).setVisibility(View.GONE);
        tabViews.get(currentIndex).setSelected(false);

        currentIndex = index;

        WebView currentWv = webViews.get(currentIndex);
        currentWv.setVisibility(View.VISIBLE);
        tabViews.get(currentIndex).setSelected(true);
        urlInput.setText(currentWv.getUrl() != null ? currentWv.getUrl() : sites.get(currentIndex).getUrl());

        final LinearLayout selectedTab = tabViews.get(currentIndex);
        tabsScroll.smoothScrollTo(selectedTab.getLeft() - 100, 0);
        showToolbar();
    }

    private void hideToolbar() {
        isToolbarVisible = false;
        topToolbar.animate().translationY(-topToolbar.getHeight()).setDuration(200).start();
    }

    private void showToolbar() {
        isToolbarVisible = true;
        topToolbar.animate().translationY(0).setDuration(200).start();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.settings_title);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 24);

        Button btnClear = new Button(this);
        btnClear.setText(R.string.clear_cache);
        btnClear.setOnClickListener(v -> {
            CookieManager.getInstance().removeAllCookies(null);
            for (WebView wv : webViews) {
                wv.clearCache(true);
                wv.clearHistory();
            }
            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnClear);

        TextView tvLinkInfo = new TextView(this);
        tvLinkInfo.setText(R.string.link_handling_desc);
        tvLinkInfo.setTextSize(13);
        tvLinkInfo.setTextColor(0xFF666666);
        tvLinkInfo.setPadding(0, 24, 0, 0);
        layout.addView(tvLinkInfo);

        builder.setView(layout);
        builder.setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onBackPressed() {
        WebView wv = webViews.get(currentIndex);
        if (wv.canGoBack()) {
            wv.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
