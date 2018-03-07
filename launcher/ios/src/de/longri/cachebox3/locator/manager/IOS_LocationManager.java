/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.locator.manager;

import de.longri.cachebox3.events.location.LocationEvents;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;

/**
 * Created by Longri on 07.03.18.
 */
public class IOS_LocationManager extends LocationManager {

    private final CLLocationManager manager;
    private final boolean backGround;

    public IOS_LocationManager(boolean backGround) {
        this.backGround = backGround;
        manager = new CLLocationManager();
        if (Foundation.getMajorSystemVersion() >= 8) {
            manager.requestAlwaysAuthorization();
            manager.requestWhenInUseAuthorization();
        }
        if (backGround) {
            manager.allowsBackgroundLocationUpdates();
        }
    }


    @Override
    public void setDelegate(final LocationEvents locationEvents) {
        manager.setDelegate(new CLLocationManagerDelegateAdapter() {
            @Override
            public void didUpdateLocations(CLLocationManager clLocationManager, NSArray<CLLocation> locations) {
                CLLocation newLocation = locations.last();
                CLLocationCoordinate2D coord = newLocation.getCoordinate();

                double lat = coord.getLatitude();
                double lon = coord.getLongitude();
                float accuracy = (float) newLocation.getHorizontalAccuracy();
                double altitude = newLocation.getAltitude();
                double speed = newLocation.getSpeed() * 3.6;
                float courseRad = (float) Math.toRadians(newLocation.getCourse());

                locationEvents.newGpsPos(lat, lon, accuracy);
                locationEvents.newAltitude(altitude);
                if (courseRad >= 0) locationEvents.newBearing(courseRad, true);
                if (speed >= 0) locationEvents.newSpeed(speed);

            }

            @Override
            public void didUpdateHeading(CLLocationManager clLocationManager, CLHeading newHeading) {
                if (newHeading.getHeadingAccuracy() < 0) return; // invalid
                float headingRad = (float) Math.toRadians(newHeading.getTrueHeading());
                locationEvents.newBearing(headingRad, false);
            }

            @Override
            public boolean shouldDisplayHeadingCalibration(CLLocationManager clLocationManager) {
                return false;
            }


            @Override
            public void didFail(CLLocationManager clLocationManager, NSError nsError) {

            }


            @Override
            public void didPauseLocationUpdates(CLLocationManager clLocationManager) {

            }

        });
    }

    @Override
    public void startUpdateLocation() {
        manager.startUpdatingLocation();
    }

    @Override
    public void startUpdateHeading() {
        manager.startUpdatingHeading();
    }

    @Override
    public void stopUpdateLocation() {
        manager.stopUpdatingLocation();
    }

    @Override
    public void stopUpdateHeading() {
        manager.stopUpdatingHeading();
    }
}
