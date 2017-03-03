/* 
 * Copyright (C) 2014-2017 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.locator.events.GPS_FallBackEventList;
import de.longri.cachebox3.locator.events.PositionChangedEventList;
import de.longri.cachebox3.utils.UnitFormatter;
import org.oscim.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


/**
 * @author Longri
 */
public class Locator {
    final static Logger log = LoggerFactory.getLogger(Locator.class);

    /**
     * @author Longri
     */
    public enum CompassType {
        GPS, Magnetic, any
    }

    ;

    // #################################
    // Private Static Member
    // #################################


    public static Locator that;

    private static long minGpsUpdateTime = 125;
    private static double altCorrection = 0;
    private static boolean mUseImperialUnits = false;
    private static boolean mUseMagneticCompass = false;
    private static int mMagneticCompassLevel = 5;
    private static boolean fix = false;
    private static boolean DisplayOff = false;

    // #################################
    // Public Static Access
    // #################################

    /**
     * Constructor </br> </br> Set initial location, maybe last saved position! </br> Or NULL for initial with 0,0 Coords!
     *
     * @param initialLocation as GPS_Location
     */
    public Locator(Location initialLocation) {
        that = this;
        if (initialLocation == null)
            initialLocation = Location.NULL_LOCATION;
        setNewLocation(initialLocation);
    }

    public static boolean isFixed() {
        return fix;
    }

    /**
     * Set Display Off. </br>Only events with priority High will fire!
     */
    public static void setDisplayOff() {
        DisplayOff = true;
    }

    /**
     * Set Display on. </br> All events will fire!
     */
    public static void setDisplayOn() {
        DisplayOff = false;
    }

    /**
     * Returns True if the flag for DisplayOff is True!
     */
    public static boolean isDisplayOff() {
        return DisplayOff;
    }

    /**
     * Set the minimum update time for fire position changed event
     *
     * @param value as long
     */
    public static void setMinUpdateTime(Long value) {
        minGpsUpdateTime = value;
    }

    /**
     * Returns the minimum update time for firing position changed event
     *
     * @return long
     */
    public static long getMinUpdateTime() {
        return minGpsUpdateTime;
    }

    /**
     * Set the speed level for using Hardware or GPS heading
     *
     * @param value
     */
    public static void setHardwareCompassLevel(int value) {
        mMagneticCompassLevel = value;
    }

    /**
     * Set true if the Locator is use heading values from Hardware Compass
     *
     * @param value
     */
    public static void setUseHardwareCompass(boolean value) {
        mUseMagneticCompass = value;
    }

    /**
     * Set a new location from GPS,Network or the last saved location!</br> For all given information is using the last best Location!</br>
     * 1. Gps</br> 2. Network</br> 3. Saved</br> </br> If the last set of GPS Location older 2min, the saved FineLocation will be cleaned!
     * (Fall back to Network or saved Location)
     *
     * @param location
     */
    public static void setNewLocation(Location location) {

        if (that == null) return;


        log.trace("new Location:" + location.toString());

        synchronized (that) {
            switch (location.getProviderType()) {
                case Saved:
                    that.mSaveLocation = location;
                    that.hasSpeed = false;
                    that.speed = 0;
                    break;
                case Network:
                    that.mNetworkLocation = location;
                    // reset Speed only if last Speed value old
                    long time = new Date().getTime();
                    if (that.mTimeStampSpeed + (minGpsUpdateTime * 3) < time) {
                        that.hasSpeed = false;
                        that.speed = 0;
                    }

                    break;
                case GPS:

                    that.mFineLocation = location;
                    that.mLastSavedFineLocation = location;
                    that.hasSpeed = location.getHasSpeed();
                    that.speed = location.getSpeed();
                    that.mTimeStampSpeed = (new Date()).getTime();
                    if (location.getHasBearing()) {
                        setHeading(location.getBearing(), CompassType.GPS);
                    }

                    if (!fix && location != null) {
                        fix = true;
                        GPS_FallBackEventList.CallFix();
                    }

                    break;
                default:
                    log.debug("invalid Location provider");
                    break;
            }

            Event event = new Event();
            PositionChangedEventList.speedChanged(event);
            PositionChangedEventList.orientationChanged(event);
            PositionChangedEventList.positionChanged(event);
        }
    }

