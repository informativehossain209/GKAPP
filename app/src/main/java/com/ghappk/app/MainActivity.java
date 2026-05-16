package com.ghappk.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final String APP_URL = "https://g-happk.vercel.app/";
    private static final int FILE_CHOOSER_REQUEST = 100;

    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;
    private boolean isShowingErrorPage = false;

    // ─────────────────────────────────────────────────────────────────
    // Bengali no-internet page (inline HTML — no asset file needed)
    // ─────────────────────────────────────────────────────────────────
    private static final String NO_INTERNET_HTML =
        "<!DOCTYPE html><html lang='bn'><head><meta charset='UTF-8'/>" +
        "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
        "<title>সংযোগ নেই</title>" +
        "<style>" +
        "  *{margin:0;padding:0;box-sizing:border-box}" +
        "  body{font-family:'Segoe UI',sans-serif;background:linear-gradient(135deg,#0a0f0a 0%,#111a0d 100%);" +
        "       min-height:100vh;display:flex;flex-direction:column;align-items:center;" +
        "       justify-content:center;padding:32px;color:#fff;text-align:center}" +
        "  .icon-wrap{width:110px;height:110px;border-radius:50%;background:rgba(255,102,0,0.12);" +
        "              border:2px solid rgba(255,102,0,0.35);display:flex;align-items:center;" +
        "              justify-content:center;margin-bottom:28px;" +
        "              animation:pulse 2.2s ease-in-out infinite}" +
        "  .icon{font-size:52px;animation:shake 2.2s ease-in-out infinite}" +
        "  @keyframes pulse{0%,100%{transform:scale(1);box-shadow:0 0 0 0 rgba(255,102,0,0.3)}" +
        "                   50%{transform:scale(1.07);box-shadow:0 0 0 16px rgba(255,102,0,0)}}" +
        "  @keyframes shake{0%,100%{transform:rotate(0deg)}20%{transform:rotate(-12deg)}" +
        "                   40%{transform:rotate(12deg)}60%{transform:rotate(-8deg)}" +
        "                   80%{transform:rotate(8deg)}}" +
        "  h1{font-size:26px;font-weight:700;color:#FF6600;margin-bottom:10px;letter-spacing:0.5px}" +
        "  p.sub{font-size:15px;color:#aaa;margin-bottom:6px;line-height:1.6}" +
        "  p.hint{font-size:13px;color:#666;margin-bottom:36px}" +
        "  .dots{display:flex;gap:8px;justify-content:center;margin-bottom:40px}" +
        "  .dot{width:10px;height:10px;border-radius:50%;background:#FF6600;opacity:0.3;" +
        "       animation:blink 1.4s ease-in-out infinite}" +
        "  .dot:nth-child(2){animation-delay:0.2s}" +
        "  .dot:nth-child(3){animation-delay:0.4s}" +
        "  @keyframes blink{0%,100%{opacity:0.15;transform:scale(1)}" +
        "                   50%{opacity:1;transform:scale(1.3)}}" +
        "  button{background:linear-gradient(135deg,#FF6600,#e65c00);color:#fff;" +
        "         border:none;border-radius:28px;padding:14px 40px;font-size:16px;" +
        "         font-weight:700;cursor:pointer;letter-spacing:0.3px;" +
        "         box-shadow:0 6px 20px rgba(255,102,0,0.4);transition:all 0.2s;" +
        "         -webkit-tap-highlight-color:transparent;}" +
        "  button:active{transform:scale(0.96);box-shadow:0 3px 10px rgba(255,102,0,0.3)}" +
        "  .brand{margin-top:48px;font-size:22px;font-weight:800;color:#FF6600;letter-spacing:0.5px}" +
        "  .brand span{color:#22bb00}" +
        "</style></head><body>" +
        "<div class='icon-wrap'><div class='icon'>📡</div></div>" +
        "<h1>ইন্টারনেট সংযোগ নেই</h1>" +
        "<p class='sub'>অ্যাপটি ব্যবহার করতে<br/>ইন্টারনেট সংযোগ প্রয়োজন</p>" +
        "<p class='hint'>WiFi বা মোবাইল ডেটা চালু করুন</p>" +
        "<div class='dots'><div class='dot'></div><div class='dot'></div><div class='dot'></div></div>" +
        "<button onclick='retry()'>🔄 আবার চেষ্টা করুন</button>" +
        "<div class='brand'><span>ঘর</span> খরচ</div>" +
        "<script>" +
        "function retry(){" +
        "  try{Android.retryConnection()}" +
        "  catch(e){window.location.reload()}" +
        "}" +
        "</script>" +
        "</body></html>";

    // ─────────────────────────────────────────────────────────────────
    // Network helpers
    // ─────────────────────────────────────────────────────────────────
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Exception e) {
            return true; // assume connected if we can't check
        }
    }

    private void loadNoInternetPage() {
        isShowingErrorPage = true;
        webView.loadDataWithBaseURL(null, NO_INTERNET_HTML, "text/html", "UTF-8", null);
    }

    private void loadApp() {
        isShowingErrorPage = false;
        webView.loadUrl(APP_URL);
    }

    // ─────────────────────────────────────────────────────────────────
    // Android ↔ JS bridge
    // ─────────────────────────────────────────────────────────────────
    public class AndroidBridge {

        @JavascriptInterface
        public void retryConnection() {
            runOnUiThread(() -> {
                if (isNetworkAvailable()) {
                    loadApp();
                } else {
                    Toast.makeText(MainActivity.this,
                            "এখনো ইন্টারনেট নেই — আবার চেষ্টা করুন",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void saveJpg(final String base64, final String fileName) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp == null) { toast("Image creation failed"); return; }
                    Uri uri = saveBitmapToGallery(bmp, fileName);
                    if (uri != null) toast("✅ Gallery-তে সেভ হয়েছে!");
                } catch (Exception e) { toast("Save failed: " + e.getMessage()); }
            });
        }

        @JavascriptInterface
        public void shareWhatsApp(final String base64, final String fileName, final String pkg) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp == null) { toast("Image creation failed"); return; }
                    File f = new File(getCacheDir(), fileName);
                    FileOutputStream fos = new FileOutputStream(f);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 93, fos);
                    fos.flush(); fos.close();
                    Uri imgUri = FileProvider.getUriForFile(
                        MainActivity.this, getPackageName() + ".provider", f);
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("image/jpeg");
                    i.setPackage(pkg);
                    i.putExtra(Intent.EXTRA_STREAM, imgUri);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        startActivity(i);
                    } catch (Exception ex) {
                        i.setPackage(null);
                        startActivity(Intent.createChooser(i, "Share"));
                    }
                } catch (Exception e) { toast("Share failed: " + e.getMessage()); }
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Gallery save helper
    // ─────────────────────────────────────────────────────────────────
    private Uri saveBitmapToGallery(Bitmap bmp, String fileName) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                cv.put(MediaStore.Images.Media.IS_PENDING, 1);
                Uri dest = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                if (dest == null) return null;
                try (OutputStream out = getContentResolver().openOutputStream(dest)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 93, out);
                }
                cv.clear();
                cv.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(dest, cv, null, null);
                return dest;
            } else {
                File pics = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES);
                if (!pics.exists()) pics.mkdirs();
                File out = new File(pics, fileName);
                FileOutputStream fos = new FileOutputStream(out);
                bmp.compress(Bitmap.CompressFormat.JPEG, 93, fos);
                fos.flush(); fos.close();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(out)));
                return Uri.fromFile(out);
            }
        } catch (Exception e) { toast("Save failed: " + e.getMessage()); return null; }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // ─────────────────────────────────────────────────────────────────
    // Activity lifecycle
    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new AndroidBridge(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String url = r.getUrl().toString();
                // Keep app URLs inside the WebView; open external links in browser
                if (url.startsWith(APP_URL) || url.startsWith("https://g-happk.vercel.app")) {
                    return false;
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                    catch (Exception ignored) {}
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isShowingErrorPage = false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                // Only intercept errors on the main frame (not sub-resources like images)
                if (request != null && request.isForMainFrame()) {
                    loadNoInternetPage();
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            android.webkit.WebResourceResponse errorResponse) {
                // Show error page for 5xx server errors on the main document
                if (request != null && request.isForMainFrame() &&
                        errorResponse != null && errorResponse.getStatusCode() >= 500) {
                    loadNoInternetPage();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> cb,
                    FileChooserParams p) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = cb;
                try {
                    startActivityForResult(p.createIntent(), FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    fileChooserCallback = null;
                    return false;
                }
                return true;
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                    boolean isUserGesture, android.os.Message resultMsg) {
                WebView popup = new WebView(MainActivity.this);
                WebSettings ps = popup.getSettings();
                ps.setJavaScriptEnabled(true);
                ps.setDomStorageEnabled(true);
                popup.setWebViewClient(new WebViewClient());
                WebView.WebViewTransport t = (WebView.WebViewTransport) resultMsg.obj;
                t.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }
        });

        // Load app or show no-internet page immediately
        if (isNetworkAvailable()) {
            loadApp();
        } else {
            loadNoInternetPage();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == FILE_CHOOSER_REQUEST && fileChooserCallback != null) {
            Uri[] results = null;
            if (res == Activity.RESULT_OK && data != null) {
                String str = data.getDataString();
                if (str != null) results = new Uri[]{Uri.parse(str)};
            }
            fileChooserCallback.onReceiveValue(results);
            fileChooserCallback = null;
        }
    }

    @Override
    public boolean onKeyDown(int kc, KeyEvent e) {
        if (kc == KeyEvent.KEYCODE_BACK) {
            if (isShowingErrorPage) {
                // Back from error page → exit app
                finish();
                return true;
            }
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(kc, e);
    }

    @Override protected void onResume() {
        super.onResume();
        webView.onResume();
        // If error page is showing and network just came back, auto-reload
        if (isShowingErrorPage && isNetworkAvailable()) {
            loadApp();
        }
    }
    @Override protected void onPause()   { super.onPause();   webView.onPause();   }
    @Override protected void onDestroy() { webView.destroy(); super.onDestroy();   }
}
