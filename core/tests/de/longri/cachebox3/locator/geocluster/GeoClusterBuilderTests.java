package de.longri.cachebox3.locator.geocluster;

//import org.testng.annotations.Test;

import de.longri.cachebox3.locator.LatLong;

import org.junit.jupiter.api.Test;
import static de.longri.cachebox3.locator.geocluster.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class GeoClusterBuilderTests {

    @Test
    public void testClusterNone() {

        GeoClusterBuilder builder = new GeoClusterBuilder(0.0);
        assertThat(builder.build().size(), equalTo(0));

        builder.add(DENVER);
        assertThat("Cluster after adding Denver", builder.build(), hasItems(
                new GeoCluster(1, DENVER, new GeoBoundingBox(DENVER))));

        builder.add(DENVER);
        assertThat("Cluster after adding Denver again", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER))));

        builder.add(SAN_DIEGO);
        assertThat("Cluster after adding San Diego", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
                new GeoCluster(1, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO))));

        builder.add(LAS_VEGAS);
        assertThat("Cluster after adding Las Vegas", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
                new GeoCluster(1, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO)),
                new GeoCluster(1, LAS_VEGAS, new GeoBoundingBox(LAS_VEGAS))));
    }

    @Test
    public void testClusterSome() {

        GeoClusterBuilder builder = new GeoClusterBuilder(0.5);
        assertThat(builder.build().size(), equalTo(0));

        builder.add(DENVER);
        assertThat("Cluster after adding Denver", builder.build(), hasItems(
                new GeoCluster(1, DENVER, new GeoBoundingBox(DENVER))));

        builder.add(DENVER);
        assertThat("Cluster after adding Denver again", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER))));

        builder.add(SAN_DIEGO);
        assertThat("Cluster after adding San Diego", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
                new GeoCluster(1, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO))));

        builder.add(LAS_VEGAS);
        assertThat("Cluster after adding Las Vegas", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
                new GeoCluster(2, new LatLong(34.4500, -116.1500), new GeoBoundingBox(SAN_DIEGO).extend(LAS_VEGAS))));
    }

    @Test
    public void testClusterAll() {

        GeoClusterBuilder builder = new GeoClusterBuilder(1.0);
        assertThat(builder.build().size(), equalTo(0));

        builder.add(DENVER);
        assertThat("Cluster after adding Denver", builder.build(), hasItems(
                new GeoCluster(1, DENVER, new GeoBoundingBox(DENVER))));

        builder.add(DENVER);
        assertThat("Cluster after adding Denver again", builder.build(), hasItems(
                new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER))));

        builder.add(SAN_DIEGO);
        assertThat("Cluster after adding San Diego", builder.build(), hasItems(
                new GeoCluster(3, new LatLong(37.4400, -108.95666666666666), new GeoBoundingBox(DENVER).extend(SAN_DIEGO))));

        builder.add(LAS_VEGAS);
        assertThat("Cluster after adding Las Vegas", builder.build(), hasItems(
                new GeoCluster(4, new LatLong(37.099999999999994, -110.5100), new GeoBoundingBox(DENVER).extend(SAN_DIEGO).extend(LAS_VEGAS))));
    }

    @Test
    public void testClusterInitialBoundingBox() {
        GeoClusterBuilder builder = new GeoClusterBuilder(1.0, new GeoBoundingBox(LOS_ANGELES).extend(270, GeoDistanceUnit.MILES));
        assertThat(builder.build().size(), equalTo(0));

        builder.add(SAN_DIEGO);
        builder.add(LAS_VEGAS);

        assertThat("One cluster after adding SD and LV", builder.build().size(), equalTo(1));
    }
}
