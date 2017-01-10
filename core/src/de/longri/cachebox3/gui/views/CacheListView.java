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
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.locator.events.PositionChangedEvent;
import de.longri.cachebox3.locator.events.PositionChangedEventList;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheWithWP;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView implements CacheListChangedEventListener, PositionChangedEvent {
    final static Logger log = LoggerFactory.getLogger(CacheListView.class);
    private ListView listView;

    public CacheListView() {
        super("CacheListView CacheCount: " + Database.Data.Query.size());

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);

        //register as positionChanged eventListener
        PositionChangedEventList.Add(this);
    }

    public void layout() {
        super.layout();
        if (listView == null) addNewListView();
    }

    public void resort() {
        synchronized (Database.Data.Query) {
            CacheWithWP nearstCacheWp = Database.Data.Query.Resort(CB.getSelectedCoord(), new CacheWithWP(CB.getSelectedCache(), CB.getSelectedWaypoint()));

            if (nearstCacheWp != null)
                CB.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
        }
    }


    private void addNewListView() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CacheListView.this.clear();
                Adapter listViewAdapter = new Adapter() {
                    @Override
                    public int getCount() {
                        return Database.Data.Query.size();
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        return getCacheItem(index, Database.Data.Query.get(index));
                    }

                    @Override
                    public void update(ListViewItem view) {

                        //get index from item
                        int idx = view.getListIndex();

                        // get Cache
                        Cache cache = Database.Data.Query.get(idx);

                        //get actPos and heading
                        Coordinate position = Locator.getCoordinate();
                        float heading = Locator.getHeading();


                        // get coordinate from Cache or from Final Waypoint
                        Waypoint finalWp = cache.GetFinalWaypoint();
                        Coordinate finalCoord = finalWp != null ? finalWp : cache;

                        //calculate distance and bearing
                        float result[] = new float[4];
                        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, position.getLatitude(), position.getLongitude(), finalCoord.getLatitude(), finalCoord.getLongitude(), result);


                        //update item
                        if (((CacheListItem) view).update(-(result[2] - heading), UnitFormatter.DistanceString(result[0])))
                            Gdx.graphics.requestRendering();
                    }

                    @Override
                    public float getItemSize(int position) {
                        return 0;
                    }
                };

                if (CacheListView.this.listView != null) {
                    disposeListView();
                }

                CacheListView.this.listView = new ListView(listViewAdapter);
                synchronized (CacheListView.this.listView) {
                    listView.setBounds(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight());
                    addActor(listView);
                    listView.setCullingArea(new Rectangle(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight()));
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
                        CB.setSelectedCache(cache);
                    }
                });

                int selectedIndex = 0;
                for (Cache cache : Database.Data.Query) {
                    if (cache.equals(CB.getSelectedCache())) {
                        break;
                    }
                    selectedIndex++;
                }

                listView.setSelectedItem(selectedIndex);
                listView.setSelectedItemVisible();
            }
        });
        thread.start();
        Gdx.graphics.requestRendering();
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

    private ListViewItem getCacheItem(int listIndex, final Cache cache) {
        ListViewItem listViewItem = new CacheListItem(listIndex, cache.Type, cache.getName(),
                (int) (cache.getDifficulty() * 2), (int) (cache.getTerrain() * 2),
                (int) Math.min(cache.Rating * 2, 5 * 2), cache.Size.ordinal());
        return listViewItem;
    }

    @Override
    public void dispose() {
        disposeListView();
        CacheListChangedEventList.Remove(this);
        PositionChangedEventList.Remove(this);
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
        listView.dataSetChanged();
    }

    @Override
    public void PositionChanged() {
        setChangedFlagToAllItems();
    }

    @Override
    public void OrientationChanged() {
        setChangedFlagToAllItems();
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
        Gdx.graphics.requestRendering();
    }

    @Override
    public void SpeedChanged() {
        //do nothing
    }

    @Override
    public void onHide() {
        super.onHide();

    }


    @Override
    public String getReceiverName() {
        return "CacheListView";
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }
}
