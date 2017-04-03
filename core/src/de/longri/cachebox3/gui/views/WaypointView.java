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
import com.badlogic.gdx.math.Rectangle;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 14.09.2016.
 */
public class WaypointView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(WaypointView.class);
    private Cache actCache;
    private Waypoint actWaypoint;
    private ListView listView;

    public WaypointView() {
        super("WaypointView");
        actCache = EventHandler.getSelectedCache();
    }

    @Override
    public void layout() {
        log.debug("Layout");
        super.layout();
        if (listView == null) addNewListView();
        log.debug("Finish Layout");
    }

    /**
     * Called when the actor's size has been changed.
     */
    protected void sizeChanged() {
        if (listView != null) {
            listView.setSize(this.getWidth(), this.getHeight());
        }
    }

    private void addNewListView() {

        log.debug("Start Thread add new listView");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                WaypointView.this.clear();
                Adapter listViewAdapter = new Adapter() {
                    @Override
                    public int getCount() {
                        if (actCache == null) return 0;
                        return actCache.waypoints.size() + 1;
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        if (index == 0) {
                            return CacheListItem.getListItem(index, actCache);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public void update(ListViewItem view) {

                        //get index from item
                        int idx = view.getListIndex();

                        // get Cache
                        Cache cache = Database.Data.Query.get(idx);

                        //get actPos and heading
                        Coordinate position = de.longri.cachebox3.events.EventHandler.getMyPosition();

                        if (position == null)
                            return; // can't update without an position

                        float heading = de.longri.cachebox3.events.EventHandler.getHeading();


                        // get coordinate from Cache or from Final Waypoint
                        Waypoint finalWp = cache.GetFinalWaypoint();
                        Coordinate finalCoord = finalWp != null ? finalWp : cache;

                        //calculate distance and bearing
                        float result[] = new float[4];
                        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, position.getLatitude(), position.getLongitude(), finalCoord.getLatitude(), finalCoord.getLongitude(), result);


                        //update item
                        if (((CacheListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true)))
                            Gdx.graphics.requestRendering();
                    }

                    @Override
                    public float getItemSize(int position) {
                        return 0;
                    }
                };

                if (WaypointView.this.listView != null) {
                    disposeListView();
                }

                WaypointView.this.listView = new ListView(listViewAdapter, false, true);
                synchronized (WaypointView.this.listView) {
                    listView.setBounds(0, 0, WaypointView.this.getWidth(), WaypointView.this.getHeight());
                    addActor(listView);
                    listView.setCullingArea(new Rectangle(0, 0, WaypointView.this.getWidth(), WaypointView.this.getHeight()));
                    listView.setSelectable(ListView.SelectableType.SINGLE);
                    Gdx.graphics.requestRendering();
                }

                // add selection changed event listener
                listView.addSelectionChangedEventListner(new ListView.SelectionChangedEvent() {
                    @Override
                    public void selectionChanged() {
                        CacheListItem selectedItem = (CacheListItem) listView.getSelectedItem();
                        int selectedItemListIndex = selectedItem.getListIndex();

                        Cache cache = Database.Data.Query.get(selectedItemListIndex);
                        log.debug("Cache selection changed to: " + cache.toString());
                        //set selected Cache global
                        de.longri.cachebox3.events.EventHandler.fire(new de.longri.cachebox3.events.SelectedCacheChangedEvent(cache));
                    }
                });

                int selectedIndex = 0;
                for (Cache cache : Database.Data.Query) {
                    if (cache.equals(de.longri.cachebox3.events.EventHandler.getSelectedCache())) {
                        break;
                    }
                    selectedIndex++;
                }

                listView.setSelectedItem(selectedIndex);
                listView.setSelectedItemVisible();
                log.debug("Finish Thread add new listView");
            }
        });
        thread.start();
        Gdx.graphics.requestRendering();
    }

    private void disposeListView() {
        final ListView disposeListView = this.listView;

        Thread disposeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                disposeListView.dispose();
            }
        });
        disposeThread.start();
    }

    @Override
    public void dispose() {

    }

    public Menu getContextMenu() {
        Menu cm = new Menu("CacheListContextMenu");

        cm.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case MenuID.MI_ADD:
                        addWP();
                        return true;
                    case MenuID.MI_WP_SHOW:
                        editWP(false);
                        return true;
                    case MenuID.MI_EDIT:
                        editWP(true);
                        return true;
                    case MenuID.MI_DELETE:
                        deleteWP();
                        return true;
                    case MenuID.MI_PROJECTION:
                        addProjection();
                        return true;
                    case MenuID.MI_FROM_GPS:
                        addMeasure();
                        return true;

                }
                return false;
            }
        });

        if (actWaypoint != null)
            cm.addItem(MenuID.MI_WP_SHOW, "show");
        if (actWaypoint != null)
            cm.addItem(MenuID.MI_EDIT, "edit");
        cm.addItem(MenuID.MI_ADD, "AddWaypoint");
        if ((actWaypoint != null) && (actWaypoint.IsUserWaypoint))
            cm.addItem(MenuID.MI_DELETE, "delete");
        if (actWaypoint != null || actCache != null)
            cm.addItem(MenuID.MI_PROJECTION, "Projection");

        cm.addItem(MenuID.MI_FROM_GPS, "FromGps");

        return cm;
    }

    private void addProjection() {

    }

    private void addMeasure() {

    }

    private void deleteWP() {

    }

    private void editWP(boolean b) {

    }

    private void addWP() {

    }

}
