/*
 * Copyright (C) 2016 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.callbacks.GenerickHandleCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.types.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 14.09.2016.
 */
public class DescriptionView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(DescriptionView.class);

    private static long lastCacheId;
    private static float lastX, lastY;

    private final LinkedList<String> nonLocalImages = new LinkedList<String>();
    private final LinkedList<String> nonLocalImagesUrl = new LinkedList<String>();

    private final AtomicBoolean FIRST = new AtomicBoolean(true);
    private final GenerickHandleCallBack<String> shouldOverrideUrlLoadingCallBack = new GenerickHandleCallBack<String>() {
        @Override
        public boolean callBack(String url) {
            log.debug("Load Url callback: {}", url);
            if (FIRST.get()) {
                FIRST.set(false);
                boundsChanged(DescriptionView.this.getX(), DescriptionView.this.getY(), DescriptionView.this.getWidth(), DescriptionView.this.getHeight());
            }

            if (url.contains("fake://fake.de/Attr")) {
//                int pos = url.indexOf("+");
//                if (pos < 0)
//                    return true;
//
//                final String attr = url.substring(pos + 1, url.length() - 1);
//
//                MessageBox.Show(Translation.Get(attr));
                log.debug("Attribute icon clicked, don't load URL");
                return true;
            } else if (url.contains("fake://fake.de?Button")) {
//                int pos = url.indexOf("+");
//                if (pos < 0)
//                    return true;
//
//                final String attr = url.substring(pos + 1, url.length() - 1);
//
//                MessageBox.Show(Translation.Get(attr));
                log.debug("Attribute icon clicked, don't load URL");
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
                log.debug("Get Basic Member description clicked, don't load URL");
                return true;
            } else if (url.startsWith("http://")) {
                // Load Url in ext Browser
                //TODO PlatformConnector.callUrl(url);
                log.debug("Link clicked, don't load URL! Show on ext browser");
                PlatformConnector._openUrlExtern(url);
                return true;
            }
            return false;
        }
    };


    private PlatformDescriptionView view;


    public DescriptionView() {
        super("DescriptionView");
    }

    private String getAttributesHtml(Cache cache) {
        StringBuilder sb = new StringBuilder();
        try {
            Iterator<Attributes> attrs = cache.getAttributes().iterator();

            if (attrs == null || !attrs.hasNext())
                return "";

            do {
                Attributes attribute = attrs.next();
                File result = new File(CB.WorkPath + "/data/Attributes/" + attribute.getImageName() + ".png");
                sb.append("<form action=\"Attr\">");
                sb.append("<input name=\"Button\" type=\"image\" src=\"file://" + result.getAbsolutePath() + "\" value=\" " + attribute.getImageName() + " \">");
            } while (attrs.hasNext());

            sb.append("</form>");

            if (sb.length() > 0)
                sb.append("<br>");
            return sb.toString();
        } catch (Exception ex) {
            // TODO Handle Exception
            return "";
        }
    }

    @Override
    public void dispose() {
        PlatformConnector.setDescriptionViewToNULL();
    }

    @Override
    protected void boundsChanged(float x, float y, float width, float height) {
        if (view != null) view.setBounding(x, y, width, height, Gdx.graphics.getHeight());
    }


    @Override
    public void onShow() {
        PlatformConnector.getDescriptionView(new GenericCallBack<PlatformDescriptionView>() {
            @Override
            public void callBack(PlatformDescriptionView descriptionView) {
                view = descriptionView;
                Cache actCache = EventHandler.getSelectedCache();
                if (actCache != null) {
                    nonLocalImages.clear();
                    nonLocalImagesUrl.clear();

                    if (!actCache.isDetailLoaded()) {
                        log.warn("Details not loaded for Cache: {}", actCache);
                    }

                    String cacheHtml = actCache.getLongDescription() + actCache.getShortDescription();
                    String html = "";
                    if (actCache.getApiState() == 1)// GC.com API lite
                    { // Load Standard HTML
                        log.debug("load is Lite html");
                        String nodesc = Translation.Get("GC_NoDescription");
                        html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" " + Translation.Get("GC_DownloadDescription") + " \"></form>";
                    } else {
                        html = DescriptionImageGrabber.resolveImages(actCache, cacheHtml, false, nonLocalImages, nonLocalImagesUrl);
                        if (!Config.DescriptionNoAttributes.getValue()) {
                            html = getAttributesHtml(actCache) + html;
                            log.debug("load html with Attributes");
                        } else {
                            log.debug("load html without Attributes");
                        }


                        // add 2 empty lines so that the last line of description can be selected with the markers
                        html += "</br></br>";
                    }
                    view.setShouldOverrideUrlLoadingCallBack(shouldOverrideUrlLoadingCallBack);
                    view.display();
                    view.setHtml(html);

                    if (lastCacheId == actCache.Id) {
                        // restore last scroll position
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                view.setScrollPosition(lastX, lastY);
                            }
                        }, 0.15f);
                    }
                }
                boundsChanged(DescriptionView.this.getX(), DescriptionView.this.getY(), DescriptionView.this.getWidth(), DescriptionView.this.getHeight());
            }
        });
    }

    @Override
    public void onHide() {
        if (EventHandler.getSelectedCache() != null) {
            lastCacheId = EventHandler.getSelectedCache().Id;
            lastX = view.getScrollPositionX();
            lastY = view.getScrollPositionY();
            view.close();
        }
    }
}
