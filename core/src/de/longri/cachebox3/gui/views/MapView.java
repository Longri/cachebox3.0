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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.GetName;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.animations.map.MapAnimator;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.gui.map.baseMap.OSciMap;
import de.longri.cachebox3.gui.map.layer.*;
import de.longri.cachebox3.gui.skin.styles.MapArrowStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.utils.IChanged;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.event.Event;
import org.oscim.gdx.GestureHandlerImpl;
import org.oscim.layers.GroupLayer;
import org.oscim.layers.Layer;
import org.oscim.layers.TileGridLayer;
import org.oscim.map.Layers;
import org.oscim.map.Map;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.MapRenderer;
import org.oscim.renderer.atlas.TextureAtlas;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.TextItem;
import org.oscim.renderer.bucket.TextureBucket;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.scalebar.*;
import org.oscim.utils.FastMath;
import org.oscim.utils.TextureAtlasUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * The MapView has transparent background. The Map render runs at CacheboxMain.
 * This View has only the controls for the Map!
 * Created by Longri on 24.07.16.
 */
public class MapView extends AbstractView {
    private final static Logger log = LoggerFactory.getLogger(MapView.class);

    private static double lastCenterPosLat, lastCenterPosLon;
    private static MapMode lastMapMode = MapMode.FREE;

    public static Coordinate getLastCenterPos() {
        return new Coordinate(lastCenterPosLat, lastCenterPosLon);
    }

    private InputMultiplexer mapInputHandler;
    private CacheboxMapAdapter map;
    private final CacheboxMain main;
    private MapScaleBarLayer mapScaleBarLayer;
    private final MapStateButton mapStateButton;
    private final ZoomButton zoomButton;
    public final MapAnimator animator;
    private WaypointLayer wayPointLayer;
    private DirectLineLayer directLineLayer;
    private CenterCrossLayer ccl;
    private MapInfoPanel infoPanel;

    private final MapState lastMapState = new MapState();

    private LocationAccuracyLayer myLocationAccuracy;

    private LocationLayer myLocationLayer;

    private MapViewPositionChangedHandler positionChangedHandler;


    public MapView(CacheboxMain main) {
        super("MapView");
        this.setTouchable(Touchable.disabled);
        this.main = main;
        if (CB.textureRegionMap == null) CB.textureRegionMap = createTextureAtlasRegions();

        mapStateButton = new MapStateButton(new MapStateButton.StateChangedListener() {

            private final Event selfEvent = new Event();

            @Override
            public void stateChanged(MapMode mapMode, MapMode lastMapMode, Event event) {

                if (mapMode == MapMode.CAR) {
                    lastMapState.setMapMode(lastMapMode);
                    log.debug("Activate Carmode with last mapstate:" + lastMapState);
                    float bearing = -EventHandler.getHeading();
                    positionChangedHandler.setBearing(bearing);
                    setBuildingLayerEnabled(false);
                    setCenterCrossLayerEnabled(false);

                } else if (lastMapMode == MapMode.CAR) {
                    log.debug("Disable Carmode! Activate last Mode:" + lastMapState);
                    setBuildingLayerEnabled(true);
                    final MapPosition mapPosition = map.getMapPosition();
                    // restore last MapState
                    if (lastMapState.getMapMode() == MapMode.WP) {
                        final Coordinate wpCoord = EventHandler.getSelectedCoord();
                        log.debug("set animation to WP coords");
                        mapPosition.setPosition(wpCoord.latitude, wpCoord.longitude);
                    }
                    mapStateButton.setMapMode(lastMapState.getMapMode(), true, selfEvent);
                    mapPosition.setTilt(map.viewport().getMinTilt());
                    float ori = 0;
                    if (lastMapState.getMapOrientationMode() != MapOrientationMode.NORTH) {
                        ori = EventHandler.getHeading();
                    }
                    animator.rotate(ori);
                    animator.scale(1 << lastMapState.getZoom());
                    map.updateMap(true);
                } else if (mapMode == MapMode.GPS) {
                    log.debug("Activate GPS Mode");
                    final Coordinate myPos = EventHandler.getMyPosition();
                    if (myPos != null) {
                        animator.position(
                                MercatorProjection.longitudeToX(myPos.longitude),
                                MercatorProjection.latitudeToY(myPos.latitude)
                        );
                    }
                    setCenterCrossLayerEnabled(false);
                } else if (mapMode == MapMode.WP) {
                    log.debug("Activate WP Mode");
                    final Coordinate wpCoord = EventHandler.getSelectedCoord();
                    animator.position(
                            MercatorProjection.longitudeToX(wpCoord.longitude),
                            MercatorProjection.latitudeToY(wpCoord.latitude)
                    );
                    setCenterCrossLayerEnabled(false);
                } else if (mapMode == MapMode.LOCK) {
                    setCenterCrossLayerEnabled(false);
                } else if (mapMode == MapMode.FREE) {
                    setCenterCrossLayerEnabled(true);
                }
                if (event != selfEvent && mapMode != MapMode.CAR && lastMapMode != MapMode.CAR)
                    setMapState(lastMapState);
            }
        });
        infoPanel = new MapInfoPanel();
        infoPanel.setBounds(10, 100, 200, 100);
        this.addActor(infoPanel);

        map = createMap();
        this.animator = new MapAnimator(map);
        this.addActor(mapStateButton);
        this.setTouchable(Touchable.enabled);

        this.zoomButton = new ZoomButton(new ZoomButton.ValueChangeListener() {
            @Override
            public void valueChanged(int changeValue) {

                MapPosition mapPosition = map.getMapPosition();
                double value = mapPosition.getScale();
                if (changeValue > 0)
                    value = value * 2;
                else
                    value = value * 0.5;

                animator.scale(value);
                lastMapState.setZoom(FastMath.log2((int) value));
            }
        });
        this.zoomButton.pack();
        this.addActor(zoomButton);


    }

