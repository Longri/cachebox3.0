/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.map.layer.cluster.ClusterRenderer;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.geocluster.*;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.lists.ThreadStack;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.*;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.gdx.MotionHandler;
import org.oscim.layers.Layer;
import org.oscim.map.Map;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Longri on 27.11.16.
 */
public class WaypointLayer extends Layer implements GestureListener, CacheListChangedEventListener, Disposable {
    final static Logger log = LoggerFactory.getLogger(WaypointLayer.class);


    public static final Bitmap defaultMarker = getClusterSymbol("myterie");
    public static final Bitmap CLUSTER1_SYMBOL = getClusterSymbol("cluster1");
    public static final Bitmap CLUSTER10_SYMBOL = getClusterSymbol("cluster10");
    public static final Bitmap CLUSTER100_SYMBOL = getClusterSymbol("cluster100");


    private final ClusterRenderer mClusterRenderer;
    private final Point mTmpPoint = new Point();
    private final ClusteredList mItemList;
    private final double[] out = new double[2];


    private double lastDistance = Double.MIN_VALUE;
    private double lastFactor = 2.0;
    private ThreadStack<ClusterRunnable> clusterWorker = new ThreadStack<ClusterRunnable>();
    private ClusterRunnable.Task lastTask;


    public WaypointLayer(Map map) {
        super(map);
        mClusterRenderer = new ClusterRenderer(this, defaultMarker);
        mRenderer = mClusterRenderer;
        mItemList = new ClusteredList();
        populate();

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);
        CacheListChangedEvent();
    }

    public ClusterablePoint createItem(int index) {
        return mItemList.get(index);
    }


    public void populate() {
        mClusterRenderer.populate(mItemList.size());
    }

    @Override
    public void dispose() {
        CacheListChangedEventList.Remove(this);
        ClusterSymbolHashMap.clear();
//        TODO dispose ClusterList and ThreadStack
    }

    @Override
    public void CacheListChangedEvent() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (Database.Data.Query) {

                    //clear item list
                    mItemList.clear();

                    //add WayPoint items
                    for (Cache cache : Database.Data.Query) {
                        ClusterablePoint geoCluster = new ClusterablePoint(cache, cache.getGcCode());
                        geoCluster.setClusterSymbol(getClusterSymbolByCache(cache));
                        mItemList.add(geoCluster);

                        //add waypoints from selected Cache or all Waypoints if set
                        if (Settings.ShowAllWaypoints.getValue() || CB.isSelectedCache(cache)) {
                            for (Waypoint waypoint : cache.waypoints) {
                                ClusterablePoint waypointCluster = new ClusterablePoint(waypoint, waypoint.getGcCode());
                                waypointCluster.setClusterSymbol(getClusterSymbolByWaypoint(waypoint));
                                mItemList.add(waypointCluster);
                            }
                        }
                    }

                    mItemList.setFinishFill();
                    WaypointLayer.this.populate();
                }
            }
        });
        thread.start();
    }


    public void reduceCluster(final GeoBoundingBoxInt boundingBox, final double distance, final boolean forceReduce) {

        if (lastFactor == distance) {
            if (distance == 0) {
                // ensure that cluster is expand to all
                if (mItemList.isExpandToAll()) {
                    log.debug("GeoClustering  no distance changes");
                    return;
                }
                log.debug("GeoClustering  must expand to all");
            }
        }

        lastFactor = distance;

        boolean all = false;
        ClusterRunnable.Task task = ClusterRunnable.Task.reduce;

        if (forceReduce) {
            task = ClusterRunnable.Task.reduce;
        } else if (distance == 0) {
            if (mItemList.size() < mItemList.getAllSize()) {
                lastDistance = distance;
                task = ClusterRunnable.Task.expand;
                all = true;
            }
        } else if (lastDistance == Double.MIN_VALUE || lastDistance <= distance) {
            lastDistance = distance;
            task = ClusterRunnable.Task.reduce;
        } else {
            lastDistance = distance;
            task = ClusterRunnable.Task.expand;
        }


        ClusterRunnable clusterRunnable = new ClusterRunnable(distance, mItemList, new ClusterRunnable.CallBack() {
            @Override
            public void callBack() {
                WaypointLayer.this.populate();
                mMap.updateMap(true);
                mMap.render();
            }
        }, boundingBox, task, all);

        if (lastTask == task) {
            clusterWorker.pushAndStart(clusterRunnable);
        } else {
            clusterWorker.pushAndStartWithCancelRunning(clusterRunnable);
            lastTask = task;
        }


    }


    private final HashMap<String, Bitmap> ClusterSymbolHashMap = new HashMap<String, Bitmap>();


    private Bitmap getClusterSymbolByCache(Cache cache) {
        Bitmap symbol = null;
        String symbolName = getMapIconName(cache);
        symbol = ClusterSymbolHashMap.get(symbolName);
        if (symbol == null) {
            symbol = getClusterSymbol(symbolName);
            ClusterSymbolHashMap.put(symbolName, symbol);
        }
        return symbol;
    }

    private Bitmap getClusterSymbolByWaypoint(Waypoint waypoint) {
        Bitmap symbol = null;
        String symbolName = getMapIconName(waypoint);
        symbol = ClusterSymbolHashMap.get(symbolName);
        if (symbol == null) {
            symbol = getClusterSymbol(symbolName);
            ClusterSymbolHashMap.put(symbolName, symbol);
        }
        return symbol;
    }


    private static String getMapIconName(Cache cache) {
        if (cache.ImTheOwner())
            return "star";
        else if (cache.isFound())
            return "mapFound";
        else if ((cache.Type == CacheTypes.Mystery) && cache.CorrectedCoordiantesOrMysterySolved())
            return "mapSolved";
        else if ((cache.Type == CacheTypes.Multi) && cache.HasStartWaypoint())
            return "mapMultiStartP"; // Multi with start point
        else if ((cache.Type == CacheTypes.Mystery) && cache.HasStartWaypoint())
            return "mapMysteryStartP"; // Mystery without Final but with start point
        else
            return "map" + cache.Type.name();
    }

    private static String getMapIconName(Waypoint waypoint) {
        if ((waypoint.Type == CacheTypes.MultiStage) && (waypoint.IsStart))
            return "mapMultiStageStartP";
        else
            return "map" + waypoint.Type.name();
    }

    private static Bitmap getClusterSymbol(String name) {

        if (!VisUI.isLoaded()) return null;

        Skin skin = VisUI.getSkin();
        ScaledSvg scaledSvg = skin.get(name, ScaledSvg.class);
        FileHandle fileHandle = Gdx.files.internal(scaledSvg.path);
        Bitmap bitmap = null;
        try {
            bitmap = PlatformConnector.getSvg(fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale);
        } catch (IOException e) {
            return null;
        }
        return bitmap;
    }

    public interface ActiveItem {
        boolean run(ClusterablePoint aIndex);
    }

    protected boolean onItemSingleTap(ClusterablePoint item) {
        log.debug("Click on: " + item);
        return true;
    }

    protected boolean onItemLongPress(ClusterablePoint item) {
        log.debug("LongClick on: " + item);
        return true;
    }

    private final ActiveItem mActiveItemSingleTap = new ActiveItem() {
        @Override
        public boolean run(ClusterablePoint item) {
            return onItemSingleTap(item);
        }
    };

    private final ActiveItem mActiveItemLongPress = new ActiveItem() {
        @Override
        public boolean run(final ClusterablePoint item) {
            return onItemLongPress(item);
        }
    };

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {

        if (!(e instanceof MotionHandler)) return false;

        if (g instanceof Gesture.Tap)
            return activateSelectedItems(e, mActiveItemSingleTap);

        if (g instanceof Gesture.LongPress)
            return activateSelectedItems(e, mActiveItemLongPress);

        return false;
    }

    /**
     * When a content sensitive action is performed the content item needs to be
     * identified. This method does that and then performs the assigned task on
     * that item.
     *
     * @return true if event is handled false otherwise
     */
    private boolean activateSelectedItems(MotionEvent event, ActiveItem task) {
        // no click detection without items
        if (mItemList.size() == 0)
            return false;

        //add MapView drawing offset to event point
        final CacheboxMapAdapter mapAdapter = (CacheboxMapAdapter) mMap;
        final int eventX = (int) event.getX() - mapAdapter.getX_Offset();
        final int eventY = (int) event.getY() - mapAdapter.getY_Offset();
        log.debug("MapEvent " + eventX + "/" + eventY);

        // calculate geoCoordinate from click point
        final Point clickPoint = new Point();
        mMap.viewport().fromScreenPoint(eventX, eventY, clickPoint);
        double clickLat = MercatorProjection.toLatitude(clickPoint.y);
        double clickLon = MercatorProjection.toLongitude(clickPoint.x);
        log.debug("ClickEvent Lat/Lon :" + clickLat + "/" + clickLon);

        //extend geoCoordinate to BoundingBox
        double groundResolution = MercatorProjection.groundResolution(mMap.getMapPosition());
        double extendValue = MercatorProjection.latitudeToY(groundResolution * defaultMarker.getWidth());
        GeoBoundingBoxDouble boundingBox = new GeoBoundingBoxDouble(clickLat, clickLon, extendValue);

        // search item inside click bounding box
        ClusterablePoint clickedItem = null;
        for (int i = 0, n = mItemList.size(); i < n; i++) {
            ClusterablePoint item = mItemList.get(i);

            double lat, lon;
            if (item instanceof Cluster) {
                //the draw point is set to center of cluster
                Coordinate centerCoord = ((Cluster) item).getCenter();
                lat = centerCoord.latitude;
                lon = centerCoord.longitude;
            } else {
                lat = item.latitude;
                lon = item.longitude;
            }
            if (!boundingBox.contains(lat, lon))
                continue;
            clickedItem = item;
        }

        if (clickedItem != null && task.run(clickedItem)) {
            mClusterRenderer.update();
            mMap.render();
            return true;
        }
        return false;
    }
}
