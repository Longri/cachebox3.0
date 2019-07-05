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
import com.badlogic.gdx.utils.XmlStreamParser;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedWayPointChangedEvent;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.actions.show_activities.Action_Add_WP;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import de.longri.cachebox3.gui.map.NamedExternalRenderTheme;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.gui.map.baseMap.MapsforgeSingleMap;
import de.longri.cachebox3.gui.map.baseMap.OSciMap;
import de.longri.cachebox3.gui.map.layer.CenterCrossLayer;
import de.longri.cachebox3.gui.map.layer.DirectLineLayer;
import de.longri.cachebox3.gui.map.layer.ThemeMenu;
import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OptionMenu;
import de.longri.cachebox3.gui.skin.styles.MapArrowStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.gui.widgets.MapBubble;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.locator.track.TrackRecorder;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.settings.types.SettingBool;
import de.longri.cachebox3.translation.Language;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnZip;
import de.longri.cachebox3.utils.http.Download;
import de.longri.cachebox3.utils.http.Webb;
import de.longri.serializable.BitStore;
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
import org.oscim.layers.LocationTextureLayer;
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
import org.oscim.utils.FastMath;
import org.oscim.utils.TextureAtlasUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
    static private MapState actMapState = new MapState();
    private final Event selfEvent = new Event();
    /*
    private final OnItemClickListener styleItemClickListener = item -> {
        String cat = (String) item.getData();
        if (cat.isEmpty()) return true;
        ThemeMenu callback = (ThemeMenu) CB.actThemeFile.getMenuCallback();
        ObjectMap<String, Boolean> allCategories = callback.getAllCategories();
        boolean newValue = !allCategories.get(cat);
        item.setChecked(newValue);
        allCategories.put(cat, newValue);
        return true;
    };
     */
    float mapHalfWith;
    float mapHalfHeight;
    private boolean menuInShow;
    private InputMultiplexer mapInputHandler;
    private CacheboxMapAdapter cacheboxMapAdapter;
    private final Window.WindowCloseListener closeStyleMenuListener = new Window.WindowCloseListener() {
        @Override
        public void windowClosed() {
            // todo save selection to db and prepare CB.actThemeFile
            cacheboxMapAdapter.setTheme(CB.getCurrentTheme());
        }
    };
    private MapScaleBarLayer mapScaleBarLayer;
    private MapStateButton mapStateButton;
    private ZoomButton zoomButton;
    private MapInfoPanel infoPanel;
    private WaypointLayer wayPointLayer;
    private DirectLineLayer directLineLayer;
    private CenterCrossLayer ccl;
    private final IChanged showMapCenterCrossChangedListener = new IChanged() {
        @Override
        public void isChanged() {
            if (cacheboxMapAdapter == null) return;
            log.debug("change center cross visibility to {}", Settings_Map.ShowMapCenterCross.getValue() ? "visible" : "invisible");
            setCenterCrossLayerEnabled(Settings_Map.ShowMapCenterCross.getValue());
            cacheboxMapAdapter.updateMap(true);
        }
    };
    private LocationTextureLayer myLocationLayer;
    private MapViewPositionChangedHandler positionChangedHandler;
    private Point screenPoint = new Point();
    private String themesPath;
    private FZKThemesInfo fzkThemesInfo;
    private Array<FZKThemesInfo> fzkThemesInfoList = new Array<>();
    private MapWayPointItem infoItem = null;
    private MapBubble infoBubble;
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
    private CB.ThemeIsFor whichCase;

    public MapView(BitStore reader) {
        super(reader);
        whichCase = CB.ThemeIsFor.day;
        create();
    }

    public MapView() {
        super("MapView");
        whichCase = CB.ThemeIsFor.day;
        create();
    }

    public static boolean isCarMode() {
        return actMapState.getMapMode() == MapMode.CAR;
    }

    public static Coordinate getLastCenterPos() {
        return new Coordinate(lastCenterPosLat, lastCenterPosLon);
    }

    public static LinkedHashMap<Object, TextureRegion> createTextureAtlasRegions() {
        // create TextureRegions from all Bitmap symbols
        LinkedHashMap<Object, TextureRegion> textureRegionMap = new LinkedHashMap<>();
        ObjectMap<String, MapWayPointItemStyle> list = VisUI.getSkin().getAll(MapWayPointItemStyle.class);
        if (list == null) return null;
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

    public MapState getActMapState() {
        return actMapState;
    }

    @Override
    protected void create() {
        this.setTouchable(Touchable.disabled);

        //TODO use better packer like rectPack2D
        if (CB.textureRegionMap == null) CB.textureRegionMap = createTextureAtlasRegions();

        mapStateButton = new MapStateButton(new MapStateButton.StateChangedListener() {

            @Override
            public void stateChanged(MapMode mapMode, MapMode lastMapMode, Event event) {

                MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
                actMapState.setPosition(new LatLong(mapPosition.getLatitude(), mapPosition.getLongitude()));
                actMapState.setMapMode(mapMode);
                actMapState.setOrientation(mapPosition.bearing);
                actMapState.setTilt(mapPosition.tilt);
                actMapState.setMapOrientationMode(infoPanel.getOrientationState());

                log.debug("Map state changed to:" + actMapState);

                if (mapMode == MapMode.CAR) {
                    storeMapstate(mapMode, lastMapMode);
                    log.debug("Activate Carmode with last mapstate:{}", CB.lastMapStateBeforeCar);
                    float bearing = -EventHandler.getHeading();
                    positionChangedHandler.setBearing(bearing);
                    setBuildingLayerEnabled(false);
                    setCenterCrossLayerEnabled(false);
                } else if (lastMapMode == MapMode.CAR) {
                    log.debug("Disable Carmode! Activate last Mode:{}", CB.lastMapState);
                    restoreMapstate(CB.lastMapStateBeforeCar);
                } else if (mapMode == MapMode.GPS) {
                    log.debug("Activate GPS Mode");
                    storeMapstate(mapMode, null);
                    final Coordinate myPos = EventHandler.getMyPosition();
                    if (myPos != null) {
                        positionChangedHandler.position(
                                MercatorProjection.longitudeToX(myPos.getLongitude()),
                                MercatorProjection.latitudeToY(myPos.getLatitude())
                        );
                    }
                    setCenterCrossLayerEnabled(false);
                } else if (mapMode == MapMode.WP) {
                    storeMapstate(mapMode, null);
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
                                MercatorProjection.longitudeToX(wpCoord.getLongitude()),
                                MercatorProjection.latitudeToY(wpCoord.getLatitude())
                        );
                        setCenterCrossLayerEnabled(false);
                    }
                } else if (mapMode == MapMode.LOCK) {
                    storeMapstate(mapMode, null);
                    setCenterCrossLayerEnabled(false);
                } else if (mapMode == MapMode.FREE) {
                    storeMapstate(mapMode, null);
                    setCenterCrossLayerEnabled(true);
                }
            }
        }) {
            @Override
            public Menu getMenu() {
                menuInShow = true;
                return super.getMenu();
            }
        };
        infoPanel = new MapInfoPanel();
        infoPanel.setBounds(10, 100, 200, 100);
        this.addActor(infoPanel);

        createCacheboxMapAdapter();

        this.addActor(mapStateButton);
        this.setTouchable(Touchable.enabled);

        this.zoomButton = new ZoomButton(new ZoomButton.ValueChangeListener() {
            @Override
            public void valueChanged(int changeValue) {
                MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
                double value = mapPosition.getScale();
                if (changeValue > 0)
                    value = value * 2;
                else
                    value = value * 0.5;

                positionChangedHandler.scale(value);
                CB.lastMapState.setZoom(FastMath.log2((int) value));
            }
        });
        this.zoomButton.pack();
        this.addActor(zoomButton);
    }

    private void storeMapstate(MapMode mapMode, MapMode beforeCar) {
        MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
        CB.lastMapState.setPosition(new LatLong(mapPosition.getLatitude(), mapPosition.getLongitude()));
        CB.lastMapState.setMapMode(mapMode);
        CB.lastMapState.setOrientation(mapPosition.bearing);
        CB.lastMapState.setTilt(mapPosition.tilt);
        CB.lastMapState.setMapOrientationMode(infoPanel.getOrientationState());

        if (beforeCar != null) {
            CB.lastMapStateBeforeCar.set(CB.lastMapState);
            CB.lastMapStateBeforeCar.setMapMode(beforeCar);
        }


        log.debug("store MapState: " + CB.lastMapState);

        // write to config
        Config.lastMapState.setValue(CB.lastMapState.serialize());
        Config.lastMapStateBeforeCar.setValue(CB.lastMapStateBeforeCar.serialize());
        Config.AcceptChanges();
    }

    private void restoreMapstate(MapState mapState) {

        log.debug("restore MapState: " + mapState);

        MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
        mapPosition.scale = 1 << mapState.getZoom();
        mapPosition.bearing = mapState.getOrientation();
        mapPosition.tilt = mapState.getTilt();
        if (mapState.getFreePosition() != null)
            mapPosition.setPosition(mapState.getFreePosition().getLatitude(), mapState.getFreePosition().getLongitude());
        mapStateButton.setMapMode(mapState.getMapMode(), true, selfEvent);
        infoPanel.setMapOrientationMode(mapState.getMapOrientationMode());
        cacheboxMapAdapter.setMapPosition(mapPosition);
        cacheboxMapAdapter.updateMap(true);
    }

    private void setCenterCrossLayerEnabled(boolean enabled) {
        enabled &= Settings_Map.ShowMapCenterCross.getValue();
        ccl.setEnabled(enabled);
    }

    private void setBuildingLayerEnabled(boolean enabled) {
        Layers layers = cacheboxMapAdapter.layers();
        for (Layer layer : layers) {
            if (layer instanceof CacheboxMapAdapter.BuildingLabelLayer) {
                log.debug("{} BuildingLayer", enabled ? "Enable" : "Disable");
                ((CacheboxMapAdapter.BuildingLabelLayer) layer).buildingLayer.setEnabled(enabled);
            }
        }
    }

    private void createCacheboxMapAdapter() {

        if (CB.isMocked()) return;

        log.debug("Tile.SIZE:" + Integer.toString(Tile.SIZE));
        log.debug("Canvas.dpi:" + Float.toString(CanvasAdapter.dpi));


        CacheboxMain.drawMap.set(true);

        cacheboxMapAdapter = new CacheboxMapAdapter() {

            @Override
            public void beginFrame() {
                if (!CacheboxMain.drawMap.get()) return;
                super.beginFrame();
                if (positionChangedHandler != null) positionChangedHandler.update(Gdx.graphics.getDeltaTime());
            }

            @Override
            public void onMapEvent(Event e, final MapPosition mapPosition) {
                if (!CacheboxMain.drawMap.get()) return;
                super.onMapEvent(e, mapPosition);
                if (e == Map.MOVE_EVENT) {
//                    log.debug("Map.MOVE_EVENT");
                    if (CB.lastMapState.getMapMode() != MapMode.FREE)
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
        ((CacheboxMain) Gdx.app.getApplicationListener()).mMapRenderer = new MapRenderer(cacheboxMapAdapter);
        ((CacheboxMain) Gdx.app.getApplicationListener()).mMapRenderer.onSurfaceCreated();

        initLayers(false);

        //add position changed handler
        positionChangedHandler = new MapViewPositionChangedHandler(cacheboxMapAdapter, directLineLayer, myLocationLayer, infoPanel);

        return;
    }

    @Override
    public void onShow() {
        addInputListener();
        sizeChanged();

        //set initial position/direction without animation
        Coordinate myPos = EventHandler.getMyPosition();

        //use saved pos
        if (myPos == null) {
            LatLong latLon = CB.lastMapState.getFreePosition();
            if (latLon != null) {
                myPos = new Coordinate(latLon);
            }
        }
        if (myPos != null) {
            infoPanel.setNewValues(myPos, EventHandler.getHeading());
            positionChangedHandler.setPositionWithoutAnimation(myPos.getLatitude(), myPos.getLongitude());
        }
        if (!menuInShow) {
            if (CB.lastMapState.isEmpty()) {
                //load from config
                CB.lastMapState.deserialize(Config.lastMapState.getValue());
                CB.lastMapStateBeforeCar.deserialize(Config.lastMapStateBeforeCar.getValue());
            }
            restoreMapstate(CB.lastMapState);
        } else
            menuInShow = false;
    }

    @Override
    public void onHide() {
        removeInputListener();
        if (!menuInShow)
            storeMapstate(mapStateButton.getSelected(), null);
    }

    @Override
    public void dispose() {
        log.debug("Dispose MapView");
        CacheboxMain.drawMap.set(false);

        positionChangedHandler.dispose();
        positionChangedHandler = null;

        Layers layers = cacheboxMapAdapter.layers();
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
        cacheboxMapAdapter.clearMap();
        cacheboxMapAdapter.destroy();
        cacheboxMapAdapter = null;
        CB.postOnGlThread(new NamedRunnable("MapView:dispose texture items") {
            @Override
            public void run() {
                TextureBucket.pool.clear();
                TextItem.pool.clear();
                TextureItem.disposeTextures();
            }
        });

        ((CacheboxMain) Gdx.app.getApplicationListener()).mMapRenderer = null;

        //dispose actors
        mapStateButton.dispose();
        infoPanel.dispose();
        Settings_Map.ShowMapCenterCross.removeChangedEventListener(showMapCenterCrossChangedListener);
    }

    @Override
    public void sizeChanged() {
        if (cacheboxMapAdapter == null) return;
        cacheboxMapAdapter.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
        cacheboxMapAdapter.viewport().setViewSize((int) this.getWidth(), (int) this.getHeight());
        ((CacheboxMain) Gdx.app.getApplicationListener()).setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

        mapHalfWith = cacheboxMapAdapter.getWidth() / 2;
        mapHalfHeight = cacheboxMapAdapter.getHeight() / 2;

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
        ((CacheboxMain) Gdx.app.getApplicationListener()).setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
    }

    private void initLayers(boolean tileGrid) {

        log.debug("Init layer");

        // load last saved BaseMap
        String baseMapName = Settings_Map.CurrentMapLayer.getValue()[0];
        BaseMapManager.INSTANCE.refreshMaps();
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

        cacheboxMapAdapter.setNewBaseMap(baseMap);

        DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(cacheboxMapAdapter);
        mapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
        mapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
        mapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
        mapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

        directLineLayer = new DirectLineLayer(cacheboxMapAdapter);
        mapScaleBarLayer = new MapScaleBarLayer(cacheboxMapAdapter, mapScaleBar);
        wayPointLayer = new WaypointLayer(this, cacheboxMapAdapter, CB.textureRegionMap);
        MapArrowStyle style = VisUI.getSkin().get("myLocation", MapArrowStyle.class);
        String bmpName = ((GetName) style.myLocation).getName();
        TextureRegion textureRegion = CB.textureRegionMap.get(bmpName);
        myLocationLayer = new LocationTextureLayer(cacheboxMapAdapter, textureRegion);
        myLocationLayer.locationRenderer.setAccuracyColor(Color.BLUE);
        myLocationLayer.locationRenderer.setIndicatorColor(Color.RED);
        myLocationLayer.locationRenderer.setBillboard(false);

        boolean showDirectLine = Settings_Map.ShowDirektLine.getValue();
        log.debug("Initial direct line layer and {}", showDirectLine ? "enable" : "disable");
        directLineLayer.setEnabled(showDirectLine);
        GroupLayer layerGroup = new GroupLayer(cacheboxMapAdapter);

        ccl = new CenterCrossLayer(cacheboxMapAdapter);


        if (tileGrid)
            layerGroup.layers.add(new TileGridLayer(cacheboxMapAdapter));

        layerGroup.layers.add(wayPointLayer);
        layerGroup.layers.add(directLineLayer);
        layerGroup.layers.add(myLocationLayer);
        layerGroup.layers.add(mapScaleBarLayer);
        layerGroup.layers.add(ccl);

        Settings_Map.ShowDirektLine.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                if (cacheboxMapAdapter == null) return;
                log.debug("change direct line visibility to {}", Settings_Map.ShowDirektLine.getValue() ? "visible" : "invisible");
                directLineLayer.setEnabled(Settings_Map.ShowDirektLine.getValue());
                cacheboxMapAdapter.updateMap(true);
            }
        });


        boolean showCenterCross = Settings_Map.ShowMapCenterCross.getValue();
        log.debug("Initial center cross layer and {}", showCenterCross ? "enable" : "disable");

        ccl.setEnabled(showCenterCross);
        Settings_Map.ShowMapCenterCross.addChangedEventListener(showMapCenterCrossChangedListener);
        cacheboxMapAdapter.layers().add(layerGroup);
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
        GestureDetector gestureDetector = new GestureDetector(new GestureHandlerImpl(cacheboxMapAdapter));
        MapMotionHandler motionHandler = new MapMotionHandler(cacheboxMapAdapter, mapStateButton);
        MapInputHandler inputHandler = new MapInputHandler(cacheboxMapAdapter, mapStateButton);
        mapInputHandler = new InputMultiplexer();
        mapInputHandler.addProcessor(gestureDetector);
        mapInputHandler.addProcessor(motionHandler);
        mapInputHandler.addProcessor(inputHandler);
    }

    private void addInputListener() {
        if (mapInputHandler == null) createMapInputHandler();
        CB.stageManager.addMapMultiplexer(mapInputHandler);
    }

    private void removeInputListener() {
        CB.stageManager.removeMapMultiplexer(mapInputHandler);
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

    public Coordinate getMapCenter() {
        MapPosition mp = this.cacheboxMapAdapter.getMapPosition();
        return new Coordinate(mp.getLatitude(), mp.getLongitude());
    }

    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("MapViewContextMenuTitle");
        icm.addMenuItem("Layer", CB.getSkin().getMenuIcon.mapLayer, () -> showMapViewLayerMenu());
        if (cacheboxMapAdapter.getBaseMap() instanceof MapsforgeSingleMap) {
            icm.addMenuItem("Renderthemes", CB.getSkin().getMenuIcon.theme, () -> showMapViewThemeMenu());
            icm.addMenuItem("Styles", CB.getSkin().getMenuIcon.themeStyle, () -> showMapViewThemeStyleMenu());
        }
        icm.addMenuItem("overlays", CB.getSkin().getMenuIcon.todo, () -> showMapViewOverlaysMenu()); // todo icon
        icm.addMenuItem("view", CB.getSkin().getMenuIcon.viewSettings, () -> showMapViewElementsMenu());
        // todo needed? nach Kompass ausrichten | setAlignToCompass
        icm.addMenuItem("CenterWP", CB.getSkin().getMenuIcon.addWp, () -> createWaypointAtCenter());
        icm.addMenuItem("RecTrack", CB.getSkin().getMenuIcon.todo, () -> showTrackRecordMenu()); // todo icon
        return icm;
    }

    private void showMapViewLayerMenu() {
        Menu icm = new Menu("MapViewLayerMenuTitle");

        BaseMapManager.INSTANCE.refreshMaps();


        int menuID = 0;
        for (int i = 0, n = BaseMapManager.INSTANCE.size; i < n; i++) {

            AbstractManagedMapLayer baseMap = BaseMapManager.INSTANCE.get(i);

            if (!baseMap.isOverlay) {

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

                boolean isChecked = false;
                String[] currentLayer = Settings_Map.CurrentMapLayer.getValue();
                for (int j = 0, m = currentLayer.length; j < m; j++) {
                    String str = currentLayer[j];
                    if (str.equals(baseMap.name)) {
                        isChecked = true;
                        break;
                    }
                }

                MenuItem mi = icm.addCheckableMenuItem("", baseMap.name, icon, isChecked, () -> {
                    cacheboxMapAdapter.setNewBaseMap(baseMap);
                });
            }
        }

        /*
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

                cacheboxMapAdapter.setNewBaseMap(baseMap);
                return true;
            }
        });

         */

        icm.show();
    }

    //todo ISSUE (#110 add MapView Overlays)
    private void showMapViewOverlaysMenu() {
        final Menu icm = new Menu("MapViewOverlayMenuTitle");

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
//                showMapViewOverlaysMenu();
//                return true;
//            }
//        });

        icm.show();
    }

    private void showMapViewElementsMenu() {
        Menu icm = new Menu("MapViewElementsMenuTitle");
        icm.addCheckableMenuItem("HideFinds", Settings_Map.MapHideMyFinds.getValue(), () -> toggleSettingWithReload(Settings_Map.MapHideMyFinds));
        // icm.addCheckableMenuItem("MapShowCompass", Settings_Map.MapShowCompass.getValue(),()-> toggleSetting(Settings_Map.MapShowCompass));
        // todo icm.addCheckableMenuItem("MapShowInfoBar",Settings_Map.ShowInfo .....)
        icm.addCheckableMenuItem("ShowAllWaypoints", Settings_Map.ShowAllWaypoints.getValue(), () -> toggleSetting(Settings_Map.ShowAllWaypoints));
        icm.addCheckableMenuItem("ShowRatings", Settings_Map.MapShowRating.getValue(), () -> toggleSetting(Settings_Map.MapShowRating));
        icm.addCheckableMenuItem("ShowDT", Settings_Map.MapShowDT.getValue(), () -> toggleSetting(Settings_Map.MapShowDT));
        icm.addCheckableMenuItem("ShowTitle", Settings_Map.MapShowTitles.getValue(), () -> toggleSetting(Settings_Map.MapShowTitles));
        icm.addCheckableMenuItem("ShowDirectLine", Settings_Map.ShowDirektLine.getValue(), () -> toggleSetting(Settings_Map.ShowDirektLine));
        icm.addCheckableMenuItem("MenuTextShowAccuracyCircle", Settings_Map.ShowAccuracyCircle.getValue(), () -> toggleSetting(Settings_Map.ShowAccuracyCircle));
        icm.addCheckableMenuItem("ShowCenterCross", Settings_Map.ShowMapCenterCross.getValue(), () -> toggleSetting(Settings_Map.ShowMapCenterCross));
        icm.show();
    }

    private void showMapViewThemeMenu() {
        Menu mapViewThemeMenu = new Menu("MapViewThemeMenuTitle");
        //add default themes
        for (VtmThemes vtmTheme : VtmThemes.values()) {
            mapViewThemeMenu.addCheckableMenuItem("", vtmTheme.name(), null, vtmTheme.equals(CB.getCurrentTheme()),
                    () -> {
                        CB.setCurrentTheme(whichCase);
                        cacheboxMapAdapter.setTheme(CB.getCurrentTheme());
                    });
        }

        final Array<NamedExternalRenderTheme> themes = new Array<>();
        themesPath = "";

        // search themes on repository/maps/themes (recursive)
        FileHandle folder = Gdx.files.absolute(CB.WorkPath + "/repository/maps/themes");
        searchThemes(folder, themes);
        if (folder.file().canWrite())
            themesPath = folder.path();

        // search themes on User map folder
        folder = Gdx.files.absolute(Config.MapPackFolder.getValue());
        searchThemes(folder, themes);
        if (folder.file().canWrite())
            themesPath = folder.path();

        for (NamedExternalRenderTheme themeFile : themes) {
            mapViewThemeMenu.addCheckableMenuItem("", themeFile.name, null, CB.getConfigsThemePath(whichCase).equals(themeFile.path),
                    () -> {
                        CB.setConfigsThemePath(whichCase, themeFile.path);
                        CB.setCurrentTheme(whichCase);
                        cacheboxMapAdapter.setTheme(CB.getCurrentTheme());
                        // todo just save to config or load with defaults?
                    });
        }

        final String target = themesPath + "/Elevate4.zip";
        if (themesPath.length() > 0) {
            mapViewThemeMenu.addDivider(-1);
            mapViewThemeMenu.addMenuItem("Download", "\n OpenAndroMaps",
                    CB.getSkin().getMenuIcon.baseMapMapsforge,
                    new ClickListener() {
                        public void clicked(InputEvent event, float x, float y) {
                            if (mapViewThemeMenu.mustHandle(event)) {
                                MenuItem mi = (MenuItem) event.getListenerActor();
                                mi.setEnabled(false);
                                // todo doesn't show disabled (enough animation?)
                                Download.Download("http://download.openandromaps.org/themes/Elevate4.zip", target);
                                try {
                                    new UnZip().extractFolder(target);
                                } catch (Exception ex) {
                                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                                }
                                Gdx.files.absolute(target).delete();
                            }
                        }
                    });
            mapViewThemeMenu.addMenuItem("Download", "\n Freizeitkarte",
                    CB.getSkin().getMenuIcon.baseMapFreizeitkarte, () -> showFZKDownloadMenu());
        }

        mapViewThemeMenu.show();
    }

    private void showFZKDownloadMenu() {
        Menu mapViewFZKDownloadMenu = new Menu("Download");

        if (fzkThemesInfoList.size == 0) {
            InputStream repository_freizeitkarte_android = null;
            fzkThemesInfo = new FZKThemesInfo();

            repository_freizeitkarte_android = Webb.create()
                    .get("http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml")
                    .readTimeout(Config.socket_timeout.getValue())
                    .ensureSuccess()
                    .asStream()
                    .getBody();

            fzkThemesInfoList = getMapInfoList(repository_freizeitkarte_android);
        }

        for (FZKThemesInfo fzkThemesInfo : fzkThemesInfoList) {
            // todo change to explicit clicklistener, if animation works
            mapViewFZKDownloadMenu.addMenuItem("Download", "\n" + fzkThemesInfo.Description, CB.getSkin().getMenuIcon.baseMapFreizeitkarte, () -> {
                String zipFile = fzkThemesInfo.Url.substring(fzkThemesInfo.Url.lastIndexOf("/") + 1);
                String target = themesPath + "/" + zipFile;

                Download.Download(fzkThemesInfo.Url, target);
                try {
                    new UnZip().extractFolder(target, true);
                } catch (Exception ex) {
                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                }
                Gdx.files.absolute(target).delete();
            });
        }

        mapViewFZKDownloadMenu.show();
    }

    public static Array<FZKThemesInfo> getMapInfoList(InputStream stream) {
        final FZKThemesInfo[] info = {new FZKThemesInfo()};
        final Array<FZKThemesInfo> list = new Array<>();

        XmlStreamParser parser = new XmlStreamParser();
        parser.registerDataHandler("/Freizeitkarte/Theme/Name", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                info[0].Name = new String(data, offset, length).trim();
            }
        });
        if (Config.localisation.getEnumValue() == Language.de) {
            parser.registerDataHandler("/Freizeitkarte/Theme/DescriptionGerman", new XmlStreamParser.DataHandler() {
                @Override
                public void handleData(char[] data, int offset, int length) {
                    info[0].Description = new String(data, offset, length).trim();
                }
            });
        } else {
            parser.registerDataHandler("/Freizeitkarte/Theme/DescriptionEnglish", new XmlStreamParser.DataHandler() {
                @Override
                public void handleData(char[] data, int offset, int length) {
                    info[0].Description = new String(data, offset, length).trim();
                }
            });
        }
        parser.registerDataHandler("/Freizeitkarte/Theme/Url", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                info[0].Url = new String(data, offset, length).trim();
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Theme/Size", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                info[0].Size = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });
        // MD5 ignored


        parser.registerEndTagHandler("/Freizeitkarte/Theme", new XmlStreamParser.EndTagHandler() {
            @Override
            protected void handleEndTag() {
                list.add(info[0]);
                info[0] = new FZKThemesInfo();
            }
        });

        try {
            parser.parse(stream);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        info[0] = null;
        return list;
    }


    private void showMapViewThemeStyleMenu() {
        OptionMenu menuMapStyle = new OptionMenu("MapViewThemeStyleMenuTitle");
        ObjectMap<String, String> mapStyles;
        ThemeMenu themeMenu = new ThemeMenu(CB.getConfigsThemePath(whichCase));
        themeMenu.readTheme();
        mapStyles = themeMenu.getStyles();

        for (String mapStyle : mapStyles.keys()) {
            // check, if saved for selected layer
            boolean isSelected = false;
            menuMapStyle.addCheckableMenuItem("", mapStyle, null, isSelected, () -> {

            });
        }

        /*
        ThemeMenu callback = (ThemeMenu) CB.actThemeFile.getMenuCallback();
        Array<XmlRenderThemeStyleLayer> overlays = callback.getOverlays();
        int id = 0;
        String lang = "de";
        for (XmlRenderThemeStyleLayer overlay : overlays) {

            if (overlay.getCategories().size() > 1) {
                MenuItem menuItem = icm.addItem(id, overlay.getTitle(lang), true);
                OptionMenu moreMenu = new OptionMenu("-" + overlay.getTitle(lang));
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

         */
        menuMapStyle.show();
    }

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

    private void createWaypointAtCenter() {
        //show EditWaypoint dialog;
        new Action_Add_WP().execute();
    }

    //todo ISSUE (#112 Record Track)
    private void showTrackRecordMenu() {
        Menu cm2 = new Menu("TrackRecordMenuTitle");
        cm2.addMenuItem("start", null, () -> TrackRecorder.INSTANCE.startRecording()).setEnabled(!TrackRecorder.INSTANCE.recording);
        if (TrackRecorder.INSTANCE.pauseRecording)
            cm2.addMenuItem("continue", null, () -> TrackRecorder.INSTANCE.pauseRecording()).setEnabled(TrackRecorder.INSTANCE.recording);
        else
            cm2.addMenuItem("pause", null, () -> TrackRecorder.INSTANCE.pauseRecording()).setEnabled(TrackRecorder.INSTANCE.recording);
        cm2.addMenuItem("stop", null, () -> TrackRecorder.INSTANCE.stopRecording()).setEnabled(TrackRecorder.INSTANCE.recording | TrackRecorder.INSTANCE.pauseRecording);
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
        if (this.cacheboxMapAdapter != null && infoBubble != null && screenPoint != null) {
            this.cacheboxMapAdapter.viewport().toScreenPoint(infoBubble.getCoordX(), infoBubble.getCoordY(), screenPoint);
            infoBubble.setPosition((float) (screenPoint.x + this.getWidth() / 2) + infoBubble.getOffsetX(),
                    (float) (this.getHeight() - (screenPoint.y + this.getHeight() / 2)) + infoBubble.getOffsetY());
        }
    }

    public boolean infoBubbleVisible() {
        return infoBubble != null;
    }

    public void setTilt(double tilt) {
        MapPosition actPosition = cacheboxMapAdapter.getMapPosition();
        actPosition.tilt = (float) tilt;
        cacheboxMapAdapter.setMapPosition(actPosition);
    }

    public static class FZKThemesInfo {
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        public String MD5;
    }

}
