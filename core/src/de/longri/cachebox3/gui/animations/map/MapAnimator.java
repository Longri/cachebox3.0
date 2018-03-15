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
import de.longri.cachebox3.gui.map.MapViewPositionChangedHandler;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.animations.map.DoubleAnimator.DEFAULT_DURATION;

/**
 * Created by Longri on 28.03.2017.
 */
public class MapAnimator {

    private static final Logger log = LoggerFactory.getLogger(MapAnimator.class);

    private final Map map;
    private final DoubleAnimator mapX, mapY, scale, rotate, tilt, myPosX, myPosY;
    private final MapPosition mapPosition = new MapPosition();
    private float arrowHeading;
    private final MapViewPositionChangedHandler mapViewPositionChangedHandler;

    public MapAnimator(MapViewPositionChangedHandler mapViewPositionChangedHandler, Map map) {
        this.map = map;
        this.mapX = new DoubleAnimator();
        this.mapY = new DoubleAnimator();
        this.myPosX = new DoubleAnimator();
        this.myPosY = new DoubleAnimator();
        this.scale = new DoubleAnimator();
        this.rotate = new DoubleAnimator();
        this.tilt = new DoubleAnimator();
        this.mapViewPositionChangedHandler = mapViewPositionChangedHandler;
    }

    public void update(float delta) {
        map.viewport().getMapPosition(mapPosition);
        boolean changed = false;
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
            mapPosition.setBearing((float) rotate.getAct());
        }
        if (tilt.update(delta)) {
            changed = true;
            mapPosition.setTilt((float) tilt.getAct());
        }
        if (changed) {
            log.debug("setMapPosition Bearing {}", mapPosition.bearing);
            map.setMapPosition(mapPosition);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Gdx.graphics.requestRendering();
                }
            });
        }
    }

    public void animateToPos(double x, double y) {
        this.mapX.start(0.5f, map.getMapPosition().x, x);
        this.mapY.start(0.5f, map.getMapPosition().y, y);
        centerAnimation = true;
    }

    public void position(double x, double y) {
        this.position(DEFAULT_DURATION, x, y);
    }

    private boolean lastMapCenter = false;
    private boolean centerAnimation = false;

    public void position(float duration, double x, double y) {
        log.debug("new Position. Set ArrowHeading {}", arrowHeading);

        if (mapViewPositionChangedHandler.getCenterGps()) {
            if (centerAnimation) {
                if (mapX.isFinish()) {
                    centerAnimation = false;
                } else {
                    return;
                }
            }

            if (!lastMapCenter) {
                this.mapX.start(0.5f, map.getMapPosition().x, x);
                this.mapY.start(0.5f, map.getMapPosition().y, y);
                centerAnimation = true;
            } else {
                this.mapX.start(duration, mapPosition.getX(), x);
                this.mapY.start(duration, mapPosition.getY(), y);
            }
            lastMapCenter = true;
            this.myPosX.setAct(x);
            this.myPosY.setAct(y);
        } else {
            this.myPosX.start(duration, this.myPosX.getAct(), x);
            this.myPosY.start(duration, this.myPosY.getAct(), y);
            lastMapCenter = false;
        }
    }

    public void scale(double value) {
        this.scale(DEFAULT_DURATION, value);
    }

    public void scale(float duration, double value) {
        this.scale.start(duration, mapPosition.getScale(), value);
    }

    public void rotate(double value) {
        this.rotate(DEFAULT_DURATION, (float) value);
    }

    public void rotate(float duration, float value) {
        map.viewport().getMapPosition(mapPosition);
        float mr = mapPosition.bearing;

        if (mr == value) return;

        if (mr < 0) mr += 360;
        if (mr > 360) mr -= 360;

        float delta = (float) Math.abs(value - mr);

        while (delta > 270) {
            //Delta to big, rotate other direction"
            log.debug("Rotation delta to big   delta:{} to{} from {}", delta, value, mr);
            if (value - mr < 0) {
                value += 360;
            } else {
                value -= 360;
            }
            delta = (float) Math.abs(value - mr);
        }
        log.debug("Start rotate animation to: {}  from: {}", value, mr);
        this.rotate.start(duration, mr, value);
    }

    public void tilt(double value) {
        this.tilt(DEFAULT_DURATION, value);
    }

    private void tilt(float duration, double value) {
        this.tilt.start(duration, mapPosition.getTilt(), value);
    }

    public void setArrowHeading(float arrowHeading) {
        this.arrowHeading = arrowHeading;
    }
}
