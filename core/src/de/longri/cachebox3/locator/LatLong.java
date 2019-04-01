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

/**
 * A LatLong represents an immutable pair of latitude and longitude coordinates.
 */
public class LatLong {

    /**
     * The latitude coordinate of this LatLong in degrees.
     */
    protected double latitude;

    /**
     * The longitude coordinate of this LatLong in degrees.
     */
    protected double longitude;

    protected int hash = 0;

    public LatLong(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLong(LatLong latLon) {
        this.latitude = latLon.latitude;
        this.longitude = latLon.longitude;
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
        if (other.hashCode() != this.hashCode()) return false;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        } else if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) return hash;
        int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        hash = result;
        return hash;
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public LatLong copy() {
        return new LatLong(this.latitude, this.longitude);
    }

    public void setLatLon(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hash = 0;
    }

}