    private void setCenterCrossLayerEnabled(boolean enabled) {
        enabled &= Settings_Map.ShowMapCenterCross.getValue();
        ccl.setEnabled(enabled);
    }

    private void setBuildingLayerEnabled(boolean enabled) {
        Layers layers = map.layers();
        for (Layer layer : layers) {
            if (layer instanceof CacheboxMapAdapter.BuildingLabelLayer) {
                log.debug("{} BuildingLayer", enabled ? "Enable" : "Disable");
                ((CacheboxMapAdapter.BuildingLabelLayer) layer).buildingLayer.setEnabled(enabled);
            } else if (layer instanceof GroupLayer) {
                List<Layer> groupLayers = ((GroupLayer) layer).layers;
                for (Layer l : groupLayers) {
                    if (l instanceof CenterCrossLayer) {
                        log.debug("{} CenterCrossLayer", enabled ? "Enable" : "Disable");
                        if (enabled) {
                            //check settings
                            l.setEnabled(Settings_Map.ShowMapCenterCross.getValue());
                        } else {
                            l.setEnabled(false);
                        }
                    }
                }
            }
        }
    }


    private CacheboxMapAdapter createMap() {


        {//set map scale
            float scaleFactor = CB.getScaledFloat(Settings.MapViewDPIFaktor.getValue());
            Tile.SIZE = (int) (400 * scaleFactor);
            CanvasAdapter.dpi = 240 * scaleFactor;
            CanvasAdapter.textScale = scaleFactor;
            CanvasAdapter.scale = scaleFactor;
            log.debug("Create new map instance with scale factor:" + Float.toString(scaleFactor));
            log.debug("Tile.SIZE:" + Integer.toString(Tile.SIZE));
            log.debug("Canvas.dpi:" + Float.toString(CanvasAdapter.dpi));
        }


        CacheboxMain.drawMap = true;
        map = new CacheboxMapAdapter() {

            @Override
            public void beginFrame() {
                super.beginFrame();
                animator.update(Gdx.graphics.getDeltaTime());
            }

            @Override
            public void onMapEvent(Event e, final MapPosition mapPosition) {
                super.onMapEvent(e, mapPosition);
                if (e == Map.MOVE_EVENT) {
//                    log.debug("Map.MOVE_EVENT");
                    if (CB.mapMode != MapMode.FREE)
                        mapStateButton.setMapMode(MapMode.FREE, new Event());
                } else if (e == Map.TILT_EVENT) {
//                    log.debug("Map.TILT_EVENT");
                    if (positionChangedHandler != null)
                        positionChangedHandler.tiltChangedFromMap(mapPosition.getTilt());
                } else if (e == Map.ROTATE_EVENT) {
//                    log.debug("Map.ROTATE_EVENT");
                    if (positionChangedHandler != null)
                        positionChangedHandler.rotateChangedFromUser(mapPosition.getBearing());
                }

                lastCenterPosLat = mapPosition.getLatitude();
                lastCenterPosLon = mapPosition.getLongitude();
            }
        };
        main.mMapRenderer = new MapRenderer(map);
        main.mMapRenderer.onSurfaceCreated();

        this.lastMapState.setValues(Settings_Map.lastMapState.getValue());
        this.lastMapState.setValues(Settings_Map.lastMapState.getValue());

        double lastLatitude = Settings_Map.MapInitLatitude.getValue();
        double lastLongitude = Settings_Map.MapInitLongitude.getValue();

        map.setMapPosition(lastLatitude, lastLongitude, 1 << this.lastMapState.getZoom());

        initLayers(false);


        //add position changed handler
        positionChangedHandler = new MapViewPositionChangedHandler(
                this, map, myLocationLayer,
                myLocationAccuracy, mapStateButton, infoPanel);

        return map;
    }

