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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
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
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.BaseMapManager;
import de.longri.cachebox3.gui.map.baseMap.OSciMap;
import de.longri.cachebox3.gui.map.layer.LocationAccuracyLayer;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.gui.skin.styles.MapArrowStyle;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.MapCompass;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.locator.Location;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.settings.Settings_Map;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.event.Event;
import org.oscim.gdx.GestureHandlerImpl;
import org.oscim.gdx.MotionHandler;
import org.oscim.layers.GroupLayer;
import org.oscim.layers.Layer;
import org.oscim.layers.TileGridLayer;
import org.oscim.map.Layers;
import org.oscim.map.Map;
import org.oscim.map.Viewport;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.MapRenderer;
import org.oscim.renderer.atlas.TextureAtlas;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.PolygonBucket;
import org.oscim.renderer.bucket.TextItem;
import org.oscim.renderer.bucket.TextureBucket;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.scalebar.*;
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
    final static Logger log = LoggerFactory.getLogger(MapView.class);

    private InputMultiplexer mapInputHandler;
    private CacheboxMapAdapter mMap;
    private final CacheboxMain main;
    private MapScaleBarLayer mapScaleBarLayer;
    private float myBearing;
    private final MapStateButton mapStateButton;
    private final MapCompass mapOrientationButton;
    private final ZoomButton zoomButton;
    private WaypointLayer wayPointLayer;
    private final LinkedHashMap<Object, TextureRegion> textureRegionMap;

    LocationAccuracyLayer myLocationAccuracy;

    private LocationLayer myLocationLayer;

    MapViewPositionChangedHandler positionChangedHandler;


    public MapView(CacheboxMain main) {
        super("MapView");
        this.setTouchable(Touchable.disabled);
        this.main = main;
        this.textureRegionMap = createTextureAtlasRegions();

        mapStateButton = new MapStateButton(new MapStateButton.StateChangedListener() {
            @Override
            public void stateChanged(MapState state) {
                positionChangedHandler.setMapState(state);
                checkInputListener();

                Location actLocation;
                double scale;

                switch (state) {

                    case FREE:
                        break;
                    case GPS:
                        // set to act position
                        actLocation = Locator.getLocation();
                        scale = mMap.getMapPosition().getScale();
                        mMap.setMapPosition(actLocation.latitude, actLocation.longitude, scale);
                        break;
                    case WP:
                        break;
                    case LOCK:
                        break;
                    case CAR:

                        // set to act position
                        actLocation = Locator.getLocation();
                        scale = mMap.getMapPosition().getScale();
                        mMap.setMapPosition(actLocation.latitude, actLocation.longitude, scale);

                        // set full tilt
                        MapPosition mapPosition = mMap.getMapPosition();
                        mapPosition.setTilt(Viewport.MAX_TILT);
                        mMap.setMapPosition(mapPosition);

                        break;
                }


            }
        });
        this.mapOrientationButton = new MapCompass(mapStateButton.getWidth(), mapStateButton.getHeight());


        mMap = createMap();

        this.addActor(mapStateButton);
        this.addActor(mapOrientationButton);
        this.setTouchable(Touchable.enabled);

        this.zoomButton = new ZoomButton(new ZoomButton.ValueChangeListener() {
            @Override
            public void valueChanged(int changeValue) {
                if (changeValue > 0)
                    MapView.this.mMap.animator().animateZoom(500, 2, 0, 0);
                else
                    MapView.this.mMap.animator().animateZoom(500, 0.5, 0, 0);

                MapView.this.mMap.updateMap(true);

            }
        });
        this.zoomButton.pack();
        this.addActor(zoomButton);
    }

    private void checkInputListener() {
        MapState state = mapStateButton.getState();
        // remove input handler with map state Car and Lock
        if (state == MapState.CAR || state == MapState.LOCK) {
            removeInputListener();
        } else {
            addInputListener();
        }
    }

    public CacheboxMapAdapter createMap() {

        {//set map scale
            float scaleFactor = CB.getScaledFloat(Settings.MapViewDPIFaktor.getValue());
            Tile.SIZE = (int) (400 * scaleFactor);
            CanvasAdapter.dpi = 240 * scaleFactor;
            CanvasAdapter.textScale = scaleFactor;
            CanvasAdapter.scale = scaleFactor;
            PolygonBucket.enableTexture = CanvasAdapter.platform != Platform.IOS;//fixme if vtm can render polygon texture

            log.debug("Create new map instance with scale factor:" + Float.toString(scaleFactor));
            log.debug("Tile.SIZE:" + Integer.toString(Tile.SIZE));
            log.debug("Canvas.dpi:" + Float.toString(CanvasAdapter.dpi));
        }


        main.drawMap = true;
        mMap = new CacheboxMapAdapter() {
            @Override
            public void onMapEvent(Event e, final MapPosition mapPosition) {

                if (e == Map.MOVE_EVENT) {
//                    log.debug("Map.MOVE_EVENT");
                    mapStateButton.setState(MapState.FREE);
                } else if (e == Map.TILT_EVENT) {
//                    log.debug("Map.TILT_EVENT");
                    if (positionChangedHandler != null)
                        positionChangedHandler.tiltChangedFromMap(mapPosition.getTilt());
                } else if (e == Map.ROTATE_EVENT) {
//                    log.debug("Map.ROTATE_EVENT");
                    if (positionChangedHandler != null)
                        positionChangedHandler.rotateChangedFromUser(mapPosition.getBearing());
                } else if (e == Map.ANIM_END) {
//                    log.debug("Map.ANIM_END" + mapPosition);
                }
            }
        };
        main.mMapRenderer = new MapRenderer(mMap);
        main.mMapRenderer.onSurfaceCreated();
        mMap.setMapPosition(52.580400947530364, 13.385594096047232, 1 << 17);

        //          grid,labels,buldings,scalebar
        initLayers(true, true, true, true);


        //add position changed handler
        positionChangedHandler = MapViewPositionChangedHandler.getInstance
                (mMap, myLocationLayer, myLocationAccuracy, mapOrientationButton);


        return mMap;
    }

    public static LinkedHashMap<Object, TextureRegion> createTextureAtlasRegions() {
        // create TextureRegions from all Bitmap symbols
        LinkedHashMap<Object, TextureRegion> textureRegionMap = new LinkedHashMap<Object, TextureRegion>();
        ObjectMap<String, MapWayPointItemStyle> list = VisUI.getSkin().getAll(MapWayPointItemStyle.class);
        Array<Bitmap> bitmapList = new Array<Bitmap>();
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
        }

        if (mapArrowStyle != null) {
            if (mapArrowStyle.myLocation != null) bitmapList.add(mapArrowStyle.myLocation);
            if (mapArrowStyle.myLocationTransparent != null) bitmapList.add(mapArrowStyle.myLocationTransparent);
            if (mapArrowStyle.myLocationCar != null) bitmapList.add(mapArrowStyle.myLocationCar);
        }


        LinkedHashMap<Object, Bitmap> input = new LinkedHashMap<Object, Bitmap>();
        for (Bitmap bmp : bitmapList) {
            input.put(((GetName) bmp).getName(), bmp);
        }
        ArrayList<TextureAtlas> atlasList = new ArrayList<TextureAtlas>();
        TextureAtlasUtils.createTextureRegions(input, textureRegionMap, atlasList, true,
                CanvasAdapter.platform == Platform.IOS);


        if (false) {//Debug write atlas Bitmap to tmp folder
            int count = 0;
            for (TextureAtlas atlas : atlasList) {
                byte[] data = atlas.texture.bitmap.getPngEncodedData();
                Pixmap pixmap = new Pixmap(data, 0, data.length);
                FileHandle file = Gdx.files.absolute(CB.WorkPath + "/user/temp/testAtlas" + count++ + ".png");
                PixmapIO.writePNG(file, pixmap);
                pixmap.dispose();
            }
        }
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
    }

    @Override
    public void onHide() {
        removeInputListener();
    }


    @Override
    public void dispose() {
        log.debug("Dispose MapView");

        positionChangedHandler.dispose();
        positionChangedHandler = null;

        Layers layers = mMap.layers();
        for (Layer layer : layers) {
            if (layer instanceof Disposable) {
                ((Disposable) layer).dispose();
            }
        }

        layers.clear();

        wayPointLayer = null;

        mapInputHandler.clear();
        mapInputHandler = null;

        main.drawMap = false;
        mMap.clearMap();
        mMap.destroy();
        TextureBucket.pool.clear();
        TextItem.pool.clear();
        TextureItem.disposeTextures();

        main.mMapRenderer = null;
        mMap = null;

        //dispose actors
        mapOrientationButton.dispose();
        mapStateButton.dispose();

    }


    @Override
    public void sizeChanged() {
        if (mMap == null) return;
        mMap.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
        mMap.viewport().setScreenSize((int) this.getWidth(), (int) this.getHeight());
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

        // set position of MapScaleBar
        setMapScaleBarOffset(CB.scaledSizes.MARGIN, CB.scaledSizes.MARGIN_HALF);

        mapStateButton.setPosition(getWidth() - (mapStateButton.getWidth() + CB.scaledSizes.MARGIN),
                getHeight() - (mapStateButton.getHeight() + CB.scaledSizes.MARGIN));

        mapOrientationButton.setPosition(CB.scaledSizes.MARGIN,
                getHeight() - (mapOrientationButton.getHeight() + CB.scaledSizes.MARGIN));

        zoomButton.setPosition(getWidth() - (zoomButton.getWidth() + CB.scaledSizes.MARGIN), CB.scaledSizes.MARGIN);
    }


    @Override
    public void positionChanged() {
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
    }


    protected void initLayers(boolean tileGrid, boolean labels,
                              boolean buildings, boolean mapScalebar) {

        // load last saved BaseMap
        String baseMapName = Settings_Map.CurrentMapLayer.getValue()[0];
        BaseMapManager.INSTANCE.setMapFolder(Gdx.files.absolute(Settings_Map.MapPackFolder.getValue()));
        AbstractManagedMapLayer baseMap = null;
        for (int i = 0, n = BaseMapManager.INSTANCE.size(); i < n; i++) {
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

        //MyLocationLayer
        myLocationAccuracy = new LocationAccuracyLayer(mMap);
        myLocationAccuracy.setPosition(52.580400947530364, 13.385594096047232, 100);

        myLocationLayer = new LocationLayer(mMap, textureRegionMap);
        myLocationLayer.setPosition(52.580400947530364, 13.385594096047232);


        GroupLayer layerGroup = new GroupLayer(mMap);


        if (tileGrid)
            layerGroup.layers.add(new TileGridLayer(mMap));

        if (mapScalebar) {
            DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(mMap);
            mapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
            mapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
            mapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
            mapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

            mapScaleBarLayer = new MapScaleBarLayer(mMap, mapScaleBar);
            layerGroup.layers.add(mapScaleBarLayer);
            layerGroup.layers.add(myLocationAccuracy);
        }
        wayPointLayer = new WaypointLayer(mMap, textureRegionMap);
        layerGroup.layers.add(wayPointLayer);
        layerGroup.layers.add(myLocationLayer);

        mMap.layers().add(layerGroup);

    }

    public void setMapScaleBarOffset(float xOffset, float yOffset) {
        if (mapScaleBarLayer == null) return;
        BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
        renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
        renderer.setOffset(xOffset, yOffset);
    }


    public void setInputListener(boolean on) {
        if (on) {
            checkInputListener();
        } else {
            removeInputListener();
        }
    }


    private void createMapInputHandler() {
        GestureDetector gestureDetector = new GestureDetector(new GestureHandlerImpl(mMap));
        MotionHandler motionHandler = new MotionHandler(mMap);
        MapInputHandler inputHandler = new MapInputHandler(mMap);
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
        return mapOrientationButton.isNorthOriented();
    }

    public void setAlignToCompass(boolean align) {
        mapOrientationButton.setState(align ? MapCompass.State.NORTH : MapCompass.State.COMPASS);
    }

    public void setNewSettings() {
        //TODO
    }

    public void setBaseMap(AbstractManagedMapLayer baseMap) {
        this.mMap.setNewBaseMap(baseMap);
    }
}
