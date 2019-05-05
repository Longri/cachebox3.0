/*
 * Copyright (C) 2016 - 2019 team-cachebox.de
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
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.activities.ReloadCacheActivity;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.skin.styles.DescriptionViewStyle;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.NetUtils;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 14.09.2016.
 */
public class DescriptionView extends AbstractView implements SelectedCacheChangedListener {

    private static final Logger log = LoggerFactory.getLogger(DescriptionView.class);

    private static long lastCacheId;
    private static float lastX, lastY, lastScale;
    private PlatformDescriptionView view;

    private final Array<String> nonLocalImages = new Array<String>();
    private final Array<String> nonLocalImagesUrl = new Array<String>();

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
//                        if (!CB_Core.Api.GroundspeakLiveAPI.CacheStatusValid) {
//                            int result = CB_Core.Api.GroundspeakLiveAPI.GetCacheLimits(null);
//                            if (result != 0) {
//                                onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
//                                return;
//                            }
//
//                            if (result == GroundspeakLiveAPI.CONNECTION_TIMEOUT) {
//                                GL.that.Toast(ConnectionError.INSTANCE);
//                                return;
//                            }
//                            if (result == GroundspeakLiveAPI.API_IS_UNAVAILABLE) {
//                                GL.that.Toast(ApiUnavailable.INSTANCE);
//                                return;
//                            }
//                        }
//                        if (CB_Core.Api.GroundspeakLiveAPI.CachesLeft <= 0) {
//                            String s = "download limit is reached!\n";
//                            s += "You have downloaded the full cache details of " + CB_Core.Api.GroundspeakLiveAPI.MaxCacheCount + " caches in the last 24 hours.\n";
//                            if (CB_Core.Api.GroundspeakLiveAPI.MaxCacheCount < 10)
//                                s += "If you want to download the full cache details of 6000 caches per day you can upgrade to Premium Member at \nwww.geocaching.com!";
//
//                            message = s;
//
//                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));
//
//                            return;
//                        }
//
//                        if (!CB_Core.Api.GroundspeakLiveAPI.IsPremiumMember()) {
//                            String s = "download Details of this cache?\n";
//                            s += "Full Downloads left: " + CB_Core.Api.GroundspeakLiveAPI.CachesLeft + "\n";
//                            s += "Actual Downloads: " + CB_Core.Api.GroundspeakLiveAPI.CurrentCacheCount + "\n";
//                            s += "Max. Downloads in 24h: " + CB_Core.Api.GroundspeakLiveAPI.MaxCacheCount;
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
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                // Load Url in ext Browser
                log.debug("Link clicked, don't load URL! show on ext browser");
                PlatformConnector._openUrlExtern(url);
                return true;
            } else if (url.equals("about:blank")) {
                // Load description
                return false;
            }
            return false;
        }
    };

    public DescriptionView(BitStore reader) {
        super(reader);
    }

    public DescriptionView() {
        super("DescriptionView");
        EventHandler.add(this);
    }

    private String getAttributesHtml(AbstractCache abstractCache) {
        StringBuilder sb = new StringBuilder();
        try {
            Array<Attributes> attributes = abstractCache.getAttributes();
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
        CB.postOnMainThreadDelayed(100, new NamedRunnable("DescriptionView:dispose") {
            @Override
            public void run() {
                PlatformConnector.setDescriptionViewToNULL();
                view = null;
            }
        });
    }

    @Override
    protected void boundsChanged(float x, float y, float width, float height) {
        if (view != null) view.setBounding(x, y, width, height, Gdx.graphics.getHeight());
    }


    @Override
    public void onShow() {
        CB.postOnGlThread(new NamedRunnable("DescriptionView") {
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

            CharSequence longDescription = actCache.getLongDescription();
            CharSequence shortDescription = actCache.getShortDescription();

            // try to load from Db if NULL
            if (longDescription == null) {
                longDescription = getStringFromDB(Database.Data, "SELECT Description FROM CacheText WHERE Id=?", actCache.getId());
                //set on Cache Object for next showing
                actCache.setLongDescription(longDescription);
            }

            if (shortDescription == null) {
                shortDescription = getStringFromDB(Database.Data, "SELECT ShortDescription FROM CacheText WHERE Id=?", actCache.getId());
                //set on Cache Object for next showing
                actCache.setShortDescription(shortDescription);
            }

            CharSequence cacheHtml = new CompoundCharSequence(longDescription, shortDescription);
            String html = "";
            if (actCache.getApiState() == 1)// GC.com API lite
            { // Load Standard HTML
                log.debug("load is Lite html");
                String nodesc = Translation.get("GC_NoDescription").toString();
                html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" " + Translation.get("GC_DownloadDescription") + " \"></form>";
            } else {
                html = DescriptionImageGrabber.ResolveImages(actCache, cacheHtml.toString(), false, nonLocalImages, nonLocalImagesUrl);
                if (!Config.DescriptionNoAttributes.getValue()) {
                    html = getAttributesHtml(actCache) + html;
                    log.debug("load html with Attributes");
                } else {
                    log.debug("load html without Attributes");
                }


                // add 2 empty lines so that the last line of description can be selected with the markers
                html += "</br></br>";
            }

            if (!actCache.getShowOriginalHtmlColor())
                html = setDescriptionViewColorStyle(html);


            if (lastCacheId == actCache.getId()) {
                // restore last scroll position
                if (view != null) {
                    //Wait for html is loaded
                    view.setFinishLoadingCallBack(value -> {
                        CB.postOnMainThreadDelayed(100, new NamedRunnable("DescriptionView:set scale") {
                            @Override
                            public void run() {
                                log.debug("Set scale: {}", lastScale);
                                if (view != null) {
                                    view.setScale(lastScale);
                                    CB.postOnMainThreadDelayed(200, new NamedRunnable("DescriptionView:set pos") {
                                        @Override
                                        public void run() {
                                            if (view != null) {
                                                log.debug("Set x: {} y: {} ", lastX, lastY);
                                                view.setScrollPosition(lastX, lastY);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        return true;
                    });
                }
            }
            view.display();
            view.setHtml(html);
        }


        if (nonLocalImages.size > 0) {
            CB.postAsync(new NamedRunnable("DescriptionView") {
                @Override
                public void run() {
                    //download and store
                    log.debug("download description images");
                    for (int i = 0, n = nonLocalImages.size; i < n; i++) {
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

    private String setDescriptionViewColorStyle(String html) {

        DescriptionViewStyle style = CB.getSkin().get(DescriptionViewStyle.class);
        if (style == null) return html;

        //set HtmlBackground to #93B874 TODO set over style

        StringBuilder sb = new StringBuilder("<!DOCTYPE html><html><head><style>body {");

        if (style.backgroundColor != null) {
            sb.append("background-color: #");
            sb.append(style.backgroundColor.toString());
            sb.append(";");
        }

        if (style.foregroundColor != null) {
            sb.append("color: #");
            sb.append(style.foregroundColor.toString());
            sb.append(";");
        }

        if (style.linkColor != null) {
            sb.append("link: #");
            sb.append(style.linkColor.toString());
            sb.append(";");
        }

        sb.append("}</style></head><body>");
        sb.append(html);
        sb.append("</body></html>");
        return sb.toString();
    }

    @Override
    public void onHide() {
        super.onHide();
        final AbstractCache selectedCache = EventHandler.getSelectedCache();
        if (selectedCache != null) {
            CB.postOnMainThread(new NamedRunnable("DescriptionView:hide view") {
                @Override
                public void run() {
                    lastCacheId = selectedCache.getId();
                    lastX = view.getScrollPositionX();
                    lastY = view.getScrollPositionY();
                    lastScale = view.getScale();
                    log.debug("store last X: {} Y: {} scale: {}", lastX, lastY, lastScale);
                    view.close();
                }
            });
        }
    }

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        CB.postOnGlThread(new NamedRunnable("DescriptionView") {
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
        Menu cm = new Menu("DescriptionViewTitle");

        MenuItem mi;
        boolean isSelected = (EventHandler.getSelectedCache() != null);

        boolean selectedCacheIsNoGC = false;
        if (isSelected)
            selectedCacheIsNoGC = !EventHandler.getSelectedCache().getGcCode().toString().startsWith("GC");
        mi = cm.addMenuItem("ReloadCacheAPI", CB.getSkin().getMenuIcon.reloadCacheIcon, () -> {
            new ReloadCacheActivity().show();
        });
        if (!isSelected)
            mi.setEnabled(false);
        if (selectedCacheIsNoGC)
            mi.setEnabled(false);

        mi = cm.addMenuItem("Favorite", CB.getSkin().getMenuIcon.favorit, () -> {
            if (EventHandler.getSelectedCache() == null) {
                new ButtonDialog("NoCacheSelect", Translation.get("NoCacheSelect"), Translation.get("Error"),
                        MessageBoxButtons.OKCancel, MessageBoxIcon.Error, null).show();
            } else {
                AbstractCache selectedCache = EventHandler.getSelectedCache();

                selectedCache.setFavorite(!selectedCache.isFavorite());
                selectedCache.updateBooleanStore(Database.Data);

                DaoFactory.CACHE_DAO.updateDatabase(Database.Data, selectedCache, true);

                // Update cacheList
                Database.Data.cacheList.removeValue(EventHandler.getSelectedCache(), true);
                Database.Data.cacheList.add(selectedCache);

                //update EventHandler
                EventHandler.updateSelectedCache(selectedCache);
                EventHandler.fire(new CacheListChangedEvent());
            }
        });
        mi.setCheckable(true);
        if (isSelected) {
            mi.setChecked(EventHandler.getSelectedCache().isFavorite());
        } else {
            mi.setEnabled(false);
        }

        cm.addMenuItem("AddToWatchList", CB.getSkin().getMenuIcon.todo, () -> {
        }).setEnabled(false);
        cm.addMenuItem("RemoveFromWatchList", CB.getSkin().getMenuIcon.todo, () -> {
        }).setEnabled(false);
        cm.addMenuItem("Solver", CB.getSkin().getMenuIcon.todo, () -> {
            // replace icon with CB.getSkin().getMenuIcon.solverIcon
            SolverView view = new SolverView();
            CB.viewmanager.showView(view);
        }).setEnabled(false);
        cm.addMenuItem("MI_EDIT_CACHE", CB.getSkin().getMenuIcon.todo, () -> {
        }).setEnabled(false);
        cm.addMenuItem("MI_DELETE_CACHE", CB.getSkin().getMenuIcon.todo, () -> {
        }).setEnabled(false);

        mi = cm.addMenuItem("ShowOriginalHtmlColor", CB.getSkin().getMenuIcon.showOriginalHtmlColor, () -> {
            AbstractCache actCache = EventHandler.getSelectedCache();

            actCache.setShowOriginalHtmlColor(!actCache.getShowOriginalHtmlColor());
            actCache.updateBooleanStore(Database.Data);

            DaoFactory.CACHE_DAO.updateDatabase(Database.Data, actCache, true);

            // Update cacheList
            Database.Data.cacheList.removeValue(EventHandler.getSelectedCache(), true);
            Database.Data.cacheList.add(actCache);

            //update EventHandler
            EventHandler.updateSelectedCache(actCache);

            //reload html
            CB.postOnGlThread(new NamedRunnable("reload DescriptionView") {
                @Override
                public void run() {
                    showPlatformWebView();
                }
            });
        });
        mi.setCheckable(true);
        mi.setChecked(EventHandler.getSelectedCache().getShowOriginalHtmlColor());

        return cm;
    }

    private static String getStringFromDB(Database database, String statement, long cacheID) {
        String[] args = new String[]{Long.toString(cacheID)};
        return database.getString(statement, args);
    }
}
