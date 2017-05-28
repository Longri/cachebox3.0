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
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheWithWP;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView implements CacheListChangedEventListener, PositionChangedListener, OrientationChangedListener {
    final static Logger log = LoggerFactory.getLogger(CacheListView.class);
    private ListView listView;
    private final float result[] = new float[4];


    public CacheListView() {
        super("CacheListView CacheCount: " + Database.Data.Query.size);

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);

        //register as positionChanged eventListener
        EventHandler.add(this);
    }

    public synchronized void layout() {
        log.debug("Layout");
        super.layout();
        if (listView == null) addNewListView();
        log.debug("Finish Layout");
    }

    public void resort() {
        log.debug("resort Query");
        synchronized (Database.Data.Query) {
            Database.Data.Query.Resort(EventHandler.getSelectedCoord(),
                    new CacheWithWP(EventHandler.getSelectedCache(), EventHandler.getSelectedWaypoint()));
        }
        log.debug("Finish resort Query");
    }


    private void addNewListView() {
        log.debug("Start Thread add new listView");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CacheListView.this.clear();
                Adapter listViewAdapter = new Adapter() {
                    @Override
                    public int getCount() {
                        return Database.Data.Query.size;
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        return CacheListItem.getListItem(index, Database.Data.Query.get(index));
                    }

                    @Override
                    public void update(ListViewItem view) {

                        //get index from item
                        int idx = view.getListIndex();

                        // get Cache
                        Cache cache = Database.Data.Query.get(idx);

                        //get actPos and heading
                        Coordinate position = EventHandler.getMyPosition();

                        if (position == null)
                            return; // can't update without an position

                        float heading = EventHandler.getHeading();

                        // get coordinate from Cache or from Final Waypoint
                        Waypoint finalWp = cache.GetFinalWaypoint();
                        Coordinate finalCoord = finalWp != null ? finalWp : cache;

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

                        Cache cache = Database.Data.Query.get(selectedItemListIndex);
                        log.debug("Cache selection changed to: " + cache.toString());
                        //set selected Cache global
                        EventHandler.fire(new SelectedCacheChangedEvent(cache));
                    }
                });

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        int selectedIndex = 0;
                        for (Cache cache : Database.Data.Query) {
                            if (cache.equals(EventHandler.getSelectedCache())) {
                                break;
                            }
                            selectedIndex++;
                        }
                        listView.setSelection(selectedIndex);
                        listView.setSelectedItemVisible();
                        log.debug("Finish Thread add new listView");
                    }
                });
                CB.requestRendering();
            }
        });
        thread.start();
        CB.requestRendering();
    }

    private void disposeListView() {
        final ListView disposeListView = CacheListView.this.listView;
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
        if (listView == null) return;
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

}
