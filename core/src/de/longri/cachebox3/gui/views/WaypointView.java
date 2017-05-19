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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedWayPointChangedEvent;
import de.longri.cachebox3.gui.activities.EditWaypoint;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.WaypointDAO;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;
import org.oscim.event.Event;
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
    public void onShow() {
        Gdx.graphics.requestRendering();
        log.debug("onShow");
    }

    @Override
    public void layout() {
        log.debug("Layout");
        super.layout();
        if (listView == null) addNewListView();
        log.debug("Finish Layout");
        Gdx.graphics.requestRendering();
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
                        return actCache.waypoints.size + 1;
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        if (index == 0) {
                            return CacheListItem.getListItem(index, actCache);
                        } else {
                            WayPointListItem item = WayPointListItem.getListItem(index, actCache.waypoints.get(index - 1));

                            item.setLongCLickListener(new ListViewItem.ItemClickListener() {
                                @Override
                                public void click(ListViewItem item) {
                                    actWaypoint = actCache.waypoints.get(item.getListIndex() - 1);
                                    if (WaypointView.this.listView != null)
                                        WaypointView.this.listView.setSelection(item.getListIndex());
                                    getContextMenu().show();
                                }
                            });
                            return item;
                        }
                    }

                    @Override
                    public void update(ListViewItem view) {

                        //get index from item
                        int idx = view.getListIndex();

                        Coordinate myPosition = EventHandler.getMyPosition();
                        if (myPosition == null)
                            return; // can't update without an position

                        float heading = de.longri.cachebox3.events.EventHandler.getHeading();

                        // get coordinate from Cache or from Waypoint
                        Coordinate targetCoord = idx == 0 ? actCache : actCache.waypoints.get(idx - 1);

                        //calculate distance and bearing
                        float result[] = new float[4];
                        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST,
                                myPosition.getLatitude(), myPosition.getLongitude(),
                                targetCoord.getLatitude(), targetCoord.getLongitude(), result);


                        //update item
                        boolean changed;
                        if (idx == 0) {
                            changed = ((CacheListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true));
                        } else {
                            changed = ((WayPointListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true));
                        }
                        if (changed)
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

                        if (listView.getSelectedItem() instanceof WayPointListItem) {
                            WayPointListItem selectedItem = (WayPointListItem) listView.getSelectedItem();
                            int index = selectedItem.getListIndex() - 1;
                            Waypoint wp = actCache.waypoints.get(index);

                            log.debug("Waypoint selection changed to: " + wp.toString());
                            //set selected Waypoint global
                            EventHandler.fire(new SelectedWayPointChangedEvent(wp));
                            actWaypoint = wp;

                        } else {
                            CacheListItem selectedItem = (CacheListItem) listView.getSelectedItem();
                            int selectedItemListIndex = selectedItem.getListIndex();

                            Cache cache = Database.Data.Query.get(selectedItemListIndex);
                            log.debug("Cache selection changed to: " + cache.toString());
                            //set selected Cache global
                            EventHandler.fire(new SelectedCacheChangedEvent(cache));
                            actWaypoint = null;
                        }
                    }
                });

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        Waypoint wp = EventHandler.getSelectedWaypoint();
                        if (wp == null) {
                            //select Cache
                            listView.setSelection(0);
                            listView.setSelectedItemVisible();
                        } else {
                            int index = 0;
                            for (ListViewItem item : listView.items()) {
                                if (index == 0) {
                                    index++;
                                    continue;
                                }
                                WayPointListItem wayPointListItem = (WayPointListItem) item;
                                if (wayPointListItem.getWaypointName().equals(wp.getGcCode())) {
                                    listView.setSelection(index);
                                    listView.setSelectedItemVisible();
                                    break;
                                }
                                index++;
                            }
                        }
                    }
                });
                log.debug("Finish Thread add new listView");
                Gdx.graphics.requestRendering();
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
//TODO dispose WaypointView
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

    private void editWP(boolean show) {
        showEditWpDialog(actWaypoint);
    }

    public void addWP() {
        String newGcCode = "";
        try {
            newGcCode = Database.CreateFreeGcCode(EventHandler.getSelectedCache().getGcCode());
        } catch (Exception e) {
            return;
        }
        Coordinate coord = EventHandler.getSelectedCoord();
        if (coord == null)
            coord = EventHandler.getMyPosition();
        if ((coord == null) || (!coord.isValid()))
            coord = EventHandler.getSelectedCache();
        Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, ""
                , coord.getLatitude(), coord.getLongitude(), EventHandler.getSelectedCache().Id, "", newGcCode);


        showEditWpDialog(newWP);
    }

    private void showEditWpDialog(Waypoint newWP) {
        EditWaypoint editWaypoint = new EditWaypoint(newWP, true, new GenericCallBack<Waypoint>() {
            @Override
            public void callBack(Waypoint value) {
                if (value != null) {
                    if (actCache.waypoints.contains(value, false)) {
                        int index = actCache.waypoints.indexOf(value, false);
                        actCache.waypoints.set(index, value);
                    } else {
                        actCache.waypoints.add(value);
                    }

                    addNewListView();
                    EventHandler.fire(new SelectedWayPointChangedEvent(value));
                    final WaypointDAO waypointDAO = new WaypointDAO();
                    if (value.IsStart) {
                        //It must be ensured here that this waypoint is the only one of these Cache,
                        //which is defined as starting point !!!
                        waypointDAO.ResetStartWaypoint(EventHandler.getSelectedCache(), value);
                    }
                    waypointDAO.WriteToDatabase(value);
                    Gdx.graphics.requestRendering();
                }
            }
        });
        editWaypoint.show();
    }

}
