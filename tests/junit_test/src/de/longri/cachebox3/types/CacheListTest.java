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
package de.longri.cachebox3.types;

import com.badlogic.gdx.math.MathUtils;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 25.10.2017.
 */
class CacheListTest {
    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();
    }

    @Test
    void compareToTest() {

        CacheList caches = new CacheList();
        Coordinate myPosition = new Coordinate(1, 1);

        int distance = 23;

        addCacheToList(caches, "GC12DR6", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC14PAT", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC16JN5", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC16NXB", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC1JH4H", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC1M3A2", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC1RDE7", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC1ZTH3", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2588R", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2FG37", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2GR64", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2MGA2", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2NKQY", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2RMER", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2T4TW", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2T4VM", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2T4ZF", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2T50X", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC2VMBH", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC360HJ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC3GGFY", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC3JXNH", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC3TC0K", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC3XXT0", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC47VFB", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC487KE", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4ANRV", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4B2N0", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4CP0Y", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4CPAG", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4D5JY", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4D5K7", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4DJWK", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4MME7", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC4YB2Q", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC508F2", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC508FF", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC51APC", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC55KPT", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC55QAJ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC569RP", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC56BB2", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC56FVX", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC56FXQ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC56FYH", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5AYH2", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5BC60", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5BFJ3", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5BXEM", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5CF6E", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5GR8M", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5GZ18", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5H7VQ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5KT1G", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5NMA7", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5PNKC", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5R1P0", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5ZE9D", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC5ZEAC", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60F5D", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60GX5", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60JQH", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60K9C", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60KXP", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60MFQ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC60MQQ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M1R", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M20", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M23", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M2G", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M2N", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M31", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M37", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M3T", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M47", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M5P", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M7Q", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC61M87", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC64EEZ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC65FXD", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC65H6H", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC65P6N", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC65PXP", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC660RC", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC662VA", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC662XH", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC666QV", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC666RD", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC666RV", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC666T7", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC666TJ", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC66WAE", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC689HM", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC68QYF", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6CFTW", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6EKP4", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6GM7J", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6MB49", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6N911", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6NK72", getNextCoordinate(distance++, myPosition));
        addCacheToList(caches, "GC6QJED", getNextCoordinate(distance++, myPosition));

        List<MutableCache> shufleList = new ArrayList<>();
        for (Object ca : caches.toArray(MutableCache.class)) {
            shufleList.add((MutableCache) ca);
        }

        Collections.shuffle(shufleList);
        CacheList shuffledCacheList = new CacheList();
        for (MutableCache ca : shufleList) {
            shuffledCacheList.add(ca);
        }


        assertThat("Must not charSequenceEquals", !shuffledCacheList.equals(caches));
        shuffledCacheList.resort(myPosition, new CacheWithWP(null, null));
        assertThat("Must charSequenceEquals", shuffledCacheList.equals(caches));


    }

    private Coordinate getNextCoordinate(float distance, Coordinate pos) {
        float direction = MathUtils.random(0, 360);
        return Coordinate.Project(pos, direction, distance);
    }

    private void addCacheToList(CacheList caches, String gcCode, Coordinate coordinate) {
        caches.add(new MutableCache(coordinate.getLatitude(), coordinate.getLongitude(), "dummy", CacheTypes.Traditional, gcCode));
    }

}