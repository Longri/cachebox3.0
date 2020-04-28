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
import de.longri.cachebox3.PlatformWebView;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.actions.extendsAbstractAction.ContactOwner;
import de.longri.cachebox3.gui.actions.extendsAbstractAction.ListsAtGroundSpeak;
import de.longri.cachebox3.gui.activities.DeleteCaches;
import de.longri.cachebox3.gui.activities.EditCache;
import de.longri.cachebox3.gui.activities.ReloadCacheActivity;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.apis.GroundspeakAPI.OK;
import static de.longri.cachebox3.gui.dialogs.ButtonDialog.BUTTON_NEGATIVE;
import static de.longri.cachebox3.gui.dialogs.ButtonDialog.BUTTON_POSITIVE;

/**
 * Created by Longri on 14.09.2016.
 */
public class DescriptionView extends AbstractView implements SelectedCacheChangedListener {

    private static final Logger log = LoggerFactory.getLogger(DescriptionView.class);

    private static long lastCacheId;
    private static float lastX, lastY, lastScale;
    private final Array<String> nonLocalImages = new Array<>();
    private final Array<String> nonLocalImagesUrl = new Array<>();
    private final AtomicBoolean FIRST = new AtomicBoolean(true);
    private PlatformWebView view;
    private final GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack = url -> {
        log.debug("Load Url callback: {}", url);
        if (FIRST.get()) {
            FIRST.set(false);
            boundsChanged(DescriptionView.this.getX(), DescriptionView.this.getY(), DescriptionView.this.getWidth(), DescriptionView.this.getHeight());
        }

        // contains fake://fake.de?GetAttInfo
        if (url.contains("GetAttInfo")) {
            // the url is missing the name=value on different devices (perhaps dependant from chromium), so we give that appended to the name and the blank
            int pos = url.indexOf("+"); // the Blank is converted to + in url
            // 25 is the length of "fake://fake.de?GetAttInfo"
            if (pos > 0)
                // todo a nicer box
                MessageBox.show(Translation.get(url.substring(25, pos)));
            // todo scale of Descriptionview changes sometime (bigger)after showing msgbox
            return true;
        } else if (url.contains("fake://fake.de/download")) {
            // not yet tested
            Thread thread = new Thread(() -> {

                GroundspeakAPI.getInstance().fetchMyCacheLimits();
                if (GroundspeakAPI.getInstance().APIError != OK) {
                    MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get("Friends"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                    // onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
                    return;
                }
                if (GroundspeakAPI.getInstance().isDownloadLimitExceeded()) {
                    String msg;
                    if (GroundspeakAPI.getInstance().isPremiumMember()) {
                        msg = "You have left " + GroundspeakAPI.getInstance().fetchMyUserInfos().remaining + " full and " + GroundspeakAPI.getInstance().fetchMyUserInfos().remainingLite + " lite caches.";
                        msg += "The time to wait is " + GroundspeakAPI.getInstance().fetchMyUserInfos().remainingTime + "/" + GroundspeakAPI.getInstance().fetchMyUserInfos().remainingLiteTime;
                    } else {
                        msg = "Upgrade to Geocaching.com Premium Membership today\n"
                                + "for as little at $2.50 per month\n"
                                + "to download the full details for up to 6000 caches per day,\n"
                                + "view all cache types in your area,\n"
                                + "and access many more benefits. \n"
                                + "Visit Geocaching.com to upgrade.";
                    }

                    //message = msg;
                    //onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));
                    MessageBox.show(msg, Translation.get("download"), MessageBoxButton.OK, MessageBoxIcon.Information, null);

                    return;
                }
                /*
                if (!GroundspeakAPI.getInstance().isPremiumMember()) {
                    String msg = "Download Details of this cache?\n";
                    msg = msg + ("Full Downloads left: " + GroundspeakAPI.getInstance().fetchMyUserInfos().remaining + "\n");
                    message = msg;
                    onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(3));
                }
                else {
                    // call the download directly
                    onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(4));
                }
                */
            });
            // pd = ProgressDialog.show(getContext(), "", "Download Description", true);
            thread.start();
            return true;
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            // Load Url in ext Browser
            log.debug("Link clicked, don't load URL! show on ext browser");
            PlatformConnector.callUrl(url);
            return true;
        } else if (url.equals("about:blank")) {
            // Load description
            return false;
        }
        return false;
    };

    public DescriptionView(BitStore reader) {
        super(reader);
    }

    public DescriptionView() {
        super("DescriptionView");
        EventHandler.add(this);
    }

    private static String getStringFromDB(Database database, String statement, long cacheID) {
        String[] args = new String[]{Long.toString(cacheID)};
        return database.getString(statement, args);
    }

    private String getAttributesHtml(AbstractCache abstractCache) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Attributes attribute : abstractCache.getAttributes()) {
                File result = new File(CB.WorkPath + "/data/Attributes/" + attribute.getImageName() + ".png");
                // the url is missing the value, so we give that appended in the name and the blank
                sb.append("<input name=\"GetAttInfo")
                        .append(attribute.getImageName())
                        .append(" \" height=\"40\" width=\"40\" type=\"image\" src=\"file://")
                        .append(result.getAbsolutePath())
                        .append("\" value=\"1\">");
            }
            if (sb.length > 0) {
                return "<form action=\"Attr\">" + sb.toString() + "</form><br>";
            } else return "";
        } catch (Exception ex) {
            log.error("getAttributesHtml:", ex);
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
            PlatformConnector.getDescriptionView(descriptionView -> {
                view = descriptionView;
                view.setShouldOverrideUrlLoadingCallBack(shouldOverrideUrlLoadingCallBack);
                WAIT.set(false);
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
            String html;
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
            if (view != null) {
                view.display();
                view.setHtml(html);
            }
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
        return getContextMenu(true);
    }

    public Menu getContextMenu(boolean forDescription) {
        Menu cacheContextMenu = new Menu("DescriptionViewTitle");
        AbstractCache geoCache = EventHandler.getSelectedCache();
        boolean selectedCacheIsSet = (geoCache != null);

        if (selectedCacheIsSet) {
            boolean selectedCacheIsGC = geoCache.getGeoCacheCode().toString().startsWith("GC");
            if (forDescription) {
                cacheContextMenu.addCheckableMenuItem("ShowOriginalHtmlColor", "", CB.getSkin().getMenuIcon.showOriginalHtmlColor,
                        geoCache.getShowOriginalHtmlColor(), this::showOriginalHtmlColor);
                cacheContextMenu.addDivider(0);
            }
            if (!forDescription) {
                cacheContextMenu.addCheckableMenuItem("CacheContextMenuShortClickToggle", Config.CacheContextMenuShortClickToggle.getValue(), this::toggleShortClick);
                /*
                cacheContextMenu.addMoreMenu(ShowDrafts.getInstance().getContextMenu(), Translation.get("DraftsContextMenuTitle"), Translation.get("DraftsContextMenuTitle"));
                 */
            }
            if (selectedCacheIsGC) {
                cacheContextMenu.addMenuItem("ReloadCacheAPI", CB.getSkin().getMenuIcon.reloadCacheIcon, () -> new ReloadCacheActivity().show());
            }
            cacheContextMenu.addMenuItem("Open_Cache_Link", CB.getSkin().getMenuIcon.gc_logo, () -> PlatformConnector.callUrl(geoCache.getUrl().toString()));
            // CB.getSkin()."big" + geoCache.getType().name()),
            // getType -> getGeoCacheType
            cacheContextMenu.addCheckableMenuItem("Favorite", "", CB.getSkin().getMenuIcon.favorit, geoCache.isFavorite(), this::toggleAsFavorite);
            cacheContextMenu.addMenuItem("MI_EDIT_CACHE", CB.getSkin().getMenuIcon.reloadCacheIcon,
                    () -> EditCache.getInstance(Database.Data, "MI_EDIT_CACHE", CB.getSkin().getMenuIcon.reloadCacheIcon).edit(geoCache));
            if (selectedCacheIsGC) {
                cacheContextMenu.addMenuItem("contactOwner", ContactOwner.getInstance().getIcon(), () -> ContactOwner.getInstance().execute());
                cacheContextMenu.addMenuItem("GroundSpeakLists", ListsAtGroundSpeak.getInstance().getIcon(), () -> ListsAtGroundSpeak.getInstance().execute());
            }
            if (!Config.rememberedGeoCache.getValue().contentEquals(geoCache.getGeoCacheCode())) {
                cacheContextMenu.addCheckableMenuItem("rememberGeoCache", "", null,
                        Config.rememberedGeoCache.getValue().equals(geoCache.getGeoCacheCode().toString()), this::rememberGeoCache);
            }
            cacheContextMenu.addMenuItem("MI_DELETE_CACHE", CB.getSkin().getMenuIcon.deleteCaches, this::deleteGeoCache);
        }

        cacheContextMenu.addDivider(1);
        cacheContextMenu.addMenuItem("Solver", CB.getSkin().getMenuIcon.todo, () -> {
            // replace icon with CB.getSkin().getMenuIcon.solverIcon
            SolverView view = new SolverView();
            CB.viewmanager.showView(view);
        }).setEnabled(false);

        return cacheContextMenu;
    }

    private void toggleAsFavorite() {
        AbstractCache selectedCache = EventHandler.getSelectedCache();

        selectedCache.setFavorite(!selectedCache.isFavorite());
        selectedCache.updateBooleanStore();

        DaoFactory.CACHE_DAO.updateDatabase(Database.Data, selectedCache, true);

        // Update cacheList
        Database.Data.cacheList.removeValue(EventHandler.getSelectedCache(), true);
        Database.Data.cacheList.add(selectedCache);

        //update EventHandler
        EventHandler.updateSelectedCache(selectedCache);
        EventHandler.fire(new CacheListChangedEvent());
    }

    private void rememberGeoCache() {
        ButtonDialog mb = new ButtonDialog("rememberGeoCache",
                Translation.get("rememberThisOrSelectRememberedGeoCache"),
                Translation.get("rememberGeoCacheTitle"),
                MessageBoxButton.AbortRetryIgnore,
                MessageBoxIcon.Question,
                (which, data) -> {
                    if (which == BUTTON_POSITIVE) {
                        Config.rememberedGeoCache.setValue(EventHandler.getSelectedCache().getGeoCacheCode().toString());
                        Config.AcceptChanges();
                    } else if (which == BUTTON_NEGATIVE) {
                        Config.rememberedGeoCache.setValue("");
                        Config.AcceptChanges();
                    } else {
                        AbstractCache rememberedCache = Database.Data.cacheList.getCacheByGcCode(Config.rememberedGeoCache.getValue());
                        if (rememberedCache != null) {
                            EventHandler.fireSelectedWaypointChanged(rememberedCache, null);
                        }
                    }
                    return true;
                });
        mb.setButtonText("rememberGeoCache", "selectGeoCache", "forgetGeoCache");
        mb.show();
    }

    private void showOriginalHtmlColor() {
        AbstractCache actCache = EventHandler.getSelectedCache();

        actCache.setShowOriginalHtmlColor(!actCache.getShowOriginalHtmlColor());
        actCache.updateBooleanStore();

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
    }

    private void deleteGeoCache() {
        ButtonDialog mb = new ButtonDialog("", Translation.get("sure"), Translation.get("question"), MessageBoxButton.OKCancel, MessageBoxIcon.Question,
                (which, data) -> {
                    if (which == BUTTON_POSITIVE) {
                        new DeleteCaches().deleteCaches("SELECT * FROM CacheCoreInfo core WHERE Id = " + EventHandler.getSelectedCache().getId());
                        EventHandler.fire(new CacheListChangedEvent());
                        EventHandler.fireSelectedWaypointChanged(Database.Data.cacheList.first(), null);
                    }
                    return true;
                });
        mb.show();
    }


    private void toggleShortClick() {
        ButtonDialog mb = new ButtonDialog("", Translation.get("CacheContextMenuShortClickToggleQuestion"), Translation.get("CacheContextMenuShortClickToggleTitle"), MessageBoxButton.YesNo, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BUTTON_POSITIVE)
                        Config.CacheContextMenuShortClickToggle.setValue(false);
                    else
                        Config.CacheContextMenuShortClickToggle.setValue(true);
                    Config.AcceptChanges();
                    return true;
                });
        mb.show();
    }

}
