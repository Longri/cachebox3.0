/*
 * Copyright (C) 2016-2020 team-cachebox.de
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
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.map.MapMode;
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
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.lists.CB_List;
import de.longri.cachebox3.utils.lists.ThreadStack;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.Box;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.event.Event;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.gdx.MotionHandler;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.atlas.TextureRegion;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Longri on 27.11.16.
 */
public class WaypointLayer extends Layer implements GestureListener, CacheListChangedListener, Disposable, SelectedCacheChangedListener, SelectedWayPointChangedListener {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(WaypointLayer.class);

    private static final String ERROR_MSG = "No MapWayPointItemStyle registered with name: ";

    private final WaypointLayerRenderer mClusterRenderer;
    final ClusteredList mItemList;
    private final Point clickPoint = new Point();
    private final Box mapVisibleBoundingBox = new Box();
    private final CB_List<MapWayPointItem> clickedItems = new CB_List<>();
    private final ThreadStack<ClusterRunnable> clusterWorker = new ThreadStack<>();

    private final TextureRegion smallSelected, middleSelected, largeSelected;
    private final TextureRegion smallDisabled, middleDisabled, largeDisabled;
    private final TextureRegion smallLite, middleLite, largeLite;

    private final MapView mapView;


    private double lastDistance = Double.MIN_VALUE;
    private double lastFactor = 2.0;

    private ClusterRunnable.Task lastTask;

