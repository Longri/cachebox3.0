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


import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import org.oscim.backend.GL;
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
public class LocationRenderer extends BucketRenderer implements Disposable {

    public static final Logger log = LoggerFactory.getLogger(LocationRenderer.class);
    private static final PointF CENTER_OFFSET = new PointF(0.5f, 0.5f);
    private static final long ANIM_RATE = 50;
    private static final long INTERVAL = 2000;

    private static final float CIRCLE_SIZE = 30;
    private static final int SHOW_ACCURACY_ZOOM = 13;

    private final SymbolBucket mSymbolBucket;
    private final float[] mBox = new float[8];
    private final Point mapPoint = new Point();
    private final Map mMap;
    private boolean mInitialized;
    private boolean mLocationIsVisible;
    private int mShaderProgram;
    private int hVertexPosition;
    private int hMatrixPosition;
    private int hScale;
    private int hPhase;
    private int hDirection;
    private double mRadius;

    private final Point mIndicatorPosition = new Point();

    private final Point mScreenPoint = new Point();
    private final Box mBBox = new Box();

    private boolean mRunAnim;
    private long mAnimStart;


    /**
     * flag to force update with location changed
     */
    private boolean mUpdate;
    private TextureRegion arrowRegion;
    private float arrowHeading;

    public void dispose() {

    }

    public LocationRenderer(Map map) {
        mSymbolBucket = new SymbolBucket();
        this.mMap = map;

    }


    @Override
    public synchronized void update(GLViewport v) {





        if (!v.changed() && !mUpdate) return;


        //accuracy

        if (!mInitialized) {
            init();
            mInitialized = true;
        }

        setReady(true);

        int width = mMap.getWidth();
        int height = mMap.getHeight();

        // clamp location to a position that can be
        // savely translated to screen coordinates
        v.getBBox(mBBox, 0);

        double x = mapPoint.x;
        double y = mapPoint.y;

        if (!mBBox.contains(mapPoint)) {
            x = FastMath.clamp(x, mBBox.xmin, mBBox.xmax);
            y = FastMath.clamp(y, mBBox.ymin, mBBox.ymax);
        }

        // get position of Location in pixel relative to
        // screen center
        v.toScreenPoint(x, y, mScreenPoint);

        x = mScreenPoint.x + width / 2;
        y = mScreenPoint.y + height / 2;

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

        mLocationIsVisible = (visible == 2);

        if (mLocationIsVisible) {
            animate(false);
        } else {
            animate(true);
        }
        // set location indicator position
        v.fromScreenPoint(x, y, mIndicatorPosition);



       //Texture
        mMapPosition.copy(v.pos);

        double mx = v.pos.x;
        double my = v.pos.y;
        double scale = Tile.SIZE * v.pos.scale;
        mMap.viewport().getMapExtents(mBox, 100);
        long flip = (long) (Tile.SIZE * v.pos.scale) >> 1;

        /* check visibility */
        float symbolX = (float) ((mapPoint.x - mx) * scale);
        float symbolY = (float) ((mapPoint.y - my) * scale);

        if (symbolX > flip)
            symbolX -= (flip << 1);
        else if (symbolX < -flip)
            symbolX += (flip << 1);
        buckets.clear();
        if (!GeometryUtils.pointInPoly(symbolX, symbolY, mBox, 8, 0)) {
            return;
        }

        mMapPosition.bearing = -mMapPosition.bearing;
        if (arrowRegion == null) return;
        SymbolItem symbolItem = SymbolItem.pool.get();
        symbolItem.set(symbolX, symbolY, arrowRegion, this.arrowHeading, true);
        symbolItem.offset = CENTER_OFFSET;
        mSymbolBucket.pushSymbol(symbolItem);

        buckets.set(mSymbolBucket);
        buckets.prepare();
        buckets.compile(true);
        compile();
        mUpdate = false;
    }

