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
package de.longri.cachebox3.gui.map.layer.renderer;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.GL;
import org.oscim.backend.canvas.Color;
import org.oscim.core.*;
import org.oscim.map.Map;
import org.oscim.renderer.*;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.SymbolBucket;
import org.oscim.renderer.bucket.SymbolItem;
import org.oscim.utils.FastMath;
import org.oscim.utils.geom.GeometryUtils;
import org.oscim.utils.math.Interpolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static org.oscim.backend.GLAdapter.gl;

/**
 * Created by Longri on 14.02.17
 */
public class LocationRenderer extends BucketRenderer {

    private static final Logger log = LoggerFactory.getLogger(LocationRenderer.class);
    private static final PointF CENTER_OFFSET = new PointF(0.5f, 0.5f);
    private static final long ANIM_RATE = 50;
    private static final long INTERVAL = 2000;
    private static final float CIRCLE_SIZE = 30;
    private static final int SHOW_ACCURACY_ZOOM = 13;


    private int accuracyColor = Color.BLUE;
    private int viewShedColor = Color.RED;
    private final SymbolBucket symbolBucket;
    private final float[] box = new float[8];
    private final Point mapPoint = new Point();
    private final Map map;
    private boolean initialized;
    private boolean locationIsVisible;
    private int shaderProgramNumber;
    private int hVertexPosition;
    private int hMatrixPosition;
    private int hScale;
    private int hPhase;
    private int uFill;
    private double accuracyRadius;

    private final Point indicatorPosition = new Point();
    private final Point screenPoint = new Point();
    private final Box boundingBox = new Box();
    private boolean runAnim;
    private long animStart;
    private boolean update;
    private TextureRegion arrowRegion;
    private float arrowHeading;

    public LocationRenderer(Map map) {
        symbolBucket = new SymbolBucket();
        this.map = map;
    }


    @Override
    public synchronized void update(GLViewport v) {
        if (!v.changed() && !update) return;


        //accuracy

        if (!initialized) {
            init();
            initialized = true;
        }

        setReady(true);

        int width = map.getWidth();
        int height = map.getHeight();
        v.getBBox(boundingBox, 0);

        double x = mapPoint.x;
        double y = mapPoint.y;

        if (!boundingBox.contains(mapPoint)) {
            x = FastMath.clamp(x, boundingBox.xmin, boundingBox.xmax);
            y = FastMath.clamp(y, boundingBox.ymin, boundingBox.ymax);
        }

        // get position of Location in pixel relative to
        // screen center
        v.toScreenPoint(x, y, screenPoint);

        x = screenPoint.x + width / 2;
        y = screenPoint.y + height / 2;

        // clip position to screen boundaries
        int visible = 0;

        if (x > width - 5)
            x = width;
        else if (x < 5)
            x = 0;
        else
            visible++;

        if (y > height - 5)
            y = height;
        else if (y < 5)
            y = 0;
        else
            visible++;

        locationIsVisible = (visible == 2);

        if (locationIsVisible) {
            animate(false);
        } else {
            animate(true);
        }
        // set location indicator position
        v.fromScreenPoint(x, y, indicatorPosition);


        //Texture
        mMapPosition.copy(v.pos);

        double mx = v.pos.x;
        double my = v.pos.y;
        double scale = Tile.SIZE * v.pos.scale;
        map.viewport().getMapExtents(box, 100);
        long flip = (long) (Tile.SIZE * v.pos.scale) >> 1;

        /* check visibility */
        float symbolX = (float) ((mapPoint.x - mx) * scale);
        float symbolY = (float) ((mapPoint.y - my) * scale);

        if (symbolX > flip)
            symbolX -= (flip << 1);
        else if (symbolX < -flip)
            symbolX += (flip << 1);
        buckets.clear();
        if (!GeometryUtils.pointInPoly(symbolX, symbolY, box, 8, 0)) {
            return;
        }

        mMapPosition.bearing = -mMapPosition.bearing;
        if (arrowRegion == null) return;
        SymbolItem symbolItem = SymbolItem.pool.get();
        symbolItem.set(symbolX, symbolY, arrowRegion, this.arrowHeading, true);
        symbolItem.offset = CENTER_OFFSET;
        symbolBucket.pushSymbol(symbolItem);

        buckets.set(symbolBucket);
        buckets.prepare();
        buckets.compile(true);
        compile();
        update = false;
    }

    public void update(double latitude, double longitude, float arrowHeading, double actAccuracy) {
        update = true;
        this.arrowHeading = -arrowHeading;
        while (this.arrowHeading < 0) this.arrowHeading += 360;
        mapPoint.x = (longitude + 180.0) / 360.0;
        double sinLatitude = Math.sin(latitude * (Math.PI / 180.0));
        mapPoint.y = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);
        log.debug("Set x: {} y: {} head: {}", mapPoint.x, mapPoint.y, arrowHeading);


