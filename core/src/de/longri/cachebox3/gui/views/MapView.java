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
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import de.longri.cachebox3.gui.map.NamedExternalRenderTheme;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.gui.map.baseMap.MapsforgeSingleMap;
import de.longri.cachebox3.gui.map.baseMap.OSciMap;
import de.longri.cachebox3.gui.map.layer.*;
import de.longri.cachebox3.gui.menu.quickBtns.Action_Add_WP;
import de.longri.cachebox3.gui.skin.styles.MapArrowStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;
import de.longri.cachebox3.gui.widgets.MapBubble;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
import de.longri.cachebox3.gui.widgets.menu.OptionMenu;
import de.longri.cachebox3.live.LiveButton;
import de.longri.cachebox3.live.LiveMapQue;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.track.Track;
import de.longri.cachebox3.locator.track.TrackList;
import de.longri.cachebox3.locator.track.TrackRecorder;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings_Const;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.settings.types.SettingBool;
import de.longri.cachebox3.translation.Language;
import de.longri.cachebox3.translation.Translation;
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
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;
import org.oscim.utils.FastMath;
import org.oscim.utils.TextureAtlasUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * The MapView has transparent background. The Map render runs at CacheboxMain.
 * This View has only the controls for the Map!
 * Created by Longri on 24.07.16.
 */
