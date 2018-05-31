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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.GetName;
import com.badlogic.gdx.scenes.scene2d.ui.MapWayPointItem;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedWayPointChangedEvent;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.actions.Action_Add_WP;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import de.longri.cachebox3.gui.map.NamedExternalRenderTheme;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.gui.map.baseMap.MapsforgeSingleMap;
import de.longri.cachebox3.gui.map.baseMap.OSciMap;
import de.longri.cachebox3.gui.map.layer.*;
import de.longri.cachebox3.gui.menu.*;
import de.longri.cachebox3.gui.skin.styles.MapArrowStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.MapBubble;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.track.TrackRecorder;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.settings.types.SettingBool;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.utils.EQUALS;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
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
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.VtmThemes;
import org.oscim.theme.XmlRenderThemeStyleLayer;
import org.oscim.utils.FastMath;
import org.oscim.utils.TextureAtlasUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * The MapView has transparent background. The Map render runs at CacheboxMain.
 * This View has only the controls for the Map!
 * Created by Longri on 24.07.16.
 */
public class MapView extends AbstractView {
    private final static Logger log = LoggerFactory.getLogger(MapView.class);

    private static double lastCenterPosLat, lastCenterPosLon;
    private static MapMode lastMapMode = MapMode.FREE;
    Point screenPoint = new Point();

    public static Coordinate getLastCenterPos() {
        return new Coordinate(lastCenterPosLat, lastCenterPosLon);
    }

    private InputMultiplexer mapInputHandler;
    private CacheboxMapAdapter map;
    private final CacheboxMain main;
    private MapScaleBarLayer mapScaleBarLayer;
    private final MapStateButton mapStateButton;
    private final ZoomButton zoomButton;
    private WaypointLayer wayPointLayer;
    private DirectLineLayer directLineLayer;
    private CenterCrossLayer ccl;
    private MapInfoPanel infoPanel;

    private final MapState lastMapState = new MapState();

