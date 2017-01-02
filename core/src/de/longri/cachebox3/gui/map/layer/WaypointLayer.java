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
package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.map.layer.cluster.ClusterSymbol;
import de.longri.cachebox3.gui.map.layer.cluster.ItemizedClusterLayer;
import de.longri.cachebox3.locator.geocluster.ClusterRunnable;
import de.longri.cachebox3.locator.geocluster.ClusterablePoint;
import de.longri.cachebox3.locator.geocluster.ClusteredList;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.utils.lists.CB_List;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.map.Map;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Longri on 27.11.16.
 */
public class WaypointLayer extends ItemizedClusterLayer implements CacheListChangedEventListener, Disposable {
    final static Logger log = LoggerFactory.getLogger(WaypointLayer.class);


    public static final ClusterSymbol defaultMarker = getClusterSymbol("myterie");
    public static final ClusterSymbol CLUSTER1_SYMBOL = getClusterSymbol("cluster1");
    public static final ClusterSymbol CLUSTER10_SYMBOL = getClusterSymbol("cluster10");
    public static final ClusterSymbol CLUSTER100_SYMBOL = getClusterSymbol("cluster100");


//    protected final ClusteredList mAllItemList = new ClusteredList();
    private double lastFactor = 2.0;

    public WaypointLayer(Map map) {
        super(map, new ClusteredList(), defaultMarker, null);
        mOnItemGestureListener = gestureListener;

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);
        CacheListChangedEvent();
    }

    private final OnItemGestureListener<ClusterablePoint> gestureListener = new OnItemGestureListener<ClusterablePoint>() {
        @Override
        public boolean onItemSingleTapUp(int index, ClusterablePoint item) {
            return false;
        }

        @Override
        public boolean onItemLongPress(int index, ClusterablePoint item) {
            return false;
        }
    };

    @Override
    public void dispose() {
        CacheListChangedEventList.Remove(this);
        ClusterSymbolHashMap.clear();
        mOnItemGestureListener = null;
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
                        ClusterablePoint geoCluster = new ClusterablePoint(cache,cache.getGcCode());
                        geoCluster.setClusterSymbol(getClusterSymbolByCache(cache));
                        mItemList.add(geoCluster);
                    }
                    WaypointLayer.this.populate();
                    setZoomLevel(mMap.getMapPosition());
                }
            }
        });
        thread.start();

    }

    Thread clusterThread;
    ClusterRunnable clusterRunnable;


    private void reduceCluster(final double distance) {

        if (lastFactor == distance) {
            log.debug("GeoClustering  no distance changes");
            return;
        }

        if (clusterThread != null && clusterThread.isAlive()) {
            clusterRunnable.terminate();
            try {
                clusterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        log.debug("START GeoClustering distance:" + distance + " ZoomLevel:" + lastZoomLevel);




        lastFactor = distance;
        clusterRunnable = new ClusterRunnable(distance, mItemList, new ClusterRunnable.CallBack() {
            @Override
            public void callBack(CB_List<ClusterablePoint> reduced) {
                log.debug("Cluster Reduce from " + mItemList.size() + " items to " + reduced.size() + " items");
                WaypointLayer.this.populate();
                mMap.updateMap(true);
            }
        });
        clusterThread = new Thread(clusterRunnable);
        clusterThread.start();
    }


    private final HashMap<String, ClusterSymbol> ClusterSymbolHashMap = new HashMap<String, ClusterSymbol>();


    private ClusterSymbol getClusterSymbolByCache(Cache cache) {
        ClusterSymbol symbol = null;
        String symbolName = getMapIconName(cache);
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

    private static ClusterSymbol getClusterSymbol(String name) {

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
        return new ClusterSymbol(bitmap, ClusterSymbol.HotspotPlace.CENTER, true);
    }


    private int lastZoomLevel = -1;

    public void setZoomLevel(final MapPosition mapPos) {

        final int zoomLevel = mapPos.getZoomLevel();

        log.debug("Set zoom level to " + zoomLevel);

        if (lastZoomLevel == zoomLevel) {
            log.debug("no zoom level changes");
            return;
        }
        lastZoomLevel = zoomLevel;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                double groundResolution = zoomLevel < 14 ? (MercatorProjection.groundResolution(mapPos) * Tile.SIZE) / 5 : 0;

                log.debug("call reduce cluster with distance: " + groundResolution);
                reduceCluster(groundResolution);
            }
        });
        thread.start();

    }
}
