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

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Created by Longri on 25.07.16.
 */
public class AndroidLocationListener implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {

    private static Logger log = LoggerFactory.getLogger(AndroidLocationListener.class);

    long lastTime;
    Location lastLocation;

    @Override
    public void onLocationChanged(Location location) {

        if (location.getTime() < lastTime + 1000) {
            return;
        }
        lastTime = location.getTime();

        if (lastLocation != null) {
            float dis = location.distanceTo(lastLocation);
            if (dis < 10) {
                System.out.print("distance to slow, skip");
                return;
            }
        }
        lastLocation = location;


        de.longri.cachebox3.locator.Location.ProviderType provider = de.longri.cachebox3.locator.Location.ProviderType.NULL;

        if (location.getProvider().toLowerCase(new Locale("en")).contains("gps"))
            provider = de.longri.cachebox3.locator.Location.ProviderType.GPS;
        if (location.getProvider().toLowerCase(new Locale("en")).contains("network"))
            provider = de.longri.cachebox3.locator.Location.ProviderType.Network;

        de.longri.cachebox3.locator.Location cbLocation = new de.longri.cachebox3.locator.Location(location.getLatitude(), location.getLongitude(), location.getAccuracy());

        cbLocation.setHasSpeed(location.hasSpeed());
        cbLocation.setSpeed(location.getSpeed());
        cbLocation.setHasBearing(location.hasBearing());
        cbLocation.setBearing(location.getBearing());
        cbLocation.setAltitude(location.getAltitude());
        cbLocation.setProvider(provider);

        de.longri.cachebox3.locator.Locator.setNewLocation(cbLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {

    }
}
