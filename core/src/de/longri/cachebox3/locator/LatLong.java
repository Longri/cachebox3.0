/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.longri.cachebox3.locator;

import de.longri.cachebox3.locator.geocluster.GeoDistanceUnit;

import java.io.Serializable;

/**
 * A LatLong represents an immutable pair of latitude and longitude coordinates.
 */
public class LatLong implements Comparable<LatLong>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final double EARTH_RADIUS = 6371.01d;

    /**
     * The latitude coordinate of this LatLong in degrees.
     */
    public final double latitude;

    /**
     * The longitude coordinate of this LatLong in degrees.
     */
    public final double longitude;


    public LatLong(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public int compareTo(LatLong latLong) {
        if (this.longitude > latLong.longitude) {
            return 1;
        } else if (this.longitude < latLong.longitude) {
            return -1;
        } else if (this.latitude > latLong.latitude) {
            return 1;
        } else if (this.latitude < latLong.latitude) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns the approximate distance in degrees between this location and the
     * given location, calculated in Euclidean space.
     */
    public double distance(LatLong other) {
        return Math.hypot(this.longitude - other.longitude, this.latitude - other.latitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof LatLong)) {
            return false;
        }
        LatLong other = (LatLong) obj;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        } else if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("lat=");
        stringBuilder.append(String.format("%.4f", this.latitude));
        // stringBuilder.append(this.latitude);
        stringBuilder.append(", long=");
        stringBuilder.append(String.format("%.4f", this.longitude));
        // stringBuilder.append(this.longitude);
        return stringBuilder.toString();
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Computes the great circle distance between this GeoPoint instance and {point} argument.
     *
     * @param radius the radius of the sphere, e.g. the average radius for a spherical approximation of the figure of the Earth is
     *               approximately 6371.01 kilometers.
     * @return the distance, measured in the same unit as the radius argument.
     */
    public double distanceTo(LatLong point, double radius, GeoDistanceUnit unit) {
        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);
        double pointRadLat = Math.toRadians(point.latitude);
        double pointRadLon = Math.toRadians(point.longitude);

        double rad = Math.sin(radLat) * Math.sin(pointRadLat) +
                Math.cos(radLat) * Math.cos(pointRadLat) *
                        Math.cos(radLon - pointRadLon);

        // Valid result is in range -1.0..+1.0
        rad = (rad < -1.0) ? -1.0 : (rad > 1.0) ? 1.0 : rad;

        return unit.fromKm(Math.acos(rad) * radius);
    }

    /* Same as above, but we assume that we're referring to coordinates on planet earth. */
    public double distanceTo(LatLong point, GeoDistanceUnit unit) {
        return distanceTo(point, EARTH_RADIUS, unit);
    }

    /**
     * Destination point given distance and bearing from start point
     * <p>
     * Given a start point, initial bearing, and distance, this will calculate the destination point and final bearing travelling along a (shortest distance) great circle arc.
     *
     * @param distance
     * @param bearing
     * @param unit
     * @return
     */
    public LatLong offsetBy(double distance, double bearing, GeoDistanceUnit unit) {
        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);

        double d = unit.toKm(distance) / EARTH_RADIUS;
        double b = Math.toRadians(bearing);

        double lat = Math.asin(Math.sin(radLat) * Math.cos(d) +
                Math.cos(radLat) * Math.sin(d) * Math.cos(b));
        double lon = radLon + Math.atan2(Math.sin(b) * Math.sin(d) * Math.cos(radLat),
                Math.cos(d) - Math.sin(radLat) * Math.sin(lat));

        lon = (lon + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new LatLong(Math.toDegrees(lat), Math.toDegrees(lon));
    }
}
