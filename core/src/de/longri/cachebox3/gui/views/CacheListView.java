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
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.CacheListChangedListener;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedListener;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.gui.actions.ShowDeleteMenu;
import de.longri.cachebox3.gui.actions.ShowImportMenu;
import de.longri.cachebox3.gui.actions.show_activities.Action_EditFilterSettings;
import de.longri.cachebox3.gui.actions.show_activities.Action_SelectDB_Dialog;
import de.longri.cachebox3.gui.activities.EditCache;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.list_view.SelectionChangedEvent;
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
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.SINGLE;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView implements CacheListChangedListener, PositionChangedListener, OrientationChangedListener {
    private final static Logger log = LoggerFactory.getLogger(CacheListView.class);
    private final float result[] = new float[4];
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

    public void resort() {
        log.debug("resort cacheList");
        Database.Data.cacheList.resort(EventHandler.getSelectedCoord(),
                new CacheWithWP(EventHandler.getSelectedCache(), EventHandler.getSelectedWaypoint()));
        log.debug("Finish resort cacheList");
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
                ListViewItem item = CacheListItem.getListItem(index, Database.Data.cacheList.get(index), getWidth());
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
                AbstractWaypoint finalWp = abstractCache.GetFinalWaypoint();
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
        this.listView.setSelectable(SINGLE);

        // add selection changed event listener
        this.listView.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                CacheListItem selectedItem = (CacheListItem) CacheListView.this.listView.getSelectedItem();
                int selectedItemListIndex = selectedItem.getListIndex();

                AbstractCache cache = Database.Data.cacheList.get(selectedItemListIndex);
                log.debug("Cache selection changed to: " + cache.toString());
                //set selected Cache global
                EventHandler.fire(new SelectedCacheChangedEvent(cache));
            }
        });

        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
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
                        CacheListView.this.listView.setSelection(selectedIndex);
                        CacheListView.this.listView.setSelectedItemVisible(false);
                    }
                } catch (Exception e) {
                    log.error("setSelected index", e);
                }
                CB.requestRendering();
                log.debug("Finish Thread add new listView");
                if (WAIT_TOAST_LENGTH != null) WAIT_TOAST_LENGTH.close();
                WAIT_TOAST_LENGTH = null;
            }
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
        final Menu cm = new Menu("CacheListViewTitle");

        if (!(CB.viewmanager.getActView() instanceof CacheListView))
            return null;

        final CacheListView cacheListView = (CacheListView) CB.viewmanager.getActView();

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

        MenuItem mi;
        cm.addMenuItem("ResortList", CB.getSkin().getMenuIcon.sortIcon, () -> cacheListView.resort());
        cm.addMenuItem("Filter", CB.getSkin().getMenuIcon.filterIcon, () -> new Action_EditFilterSettings().execute());
        cm.addMenuItem("MI_RESET_FILTER", CB.getSkin().getMenuIcon.resetFilterIcon, () -> CB.viewmanager.setNewFilter(FilterInstances.ALL));
        cm.addMenuItem("Search", CB.getSkin().getMenuIcon.searchIcon, () -> CB.viewmanager.toast("NOT IMPLEMENTED")).setEnabled(false);// todo ISSUE (#115 Add search Dialog for ListView)
        /*
        if (SearchDialog.that == null) {
            new SearchDialog();
        }
        SearchDialog.that.showNotCloseAutomatically();
         */
        cm.addMenuItem("importExport", CB.getSkin().getMenuIcon.importIcon, () -> {}).setMoreMenu(new ShowImportMenu());
        cm.addMenuItem("manage", "  (" + DBName + ")", CB.getSkin().getMenuIcon.manageDB, () -> CB.postAsync(
                new NamedRunnable("CacheListView:showSelectDbDialog") {
                    @Override
                    public void run() {
                        new Action_SelectDB_Dialog(Action_SelectDB_Dialog.ViewMode.ASK).execute();
                    }
                })
        );
        mi = cm.addMenuItem("AutoResort", null, () -> CB.viewmanager.toast("NOT IMPLEMENTED")); // todo ISSUE (#116 addAutoResort)   icon: CB.getSkin().getMenuIcon.MI_AUTO_RESORT
        mi.setEnabled(false);
        /*
        CB.setAutoResort(!CB.getAutoResort());
        if (CB.getAutoResort()) {
            synchronized (Database.Data.cacheList) {
                Database.Data.cacheList.resort(CB.getSelectedCoord(), new CacheWithWP(CB.getSelectedCache(), CB.getSelectedWaypoint()));
            }
        }
         */
        mi.setCheckable(true);
        mi.setChecked(CB.getAutoResort());
        cm.addMenuItem("MI_NEW_CACHE", CB.getSkin().getMenuIcon.addCacheIcon, () -> (new EditCache("editCache")).create()).setEnabled(false); //todo ISSUE (#118 add new Cache)
        cm.addMenuItem("DeleteCaches", CB.getSkin().getMenuIcon.deleteCaches, () -> {}).setMoreMenu(new ShowDeleteMenu());
        return cm;
    }

}
