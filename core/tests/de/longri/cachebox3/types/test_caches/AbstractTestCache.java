/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.types.test_caches;

import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.utils.CharSequenceUtilTest;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 31.03.18.
 */
public abstract class AbstractTestCache {
    double longitude;
    double latitude;
    CacheTypes cacheType;
    String gcCode;

    public void assertCache(AbstractCache other) {
        assertThat("Cache must not be NULL", other != null);
        assertThat("Latitude must be " + latitude + " but was :" + other.latitude, latitude == other.latitude);
        assertThat("Longitude must be " + longitude + " but was :" + other.longitude, longitude == other.longitude);
        assertThat("Cache type must be " + cacheType + " but was :" + other.getType(), cacheType == other.getType());
        assertThat("GcCode must be " + gcCode + " but was :" + other.getGcCode(), CharSequenceUtilTest.equals(gcCode, other.getGcCode()));
    }
}
