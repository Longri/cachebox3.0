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
    private static double ACCURACY = CLLocationAccuracy.Best;
    private static double DISTANCE_FILTER = 5;
    private static double HEADING_FILTER = 5;

    private CLLocationManager locationManager;



    private void stopUpdatingLocation(String state) {
        log.debug("locationManager stop", state);
        locationManager.stopUpdatingLocation();
        locationManager.setDelegate(null);
    }


    public void createLocationManager() {
        DispatchQueue.getMainQueue().sync(new Runnable() {
            @Override
            public void run() {
                // Create the LocationManager
                locationManager = new CLLocationManager();
                locationManager.setDelegate(delegateAdapter);
                locationManager.setDesiredAccuracy(ACCURACY);
                locationManager.setDistanceFilter(DISTANCE_FILTER);
                if (Foundation.getMajorSystemVersion() >= 8) {
                    locationManager.requestAlwaysAuthorization();
                    locationManager.requestWhenInUseAuthorization();
                }
                locationManager.setHeadingFilter(HEADING_FILTER);

                // Once configured, the location manager must be "started".
                locationManager.startUpdatingLocation();
                locationManager.startUpdatingHeading();
                log.debug("locationManager started");
            }
        });


    }

    CLLocationManagerDelegateAdapter delegateAdapter = new CLLocationManagerDelegateAdapter() {

        @Override
        public void didUpdateLocations(CLLocationManager manager, NSArray<CLLocation> locations) {

            CLLocation newLocation = locations.last();
            CLLocationCoordinate2D coord = newLocation.getCoordinate();

            CB.eventHelper.newGpsPos(coord.getLatitude(), coord.getLongitude(), true,
                    newLocation.getAltitude(), newLocation.getSpeed()*3.6, newLocation.getCourse(),
                    (float) newLocation.getHorizontalAccuracy());

        }

        /**
         * This delegate method is invoked when the location manager has
         * heading data.
         */
        @Override
        public void didUpdateHeading(CLLocationManager manager, CLHeading newHeading) {
            if (newHeading.getHeadingAccuracy() > 0) {
                CB.eventHelper.setMagneticCompassHeading(newHeading.getTrueHeading());
            }
        }


        @Override
        public void didFail(CLLocationManager manager, NSError error) {
            if (error.getErrorCode() != CLErrorCode.LocationUnknown) {
                stopUpdatingLocation("Error: " + error.getErrorCode().toString());
            }
        }
    };
}
