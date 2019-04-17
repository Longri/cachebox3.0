

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 20.06.2017.
 */
public class GeoUtilsTest {
    @Test
    public void parseCoordinate() throws PlatformAssertionError {
        String coordStr = "53° 36,135N / 10° 10,017E";
        Coordinate coordinate = new Coordinate(coordStr);

        assertThat("Coordinate must valid", coordinate.isValid());
        assertThat("Coordinate latitude must be", coordinate.getLatitude() == 53.60225);
        assertThat("Coordinate longitude must be", coordinate.getLongitude() == 10.16695);
    }

}
