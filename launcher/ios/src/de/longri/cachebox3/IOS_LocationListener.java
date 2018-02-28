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
package de.longri.cachebox3;

import de.longri.cachebox3.events.location.GpsEventHelper;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 26.07.2016.
 */
public class IOS_LocationListener {
    final static Logger log = LoggerFactory.getLogger(IOS_LocationListener.class);
    private static double HEADING_FILTER = 5;

    private CLLocationManager locationManager;
    private CLLocationManager networkLocationManager;


    private void stopUpdatingLocation(String state) {
        log.debug("locationManager stop", state);
        locationManager.stopUpdatingLocation();
        locationManager.setDelegate(null);

        networkLocationManager.stopUpdatingLocation();
        networkLocationManager.setDelegate(null);
    }


    public void createLocationManager() {
        DispatchQueue.getMainQueue().sync(new Runnable() {
            @Override
            public void run() {
                // Create the Gps LocationManager
                locationManager = new CLLocationManager();
                locationManager.setDelegate(delegateAdapter);
                locationManager.setDesiredAccuracy(CLLocationAccuracy.Best);
                locationManager.setDistanceFilter(3.0); //3 m
                if (Foundation.getMajorSystemVersion() >= 8) {
                    locationManager.requestAlwaysAuthorization();
                    locationManager.requestWhenInUseAuthorization();
                }
                locationManager.setHeadingFilter(HEADING_FILTER);
                //  locationManager.setAllowsBackgroundLocationUpdates(true);

                // Once configured, the location manager must be "started".
                locationManager.startUpdatingLocation();
                locationManager.startUpdatingHeading();


                // Create the Network LocationManager
                networkLocationManager = new CLLocationManager();
                networkLocationManager.setDelegate(networkDelegateAdapter);
                networkLocationManager.setDesiredAccuracy(CLLocationAccuracy.NearestTenMeters);
                networkLocationManager.setDistanceFilter(100.0); //100m
                if (Foundation.getMajorSystemVersion() >= 8) {
                    networkLocationManager.requestAlwaysAuthorization();
                    networkLocationManager.requestWhenInUseAuthorization();
                }
                networkLocationManager.setHeadingFilter(HEADING_FILTER);
                // networkLocationManager.setAllowsBackgroundLocationUpdates(true);

                networkLocationManager.startMonitoringSignificantLocationChanges();
                log.debug("locationManager started");
            }
        });


    }

    CLLocationManagerDelegateAdapter delegateAdapter = new CLLocationManagerDelegateAdapter() {

        @Override
        public void didUpdateLocations(CLLocationManager manager, NSArray<CLLocation> locations) {
            if (CB.sensoerIO.isPlay()) return;
            CLLocation newLocation = locations.last();
            CLLocationCoordinate2D coord = newLocation.getCoordinate();

            double lat = coord.getLatitude();
            double lon = coord.getLongitude();
            float accuracy = (float) newLocation.getHorizontalAccuracy();
            double altitude = newLocation.getAltitude();
            double speed = newLocation.getSpeed() * 3.6;
            float courseRad = (float) Math.toRadians(newLocation.getCourse());

            CB.eventHelper.newGpsPos(lat, lon, accuracy);
            CB.eventHelper.newAltitude(altitude);
            if (courseRad >= 0) CB.eventHelper.newBearing(courseRad, true);
            CB.eventHelper.newSpeed(speed);

        }

        /**
         * This delegate method is invoked when the location manager has
         * heading data.
         */
        @Override
        public void didUpdateHeading(CLLocationManager manager, CLHeading newHeading) {
            if (CB.sensoerIO.isPlay()) return;
            if (newHeading.getHeadingAccuracy() < 0) return; // invalid
            float headingRad = (float) Math.toRadians(newHeading.getTrueHeading());
            CB.eventHelper.newBearing(headingRad, false);
        }


        @Override
        public void didFail(CLLocationManager manager, NSError error) {
            if (error.getErrorCode() != CLErrorCode.LocationUnknown) {
                stopUpdatingLocation("Error: " + error.getErrorCode().toString());
            }
        }
    };

    CLLocationManagerDelegateAdapter networkDelegateAdapter = new CLLocationManagerDelegateAdapter() {

        @Override
        public void didUpdateLocations(CLLocationManager manager, NSArray<CLLocation> locations) {
            if (CB.sensoerIO.isPlay()) return;
            CLLocation newLocation = locations.last();
            CLLocationCoordinate2D coord = newLocation.getCoordinate();

            double lat = coord.getLatitude();
            double lon = coord.getLongitude();
            float accuracy = (float) newLocation.getHorizontalAccuracy();
            CB.eventHelper.newNetworkPos(lat, lon, accuracy);

        }

        /**
         * This delegate method is invoked when the location manager has
         * heading data.
         */
        @Override
        public void didUpdateHeading(CLLocationManager manager, CLHeading newHeading) {
            // ignore heading from Network
        }


        @Override
        public void didFail(CLLocationManager manager, NSError error) {
            if (error.getErrorCode() != CLErrorCode.LocationUnknown) {
                stopUpdatingLocation("Error: " + error.getErrorCode().toString());
            }
        }
    };
}
