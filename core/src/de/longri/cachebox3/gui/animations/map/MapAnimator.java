/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.animations.map;

import com.badlogic.gdx.Gdx;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

/**
 * Created by Longri on 28.03.2017.
 */
public class MapAnimator {

    private final double POS_PRECISION = 1e-6;
    private final double SCALE_PRECISION = 1;
    private final double TILT_PRECISION = 1;
    private final double ROTATE_PRECISION = 1e-2;
    private final float DEFAULT_DURATION = 0.5f; // 500 ms

    private final Map map;
    private final DoubleAnimator mapX, mapY, scale, rotate, tilt;
    private final MapPosition mapPosition = new MapPosition();
    private boolean changed;

    public MapAnimator(Map map) {
        this.map = map;
        this.mapX = new DoubleAnimator();
        this.mapY = new DoubleAnimator();
        this.scale = new DoubleAnimator();
        this.rotate = new DoubleAnimator();
        this.tilt = new DoubleAnimator();
    }

    public void update(float delta) {
        map.viewport().getMapPosition(mapPosition);
        changed = false;
        if (mapX.update(delta)) {
            changed = true;
            mapPosition.setX(mapX.getAct());
        }
        if (mapY.update(delta)) {
            changed = true;
            mapPosition.setY(mapY.getAct());
        }
        if (scale.update(delta)) {
            changed = true;
            mapPosition.setScale(scale.getAct());
        }
        if (rotate.update(delta)) {
            changed = true;
            mapPosition.setBearing((float) -rotate.getAct());
        }
        if (tilt.update(delta)) {
            changed = true;
            mapPosition.setTilt((float) tilt.getAct());
        }
        if (changed) {
            map.setMapPosition(mapPosition);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Gdx.graphics.requestRendering();
                }
            });
        }
    }

    public void position(double x, double y) {
        this.position(DEFAULT_DURATION, x, y);
    }

    public void position(float duration, double x, double y) {
        this.mapX.start(duration, mapPosition.getX(), x, POS_PRECISION);
        this.mapY.start(duration, mapPosition.getY(), y, POS_PRECISION);
    }

    public void scale(double value) {
        this.scale(DEFAULT_DURATION, value);
    }

    public void scale(float duration, double value) {
        this.scale.start(duration, mapPosition.getScale(), value, SCALE_PRECISION);
    }

    public void rotate(double value) {
        this.rotate(DEFAULT_DURATION, value);
    }

    public void rotate(float duration, double value) {
        this.rotate.start(duration, mapPosition.getBearing(), value, ROTATE_PRECISION);
    }

    public void tilt(double value) {
        this.tilt(DEFAULT_DURATION, value);
    }

    public void tilt(float duration, double value) {
        this.tilt.start(duration, mapPosition.getTilt(), value, TILT_PRECISION);
    }
}
