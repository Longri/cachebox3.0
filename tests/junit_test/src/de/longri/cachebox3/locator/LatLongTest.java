/*
 * Copyright (C) 2019 team-cachebox.de
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 2019-03-24.
 */
public class LatLongTest {

    @Test
    void equalsTest() {

        LatLong latLong1 = new LatLong(52.605117, 13.358617);
        LatLong latLong2 = new LatLong(52.60725, 13.363683);
        LatLong latLong3 = new LatLong(52.60075, 13.362667);

        LatLong latLong4 = new LatLong(52.605117, 13.358617);
        LatLong latLong5 = new LatLong(52.60725, 13.363683);
        LatLong latLong6 = new LatLong(52.60075, 13.362667);

        LatLong latLong7 = new LatLong(52.6007500001, 13.362667000002);

        assertTrue(latLong1.equals(latLong1), "Coordinates.charSequenceEquals() must be true");
        assertTrue(latLong2.equals(latLong2), "Coordinates.charSequenceEquals() must be true");
        assertTrue(latLong3.equals(latLong3), "Coordinates.charSequenceEquals() must be true");

        assertTrue(latLong4.equals(latLong4), "Coordinates.charSequenceEquals() must be true");
        assertTrue(latLong5.equals(latLong5), "Coordinates.charSequenceEquals() must be true");
        assertTrue(latLong6.equals(latLong6), "Coordinates.charSequenceEquals() must be true");

        assertTrue(latLong1.equals(latLong4), "Coordinates.charSequenceEquals() must be true");
        assertTrue(latLong2.equals(latLong5), "Coordinates.charSequenceEquals() must be true");
        assertTrue(latLong3.equals(latLong6), "Coordinates.charSequenceEquals() must be true");

        assertFalse(latLong7.equals(latLong6), "Coordinates.charSequenceEquals() must be false");
        assertFalse(latLong6.equals(latLong7), "Coordinates.charSequenceEquals() must be false");

        assertFalse(latLong2.equals(latLong1), "Coordinates.charSequenceEquals() must be false");
        assertFalse(latLong1.equals(latLong2), "Coordinates.charSequenceEquals() must be false");

        assertFalse(latLong3.equals(latLong1), "Coordinates.charSequenceEquals() must be false");
        assertFalse(latLong1.equals(latLong3), "Coordinates.charSequenceEquals() must be false");

        assertFalse(latLong2.equals(latLong3), "Coordinates.charSequenceEquals() must be false");
        assertFalse(latLong3.equals(latLong2), "Coordinates.charSequenceEquals() must be false");

    }
}
