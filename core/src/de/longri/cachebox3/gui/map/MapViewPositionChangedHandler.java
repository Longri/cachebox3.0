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

import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.map.layer.LocationAccuracyLayer;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.MapCompass;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.locator.CoordinateGPS;
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
public class MapViewPositionChangedHandler implements de.longri.cachebox3.events.PositionChangedListener, de.longri.cachebox3.events.SpeedChangedListener, de.longri.cachebox3.events.OrientationChangedListener {

    private static Logger log = LoggerFactory.getLogger(MapViewPositionChangedHandler.class);
    private final MapInfoPanel infoPanel;

    private float arrowHeading, accuracy, mapBearing, userBearing, tilt;
    private CoordinateGPS mapCenter;
    private CoordinateGPS myPosition;
    private final CacheboxMapAdapter map;
    private final LocationAccuracyLayer myLocationAccuracy;
    private final LocationLayer myLocationLayer;
    private final MapCompass mapOrientationButton;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final MapStateButton mapStateButton;
    private final MapView mapView;

    public MapViewPositionChangedHandler(MapView mapView, CacheboxMapAdapter map, LocationLayer myLocationLayer,
                                         LocationAccuracyLayer myLocationAccuracy, MapCompass mapOrientationButton,
                                         MapStateButton mapStateButton, MapInfoPanel infoPanel) {
        this.map = map;
        this.myLocationLayer = myLocationLayer;
        this.myLocationAccuracy = myLocationAccuracy;
        this.mapOrientationButton = mapOrientationButton;
        this.mapStateButton = mapStateButton;
        this.infoPanel = infoPanel;
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
        return this.mapStateButton.getMapMode() != MapMode.FREE && this.mapStateButton.getMapMode() != MapMode.WP;
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
                    assumeValues(true, eventID);
                }
            };
            timer.schedule(timerTask, 500);
            return;
        }
        timer = null;


        if (isDisposed.get()) return;
        myPosition = de.longri.cachebox3.events.EventHandler.getMyPosition();
        infoPanel.setNewValues(myPosition, mapBearing);

        // set map values
//        final MapPosition currentMapPosition = this.map.getMapPosition();
//        this.tilt = currentMapPosition.tilt;
        if (this.mapCenter != null && getCenterGps()) {
            mapView.animator.position(
                    MercatorProjection.longitudeToX(this.mapCenter.longitude),
                    MercatorProjection.latitudeToY(this.mapCenter.latitude)
            );
        }
        //force full tilt on CarMode
        if (this.mapStateButton.getMapMode() == MapMode.CAR)
            mapView.animator.tilt(map.viewport().getMaxTilt());


        // heading for map must between -180 and 180
        if (mapBearing < -180) mapBearing += 360;
        mapView.animator.rotate(mapBearing);


        if (this.mapStateButton.getMapMode() == MapMode.CAR && Settings_Map.dynamicZoom.getValue()) {
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
//                log.debug("SetDynamic scale to: " + lastDynZoom);
            }
        }

        myLocationAccuracy.setPosition(myPosition.latitude, myPosition.longitude, accuracy);
        myLocationLayer.setPosition(myPosition.latitude, myPosition.longitude, arrowHeading);
    }

    public void tiltChangedFromMap(float newTilt) {
        this.tilt = newTilt;
    }

    public void rotateChangedFromUser(float bearing) {
        this.mapOrientationButton.setUserRotation();
        userBearing = bearing;
    }

    public void setBearing(float bearing) {
        this.mapBearing = bearing;
    }

    @Override
    public void positionChanged(de.longri.cachebox3.events.PositionChangedEvent event) {
        if (this.mapStateButton.getMapMode() == MapMode.CAR && !event.pos.isGPSprovided())
            return;// at CarMode ignore Network provided positions!

        this.myPosition = event.pos;
        if (getCenterGps())
            this.mapCenter = this.myPosition;

        this.accuracy = this.myPosition.getAccuracy();

        if (this.mapStateButton.getMapMode() == MapMode.CAR) {
            this.mapBearing = (float) event.pos.getHeading();
            this.arrowHeading = 0;
        }

        this.actSpeed = (float) event.pos.getSpeed();
        assumeValues(false, event.ID);
    }


    float actSpeed;

    @Override
    public void speedChanged(de.longri.cachebox3.events.SpeedChangedEvent event) {
        actSpeed = event.speed;
        assumeValues(false, event.ID);
    }

    @Override
    public void orientationChanged(de.longri.cachebox3.events.OrientationChangedEvent event) {
        float bearing = -event.orientation;

        // at CarMode no orientation changes below 20kmh
        if (this.mapStateButton.getMapMode() == MapMode.CAR)
            return;

        if (this.mapOrientationButton.isUserRotate()) {
            this.mapBearing = userBearing;
            this.arrowHeading = bearing;
        } else if (!this.mapOrientationButton.isNorthOriented() || this.mapStateButton.getMapMode() == MapMode.CAR) {
            this.mapBearing = bearing;
            this.arrowHeading = 0;
        } else {
            this.mapBearing = 0;
            this.arrowHeading = bearing;
        }

        //set orientation
        this.mapOrientationButton.setOrientation(-this.mapBearing);
        assumeValues(false, event.ID);
    }

    public String toString() {
        return "MapViewPositionHandler";
    }
}
