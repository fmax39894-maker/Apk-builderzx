package com.detectorpro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String HOME = "https://www.google.com/";
    private static final int BLACK = Color.rgb(12,12,12);
    private static final int WHITE = Color.WHITE;
    private static final int GRAY = Color.rgb(125,125,125);
    private WebView webView;
    private EditText addressBar;
    private LinearLayout detectedList;
    private LinearLayout content;
    private TextView status;
    private Button homeTab, downloadsTab;
    private final Set<String> found = new LinkedHashSet<>();
    private boolean showingDownloads = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
        clearPrivateData();
        webView.loadUrl(HOME);
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView label(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private Button actionButton(String value, boolean filled) {
        Button b = new Button(this); b.setText(value); b.setTextSize(12); b.setAllCaps(false);
        b.setTextColor(filled ? WHITE : BLACK);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(filled ? BLACK : Color.rgb(238,238,238)));
        return b;
    }

    private void buildScreen() {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(WHITE);
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(14), dp(12), dp(14), 0);
        frame.addView(shell, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = label("MONO VIDEO", 20, BLACK); brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView tag = label("B&W  •  VIDEO TOOL", 10, GRAY); header.addView(tag);
        shell.addView(header);
        status = label("Browse the web and save supported video files", 11, GRAY);
        status.setPadding(0, dp(4), 0, dp(10)); shell.addView(status);

        LinearLayout addressRow = new LinearLayout(this); addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressBar = new EditText(this); addressBar.setSingleLine(true); addressBar.setTextSize(13);
        addressBar.setHint("Search or enter website address"); addressBar.setText(HOME);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO); addressBar.setSelectAllOnFocus(true);
        addressBar.setPadding(dp(12), 0, dp(12), 0); addressBar.setTextColor(BLACK);
        addressBar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
        addressRow.addView(addressBar, new LinearLayout.LayoutParams(0, dp(46), 1));
        Button go = actionButton("GO", true); addressRow.addView(go, new LinearLayout.LayoutParams(dp(58), dp(46)));
        shell.addView(addressRow);
        go.setOnClickListener(v -> navigate());
        addressBar.setOnEditorActionListener((v, action, event) -> { if (action == EditorInfo.IME_ACTION_GO) { navigate(); return true; } return false; });

        LinearLayout browserControls = new LinearLayout(this); browserControls.setPadding(0, dp(5), 0, dp(5));
        Button back = actionButton("‹", false), forward = actionButton("›", false), reload = actionButton("↻", false);
        browserControls.addView(back, new LinearLayout.LayoutParams(0, dp(38), 1));
        browserControls.addView(forward, new LinearLayout.LayoutParams(0, dp(38), 1));
        browserControls.addView(reload, new LinearLayout.LayoutParams(0, dp(38), 1));
        shell.addView(browserControls);
        back.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        forward.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        reload.setOnClickListener(v -> webView.reload());

        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        shell.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        webView = new WebView(this); webView.setBackgroundColor(WHITE);
        WebSettings settings = webView.getSettings(); settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE); settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false); settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setAllowFileAccess(false); settings.setAllowContentAccess(false);
        CookieManager cookies = CookieManager.getInstance(); cookies.setAcceptCookie(true); cookies.setAcceptThirdPartyCookies(webView, true);
        webView.setWebChromeClient(new WebChromeClient()); webView.addJavascriptInterface(new PageMediaBridge(), "MonoVideoBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme(); return !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString(); if (looksLikeVideo(url)) runOnUiThread(() -> addDetected(url));
                return super.shouldInterceptRequest(view, request);
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                addressBar.setText(url); found.clear(); if (detectedList != null) detectedList.removeAllViews();
            }
            @Override public void onPageFinished(WebView view, String url) {
                addressBar.setText(url);
                status.setText(isYouTubeUrl(url) ? "Official YouTube playback · downloads aren't provided" : "Private session · direct video files may appear in Video Tool");
                scanPageVideos();
            }
        });
        webView.setDownloadListener((url, ua, disposition, mime, length) -> {
            if (isYouTubeUrl(url)) Toast.makeText(this, "Use the platform's official download options where available.", Toast.LENGTH_LONG).show();
            else if (isHttp(url) && (looksLikeVideo(url) || (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("video/")))) {
                addDetected(url); startDownload(url, ua, disposition, mime);
            } else Toast.makeText(this, "Only supported direct video files can be saved.", Toast.LENGTH_SHORT).show();
        });
        content.addView(webView, new LinearLayout.LayoutParams(-1, -1));

        LinearLayout bottom = new LinearLayout(this); bottom.setPadding(0, dp(5), 0, dp(8));
        bottom.setBackgroundColor(WHITE); bottom.setElevation(dp(8));
        homeTab = actionButton("⌂  Home", true); downloadsTab = actionButton("↓  Downloads", false);
        bottom.addView(homeTab, new LinearLayout.LayoutParams(0, dp(52), 1));
        bottom.addView(downloadsTab, new LinearLayout.LayoutParams(0, dp(52), 1));
        shell.addView(bottom);
        homeTab.setOnClickListener(v -> showHome()); downloadsTab.setOnClickListener(v -> showDownloads());

        Button floating = actionButton("▶  VIDEO TOOL", true);
        floating.setTextSize(12); floating.setElevation(dp(7));
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(-2, dp(52), Gravity.RIGHT | Gravity.BOTTOM);
        fp.setMargins(0, 0, dp(20), dp(82)); frame.addView(floating, fp);
        floating.setOnClickListener(v -> showVideoTool());
        setContentView(frame);
    }

    private void showHome() {
        showingDownloads = false; content.removeAllViews(); content.addView(webView, new LinearLayout.LayoutParams(-1, -1));
        homeTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLACK)); homeTab.setTextColor(WHITE);
        downloadsTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(238,238,238))); downloadsTab.setTextColor(BLACK);
        status.setText("Browse the web and save supported video files");
    }

    private void showDownloads() {
        showingDownloads = true; content.removeAllViews();
        ScrollView scroll = new ScrollView(this); LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(4), dp(12), dp(4), dp(12));
        TextView title = label("YOUR DOWNLOADS", 18, BLACK); title.setTypeface(Typeface.DEFAULT, Typeface.BOLD); list.addView(title);
        TextView note = label("Files are saved in your device's Downloads folder.", 12, GRAY); note.setPadding(0, dp(5), 0, dp(12)); list.addView(note);
        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        Cursor c = null; int count = 0;
        try {
            c = dm.query(new DownloadManager.Query());
            while (c != null && c.moveToNext()) {
                count++; String name = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                int state = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                String stateText = state == DownloadManager.STATUS_SUCCESSFUL ? "Completed" : state == DownloadManager.STATUS_FAILED ? "Failed" : "In progress";
                TextView item = label("↓  " + name + "\n     " + stateText, 14, BLACK); item.setPadding(dp(10), dp(12), dp(10), dp(12));
                list.addView(item); View line = new View(this); line.setBackgroundColor(Color.rgb(230,230,230)); list.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        } catch (Exception ignored) { } finally { if (c != null) c.close(); }
        if (count == 0) { TextView empty = label("No downloads yet", 15, GRAY); empty.setPadding(0, dp(24), 0, 0); list.addView(empty); }
        Button refresh = actionButton("Refresh downloads", true); refresh.setOnClickListener(v -> showDownloads()); list.addView(refresh);
        scroll.addView(list); content.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        homeTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(238,238,238))); homeTab.setTextColor(BLACK);
        downloadsTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLACK)); downloadsTab.setTextColor(WHITE);
        status.setText("Download manager");
    }

    private void showVideoTool() {
        if (found.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Video Tool").setMessage("No supported direct video links detected on this page yet. Play or open a supported video file, then check again.")
                .setPositiveButton("OK", null).show(); return;
        }
        ScrollView scroll = new ScrollView(this); LinearLayout rows = new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL); rows.setPadding(dp(14), dp(8), dp(14), dp(8));
        for (String url : new ArrayList<>(found)) {
            LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0, dp(7), 0, dp(7));
            TextView name = label(Uri.parse(url).getLastPathSegment() == null ? "Video file" : Uri.parse(url).getLastPathSegment(), 12, BLACK);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            Button download = actionButton("Download", true); row.addView(download, new LinearLayout.LayoutParams(-2, dp(42)));
            download.setOnClickListener(v -> startDownload(url, null, null, "video/*")); rows.addView(row);
            View divider = new View(this); divider.setBackgroundColor(Color.LTGRAY); rows.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        }
        scroll.addView(rows);
        new AlertDialog.Builder(this).setTitle("Detected video links").setView(scroll).setNegativeButton("Close", null).show();
    }

    private void navigate() {
        String value = addressBar.getText().toString().trim(); if (value.isEmpty()) return;
        String target = value.matches("(?i)^https?://.*") ? value : value.contains(".") && !value.contains(" ") ? "https://" + value : "https://www.google.com/search?q=" + Uri.encode(value);
        if (isHttp(target)) { showHome(); webView.loadUrl(target); } else Toast.makeText(this, "Only regular web addresses are supported.", Toast.LENGTH_SHORT).show();
    }
    private boolean isHttp(String value) { return value != null && (value.toLowerCase(Locale.ROOT).startsWith("https://") || value.toLowerCase(Locale.ROOT).startsWith("http://")); }
    private boolean isYouTubeUrl(String value) {
        try { String host = Uri.parse(value).getHost(); if (host == null) return false; host = host.toLowerCase(Locale.ROOT);
            return host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtu.be") || host.endsWith(".youtu.be");
        } catch (Exception e) { return false; }
    }
    private boolean looksLikeVideo(String value) {
        try { String path = Uri.parse(value).getPath(); if (path == null) path = value; path = URLDecoder.decode(path, "UTF-8").toLowerCase(Locale.ROOT);
            return path.endsWith(".mp4") || path.endsWith(".webm") || path.endsWith(".m4v") || path.endsWith(".3gp") || path.endsWith(".mov");
        } catch (Exception e) { return false; }
    }
    private void scanPageVideos() {
        String js = "(function(){function scan(){var vids=document.querySelectorAll('video'),a=[];vids.forEach(function(v){if(v.currentSrc)a.push(v.currentSrc);if(v.src)a.push(v.src);v.querySelectorAll('source').forEach(function(s){if(s.src)a.push(s.src);});});document.querySelectorAll('a[href]').forEach(function(x){if(/\\.(mp4|webm|m4v|3gp|mov)(\\?|$)/i.test(x.href))a.push(x.href);});a.forEach(function(u){if(/^https?:/i.test(u)&&window.MonoVideoBridge)MonoVideoBridge.found(u);});}scan();if(!window.__monoObserver){window.__monoObserver=new MutationObserver(scan);window.__monoObserver.observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['src']});}})();";
        webView.evaluateJavascript(js, null);
    }
    private class PageMediaBridge {
        @JavascriptInterface public void found(String url) { if (isHttp(url) && looksLikeVideo(url) && !isYouTubeUrl(url)) runOnUiThread(() -> addDetected(url)); }
    }
    private void addDetected(String url) { if (isYouTubeUrl(url) || !found.add(url)) return; if (detectedList != null) { TextView t = label("▶  " + url, 11, BLACK); detectedList.addView(t); } }
    private void startDownload(String url, String userAgent, String disposition, String mime) {
        if (isYouTubeUrl(url)) { Toast.makeText(this, "Use the platform's official download options where available.", Toast.LENGTH_LONG).show(); return; }
        if (!isHttp(url) || !looksLikeVideo(url)) { Toast.makeText(this, "Only supported direct video files can be downloaded.", Toast.LENGTH_LONG).show(); return; }
        try {
            DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)); request.setTitle("Mono Video"); request.setDescription("Saving supported video file");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); request.setAllowedOverMetered(true); request.setAllowedOverRoaming(false);
            if (mime != null && mime.startsWith("video/")) request.setMimeType(mime); if (userAgent != null) request.addRequestHeader("User-Agent", userAgent);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MonoVideo_" + System.currentTimeMillis() + extensionFor(url)); manager.enqueue(request);
            Toast.makeText(this, "Download started. Check Downloads.", Toast.LENGTH_LONG).show();
        } catch (Exception e) { Toast.makeText(this, "Could not start this download.", Toast.LENGTH_LONG).show(); }
    }
    private String extensionFor(String url) { String p = Uri.parse(url).getLastPathSegment(); if (p != null) { int dot = p.lastIndexOf('.'); if (dot >= 0 && p.length() - dot <= 6) return p.substring(dot); } return ".mp4"; }
    private void clearPrivateData() { try { CookieManager cm = CookieManager.getInstance(); cm.removeAllCookies(null); cm.flush(); webView.clearCache(true); webView.clearHistory(); webView.clearFormData(); } catch (Exception ignored) { } }
    @Override public void onBackPressed() { if (showingDownloads) showHome(); else if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { clearPrivateData(); if (webView != null) { webView.loadUrl("about:blank"); webView.clearHistory(); webView.removeAllViews(); webView.destroy(); } super.onDestroy(); }
}
