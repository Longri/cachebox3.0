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
import android.graphics.Point;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static de.longri.cachebox3.AndroidLauncher.androidLauncher;

/**
 * Created by Longri on 26.04.2017.
 */
public class AndroidDescriptionView extends WebView implements PlatformDescriptionView {

    final String mimeType = "text/html";
    final String encoding = "utf-8";


    public AndroidDescriptionView(Context context) {
        super(AndroidLauncher.androidLauncher, null, android.R.attr.webViewStyle);
        this.setDrawingCacheEnabled(false);
        this.setAlwaysDrawnWithCacheEnabled(false);

        // this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setLightTouchEnabled(false);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.setWebViewClient(clint);
    }

    WebViewClient clint = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("fake://fake.de/Attr")) {
//                int pos = url.indexOf("+");
//                if (pos < 0)
//                    return true;
//
//                final String attr = url.substring(pos + 1, url.length() - 1);
//
//                MessageBox.Show(Translation.Get(attr));
                return true;
            } else if (url.contains("fake://fake.de?Button")) {
//                int pos = url.indexOf("+");
//                if (pos < 0)
//                    return true;
//
//                final String attr = url.substring(pos + 1, url.length() - 1);
//
//                MessageBox.Show(Translation.Get(attr));
                return true;
            } else if (url.contains("fake://fake.de/download")) {

//                Thread thread = new Thread() {
//                    @Override
//                    public void run() {
//
//                        if (!CB_Core.Api.GroundspeakAPI.CacheStatusValid) {
//                            int result = CB_Core.Api.GroundspeakAPI.GetCacheLimits(null);
//                            if (result != 0) {
//                                onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
//                                return;
//                            }
//
//                            if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
//                                GL.that.Toast(ConnectionError.INSTANCE);
//                                return;
//                            }
//                            if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
//                                GL.that.Toast(ApiUnavailable.INSTANCE);
//                                return;
//                            }
//                        }
//                        if (CB_Core.Api.GroundspeakAPI.CachesLeft <= 0) {
//                            String s = "Download limit is reached!\n";
//                            s += "You have downloaded the full cache details of " + CB_Core.Api.GroundspeakAPI.MaxCacheCount + " caches in the last 24 hours.\n";
//                            if (CB_Core.Api.GroundspeakAPI.MaxCacheCount < 10)
//                                s += "If you want to download the full cache details of 6000 caches per day you can upgrade to Premium Member at \nwww.geocaching.com!";
//
//                            message = s;
//
//                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));
//
//                            return;
//                        }
//
//                        if (!CB_Core.Api.GroundspeakAPI.IsPremiumMember()) {
//                            String s = "Download Details of this cache?\n";
//                            s += "Full Downloads left: " + CB_Core.Api.GroundspeakAPI.CachesLeft + "\n";
//                            s += "Actual Downloads: " + CB_Core.Api.GroundspeakAPI.CurrentCacheCount + "\n";
//                            s += "Max. Downloads in 24h: " + CB_Core.Api.GroundspeakAPI.MaxCacheCount;
//                            message = s;
//                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(3));
//                            return;
//                        } else {
//                            // call the download directly
//                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(4));
//                            return;
//                        }
//                    }
//                };
//                pd = ProgressDialog.show(getContext(), "", "Download Description", true);
//
//                thread.start();

                return true;
            } else if (url.startsWith("http://")) {
                // Load Url in ext Browser
              //TODO PlatformConnector.callUrl(url);
                return true;
            }
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//TODO msg to core descriptionView
        }

    };

    @Override
    public void setBounding(float x, float y, float width, float height) {

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
    public void setHtml(final String html) {
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

    }

    @Override
    public void close() {

    }

    private Point scrollPos = new Point(0, 0);

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