    public static LinkedHashMap<Object, TextureRegion> createTextureAtlasRegions() {
        // create TextureRegions from all Bitmap symbols
        LinkedHashMap<Object, TextureRegion> textureRegionMap = new LinkedHashMap<>();
        ObjectMap<String, MapWayPointItemStyle> list = VisUI.getSkin().getAll(MapWayPointItemStyle.class);
        Array<Bitmap> bitmapList = new Array<>();
        for (MapWayPointItemStyle style : list.values()) {
            if (style.small != null) if (!bitmapList.contains(style.small, true)) bitmapList.add(style.small);
            if (style.middle != null) if (!bitmapList.contains(style.middle, true)) bitmapList.add(style.middle);
            if (style.large != null) if (!bitmapList.contains(style.large, true)) bitmapList.add(style.large);
        }

        //add Bitmaps from MapArrowStyle
        MapArrowStyle mapArrowStyle = null;
        try {
            mapArrowStyle = VisUI.getSkin().get("myLocation", MapArrowStyle.class);
        } catch (Exception e) {
            log.error("Get MapArrowStyle 'myLocation'", e);
        }

        if (mapArrowStyle != null) {
            if (mapArrowStyle.myLocation != null) bitmapList.add(mapArrowStyle.myLocation);
            if (mapArrowStyle.myLocationTransparent != null) bitmapList.add(mapArrowStyle.myLocationTransparent);
            if (mapArrowStyle.myLocationCar != null) bitmapList.add(mapArrowStyle.myLocationCar);
        }


        LinkedHashMap<Object, Bitmap> input = new LinkedHashMap<>();
        for (Bitmap bmp : bitmapList) {
            input.put(((GetName) bmp).getName(), bmp);
        }
        ArrayList<TextureAtlas> atlasList = new ArrayList<>();
        boolean flipped = CanvasAdapter.platform == Platform.IOS;
        System.out.print("create MapTextureAtlas with flipped Y? " + flipped);
        TextureAtlasUtils.createTextureRegions(input, textureRegionMap, atlasList, false,
                flipped);


//        if (false) {//Debug write atlas Bitmap to tmp folder
//            int count = 0;
//            for (TextureAtlas atlas : atlasList) {
//                byte[] data = atlas.texture.bitmap.getPngEncodedData();
//                Pixmap pixmap = new Pixmap(data, 0, data.length);
//                FileHandle file = Gdx.files.absolute(CB.WorkPath + "/user/temp/testAtlas" + count++ + ".png");
//                PixmapIO.writePNG(file, pixmap);
//                pixmap.dispose();
//            }
//        }
        return textureRegionMap;
    }

