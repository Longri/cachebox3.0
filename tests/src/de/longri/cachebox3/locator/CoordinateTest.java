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
package de.longri.cachebox3.locator;

import de.longri.cachebox3.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 25.10.2017.
 */
class CoordinateTest {

    @Test
    void project() {

        int distance = 24;//meter

        Coordinate c1 = new Coordinate(1, 1);
        Coordinate c2 = Coordinate.Project(c1, 12, distance);
        int calculatedDistance = Math.round(c1.distance(c2, MathUtils.CalculationType.FAST));

        assertEquals(distance, calculatedDistance, "Distance must equals");

    }

    @Test
    void stringConstructor() {
        String str = "N 48° 40.441 E 009° 23.470";
        Coordinate coord = new Coordinate(str);
        assertEquals(48.674017, coord.getLatitude());
        assertEquals(9.391167, coord.getLongitude());

        str = "52°34′49.7316″N 13°23′8.3364″E";
        coord = new Coordinate(str);
        assertEquals(52.580481, coord.getLatitude());
        assertEquals(13.385649, coord.getLongitude());

    }

}