    /**
     * Returns the last saved fine location (from GPS) or null !
     *
     * @return
     */
    public static Location getLastSavedFineLocation() {
        synchronized (that) {
            return that.mLastSavedFineLocation;
        }
    }

    /**
     * Returns the last Latitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     * @return Latitude as double
     */
    public static double getLatitude() {
        return getLatitude(Location.ProviderType.any);
    }

    /**
     * Returns the last Latitude from the last position of the given ProviderType
     *
     * @param type
     * @return
     */
    public static double getLatitude(Location.ProviderType type) {
        return getLocation(type).getLatitude();
    }

    /**
     * Returns the last Longitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     * @return Longitude as double
     */
    public static double getLongitude() {
        return getLongitude(Location.ProviderType.any);
    }

    /**
     * Returns the last Longitude from the last position of the given ProviderType
     *
     * @param type
     * @return
     */
    public static double getLongitude(Location.ProviderType type) {
        return getLocation(type).getLongitude();
    }

    /**
     * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     * @return
     */
    public static Location getLocation() {
        return getLocation(Location.ProviderType.any);
    }

    /**
     * Returns the last valid position of the given ProviderType
     */
    public static Location getLocation(Location.ProviderType type) {
        if (that == null) return new Location(0, 0, 0);

        synchronized (that) {

            if (type == Location.ProviderType.any) {
                if (that.mFineLocation != null)
                    return that.mFineLocation;
                if (that.mNetworkLocation != null)
                    return that.mNetworkLocation;
                if (that.mSaveLocation != null)
                    return that.mSaveLocation;
                return Location.NULL_LOCATION;
            } else if (type == Location.ProviderType.GPS) {
                return that.mLastSavedFineLocation;
            } else if (type == Location.ProviderType.Network) {
                return that.mNetworkLocation;
            } else if (type == Location.ProviderType.Saved) {
                return that.mSaveLocation;
            }
            return Location.NULL_LOCATION;
        }
    }

    /**
     * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     * @return
     */
    public static CoordinateGPS getCoordinate() {
        return getLocation(Location.ProviderType.any).toCordinate();
    }

    /**
     * Returns True if the saved Location != ProviderType.NULL
     *
     * @return
     */
    public static boolean Valid() {
        return getLocation().getProviderType() == Location.ProviderType.GPS || getLocation().getProviderType() == Location.ProviderType.Network;
    }

    /**
     * Returns the last valid position of the given ProviderType
     *
     * @param type
     * @return
     */
    public static Coordinate getCoordinate(Location.ProviderType type) {
        Location loc = getLocation(type);
        if (loc == null)
            return null;
        return loc.toCordinate();
    }

    /**
     * Set a flag, that all Units are formated as Imperial Units or not! </br> default are false
     *
     * @param value as boolean
     */
    public static void setUseImperialUnits(boolean value) {
        mUseImperialUnits = value;
    }

    /**
     * Returns the formated speed String!
     *
     * @return
     */
    public static String SpeedString() {
        synchronized (that) {
            if (that.hasSpeed)
                return Formatter.SpeedString(SpeedOverGround(), mUseImperialUnits);
            else
                return "-----";
        }
    }

    /**
     * Returns the Speed as float
     *
     * @return
     */
    public static float SpeedOverGround() {
        synchronized (that) {
            if (that.hasSpeed) {
                return that.speed * 3600 / 1000;
            } else
                return 0;
        }
    }

    /**
     * Return True if the last valid Location from Type GPS
     *
     * @return
     */
    public static boolean isGPSprovided() {
        return getLocation().getProviderType() == Location.ProviderType.GPS;
    }

    /**
     * Set the alt correction value
     */
    public static void setAltCorrection(double value) {
        log.debug("set alt corection to: " + value);
        altCorrection = value;
    }

    static long lastFixLose = 0;

