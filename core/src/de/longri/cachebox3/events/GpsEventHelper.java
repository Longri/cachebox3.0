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
package de.longri.cachebox3.events;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.locator.CoordinateGPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.03.2017.
 */
public class GpsEventHelper {

    private static Logger log = LoggerFactory.getLogger(GpsEventHelper.class);

    private double lastLat, lastLon, lastSpeed, lastEle, lastHeading;
    private float lastAccuracy;

    public void newGpsPos(double lat, double lon, boolean isGpsProvided, double elevation,
                          double speed, double bearing, float accuracy) {

        // clamp coordinate to handled precision
        lat = ((int) (lat * 1E6)) / 1E6;
        lon = ((int) (lon * 1E6)) / 1E6;

        short eventID = EventHandler.getId();

        if (lastLat != lat || lastLon != lon) {
            lastLat = lat;
            lastLon = lon;
            CoordinateGPS newPos = new CoordinateGPS(lat, lon);
            //set additional info's
            newPos.setElevation(elevation);
            newPos.setSpeed(speed);
            newPos.setHeading(bearing);
            newPos.setIsGpsProvided(isGpsProvided);
            newPos.setAccuracy(accuracy);
            log.debug("Send new Position from GPS Bearing:{}", newPos.getHeading());
            EventHandler.fire(new PositionChangedEvent(newPos, eventID));

            setSpeed(speed, eventID);
            setElevation(elevation, eventID);
            setAccuracy(accuracy, eventID);
            setCourse(bearing, eventID);

        } else {
            // not a new position call other event's only
            setSpeed(speed, eventID);
            setElevation(elevation, eventID);
            setAccuracy(accuracy, eventID);
            setCourse(bearing, eventID);
        }
    }

    public void setElevation(double altitude) {
        this.setElevation(altitude, EventHandler.getId());
    }

    public void setElevation(double altitude, short id) {
        lastEle = altitude;
        //TODO add EventHandler
    }

    public void setSpeed(double speed) {
        this.setSpeed(speed, EventHandler.getId());
    }

    private void setSpeed(double speed, short id) {
        if (lastSpeed != speed) {
            EventHandler.fire(new SpeedChangedEvent((float) speed, id));
            lastSpeed = speed;
        }
    }

    public void setMagneticCompassHeading(double heading) {
        if (CB.mapMode == MapMode.CAR) return; //Don't use Compass heading on Car mode!!!
        this.setCourse(heading, EventHandler.getId());
    }

    private void setCourse(double heading, short id) {
        if (lastHeading != heading) {
            EventHandler.fire(new OrientationChangedEvent((float) heading, id));
            lastHeading = heading;
        }
    }

    public void setAccuracy(float accuracy) {
        this.setAccuracy(accuracy, EventHandler.getId());
    }

    public void setAccuracy(float accuracy, short id) {
        this.lastAccuracy = accuracy;
    }
}