public class MapView extends AbstractView {
    private final static Logger log = LoggerFactory.getLogger(MapView.class);
    private static final MapState currentMapState = new MapState();
    private static double lastCenterPosLat, lastCenterPosLon;
    private final Event selfEvent = new Event();
    private final Point screenPoint = new Point();
    private final CB.ThemeUsage whichUsage;
    private static CacheboxMapAdapter staticCacheboxMapAdapter;
    private boolean menuInShow;
    private InputMultiplexer mapInputHandler;
    private CacheboxMapAdapter cacheboxMapAdapter;
    private MapScaleBarLayer mapScaleBarLayer;
    private MapStateButton mapStateButton;
    private ZoomButton zoomButton;
    private MapInfoPanel infoPanel;
    private WaypointLayer wayPointLayer;
    private DirectLineLayer directLineLayer;
    private CenterCrossLayer centerCrossLayer;
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
    private String themesPath;
    private Array<FZKThemesInfo> fzkThemesInfoList = new Array<>();
    private MapWayPointItem infoItem = null;
    private MapBubble infoBubble;
    private final ClickListener bubbleClickListener = new ClickListener() {
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            super.touchDown(event, x, y, pointer, button);
            if (infoBubble != null) {
                //click detection
                return infoBubble.getX() <= x && infoBubble.getX() + infoBubble.getWidth() >= x &&
                        infoBubble.getY() <= y && infoBubble.getY() + infoBubble.getHeight() >= y;
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

    public MapView(BitStore reader) {
        super(reader);
        whichUsage = CB.ThemeUsage.day;
        create();
    }

    public MapView() {
        super("MapView");
        whichUsage = CB.ThemeUsage.day;
        create();
    }

    public static boolean isCarMode() {
        return currentMapState.getMapMode() == MapMode.CAR;
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
            mapArrowStyle = VisUI.getSkin().get(MapArrowStyle.class);
        } catch (Exception e) {
            log.error("get MapArrowStyle", e);
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

    public static CacheboxMapAdapter getCacheboxMapAdapter() {
        return staticCacheboxMapAdapter;
    }

    public MapState getCurrentMapState() {
        return currentMapState;
    }

    @Override
    protected void create() {
        this.setTouchable(Touchable.disabled);

        //TODO use better packer like rectPack2D
        if (CB.textureRegionMap == null) CB.textureRegionMap = createTextureAtlasRegions();

        mapStateButton = new MapStateButton((mapMode, lastMapMode, event) -> {

            MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
            currentMapState.setPosition(new Coordinate(mapPosition.getLatitude(), mapPosition.getLongitude()));
            currentMapState.setMapMode(mapMode);
            currentMapState.setOrientation(mapPosition.bearing);
            currentMapState.setTilt(mapPosition.tilt);
            currentMapState.setMapOrientationMode(infoPanel.getOrientationState());

            log.debug("Map state changed to:" + currentMapState);

            if (mapMode == MapMode.CAR) {
                storeMapstate(mapMode, lastMapMode);
                log.debug("Activate Carmode with last mapstate:{}", CB.lastMapStateBeforeCar);
                float bearing = -EventHandler.getHeading();
                positionChangedHandler.setBearing(bearing);
                setBuildingLayerEnabled(false);
                setCenterCrossLayerEnabled(false);
            } else if (lastMapMode == MapMode.CAR) {
                log.debug("Disable Carmode! Activate last Mode:{}", CB.lastMapState);
                restoreMapState(CB.lastMapStateBeforeCar);
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
        }) {
            @Override
            public Menu getMenu() {
                menuInShow = true;
                return super.getMenu();
            }
        };
        addActor(mapStateButton);

        infoPanel = new MapInfoPanel();
        infoPanel.setBounds(10, 100, 200, 100);
        addActor(infoPanel);

        createCacheboxMapAdapter();

        addActor(LiveButton.getInstance());
        LiveButton.getInstance().setVisible(Config.showLiveButton.getValue());

        setTouchable(Touchable.enabled);

        zoomButton = new ZoomButton(changeValue -> {
            MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
            double value = mapPosition.getScale();
            if (changeValue > 0)
                value = value * 2;
            else
                value = value * 0.5;

            positionChangedHandler.scale(value);
            CB.lastMapState.setZoom(FastMath.log2((int) value));
        });
        zoomButton.pack();
        addActor(zoomButton);
    }

    private void storeMapstate(MapMode mapMode, MapMode beforeCar) {
        MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
        CB.lastMapState.setPosition(new Coordinate(mapPosition.getLatitude(), mapPosition.getLongitude()));
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

    private void restoreMapState(MapState mapState) {
        MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
        mapPosition.scale = 1 << mapState.getZoom();
        mapPosition.bearing = mapState.getOrientation();
        mapPosition.tilt = mapState.getTilt();
        if (mapState.getFreePosition() != null)
            mapPosition.setPosition(mapState.getFreePosition().getLatitude(), mapState.getFreePosition().getLongitude());

        mapStateButton.setMapMode(mapState.getMapMode(), true, selfEvent);
        infoPanel.setMapOrientationMode(mapState.getMapOrientationMode());
        cacheboxMapAdapter.setMapPosition(mapPosition);

        wayPointLayer.setLastZoomLevel(Config.lastZoomLevel.getValue());

        cacheboxMapAdapter.updateMap(true);
    }

    private void setCenterCrossLayerEnabled(boolean enabled) {
        enabled &= Settings_Map.ShowMapCenterCross.getValue();
        centerCrossLayer.setEnabled(enabled);
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

        log.debug("Tile.SIZE:" + Tile.SIZE);
        log.debug("Canvas.dpi:" + CanvasAdapter.dpi);


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
                if (Config.liveMapEnabled.getValue() && !isCarMode())
                    LiveMapQue.getInstance().quePosition(new Coordinate(lastCenterPosLat, lastCenterPosLon));

            }
        };
        staticCacheboxMapAdapter = cacheboxMapAdapter;

        ((CacheboxMain) Gdx.app.getApplicationListener()).mMapRenderer = new MapRenderer(cacheboxMapAdapter);
        ((CacheboxMain) Gdx.app.getApplicationListener()).mMapRenderer.onSurfaceCreated();

        initLayers(false);

        //add position changed handler
        positionChangedHandler = new MapViewPositionChangedHandler(cacheboxMapAdapter, directLineLayer, myLocationLayer, infoPanel);

    }

    @Override
    public void onShow() {
        addInputListener();
        sizeChanged();

        //set initial position/direction without animation
        Coordinate myPos = EventHandler.getMyPosition();

        //use saved pos
        if (myPos == null) {
            Coordinate latLon = CB.lastMapState.getFreePosition();
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
            restoreMapState(CB.lastMapState);
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
        LiveMapQue.getInstance().cancelDownloads();
        /*
        // is saved in MapState
        MapPosition mapPosition = cacheboxMapAdapter.getMapPosition();
        Config.mapInitLatitude.setValue(mapPosition.getLatitude());
        Config.mapInitLongitude.setValue(mapPosition.getLongitude());
         */
        Config.lastZoomLevel.setValue(wayPointLayer.getLastZoomLevel());
        Config.AcceptChanges();
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
        staticCacheboxMapAdapter = null;

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

        // float mapHalfWith = cacheboxMapAdapter.getWidth() / 2;
        // float mapHalfHeight = cacheboxMapAdapter.getHeight() / 2;

        // set position of MapScaleBar
        setMapScaleBarOffset(CB.scaledSizes.MARGIN, CB.scaledSizes.MARGIN_HALF);

        mapStateButton.setPosition(getWidth() - (mapStateButton.getWidth() + CB.scaledSizes.MARGIN),
                getHeight() - (mapStateButton.getHeight() + CB.scaledSizes.MARGIN));

        LiveButton.getInstance().setWidth(mapStateButton.getWidth());
        LiveButton.getInstance().setPosition(mapStateButton.getX(), mapStateButton.getY() - LiveButton.getInstance().getHeight());

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
        String baseMapName = Config.CurrentMapLayer.getValue()[0];
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

        // init basemap is with preloaded theme for day, no carmode,
        cacheboxMapAdapter.setNewBaseMap(baseMap);

        DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(cacheboxMapAdapter);
        mapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
        mapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
        mapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
        mapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

        directLineLayer = new DirectLineLayer(cacheboxMapAdapter);
        mapScaleBarLayer = new MapScaleBarLayer(cacheboxMapAdapter, mapScaleBar);
        wayPointLayer = new WaypointLayer(this, cacheboxMapAdapter);

        MapArrowStyle style = VisUI.getSkin().get(MapArrowStyle.class);
        String bmpName = ((GetName) style.myLocation).getName();
        myLocationLayer = new LocationTextureLayer(cacheboxMapAdapter, CB.textureRegionMap.get(bmpName));
        myLocationLayer.locationRenderer.setAccuracyColor(Color.BLUE);
        myLocationLayer.locationRenderer.setIndicatorColor(Color.RED);
        myLocationLayer.locationRenderer.setBillboard(false);

        boolean showDirectLine = Config.ShowDirektLine.getValue();
        log.debug("Initial direct line layer and {}", showDirectLine ? "enable" : "disable");
        directLineLayer.setEnabled(showDirectLine);
        GroupLayer layerGroup = new GroupLayer(cacheboxMapAdapter);

        centerCrossLayer = new CenterCrossLayer(cacheboxMapAdapter);

        if (tileGrid)
            layerGroup.layers.add(new TileGridLayer(cacheboxMapAdapter));
        layerGroup.layers.add(wayPointLayer);
        layerGroup.layers.add(directLineLayer);
        layerGroup.layers.add(myLocationLayer);
        layerGroup.layers.add(mapScaleBarLayer);
        layerGroup.layers.add(centerCrossLayer);

        if (TrackRecorder.getInstance().isStarted()) {
            TrackRecorder.getInstance().getRecordingTrack().showTrack();
        }

        for (Track track : TrackList.getTrackList()) {
            track.showTrack();
        }

        Config.ShowDirektLine.addChangedEventListener(() -> {
            if (cacheboxMapAdapter == null) return;
            log.debug("change direct line visibility to {}", Config.ShowDirektLine.getValue() ? "visible" : "invisible");
            directLineLayer.setEnabled(Config.ShowDirektLine.getValue());
            cacheboxMapAdapter.updateMap(true);
        });


        boolean showCenterCross = Config.ShowMapCenterCross.getValue();
        log.debug("Initial center cross layer and {}", showCenterCross ? "enable" : "disable");
        centerCrossLayer.setEnabled(showCenterCross);
        Config.ShowMapCenterCross.addChangedEventListener(showMapCenterCrossChangedListener);
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
        LiveButton.getInstance().setVisible(Config.showLiveButton.getValue());
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
        icm.addMenuItem("Layer", CB.getSkin().menuIcon.mapLayer, this::showMapViewLayerMenu);
        if (cacheboxMapAdapter.getBaseMap() instanceof MapsforgeSingleMap) {
            icm.addMenuItem("Renderthemes", CB.getSkin().menuIcon.theme, this::showMapViewThemeMenu);
            icm.addMenuItem("Styles", CB.getSkin().menuIcon.themeStyle, this::showMapViewThemeStyleMenu);
        }
        icm.addMenuItem("overlays", CB.getSkin().menuIcon.todo, this::showMapViewOverlaysMenu).setEnabled(false); // todo icon
        icm.addMenuItem("view", CB.getSkin().menuIcon.viewSettings, this::showMapViewElementsMenu);
        // nach Kompass ausrichten | setAlignToCompass is implemented in clicking mapInfo
        icm.addMenuItem("CenterWP", CB.getSkin().menuIcon.addWp, this::createWaypointAtCenter);
        icm.addMenuItem("TrackRecordMenuTitle", CB.getSkin().menuIcon.me3TrackList, this::showMenuTrackFunctions);
        return icm;
    }

    private void showMapViewLayerMenu() {
        Menu icm = new Menu("MapViewLayerMenuTitle");

        BaseMapManager.INSTANCE.refreshMaps();


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
                String[] currentLayer = Config.CurrentMapLayer.getValue();
                for (int j = 0, m = currentLayer.length; j < m; j++) {
                    String str = currentLayer[j];
                    if (str.equals(baseMap.name)) {
                        isChecked = true;
                        break;
                    }
                }

                icm.addCheckableMenuItem("", baseMap.name, icon, isChecked, () -> {
                    if (baseMap.isVector()) {

// if current layer is a Mapsforge map, it is posible to add the selected Mapsforge map
// to the current layer. We ask the User!
//                if (MapView.mapTileLoader.getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
//                    GL_MsgBox msgBox = GL_MsgBox.show("add or change", "Map selection", MessageBoxButton.YesNoCancel, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
//
//                        @Override
//                        public boolean onClick(int which, Object data) {
//
//                            switch (which) {
//                                case GL_MsgBox.BUTTON_POSITIVE:
//                                    // add the selected map to the current layer
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
                        showLanguageSelectionMenu(baseMap);
                        // set a theme for the map
                        String lastThemePath = CB.readThemeOfMap(baseMap.name, CB.currentThemeUsage);
                        if (lastThemePath.length() > 0) {
                            if (CB.setConfigsThemePath(CB.currentThemeUsage, lastThemePath)) {
                                // theme is changed, so mapStyle will also be bad (but not saved)
                                CB.createTheme(CB.getConfigsThemePath(CB.currentThemeUsage), "");
                            }
                        }
                    }
                    // possibly a not suitable theme will be set. Changed by Theme Selection
                    cacheboxMapAdapter.setNewBaseMap(baseMap);
                });
            }
        }

        icm.show();
    }

    private boolean showLanguageSelectionMenu(AbstractManagedMapLayer layer) {
        boolean hasLanguage = false;
        if (layer instanceof MapsforgeSingleMap) {
            MapFileTileSource mapFileTileSource = (MapFileTileSource) ((MapsforgeSingleMap) layer).getVectorTileSource();
            try {
                mapFileTileSource.open();
                MapInfo mapInfo = mapFileTileSource.getMapInfo();
                if (mapInfo != null) {
                    if (mapInfo.languagesPreference != null) {
                        String[] languages = mapInfo.languagesPreference.split(",");
                        if (languages.length > 1) {
                            final Menu lsm = new Menu("MapViewLayerSelectLanguageTitle");
                            for (String lang : languages) {
                                lsm.addMenuItem("", lang, null, () -> {
                                    mapFileTileSource.setPreferredLanguage(lang);
                                    Config.PreferredMapLanguage.setValue(lang);
                                    Config.AcceptChanges();
                                });
                            }
                            lsm.show();
                            hasLanguage = true;
                        }
                    }
                }
                mapFileTileSource.close();
            } catch (Exception ignored) {
            }
        }
        return hasLanguage;
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
        Menu icm = new OptionMenu("MapViewElementsMenuTitle");
        // icm.addCheckableMenuItem("MapShowCompass", Config.MapShowCompass.getValue(),()-> toggleSetting(Config.MapShowCompass));
        // todo icm.addCheckableMenuItem("MapShowInfoBar",Config.ShowInfo .....)
        addViewElement(icm, "ShowLiveMap", Config.showLiveButton, true);
        addViewElement(icm, "ShowAllWaypoints", Config.ShowAllWaypoints, true);
        addViewElement(icm, "ShowRatings", Config.MapShowRating, false);
        addViewElement(icm, "ShowDT", Config.MapShowDT, false);
        addViewElement(icm, "ShowTitle", Config.MapShowTitles, false);
        addViewElement(icm, "ShowDirectLine", Config.ShowDirektLine, false);
        addViewElement(icm, "MenuTextShowAccuracyCircle", Config.ShowAccuracyCircle, false);
        addViewElement(icm, "ShowCenterCross", Config.ShowMapCenterCross, false);
        icm.show();
    }

    private void addViewElement(Menu icm, String title, SettingBool setting, boolean withReload) {
        icm.addCheckableMenuItem(title, "", null, setting.getValue(), new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (icm.mustHandle(event)) {
                    MenuItem mi = (MenuItem) event.getListenerActor();
                    mi.setChecked(!mi.isChecked());
                    if (withReload) {
                        setting.setValue(!setting.getValue());
                        Config.AcceptChanges();
                        setNewSettings();
                    } else {
                        setting.setValue(!setting.getValue());
                        Config.AcceptChanges();
                        setNewSettings();
                    }
                }
            }
        });
    }

