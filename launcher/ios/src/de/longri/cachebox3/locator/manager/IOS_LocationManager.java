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

import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.CircularRegion;
import de.longri.cachebox3.locator.Region;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 07.03.18.
 */
public class IOS_LocationManager extends LocationManager {

    private static final Logger log = LoggerFactory.getLogger(IOS_LocationManager.class);

    private final CLLocationManager manager;
    private final ObjectMap<CLRegion, Region> regionMap = new ObjectMap<>();
    private float distanceFilter = 0;

    public IOS_LocationManager(boolean backGround) {
        manager = new CLLocationManager();
        if (Foundation.getMajorSystemVersion() >= 8) {
            manager.requestAlwaysAuthorization();
            manager.requestWhenInUseAuthorization();
        }
        if (backGround) {
            manager.setAllowsBackgroundLocationUpdates(true);
            manager.setPausesLocationUpdatesAutomatically(false);
        }
        manager.setDesiredAccuracy(CLLocationAccuracy.BestForNavigation);
        manager.setActivityType(CLActivityType.Fitness);
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
                return true;
            }


            @Override
            public void didFail(CLLocationManager clLocationManager, NSError nsError) {
                log.debug("Did fail: {}", nsError.toString());
            }


            @Override
            public void didPauseLocationUpdates(CLLocationManager clLocationManager) {
                log.debug("Did pause location updates");
            }

            @Override
            public void didEnterRegion(CLLocationManager clLocationManager, CLRegion region) {
                locationEvents.didEnterRegion(regionMap.get(region));
            }

            @Override
            public void didExitRegion(CLLocationManager clLocationManager, CLRegion region) {
                locationEvents.didExitRegion(regionMap.get(region));
            }

        });
    }

    @Override
    public void startUpdateLocation() {
        manager.setDistanceFilter(distanceFilter);
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

    @Override
    public void setDistanceFilter(float distance) {
        this.distanceFilter = distance;
        manager.setDistanceFilter(distanceFilter);
    }

    @Override
    public void dispose() {
        log.debug("dispose location manager");
        stopUpdateHeading();
        startUpdateLocation();
        manager.dispose();
    }

    @Override
    public void stopMonitoring(Region region) {
        CLRegion clRegion = regionMap.findKey(region, false);
        manager.startMonitoring(clRegion);
        regionMap.remove(clRegion);
    }

    @Override
    public void startMonitoring(Region region) {

        //create CLRegion
        CLRegion clRegion;

        if (region instanceof CircularRegion) {
            CircularRegion circularRegion = (CircularRegion) region;
            CLLocationCoordinate2D center = new CLLocationCoordinate2D(circularRegion.center.latitude, circularRegion.center.longitude);
            clRegion = new CLCircularRegion(center, circularRegion.radius, "");
            regionMap.put(clRegion, region);
        } else {
            throw new RuntimeException("Region: " + region.getClass().getName() + " not supported");
        }
        manager.startMonitoring(clRegion);
    }
}