    public void update(double latitude, double longitude, float arrowHeading, double actAccuracy) {
        mUpdate = true;
        this.arrowHeading = -arrowHeading;
        while (this.arrowHeading < 0) this.arrowHeading += 360;
        mapPoint.x = (longitude + 180.0) / 360.0;
        double sinLatitude = Math.sin(latitude * (Math.PI / 180.0));
        mapPoint.y = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);
        log.debug("Set x: {} y: {} head: {}", mapPoint.x, mapPoint.y, arrowHeading);


        mRadius = actAccuracy;

    }

    private void animate(boolean enable) {
        if (mRunAnim == enable)
            return;

        mRunAnim = enable;
        if (!enable)
            return;

        final Runnable action = new Runnable() {
            private long lastRun;

            @Override
            public void run() {
                if (!mRunAnim)
                    return;

                long diff = System.currentTimeMillis() - lastRun;
                mMap.postDelayed(this, Math.min(ANIM_RATE, diff));
                mMap.render();
                lastRun = System.currentTimeMillis();
            }
        };

        mAnimStart = System.currentTimeMillis();
        mMap.postDelayed(action, ANIM_RATE);
    }

    private float animPhase() {
        return (float) ((MapRenderer.frametime - mAnimStart) % INTERVAL) / INTERVAL;
    }


    public void setTextureRegion(TextureRegion region) {
        arrowRegion = region;
    }

    private void init() {
        int shader = 0;

                shader = GLShader.createProgram(vShaderStr, fShaderStr3);

        if (shader == 0)
            return;

        mShaderProgram = shader;
        hVertexPosition = gl.getAttribLocation(shader, "a_pos");
        hMatrixPosition = gl.getUniformLocation(shader, "u_mvp");
        hPhase = gl.getUniformLocation(shader, "u_phase");
        hScale = gl.getUniformLocation(shader, "u_scale");
        hDirection = gl.getUniformLocation(shader, "u_dir");
    }

    public void render(GLViewport v) {
        renderAccuracyCircle(v);
        super.render(v);
    }

    public void renderAccuracyCircle(GLViewport v) {
        GLState.useProgram(mShaderProgram);
        GLState.blend(true);
        GLState.test(false, false);

        GLState.enableVertexArrays(hVertexPosition, -1);
        MapRenderer.bindQuadVertexVBO(hVertexPosition/*, true*/);

        float radius = 10;
        boolean viewShed = false;
        if (!mLocationIsVisible) {
            radius = CB.getScaledFloat(CIRCLE_SIZE);
        } else {
            if (v.pos.zoomLevel >= SHOW_ACCURACY_ZOOM) {
                radius = (float) (mRadius / MercatorProjection.groundResolution(v.pos));
            }
            radius = Math.max(CB.getScaledFloat(10), radius);
            viewShed = true;
        }
        gl.uniform1f(hScale, radius);

        double x = mIndicatorPosition.x - v.pos.x;
        double y = mIndicatorPosition.y - v.pos.y;
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

        if (viewShed && mLocationIsVisible) {
//            float rotation = 0;
//            if (mCallback != null)
//                rotation = mCallback.getRotation();
//            rotation -= 90;
            gl.uniform2f(hDirection,
                    (float) Math.cos(Math.toRadians(-90)),
                    (float) Math.sin(Math.toRadians(-90)));
        } else {
            gl.uniform2f(hDirection, 0, 0);
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
            + "uniform vec2 u_dir;"
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
            + "  if (u_dir.x == 0.0 && u_dir.y == 0.0){"
            + "  gl_FragColor = vec4(0.8, 0.2, 0.1, 0.8) * a;"
            + "  } else {"
            + "  gl_FragColor = vec4(0.2, 0.2, 0.8, 1.0) * a;"
            + "}}").replace("precision highp float;", isMac ? "" : "precision highp float;");

    public interface Callback {
        float getRotation();
    }

}
