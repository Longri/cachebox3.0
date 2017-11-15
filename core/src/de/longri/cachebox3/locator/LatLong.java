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

import java.io.Serializable;

/**
 * A LatLong represents an immutable pair of latitude and longitude coordinates.
 */
public class LatLong implements  Serializable {
    private static final long serialVersionUID = 1L;


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
        stringBuilder.append(", lon=");
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

}
