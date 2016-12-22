package de.longri.cachebox3.locator.geocluster;

//import org.testng.annotations.Test;


import org.junit.jupiter.api.Test;
import org.oscim.core.GeoPoint;

import static de.longri.cachebox3.locator.geocluster.GeoPointMatchers.closeTo;
import static de.longri.cachebox3.locator.geocluster.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GeoClusterTests {

    @Test
    public void testGrow() {

        GeoCluster cluster = new GeoCluster(DENVER);
        assertThat("Cluster size after adding the first point", cluster.size(), equalTo(1));
        assertThat("Center after adding the first point", cluster.center(), closeTo(DENVER));
        assertThat("Top left corner after adding the first point", cluster.bounds().topLeft(), closeTo(DENVER));
        assertThat("Bottom right corner after adding the first point", cluster.bounds().bottomRight(), closeTo(DENVER));

        cluster.add(LAS_VEGAS);
        assertThat("Cluster size after adding a second point", cluster.size(), equalTo(2));
        assertThat("Center after adding a second point", cluster.center(), closeTo(new GeoPoint(37.915, -110.02)));
        assertThat("Top left corner after adding a second point", cluster.bounds().topLeft(), closeTo(new GeoPoint(DENVER.getLatitude(), LAS_VEGAS.getLongitude())));
        assertThat("Bottom right corner after adding a second point", cluster.bounds().bottomRight(), closeTo(new GeoPoint(LAS_VEGAS.getLatitude(), DENVER.getLongitude())));

        cluster.add(SAN_DIEGO);
        assertThat("Cluster size after adding a third point", cluster.size(), equalTo(3));
        assertThat("Center after adding a third point", cluster.center(), closeTo(new GeoPoint(36.217, -112.39)));
        assertThat("Top left corner after adding a third point", cluster.bounds().topLeft(), closeTo(new GeoPoint(DENVER.getLatitude(), SAN_DIEGO.getLongitude())));
        assertThat("Bottom right corner after adding a third point", cluster.bounds().bottomRight(), closeTo(new GeoPoint(SAN_DIEGO.getLatitude(), DENVER.getLongitude())));
    }

    @Test
    public void testMerge() {
        GeoCluster cluster = new GeoCluster(1, LAS_VEGAS, new GeoBoundingBox(LAS_VEGAS));
        GeoCluster merged = cluster.merge(new GeoCluster(2, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO)));
        assertThat(merged.size(), equalTo(3));
        assertThat(merged.center(), closeTo(new GeoPoint(33.9067, -116.4767)));
        assertThat(merged.bounds(), equalTo(new GeoBoundingBox(LAS_VEGAS).extend(SAN_DIEGO)));
    }
}
