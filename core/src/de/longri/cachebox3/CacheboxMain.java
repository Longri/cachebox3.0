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
package de.longri.cachebox3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.stages.Splash;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.settings.Config;
import org.oscim.layers.TileGridLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Layers;
import org.oscim.map.Map;
import org.oscim.renderer.GLState;
import org.oscim.renderer.MapRenderer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;

import static org.slf4j.impl.LibgdxLogger.DEFAULT_LOG_LEVEL_KEY;

public class CacheboxMain extends ApplicationAdapter {

    static {

        System.setProperty(DEFAULT_LOG_LEVEL_KEY, CB.USED_LOG_LEVEL);
        LibgdxLogger.init();

    }

    final static org.slf4j.Logger log = LoggerFactory.getLogger(CacheboxMain.class);

    Batch batch;
    protected int FpsInfoPos = 0;

    private Sprite FpsInfoSprite;
    private final Matrix4 NORMAL_MATRIX = new Matrix4().toNormalMatrix();

    protected CacheboxMapAdapter mMap;
    protected GestureDetector mGestureDetector;

    private MapRenderer mMapRenderer;

    @Override
    public void create() {

        mMap = new CacheboxMapAdapter();
        mMapRenderer = new MapRenderer(mMap);

        Gdx.graphics.setContinuousRendering(false);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        mMapRenderer.onSurfaceCreated();
        setMapSize(w, h);

        StageManager.setMainStage(new Splash(new Splash.LoadReady() {
            @Override
            public void ready() {
                Config.AppRaterlaunchCount.setValue(Config.AppRaterlaunchCount.getValue() + 1);
                Config.AcceptChanges();

                // Splash is ready with initialisation
                // now switch Stage to ViewManager
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        StageManager.setMainStage(new ViewManager());
                    }
                });
            }
        }));

        createLayers();
        Gdx.graphics.requestRendering();

    }

    private void setMapSize(int width, int height) {
        mMap.setSize(width, height);
        mMap.viewport().setScreenSize(width, height);
        mMapRenderer.onSurfaceChanged(width, height);
    }

    public void createLayers() {
        TileSource tileSource = new OSciMap4TileSource();

        // TileSource tileSource = new MapFileTileSource();
        // tileSource.setOption("file", "/home/jeff/germany.map");

        initDefaultLayers(tileSource, false, true, true);

        //mMap.getLayers().add(new BitmapTileLayer(mMap, new ImagicoLandcover(), 20));
        //mMap.getLayers().add(new BitmapTileLayer(mMap, new OSMTileSource(), 20));
        //mMap.getLayers().add(new BitmapTileLayer(mMap, new ArcGISWorldShaded(), 20));

        mMap.setMapPosition(0, 0, 1 << 2);
    }

    protected void initDefaultLayers(TileSource tileSource, boolean tileGrid, boolean labels,
                                     boolean buildings) {
        Layers layers = mMap.layers();

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
    }


    @Override
    public void render() {
        CB.stateTime += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(CB.backgroundColor.r, CB.backgroundColor.g, CB.backgroundColor.b, CB.backgroundColor.a);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ?
                GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        StageManager.draw();

        mMapRenderer.onDrawFrame();

        //release Buffers from map renderer
        GLState.bindVertexBuffer(0);
        GLState.bindElementBuffer(0);

        if (CB.isTestVersion()) {
            float FpsInfoSize = CB.getScaledFloat(4f);
            if (FpsInfoSprite != null) {
                batch = StageManager.getBatch();
                batch.begin();
                Color lastColor = batch.getColor();
                batch.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                batch.draw(FpsInfoSprite, FpsInfoPos, 2, FpsInfoSize, FpsInfoSize);
                batch.setColor(lastColor);
                batch.end();
            } else {
                Sprite sprite = CB.getSprite("color");
                if (sprite != null) {
                    FpsInfoSprite = new Sprite(sprite);
                    FpsInfoSprite.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                    FpsInfoSprite.setSize(FpsInfoSize, FpsInfoSize);
                }
            }

            FpsInfoPos += FpsInfoSize;
            if (FpsInfoPos > 60 * FpsInfoSize) {
                FpsInfoPos = 0;
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public void pause() {
        log.debug("on pause", "close databases");
        //close databases
//        if (Database.Data != null) Database.Data.Close();
//        if (Database.Settings != null) Database.Settings.Close();
//        if (Database.FieldNotes != null) Database.FieldNotes.Close();
    }

    @Override
    public void resume() {
//        log.debug("on resume", "reopen databases");
//        //open databases
//        if (Database.Data != null) Database.Data.Open();
//        if (Database.Settings != null) Database.Settings.Open();
//        if (Database.FieldNotes != null) Database.FieldNotes.Open();
    }
}
