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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.locator.CircularRegion;
import de.longri.cachebox3.locator.Region;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.robovm.apple.dispatch.DispatchQueue.PRIORITY_LOW;

/**
 * Created by Longri on 07.03.18.
 */
public class IOS_LocationManager extends LocationManager {

    private static final Logger log = LoggerFactory.getLogger(IOS_LocationManager.class);
    private final static long MIN_UPDATE_TIME = 1000;

    private final CLLocationManager manager;
    private final ObjectMap<CLRegion, Region> regionMap = new ObjectMap<>();
    private float distanceFilter = 0;
    private GenericHandleCallBack<Boolean> canCalibrateCallBack;

    private final AtomicBoolean updateLocations = new AtomicBoolean();
    private final AtomicBoolean updateHeadings = new AtomicBoolean();
    private final AtomicBoolean updateRegions = new AtomicBoolean();

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

        if (locationEvents == null) {
            manager.setDelegate(null);
            return;
        }

        manager.setDelegate(new CLLocationManagerDelegateAdapter() {

            long lastLocationUpdate;

            @Override
            public void didUpdateLocations(CLLocationManager clLocationManager, final NSArray<CLLocation> locations) {
                if (!updateLocations.get()) return;
                if (locations == null) return;

                long now = System.currentTimeMillis();
                if (lastLocationUpdate + MIN_UPDATE_TIME > now) return;
                lastLocationUpdate = now;

                DispatchQueue.getGlobalQueue(PRIORITY_LOW, 0).async(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CLLocation newLocation = locations.last();
                            if (newLocation == null) return;
                            CLLocationCoordinate2D coord = newLocation.getCoordinate();
                            if (coord == null) return;
                            log.debug("didUpdateLocations");

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
                        } catch (Exception e) {
                            CB.stageManager.indicateException(CB.EXCEPTION_COLOR_LOCATION);
                            log.error("didUpdateLocations", e);
                        }
                    }
                });
            }


            long lastHeadingUpdate;

            @Override
            public void didUpdateHeading(CLLocationManager clLocationManager, final CLHeading newHeading) {
                if (!updateHeadings.get()) return;
                if (newHeading == null) return;
                long now = System.currentTimeMillis();
                if (lastHeadingUpdate + MIN_UPDATE_TIME > now) return;
                lastHeadingUpdate = now;

                log.debug("didUpdateHeading");
                DispatchQueue.getGlobalQueue(PRIORITY_LOW, 0).async(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (newHeading.getHeadingAccuracy() < 0) return; // invalid
                            float headingRad = (float) Math.toRadians(newHeading.getTrueHeading());
                            if (locationEvents != null) locationEvents.newBearing(headingRad, false);
                        } catch (Exception e) {
                            CB.stageManager.indicateException(CB.EXCEPTION_COLOR_LOCATION);
                            log.error("didUpdateHeading", e);
                        }
                    }
                });
            }

            @Override
            public boolean shouldDisplayHeadingCalibration(CLLocationManager clLocationManager) {
                if (canCalibrateCallBack == null) {
                    return false;
                } else {
                    return canCalibrateCallBack.callBack(true);
                }
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
                log.debug("didEnterRegion");
                try {
                    locationEvents.didEnterRegion(regionMap.get(region));
                } catch (Exception e) {
                    CB.stageManager.indicateException(CB.EXCEPTION_COLOR_LOCATION);
                    log.error("didEnterRegion", e);
                }
            }

            @Override
            public void didExitRegion(CLLocationManager clLocationManager, CLRegion region) {
                log.debug("didExitRegion");
                try {
                    locationEvents.didExitRegion(regionMap.get(region));
                } catch (Exception e) {
                    CB.stageManager.indicateException(CB.EXCEPTION_COLOR_LOCATION);
                    log.error("didExitRegion", e);
                }
            }

        });
    }

    @Override
    public void startUpdateLocation() {
        manager.setDistanceFilter(distanceFilter);
        manager.startUpdatingLocation();
        updateLocations.set(true);
    }

    @Override
    public void startUpdateHeading() {
        manager.startUpdatingHeading();
        updateHeadings.set(true);
    }

    @Override
    public void stopUpdateLocation() {
        manager.stopUpdatingLocation();
        updateLocations.set(false);
    }

    @Override
    public void stopUpdateHeading() {
        manager.stopUpdatingHeading();
        updateHeadings.set(false);
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
        updateRegions.set(false);
    }

    @Override
    public void startMonitoring(Region region) {

        //create CLRegion
        CLRegion clRegion;

        if (region instanceof CircularRegion) {
            CircularRegion circularRegion = (CircularRegion) region;
            CLLocationCoordinate2D center = new CLLocationCoordinate2D(circularRegion.center.getLatitude(), circularRegion.center.getLongitude());
            clRegion = new CLCircularRegion(center, circularRegion.radius, "");
            regionMap.put(clRegion, region);
        } else {
            throw new RuntimeException("Region: " + region.getClass().getName() + " not supported");
        }
        manager.startMonitoring(clRegion);
        updateRegions.set(true);
    }

    @Override
    public void setCanCalibrateCallBack(GenericHandleCallBack<Boolean> canCalibrateCallBack) {
        this.canCalibrateCallBack = canCalibrateCallBack;
    }
}
