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

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.locator.Locator;
import org.robovm.apple.corelocation.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSString;

/**
 * Created by Longri on 26.07.2016.
 */
public class IOS_LocationListener {

    private static double ACCURACY = CLLocationAccuracy.Best;
    private static double DISTANCE_FILTER = 5;

    private CLLocationManager locationManager;


    private void stopUpdatingLocation(String state) {
        Gdx.app.log("locationManager stop", state);
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

                // Once configured, the location manager must be "started".
                locationManager.startUpdatingLocation();
                Gdx.app.log("locationManager", "startet");
            }
        });


    }

    CLLocationManagerDelegateAdapter delegateAdapter = new CLLocationManagerDelegateAdapter() {

        @Override
        public void didUpdateLocations(CLLocationManager manager, NSArray<CLLocation> locations) {

            CLLocation newLocation = locations.last();
            CLLocationCoordinate2D coord = newLocation.getCoordinate();

            de.longri.cachebox3.locator.Location.ProviderType provider = de.longri.cachebox3.locator.Location.ProviderType.NULL;

            //TODO set  GPS provider
//                if (location.getProvider().toLowerCase(new Locale("en")).contains("gps"))
            provider = de.longri.cachebox3.locator.Location.ProviderType.GPS;
//                if (location.getProvider().toLowerCase(new Locale("en")).contains("network"))
//                    provider = de.longri.cachebox3.locator.Location.ProviderType.Network;

            de.longri.cachebox3.locator.Location cbLocation =
                    new de.longri.cachebox3.locator.Location(coord.getLatitude(),
                            coord.getLongitude(), (float) newLocation.getHorizontalAccuracy());

            cbLocation.setHasSpeed(newLocation.getSpeed() >= 0);
            cbLocation.setSpeed((float) newLocation.getSpeed());
            cbLocation.setHasBearing(newLocation.getCourse() >= 0);
            cbLocation.setBearing((float) newLocation.getCourse());
            cbLocation.setAltitude(newLocation.getAltitude());
            cbLocation.setProvider(provider);

            de.longri.cachebox3.locator.Locator.setNewLocation(cbLocation);


        }

        /**
         * This delegate method is invoked when the location manager has
         * heading data.
         */
        @Override
        public void didUpdateHeading(CLLocationManager manager, CLHeading newHeading) {
            double x = newHeading.getX();
            double y = newHeading.getY();
            double z = newHeading.getZ();

            // Compute and display the magnitude (size or strength) of
            // the vector.
            // magnitude = sqrt(x^2 + y^2 + z^2)
            double magnitute = Math.sqrt(x * x + y * y + z * z);
            de.longri.cachebox3.locator.Locator.setHeading((float) magnitute, Locator.CompassType.Magnetic);
        }


        @Override
        public void didFail(CLLocationManager manager, NSError error) {
            if (error.getErrorCode() != CLErrorCode.LocationUnknown) {
                stopUpdatingLocation("Error: " + error.getErrorCode().toString());
            }
        }
    };

    public static String getLocalizedCoordinateString(CLLocation location) {
        if (location.getHorizontalAccuracy() < 0) {
            return NSString.getLocalizedString("DataUnavailable");
        }
        String latString = (location.getCoordinate().getLatitude() < 0) ? NSString.getLocalizedString("South")
                : NSString.getLocalizedString("North");
        String lonString = (location.getCoordinate().getLongitude() < 0) ? NSString.getLocalizedString("West")
                : NSString.getLocalizedString("East");
        return String.format("%.4fÂ° %s, %.4fÂ° %s", Math.abs(location.getCoordinate().getLatitude()), latString,
                Math.abs(location.getCoordinate().getLongitude()), lonString);
    }
}
