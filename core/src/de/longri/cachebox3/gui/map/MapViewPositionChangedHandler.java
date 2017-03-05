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
package de.longri.cachebox3.gui.map;

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.gui.map.layer.LocationAccuracyLayer;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import de.longri.cachebox3.gui.widgets.MapCompass;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.locator.events.PositionChangedEvent;
import de.longri.cachebox3.locator.events.PositionChangedEventList;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.map.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 28.09.2016.
 */
public class MapViewPositionChangedHandler implements PositionChangedEvent {

    private static Logger log = LoggerFactory.getLogger(MapViewPositionChangedHandler.class);

    private float arrowHeading, accuracy, mapBearing, userBearing, tilt;
    private MapState mapState;
    private CoordinateGPS mapCenter;
    private CoordinateGPS myPosition;
    private final Map map;
    private final LocationAccuracyLayer myLocationAccuracy;
    private final LocationLayer myLocationLayer;
    private final MapCompass mapOrientationButton;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private Event lastEvent;

    public static MapViewPositionChangedHandler
    getInstance(Map map, LocationLayer myLocationLayer, LocationAccuracyLayer myLocationAccuracy
            , MapCompass mapOrientationButton) {
        MapViewPositionChangedHandler handler =
                new MapViewPositionChangedHandler(map, myLocationLayer, myLocationAccuracy, mapOrientationButton);

        //register this handler
        PositionChangedEventList.add(handler);
        return handler;
    }


    private MapViewPositionChangedHandler(Map map, LocationLayer myLocationLayer, LocationAccuracyLayer myLocationAccuracy
            , MapCompass mapOrientationButton) {
        this.map = map;
        this.myLocationLayer = myLocationLayer;
        this.myLocationAccuracy = myLocationAccuracy;
        this.mapOrientationButton = mapOrientationButton;
    }

    @Override
    public void positionChanged(Event event) {
        if (mapState == MapState.CAR && !Locator.isGPSprovided())
            return;// at CarMode ignore Network provided positions!

        this.myPosition = Locator.getCoordinate();


        if (getCenterGps())
            this.mapCenter = this.myPosition;


        this.accuracy = this.myPosition.getAccuracy();

        assumeValues(event);
    }

    @Override
    public void orientationChanged(Event event) {
        float bearing = -Locator.getHeading();

        // at CarMode no orientation changes below 20kmh
        if (mapState == MapState.CAR && Locator.SpeedOverGround() < 20)
            bearing = this.mapBearing;

        if (this.mapOrientationButton.isUserRotate()) {
            this.mapBearing = userBearing;
            this.arrowHeading = bearing;
        } else if (!this.mapOrientationButton.isNorthOriented() || mapState == MapState.CAR) {
            this.mapBearing = bearing;
            this.arrowHeading = 0;
        } else {
            this.mapBearing = 0;
            this.arrowHeading = bearing;
        }

        //set orientation
        this.mapOrientationButton.setOrientation(-this.mapBearing);
        assumeValues(event);
    }

    @Override
    public void speedChanged(Event event) {

    }

    @Override
    public String getReceiverName() {
        return "MapViewPositionChangedHandler";
    }

    @Override
    public Priority getPriority() {
        return Priority.High;
    }

    public void dispose() {

        isDisposed.set(true);

        // unregister this handler
        PositionChangedEventList.remove(this);

    }

    /**
     * Returns True, if MapState <br>
     * MapState.GPS<br>
     * MapState.LOCK<br>
     * MapState.CAR<br>
     *
     * @return Boolean
     */
    private boolean getCenterGps() {
        return this.mapState != MapState.FREE && this.mapState != MapState.WP;
    }

    private Timer timer;

    private double lastDynZoom;

    /**
     * Set the values to Map and position overlays
     */
    private void assumeValues(final Event event) {
        if (this.map.animator().isActive()) {
            if (timer != null) return;
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    assumeValues(event);
                }
            };
            timer.schedule(timerTask, 500);
            return;
        }
        timer = null;
        if (event != null && event == lastEvent) {
            // skip handling
            return;
        }
        lastEvent = event;

        if (isDisposed.get()) return;


        // set map values
        final MapPosition currentMapPosition = this.map.getMapPosition();
        this.tilt = currentMapPosition.tilt;
        if (this.mapCenter != null && getCenterGps())
            currentMapPosition.setPosition(this.mapCenter.latitude, this.mapCenter.longitude);

        // heading for map must between -180 and 180
        if (mapBearing < -180) mapBearing += 360;
        currentMapPosition.setBearing(mapBearing);
        currentMapPosition.setTilt(this.tilt);


        if (this.mapState == MapState.CAR && Settings_Map.dynamicZoom.getValue()) {
            // calculate dynamic Zoom

            double maxSpeed = Settings_Map.MoveMapCenterMaxSpeed.getValue();
            double maxZoom = 1 << Settings_Map.dynamicZoomLevelMax.getValue();
            double minZoom = 1 << Settings_Map.dynamicZoomLevelMin.getValue();

            double percent = Locator.SpeedOverGround() / maxSpeed;

            double dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
            if (dynZoom > maxZoom)
                dynZoom = maxZoom;
            if (dynZoom < minZoom)
                dynZoom = minZoom;

            currentMapPosition.setScale(dynZoom);
            if (lastDynZoom != (dynZoom)) {
                lastDynZoom = dynZoom;
                log.debug("SetDynamic scale to: " + lastDynZoom);
            }

        }


        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                map.animator().animateTo(500, currentMapPosition);
                myPosition = Locator.getCoordinate();
                myLocationAccuracy.setPosition(myPosition.latitude, myPosition.longitude, accuracy);
                myLocationLayer.setPosition(myPosition.latitude, myPosition.longitude, arrowHeading);
                map.updateMap(true);


                {// set yOffset at dependency of tilt
                    if (tilt > 0) {
                        //TODO do this at Map (Maybe tilt is animated)
                        float offset = MathUtils.linearInterpolation
                                (map.viewport().getMinTilt(), map.viewport().getMaxTilt(), 0, 0.8f, tilt);
                        map.viewport().setMapScreenCenter(offset);
                    } else {
                        map.viewport().setMapScreenCenter(0);
                    }
                }

                {// set mapOrientationButton tilt
                    if (tilt > 0) {
                        float buttonTilt = MathUtils.linearInterpolation
                                (map.viewport().getMinTilt(), map.viewport().getMaxTilt(), 0, -60f, tilt);
                        mapOrientationButton.setTilt(buttonTilt);
                    } else {
                        mapOrientationButton.setTilt(0);
                    }
                }
            }
        });
    }

    public void tiltChangedFromMap(float newTilt) {
        this.tilt = newTilt;
    }

    public void setMapState(MapState state) {
        mapState = state;
    }

    public void rotateChangedFromUser(float bearing) {
        this.mapOrientationButton.setUserRotation();
        userBearing = bearing;
    }


}
