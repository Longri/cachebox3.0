package de.longri.cachebox3.locator.geocluster;


import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBox;

public interface Places {

    GeoBoundingBox COLORADO = new GeoBoundingBox(new LatLong(41.00, -109.05), new LatLong(37.00, -102.04));
    GeoBoundingBox TROMS = new GeoBoundingBox(new LatLong(70.6, 16.0), new LatLong(68.4, 21.9));


    LatLong DENVER = new LatLong(39.75, -104.87);
    LatLong LOS_ANGELES = new LatLong(34.05, -118.24);
    LatLong LAS_VEGAS = new LatLong(36.08, -115.17);
    LatLong SAN_DIEGO = new LatLong(32.82, -117.13);
    LatLong OSLO = new LatLong(59.91, 10.75);
    LatLong BARDU = new LatLong(68.86175, 18.33674);
    LatLong BARDU_5KM_SOUTH = new LatLong(68.81678, 18.33674);
}
