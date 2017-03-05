/* 
 * Copyright (C) 2013-2016 team-cachebox.de
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

package de.longri.cachebox3.locator;

import java.util.Date;

/**
 * Represented a GPS Signal with all given Informations like Position, Speed, accuracy, altitude, bearing, Sats
 *
 * @author Longri
 */
public class Location extends CoordinateGPS {
    private static final long serialVersionUID = 3339644242602640422L;

    /**
     * @author Longri
     */
    public enum ProviderType {
        GPS, Network, Saved, NULL, any
    }

    private boolean hasSpeed = false;

    private float speed = 0;

    private boolean hasBearing = false;

    private float bearing = 0;

    private float altitude = 0;


    private ProviderType provider = ProviderType.NULL;

    private Date TimeStamp;

    /**
     * Constructor </br> You can set the values only over a constructor! </br> No manipulation of any value!
     *
     * @param latitude
     * @param longitude
     * @param accuracy
     * @param hasSpeed
     * @param speed
     * @param hasBearing
     * @param bearing
     * @param altitude
     * @param provider
     */
    public Location(double latitude, double longitude, float accuracy, boolean hasSpeed, float speed, boolean hasBearing, float bearing, double altitude, ProviderType provider) {
        super(latitude, longitude, (int) accuracy);
        this.hasSpeed = hasSpeed;
        this.speed = speed;
        this.hasBearing = hasBearing;
        this.bearing = bearing;
        this.altitude = (float) altitude;
        this.provider = provider;
    }

    public Location(int latitude, int longitude, int accuracy) {
        super(latitude, longitude, accuracy);
    }

    public Location(double latitude, double longitude, float accuracy) {
        super(latitude, longitude);
        this.Accuracy = (int) accuracy;
    }

    /**
     * Constant Location with all values are 0 ore false!</br> ProviderType is ProviderType.NULL
     *
     * @uml.property name="nULL_LOCATION"
     * @uml.associationEnd
     */
    public static final Location NULL_LOCATION = new Location(0, 0, 0);

    /**
     * Returns the Provider Type of this location
     *
     * @return ProviderType as ProviderType
     */
    public ProviderType getProviderType() {
        return provider;
    }

    /**
     * Returns the Timestamp of this location
     *
     * @return Timestamp as date
     * @uml.property name="timeStamp"
     */
    public Date getTimeStamp() {
        return TimeStamp;
    }

    /**
     * Returns the Latitude of this location!
     *
     * @return Latitude as double
     */
    @Override
    public double getLatitude() {
        return super.getLatitude();
    }

    /**
     * Returns the Longitude of this location
     *
     * @return Longitude as double
     */
    @Override
    public double getLongitude() {
        return super.getLongitude();
    }

    /**
     * Returns True if this Location have a bearing Value
     *
     * @return boolean
     * @uml.property name="hasBearing"
     */
    public boolean getHasBearing() {
        return hasBearing;
    }

    /**
     * Returns the bearing of this location as float
     *
     * @return float
     * @uml.property name="bearing"
     */
    public float getBearing() {
        return bearing;
    }

    /**
     * Returns True if this Location have a Speed Value
     *
     * @return boolean
     * @uml.property name="hasSpeed"
     */
    public boolean getHasSpeed() {
        return hasSpeed;
    }

    /**
     * Returns the speed of this location as float (kmh)
     *
     * @return float
     * @uml.property name="speed"
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Returns the Altitude of this location as float (m)
     *
     * @return
     * @uml.property name="altitude"
     */
    public float getAltitude() {
        return altitude;
    }

    public CoordinateGPS toCordinate() {
        return new CoordinateGPS(this);
    }

    @Override
    public void setAccuracy(float accuracy) {
        super.setAccuracy(accuracy);
    }

    public void setHasSpeed(boolean hasSpeed) {
        this.hasSpeed = hasSpeed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setHasBearing(boolean hasBearing) {
        this.hasBearing = hasBearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public void setAltitude(double altitude) {
        this.altitude = (float) altitude;
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public Location cpy() {
        Location ret = new Location(this.latitude, this.longitude, this.Accuracy);

        ret.hasSpeed = this.hasSpeed;
        ret.speed = this.speed;
        ret.hasBearing = this.hasBearing;
        ret.bearing = this.bearing;
        ret.altitude = this.altitude;
        ret.provider = this.provider;
        return ret;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Location Typ:");
        sb.append(this.provider);
        sb.append(" [latitude=");
        sb.append(this.latitude);
        sb.append(", longitude=");
        sb.append(this.longitude);
        sb.append("]  BEARING: ");
        sb.append(this.bearing);
        sb.append("  Speed: ");
        sb.append(this.speed);
        return sb.toString();
    }

}
