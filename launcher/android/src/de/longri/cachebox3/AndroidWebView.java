/*
 * Copyright (C) 2020 team-cachebox.de
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static de.longri.cachebox3.AndroidLauncher.androidLauncher;

/**
 * Created by Longri on 26.04.2017.
 */
public class AndroidWebView extends WebView implements PlatformWebView {

    private final static Logger log = LoggerFactory.getLogger(AndroidWebView.class);


    final String mimeType = "text/html";
    final String encoding = "utf-8";
    private final AtomicBoolean pageVisible = new AtomicBoolean(false);
    private GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack;
    private GenericHandleCallBack<String> startLoadingCallBack;
    private GenericHandleCallBack<String> finishLoadingCallBack;
    private Point scrollPos = new Point(0, 0);
    private float scale = 4;
    private final AtomicReference<String> HTML_STRING;
    private final MyJavaScriptInterface javaScriptInterface;
    WebViewClient clint = new WebViewClient() {

        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // this method is not called on my device (sdk_int = 22)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String url = request.getUrl().getPath();
                return AndroidWebView.this.shouldOverrideUrlLoading(view, url);
            } else {
                // what todo
                return AndroidWebView.this.shouldOverrideUrlLoading(view, "fake://fake.de?GetAttInfo Kann Attribut nicht bestimmen.");
            }
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return AndroidWebView.this.shouldOverrideUrlLoading(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            log.debug("onPageStarted");
            if (AndroidWebView.this.startLoadingCallBack != null) {
                CB.postAsyncDelayd(10, new NamedRunnable("finishLoadingCallBack") {
                    @Override
                    public void run() {
                        AndroidWebView.this.startLoadingCallBack.callBack(url);
                    }
                });
            }
        }

        public void onPageFinished(WebView view, String url) {
            log.debug("onPageFinished URL: {}", url);
            if (AndroidWebView.this.finishLoadingCallBack != null) {
                CB.postAsyncDelayd(100, new NamedRunnable("finishLoadingCallBack") {
                    @Override
                    public void run() {
                        AndroidWebView.this.finishLoadingCallBack.callBack(url);
                    }
                });
            }
        }

        public void onLoadResource(WebView view, String url) {
            log.debug("onLoadResource URL: {}", url);
        }

        public void onPageCommitVisible(WebView view, String url) {
            log.debug("onPageCommitVisible URL: {}", url);
            pageVisible.set(true);
        }

        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            scale = newScale;
            log.debug("Scale changed to {}", scale);
        }
    };

    public AndroidWebView(Context context) {
        super(AndroidLauncher.androidLauncher, null, android.R.attr.webViewStyle);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.setWebViewClient(clint);
        HTML_STRING = new AtomicReference<>();
        javaScriptInterface = new MyJavaScriptInterface(HTML_STRING);
        this.addJavascriptInterface(javaScriptInterface, "HTMLOUT");
    }

    private boolean shouldOverrideUrlLoading(WebView view, String url) {
        return shouldOverrideUrlLoadingCallBack.callBack(url);
    }

    @Override
    public void setBounding(final float x, final float y, final float width, final float height, final int screenHeight) {
        androidLauncher.runOnUiThread(() -> {
            FrameLayout.LayoutParams paramsLeft = (FrameLayout.LayoutParams) AndroidWebView.this.getLayoutParams();
            if (paramsLeft != null) {
                paramsLeft.width = (int) width;
                paramsLeft.height = (int) height;
                AndroidWebView.this.setLayoutParams(paramsLeft);
                AndroidWebView.this.setX(x);
                AndroidWebView.this.setY(screenHeight - height - y);
            }
        });
    }

    @Override
    public void setScrollPosition(float x, float y) {
        scrollTo((int) x, (int) y);
    }

    @Override
    public float getScrollPositionX() {
        return scrollPos.x;
    }

    @Override
    public float getScrollPositionY() {
        return scrollPos.y;
    }

    @Override
    public float getScale() {
        log.debug("return scale: {}", scale);
        return scale;
    }

    @Override
    public void setScale(float scale) {
        log.debug("setScale: {} ", scale);
        setInitialScale((int) (100 * scale));
    }

    @Override
    public void setHtml(final String html) {
        pageVisible.set(false);
        androidLauncher.runOnUiThread(() -> {
            try {
                loadDataWithBaseURL("fake://fake.de", html, mimeType, encoding, null);
            } catch (Exception ignored) {
                // if an exception here, then this is not initializes
            }
        });
    }

    @Override
    public void display() {
        log.debug("display webView");
        androidLauncher.runOnUiThread(() -> androidLauncher.show(AndroidWebView.this));
    }

    @Override
    public void close() {
        log.debug("close webView");
        androidLauncher.runOnUiThread(() -> androidLauncher.removeView(AndroidWebView.this));
    }

    @Override
    public void setShouldOverrideUrlLoadingCallBack(GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack) {
        this.shouldOverrideUrlLoadingCallBack = shouldOverrideUrlLoadingCallBack;
    }

    @Override
    public void setStartLoadingCallBack(GenericHandleCallBack<String> startLoadingCallBack) {
        this.startLoadingCallBack = startLoadingCallBack;
    }

    @Override
    public void setFinishLoadingCallBack(GenericHandleCallBack<String> finishLoadingCallBack) {
        this.finishLoadingCallBack = finishLoadingCallBack;
    }

    @Override
    public boolean isPageVisible() {
        return pageVisible.get();
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldl, int oldt) {
        try {
            super.onScrollChanged(x, y, oldl, oldt);
            scrollPos.x = x;
            scrollPos.y = y;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getContentAsString() {
        javaScriptInterface.WAIT_FOR_GET_HTML.set(true);
        CB.postAsync(new NamedRunnable("call get html") {
            @Override
            public void run() {
                CB.postOnMainThread(new NamedRunnable("") {
                    @Override
                    public void run() {
                        AndroidWebView.this.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }
                });
            }
        });

        CB.wait(javaScriptInterface.WAIT_FOR_GET_HTML);

        return HTML_STRING.get();
    }


    private class MyJavaScriptInterface {

        private final AtomicReference<String> ATOMIC_HTML_STRING;
        public final AtomicBoolean WAIT_FOR_GET_HTML = new AtomicBoolean(false);

        private MyJavaScriptInterface(AtomicReference<String> atomic_html_string) {
            ATOMIC_HTML_STRING = atomic_html_string;
        }


        @JavascriptInterface
        public void showHTML(String html) {
            ATOMIC_HTML_STRING.set(html);
            WAIT_FOR_GET_HTML.set(false);
        }
    }

}
