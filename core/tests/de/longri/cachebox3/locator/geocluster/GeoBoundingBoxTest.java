package de.longri.cachebox3.locator.geocluster;

import org.junit.jupiter.api.Test;
import org.oscim.core.Box;
import org.oscim.core.GeoPoint;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 04.01.17.
 */
class GeoBoundingBoxTest {

    @Test
    void constructor() {

        double latTopLeft = 0.0;
        double lonTopLeft = 0.0;
        double latBotomRight = 10.0;
        double lonBotomright = 10.0;

        GeoPoint geoPoint = new GeoPoint(5.0, 5.0);

        Box box = new Box(latTopLeft, lonTopLeft, latBotomRight, lonBotomright);

        GeoBoundingBox geoBoundingBox = new GeoBoundingBox(box);
        assertThat("Point must inside Box", geoBoundingBox.contains(geoPoint));

    }


    @Test
    void contains() {

    }

}