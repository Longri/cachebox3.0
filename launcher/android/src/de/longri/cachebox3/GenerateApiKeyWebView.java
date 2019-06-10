/*
 * Copyright (C) 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.*;
import android.widget.LinearLayout;
import de.longri.cachebox3.apis.cachebox_api.CB_Api;
import de.longri.cachebox3.settings.Config;

/**
 * Created by Longri on 10.04.2017.
 */
public class GenerateApiKeyWebView extends Activity {
    private static ProgressDialog pd;
    private static boolean pdIsShow = false;
    final String javaScript = "javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');";
    private LinearLayout webViewLayout;
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gcapilogin);

        webViewLayout = (LinearLayout) findViewById(R.id.gal_Layout);
        if (Config.OverrideUrl.getValue().equals("")) {
            ShowWebsite(CB_Api.getGcAuthUrl());
        } else {
            runOnUiThread(() -> {
                String GC_AuthUrl = Config.OverrideUrl.getValue();
                if (GC_AuthUrl.equals("")) {
                    finish();
                }
                ShowWebsite(GC_AuthUrl);
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        pd = null;
    }

    private void ShowWebsite(String GC_AuthUrl) {
        // Initial new VebView Instanz

        webView = findViewById(R.id.gal_WebView);

        webViewLayout.removeAllViews();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }

        // Instanz new WebView
        webView = new WebView(AndroidLauncher.androidLauncher, null, android.R.attr.webViewStyle);
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
        });
        webViewLayout.addView(webView);

        if (!pdIsShow) {
            this.runOnUiThread(() -> {
                pd = ProgressDialog.show(GenerateApiKeyWebView.this, "", "Loading....", true);
                pdIsShow = true;
            });

        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setTitle("Loading...");

                if (!pdIsShow) {
                    runOnUiThread(() -> {
                        pd = ProgressDialog.show(GenerateApiKeyWebView.this, "", "Loading....", true);
                        pdIsShow = true;
                    });

                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:"))
                    url = url.replace("http:", "https:");
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String url = request.getUrl().toString();
                    if (url.startsWith("http:"))
                        url = url.replace("http:", "https:");
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                setTitle(R.string.app_name);
                if (pd != null)
                    pd.dismiss();
                pdIsShow = false;

                if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token="))) {
                    webView.loadUrl(javaScript);
                } else
                    super.onPageFinished(view, url);
            }

        });

        WebSettings settings = webView.getSettings();
        // settings.setPluginsEnabled(true);
        settings.setJavaScriptEnabled(true);
        // settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // webView.setWebChromeClient(new WebChromeClient());


        //delete cookies and cache
        webView.clearCache(true);
        webView.clearHistory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("GcApiLogin", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d("GcApiLogin", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }


        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        webView.loadUrl(GC_AuthUrl);
    }

    class MyJavaScriptInterface {

        @JavascriptInterface
        public void showHTML(String html) {

            String search = "Access token: ";
            int pos = html.indexOf(search);
            if (pos < 0)
                return;
            int pos2 = html.indexOf("</span>", pos);
            if (pos2 < pos)
                return;
            // zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
            final String accessToken = html.substring(pos + search.length(), pos2);
            AndroidPlatformConnector.platformConnector.callBack.callBack(accessToken);
            finish();
        }
    }
}
