package com.detectorpro.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String HOME = "https://www.google.com/";
    private WebView webView;
    private EditText addressBar;
    private LinearLayout detections;
    private TextView status;
    private final Set<String> found = new LinkedHashSet<>();
    private int ink = Color.rgb(28, 36, 48);
    private int muted = Color.rgb(105, 116, 130);
    private int blue = Color.rgb(38, 105, 220);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
        clearPrivateData();
        webView.loadUrl(HOME);
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(248, 250, 253));
        root.setPadding(dp(14), dp(12), dp(14), dp(8));

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView brand = text("DETECTOR PRO", 19, ink);
        brand.setTypeface(null, 1);
        heading.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView privateTag = text("● PRIVATE", 11, Color.rgb(25, 135, 84));
        privateTag.setTypeface(null, 1);
        heading.addView(privateTag);
        root.addView(heading);

        status = text("Private session · session data cleared when the app closes", 11, muted);
        status.setPadding(0, dp(4), 0, dp(10));
        root.addView(status);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(android.view.Gravity.CENTER_VERTICAL);
        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextSize(14);
        addressBar.setHint("Search Google or enter a link");
        addressBar.setText(HOME);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressBar.setSelectAllOnFocus(true);
        addressBar.setPadding(dp(12), 0, dp(12), 0);
        addressBar.setBackgroundColor(Color.WHITE);
        nav.addView(addressBar, new LinearLayout.LayoutParams(0, dp(46), 1));
        Button go = button("Go");
        nav.addView(go, new LinearLayout.LayoutParams(dp(58), dp(46)));
        root.addView(nav);
        go.setOnClickListener(v -> navigate());
        addressBar.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_GO) { navigate(); return true; }
            return false;
        });

        LinearLayout controls = new LinearLayout(this);
        controls.setPadding(0, dp(8), 0, dp(8));
        Button back = button("‹");
        Button forward = button("›");
        Button reload = button("↻");
        Button home = button("Home");
        controls.addView(back, new LinearLayout.LayoutParams(0, dp(40), 1));
        controls.addView(forward, new LinearLayout.LayoutParams(0, dp(40), 1));
        controls.addView(reload, new LinearLayout.LayoutParams(0, dp(40), 1));
        controls.addView(home, new LinearLayout.LayoutParams(0, dp(40), 1));
        root.addView(controls);
        back.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        forward.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        reload.setOnClickListener(v -> webView.reload());
        home.setOnClickListener(v -> webView.loadUrl(HOME));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        CookieManager cookies = CookieManager.getInstance();
        // Session-only cookies help official sites such as YouTube work.
        // Cookies and WebView data are cleared when the app closes.
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new PageMediaBridge(), "DetectorProBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                return !"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (looksLikeVideo(url)) runOnUiThread(() -> addDetected(url));
                return super.shouldInterceptRequest(view, request);
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                addressBar.setText(url);
                found.clear();
                detections.removeAllViews();
            }
            @Override public void onPageFinished(WebView view, String url) {
                addressBar.setText(url);
                if (isYouTubeUrl(url)) {
                    status.setText("YouTube · official website playback · downloads unavailable here");
                } else {
                    status.setText("Private session · supported direct video links can be detected");
                }
                scanPageVideos();
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (isYouTubeUrl(url)) {
                Toast.makeText(this, "YouTube playback is supported on its official website; downloading is not available.", Toast.LENGTH_LONG).show();
            } else if (isHttp(url) && (looksLikeVideo(url) || (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("video/")))) {
                addDetected(url);
                startDownload(url, userAgent, contentDisposition, mimeType);
            } else {
                Toast.makeText(this, "This link is not recognised as a supported video file.", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(8), dp(10), dp(6));
        panel.setBackgroundColor(Color.WHITE);
        TextView title = text("Detected videos", 15, ink);
        title.setTypeface(null, 1);
        panel.addView(title);
        TextView hint = text("Embedded players are recognised; only supported direct files can be downloaded", 11, muted);
        panel.addView(hint);
        ScrollView scroll = new ScrollView(this);
        detections = new LinearLayout(this);
        detections.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(detections);
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, dp(92)));
        root.addView(panel, new LinearLayout.LayoutParams(-1, dp(150)));
        setContentView(root);
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(12); b.setAllCaps(false);
        b.setTextColor(ink);
        return b;
    }

    private void navigate() {
        String value = addressBar.getText().toString().trim();
        if (value.isEmpty()) return;
        String target;
        if (value.matches("(?i)^https?://.*")) target = value;
        else if (value.contains(".") && !value.contains(" ")) target = "https://" + value;
        else target = "https://www.google.com/search?q=" + Uri.encode(value);
        if (isHttp(target)) webView.loadUrl(target);
        else Toast.makeText(this, "Only regular web addresses are supported.", Toast.LENGTH_SHORT).show();
    }

    private boolean isHttp(String value) {
        if (value == null) return false;
        String s = value.toLowerCase(Locale.ROOT);
        return s.startsWith("https://") || s.startsWith("http://");
    }

    private boolean isYouTubeUrl(String value) {
        if (value == null) return false;
        try {
            String host = Uri.parse(value).getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.ROOT);
            return host.equals("youtube.com") || host.endsWith(".youtube.com")
                    || host.equals("youtu.be") || host.endsWith(".youtu.be");
        } catch (Exception e) { return false; }
    }

    private boolean looksLikeVideo(String value) {
        try {
            String path = Uri.parse(value).getPath();
            if (path == null) path = value;
            path = URLDecoder.decode(path, "UTF-8").toLowerCase(Locale.ROOT);
            return path.endsWith(".mp4") || path.endsWith(".webm") ||
                   path.endsWith(".m4v") || path.endsWith(".3gp") ||
                   path.endsWith(".mov");
        } catch (Exception e) { return false; }
    }

    private void scanPageVideos() {
        String js = "(function(){"
            + "function scan(){"
            + "var vids=document.querySelectorAll('video');"
            + "if(vids.length && window.DetectorProBridge){DetectorProBridge.playerFound(vids.length);}"
            + "var a=[];"
            + "vids.forEach(function(v){"
            + "if(v.currentSrc)a.push(v.currentSrc);"
            + "if(v.src)a.push(v.src);"
            + "v.querySelectorAll('source').forEach(function(s){if(s.src)a.push(s.src);});"
            + "});"
            + "document.querySelectorAll('a[href]').forEach(function(x){"
            + "if(/\\\\.(mp4|webm|m4v|3gp|mov)(\\\\?|$)/i.test(x.href))a.push(x.href);"
            + "});"
            + "a.forEach(function(u){if(/^https?:/i.test(u)&&window.DetectorProBridge)DetectorProBridge.found(u);});"
            + "}"
            + "scan();"
            + "if(!window.__detectorProObserver){"
            + "window.__detectorProObserver=new MutationObserver(function(){scan();});"
            + "window.__detectorProObserver.observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['src']});"
            + "}"
            + "document.querySelectorAll('video').forEach(function(v){v.addEventListener('loadedmetadata',scan);v.addEventListener('play',scan);});"
            + "})();";
        webView.evaluateJavascript(js, null);
    }

    private class PageMediaBridge {
        @JavascriptInterface public void found(String url) {
            if (isHttp(url) && looksLikeVideo(url) && !isYouTubeUrl(url))
                runOnUiThread(() -> addDetected(url));
        }
        @JavascriptInterface public void playerFound(int count) {
            runOnUiThread(() -> {
                if (status != null && webView != null && !isYouTubeUrl(webView.getUrl())) {
                    status.setText("Embedded player detected · direct downloadable links appear below when available");
                }
            });
        }
    }

    private void addDetected(String url) {
        // YouTube pages are for official in-browser playback only. Do not treat
        // YouTube page or media requests as downloadable files.
        if (isYouTubeUrl(url)) return;
        if (!found.add(url)) return;
        TextView item = text("▶  " + Uri.parse(url).getLastPathSegment(), 12, ink);
        item.setPadding(0, dp(7), 0, dp(7));
        item.setOnClickListener(v -> startDownload(url, null, null, "video/*"));
        detections.addView(item);
    }

    private void startDownload(String url, String userAgent, String disposition, String mime) {
        if (isYouTubeUrl(url)) {
            Toast.makeText(this, "YouTube playback is supported in the browser; downloading is not provided.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isHttp(url) || !looksLikeVideo(url)) {
            Toast.makeText(this, "Only supported direct video files can be downloaded.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Detector Pro video");
            request.setDescription("Downloading a video file");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            if (mime != null && mime.startsWith("video/")) request.setMimeType(mime);
            if (userAgent != null) request.addRequestHeader("User-Agent", userAgent);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DetectorPro_" + System.currentTimeMillis() + extensionFor(url));
            manager.enqueue(request);
            Toast.makeText(this, "Download added to Android Downloads.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not start download. The site may require a supported direct link.", Toast.LENGTH_LONG).show();
        }
    }

    private String extensionFor(String url) {
        String p = Uri.parse(url).getLastPathSegment();
        if (p != null) {
            int dot = p.lastIndexOf('.');
            if (dot >= 0 && p.length() - dot <= 6) return p.substring(dot);
        }
        return ".mp4";
    }

    private void clearPrivateData() {
        try {
            CookieManager cm = CookieManager.getInstance();
            cm.removeAllCookies(null);
            cm.flush();
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();
        } catch (Exception ignored) {}
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        clearPrivateData();
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();
    }
}
