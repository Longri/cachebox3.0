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
import de.longri.cachebox3.gui.stages.Splash;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.settings.Config;
import org.oscim.backend.GL;
import org.oscim.map.Map;
import org.oscim.renderer.GLState;
import org.oscim.renderer.MapRenderer;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;

import java.text.NumberFormat;

import static org.oscim.backend.GLAdapter.gl;
import static org.slf4j.impl.LibgdxLogger.DEFAULT_LOG_LEVEL_KEY;

public class CacheboxMain extends ApplicationAdapter {

    static {
        Map.NEW_GESTURES = true;
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

    // public CacheboxMapAdapter mMap;
    public MapRenderer mMapRenderer;


    @Override
    public void create() {

        Gdx.graphics.setContinuousRendering(false);

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


        CB.stateTime += Gdx.graphics.getDeltaTime();

        if (drawMap && mMapRenderer != null) {
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
