package de.longri.cachebox3.locator.geocluster;

//import org.testng.annotations.Test;

import org.junit.jupiter.api.Test;

import static de.longri.cachebox3.locator.geocluster.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GeoBoundingBoxTests {

    @Test
    public void testSize() {
        assertThat("Size of Denver", new GeoBoundingBox(DENVER, DENVER).size(GeoDistanceUnit.KILOMETERS), equalTo(0.0));
        assertThat("Size of Colorado", COLORADO.size(GeoDistanceUnit.KILOMETERS), greaterThan(750.0));
    }

    @Test
    public void testContains() {
        assertThat("Top left corner is in bounds", COLORADO.contains(COLORADO.topLeft()), is(true));
        assertThat("Bottom right corner is in bounds", COLORADO.contains(COLORADO.bottomRight()), is(true));
        assertThat("Denver is in Colorado", COLORADO.contains(DENVER), is(true));
        assertThat("Las Vegas is in Colorado", COLORADO.contains(LAS_VEGAS), is(false));
        assertThat("Oslo is in Colorado", COLORADO.contains(OSLO), is(false));
    }

    @Test
    public void testExtend() {
        GeoBoundingBox southwest = COLORADO.extend(SAN_DIEGO);
        assertThat("Denver is in the SW", southwest.contains(DENVER), is(true));
        assertThat("Las Vegas is in the SW", southwest.contains(LAS_VEGAS), is(true));
    }

    @Test
    public void testExtend5km() {
        GeoBoundingBox troms = TROMS;
        GeoBoundingBox tromsExtended = TROMS.extend(5.0, GeoDistanceUnit.KILOMETERS);
        assertThat("Troms bounding box extended with 10km is X", tromsExtended.size(GeoDistanceUnit.KILOMETERS), closeTo(troms.size(GeoDistanceUnit.KILOMETERS) + 10.0 , 0.1));

    }

    @Test
    public void testExtend25pct() {
        GeoBoundingBox troms = TROMS;
        GeoBoundingBox tromsExtended = TROMS.extend(0.25);
        assertThat("Troms bounding box extended by 25% is X", tromsExtended.size(GeoDistanceUnit.KILOMETERS), closeTo(troms.size(GeoDistanceUnit.KILOMETERS) * 1.5 , 0.3));

    }
}