    @Override
    protected void create() {
        // override and don't call super
        // for non creation of default name label
    }

    @Override
    public void onShow() {
        addInputListener();
        sizeChanged();

        //set initial direction
        infoPanel.setNewValues(EventHandler.getMyPosition(), EventHandler.getHeading());
        CB.mapMode = lastMapMode;
    }

    @Override
    public void onHide() {
        removeInputListener();
        lastMapMode = CB.mapMode;
        CB.mapMode = MapMode.NONE;
    }


    @Override
    public void dispose() {
        log.debug("Dispose MapView");

        //save last position for next initial
        MapPosition mapPosition = this.map.getMapPosition();
        Settings_Map.lastMapState.setValue(lastMapState.getValues());
        Settings_Map.MapInitLatitude.setValue(mapPosition.getLatitude());
        Settings_Map.MapInitLongitude.setValue(mapPosition.getLongitude());
        Config.AcceptChanges();

        positionChangedHandler.dispose();
        positionChangedHandler = null;

        Layers layers = map.layers();
        for (Layer layer : layers) {
            if (layer instanceof Disposable) {
                ((Disposable) layer).dispose();
            } else if (layer instanceof GroupLayer) {
                for (Layer groupLayer : ((GroupLayer) layer).layers) {
                    if (groupLayer instanceof Disposable) {
                        ((Disposable) groupLayer).dispose();
                    }
                }
            }
        }

        layers.clear();

        wayPointLayer = null;

        mapInputHandler.clear();
        mapInputHandler = null;

        CacheboxMain.drawMap = false;
        map.clearMap();
        map.destroy();
        TextureBucket.pool.clear();
        TextItem.pool.clear();
        TextureItem.disposeTextures();

        main.mMapRenderer = null;
        map = null;

        //dispose actors
        mapStateButton.dispose();

        infoPanel.dispose();

    }

    private void setMapState(MapState state) {
        state.setMapMode(CB.mapMode);
//        state.setMapOrientationMode(mapOrientationButton.getMode());
        state.setZoom(this.map.getMapPosition().getZoomLevel());
    }

    @Override
    public void sizeChanged() {
        if (map == null) return;
        map.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
        map.viewport().setScreenSize((int) this.getWidth(), (int) this.getHeight());
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

        // set position of MapScaleBar
        setMapScaleBarOffset(CB.scaledSizes.MARGIN, CB.scaledSizes.MARGIN_HALF);

        mapStateButton.setPosition(getWidth() - (mapStateButton.getWidth() + CB.scaledSizes.MARGIN),
                getHeight() - (mapStateButton.getHeight() + CB.scaledSizes.MARGIN));

        zoomButton.setPosition(getWidth() - (zoomButton.getWidth() + CB.scaledSizes.MARGIN), CB.scaledSizes.MARGIN);

        float calculatedWidth = Math.min(infoPanel.getPrefWidth(), mapStateButton.getX() - (CB.scaledSizes.MARGINx2));

        infoPanel.setBounds(CB.scaledSizes.MARGIN,
                getHeight() - (infoPanel.getHeight() + CB.scaledSizes.MARGIN),
                calculatedWidth, infoPanel.getPrefHeight());
    }


    @Override
    public void positionChanged() {
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
    }