    public WaypointLayer(MapView _mapView, Map map) {
        super(map);
        log.debug("Create new INSTANCE");

        mapView = _mapView;

        mClusterRenderer = new WaypointLayerRenderer(this, null);
        mRenderer = mClusterRenderer;
        mItemList = new ClusteredList();
        populate(true);

        //initial Overlay styles
        MapWayPointItemStyle selectedStyle = VisUI.getSkin().get("selectOverlay", MapWayPointItemStyle.class);
        smallSelected = selectedStyle.small != null ? CB.textureRegionMap.get(((GetName) selectedStyle.small).getName()) : null;
        middleSelected = selectedStyle.middle != null ? CB.textureRegionMap.get(((GetName) selectedStyle.middle).getName()) : null;
        largeSelected = selectedStyle.large != null ? CB.textureRegionMap.get(((GetName) selectedStyle.large).getName()) : null;

        MapWayPointItemStyle disabledStyle = VisUI.getSkin().get("disabledOverlay", MapWayPointItemStyle.class);
        smallDisabled = disabledStyle.small != null ? CB.textureRegionMap.get(((GetName) disabledStyle.small).getName()) : null;
        middleDisabled = disabledStyle.middle != null ? CB.textureRegionMap.get(((GetName) disabledStyle.middle).getName()) : null;
        largeDisabled = disabledStyle.large != null ? CB.textureRegionMap.get(((GetName) disabledStyle.large).getName()) : null;

        MapWayPointItemStyle liteStyle = VisUI.getSkin().get("liteOverlay", MapWayPointItemStyle.class);
        smallLite = liteStyle.small != null ? CB.textureRegionMap.get(((GetName) liteStyle.small).getName()) : null;
        middleLite = liteStyle.middle != null ? CB.textureRegionMap.get(((GetName) liteStyle.middle).getName()) : null;
        largeLite = liteStyle.large != null ? CB.textureRegionMap.get(((GetName) liteStyle.large).getName()) : null;

        //register SelectedCacheChangedEvent
        EventHandler.add(this);
        cacheListChanged(null);
        Settings.ShowAllWaypoints.addChangedEventListener(() -> cacheListChanged(null));
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
        Thread thread = new Thread(() -> CB.postOnGlThread(new NamedRunnable("") {
            @Override
            public void run() {
                //clear item list
                mItemList.clear();

                //add WayPoint items

                CB_List<String> missingIconList = new CB_List<>();
                boolean hasSelectedWP = EventHandler.getSelectedWayPoint() != null;

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
                        msg.append(", ").append(name);
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
        }));
        thread.start();
    }

    private void addCache(CB_List<String> missingIconList, boolean hasSelectedWP, AbstractCache geoCache) {
        CB.assertGlThread();
        boolean dis = geoCache.isArchived() || !geoCache.isAvailable();
        boolean isGeoCacheSelected = !hasSelectedWP && EventHandler.isSelectedCache(geoCache);
        try {
            MapWayPointItem geoCluster = getMapWayPointItem(geoCache, dis, isGeoCacheSelected);
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
        isGeoCacheSelected = EventHandler.isSelectedCache(geoCache);
        AbstractWaypoint selWp = null;
        if (geoCache.getWaypoints() != null) {
            if (Settings.ShowAllWaypoints.getValue() || isGeoCacheSelected) {
                selWp = selectedWaypoint = EventHandler.getSelectedWayPoint();
                for (AbstractWaypoint waypoint : geoCache.getWaypoints()) {
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
                    }
                }
            }
        }
        if (isGeoCacheSelected) {
            if (selWp == null) {
                selectedGeoCache = geoCache;
                log.debug("set selected Cache {}", geoCache);
            } else {
                selectedGeoCache = null;
                log.debug("set selected Waypoint {}", selectedWaypoint);
            }
        }
    }

    private MapWayPointItem getMapWayPointItem(AbstractCache geoCache, boolean isDisabled, boolean isSelected) {
        MapWayPointItemStyle style = getClusterSymbolsByCache(geoCache);
        TextureRegion small = style.small == null ? null : CB.textureRegionMap.get(((GetName) style.small).getName());
        TextureRegion middle = style.middle == null ? null : CB.textureRegionMap.get(((GetName) style.middle).getName());
        TextureRegion large = style.large == null ? null : CB.textureRegionMap.get(((GetName) style.large).getName());

        MapWayPointItem.SizedRegions normal = new MapWayPointItem.SizedRegions(small, middle, large);
        MapWayPointItem.SizedRegions selectedOverlay = isSelected ? new MapWayPointItem.SizedRegions(smallSelected, middleSelected, largeSelected) : null;
        MapWayPointItem.SizedRegions disabledOverlay = isDisabled ? new MapWayPointItem.SizedRegions(smallDisabled, middleDisabled, largeDisabled) : null;
        MapWayPointItem.SizedRegions liteOverlay;
        // MapWayPointItem.SizedRegions liteOverlay = geoCache.isLive() ? new MapWayPointItem.SizedRegions(smallLite, middleLite, largeLite) : null;
        if (geoCache.isLive()) {
            liteOverlay = new MapWayPointItem.SizedRegions(smallLite, middleLite, largeLite);
        }
        else {
            liteOverlay = null;
        }

        MapWayPointItem.Regions regions = new MapWayPointItem.Regions(normal, selectedOverlay, disabledOverlay, liteOverlay);

        return new MapWayPointItem(geoCache, geoCache, regions, isSelected);
    }

    private MapWayPointItem getMapWayPointItem(AbstractWaypoint waypoint, boolean isDisabled, boolean isSelected) {
        MapWayPointItemStyle style = getClusterSymbolsByWaypoint(waypoint);

        TextureRegion small = CB.textureRegionMap.get(((GetName) style.small).getName());
        TextureRegion middle = CB.textureRegionMap.get(((GetName) style.middle).getName());
        TextureRegion large = CB.textureRegionMap.get(((GetName) style.large).getName());


        MapWayPointItem.SizedRegions normal = new MapWayPointItem.SizedRegions(small, middle, large);
        MapWayPointItem.SizedRegions selectedOverlay = isSelected ? new MapWayPointItem.SizedRegions(smallSelected, middleSelected, largeSelected) : null;
        MapWayPointItem.SizedRegions disabledOverlay = isDisabled ? new MapWayPointItem.SizedRegions(smallDisabled, middleDisabled, largeDisabled) : null;

        MapWayPointItem.Regions regions = new MapWayPointItem.Regions(normal, selectedOverlay, disabledOverlay, null);


        return new MapWayPointItem(waypoint, waypoint, regions, isSelected);
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


        ClusterRunnable clusterRunnable = new ClusterRunnable(distance, mItemList, () -> {
            WaypointLayer.this.populate(false);
            mMap.updateMap(true);
            mMap.render();
        }, boundingBox, task, all);

        if (lastTask == task) {
            clusterWorker.pushAndStart(clusterRunnable);
        } else {
            clusterWorker.pushAndStartWithCancelRunning(clusterRunnable);
            lastTask = task;
        }


    }

    public static MapWayPointItemStyle getClusterSymbolsByCache(AbstractCache geoCache) {
        String symbolStyleName = getMapIconName(geoCache);
        return VisUI.getSkin().get(symbolStyleName, MapWayPointItemStyle.class);
    }

    public static MapWayPointItemStyle getClusterSymbolsByWaypoint(AbstractWaypoint waypoint) {
        String symbolStyleName = getMapIconName(waypoint);
        return VisUI.getSkin().get(symbolStyleName, MapWayPointItemStyle.class);
    }

    public static String getMapIconName(AbstractCache geoCache) {
        if (geoCache.iAmTheOwner())
            return "mapStar";
        else if (geoCache.isFound())
            return "mapFound";
        else if ((geoCache.getType() == CacheTypes.Mystery) && geoCache.hasCorrectedCoordinates())
            return "mapSolved";
        else if ((geoCache.getType() == CacheTypes.Multi) && geoCache.hasStartWaypoint())
            return "mapMultiStartP"; // Multi with start point
        else if ((geoCache.getType() == CacheTypes.Mystery) && geoCache.hasStartWaypoint())
            return "mapMysteryStartP"; // Mystery without Final but with start point
        else
            return "map" + geoCache.getType().name();
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

    AbstractCache selectedGeoCache;
    AbstractWaypoint selectedWaypoint;

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        selectedGeoCache = event.cache;
        selectedWaypoint = null;
        cacheListChanged(null);
        mapView.getMapStateButton().setMapMode(MapMode.WP, false, new Event());
    }

    @Override
    public void selectedWayPointChanged(SelectedWayPointChangedEvent event) {
        selectedGeoCache = EventHandler.getSelectedCache();
        selectedWaypoint = event.wayPoint;
        cacheListChanged(null);
        mapView.getMapStateButton().setMapMode(MapMode.WP, false, new Event());
    }

    public int getLastZoomLevel() {
        return mClusterRenderer.getLastZoomLevel();
    }

    public void setLastZoomLevel(int _lastZoomLevel) {
        mClusterRenderer.setLastZoomLevel(_lastZoomLevel);
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

    private final ActiveItem mActiveItemSingleTap = this::onItemSingleTap;

    private final ActiveItem mActiveItemLongPress = this::onItemLongPress;

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {
        if (!(e instanceof MotionHandler)) return false;
        if (g instanceof Gesture.Tap) {
            boolean result = activateSelectedItems(e, mActiveItemSingleTap);
            if (!result && mapView.infoBubbleVisible()) {
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
