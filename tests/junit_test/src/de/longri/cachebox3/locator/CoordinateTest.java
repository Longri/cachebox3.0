/*
 * Copyright (C) 2020 team-cachebox.de
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

import de.longri.cachebox3.utils.GeoUtils;
import de.longri.cachebox3.utils.MathUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;



/**
 * Created by Longri on 25.10.2017.
 */
class CoordinateTest {

    @Test
    void project() {

        int distance = 24;//meter

        Coordinate c1 = new Coordinate(1, 1);
        Coordinate c2 = GeoUtils.project(c1, 12, distance);
        int calculatedDistance = Math.round(c1.distance(c2, MathUtils.CalculationType.FAST));

        assertEquals(distance, calculatedDistance, "Distance must charSequenceEquals");

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

    @Test
    void equalsTest() {
        Coordinate corrd1 = new Coordinate("N 52° 36.307 E 013° 21.517");
        Coordinate corrd2 = new Coordinate("N 52° 36.435 E 013° 21.821");
        Coordinate corrd3 = new Coordinate("N 52° 36.045 E 013° 21.760");

        Coordinate corrd4 = new Coordinate("N 52° 36.307 E 013° 21.517");
        Coordinate corrd5 = new Coordinate("N 52° 36.435 E 013° 21.821");
        Coordinate corrd6 = new Coordinate("N 52° 36.045 E 013° 21.760");

        Coordinate corrd7 = new Coordinate("N 52° 36.045000005 E 013° 21.760000002");


        assertTrue(corrd1.equals(corrd1), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd2.equals(corrd2), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd3.equals(corrd3), "Coordinates.charSequenceEquals() must be true");

        assertTrue(corrd4.equals(corrd4), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd5.equals(corrd5), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd6.equals(corrd6), "Coordinates.charSequenceEquals() must be true");

        assertTrue(corrd1.equals(corrd4), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd2.equals(corrd5), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd3.equals(corrd6), "Coordinates.charSequenceEquals() must be true");

        assertTrue(corrd7.equals(corrd6), "Coordinates.charSequenceEquals() must be true");
        assertTrue(corrd6.equals(corrd7), "Coordinates.charSequenceEquals() must be true");

        assertFalse(corrd2.equals(corrd1), "Coordinates.charSequenceEquals() must be false");
        assertFalse(corrd1.equals(corrd2), "Coordinates.charSequenceEquals() must be false");

        assertFalse(corrd3.equals(corrd1), "Coordinates.charSequenceEquals() must be false");
        assertFalse(corrd1.equals(corrd3), "Coordinates.charSequenceEquals() must be false");

        assertFalse(corrd2.equals(corrd3), "Coordinates.charSequenceEquals() must be false");
        assertFalse(corrd3.equals(corrd2), "Coordinates.charSequenceEquals() must be false");

    }

    @Test
    void resetHashTest() {
        Coordinate RESET = new Coordinate();

        assertFalse(RESET.isValid());
        assertTrue(RESET.isZero());
        assertEquals(-1836045267, RESET.hashCode());


        Coordinate TEST_COORD = new Coordinate();
        checkObjecktsResetet(RESET, TEST_COORD);

        TEST_COORD.setLatitude(53.12345);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(1097925113, TEST_COORD.hashCode());
        assertNotEquals(RESET, TEST_COORD);

        TEST_COORD.reset();
        checkObjecktsResetet(RESET, TEST_COORD);

        TEST_COORD.setLongitude(53.12345);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLongitude());
        assertEquals(613903585, TEST_COORD.hashCode());
        assertNotEquals(RESET, TEST_COORD);

        TEST_COORD.reset();
        checkObjecktsResetet(RESET, TEST_COORD);

        TEST_COORD.setLatLon(53.12345, 13.12345);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(13.12345, TEST_COORD.getLongitude());
        assertEquals(1031028746, TEST_COORD.hashCode());
        assertNotEquals(RESET, TEST_COORD);

        TEST_COORD.set(RESET);
        checkObjecktsResetet(RESET, TEST_COORD);


        Date date = new Date(123456);
        Coordinate coordinate = new Coordinate(53.12345, 13.12345, 46.23, 183.123456, date);
        TEST_COORD.setLatLon(53.12345, 13.12345);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(13.12345, TEST_COORD.getLongitude());
        assertEquals(1031028746, TEST_COORD.hashCode());

        TEST_COORD.setElevation(46.23);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(13.12345, TEST_COORD.getLongitude());
        assertEquals(46.23, TEST_COORD.getElevation());
        assertEquals(559802583, TEST_COORD.hashCode());

        TEST_COORD.setHeading(183.123456);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(13.12345, TEST_COORD.getLongitude());
        assertEquals(46.23, TEST_COORD.getElevation());
        assertEquals(183.123456, TEST_COORD.getHeading());
        assertEquals(1130699315, TEST_COORD.hashCode());

        TEST_COORD.setDate(date);
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(13.12345, TEST_COORD.getLongitude());
        assertEquals(46.23, TEST_COORD.getElevation());
        assertEquals(183.123456, TEST_COORD.getHeading());
        assertEquals(date, TEST_COORD.getDate());
        assertEquals(1130822771, TEST_COORD.hashCode());

        TEST_COORD.reset();
        checkObjecktsResetet(RESET, TEST_COORD);

        coordinate.reset();
        checkObjecktsResetet(RESET, coordinate);


        TEST_COORD.setLatLon(53.12345, 13.12345);
        assertFalse(TEST_COORD.isGPSprovided());
        assertEquals(0, TEST_COORD.getSpeed());
        assertEquals(-1, TEST_COORD.getAccuracy());
        TEST_COORD.setIsGpsProvided(true);
        TEST_COORD.setSpeed(320);
        TEST_COORD.setAccuracy(23.4567f); // will round to int (23)
        assertTrue(TEST_COORD.isValid());
        assertFalse(TEST_COORD.isZero());
        assertTrue(TEST_COORD.isGPSprovided());
        assertEquals(53.12345, TEST_COORD.getLatitude());
        assertEquals(13.12345, TEST_COORD.getLongitude());
        assertEquals(320, TEST_COORD.getSpeed());
        assertEquals(23, TEST_COORD.getAccuracy());
        assertEquals(1507626552, TEST_COORD.hashCode());
        assertNotEquals(RESET, TEST_COORD);

        TEST_COORD.reset();
        checkObjecktsResetet(RESET, TEST_COORD);
    }

    private void checkObjecktsResetet(Coordinate RESET, Coordinate TEST_COORD) {
        assertFalse(TEST_COORD.isValid());
        assertTrue(TEST_COORD.isZero());
        assertEquals(-1836045267, TEST_COORD.hashCode());
        assertEquals(RESET, TEST_COORD);
    }

}