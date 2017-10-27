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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.actions.ShowImportMenu;
import de.longri.cachebox3.gui.actions.show_activities.Action_ShowFilterSettings;
import de.longri.cachebox3.gui.actions.show_activities.Action_Show_SelectDB_Dialog;
import de.longri.cachebox3.gui.activities.CheckStateActivity;
import de.longri.cachebox3.gui.activities.EditCache;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheWithWP;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView implements CacheListChangedEventListener, PositionChangedListener, OrientationChangedListener {
    final static Logger log = LoggerFactory.getLogger(CacheListView.class);
    private ListView listView;
    private final float result[] = new float[4];
    private final AtomicBoolean ON_LAYOUT_WORK = new AtomicBoolean(false);

    private ViewManager.ToastLength WAIT_TOAST_LENGTH = ViewManager.ToastLength.WAIT;

    public CacheListView() {
        super("CacheListView CacheCount: " + Database.Data.Query.size);
        CB.viewmanager.toast(Translation.get("LoadCacheList"), WAIT_TOAST_LENGTH);

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);

        //register as positionChanged eventListener
        EventHandler.add(this);
    }

    @Override
    public synchronized void layout() {
        log.debug("Layout");
        super.layout();
        if (listView == null) addNewListView();
        log.debug("Finish Layout");
    }

    public void resort() {
        log.debug("resort Query");
        Database.Data.Query.resort(EventHandler.getSelectedCoord(),
                new CacheWithWP(EventHandler.getSelectedCache(), EventHandler.getSelectedWaypoint()));
        log.debug("Finish resort Query");
    }


    private void addNewListView() {
        log.debug("Start Thread add new listView");
        ON_LAYOUT_WORK.set(true);
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                CacheListView.this.clear();
                Adapter listViewAdapter = new Adapter() {

                    boolean outDated = false;

                    @Override
                    public int getCount() {
                        if (outDated) return 0;
                        return Database.Data.Query.size;
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        if (outDated) return null;
                        if (Database.Data.Query.size == 0) return null;
                        return CacheListItem.getListItem(index, Database.Data.Query.get(index));
                    }

                    @Override
                    public void update(ListViewItem view) {
                        if (outDated) return;

                        //get index from item
                        int idx = view.getListIndex();

                        if (idx > Database.Data.Query.getSize()) {
                            // Cachelist is changed, reload!
                            outDated = true;
                            addNewListView();
                            return;
                        }

                        // get Cache
                        AbstractCache abstractCache = Database.Data.Query.get(idx);

                        //get actPos and heading
                        Coordinate position = EventHandler.getMyPosition();

                        if (position == null)
                            return; // can't update without an position

                        float heading = EventHandler.getHeading();

                        // get coordinate from Cache or from Final Waypoint
                        AbstractWaypoint finalWp = abstractCache.GetFinalWaypoint();
                        Coordinate finalCoord = finalWp != null ? finalWp : abstractCache;

                        //calculate distance and bearing
                        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, position.getLatitude(), position.getLongitude(), finalCoord.getLatitude(), finalCoord.getLongitude(), result);

                        //update item
                        if (((CacheListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true)))
                            CB.requestRendering();
                    }

                    @Override
                    public float getItemSize(int position) {
                        return 0;
                    }
                };

                if (CacheListView.this.listView != null) {
                    disposeListView();
                }

                CacheListView.this.listView = new ListView(listViewAdapter, false, true);
                synchronized (CacheListView.this.listView) {
                    listView.setEmptyString(Translation.get("EmptyCacheList"));
                    listView.setBounds(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight());
                    addActor(listView);
                    listView.setCullingArea(new Rectangle(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight()));
                    listView.setSelectable(ListView.SelectableType.SINGLE);
                    CB.requestRendering();
                }

                // add selection changed event listener
                listView.addSelectionChangedEventListner(new ListView.SelectionChangedEvent() {
                    @Override
                    public void selectionChanged() {
                        CacheListItem selectedItem = (CacheListItem) listView.getSelectedItem();
                        int selectedItemListIndex = selectedItem.getListIndex();

                        AbstractCache cache = Database.Data.Query.get(selectedItemListIndex);
                        log.debug("Cache selection changed to: " + cache.toString());
                        //set selected Cache global
                        EventHandler.fire(new SelectedCacheChangedEvent(cache));
                    }
                });

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        int selectedIndex = 0;
                        for (AbstractCache abstractCache : Database.Data.Query) {
                            if (abstractCache.equals(EventHandler.getSelectedCache())) {
                                break;
                            }
                            selectedIndex++;
                        }
                        try {
                            listView.setSelection(selectedIndex);
                            listView.setSelectedItemVisible(false);
                        } catch (Exception e) {
                            log.error("setSelected index", e);
                        }
                        CB.requestRendering();
                        log.debug("Finish Thread add new listView");
                        ON_LAYOUT_WORK.set(false);
                        if (WAIT_TOAST_LENGTH != null) WAIT_TOAST_LENGTH.close();
                        WAIT_TOAST_LENGTH = null;
                    }
                });
                CB.requestRendering();
            }
        });
        CB.requestRendering();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (ON_LAYOUT_WORK.get()) CB.requestRendering();
    }

    private void disposeListView() {
        final ListView disposeListView = this.listView;
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                disposeListView.dispose();
            }
        });
        this.listView = null;
    }


    @Override
    public void dispose() {
        disposeListView();
        CacheListChangedEventList.Remove(this);
        EventHandler.remove(this);
        if (listView != null) listView.dispose();
        listView = null;
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
    public void CacheListChangedEvent() {
        log.debug("Cachelist changed, reload listView");
        listView = null;
        layout();
    }

    private void setChangedFlagToAllItems() {

        if (listView == null || ON_LAYOUT_WORK.get()) return;
        SnapshotArray<ListViewItem> allItems = listView.items();
        Object[] actors = allItems.begin();
        for (int i = 0, n = allItems.size; i < n; i++) {
            CacheListItem item = (CacheListItem) actors[i];
            item.posOrBearingChanged();
        }
        allItems.end();
        CB.requestRendering();
    }

    @Override
    public void onShow() {
        super.onShow();
        resort();
        CB.requestRendering();
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
        final Menu cm = new Menu("CacheListContextMenu");


        if (!(CB.viewmanager.getActView() instanceof CacheListView))
            return null;

        final CacheListView cacheListView = (CacheListView) CB.viewmanager.getActView();

        cm.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                EditCache editCache = null;
                switch (item.getMenuItemId()) {
                    case MenuID.MI_RESORT:
                        cacheListView.resort();
                        return true;
                    case MenuID.MI_FilterSet:
                        new Action_ShowFilterSettings().execute();
                        return true;
                    case MenuID.MI_RESET_FILTER:
                        CB.viewmanager.toast("RESET FILTER NOT IMPLEMENTED NOW");
//                        FilterInstances.setLastFilter(new FilterProperties());
//                        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
                        return true;
                    case MenuID.MI_SEARCH_LIST:
                        CB.viewmanager.toast("Search NOT IMPLEMENTED NOW");
//                        if (SearchDialog.that == null) {
//                            new SearchDialog();
//                        }
//
//                        SearchDialog.that.showNotCloseAutomaticly();

                        return true;
                    case MenuID.MI_IMPORT:
                        // do nothing, will show mor menu
                        return true;
                    case MenuID.MI_SYNC:
                        CB.viewmanager.toast("Sync NOT IMPLEMENTED NOW");
//                        SyncActivity sync = new SyncActivity();
//                        sync.show();
                        return true;
                    case MenuID.MI_MANAGE_DB:
                        CB.postAsync(new Runnable() {
                            @Override
                            public void run() {
                                new Action_Show_SelectDB_Dialog(Action_Show_SelectDB_Dialog.ViewMode.ASK).execute();
                            }
                        });
                        return true;
                    case MenuID.MI_AUTO_RESORT:
                        CB.viewmanager.toast("Toggle Autoresort NOT IMPLEMENTED NOW");

//                        CB.setAutoResort(!(CB.getAutoResort()));
//                        if (CB.getAutoResort()) {
//                            synchronized (Database.Data.Query) {
//                                Database.Data.Query.resort(CB.getSelectedCoord(), new CacheWithWP(CB.getSelectedCache(), CB.getSelectedWaypoint()));
//                            }
//                        }
                        return true;
                    case MenuID.MI_CHK_STATE_API:
                        new CheckStateActivity().show();
                        return true;

                    case MenuID.MI_NEW_CACHE:
                        if (editCache == null)
                            editCache = new EditCache("editCache");
                        editCache.create();
                        return true;

                    case MenuID.AID_SHOW_DELETE_DIALOG:
                        CB.viewmanager.toast("deleteIcon NOT IMPLEMENTED NOW");
                        //   TabMainView.actionDelCaches.execute();
                        return true;
                }
                return false;
            }

        });

        String DBName = Database.Data == null || !Database.Data.isStarted() ? Translation.get("noDB") : Database.Data.getPath();
        try {
            int pos = DBName.lastIndexOf("/");
            if (pos < 0) pos = DBName.lastIndexOf("\\");
            DBName = DBName.substring(pos + 1);
            pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, pos);
        } catch (Exception e) {
            DBName = "???";
        }

        MenuItem mi;
        cm.addItem(MenuID.MI_RESORT, "ResortList", CB.getSkin().getMenuIcon.sortIcon);
        //ISSUE (#114 add FilterView)   cm.addItem(MenuID.MI_FilterSet, "filter", CB.getSkin().getMenuIcon.filterIcon);
        //ISSUE (#114 add FilterView)  cm.addItem(MenuID.MI_RESET_FILTER, "MI_RESET_FILTER", CB.getSkin().getMenuIcon.resetFilterIcon);
        //ISSUE (#115 Add search Dialog for ListView) cm.addItem(MenuID.MI_SEARCH_LIST, "search", CB.getSkin().getMenuIcon.searchIcon);
        mi = cm.addItem(MenuID.MI_IMPORT, "importExport", CB.getSkin().getMenuIcon.importIcon);
        mi.setMoreMenu(new ShowImportMenu());
        cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", CB.getSkin().getMenuIcon.manageDB);
        //ISSUE (#116 addAutoResort)  mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
        // mi.setCheckable(true);
        // mi.setChecked(CB.getAutoResort());


        cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", CB.getSkin().getMenuIcon.GC_Live);
        //ISSUE (#118 add new Cache) cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", CB.getSkin().getMenuIcon.addCacheIcon);
        //ISSUE (#119 add delete Cache Dialog) cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", CB.getSkin().getMenuIcon.deleteIcon);

        return cm;
    }
}
