/*
 * Copyright (C) 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;
import org.oscim.core.Box;
import org.oscim.core.GeoPoint;
import org.oscim.core.MercatorProjection;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 04.01.17.
 */
class GeoBoundingBoxTest {

    static {
        TestUtils.initialGdx();
    }

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


        GeoBoundingBoxInt gbb = new GeoBoundingBoxInt(leftTop, rightBotom);
        assertThat("Point must inside Box", gbb.contains(geoPoint));


        Box box = new Box(MercatorProjection.longitudeToX(lonTopLeft)
                , MercatorProjection.latitudeToY(latTopLeft)
                , MercatorProjection.longitudeToX(lonBotomright)
                , MercatorProjection.latitudeToY(latBotomRight));

        box.map2mercator();
        assertThat("Point must inside Box", box.contains(lonCenter, latCenter));

        GeoBoundingBoxInt geoBoundingBox = new GeoBoundingBoxInt(box);
        assertThat("Point must inside Box", geoBoundingBox.contains(geoPoint));

    }


    @Test
    void contains() {

    }

}