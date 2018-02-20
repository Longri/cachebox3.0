/*
 * Copyright (C) 2016 -2017 team-cachebox.de
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

import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.location.*;
import de.longri.cachebox3.events.location.SpeedChangedListener;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.map.layer.LocationAccuracyLayer;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import de.longri.cachebox3.gui.map.layer.MapOrientationMode;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.Compass;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Settings_Map;
import org.oscim.core.MercatorProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 28.09.2016.
 */
public class MapViewPositionChangedHandler implements PositionChangedListener, SpeedChangedListener, OrientationChangedListener {

    private static Logger log = LoggerFactory.getLogger(MapViewPositionChangedHandler.class);
    private final MapInfoPanel infoPanel;

    private float arrowHeading, accuracy, mapBearing, userBearing, tilt;
    private Coordinate mapCenter;
    private Coordinate myPosition;
    private final CacheboxMapAdapter map;
    private final LocationAccuracyLayer myLocationAccuracy;
    private final LocationLayer myLocationLayer;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final MapStateButton mapStateButton;
    private final MapView mapView;

    public MapViewPositionChangedHandler(MapView mapView, CacheboxMapAdapter map, LocationLayer myLocationLayer,
                                         LocationAccuracyLayer myLocationAccuracy,
                                         MapStateButton mapStateButton, MapInfoPanel infoPanel) {
        this.map = map;
        this.myLocationLayer = myLocationLayer;
        this.myLocationAccuracy = myLocationAccuracy;
        this.mapStateButton = mapStateButton;
        this.infoPanel = infoPanel;
        this.infoPanel.setStateChangedListener(new Compass.StateChanged() {
            @Override
            public void stateChanged(MapOrientationMode state) {
                CB.viewmanager.toast("Change Map orientation Mode to:" + state.name());
                log.debug("AssumeValues CompassStateChanged  eventID:{}", "EventID-1");
                assumeValues(false, (short) (lastEventID - 1));
            }
        });
        this.mapView = mapView;
        de.longri.cachebox3.events.EventHandler.add(this);
    }

    public void dispose() {
        isDisposed.set(true);
        // unregister this handler
        de.longri.cachebox3.events.EventHandler.remove(this);
    }

    /**
     * Returns True, if MapMode <br>
     * MapMode.GPS<br>
     * MapMode.LOCK<br>
     * MapMode.CAR<br>
     *
     * @return Boolean
     */
    private boolean getCenterGps() {
        return CB.mapMode != MapMode.FREE && CB.mapMode != MapMode.WP;
    }

    private Timer timer;

    private double lastDynZoom;
    private short lastEventID = -1;

    /**
     * Set the values to Map and position overlays
     */
    private void assumeValues(boolean force, final short eventID) {

        if (lastEventID == eventID) return;
        lastEventID = eventID;

        if (!force && this.map.animator().isActive()) {
            if (timer != null) return;
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    log.debug("AssumeValues TimerTask  eventID:{}", eventID);
                    assumeValues(true, eventID);
                }
            };
            timer.schedule(timerTask, 500);
            return;
        }
        timer = null;


        if (isDisposed.get()) return;
        myPosition = EventHandler.getMyPosition();

        if (this.mapCenter != null && getCenterGps()) {
            mapView.animator.position(
                    MercatorProjection.longitudeToX(this.mapCenter.longitude),
                    MercatorProjection.latitudeToY(this.mapCenter.latitude)
            );
        }
        //force full tilt on CarMode
        if (CB.mapMode == MapMode.CAR)
            mapView.animator.tilt(map.viewport().getMaxTilt());

        if (CB.mapMode == MapMode.CAR /*&& Settings_Map.dynamicZoom.getValue()*/) {
            // calculate dynamic Zoom

            double maxSpeed = Settings_Map.MoveMapCenterMaxSpeed.getValue();
            double maxZoom = 1 << Settings_Map.dynamicZoomLevelMax.getValue();
            double minZoom = 1 << Settings_Map.dynamicZoomLevelMin.getValue();

            double percent = actSpeed / maxSpeed;

            double dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
            if (dynZoom > maxZoom)
                dynZoom = maxZoom;
            if (dynZoom < minZoom)
                dynZoom = minZoom;

            mapView.animator.scale(dynZoom);
            if (lastDynZoom != (dynZoom)) {
                lastDynZoom = dynZoom;
            }
        }

        float bearing = -EventHandler.getHeading();
        log.debug("Eventhandler bearing: {}", bearing);
        if (CB.mapMode == MapMode.CAR) {
            this.infoPanel.setMapOrientationMode(MapOrientationMode.COMPASS);
        }

        switch (this.infoPanel.getOrientationState()) {
            case NORTH:
                this.mapBearing = 0;
                this.arrowHeading = bearing;
                mapView.animator.rotate(mapBearing);
                break;
            case COMPASS:
                this.mapBearing = bearing;
                this.arrowHeading = 0;
                mapView.animator.rotate(mapBearing);
                break;
            case USER:
                this.mapBearing = userBearing;
                this.arrowHeading = userBearing + bearing;
                break;
        }
        log.debug("OrientationState {}| MapBearing {}| ArrowHeading {}", this.infoPanel.getOrientationState(), mapBearing, arrowHeading);

        infoPanel.setNewValues(myPosition, -mapBearing);
        if (myPosition != null) {
            myLocationAccuracy.setPosition(myPosition.latitude, myPosition.longitude, accuracy);
            myLocationLayer.setPosition(myPosition.latitude, myPosition.longitude, arrowHeading);
        }
        CB.requestRendering();
    }

    public void tiltChangedFromMap(float newTilt) {
        this.tilt = newTilt;
    }

    public void rotateChangedFromUser(float bearing) {
        userBearing = -bearing;
        this.infoPanel.setMapOrientationMode(MapOrientationMode.USER);
        log.debug("AssumeValues RotateChangeFromUser  eventID:{}", "EventID-1");
        assumeValues(false, (short) (lastEventID - 1));
    }

    public void setBearing(float bearing) {
        this.mapBearing = bearing;
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
//        if (CB.mapMode == MapMode.CAR && !event.pos.isGPSprovided())
//            return;// at CarMode ignore Network provided positions!
//
//        this.myPosition = event.pos;
//        if (getCenterGps())
//            this.mapCenter = this.myPosition;
//
//        this.accuracy = this.myPosition.getAccuracy();
//
//        if (CB.mapMode == MapMode.CAR) {
//            this.mapBearing = (float) event.pos.getHeading();
//            this.arrowHeading = 0;
//        }
//
//        this.actSpeed = (float) event.pos.getSpeed();
        log.debug("AssumeValues positionChanged Event  eventID:{}", event.ID);
        assumeValues(false, event.ID);
    }


    float actSpeed;

    @Override
    public void speedChanged(SpeedChangedEvent event) {
        actSpeed = event.speed;
        log.debug("AssumeValues SpeedChanged Event  eventID:{}", event.ID);
        assumeValues(false, event.ID);
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        // at CarMode no orientation changes below 20kmh
        if (CB.mapMode == MapMode.CAR)
            return;
        log.debug("AssumeValues orientationChanged Event eventID:{}", event.ID);
        assumeValues(false, event.ID);
    }

    public String toString() {
        return "MapViewPositionHandler";
    }
}