    private void initLayers(boolean tileGrid) {

        log.debug("Init layer");

        // load last saved BaseMap
        String baseMapName = Settings_Map.CurrentMapLayer.getValue()[0];
        BaseMapManager.INSTANCE.setMapFolder(Gdx.files.absolute(Settings_Map.MapPackFolder.getValue()));
        AbstractManagedMapLayer baseMap = null;
        for (int i = 0, n = BaseMapManager.INSTANCE.size; i < n; i++) {
            AbstractManagedMapLayer map = BaseMapManager.INSTANCE.get(i);
            if (baseMapName.equals(map.name)) {
                baseMap = map;
                break;
            }
        }

        if (baseMap == null) {
            baseMap = new OSciMap();
        }

        setBaseMap(baseMap);

        DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(map);
        mapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
        mapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
        mapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
        mapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

        directLineLayer = new DirectLineLayer(map);
        mapScaleBarLayer = new MapScaleBarLayer(map, mapScaleBar);
        wayPointLayer = new WaypointLayer(map, CB.textureRegionMap);
        myLocationAccuracy = new LocationAccuracyLayer(map);
        myLocationLayer = new LocationLayer(map, CB.textureRegionMap);

        boolean showDirectLine = Settings_Map.ShowDirektLine.getValue();
        log.debug("Initial direct line layer and {}", showDirectLine ? "enable" : "disable");
        directLineLayer.setEnabled(showDirectLine);
        GroupLayer layerGroup = new GroupLayer(map);

        ccl = new CenterCrossLayer(map);


        if (tileGrid)
            layerGroup.layers.add(new TileGridLayer(map));

        layerGroup.layers.add(myLocationAccuracy);
        layerGroup.layers.add(wayPointLayer);
        layerGroup.layers.add(directLineLayer);
        layerGroup.layers.add(myLocationLayer);
        layerGroup.layers.add(mapScaleBarLayer);
        layerGroup.layers.add(ccl);

        Settings_Map.ShowDirektLine.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                log.debug("change direct line visibility to {}", Settings_Map.ShowDirektLine.getValue() ? "visible" : "invisible");
                directLineLayer.setEnabled(Settings_Map.ShowDirektLine.getValue());
                map.updateMap(true);
            }
        });


        boolean showCenterCross = Settings_Map.ShowMapCenterCross.getValue();
        log.debug("Initial center cross layer and {}", showCenterCross ? "enable" : "disable");

        ccl.setEnabled(showCenterCross);
        Settings_Map.ShowMapCenterCross.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                log.debug("change center cross visibility to {}", Settings_Map.ShowMapCenterCross.getValue() ? "visible" : "invisible");
                setCenterCrossLayerEnabled(Settings_Map.ShowMapCenterCross.getValue());
                map.updateMap(true);
            }
        });
        map.layers().add(layerGroup);
    }

    private void setMapScaleBarOffset(float xOffset, float yOffset) {
        if (mapScaleBarLayer == null) return;
        BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
        renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
        renderer.setOffset(xOffset, yOffset);
    }


    public void setInputListener(boolean on) {
        if (on) {
            addInputListener();
        } else {
            removeInputListener();
        }
    }


    private void createMapInputHandler() {
        GestureDetector gestureDetector = new GestureDetector(new GestureHandlerImpl(map));
        MapMotionHandler motionHandler = new MapMotionHandler(map, mapStateButton);
        MapInputHandler inputHandler = new MapInputHandler(map, mapStateButton);
        mapInputHandler = new InputMultiplexer();
        mapInputHandler.addProcessor(gestureDetector);
        mapInputHandler.addProcessor(motionHandler);
        mapInputHandler.addProcessor(inputHandler);
    }

    private void addInputListener() {
        if (mapInputHandler == null) createMapInputHandler();
        StageManager.addMapMultiplexer(mapInputHandler);
    }

    private void removeInputListener() {
        StageManager.removeMapMultiplexer(mapInputHandler);
    }

    public boolean getAlignToCompass() {
        return false;//mapOrientationButton.isNorthOriented();
    }

    public void setAlignToCompass(boolean align) {
//        mapOrientationButton.setMode(align ? MapOrientationMode.NORTH : MapOrientationMode.COMPASS);
    }

    public void setNewSettings() {
        //TODO
    }

    public void setBaseMap(AbstractManagedMapLayer baseMap) {
        this.map.setNewBaseMap(baseMap);
    }

    public Coordinate getMapCenter() {
        MapPosition mp = this.map.getMapPosition();
        return new Coordinate(mp.getLatitude(), mp.getLongitude());
    }
}
