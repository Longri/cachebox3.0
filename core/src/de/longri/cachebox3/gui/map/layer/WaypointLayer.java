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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.events.SelectedCacheEvent;
import de.longri.cachebox3.gui.events.SelectedCacheEventList;
import de.longri.cachebox3.gui.map.layer.renderer.WaypointLayerRenderer;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.geocluster.ClusterRunnable;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBoxDouble;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBoxInt;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.lists.CB_List;
import de.longri.cachebox3.utils.lists.ThreadStack;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.Box;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.gdx.MotionHandler;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.atlas.TextureRegion;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by Longri on 27.11.16.
 */
public class WaypointLayer extends Layer implements GestureListener, CacheListChangedEventListener, Disposable, SelectedCacheEvent {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(WaypointLayer.class);

    private static final String ERROR_MSG = "No de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle registered with name: ";

    private final WaypointLayerRenderer mClusterRenderer;
    final ClusteredList mItemList;
    private final Point clickPoint = new Point();
    private final Box mapVisibleBoundingBox = new Box();
    private final CB_List<MapWayPointItem> clickedItems = new CB_List<MapWayPointItem>();
    private final ThreadStack<ClusterRunnable> clusterWorker = new ThreadStack<ClusterRunnable>();
    public final LinkedHashMap<Object, TextureRegion> textureRegionMap;

    private final MapWayPointItemStyle selectedStyle;
    private final TextureRegion smallSelected;
    private final TextureRegion middleSelected;
    private final TextureRegion largeSelected;

    private final MapWayPointItemStyle disabledStyle;
    private final TextureRegion smallDisabled;
    private final TextureRegion middleDisabled;
    private final TextureRegion largeDisabled;


    private double lastDistance = Double.MIN_VALUE;
    private double lastFactor = 2.0;

    private ClusterRunnable.Task lastTask;

    public WaypointLayer(Map map, LinkedHashMap<Object, TextureRegion> textureRegionMap) {
        super(map);
        mClusterRenderer = new WaypointLayerRenderer(this, null);
        mRenderer = mClusterRenderer;
        mItemList = new ClusteredList();
        populate();

        this.textureRegionMap = textureRegionMap;

        //initial Overlay styles
        selectedStyle = VisUI.getSkin().get("selectOverlay", MapWayPointItemStyle.class);
        smallSelected = selectedStyle.small != null ? textureRegionMap.get(((GetName) selectedStyle.small).getName()) : null;
        middleSelected = selectedStyle.middle != null ? textureRegionMap.get(((GetName) selectedStyle.middle).getName()) : null;
        largeSelected = selectedStyle.large != null ? textureRegionMap.get(((GetName) selectedStyle.large).getName()) : null;

        disabledStyle = VisUI.getSkin().get("disabledOverlay", MapWayPointItemStyle.class);
        smallDisabled = disabledStyle.small != null ? textureRegionMap.get(((GetName) disabledStyle.small).getName()) : null;
        middleDisabled = disabledStyle.middle != null ? textureRegionMap.get(((GetName) disabledStyle.middle).getName()) : null;
        largeDisabled = disabledStyle.large != null ? textureRegionMap.get(((GetName) disabledStyle.large).getName()) : null;


        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);
        CacheListChangedEvent();

