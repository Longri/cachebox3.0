/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.CacheListChangedListener;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedListener;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.gui.activities.EditCache;
import de.longri.cachebox3.gui.activities.ShowDeleteMenu;
import de.longri.cachebox3.gui.activities.ShowImportMenu;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.QuickAction;
import de.longri.cachebox3.gui.menu.menuBtn1.contextmenus.Action_SelectDB_Dialog;
import de.longri.cachebox3.gui.menu.menuBtn1.contextmenus.Action_Switch_Autoresort;
import de.longri.cachebox3.gui.menu.quickBtns.Action_EditFilterSettings;
import de.longri.cachebox3.gui.menu.quickBtns.Action_SearchDialog;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.QuickButtonItem;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheWithWP;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectionType.SINGLE;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView implements CacheListChangedListener, PositionChangedListener, OrientationChangedListener {
    private final static Logger log = LoggerFactory.getLogger(CacheListView.class);
    private final float[] result = new float[4];
    private ListView listView;
    private ListViewItem[] createdItems;

    private ViewManager.ToastLength WAIT_TOAST_LENGTH = ViewManager.ToastLength.WAIT;

    public CacheListView(BitStore reader) {
        super(reader);
    }

    public CacheListView() {
        super("CacheListView CacheCount: ");

        //register as positionChanged, CacheListChanged, and OrientationChanged eventListener
        EventHandler.add(this);
    }

    @Override
    public synchronized void layout() {
        log.debug("Layout");
        super.layout();
        if (listView == null) {
            addNewListView();
        }
    }

    private void addNewListView() {
        log.debug("Start Thread add new listView");

        this.clear();
        createdItems = new ListViewItem[Database.Data.cacheList.size];
        ListViewAdapter listViewAdapter = new ListViewAdapter() {

            boolean outDated = false;

            @Override
            public int getCount() {
                if (outDated) return 0;
                return Database.Data.cacheList.size;
            }

            @Override

            public ListViewItem getView(int index) {
                if (outDated || Database.Data.cacheList.size == 0) {
                    createdItems[index] = null;
                    return null;
                }
                ListViewItem item = new CacheListItem(index, Database.Data.cacheList.get(index), getWidth());
                createdItems[index] = item;
                return item;
            }

            @Override
            public void update(ListViewItem view) {
                if (outDated) return;

                //get index from item
                int idx = view.getListIndex();

                if (idx > Database.Data.cacheList.getSize()) {
                    // Cachelist is changed, reload!
                    outDated = true;
                    addNewListView();
                    return;
                }

                // get Cache
                AbstractCache abstractCache = Database.Data.cacheList.get(idx);

                //get actPos and heading
                Coordinate position = EventHandler.getMyPosition();

                if (position == null)
                    return; // can't update without an position

                float heading = EventHandler.getHeading();

                // get coordinate from Cache or from Final Waypoint
                AbstractWaypoint finalWp = abstractCache.getFinalWaypoint();
                Coordinate finalCoord = finalWp != null ? finalWp : abstractCache;

                //calculate distance and bearing
                MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, position.getLatitude(), position.getLongitude(), finalCoord.getLatitude(), finalCoord.getLongitude(), result);

                //update item
                if (((CacheListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true)))
                    CB.requestRendering();
            }

        };

        if (this.listView != null) {
            log.warn("Dispose ListView");
            disposeListView();
        }

        this.listView = new ListView(VERTICAL);
        this.listView.setEmptyString(Translation.get("EmptyCacheList"));
        this.addActor(listView);
        this.listView.setAdapter(listViewAdapter);

        this.listView.setEmptyString(Translation.get("EmptyCacheList"));
        this.listView.setBounds(0, 0, this.getWidth(), this.getHeight());

        this.listView.setCullingArea(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
        this.listView.setSelectionType(SINGLE);

        // add selection changed event listener
        this.listView.addSelectionChangedEventListner(() -> {
            CacheListItem selectedItem = (CacheListItem) listView.getSelectedItem();
            int selectedItemListIndex = selectedItem.getListIndex();
            AbstractCache cache = Database.Data.cacheList.get(selectedItemListIndex);
            log.debug("Cache selection changed to: " + cache.toString());
            //set selected Cache global
            EventHandler.fire(new SelectedCacheChangedEvent(cache));
        });

        CB.postOnNextGlThread(() -> {
            int selectedIndex = 0;
            for (AbstractCache abstractCache : Database.Data.cacheList) {
                if (abstractCache.equals(EventHandler.getSelectedCache())) {
                    break;
                }
                selectedIndex++;
            }
            try {
                if (selectedIndex >= Database.Data.cacheList.size)
                    selectedIndex = 0;// select first item, if Cache not found
                if (Database.Data.cacheList.size > 0) {
                    listView.setSelection(selectedIndex);
                    listView.setSelectedItemVisible(false);
                }
            } catch (Exception e) {
                log.error("setSelected index", e);
            }
            CB.requestRendering();
            log.debug("Finish Thread add new listView");
            if (WAIT_TOAST_LENGTH != null) WAIT_TOAST_LENGTH.close();
            WAIT_TOAST_LENGTH = null;
        });

        CB.requestRendering();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    private void disposeListView() {
        final ListView disposeListView = this.listView;
        CB.postAsync(new NamedRunnable("CacheListView:disposeListView") {
            @Override
            public void run() {
                disposeListView.dispose();
            }
        });
        this.listView = null;
    }


    @Override
    public void dispose() {
        if (this.listView != null) this.listView.dispose();
        this.listView = null;
        EventHandler.remove(this);
        if (listView != null) listView.dispose();
        listView = null;
        int n = createdItems.length;
        while (--n >= 0) {
            if (createdItems[n] != null) createdItems[n].dispose();
            createdItems[n] = null;
        }
        createdItems = null;
    }

    /**
     * Called when the actor's size has been changed.
     */
    protected void sizeChanged() {
        if (listView != null) {
            listView.setSize(this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void cacheListChanged(CacheListChangedEvent event) {
        CB.postOnGlThread(new NamedRunnable("CacheListView: CacheListChanged") {
            @Override
            public void run() {
                log.debug("Cachelist changed, reload listView");
                listView = null;
                layout();
            }
        });
    }

    private void setChangedFlagToAllItems() {
        if (listView == null) return;
        SnapshotArray<Actor> allItems = listView.items();
        Object[] actors = allItems.begin();
        for (int i = 0, n = allItems.size; i < n; i++) {
            if (actors[i] instanceof CacheListItem) {
                CacheListItem item = (CacheListItem) actors[i];
                item.posOrBearingChanged();
            }
        }
        allItems.end();
        CB.requestRendering();
    }

    @Override
    public void onShow() {
        super.onShow();
        /*
        log.debug("CacheListView onShow (with resort)");
        resort();
        CB.requestRendering();
         */
    }

    @Override
    public void onHide() {
        super.onHide();
        CB.requestRendering();
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        setChangedFlagToAllItems();
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        setChangedFlagToAllItems();
    }

    public String toString() {
        return "CacheListView";
    }

    public void setWaitToastLength(ViewManager.ToastLength wait_toast_length) {
        this.WAIT_TOAST_LENGTH = wait_toast_length;
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        final Menu cm = new Menu("CacheListViewTitle");
        cm.addMenuItem("ResortList", CB.getSkin().menuIcon.sortIcon, this::resort);
        cm.addMenuItem("Filter", CB.getSkin().menuIcon.filterIcon, () -> new Action_EditFilterSettings().execute());
        cm.addMenuItem("MI_RESET_FILTER", CB.getSkin().menuIcon.resetFilterIcon, () -> CB.viewmanager.setNewFilter(FilterInstances.ALL));
        cm.addMenuItem("Search", CB.getSkin().menuIcon.searchIcon, () -> new Action_SearchDialog().execute());
        cm.addMoreMenuItem("importExport", "", CB.getSkin().menuIcon.importIcon, new ShowImportMenu());
        cm.addCheckableMenuItem("setOrResetFavorites", "", CB.getSkin().menuIcon.favorite, true, setOrResetFavorites(cm));
        cm.addMenuItem("manage", getSelectDBTitleExtension(), CB.getSkin().menuIcon.manageDB, this::selectDbDialog);
        cm.addMenuItem("AutoResort", CB.getAutoResort() ? CB.getSkin().menuIcon.autoSortOnIcon : CB.getSkin().menuIcon.autoSortOffIcon, this::setAutoResort);
        cm.addMenuItem("MI_NEW_CACHE", CB.getSkin().menuIcon.addCache, this::createCache);
        cm.addMoreMenuItem("DeleteCaches", "", CB.getSkin().menuIcon.deleteCaches, new ShowDeleteMenu());
        cm.addMenuItem("ClearHistory",  VisUI.getSkin().getDrawable("history48"), this::clearHistory);
        return cm;
    }

    private void clearHistory() {
        CB.cacheHistory = "";
        if (CB.viewmanager.getActFilter().isHistory) {
            CB.viewmanager.setNewFilter(FilterInstances.ALL);
        }
    }

    private void resort() {
        log.debug("resort cacheList");
        Database.Data.cacheList.resort(EventHandler.getSelectedCoordinate(), new CacheWithWP(EventHandler.getSelectedCache(), EventHandler.getSelectedWayPoint()));
        // Database.Data.cacheList.resort(null, null);
        log.debug("Finish resort cacheList");
    }

    private String getSelectDBTitleExtension() {
        String DBName = Database.Data == null || !Database.Data.isStarted() ? Translation.get("noDB").toString() : Database.Data.getPath();
        try {
            int pos = DBName.lastIndexOf("/");
            if (pos < 0) pos = DBName.lastIndexOf("\\");
            DBName = DBName.substring(pos + 1);
            pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, pos);
        } catch (Exception e) {
            DBName = "???";
        }
        return "  (" + DBName + ")";
    }

    private ClickListener setOrResetFavorites(final Menu cm) {
        return new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (cm.mustHandle(event)) {
                    MenuItem mi = (MenuItem) event.getListenerActor();
                    boolean checked = mi.isChecked();
                    if (event.getTarget().toString().contains("Drawable")) {
                        // checkbox clicked
                        checked = !checked;
                    }
                    mi.setChecked(!mi.isChecked());
                    String msgText;
                    if (checked) {
                        msgText = "askSetFavorites";
                    } else {
                        msgText = "askResetFavorites";
                    }
                    final boolean finalchecked = checked;
                    MessageBox.show(Translation.get(msgText), Translation.get("Favorites"), MessageBoxButton.OKCancel, MessageBoxIcon.Question, (which, data) -> {
                        if (which == ButtonDialog.BUTTON_POSITIVE) {
                            Database.Data.beginTransaction();
                            for (AbstractCache cache : Database.Data.cacheList) {
                                try {
                                    cache.setFavorite(finalchecked);
                                    cache.updateBooleanStore();
                                } catch (Exception exc) {
                                    log.error("Update_Favorite", exc);
                                }
                            }
                            Database.Data.endTransaction();
                            EventHandler.fire(new CacheListChangedEvent());
                        }
                        return true;
                    });
                }
            }
        };
    }

    private void selectDbDialog() {
        CB.postAsync(
                new NamedRunnable("CacheListView:showSelectDbDialog") {
                    @Override
                    public void run() {
                        new Action_SelectDB_Dialog(Action_SelectDB_Dialog.ViewMode.ASK).execute();
                    }
                });
    }

    private void createCache() {
        EditCache.getInstance(Database.Data, "MI_NEW_CACHE", CB.getSkin().menuIcon.addCache).create();
    }

    private void setAutoResort() {
        for (QuickButtonItem qbi : CB.viewmanager.getSlider().getQuickButtonList().getQuickButtonList()) {
            if (qbi.getAction() == QuickAction.AutoResort) {
                qbi.clicked(); // executes and sets corresponding icon
                return;
            }
        }
        new Action_Switch_Autoresort().execute();
    }
}
