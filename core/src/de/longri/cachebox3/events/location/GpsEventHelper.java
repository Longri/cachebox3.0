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
package de.longri.cachebox3.events.location;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.locator.Region;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.LowpassFilter;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.03.2017.
 */
public class GpsEventHelper implements LocationEvents {

    private static Logger log = LoggerFactory.getLogger(GpsEventHelper.class);

    private GpsState gpsState = GpsState.UNKNOWN;
    private final LowpassFilter lowpassFilterCompass = new LowpassFilter(CanvasAdapter.platform == Platform.IOS ? 0 : 20);
    private final LowpassFilter lowpassFilterGPS = new LowpassFilter(2);
    private final LowpassFilter pitchLowpassFilter = new LowpassFilter(50);

    double lastGpsLat, lastGpsLon, lastNetLat, lastNetLon;
    private float lastGpsAccuracy;
    private double lastGpsElevation;
    private double lastCompassHeading, lastGpsHeading;
    private double lastSpeed;
    private float accuracy;
    private boolean useCompassOnly;
    private float compassLevel;
    private int lastLowpassValue = 0;

    public void init() {
        useCompassOnly = Config.HardwareCompassOnly.getValue();
        compassLevel = Config.HardwareCompassLevel.getValue();
        Config.HardwareCompassOnly.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                useCompassOnly = Config.HardwareCompassOnly.getValue();
            }
        });
        Config.HardwareCompassLevel.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                compassLevel = Config.HardwareCompassLevel.getValue();
            }
        });
    }

    @Override
    public void newGpsPos(double latitude, double longitude, float accuracy) {
        CB.sensoerIO.write_newGpsPos(latitude, longitude, accuracy);

        // clamp coordinate to handled precision
        latitude = ((int) (latitude * 1E6)) / 1E6;
        longitude = ((int) (longitude * 1E6)) / 1E6;

        if (lastGpsLat != latitude || lastGpsLon != longitude) {
            lastGpsLat = latitude;
            lastGpsLon = longitude;

            //fire pos changed event
            EventHandler.fire(new PositionChangedEvent(new Coordinate(latitude, longitude), true, EventHandler.getId()));
        }
    }

    @Override
    public void newNetworkPos(double latitude, double longitude, float accuracy) {
        CB.sensoerIO.write_newNetworkPos(latitude, longitude, accuracy);
        // clamp coordinate to handled precision
        latitude = ((int) (latitude * 1E6)) / 1E6;
        longitude = ((int) (longitude * 1E6)) / 1E6;
    }

    @Override
    public void newAltitude(double altitude) {
        CB.sensoerIO.write_newAltitude(altitude);
    }

    /**
     * @param bearing as radians
     * @param gps     is from GPS
     */
    @Override
    public void newBearing(float bearing, boolean gps) {
        log.debug("new Bearing {} ({})  GPS:{}", bearing, Math.toDegrees(bearing), gps);

        if (gps) {
            CB.sensoerIO.write_newBearingGPS(bearing);
            float value = lowpassFilterGPS.add(bearing);
            if (lastGpsHeading != value) {
                this.lastGpsHeading = value;
            }
        } else {
            CB.sensoerIO.write_newBearingCompass(bearing);
            float value;
            if (CanvasAdapter.platform == Platform.IOS) {
                value = (float) Math.toDegrees(bearing);
            } else {
                value = lowpassFilterCompass.add(bearing);
            }
            if (lastCompassHeading != value) {
                this.lastCompassHeading = value;
            }
        }

        if (!useCompassOnly && (lastSpeed > compassLevel || CB.mapMode == MapMode.CAR)) {
            EventHandler.fire(new OrientationChangedEvent((float) lastGpsHeading));
            log.debug("fire GPS heading event {} (rad:{}) ", lastGpsHeading, Math.toRadians(lastGpsHeading));
        } else {
            EventHandler.fire(new OrientationChangedEvent((float) lastCompassHeading));
            log.debug("fire Compass heading event {} (rad:{})", lastCompassHeading, Math.toRadians(lastCompassHeading));
        }
    }

    public void newPitch(float pitch) {
        if (CanvasAdapter.platform == Platform.IOS) return;
        CB.sensoerIO.write_newPitch(pitch);
        pitch = pitchLowpassFilter.add(pitch);
        int pitchInt = Math.round(pitch);
        int lowPassValue = (int) Math.round(
                ((int) MathUtils.linearInterpolation(0, 90, 20, 500, Math.abs(pitchInt)))
                        / 100.0) * 100;

        if (lowPassValue < 100) lowPassValue = 20;
        if (lowPassValue >= 100) lowPassValue = 200;

        if (lastLowpassValue != lowPassValue) {
            lastLowpassValue = lowPassValue;
            lowpassFilterCompass.changeSmoothValue(lastLowpassValue);
            log.debug("change LowpassValue to {}", lowPassValue);
        }
    }

    public void newRoll(float roll) {
        CB.sensoerIO.write_newRoll(roll);
    }

    @Override
    public void didEnterRegion(Region region) {
        // todo write to SensorIO
    }

    @Override
    public void didExitRegion(Region region) {
        // todo write to SensorIO
    }

//    public void newAccuracy(float accuracy) {
//        CB.sensoerIO.write_newAccuracy(accuracy);
//        this.accuracy = accuracy;
//    }

    @Override
    public void newSpeed(double speed) {
        CB.sensoerIO.write_newSpeed(speed);
        if (lastSpeed != speed) {
            lastSpeed = speed;
            EventHandler.fire(new SpeedChangedEvent((float) speed));
        }
    }

    public void gpsStateChanged(GpsState state) {

        this.gpsState = state;
//        log.debug("Gps state changed to {}", state);
    }

    public CoordinateGPS getLastGpsCoordinate() {
        CoordinateGPS coord = new CoordinateGPS(this.lastGpsLat, this.lastGpsLon);
        coord.setAccuracy(this.lastGpsAccuracy);
        coord.setElevation(this.lastGpsElevation);
        coord.setHeading(this.lastCompassHeading);
        coord.setSpeed(this.lastSpeed);
        return coord;
    }


    public float getAccuracy() {
        return this.accuracy;
    }
}