    private LocationTextureLayer myLocationLayer;

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
                    positionChangedHandler.rotate(ori);
                    positionChangedHandler.scale(1 << lastMapState.getZoom());
                    map.updateMap(true);
                } else if (mapMode == MapMode.GPS) {
                    log.debug("Activate GPS Mode");
                    final Coordinate myPos = EventHandler.getMyPosition();
                    if (myPos != null) {
                        positionChangedHandler.position(
                                MercatorProjection.longitudeToX(myPos.longitude),
                                MercatorProjection.latitudeToY(myPos.latitude)
                        );
                    }
                    setCenterCrossLayerEnabled(false);
                } else if (mapMode == MapMode.WP) {
                    final Coordinate wpCoord = EventHandler.getSelectedCoord();
                    if (wpCoord == null) {
                        // we hav no selected WP, so switch MapMode to 'LOCK'
                        CB.postOnGlThread(new NamedRunnable("MapView") {
                            @Override
                            public void run() {
                                mapStateButton.setMapMode(MapMode.LOCK, true, new Event());
                            }
                        });
                    } else {
                        log.debug("Activate WP Mode");
                        positionChangedHandler.animateToPos(
                                MercatorProjection.longitudeToX(wpCoord.longitude),
                                MercatorProjection.latitudeToY(wpCoord.latitude)
                        );
                        setCenterCrossLayerEnabled(false);
                    }
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

                positionChangedHandler.scale(value);
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
            }
        }
    }


    private CacheboxMapAdapter createMap() {


        log.debug("Tile.SIZE:" + Integer.toString(Tile.SIZE));
        log.debug("Canvas.dpi:" + Float.toString(CanvasAdapter.dpi));


        CacheboxMain.drawMap = true;
        map = new CacheboxMapAdapter() {

            @Override
            public void beginFrame() {
                if (!CacheboxMain.drawMap) return;
                super.beginFrame();
                if (positionChangedHandler != null) positionChangedHandler.update(Gdx.graphics.getDeltaTime());
            }

            @Override
            public void onMapEvent(Event e, final MapPosition mapPosition) {
                if (!CacheboxMain.drawMap) return;
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

                if (infoBubble != null && infoItem != null) {
                    setInfoBubblePos();
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
        positionChangedHandler = new MapViewPositionChangedHandler(map, directLineLayer, myLocationLayer, infoPanel);

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
            log.error("get MapArrowStyle 'myLocation'", e);
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
        CacheboxMain.drawMap = false;
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


        map.clearMap();
        map.destroy();
        CB.postOnGlThread(new NamedRunnable("MapView:dispose texture items") {
            @Override
            public void run() {
                TextureBucket.pool.clear();
                TextItem.pool.clear();
                TextureItem.disposeTextures();
            }
        });

        main.mMapRenderer = null;
        map = null;

        //dispose actors
        mapStateButton.dispose();

        infoPanel.dispose();
        Settings_Map.ShowMapCenterCross.removeChangedEventListener(showMapCenterCrossChangedListener);
    }

    private void setMapState(MapState state) {
        state.setMapMode(CB.mapMode);
        state.setZoom(this.map.getMapPosition().getZoomLevel());
    }

    @Override
    public void sizeChanged() {
        if (map == null) return;
        map.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
        map.viewport().setViewSize((int) this.getWidth(), (int) this.getHeight());
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

        mapHalfWith = map.getWidth() / 2;
        mapHalfHeight = map.getHeight() / 2;

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
        BaseMapManager.INSTANCE.refreshMaps(Gdx.files.absolute(Settings_Map.MapPackFolder.getValue()));
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
        wayPointLayer = new WaypointLayer(this, map, CB.textureRegionMap);
//        myLocationAccuracy = new LocationAccuracyLayer(map);
        MapArrowStyle style = VisUI.getSkin().get("myLocation", MapArrowStyle.class);
        String bmpName = ((GetName) style.myLocation).getName();
        TextureRegion textureRegion = CB.textureRegionMap.get(bmpName);
        myLocationLayer = new LocationTextureLayer(map, textureRegion);
        myLocationLayer.setAccuracyColor(Color.BLUE);
        myLocationLayer.setIndicatorColor(Color.RED);

        boolean showDirectLine = Settings_Map.ShowDirektLine.getValue();
        log.debug("Initial direct line layer and {}", showDirectLine ? "enable" : "disable");
        directLineLayer.setEnabled(showDirectLine);
        GroupLayer layerGroup = new GroupLayer(map);

        ccl = new CenterCrossLayer(map);


        if (tileGrid)
            layerGroup.layers.add(new TileGridLayer(map));

        layerGroup.layers.add(wayPointLayer);
        layerGroup.layers.add(directLineLayer);
        layerGroup.layers.add(myLocationLayer);
        layerGroup.layers.add(mapScaleBarLayer);
        layerGroup.layers.add(ccl);

        Settings_Map.ShowDirektLine.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                if (map == null) return;
                log.debug("change direct line visibility to {}", Settings_Map.ShowDirektLine.getValue() ? "visible" : "invisible");
                directLineLayer.setEnabled(Settings_Map.ShowDirektLine.getValue());
                map.updateMap(true);
            }
        });


        boolean showCenterCross = Settings_Map.ShowMapCenterCross.getValue();
        log.debug("Initial center cross layer and {}", showCenterCross ? "enable" : "disable");

        ccl.setEnabled(showCenterCross);
        Settings_Map.ShowMapCenterCross.addChangedEventListener(showMapCenterCrossChangedListener);
        map.layers().add(layerGroup);
    }

    private final IChanged showMapCenterCrossChangedListener = new IChanged() {
        @Override
        public void isChanged() {
            if (map == null) return;
            log.debug("change center cross visibility to {}", Settings_Map.ShowMapCenterCross.getValue() ? "visible" : "invisible");
            setCenterCrossLayerEnabled(Settings_Map.ShowMapCenterCross.getValue());
            map.updateMap(true);
        }
    };

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

    private void setNewSettings() {
        //TODO
    }

    public void setBaseMap(AbstractManagedMapLayer baseMap) {
        this.map.setNewBaseMap(baseMap);
    }

    public Coordinate getMapCenter() {
        MapPosition mp = this.map.getMapPosition();
        return new Coordinate(mp.getLatitude(), mp.getLongitude());
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("menu_mapviewgl");

        icm.addItem(MenuID.MI_LAYER, "Layer", CB.getSkin().getMenuIcon.mapLayer);
        icm.addItem(MenuID.MI_MAPVIEW_THEME, "Renderthemes", CB.getSkin().getMenuIcon.theme);


        //if actual Theme styleable, show style menu point
        ThemeMenuCallback callback = (ThemeMenuCallback) CB.actThemeFile.getMenuCallback();
        if (callback != null && callback.hasStyleMenu()) {
            icm.addItem(MenuID.MI_MAPVIEW_THEME_STYLE, "Styles", CB.getSkin().getMenuIcon.themeStyle);
        }

        //ISSUE (#110 add MapView Overlays) icm.addItem(MenuID.MI_MAPVIEW_OVERLAY_VIEW, "overlays");
        icm.addItem(MenuID.MI_CENTER_WP, "CenterWP", CB.getSkin().getMenuIcon.addWp);
        icm.addItem(MenuID.MI_MAPVIEW_VIEW, "view", CB.getSkin().getMenuIcon.viewSettings);

        //ISSUE (#112 Record Track)   icm.addItem(MenuID.MI_TREC_REC, "RecTrack");
        //ISSUE (#113 Add Map download)   icm.addItem(MenuID.MI_MAP_DOWNOAD, "MapDownload");

        icm.setOnItemClickListener(onItemClickListener);
        return icm;
    }

    private void showMapLayerMenu() {
        Menu icm = new Menu("MapViewShowLayerContextMenu");

        BaseMapManager.INSTANCE.refreshMaps(Gdx.files.absolute(CB.WorkPath));


        int menuID = 0;
        for (int i = 0, n = BaseMapManager.INSTANCE.size; i < n; i++) {

            AbstractManagedMapLayer baseMap = BaseMapManager.INSTANCE.get(i);

            if (!baseMap.isOverlay) {
                MenuItem mi = icm.addItem(menuID++, "", baseMap.name); // == friendlyName == FileName !!! without translation
                mi.setData(baseMap);
                mi.setCheckable(true);

                //set icon (Online_BMP, Online_Vector, Mapsforge or Freizeitkarte)
                Drawable icon = null;
                MenuIconStyle style = VisUI.getSkin().get(MenuIconStyle.class);

                if (baseMap.isOnline) {
                    if (baseMap.isVector()) {
                        icon = style.baseMapOnlineVector;
                    } else {
                        icon = style.baseMapOnlineBitmap;
                    }
                } else {
                    if (baseMap instanceof MapsforgeSingleMap) {
                        MapsforgeSingleMap map = (MapsforgeSingleMap) baseMap;
                        if (map.isFreizeitKarte()) {
                            icon = style.baseMapFreizeitkarte;
                        } else {
                            icon = style.baseMapMapsforge;
                        }
                    }

                }

                if (icon != null)
                    mi.setIcon(icon);

                String[] currentLayer = Settings_Map.CurrentMapLayer.getValue();

                for (int j = 0, m = currentLayer.length; j < m; j++) {
                    String str = currentLayer[j];
                    if (str.equals(baseMap.name)) {
                        mi.setChecked(true);
                        break;
                    }
                }
            }
        }

        icm.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                final AbstractManagedMapLayer baseMap = (AbstractManagedMapLayer) item.getData();

                // if curent layer a Mapsforge map, it is posible to add the selected Mapsforge map
                // to the current layer. We ask the User!
//                if (MapView.mapTileLoader.getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
//                    GL_MsgBox msgBox = GL_MsgBox.show("add or change", "Map selection", MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//
//                            switch (which) {
//                                case GL_MsgBox.BUTTON_POSITIVE:
//                                    // add the selected map to the curent layer
//                                    TabMainView.mapView.addToCurrentLayer(layer);
//                                    break;
//                                case GL_MsgBox.BUTTON_NEUTRAL:
//                                    // switch curent layer to selected
//                                    TabMainView.mapView.setCurrentLayer(layer);
//                                    break;
//                                default:
//                                    // do nothing
//                            }
//
//                            return true;
//                        }
//                    });
//                    msgBox.button1.setText("add");
//                    msgBox.button2.setText("select");
//                    return true;
//                }

                setBaseMap(baseMap);
                return true;
            }
        });

        icm.show();
    }

    private void showMapOverlayMenu() {
        final Menu icm = new Menu("MapViewShowMapOverlayMenu");

//        int menuID = 0;
//        for (Layer layer : ManagerBase.Manager.getLayers()) {
//            if (layer.isOverlay()) {
//                MenuItem mi = icm.addCheckableItem(menuID++, layer.FriendlyName, layer == MapView.mapTileLoader.getCurrentOverlayLayer());
//                mi.setData(layer);
//            }
//        }
//
//        icm.addOnClickListener(new OnClickListener() {
//            @Override
//            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
//                Layer layer = (Layer) ((MenuItem) v).getData();
//                if (layer == MapView.mapTileLoader.getCurrentOverlayLayer()) {
//                    // switch off Overlay
//                    TabMainView.mapView.SetCurrentOverlayLayer(null);
//                } else {
//                    TabMainView.mapView.SetCurrentOverlayLayer(layer);
//                }
//                // Refresh menu
//                icm.close();
//                showMapOverlayMenu();
//                return true;
//            }
//        });

        icm.show();
    }

    private void showMapViewLayerMenu() {
        Menu icm = new Menu("MapViewShowLayerContextMenu");
        icm.addCheckableItem(MenuID.MI_HIDE_FINDS, "HideFinds", Settings_Map.MapHideMyFinds.getValue());
        icm.addCheckableItem(MenuID.MI_MAP_SHOW_COMPASS, "MapShowCompass", Settings_Map.MapShowCompass.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_ALL_WAYPOINTS, "ShowAllWaypoints", Settings_Map.ShowAllWaypoints.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_RATINGS, "ShowRatings", Settings_Map.MapShowRating.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_DT, "ShowDT", Settings_Map.MapShowDT.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_TITLE, "ShowTitle", Settings_Map.MapShowTitles.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_DIRECT_LINE, "ShowDirectLine", Settings_Map.ShowDirektLine.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_ACCURACY_CIRCLE, "MenuTextShowAccuracyCircle", Settings_Map.ShowAccuracyCircle.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_CENTERCROSS, "ShowCenterCross", Settings_Map.ShowMapCenterCross.getValue());
        icm.setOnItemClickListener(onItemClickListener);
        icm.show();
    }

    private void showMapViewThemeMenu() {
        Menu icm = new Menu("MapViewThemeMenu");

        //add default themes
        int id = 0;
        for (VtmThemes vtmTheme : VtmThemes.values()) {
            icm.addCheckableItem(id++, vtmTheme.name(), EQUALS.is(CB.actThemeFile, vtmTheme), true);
        }

        final Array<NamedExternalRenderTheme> themes = new Array<>();

        // search themes on repository/maps/themes (recursive)
        FileHandle folder = Gdx.files.absolute(CB.WorkPath + "/repository/maps/themes");
        searchThemes(folder, themes);

        // search themes on User map folder
        folder = Gdx.files.absolute(Config.MapPackFolder.getValue());
        searchThemes(folder, themes);

        for (NamedExternalRenderTheme themFile : themes) {
            icm.addCheckableItem(id++, themFile.name, EQUALS.is(CB.actThemeFile, themFile), true);
        }

        icm.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                if (item.getMenuItemId() < VtmThemes.values().length) {
                    VtmThemes theme = VtmThemes.values()[item.getMenuItemId()];
                    map.setTheme(theme);
                } else {
                    NamedExternalRenderTheme theme = themes.get(item.getListIndex() - VtmThemes.values().length);
                    map.setTheme(theme);
                }
                return true;
            }
        });
        icm.show();
    }

    private void showMapViewThemeStyleMenu() {
        OptionMenu icm = new OptionMenu("MapViewThemeStyleMenu");

        ThemeMenuCallback callback = (ThemeMenuCallback) CB.actThemeFile.getMenuCallback();
        Array<XmlRenderThemeStyleLayer> overlays = callback.getOverlays();
        int id = 0;
        String lang = "de";
        for (XmlRenderThemeStyleLayer overlay : overlays) {

            if (overlay.getCategories().size() > 1) {
                MenuItem menuItem = icm.addItem(id, overlay.getTitle(lang), true);
                OptionMenu moreMenu = new OptionMenu(overlay.getTitle(lang));
                for (String cat : overlay.getCategories()) {
                    ObjectMap<String, Boolean> allCategories = callback.getAllCategories();
                    moreMenu.addCheckableItem(id++, cat, allCategories.get(cat), true).setData(cat);
                }
                moreMenu.setOnItemClickListener(styleItemClickListener);
                menuItem.setMoreMenu(moreMenu);
            } else {
                //get cat name
                String cat = "";
                for (String str : overlay.getCategories()) {
                    cat = str;
                    break;
                }
                icm.addCheckableItem(id++, overlay.getTitle(lang), callback.getAllCategories().get(cat), true).setData(cat);
            }
        }
        icm.setOnItemClickListener(styleItemClickListener);
        icm.setWindowCloseListener(closeStyleMenuListener);
        icm.show();
    }

    private final Window.WindowCloseListener closeStyleMenuListener = new Window.WindowCloseListener() {
        @Override
        public void windowClosed() {
            ThemeMenuCallback callback = (ThemeMenuCallback) CB.actThemeFile.getMenuCallback();
            if (callback != null)
                callback.storeChanges();
            CB.actThemeFile.setMenuCallback(callback);
            CB.loadThemeFile(CB.actThemeFile);
            map.setTheme(CB.actThemeFile);
        }
    };


    private final OnItemClickListener styleItemClickListener = new OnItemClickListener() {
        @Override
        public boolean onItemClick(MenuItem item) {
            String cat = (String) item.getData();
            if (cat.isEmpty()) return true;
            ThemeMenuCallback callback = (ThemeMenuCallback) CB.actThemeFile.getMenuCallback();
            ObjectMap<String, Boolean> allCategories = callback.getAllCategories();
            boolean newValue = !allCategories.get(cat);
            item.setChecked(newValue);
            allCategories.put(cat, newValue);
            return true;
        }
    };


    private void searchThemes(FileHandle folder, Array<NamedExternalRenderTheme> themes) {
        if (!folder.isDirectory()) return;
        for (FileHandle handle : folder.list()) {
            if (handle.isDirectory()) {
                searchThemes(handle, themes);
            } else if (handle.extension().equals("xml")) {
                try {
                    NamedExternalRenderTheme extTheme = new NamedExternalRenderTheme(handle.nameWithoutExtension(),
                            handle.file().getAbsolutePath());
                    themes.add(extTheme);
                } catch (IRenderTheme.ThemeException e) {
                    // is invalid Theme File
                    log.warn("Found invalid Theme file: {}", handle.file().getAbsolutePath());
                }
            }
        }
    }

    private final OnItemClickListener onItemClickListener = new OnItemClickListener() {


        @Override
        public boolean onItemClick(MenuItem item) {
            switch (item.getMenuItemId()) {
                case MenuID.MI_LAYER:
                    showMapLayerMenu();
                    return true;
                case MenuID.MI_MAPVIEW_OVERLAY_VIEW:
                    showMapOverlayMenu();
                    return true;
                case MenuID.MI_MAPVIEW_VIEW:
                    showMapViewLayerMenu();
                    return true;
                case MenuID.MI_MAPVIEW_THEME:
                    showMapViewThemeMenu();
                    return true;
                case MenuID.MI_MAPVIEW_THEME_STYLE:
                    showMapViewThemeStyleMenu();
                    return true;
                case MenuID.MI_SHOW_ALL_WAYPOINTS:
                    toggleSetting(Settings_Map.ShowAllWaypoints);
                    return true;
                case MenuID.MI_HIDE_FINDS:
                    toggleSettingWithReload(Settings_Map.MapHideMyFinds);
                    return true;
                case MenuID.MI_SHOW_RATINGS:
                    toggleSetting(Settings_Map.MapShowRating);
                    return true;
                case MenuID.MI_SHOW_DT:
                    toggleSetting(Settings_Map.MapShowDT);
                    return true;
                case MenuID.MI_SHOW_TITLE:
                    toggleSetting(Settings_Map.MapShowTitles);
                    return true;
                case MenuID.MI_SHOW_DIRECT_LINE:
                    toggleSetting(Settings_Map.ShowDirektLine);
                    return true;
                case MenuID.MI_SHOW_ACCURACY_CIRCLE:
                    toggleSetting(Settings_Map.ShowAccuracyCircle);
                    return true;
                case MenuID.MI_SHOW_CENTERCROSS:
                    toggleSetting(Settings_Map.ShowMapCenterCross);
                    return true;
                case MenuID.MI_MAP_SHOW_COMPASS:
                    toggleSetting(Settings_Map.MapShowCompass);
                    return true;
                case MenuID.MI_CENTER_WP:
                    createWaypointAtCenter();
                    return true;
                case MenuID.MI_TREC_REC:
                    showMenuTrackRecording();
                    return true;
                case MenuID.MI_MAP_DOWNOAD:
                    //TODO MapDownload.INSTANCE.show();
                    return true;
                default:
                    return false;
            }
        }
    };


    private void createWaypointAtCenter() {
        //show EditWaypoint dialog;
        new Action_Add_WP().execute();
    }

    private static final int START = 1;
    private static final int PAUSE = 2;
    private static final int STOP = 3;

    private void showMenuTrackRecording() {
        MenuItem mi;
        Menu cm2 = new Menu("TrackRecordContextMenu");
        cm2.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public boolean onItemClick(MenuItem item) {
                switch (item.getMenuItemId()) {
                    case START:
                        TrackRecorder.INSTANCE.startRecording();
                        return true;
                    case PAUSE:
                        TrackRecorder.INSTANCE.pauseRecording();
                        return true;
                    case STOP:
                        TrackRecorder.INSTANCE.stopRecording();
                        return true;
                }
                return false;
            }
        });
        mi = cm2.addItem(START, "start");
        mi.setEnabled(!TrackRecorder.INSTANCE.recording);

        if (TrackRecorder.INSTANCE.pauseRecording)
            mi = cm2.addItem(PAUSE, "continue");
        else
            mi = cm2.addItem(PAUSE, "pause");

        mi.setEnabled(TrackRecorder.INSTANCE.recording);

        mi = cm2.addItem(STOP, "stop");
        mi.setEnabled(TrackRecorder.INSTANCE.recording | TrackRecorder.INSTANCE.pauseRecording);

        cm2.show();
    }

    private void toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        setNewSettings();
    }

    private void toggleSettingWithReload(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        setNewSettings();
    }

    private MapWayPointItem infoItem = null;
    private MapBubble infoBubble;
    float mapHalfWith;
    float mapHalfHeight;

    public void clickOnItem(final MapWayPointItem item) {

        if (infoBubble != null) {
            MapView.this.removeActor(infoBubble);
        }

        VisTable table = new VisTable();

        infoBubble = new MapBubble(item.dataObject);
        table.add(infoBubble).expand().fill();
        infoBubble.layout();
        table.layout();


        MapView.this.addActor(infoBubble);
        infoItem = item;
        setInfoBubblePos();
        CB.requestRendering();

        this.addListener(bubbleClickListener);


    }

    private ClickListener bubbleClickListener = new ClickListener() {

        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            super.touchDown(event, x, y, pointer, button);
            if (infoBubble != null) {
                //click detection
                if (infoBubble.getX() <= x && infoBubble.getX() + infoBubble.getWidth() >= x &&
                        infoBubble.getY() <= y && infoBubble.getY() + infoBubble.getHeight() >= y) {
                    return true;
                }
            }
            return false;
        }

        public void clicked(InputEvent event, float x, float y) {
            if (infoBubble != null) {

                //click detection
                if (infoBubble.getX() <= x && infoBubble.getX() + infoBubble.getWidth() >= x &&
                        infoBubble.getY() <= y && infoBubble.getY() + infoBubble.getHeight() >= y) {
                    // select this cache/waypoint and close bubble
                    Object obj = infoItem.dataObject;
                    if (obj instanceof AbstractCache) {
                        AbstractCache cache = (AbstractCache) obj;
                        EventHandler.fire(new SelectedCacheChangedEvent(cache));
                    } else if (obj instanceof AbstractWaypoint) {
                        AbstractWaypoint waypoint = (AbstractWaypoint) obj;
                        EventHandler.fire(new SelectedWayPointChangedEvent(waypoint));
                    }

                    closeInfoBubble();
                }
            }

        }
    };

    public void closeInfoBubble() {
        CB.postOnGlThread(new NamedRunnable("remove info bubble") {
            @Override
            public void run() {
                MapView.this.removeActor(infoBubble);
                MapView.this.removeListener(bubbleClickListener);
                infoBubble = null;
            }
        });
    }


    private void setInfoBubblePos() {
        if (this.map != null && infoBubble != null && screenPoint != null) {
            this.map.viewport().toScreenPoint(infoBubble.getCoordX(), infoBubble.getCoordY(), screenPoint);
            infoBubble.setPosition((float) (screenPoint.x + this.getWidth() / 2) + infoBubble.getOffsetX(),
                    (float) (this.getHeight() - (screenPoint.y + this.getHeight() / 2)) + infoBubble.getOffsetY());
        }
    }

    public boolean infoBubbleVisible() {
        return infoBubble != null;
    }

}
