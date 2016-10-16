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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import de.longri.cachebox3.gui.map.layer.Compass;
import de.longri.cachebox3.gui.map.layer.LocationOverlay;
import de.longri.cachebox3.gui.map.layer.MyLocationModel;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.MapOrientationButton;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.locator.Location;
import de.longri.cachebox3.locator.Locator;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.gdx.LayerHandler;
import org.oscim.gdx.MotionHandler;
import org.oscim.layers.TileGridLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Layers;
import org.oscim.map.Map;
import org.oscim.map.Viewport;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.MapRenderer;
import org.oscim.renderer.bucket.TextItem;
import org.oscim.renderer.bucket.TextureBucket;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.scalebar.*;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.slf4j.LoggerFactory;


/**
 * The MapView has transparent background. The Map render runs at CacheboxMain.
 * This View has only the controls for the Map!
 * Created by Longri on 24.07.16.
 */
public class MapView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(MapView.class);

    private InputMultiplexer mapInputHandler;
    private CacheboxMapAdapter mMap;
    private final CacheboxMain main;
    private MapScaleBarLayer mapScaleBarLayer;
    private float myBearing;
    private final MapStateButton mapStateButton;
    private final MapOrientationButton mapOrientationButton;
    private final ZoomButton zoomButton;

    LocationOverlay myLocationAccuracy;
    MyLocationModel myLocationModel;

    MapViewPositionChangedHandler positionChangedHandler;

    public MapView(CacheboxMain main) {
        super("MapView");
        this.setTouchable(Touchable.disabled);
        this.main = main;
        this.mapOrientationButton = new MapOrientationButton();
        mMap = createMap();
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

                        // set full tillt
                        MapPosition mapPosition = mMap.getMapPosition();
                        mapPosition.setTilt(Viewport.MAX_TILT);
                        mMap.setMapPosition(mapPosition);

                        // set orientation by bearing
                        mapOrientationButton.setChecked(true);

                        break;
                }


            }
        });


        this.addActor(mapStateButton);
        this.addActor(mapOrientationButton);
        this.setTouchable(Touchable.enabled);

        this.zoomButton = new ZoomButton();
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
        main.drawMap = true;
        mMap = new CacheboxMapAdapter() {

            @Override
            public void tiltChanged(float newTilt) {
                if (positionChangedHandler != null) positionChangedHandler.tiltChangedFromMap(newTilt);
            }

            @Override
            public void onMapEvent(Event e, MapPosition mapPosition) {
                if (e == Map.MOVE_EVENT) {
                    // map is moved by user
                    mapStateButton.setState(MapState.FREE);
                }
            }
        };
        main.mMapRenderer = new MapRenderer(mMap);
        main.mMapRenderer.onSurfaceCreated();
        mMap.setMapPosition(52.580400947530364, 13.385594096047232, 1 << 17);

        //          grid,labels,buldings,scalebar
        initLayers(false, true, true, true);


        //add position changed handler
        positionChangedHandler = MapViewPositionChangedHandler.getInstance
                (mMap, myLocationModel, myLocationAccuracy, mapOrientationButton);


        return mMap;
    }

    public void destroyMap() {
        main.drawMap = true;
        mMap.clearMap();
        mMap.destroy();
        mMap = null;

        TextureBucket.pool.clear();
        TextItem.pool.clear();
        TextureItem.disposeTextures();

        main.mMapRenderer = null;
    }

    @Override
    protected void create() {
        // overide and don't call super
        // for non creation of default name label
    }

    @Override
    public void onShow() {
        addInputListener();
    }

    @Override
    public void onHide() {
        destroyMap();
    }


    @Override
    public void dispose() {
        log.debug("Dispose MapView");
        mapInputHandler.clear();
        mapInputHandler = null;
        mMap = null;
        mapStateButton.dispose();
    }


    @Override
    public void sizeChanged() {
        if (mMap == null) return;
        mMap.setSize((int) this.getWidth(), (int) this.getHeight());
        mMap.viewport().setScreenSize((int) this.getWidth(), (int) this.getHeight());
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

        // set position of MapScaleBar
        setMapScaleBarOffset(CB.scaledSizes.MARGIN, CB.scaledSizes.MARGIN_HALF);

        mapStateButton.setPosition(getWidth() - (mapStateButton.getWidth() + CB.scaledSizes.MARGIN),
                getHeight() - (mapStateButton.getHeight() + CB.scaledSizes.MARGIN));

        mapOrientationButton.setPosition(CB.scaledSizes.MARGIN,
                getHeight() - (mapStateButton.getHeight() + CB.scaledSizes.MARGIN));

        zoomButton.setPosition(getWidth() - (zoomButton.getWidth() + CB.scaledSizes.MARGIN), CB.scaledSizes.MARGIN);
    }


    @Override
    public void positionChanged() {
        main.setMapPosAndSize((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());
    }


    protected void initLayers(boolean tileGrid, boolean labels,
                              boolean buildings, boolean mapScalebar) {

//        TileSource tileSource = new OSciMap4TileSource();

        MapFileTileSource tileSource = new MapFileTileSource();
        FileHandle mapFileHandle = Gdx.files.local(CB.WorkPath + "/repository/maps/germany.map");
        tileSource.setMapFile(mapFileHandle.path());
        tileSource.setPreferredLanguage("en");

        Layers layers = mMap.layers();


        //MyLocationLayer
        myLocationAccuracy = new LocationOverlay(mMap, new Compass() {
            @Override
            public void setEnabled(boolean enabled) {

            }

            @Override
            public float getRotation() {
                return myBearing;
            }
        });
        myLocationAccuracy.setPosition(52.580400947530364, 13.385594096047232, 100);


        Model model = CB.getSkin().get("MyLocationModel", Model.class);

        myLocationModel = new MyLocationModel(mMap, model);

        myLocationAccuracy.setPosition(52.580400947530364, 13.385594096047232, 100);

        if (tileSource != null) {
            VectorTileLayer mapLayer = mMap.setBaseMap(tileSource);
            mMap.setTheme(VtmThemes.DEFAULT);

            if (buildings)
                layers.add(new BuildingLayer(mMap, mapLayer));

            if (labels)
                layers.add(new LabelLayer(mMap, mapLayer));
        }

        if (tileGrid)
            layers.add(new TileGridLayer(mMap));

        if (mapScalebar) {
            DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(mMap);
            mapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
            mapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
            mapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
            mapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

            mapScaleBarLayer = new MapScaleBarLayer(mMap, mapScaleBar);
            layers.add(mapScaleBarLayer);
            layers.add(myLocationAccuracy);
            layers.add(myLocationModel);
        }


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
        GestureDetector gestureDetectore = new GestureDetector(new LayerHandler(mMap));
        MotionHandler motionHandler = new MotionHandler(mMap);
        MapInputHandler inputHandler = new MapInputHandler(mMap) {
            @Override
            public void rotateByUser() {
                mapOrientationButton.setChecked(false);
            }
        };
        mapInputHandler = new InputMultiplexer();
        mapInputHandler.addProcessor(motionHandler);
        mapInputHandler.addProcessor(gestureDetectore);
        mapInputHandler.addProcessor(inputHandler);
    }

    private void addInputListener() {
        if (mapInputHandler == null) createMapInputHandler();
        StageManager.addMapMultiplexer(mapInputHandler);
    }

    private void removeInputListener() {
        StageManager.removeMapMultiplexer(mapInputHandler);
    }
}
