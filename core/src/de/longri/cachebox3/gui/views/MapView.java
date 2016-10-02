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
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.locator.Location;
import org.oscim.gdx.LayerHandler;
import org.oscim.gdx.MotionHandler;
import org.oscim.layers.TileGridLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Layers;
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

    LocationOverlay myLocationAccuracy;
    MyLocationModel myLocationModel;

    MapViewPositionChangedHandler positionChangedHandler;

    public MapView(CacheboxMain main) {
        super("MapView");
        this.setTouchable(Touchable.disabled);
        this.main = main;
        mMap = createMap();
        mapStateButton = new MapStateButton(new MapStateButton.StateChangedListener() {
            @Override
            public void stateChanged(MapState state) {
                positionChangedHandler.setMapState(state);
                checkInputListener();
            }
        });


        this.addActor(mapStateButton);

        // set Position
        mapStateButton.setPosition(100, 250);

        this.setTouchable(Touchable.enabled);
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
            public void tiltChanged(float newTilt) {
                if (positionChangedHandler != null) positionChangedHandler.tiltChangedFromMap(newTilt);
            }
        };
        main.mMapRenderer = new MapRenderer(mMap);
        main.mMapRenderer.onSurfaceCreated();
        mMap.setMapPosition(52.580400947530364, 13.385594096047232, 1 << 17);

        //          grid,labels,buldings,scalebar
        initLayers(false, true, true, true);


        //add position changed handler
        positionChangedHandler = MapViewPositionChangedHandler.getInstance(mMap, myLocationModel, myLocationAccuracy);


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
        // overide and don't call supper
        // for non creation of default name label
    }

    @Override
    public void onShow() {
        addInputListener();
        testSetLocation();
    }

    @Override
    public void onHide() {
        destroyMap();
    }

    private void testSetLocation() {
        de.longri.cachebox3.locator.Location cbLocation =
                new de.longri.cachebox3.locator.Location(52.580400947530364,
                        13.385594096047232, 10);

        cbLocation.setHasBearing(true);
//        cbLocation.setBearing(0);
//        cbLocation.setBearing(90);
//        cbLocation.setBearing(180);
        cbLocation.setBearing(360);

        cbLocation.setProvider(Location.ProviderType.GPS);
        log.trace("Update location:" + cbLocation.toString());
        de.longri.cachebox3.locator.Locator.setNewLocation(cbLocation);

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
        MapInputHandler inputHandler = new MapInputHandler(mMap);
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
