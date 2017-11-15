/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.activities.ReloadCacheActivity;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 14.09.2016.
 */
public class DescriptionView extends AbstractView implements SelectedCacheChangedListener {

    private static final Logger log = LoggerFactory.getLogger(DescriptionView.class);

    private static long lastCacheId;
    private static float lastX, lastY;

    private final LinkedList<String> nonLocalImages = new LinkedList<String>();
    private final LinkedList<String> nonLocalImagesUrl = new LinkedList<String>();

    private final AtomicBoolean FIRST = new AtomicBoolean(true);
    private final GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack = new GenericHandleCallBack<String>() {
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
//                MessageBox.show(Translation.get(attr));
                log.debug("Attribute icon clicked, don't load URL");
                return true;
            } else if (url.contains("fake://fake.de?Button")) {
//                int pos = url.indexOf("+");
//                if (pos < 0)
//                    return true;
//
//                final String attr = url.substring(pos + 1, url.length() - 1);
//
//                MessageBox.show(Translation.get(attr));
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
//                            String s = "download limit is reached!\n";
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
//                            String s = "download Details of this cache?\n";
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
//                pd = ProgressDialog.show(getContext(), "", "download Description", true);
//
//                thread.start();
                log.debug("get Basic Member description clicked, don't load URL");
                return true;
            } else if (url.startsWith("http://")) {
                // Load Url in ext Browser
                log.debug("Link clicked, don't load URL! show on ext browser");
                PlatformConnector._openUrlExtern(url);
                return true;
            } else if (url.equals("about:blank")) {
                // Load description
                return true;
            }
            return false;
        }
    };


    private PlatformDescriptionView view;


    public DescriptionView() {
        super("DescriptionView");
        EventHandler.add(this);
    }

    private String getAttributesHtml(AbstractCache abstractCache) {
        StringBuilder sb = new StringBuilder();
        try {
            Array<Attributes> attributes = abstractCache.getAttributes(Database.Data);
            if (attributes == null) return "";
            Iterator<Attributes> attributesIterator = attributes.iterator();
            if (attributesIterator == null || !attributesIterator.hasNext())
                return "";

            do {
                Attributes attribute = attributesIterator.next();
                File result = new File(CB.WorkPath + "/data/Attributes/" + attribute.getImageName() + ".png");
                sb.append("<form action=\"Attr\">");
                sb.append("<input name=\"Button\" type=\"image\" src=\"" + result.toURI() + "\" height=\"40\" width=\"40\" value=\" " + attribute.getImageName() + " \">");

            } while (attributesIterator.hasNext());

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
        EventHandler.remove(this);
        PlatformConnector.setDescriptionViewToNULL();
        view = null;
    }

    @Override
    protected void boundsChanged(float x, float y, float width, float height) {
        if (view != null) view.setBounding(x, y, width, height, Gdx.graphics.getHeight());
    }


    @Override
    public void onShow() {
        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                showPlatformWebView();
            }
        });
    }

    private void showPlatformWebView() {

        final AtomicBoolean WAIT = new AtomicBoolean(false);

        if (view == null) {
            WAIT.set(true);
            PlatformConnector.getDescriptionView(new GenericCallBack<PlatformDescriptionView>() {
                @Override
                public void callBack(PlatformDescriptionView descriptionView) {
                    view = descriptionView;
                    view.setShouldOverrideUrlLoadingCallBack(shouldOverrideUrlLoadingCallBack);
                    WAIT.set(false);
                }
            });
        }

        CB.wait(WAIT);

        AbstractCache actCache = EventHandler.getSelectedCache();
        if (actCache != null) {
            nonLocalImages.clear();
            nonLocalImagesUrl.clear();

            String cacheHtml = actCache.getLongDescription(Database.Data) + actCache.getShortDescription(Database.Data);
            String html = "";
            if (actCache.getApiState() == 1)// GC.com API lite
            { // Load Standard HTML
                log.debug("load is Lite html");
                String nodesc = Translation.get("GC_NoDescription").toString();
                html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" " + Translation.get("GC_DownloadDescription") + " \"></form>";
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

            view.display();
            view.setHtml(html);

            if (lastCacheId == actCache.getId()) {
                // restore last scroll position
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (view != null) {// maybe is disposed now
                            view.setScrollPosition(lastX, lastY);
                        }
                    }
                }, 0.15f);
            }
        }


        if (nonLocalImages.size() > 0) {
            CB.postAsync(new Runnable() {
                @Override
                public void run() {
                    //download and store
                    log.debug("download description images");
                    for (int i = 0, n = nonLocalImages.size(); i < n; i++) {
                        String localFilename = nonLocalImages.get(i);
                        String downloadUrl = nonLocalImagesUrl.get(i);
                        if (!NetUtils.download(downloadUrl, localFilename)) {
                            log.error("Image '{}' download failed", downloadUrl);
                        }
                    }
                }
            });
        }

        boundsChanged(DescriptionView.this.getX(), DescriptionView.this.getY(), DescriptionView.this.getWidth(), DescriptionView.this.getHeight());
    }

    @Override
    public void onHide() {
        super.onHide();
        if (EventHandler.getSelectedCache() != null) {
            lastCacheId = EventHandler.getSelectedCache().getId();
            lastX = view.getScrollPositionX();
            lastY = view.getScrollPositionY();
            view.close();
        }
    }

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                showPlatformWebView();
            }
        });
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("DescriptionViewContextMenu");

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case MenuID.MI_FAVORIT:
                        if (EventHandler.getSelectedCache() == null) {

                            new ButtonDialog("NoCacheSelect", Translation.get("NoCacheSelect"), Translation.get("Error"),
                                    MessageBoxButtons.OKCancel, MessageBoxIcon.Error, null).show();
                            return true;
                        }

                        EventHandler.getSelectedCache().setFavorite(!EventHandler.getSelectedCache().isFavorite());

                        DaoFactory.CACHE_DAO.updateDatabase(Database.Data, EventHandler.getSelectedCache());

                        // Update Query
                        Database.Data.Query.GetCacheById(EventHandler.getSelectedCache().getId()).setFavorite(EventHandler.getSelectedCache().isFavorite());

                        // Update View
                        //TODO update
//                        if (TabMainView.descriptionView != null)
//                            TabMainView.descriptionView.onShow();

                        CacheListChangedEventList.Call();
                        return true;
                    case MenuID.MI_RELOAD_CACHE:
                        new ReloadCacheActivity().show();
                        return true;
                }
                return false;
            }
        });

        MenuItem mi;

        boolean isSelected = (EventHandler.getSelectedCache() != null);

        //ISSUE (#126 handle own favorites)  mi = cm.addItem(MenuID.MI_FAVORIT, "Favorite", CB.getSkin().getMenuIcon.favorit);
//        mi.setCheckable(true);
//        if (isSelected) {
//            mi.setChecked(EventHandler.getSelectedCache().isFavorite());
//        } else {
//            mi.setEnabled(false);
//        }

        boolean selectedCacheIsNoGC = false;
        if (isSelected)
            selectedCacheIsNoGC = !EventHandler.getSelectedCache().getGcCode().toString().startsWith("GC");
        mi = cm.addItem(MenuID.MI_RELOAD_CACHE, "ReloadCacheAPI", CB.getSkin().getMenuIcon.reloadCacheIcon);
        if (!isSelected)
            mi.setEnabled(false);
        if (selectedCacheIsNoGC)
            mi.setEnabled(false);

        return cm;
    }
}
