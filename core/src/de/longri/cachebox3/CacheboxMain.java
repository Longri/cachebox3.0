/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.stages.Splash;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import org.oscim.backend.GL;
import org.oscim.renderer.GLState;
import org.oscim.renderer.MapRenderer;
import org.oscim.utils.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;

import static org.oscim.backend.GLAdapter.gl;
import static org.oscim.renderer.MapRenderer.COORD_SCALE;

public class CacheboxMain extends ApplicationAdapter {

    static {
        Parameters.MAP_EVENT_LAYER2 = true;
        Parameters.TEXTURE_ATLAS = true;
        Parameters.POT_TEXTURES = true;
        COORD_SCALE = 1;
        EventHandler.INIT();

//        INCLUDE_LIST.add("de.longri.cachebox3.gui.animations.map.MapAnimator");
//        INCLUDE_LIST.add("de.longri.cachebox3.gui.stages.ViewManager");
//        INCLUDE_LIST.add("de.longri.cachebox3.gui.stages.StageManager");

//        INCLUDE_LIST.add("de.longri.cachebox3.gui.stages.ViewManager");
//        INCLUDE_LIST.add("de.longri.cachebox3.gui.widgets.filter_settings.FilterSetListView");
//        INCLUDE_LIST.add(BlockUiProgress_Activity.class.getName());
//        INCLUDE_LIST.add(CircularProgressWidget.class.getName());


//        EXCLUDE_LIST.add("de.longri.cachebox3.gui.animations.map.MapAnimator");
//        EXCLUDE_LIST.add("de.longri.cachebox3.events.GpsEventHelper");
//        EXCLUDE_LIST.add("de.longri.cachebox3.gui.map.MapViewPositionChangedHandler");
//
//        EXCLUDE_LIST.add("com.badlogic.gdx.sqlite.desktop.DesktopDatabase");
//        EXCLUDE_LIST.add("com.badlogic.gdx.sqlite.android.AndroidDatabase");
//        EXCLUDE_LIST.add("com.badlogic.gdx.sqlite.robovm.RobovmDatabase");
//
//        EXCLUDE_LIST.add(LocationAccuracyLayer.class.getName());
//        EXCLUDE_LIST.add(LocationRenderer.class.getName());
//        EXCLUDE_LIST.add(DoubleAnimator.class.getName());
//        EXCLUDE_LIST.add(GpsEventHelper.class.getName());
//        EXCLUDE_LIST.add(DirectLineLayer.class.getName());
//        EXCLUDE_LIST.add(EventHandler.class.getName());
//        EXCLUDE_LIST.add(CB.class.getName());


    }

    static Logger log = LoggerFactory.getLogger(CacheboxMain.class);

    Runtime runtime = Runtime.getRuntime();
    NumberFormat format = NumberFormat.getInstance();
    private String memoryUsage;
    private ViewManager viewManager;


    Batch batch;
    protected int FpsInfoPos = 0;

    protected Sprite FpsInfoSprite;
    public static boolean drawMap = false;

    // public CacheboxMapAdapter mMap;
    public MapRenderer mMapRenderer;


    @Override
    public void create() {

        Gdx.graphics.setContinuousRendering(false);

        final Viewport viewport = new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera());
        final Batch batch = new SpriteBatch();

        final Splash splash = new Splash(new Splash.LoadReady() {
            @Override
            public void ready() {
                Config.AppRaterlaunchCount.setValue(Config.AppRaterlaunchCount.getValue() + 1);
                Config.AcceptChanges();

                // Splash is ready with initialisation
                // now switch Stage to ViewManager
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {

                        viewManager = new ViewManager(
                                CacheboxMain.this, StageManager.viewport, StageManager.batch);

                        StageManager.setMainStage(viewManager);
                        batch.dispose();
                    }
                });
            }
        }, viewport, batch);

        StageManager.setMainStage(splash);

        Gdx.graphics.requestRendering();
        CB.initThreadCheck();
    }


    int mapDrawX, mapDrawY, mapDrawWidth, mapDrawHeight;


    public void setMapPosAndSize(int x, int y, int width, int height) {
        mapDrawX = x;
        mapDrawY = y;
        mapDrawWidth = width;
        mapDrawHeight = height;
        mMapRenderer.onSurfaceChanged(width, height);
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

        if (CB.isBackground) return;

        CB.stateTime += Gdx.graphics.getDeltaTime();

        if (drawMap && mMapRenderer != null && StageManager.isMainStageOnlyDrawing()) {
            GLState.enableVertexArrays(-1, -1);

            // set map position and size
            gl.viewport(mapDrawX, mapDrawY, mapDrawWidth, mapDrawHeight);
            gl.frontFace(GL.CW);

            try {
                mMapRenderer.onDrawFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }


            //release Buffers from map renderer
            GLState.bindVertexBuffer(0);
            GLState.bindElementBuffer(0);
        } else {
            // if MapRenderer not drawn, we must clear before draw stage
            Gdx.gl.glClearColor(CB.backgroundColor.r, CB.backgroundColor.g, CB.backgroundColor.b, CB.backgroundColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ?
                    GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        }

        gl.flush();
        gl.viewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // MapRender sets the FrontFace to GL.CW, so we revert to GL.CCW
        gl.frontFace(GL.CCW);


        try {
            StageManager.draw();
        } catch (Exception e) {
            log.error("Draw StageManager", e);
        }

        if (CB.isTestVersion()) {
            float FpsInfoSize = CB.getScaledFloat(4f);
            if (FpsInfoSprite != null) {
                batch = StageManager.getBatch();
                if (!batch.isDrawing())
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
        viewManager.dispose();
        batch.dispose();
    }

    @Override
    public void pause() {
        if (viewManager != null) viewManager.pause();
        checkLogger();

        if (EventHandler.getSelectedCache() != null) {
            //save selected Cache
            Config.LastSelectedCache.setValue(EventHandler.getSelectedCache().getGcCode().toString());
            Config.AcceptChanges();
            log.debug("Store LastSelectedCache = " + EventHandler.getSelectedCache().getGcCode());
        }

        log.debug("App on pause close databases");
        if (Database.Data != null) Database.Data.close();
        if (Database.Settings != null) Database.Settings.close();
        if (Database.Drafts != null) Database.Drafts.close();
        CB.isBackground = true;
    }

    @Override
    public void resume() {
        if (viewManager != null) viewManager.resume();
        checkLogger();
        log.debug("App on resume reopen databases");
        if (Database.Data != null) Database.Data.open();
        if (Database.Settings != null) Database.Settings.open();
        if (Database.Drafts != null) Database.Drafts.open();
        CB.isBackground = false;
    }

    public String getMemory() {
        return memoryUsage;
    }

    private static void checkLogger() {
        if (log == null) {
            log = LoggerFactory.getLogger(CacheboxMain.class);
        }
    }
}
