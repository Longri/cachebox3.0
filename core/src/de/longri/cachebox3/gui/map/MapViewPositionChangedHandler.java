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
import de.longri.cachebox3.events.SelectedCoordChangedEvent;
import de.longri.cachebox3.events.SelectedCoordChangedListener;
import de.longri.cachebox3.events.location.*;
import de.longri.cachebox3.gui.CacheboxMapAdapter;
import de.longri.cachebox3.gui.animations.map.MapAnimator;
import de.longri.cachebox3.gui.animations.map.MyPositionAnimator;
import de.longri.cachebox3.gui.map.layer.DirectLineLayer;
import de.longri.cachebox3.gui.map.layer.LocationAccuracyLayer;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import de.longri.cachebox3.gui.map.layer.MapOrientationMode;
import de.longri.cachebox3.gui.widgets.Compass;
import de.longri.cachebox3.gui.widgets.MapInfoPanel;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.utils.IChanged;
import org.oscim.core.MercatorProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 28.09.2016.
 */
public class MapViewPositionChangedHandler implements SelectedCoordChangedListener, PositionChangedListener, SpeedChangedListener, OrientationChangedListener {

    private static Logger log = LoggerFactory.getLogger(MapViewPositionChangedHandler.class);
    private final MapInfoPanel infoPanel;
    private final MapAnimator mapAnimator;
    private final MyPositionAnimator myPositionAnimator;

    private float arrowHeading, mapBearing, userBearing, tilt;
    private Coordinate mapCenter;
    private Coordinate myPosition;
    private final CacheboxMapAdapter map;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private double lastDynZoom;
    private short lastEventID = -1;
    private double maxSpeed, maxZoom, minZoom;
    private boolean dynZoomEnabled;
    private float lastBearing = -1;

