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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.AndroidLauncher.androidLauncher;

/**
 * Created by Longri on 26.04.2017.
 */
public class AndroidDescriptionView extends WebView implements PlatformDescriptionView {

    private final static Logger log = LoggerFactory.getLogger(AndroidDescriptionView.class);


    final String mimeType = "text/html";
    final String encoding = "utf-8";
    private GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack;
    private final AtomicBoolean pageVisible = new AtomicBoolean(false);

    public AndroidDescriptionView(Context context) {
        super(AndroidLauncher.androidLauncher, null, android.R.attr.webViewStyle);
        this.setDrawingCacheEnabled(false);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.setWebViewClient(clint);
    }

    WebViewClient clint = new WebViewClient() {

        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().getPath();
            return AndroidDescriptionView.this.shouldOverrideUrlLoading(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            log.debug("onPageStarted");
        }

        public void onPageFinished(WebView view, String url) {
            log.debug("onPageFinished URL: {}", url);
        }

        public void onLoadResource(WebView view, String url) {
            log.debug("onLoadResource URL: {}", url);
        }

        public void onPageCommitVisible(WebView view, String url) {
            log.debug("onPageCommitVisible URL: {}", url);
            pageVisible.set(true);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return AndroidDescriptionView.this.shouldOverrideUrlLoading(view, url);
        }

        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            scale = newScale;
            log.debug("Scale changed to {}", scale);
        }
    };

    private boolean shouldOverrideUrlLoading(WebView view, String url) {
        return shouldOverrideUrlLoadingCallBack.callBack(url);
    }

    @Override
    public void setBounding(final float x, final float y, final float width, final float height, final int screenHeight) {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams paramsLeft = (FrameLayout.LayoutParams) AndroidDescriptionView.this.getLayoutParams();
                if (paramsLeft != null) {
                    paramsLeft.width = (int) width;
                    paramsLeft.height = (int) height;
                    AndroidDescriptionView.this.setLayoutParams(paramsLeft);
                    AndroidDescriptionView.this.setX(x);
                    AndroidDescriptionView.this.setY(screenHeight - height - y);
                }
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
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AndroidDescriptionView.this.loadDataWithBaseURL("fake://fake.de", html, mimeType, encoding, null);
                } catch (Exception e) {
                    return; // if an exception here, then this is not initializes
                }
            }
        });
    }

    @Override
    public void display() {
        log.debug("display webView");
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                androidLauncher.show(AndroidDescriptionView.this);
            }
        });
    }

    @Override
    public void close() {
        log.debug("close webView");
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                androidLauncher.removeView(AndroidDescriptionView.this);
            }
        });
    }

    @Override
    public void setShouldOverrideUrlLoadingCallBack(GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack) {
        this.shouldOverrideUrlLoadingCallBack = shouldOverrideUrlLoadingCallBack;
    }

    @Override
    public void setFinishLoadingCallBack(GenericHandleCallBack<String> finishLoadingCallBack) {
        //todo call this CallBack if Html is finish loaded
    }

    @Override
    public boolean isPageVisible() {
        return pageVisible.get();
    }

    private Point scrollPos = new Point(0, 0);
    private float scale = 4;

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


}
