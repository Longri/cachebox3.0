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
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLState;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.MapRenderer;
import org.oscim.scalebar.*;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;

import java.text.NumberFormat;

import static org.slf4j.impl.LibgdxLogger.DEFAULT_LOG_LEVEL_KEY;

public class CacheboxMain extends ApplicationAdapter {

    static {
        System.setProperty(DEFAULT_LOG_LEVEL_KEY, CB.USED_LOG_LEVEL);
        LibgdxLogger.init();
    }

    final static org.slf4j.Logger log = LoggerFactory.getLogger(CacheboxMain.class);

    Runtime runtime = Runtime.getRuntime();
    NumberFormat format = NumberFormat.getInstance();
    private String memoryUsage;


    Batch batch;
    protected int FpsInfoPos = 0;

    private Sprite FpsInfoSprite;
    private final Matrix4 NORMAL_MATRIX = new Matrix4().toNormalMatrix();
    public static boolean drawMap = false;
    public static CacheboxMapAdapter mMap;

    private MapRenderer mMapRenderer;
    private MapScaleBarLayer mapScaleBarLayer;


    @Override
    public void create() {

        mMap = new CacheboxMapAdapter();
        mMapRenderer = new MapRenderer(mMap);

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
                        StageManager.setMainStage(new ViewManager(CacheboxMain.this));
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
        initDefaultLayers(tileSource, true, true, true, true);
        mMap.setMapPosition(52.580400947530364, 13.385594096047232, 1 << 17);
    }

    protected void initDefaultLayers(TileSource tileSource, boolean tileGrid, boolean labels,
                                     boolean buildings, boolean mapScalebar) {
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

        if (mapScalebar) {
            DefaultMapScaleBar mapScaleBar = new DefaultMapScaleBar(mMap);
            mapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
            mapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
            mapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
            mapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

            mapScaleBarLayer = new MapScaleBarLayer(mMap, mapScaleBar);
            layers.add(mapScaleBarLayer);
        }
    }

    public void setMapScaleBarOffset(float xOffset, float yOffset) {
        BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
        renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
        renderer.setOffset(xOffset, yOffset);
    }


    @Override
    public void render() {

        {// calculate Memory Usage
            long maxMemory = runtime.maxMemory();
            long allocatedMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            StringBuilder memoryStringBuilder = new StringBuilder();
            memoryStringBuilder.append("f: " + format.format(freeMemory / 1048576));
            memoryStringBuilder.append("  a: " + format.format(allocatedMemory / 1048576));
            memoryStringBuilder.append("  m: " + format.format(maxMemory / 1048576));
            memoryStringBuilder.append("  tf: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1048576));
            memoryUsage = memoryStringBuilder.toString();
        }


        CB.stateTime += Gdx.graphics.getDeltaTime();

        if (drawMap) {
            GLState.enableVertexArrays(-1, -1);
            mMapRenderer.onDrawFrame();

            //release Buffers from map renderer
            GLState.bindVertexBuffer(0);
            GLState.bindElementBuffer(0);
        } else {
            // if Maprenderer not drawn, we must clear before draw stage
            Gdx.gl.glClearColor(CB.backgroundColor.r, CB.backgroundColor.g, CB.backgroundColor.b, CB.backgroundColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ?
                    GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        }

        StageManager.draw();

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

    public String getMemory() {
        return memoryUsage;
    }
}
