package de.longri.cachebox3.locator.geocluster;

import org.junit.jupiter.api.Test;
import org.oscim.core.Box;
import org.oscim.core.GeoPoint;
import org.oscim.core.MercatorProjection;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 04.01.17.
 */
class GeoBoundingBoxTest {

    @Test
    void constructor() {

        double latTopLeft = 52.581;
        double lonTopLeft = 13.396;
        double latBotomRight = 52.579;
        double lonBotomright = 13.400;
        double latCenter = 52.580;
        double lonCenter = 13.398;

        GeoPoint leftTop = new GeoPoint(latTopLeft, lonTopLeft);
        GeoPoint rightBotom = new GeoPoint(latBotomRight, lonBotomright);
        GeoPoint geoPoint = new GeoPoint(latCenter, lonCenter);


        GeoBoundingBox gbb = new GeoBoundingBox(leftTop, rightBotom);
        assertThat("Point must inside Box", gbb.contains(geoPoint));


        Box box = new Box(MercatorProjection.longitudeToX(lonTopLeft)
                , MercatorProjection.latitudeToY(latTopLeft)
                , MercatorProjection.longitudeToX(lonBotomright)
                , MercatorProjection.latitudeToY(latBotomRight));

        box.map2mercator();
        assertThat("Point must inside Box", box.contains(lonCenter, latCenter));

        GeoBoundingBox geoBoundingBox = new GeoBoundingBox(box);
        assertThat("Point must inside Box", geoBoundingBox.contains(geoPoint));

    }


    @Test
    void contains() {

    }

}