    /**
     * call this if GPS state changed to no sat have a fix
     */
    public static void FallBack2Network() {
        synchronized (that) {
            // check if last GPS position older then 20 sec

            if (that.mTimeStampSpeed + 20000 >= (new Date()).getTime()) {
                log.trace("no fall back");
                return;
            }

            log.debug("Falback2Network");
            lastFixLose = System.currentTimeMillis();
            fix = false;
            that.mFineLocation = null;
        }
        GPS_FallBackEventList.CallFallBack();
    }

    /**
     * Returns True if the last valid Location have a speed value
     *
     * @return
     */
    public static boolean hasSpeed() {
        synchronized (that) {
            return that.hasSpeed;
        }
    }

    /**
     * Returns the speed value of the last valid Location
     */
    public static double getSpeed() {
        synchronized (that) {
            return that.speed;
        }
    }

    /**
     * Returns the altitude with correction from last valid Location
     *
     * @return
     */
    public static double getAlt() {
        return getLocation().getAltitude() - altCorrection;
    }

    /**
     * Returns the formated string of last valid altitude with correction value
     *
     * @return
     */
    public static String getAltStringWithCorection() {
        String result = getAltString();
        if (altCorrection > 0)
            result += " (+" + UnitFormatter.AltString((float) altCorrection);
        else if (altCorrection < 0)
            result += " (" + UnitFormatter.AltString((float) altCorrection);
        return result;
    }

    /**
     * Returns the formated string of last valid altitude
     *
     * @return
     */
    public static String getAltString() {
        return UnitFormatter.AltString((float) getAlt());
    }

    /**
     * Returns the ProviderType of the last Valid Location
     *
     * @return
     */
    public static Location.ProviderType getProvider() {
        return getLocation().getProviderType();
    }

    /**
     * Returns True if the used bearing value from magnetic compass. </br> Returns False if the bearing from GPS.
     *
     * @return
     */
    public static boolean UseMagneticCompass() {
        if (that == null)
            return false;
        synchronized (that) {
            return that.mLastUsedCompassType == CompassType.Magnetic;
        }
    }

    /**
     * Returns the last saved heading
     *
     * @return
     */
    public static float getHeading() {
        return getHeading(CompassType.any);
    }

    /**
     * Returns the last saved heading of the given ProviderType
     *
     * @param type
     * @return
     */
    public static float getHeading(CompassType type) {
        synchronized (that) {

            if (type == CompassType.GPS || !mUseMagneticCompass) {
//                log.debug("Return mlastGPSHeading1");
                return that.mlastGPSHeading;
            }

            if (type == CompassType.Magnetic) {
//                log.debug("Return mlastGPSHeading2");
                return that.mlastGPSHeading;
            }


            if (UseMagneticCompass()) {
//                log.debug("Return mlastGPSHeading3");
                return that.mlastMagneticHeading;
            } else {
//                log.debug("Return mlastMagneticHeading");
                return that.mlastGPSHeading;
            }
        }
    }

    /**
     * Set the heading from GPS or magnetic sensor
     *
     * @param heading
     * @param type
     */
    public static void setHeading(float heading, CompassType type) {

        if (that == null) return;

        if (type == CompassType.GPS) {
//            log.debug("Set last Gps heading:");
            that.mlastGPSHeading = heading;
        } else {
//            log.debug("Set last Magnetic heading:");
            that.mlastMagneticHeading = heading;
        }

        // set last used compass Type

        if ((that.mlastGPSHeading > -1 && SpeedOverGround() > mMagneticCompassLevel) || !mUseMagneticCompass) {
            that.mLastUsedCompassType = CompassType.GPS;
        } else {
            that.mLastUsedCompassType = CompassType.Magnetic;
        }

//        log.debug("Set last used Compass type:" + that.mLastUsedCompassType);

        PositionChangedEventList.orientationChanged(new Event());

        Gdx.graphics.requestRendering();
    }

    // member are private for synchronized access
    private boolean hasSpeed = false;

    private Location mFineLocation;

    private Location mLastSavedFineLocation;


    private Location mNetworkLocation;

    private Location mSaveLocation;


    private float speed = 0;
    private float mlastMagneticHeading = 0;
    private float mlastGPSHeading = -1;
    private long mTimeStampSpeed = (new Date().getTime());

    private CompassType mLastUsedCompassType = CompassType.any;

}
