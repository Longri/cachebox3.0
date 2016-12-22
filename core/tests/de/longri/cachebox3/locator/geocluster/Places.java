package de.longri.cachebox3.locator.geocluster;


import de.longri.cachebox3.locator.geocluster.GeoBoundingBox;
import org.oscim.core.GeoPoint;

public interface Places {

    GeoBoundingBox COLORADO = new GeoBoundingBox(new GeoPoint(41.00, -109.05), new GeoPoint(37.00, -102.04));
    GeoBoundingBox TROMS = new GeoBoundingBox(new GeoPoint(70.6, 16.0), new GeoPoint(68.4, 21.9));


    GeoPoint DENVER = new GeoPoint(39.75, -104.87);
    GeoPoint LOS_ANGELES = new GeoPoint(34.05, -118.24);
    GeoPoint LAS_VEGAS = new GeoPoint(36.08, -115.17);
    GeoPoint SAN_DIEGO = new GeoPoint(32.82, -117.13);
    GeoPoint OSLO = new GeoPoint(59.91, 10.75);
    GeoPoint BARDU = new GeoPoint(68.86175, 18.33674);
    GeoPoint BARDU_5KM_SOUTH = new GeoPoint(68.81678, 18.33674);
}
