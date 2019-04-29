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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.CacheListChangedListener;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.map.layer.renderer.WaypointLayerRenderer;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.geocluster.ClusterRunnable;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBoxDouble;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBoxInt;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.NamedRunnable;
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
public class WaypointLayer extends Layer implements GestureListener, CacheListChangedListener, Disposable, de.longri.cachebox3.events.SelectedCacheChangedListener, de.longri.cachebox3.events.SelectedWayPointChangedListener {
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

    private final MapView mapView;


    private double lastDistance = Double.MIN_VALUE;
    private double lastFactor = 2.0;

    private ClusterRunnable.Task lastTask;

    public WaypointLayer(MapView mapView, Map map, LinkedHashMap<Object, TextureRegion> textureRegionMap) {
        super(map);
        log.debug("Create new INSTANCE");

        this.mapView = mapView;

        mClusterRenderer = new WaypointLayerRenderer(this, null);
        mRenderer = mClusterRenderer;
        mItemList = new ClusteredList();
        populate(true);

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

        //register SelectedCacheChangedEvent
        EventHandler.add(this);
        cacheListChanged(null);
        Settings.ShowAllWaypoints.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                cacheListChanged(null);
            }
        });
    }


    public MapWayPointItem createItem(int index) {
        return mItemList.get(index);
    }


    private void populate(boolean resort) {
        mClusterRenderer.populate(mItemList.size, resort);
    }

    @Override
    public void dispose() {
        clickedItems.clear();
        mClusterRenderer.dispose();
        clusterWorker.dispose();
        EventHandler.remove(this);
//        TODO dispose ClusterList and ThreadStack
    }

    @Override
    public void cacheListChanged(CacheListChangedEvent event) {
        log.debug("Call cacheList changed event handler");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CB.postOnGlThread(new NamedRunnable("") {
                    @Override
                    public void run() {
                        //clear item list
                        mItemList.clear();

                        //add WayPoint items

                        CB_List<String> missingIconList = new CB_List<>();
                        boolean hasSelectedWP = EventHandler.getSelectedWaypoint() != null;

                        //set selected Cache at front
                        for (AbstractCache cache : Database.Data.cacheList) {
                            addCache(missingIconList, hasSelectedWP, cache);
                        }

                        mItemList.setFinishFill();
                        WaypointLayer.this.populate(true);


                        if (missingIconList.size != 0) {
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
                });
            }
        });
        thread.start();
    }

    private void addCache(CB_List<String> missingIconList, boolean hasSelectedWP, AbstractCache abstractCache) {
        CB.assertGlThread();
        boolean dis = abstractCache.isArchived() || !abstractCache.isAvailable();
        boolean sel = !hasSelectedWP && de.longri.cachebox3.events.EventHandler.isSelectedCache(abstractCache);
        try {
            MapWayPointItem geoCluster = getMapWayPointItem(abstractCache, dis, sel);
            mItemList.add(geoCluster);
        } catch (GdxRuntimeException e) {
            if (e.getMessage().startsWith(ERROR_MSG)) {
                String iconName = e.getMessage().replace(ERROR_MSG, "");
                if (!missingIconList.contains(iconName, false))
                    missingIconList.add(iconName);
            } else {
                e.printStackTrace();
            }
            return;
        }


        //add waypoints from selected Cache or all Waypoints if set
        sel = EventHandler.isSelectedCache(abstractCache);
        AbstractWaypoint selWp = null;
        if (abstractCache.getWaypoints() != null) {
            if (Settings.ShowAllWaypoints.getValue() || sel) {
                selWp = selectedWaypoint = EventHandler.getSelectedWaypoint();
                for (AbstractWaypoint waypoint : abstractCache.getWaypoints()) {
                    try {
                        MapWayPointItem waypointCluster = getMapWayPointItem(waypoint, dis, selectedWaypoint != null && selectedWaypoint.equals(waypoint));
                        mItemList.add(waypointCluster);
                    } catch (GdxRuntimeException e) {
                        if (e.getMessage().startsWith(ERROR_MSG)) {
                            String iconName = e.getMessage().replace(ERROR_MSG, "");
                            if (!missingIconList.contains(iconName, false))
                                missingIconList.add(iconName);
                        } else {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
            }
        }
        if (sel) {
            if (selWp != null) {
                selectedAbstractCache = null;
                log.debug("set selected Waypoint {}", selectedWaypoint);
            } else {
                selectedAbstractCache = abstractCache;
                log.debug("set selected Cache {}", abstractCache);
            }
        }
    }

    private MapWayPointItem getMapWayPointItem(AbstractCache abstractCache, boolean dis, boolean sel) {
        MapWayPointItemStyle style = getClusterSymbolsByCache(abstractCache);
        TextureRegion small = style.small == null ? null : textureRegionMap.get(((GetName) style.small).getName());
        TextureRegion middle = style.middle == null ? null : textureRegionMap.get(((GetName) style.middle).getName());
        TextureRegion large = style.large == null ? null : textureRegionMap.get(((GetName) style.large).getName());

        MapWayPointItem.SizedRegions normal = new MapWayPointItem.SizedRegions(small, middle, large);
        MapWayPointItem.SizedRegions selectedOverlay = sel ? new MapWayPointItem.SizedRegions(smallSelected, middleSelected, largeSelected) : null;
        MapWayPointItem.SizedRegions disabledOverlay = dis ? new MapWayPointItem.SizedRegions(smallDisabled, middleDisabled, largeDisabled) : null;

        MapWayPointItem.Regions regions = new MapWayPointItem.Regions(normal, selectedOverlay, disabledOverlay);

        return new MapWayPointItem(abstractCache, abstractCache, regions, sel);
    }

    private MapWayPointItem getMapWayPointItem(AbstractWaypoint waypoint, boolean dis, boolean sel) {
        MapWayPointItemStyle style = getClusterSymbolsByWaypoint(waypoint);

        TextureRegion small = textureRegionMap.get(((GetName) style.small).getName());
        TextureRegion middle = textureRegionMap.get(((GetName) style.middle).getName());
        TextureRegion large = textureRegionMap.get(((GetName) style.large).getName());


        MapWayPointItem.SizedRegions normal = new MapWayPointItem.SizedRegions(small, middle, large);
        MapWayPointItem.SizedRegions selectedOverlay = sel ? new MapWayPointItem.SizedRegions(smallSelected, middleSelected, largeSelected) : null;
        MapWayPointItem.SizedRegions disabledOverlay = dis ? new MapWayPointItem.SizedRegions(smallDisabled, middleDisabled, largeDisabled) : null;

        MapWayPointItem.Regions regions = new MapWayPointItem.Regions(normal, selectedOverlay, disabledOverlay);


        return new MapWayPointItem(waypoint, waypoint, regions, sel);
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
            if (mItemList.size < mItemList.getAllSize()) {
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
                WaypointLayer.this.populate(false);
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

    public static MapWayPointItemStyle getClusterSymbolsByCache(AbstractCache abstractCache) {
        String symbolStyleName = getMapIconName(abstractCache);
        return VisUI.getSkin().get(symbolStyleName, MapWayPointItemStyle.class);
    }

    public static MapWayPointItemStyle getClusterSymbolsByWaypoint(AbstractWaypoint waypoint) {
        String symbolStyleName = getMapIconName(waypoint);
        return VisUI.getSkin().get(symbolStyleName, MapWayPointItemStyle.class);
    }

    public static String getMapIconName(AbstractCache abstractCache) {
        if (abstractCache.ImTheOwner())
            return "mapStar";
        else if (abstractCache.isFound())
            return "mapFound";
        else if ((abstractCache.getType() == CacheTypes.Mystery) && abstractCache.hasCorrectedCoordinates())
            return "mapSolved";
        else if ((abstractCache.getType() == CacheTypes.Multi) && abstractCache.HasStartWaypoint())
            return "mapMultiStartP"; // Multi with start point
        else if ((abstractCache.getType() == CacheTypes.Mystery) && abstractCache.HasStartWaypoint())
            return "mapMysteryStartP"; // Mystery without Final but with start point
        else
            return "map" + abstractCache.getType().name();
    }

    public static String getMapIconName(AbstractWaypoint waypoint) {
        if ((waypoint.getType() == CacheTypes.MultiStage) && (waypoint.isStart()))
            return "mapMultiStageStartP";
        else
            return "map" + waypoint.getType().name();
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

    AbstractCache selectedAbstractCache;
    AbstractWaypoint selectedWaypoint;

    @Override
    public void selectedCacheChanged(de.longri.cachebox3.events.SelectedCacheChangedEvent event) {
        selectedAbstractCache = event.cache;
        selectedWaypoint = null;
        cacheListChanged(null);
    }

    @Override
    public void selectedWayPointChanged(de.longri.cachebox3.events.SelectedWayPointChangedEvent event) {
        selectedAbstractCache = de.longri.cachebox3.events.EventHandler.getSelectedCache();
        selectedWaypoint = event.wayPoint;
        cacheListChanged(null);
    }

    public interface ActiveItem {
        boolean run(MapWayPointItem aIndex);
    }


    private boolean onItemSingleTap(MapWayPointItem item) {
        log.debug("Click on: " + item);

        if (item.dataObject instanceof AbstractCache || item.dataObject instanceof AbstractWaypoint) {
            mapView.clickOnItem(item);
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
        if (g instanceof Gesture.Tap) {
            boolean result = activateSelectedItems(e, mActiveItemSingleTap);
            if (result == false && mapView.infoBubbleVisible()) {
                mapView.closeInfoBubble();
            }
            return result;
        }

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
        if (mItemList.size == 0)
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
        for (int i = 0, n = mItemList.size; i < n; i++) {
            if (!mItemList.get(i).visible) continue;
            MapWayPointItem item = mItemList.get(i);

            double lat, lon;
            if (item instanceof Cluster) {
                //the draw point is set to center of cluster
                final Coordinate centerCoord = ((Cluster) item).getCenter();
                lat = centerCoord.getLatitude();
                lon = centerCoord.getLongitude();
            } else {
                lat = item.getLatitude();
                lon = item.getLongitude();
            }
            if (!boundingBox.contains(lat, lon))
                continue;
            clickedItems.add(item);
        }

        if (clickedItems.size != 0) {
            MapWayPointItem clickedItem = null;
            //if more then one item so search nearest
            if (clickedItems.size == 1) {
                clickedItem = clickedItems.get(0);
            } else {
                Coordinate clickCoord = new Coordinate(clickLon, clickLat);
                double minDistance = Double.MAX_VALUE;
                for (int i = 0, n = clickedItems.size; i < n; i++) {
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