    public MapViewPositionChangedHandler(CacheboxMapAdapter map, DirectLineLayer directLineLayer, LocationLayer myLocationLayer,
                                         LocationAccuracyLayer myLocationAccuracy,
                                         MapInfoPanel infoPanel) {
        this.map = map;
        this.infoPanel = infoPanel;
        this.infoPanel.setStateChangedListener(new Compass.StateChanged() {
            @Override
            public void stateChanged(MapOrientationMode state) {
                CB.viewmanager.toast("Change Map orientation Mode to:" + state.name());
                log.debug("AssumeValues CompassStateChanged  eventID:{}", "EventID-1");
                assumeValues(false, (short) (lastEventID - 1));
            }
        });
        this.mapAnimator = new MapAnimator(this, map);
        this.myPositionAnimator = new MyPositionAnimator(directLineLayer, myLocationLayer, myLocationAccuracy);
        dynZoomEnabled = Settings_Map.dynamicZoom.getValue();
        maxSpeed = Settings_Map.MoveMapCenterMaxSpeed.getValue();
        maxZoom = 1 << Settings_Map.dynamicZoomLevelMax.getValue();
        minZoom = 1 << Settings_Map.dynamicZoomLevelMin.getValue();
        IChanged settingChangeHandler = new IChanged() {
            @Override
            public void isChanged() {
                dynZoomEnabled = Settings_Map.dynamicZoom.getValue();
                maxSpeed = Settings_Map.MoveMapCenterMaxSpeed.getValue();
                maxZoom = 1 << Settings_Map.dynamicZoomLevelMax.getValue();
                minZoom = 1 << Settings_Map.dynamicZoomLevelMin.getValue();
            }
        };
        Settings_Map.dynamicZoom.addChangedEventListener(settingChangeHandler);
        Settings_Map.MoveMapCenterMaxSpeed.addChangedEventListener(settingChangeHandler);
        Settings_Map.dynamicZoomLevelMax.addChangedEventListener(settingChangeHandler);
        Settings_Map.dynamicZoomLevelMin.addChangedEventListener(settingChangeHandler);
        de.longri.cachebox3.events.EventHandler.add(this);
        assumeValues(false, EventHandler.getId());
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
    public boolean getCenterGps() {
        return CB.mapMode == null || CB.mapMode == MapMode.GPS || CB.mapMode == MapMode.CAR || CB.mapMode == MapMode.LOCK;
    }

    /**
     * Set the values to Map and position overlays
     */
    private void assumeValues(boolean force, final short eventID) {

        try {
            if (lastEventID == eventID) {
                return;
            }
            lastEventID = eventID;
            if (isDisposed.get()) return;

            double lat, lon;
            if (getCenterGps()) {
                if (this.mapCenter == null) {
                    this.mapCenter = EventHandler.getMyPosition();
                }
                lon = this.mapCenter.longitude;
                lat = this.mapCenter.latitude;
            } else {
                if (this.myPosition == null) {
                    this.myPosition = EventHandler.getMyPosition();
                }
                lon = this.myPosition.longitude;
                lat = this.myPosition.latitude;
            }
            mapAnimator.position(
                    MercatorProjection.longitudeToX(lon),
                    MercatorProjection.latitudeToY(lat)
            );
            myPositionAnimator.setPosition(lat, lon);

            //force full tilt on CarMode
            if (CB.mapMode == MapMode.CAR)
                mapAnimator.tilt(map.viewport().getMaxTilt());


            if (dynZoomEnabled && CB.mapMode == MapMode.CAR) {
                // calculate dynamic Zoom
                double percent = actSpeed / maxSpeed;
                double dynZoom = (float) (maxZoom - ((maxZoom - minZoom) * percent));
                if (dynZoom > maxZoom)
                    dynZoom = maxZoom;
                if (dynZoom < minZoom)
                    dynZoom = minZoom;

                if (lastDynZoom != (dynZoom)) {
                    lastDynZoom = dynZoom;
                    log.debug("Set new dynZoom: speed: {}  percent: {}  zoom: {}", actSpeed, percent, dynZoom);
                    mapAnimator.scale(2.0f, dynZoom);
                }
            }

            float bearing = -EventHandler.getHeading();
            if (CB.mapMode == MapMode.CAR) {
                this.infoPanel.setMapOrientationMode(MapOrientationMode.COMPASS);
                //change bearing only with speed over 10 kmh
                if (actSpeed < 10 && lastBearing >= 0) {
                    bearing = lastBearing;
                }
            }
            lastBearing = bearing;

            switch (this.infoPanel.getOrientationState()) {
                case NORTH:
                    this.mapBearing = 0;
                    this.arrowHeading = bearing;
                    mapAnimator.rotate(mapBearing);
                    break;
                case COMPASS:
                    this.mapBearing = bearing;
                    this.arrowHeading = 0;
                    mapAnimator.rotate(mapBearing);
                    break;
                case USER:
                    this.mapBearing = userBearing;
                    this.arrowHeading = userBearing + bearing;
                    break;
            }
            mapAnimator.setArrowHeading(arrowHeading);
            myPositionAnimator.setArrowHeading(arrowHeading);
            infoPanel.setNewValues(myPosition, -mapBearing);
            CB.requestRendering();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        this.myPosition = event.pos;
        if (getCenterGps())
            this.mapCenter = this.myPosition;
        assumeValues(false, event.ID);
    }


    private float actSpeed;

    @Override
    public void speedChanged(SpeedChangedEvent event) {
        actSpeed = event.speed;
        assumeValues(false, event.ID);
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        if (CB.mapMode == MapMode.CAR) {
            this.mapBearing = event.getOrientation();
            this.arrowHeading = 0;
        }
        assumeValues(false, event.ID);
    }

    public String toString() {
        return "MapViewPositionHandler";
    }

    public void update(float deltaTime) {
        mapAnimator.update(deltaTime);
        myPositionAnimator.update(deltaTime);
    }

    public void scale(double scale) {
        mapAnimator.scale(scale);
    }

    public void rotate(float rotate) {
        mapAnimator.rotate(rotate);
    }

    public void position(double x, double y) {
        mapAnimator.position(x, y);
    }

    public void animateToPos(double x, double y) {
        mapAnimator.animateToPos(x, y);
    }

    @Override
    public void selectedCoordChanged(SelectedCoordChangedEvent event) {
        assumeValues(false, event.ID);
    }
}