        accuracyRadius = actAccuracy;

    }

    private void animate(boolean enable) {
        if (runAnim == enable)
            return;

        runAnim = enable;
        if (!enable)
            return;

        final Runnable action = new Runnable() {
            private long lastRun;

            @Override
            public void run() {
                if (!runAnim)
                    return;

                long diff = System.currentTimeMillis() - lastRun;
                map.postDelayed(this, Math.min(ANIM_RATE, diff));
                map.render();
                lastRun = System.currentTimeMillis();
            }
        };

        animStart = System.currentTimeMillis();
        map.postDelayed(action, ANIM_RATE);
    }

    private float animPhase() {
        return (float) ((MapRenderer.frametime - animStart) % INTERVAL) / INTERVAL;
    }


    public void setTextureRegion(TextureRegion region) {
        arrowRegion = region;
    }

    private void init() {
        int shader = GLShader.createProgram(vShaderStr, fShaderStr3);
        if (shader == 0)
            return;

        shaderProgramNumber = shader;
        hVertexPosition = gl.getAttribLocation(shader, "a_pos");
        hMatrixPosition = gl.getUniformLocation(shader, "u_mvp");
        hPhase = gl.getUniformLocation(shader, "u_phase");
        hScale = gl.getUniformLocation(shader, "u_scale");
        uFill = gl.getUniformLocation(shader, "u_fill");
    }

    public void render(GLViewport v) {
        renderAccuracyCircle(v);
        super.render(v);
    }

    private void renderAccuracyCircle(GLViewport v) {
        GLState.useProgram(shaderProgramNumber);
        GLState.blend(true);
        GLState.test(false, false);

        GLState.enableVertexArrays(hVertexPosition, -1);
        MapRenderer.bindQuadVertexVBO(hVertexPosition/*, true*/);

        float radius = 10;
        boolean viewShed = false;
        if (!locationIsVisible) {
            radius = CIRCLE_SIZE * CanvasAdapter.getScale();
        } else {
            if (v.pos.zoomLevel >= SHOW_ACCURACY_ZOOM) {
                radius = (float) (accuracyRadius / MercatorProjection.groundResolution(v.pos));
            }
            radius = Math.max(2, radius);
            viewShed = true;
        }
        gl.uniform1f(hScale, radius);

        double x = indicatorPosition.x - v.pos.x;
        double y = indicatorPosition.y - v.pos.y;
        double tileScale = Tile.SIZE * v.pos.scale;

        v.mvp.setTransScale((float) (x * tileScale), (float) (y * tileScale), 1);
        v.mvp.multiplyMM(v.viewproj, v.mvp);
        v.mvp.setAsUniform(hMatrixPosition);

        if (!viewShed) {
            float phase = Math.abs(animPhase() - 0.5f) * 2;
            //phase = Interpolation.fade.apply(phase);
            phase = Interpolation.swing.apply(phase);
            gl.uniform1f(hPhase, 0.8f + phase * 0.2f);
        } else {
            gl.uniform1f(hPhase, 1);
        }

        if (viewShed && locationIsVisible) {
            GLUtils.setColor(uFill, accuracyColor, 1);
        } else {
            GLUtils.setColor(uFill, viewShedColor, 1);
        }

        gl.drawArrays(GL.TRIANGLE_STRIP, 0, 4);
        gl.flush();
    }


    private final static boolean isMac = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("mac");

    private final static String vShaderStr = (""
            + "precision highp float;"
            + "uniform mat4 u_mvp;"
            + "uniform float u_phase;"
            + "uniform float u_scale;"
            + "attribute vec2 a_pos;"
            + "varying vec2 v_tex;"
            + "void main() {"
            + "  gl_Position = u_mvp * vec4(a_pos * u_scale * u_phase, 0.0, 1.0);"
            + "  v_tex = a_pos;"
            + "}").replace("precision highp float;", isMac ? "" : "precision highp float;");

    // only circle without direction
    private static final String fShaderStr3 = (""
            + "precision highp float;"
            + "varying vec2 v_tex;"
            + "uniform float u_scale;"
            + "uniform float u_phase;"
            + "uniform vec4 u_fill;"
            + "void main() {"
            + "  float len = 1.0 - length(v_tex);"
            ///  outer ring
            + "  float a = smoothstep(0.0, 2.0 / u_scale, len);"
            ///  inner ring
            + "  float b = 0.8 * smoothstep(3.0 / u_scale, 4.0 / u_scale, len);"
            ///  center point
            + "  float c = 0.5 * (1.0 - smoothstep(14.0 / u_scale, 16.0 / u_scale, 1.0 - len));"
            + "  vec2 dir = normalize(v_tex);"
            ///  - subtract inner from outer to create the outline
            ///  - multiply by viewshed
            ///  - add center point
            + "  a = (a - (b + c)) + c;"
            + "  gl_FragColor = u_fill * a;"
            + "}").replace("precision highp float;", isMac ? "" : "precision highp float;");

}