        //register SelectedCacheChangedEvent
        SelectedCacheEventList.Add(this);
    }


    public MapWayPointItem createItem(int index) {
        return mItemList.get(index);
    }


    private void populate() {
        mClusterRenderer.populate(mItemList.size());
    }

    @Override
    public void dispose() {
        CacheListChangedEventList.Remove(this);
        clickedItems.clear();
        mClusterRenderer.dispose();
        clusterWorker.dispose();
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

                    CB_List<String> missingIconList = new CB_List<String>(0);


                    boolean hasSelectedWP = CB.getSelectedWaypoint() != null;
                    for (Cache cache : Database.Data.Query) {

                        boolean dis = cache.isArchived() || !cache.isAvailable();
                        boolean sel = !hasSelectedWP && CB.isSelectedCache(cache);
                        try {
                            MapWayPointItem geoCluster = getMapWayPointItem(cache, dis, sel);
                            mItemList.add(geoCluster);
                        } catch (GdxRuntimeException e) {
                            if (e.getMessage().startsWith(ERROR_MSG)) {
                                String iconName = e.getMessage().replace(ERROR_MSG, "");
                                if (!missingIconList.contains(iconName))
                                    missingIconList.add(iconName);
                            } else {
                                e.printStackTrace();
                            }
                            continue;
                        }


                        //add waypoints from selected Cache or all Waypoints if set
                        if (Settings.ShowAllWaypoints.getValue() || CB.isSelectedCache(cache)) {
                            Waypoint selectedWaypoint = CB.getSelectedWaypoint();
                            for (Waypoint waypoint : cache.waypoints) {
                                try {
                                    MapWayPointItem waypointCluster = getMapWayPointItem(waypoint, dis, selectedWaypoint != null && selectedWaypoint.equals(waypoint));
                                    mItemList.add(waypointCluster);
                                } catch (GdxRuntimeException e) {
                                    if (e.getMessage().startsWith(ERROR_MSG)) {
                                        String iconName = e.getMessage().replace(ERROR_MSG, "");
                                        if (!missingIconList.contains(iconName))
                                            missingIconList.add(iconName);
                                    } else {
                                        e.printStackTrace();
                                    }
                                    continue;
                                }
                            }
                        }
                    }
                    mItemList.setFinishFill();
                    WaypointLayer.this.populate();


                    if (!missingIconList.isEmpty()) {
                        StringBuilder msg = new StringBuilder("\n\n" + ERROR_MSG + "\n");
                        int count = 0;
                        for (String name : missingIconList) {
                            msg.append(", " + name);
                            count++;
                            if (count > 5) {
                                msg.append("\n");
                                count = 0;
                            }
                        }
                        if (CanvasAdapter.platform.isDesktop())
                            throw new GdxRuntimeException(msg.toString());
                        else log.error(msg.toString());
                    }
                }
            }
        });
        thread.start();
    }

    private MapWayPointItem getMapWayPointItem(Cache cache, boolean dis, boolean sel) {
        MapWayPointItemStyle style = getClusterSymbolsByCache(cache);
        TextureRegion small = style.small == null ? null : textureRegionMap.get(((GetName) style.small).getName());
        TextureRegion middle = style.middle == null ? null : textureRegionMap.get(((GetName) style.middle).getName());
        TextureRegion large = style.large == null ? null : textureRegionMap.get(((GetName) style.large).getName());

        MapWayPointItem.SizedRegions normal = new MapWayPointItem.SizedRegions(small, middle, large);
        MapWayPointItem.SizedRegions selectedOverlay = sel ? new MapWayPointItem.SizedRegions(smallSelected, middleSelected, largeSelected) : null;
        MapWayPointItem.SizedRegions disabledOverlay = dis ? new MapWayPointItem.SizedRegions(smallDisabled, middleDisabled, largeDisabled) : null;

        MapWayPointItem.Regions regions = new MapWayPointItem.Regions(normal, selectedOverlay, disabledOverlay);

        return new MapWayPointItem(cache, cache, regions);
    }

    private MapWayPointItem getMapWayPointItem(Waypoint waypoint, boolean dis, boolean sel) {
        MapWayPointItemStyle style = getClusterSymbolsByWaypoint(waypoint);

        TextureRegion small = textureRegionMap.get(((GetName) style.small).getName());
        TextureRegion middle = textureRegionMap.get(((GetName) style.middle).getName());
        TextureRegion large = textureRegionMap.get(((GetName) style.large).getName());


        MapWayPointItem.SizedRegions normal = new MapWayPointItem.SizedRegions(small, middle, large);
        MapWayPointItem.SizedRegions selectedOverlay = sel ? new MapWayPointItem.SizedRegions(smallSelected, middleSelected, largeSelected) : null;
        MapWayPointItem.SizedRegions disabledOverlay = dis ? new MapWayPointItem.SizedRegions(smallDisabled, middleDisabled, largeDisabled) : null;

        MapWayPointItem.Regions regions = new MapWayPointItem.Regions(normal, selectedOverlay, disabledOverlay);


        return new MapWayPointItem(waypoint, waypoint, regions);
    }


    public void reduceCluster(final GeoBoundingBoxInt boundingBox, final double distance, final boolean forceReduce) {

        if (true) return;

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


    public static MapWayPointItemStyle getClusterSymbolsByCache(Cache cache) {
        String symbolStyleName = getMapIconName(cache);
        return VisUI.getSkin().get(symbolStyleName, MapWayPointItemStyle.class);
    }

    public static MapWayPointItemStyle getClusterSymbolsByWaypoint(Waypoint waypoint) {
        String symbolStyleName = getMapIconName(waypoint);
        return VisUI.getSkin().get(symbolStyleName, MapWayPointItemStyle.class);
    }


    public static String getMapIconName(Cache cache) {
        if (cache.ImTheOwner())
            return "mapStar";
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

        SvgSkin skin = (SvgSkin) VisUI.getSkin();
        ScaledSvg scaledSvg = skin.get(name, ScaledSvg.class);
        Bitmap bitmap;
        try {
            FileHandle svgFile = skin.skinFolder.child(scaledSvg.path);
            bitmap = PlatformConnector.getSvg(scaledSvg.getRegisterName(), svgFile.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale);
        } catch (IOException e) {
            return null;
        }
        return bitmap;
    }

    @Override
    public void selectedCacheChanged(Cache selectedCache, Waypoint selectedwaypoint, Cache lastSelectedCache, Waypoint lastWaypoint) {

        //reset selected state of last Cache/WP
        //set selected state to Cache/WP
        boolean wpchanged = false;
        if (selectedCache.equals(lastSelectedCache)) {
            if (selectedwaypoint != null) {
                if (selectedwaypoint.equals(lastWaypoint)) {
                    return;
                } else {
                    wpchanged = true;
                }
            } else {
                if (lastWaypoint == null) return;
            }
        }


        int indexOfLastSelectedCache = selectedCache.equals(lastSelectedCache) && !wpchanged ? -2 : -1;
        int indexOfNewSelectedCache = -1;
        int indexOfLastSelectedWp = lastWaypoint == null ? -2 : -1;
        int indexOfNewSelectedWp = selectedwaypoint == null ? -2 : -1;

        for (int i = 0, n = mItemList.size(); i < n; i++) {
            MapWayPointItem item = mItemList.get(i);
            if (indexOfNewSelectedCache == -1 && item.dataObject.equals(selectedCache)) indexOfNewSelectedCache = i;
            if (indexOfLastSelectedCache == -1 && item.dataObject.equals(lastSelectedCache))
                indexOfLastSelectedCache = i;
            if (indexOfLastSelectedWp == -1 && item.dataObject.equals(lastWaypoint)) indexOfLastSelectedWp = i;
            if (indexOfNewSelectedWp == -1 && item.dataObject.equals(selectedwaypoint)) indexOfNewSelectedWp = i;


            if (indexOfLastSelectedCache != -1 && indexOfNewSelectedCache != -1 &&
                    indexOfLastSelectedWp != -1 && indexOfNewSelectedWp != -1) break;
        }

        log.debug("Last item" + indexOfNewSelectedCache);
        log.debug("New item" + indexOfNewSelectedCache);


        // new selected cache
        if (indexOfNewSelectedCache >= 0 && indexOfNewSelectedWp < 0) {
            MapWayPointItem newItem = getMapWayPointItem(selectedCache, selectedCache.isArchived() || !selectedCache.isAvailable(), true);
            mItemList.set(indexOfNewSelectedCache, newItem);
        }

        // last selected cache
        if (indexOfLastSelectedCache >= 0) {
            MapWayPointItem lastItem = getMapWayPointItem(lastSelectedCache, lastSelectedCache.isArchived() || !lastSelectedCache.isAvailable(), false);
            mItemList.set(indexOfLastSelectedCache, lastItem);
        }

        // new selected wp
        if (indexOfNewSelectedWp >= 0) {
            MapWayPointItem newWp = getMapWayPointItem(selectedwaypoint, selectedCache.isArchived() || !selectedCache.isAvailable(), true);
            mItemList.set(indexOfNewSelectedWp, newWp);
        }

        // last selected wp
        if (indexOfLastSelectedWp >= 0) {
            MapWayPointItem lastWp = getMapWayPointItem(lastWaypoint, lastSelectedCache.isArchived() || !lastSelectedCache.isAvailable(), false);
            mItemList.set(indexOfLastSelectedWp, lastWp);
        }


        populate();
    }

    public interface ActiveItem {
        boolean run(MapWayPointItem aIndex);
    }

    private boolean onItemSingleTap(MapWayPointItem item) {
        log.debug("Click on: " + item);

        //set as selected cache/wp
        if (item.dataObject instanceof Cache) {
            CB.setSelectedCache((Cache) item.dataObject);
        } else if (item.dataObject instanceof Waypoint) {
            Waypoint wp = (Waypoint) item.dataObject;
            Cache cache = CB.getCacheFromId(wp.CacheId);
            CB.setSelectedWaypoint(cache, wp);
        }
        return true;
    }

    private boolean onItemLongPress(MapWayPointItem item) {
        log.debug("LongClick on: " + item);
        return true;
    }

    private final ActiveItem mActiveItemSingleTap = new ActiveItem() {
        @Override
        public boolean run(MapWayPointItem item) {
            return onItemSingleTap(item);
        }
    };

    private final ActiveItem mActiveItemLongPress = new ActiveItem() {
        @Override
        public boolean run(final MapWayPointItem item) {
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

        // calculate geoCoordinate from click point
        mMap.viewport().fromScreenPoint(eventX, eventY, clickPoint);
        double clickLon = MercatorProjection.toLatitude(clickPoint.y);
        double clickLat = MercatorProjection.toLongitude(clickPoint.x);

        //extend geoCoordinate to BoundingBox
        double groundResolution = getGroundresolution();
        double extendValue = groundResolution * CB.scaledSizes.BUTTON_WIDTH;
        GeoBoundingBoxDouble boundingBox = new GeoBoundingBoxDouble(clickLat, clickLon, extendValue);

        // search item inside click bounding box
        clickedItems.clear();
        for (int i = 0, n = mItemList.size(); i < n; i++) {
            MapWayPointItem item = mItemList.get(i);

            double lat, lon;
            if (item instanceof Cluster) {
                //the draw point is set to center of cluster
                final Coordinate centerCoord = ((Cluster) item).getCenter();
                lat = centerCoord.latitude;
                lon = centerCoord.longitude;
            } else {
                lat = item.latitude;
                lon = item.longitude;
            }
            if (!boundingBox.contains(lat, lon))
                continue;
            clickedItems.add(item);
        }

        if (!clickedItems.isEmpty()) {
            MapWayPointItem clickedItem = null;
            //if more then one item so search nearest
            if (clickedItems.size() == 1) {
                clickedItem = clickedItems.get(0);
            } else {
                Coordinate clickCoord = new Coordinate(clickLon, clickLat);
                double minDistance = Double.MAX_VALUE;
                for (int i = 0, n = clickedItems.size(); i < n; i++) {
                    MapWayPointItem item = clickedItems.get(i);
                    Coordinate pos;
                    if (item instanceof Cluster) {
                        //the draw point is set to center of cluster
                        pos = ((Cluster) item).getCenter();
                    } else {
                        pos = item;
                    }
                    double distance = clickCoord.distance(pos);
                    if (distance < minDistance) {
                        minDistance = distance;
                        clickedItem = item;
                    }
                }
            }

            task.run(clickedItem);
            mClusterRenderer.update();
            mMap.render();
            return true;
        }
        return false;
    }

    private double getGroundresolution() {
        mMap.viewport().getBBox(mapVisibleBoundingBox, 0);
        mapVisibleBoundingBox.map2mercator();
        return mapVisibleBoundingBox.getWidth() / mMap.getWidth();
    }
}