    private void showMapViewThemeMenu() {
        Menu mapViewThemeMenu = new Menu("MapViewThemeMenuTitle");
        //add default themes
        for (VtmThemes vtmTheme : VtmThemes.values()) {
            mapViewThemeMenu.addCheckableMenuItem("", vtmTheme.name(), null, vtmTheme.equals(CB.getCurrentTheme()),
                    () -> {
                        // Apply Selection to map
                        if (CB.currentThemeUsage == whichUsage) {
                            IRenderTheme thisRenderTheme = CB.createTheme(CB.getConfigsThemePath(whichUsage), "");
                            CB.setCurrentTheme(whichUsage, thisRenderTheme);
                            cacheboxMapAdapter.setTheme(CB.getCurrentTheme());
                        } else {
                            // remember the setup for this case
                            // CB.setConfigsThemePath(whichUsage, vtmTheme.mPath);
                        }
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
            mapViewThemeMenu.addCheckableMenuItem("", themeFile.name, null, CB.getConfigsThemePath(whichUsage).equals(themeFile.path),
                    () -> {
                        // apply theme to layer(s)
                        if (CB.currentThemeUsage == whichUsage) {
                            CB.setConfigsThemePath(whichUsage, themeFile.path);
                            CB.setCurrentTheme(whichUsage, CB.createTheme(CB.getConfigsThemePath(whichUsage), CB.getConfigsMapStyle(whichUsage)));
                            cacheboxMapAdapter.setTheme(CB.getCurrentTheme());
                        } else {
                            // remember the setup for this case
                            CB.setConfigsThemePath(whichUsage, themeFile.path);
                        }
                    });
        }

        final String target = themesPath + "/Elevate4.zip";
        if (themesPath.length() > 0) {
            mapViewThemeMenu.addDivider(-1);
            mapViewThemeMenu.addMenuItem("Download", "\n OpenAndroMaps",
                    CB.getSkin().menuIcon.baseMapMapsforge,
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
                                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButton.OK, MessageBoxIcon.Exclamation, null);
                                }
                                Gdx.files.absolute(target).delete();
                            }
                        }
                    });
            mapViewThemeMenu.addMenuItem("Download", "\n Freizeitkarte",
                    CB.getSkin().menuIcon.baseMapFreizeitkarte, () -> showFZKDownloadMenu());
        }

        mapViewThemeMenu.show();
    }

    private void showFZKDownloadMenu() {
        Menu mapViewFZKDownloadMenu = new Menu("Download");

        if (fzkThemesInfoList.size == 0) {
            fzkThemesInfoList = getMapInfoList(Webb.create()
                    .get("http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml")
                    .readTimeout(Config.socket_timeout.getValue())
                    .ensureSuccess()
                    .asStream()
                    .getBody());
        }

        for (FZKThemesInfo fzkThemesInfo : fzkThemesInfoList) {
            // todo change to explicit clicklistener, if animation works
            mapViewFZKDownloadMenu.addMenuItem("Download", "\n" + fzkThemesInfo.Description, CB.getSkin().menuIcon.baseMapFreizeitkarte, () -> {
                String zipFile = fzkThemesInfo.Url.substring(fzkThemesInfo.Url.lastIndexOf("/") + 1);
                String target = themesPath + "/" + zipFile;

                Download.Download(fzkThemesInfo.Url, target);
                try {
                    new UnZip().extractFolder(target);
                } catch (Exception ex) {
                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButton.OK, MessageBoxIcon.Exclamation, null);
                }
                Gdx.files.absolute(target).delete();
            });
        }

        mapViewFZKDownloadMenu.show();
    }

    private Array<FZKThemesInfo> getMapInfoList(InputStream stream) {
        final FZKThemesInfo[] info = {new FZKThemesInfo()};
        final Array<FZKThemesInfo> list = new Array<>();

        XmlStreamParser parser = new XmlStreamParser();
        parser.registerDataHandler("/Freizeitkarte/Theme/Name", new XmlStreamParser.DataHandler() {
            @Override
            public void handleData(char[] data, int offset, int length) {
                info[0].Name = new String(data, offset, length).trim();
            }
        });
        if (Config.localization.getEnumValue() == Language.de) {
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
        Menu menuMapStyle = new Menu("MapViewThemeStyleMenuTitle");
        ThemeMenu themeMenu = new ThemeMenu(CB.getConfigsThemePath(whichUsage));
        themeMenu.readTheme();
        ObjectMap<String, String> mapStyles = themeMenu.getStyles();
        final OptionMenu om = new OptionMenu("");

        for (String mapStyle : mapStyles.keys()) {
            // check, if saved for selected layer
            menuMapStyle.addMenuItem("", mapStyle, null, () -> showMapStyleOptions(om, mapStyle, mapStyles, themeMenu));
        }
        if (mapStyles.size > 1)
            menuMapStyle.show();
        else {
            for (String mapStyle : mapStyles.keys()) {
                showMapStyleOptions(om, mapStyle, mapStyles, themeMenu);
            }
        }
    }

    private void showMapStyleOptions(OptionMenu om, String mapStyle, ObjectMap<String, String> mapStyles, ThemeMenu themeMenu) {
        om.setName("-" + mapStyle);
        String mapStyleId = mapStyles.get(mapStyle);
        ObjectMap<String, String> overlay = themeMenu.getOverlays(mapStyleId);
        Set<String> configOverlays = themeMenu.readOverlays(mapStyleId);
        for (ObjectMap.Entry entry : overlay) {
            boolean checked;
            if (configOverlays.size() > 0) {
                checked = configOverlays.contains(((String) entry.value).substring(1));
            } else {
                checked = ((String) entry.value).startsWith("+");
            }
            om.addCheckableMenuItem("", entry.key.toString(), null, checked, new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (om.mustHandle(event)) {
                        MenuItem mi = (MenuItem) event.getListenerActor();
                        mi.setChecked(!mi.isChecked());
                    }
                }
            });
        }
        om.addOnHideListener(() -> {
            Array<String> result = new Array<>();
            for (ListViewItem li : om.getItems()) {
                MenuItem mi = (MenuItem) li;
                if (mi.isChecked()) {
                    result.add(overlay.get(mi.getTitle()).substring(1));
                }
            }
            themeMenu.writeConfig(mapStyles.get(mapStyle), result);
            themeMenu.applyConfig(mapStyles.get(mapStyle)); // or direct from result?
            CB.setCurrentTheme(whichUsage, themeMenu.getRenderTheme());
            CB.setConfigsMapStyle(whichUsage, mapStyles.get(mapStyle));
            cacheboxMapAdapter.setTheme(CB.getCurrentTheme());
        });
        om.show();
    }

    private void searchThemes(FileHandle folder, Array<NamedExternalRenderTheme> themes) {
        if (!folder.isDirectory()) return;
        for (FileHandle handle : folder.list()) {
            if (handle.isDirectory()) {
                searchThemes(handle, themes);
            } else if (handle.extension().equals("xml")) {
                try {
                    NamedExternalRenderTheme extTheme = new NamedExternalRenderTheme(handle.nameWithoutExtension(),
                            handle.file().getCanonicalPath());
                    themes.add(extTheme);
                } catch (IRenderTheme.ThemeException | IOException e) {
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

    private void showMenuTrackFunctions() {
        Menu cm2 = new Menu("TrackRecordMenuTitle");
        //todo internal routing
        cm2.addMenuItem("generateRoute", CB.getSkin().menuIcon.todo, () -> {
        }).setEnabled(false); // routeProfileIcons[Config.routeProfile.getValue()] // pedestrian, bicycle, car
        cm2.addDivider(-1);
        cm2.addMenuItem("", Translation.get("TrackDistance", "" + Config.TrackDistance.getValue()).toString(), null, () -> {
            Menu tdMenu = new Menu("TrackDistance");
            for (int possibleDistance : Settings_Const.trackDistanceArray) {
                MenuItem mi = tdMenu.addCheckableMenuItem("", "" + possibleDistance, null, possibleDistance == Config.TrackDistance.getValue(), new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        if (tdMenu.mustHandle(event)) {
                            int newDistance = (Integer) event.getListenerActor().getUserObject();
                            Config.TrackDistance.setValue(newDistance);
                            Config.AcceptChanges();
                            showMenuTrackFunctions();
                        }
                    }
                });
                mi.setUserObject(possibleDistance);
            }
            tdMenu.show();
        });
        cm2.addMenuItem("start", null, this::startTrackRecorder).setEnabled(!TrackRecorder.getInstance().isStarted());
        if (TrackRecorder.getInstance().isPaused())
            cm2.addMenuItem("continue", null, () -> TrackRecorder.getInstance().continueRecording()).setEnabled(TrackRecorder.getInstance().isStarted());
        else
            cm2.addMenuItem("pause", null, () -> TrackRecorder.getInstance().pauseRecording()).setEnabled(TrackRecorder.getInstance().isStarted());
        cm2.addMenuItem("stop", null, this::stopTrackRecorder).setEnabled(TrackRecorder.getInstance().isStarted() || TrackRecorder.getInstance().isPaused());
        cm2.addDivider(0);
        // to be visible on map have to create pathlayer and add to layers
        cm2.addMenuItem("load", CB.getSkin().menuIcon.me3TrackList, new TrackListView()::selectTrackFileReadAndAddToTracks);
        //todo cm2.addMenuItem("generate", null, () -> TrackCreation.getInstance().execute());
        cm2.addDivider(1);
        cm2.addMenuItem("Tracks", CB.getSkin().menuIcon.me3TrackList, () -> CB.viewmanager.showView(new TrackListView()));
        cm2.show();
    }

    private void startTrackRecorder() {
        TrackRecorder.getInstance().startRecording();
        TrackRecorder.getInstance().getRecordingTrack().showTrack();
    }

    private void stopTrackRecorder() {
        TrackRecorder.getInstance().stopRecording();
        TrackRecorder.getInstance().getRecordingTrack().hideTrack();
    }

    public void clickOnItem(final MapWayPointItem item) {
        if (infoBubble != null) removeActor(infoBubble);
        infoBubble = new MapBubble(item.dataObject);
        VisTable table = new VisTable();
        table.add(infoBubble).expand().fill();
        infoBubble.layout();
        table.layout();
        addActor(infoBubble);
        infoItem = item;
        setInfoBubblePos();
        CB.requestRendering();
        addListener(bubbleClickListener);
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

    public MapStateButton getMapStateButton() {
        return mapStateButton;
    }

    public static class FZKThemesInfo {
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        public String MD5;
    }

}
