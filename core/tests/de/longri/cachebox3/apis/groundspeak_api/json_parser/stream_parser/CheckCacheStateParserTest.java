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
package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.utils.ICancel;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 30.06.2017.
 */
class CheckCacheStateParserTest {

    @Test
    public void parseCacheStateStream() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/CheckCacheStateResult.json");
        CheckCacheStateParser parser = new CheckCacheStateParser();

        Array<Cache> caches = new Array<>();


        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC12DR6"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC14PAT"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC16JN5"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC16NXB"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1JH4H"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1M3A2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1RDE7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1ZTH3"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2588R"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2FG37"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2GR64"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2MGA2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2NKQY"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2RMER"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T4TW"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T4VM"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T4ZF"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T50X"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2VMBH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC360HJ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3GGFY"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3JXNH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3TC0K"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3XXT0"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC47VFB"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC487KE"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4ANRV"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4B2N0"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4CP0Y"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4CPAG"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4D5JY"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4D5K7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4DJWK"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4MME7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4YB2Q"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC508F2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC508FF"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC51APC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC55KPT"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC55QAJ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC569RP"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56BB2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56FVX"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56FXQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56FYH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5AYH2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5BC60"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5BFJ3"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5BXEM"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5CF6E"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5GR8M"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5GZ18"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5H7VQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5KT1G"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5NMA7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5PNKC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5R1P0"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5ZE9D"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5ZEAC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60F5D"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60GX5"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60JQH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60K9C"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60KXP"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60MFQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60MQQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M1R"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M20"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M23"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M2G"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M2N"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M31"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M37"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M3T"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M47"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M5P"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M7Q"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M87"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC64EEZ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65FXD"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65H6H"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65P6N"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65PXP"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC660RC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC662VA"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC662XH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666QV"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666RD"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666RV"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666T7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666TJ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC66WAE"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC689HM"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC68QYF"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6CFTW"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6EKP4"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6GM7J"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6MB49"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6N911"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6NK72"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6QJED"));

        Array<String> archieved = new Array<>();
        archieved.add("GC4CP0Y");
        archieved.add("GC5AYH2");
        archieved.add("GC60JQH");
        archieved.add("GC60K9C");
        archieved.add("GC60KXP");
        archieved.add("GC61M2N");
        archieved.add("GC61M5P");
        archieved.add("GC61M7Q");
        archieved.add("GC68QYF");

        Array<String> notAvaileble = new Array<>();
        notAvaileble.add("GC4CP0Y");
        notAvaileble.add("GC5AYH2");
        notAvaileble.add("GC60JQH");
        notAvaileble.add("GC60K9C");
        notAvaileble.add("GC60KXP");
        notAvaileble.add("GC61M2N");
        notAvaileble.add("GC61M5P");
        notAvaileble.add("GC61M7Q");
        notAvaileble.add("GC68QYF");
        notAvaileble.add("GC6EKP4");
        notAvaileble.add("GC6QJED");
        notAvaileble.add("GC12DR6");

        ArrayMap<String, Integer> trackableCount = new ArrayMap<>();
        trackableCount.put("GC2588R", 1);
        trackableCount.put("GC4D5JY", 1);
        trackableCount.put("GC5BC60", 1);
        trackableCount.put("GC6QJED", 5);
        trackableCount.put("GC1RDE7", 3);
        trackableCount.put("GC2RMER", 7);

        final AtomicInteger increment = new AtomicInteger(0);
        parser.parse(stream, caches, null, new CheckCacheStateParser.ProgressIncrement() {
            @Override
            public void increment() {
                increment.incrementAndGet();
            }
        });
        assertThat("Increment must be 101, but was " + increment.get(), increment.get() == 101);

        for (int i = 0, n = caches.size; i < n; i++) {
            Cache cache = caches.get(i);
            String gcCode = cache.getGcCode();

            if (archieved.contains(gcCode, false)) {
                assertThat("Cache must archived:" + gcCode, cache.isArchived());
            } else {
                assertThat("Cache must not archived:" + gcCode, !cache.isArchived());
            }

            if (notAvaileble.contains(gcCode, false)) {
                assertThat("Cache must not available:" + gcCode, !cache.isAvailable());
            } else {
                assertThat("Cache must available:" + gcCode, cache.isAvailable());
            }

            if (trackableCount.containsKey(gcCode)) {
                assertThat("Cache trackable count must be " + trackableCount.get(gcCode) + "on Cache:" + gcCode, cache.NumTravelbugs == trackableCount.get(gcCode));
            } else {
                assertThat("Cache trackable count must be 0 on Cache:" + gcCode, cache.NumTravelbugs == 0);
            }
        }


    }

    @Test
    public void parseCacheStateStreamCanceld() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/CheckCacheStateResult.json");
        final CheckCacheStateParser parser = new CheckCacheStateParser();

        Array<Cache> caches = new Array<>();


        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC12DR6"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC14PAT"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC16JN5"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC16NXB"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1JH4H"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1M3A2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1RDE7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC1ZTH3"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2588R"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2FG37"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2GR64"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2MGA2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2NKQY"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2RMER"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T4TW"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T4VM"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T4ZF"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2T50X"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC2VMBH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC360HJ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3GGFY"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3JXNH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3TC0K"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC3XXT0"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC47VFB"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC487KE"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4ANRV"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4B2N0"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4CP0Y"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4CPAG"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4D5JY"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4D5K7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4DJWK"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4MME7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC4YB2Q"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC508F2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC508FF"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC51APC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC55KPT"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC55QAJ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC569RP"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56BB2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56FVX"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56FXQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC56FYH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5AYH2"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5BC60"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5BFJ3"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5BXEM"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5CF6E"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5GR8M"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5GZ18"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5H7VQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5KT1G"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5NMA7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5PNKC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5R1P0"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5ZE9D"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC5ZEAC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60F5D"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60GX5"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60JQH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60K9C"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60KXP"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60MFQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC60MQQ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M1R"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M20"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M23"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M2G"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M2N"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M31"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M37"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M3T"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M47"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M5P"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M7Q"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC61M87"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC64EEZ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65FXD"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65H6H"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65P6N"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC65PXP"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC660RC"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC662VA"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC662XH"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666QV"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666RD"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666RV"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666T7"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC666TJ"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC66WAE"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC689HM"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC68QYF"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6CFTW"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6EKP4"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6GM7J"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6MB49"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6N911"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6NK72"));
        caches.add(new Cache(0, 0, "dummy", CacheTypes.Traditional, "GC6QJED"));

        Array<String> archieved = new Array<>();
        archieved.add("GC4CP0Y");
        archieved.add("GC5AYH2");
        archieved.add("GC60JQH");
        archieved.add("GC60K9C");
        archieved.add("GC60KXP");
        archieved.add("GC61M2N");
        archieved.add("GC61M5P");
        archieved.add("GC61M7Q");
        archieved.add("GC68QYF");

        Array<String> notAvaileble = new Array<>();
        notAvaileble.add("GC4CP0Y");
        notAvaileble.add("GC5AYH2");
        notAvaileble.add("GC60JQH");
        notAvaileble.add("GC60K9C");
        notAvaileble.add("GC60KXP");
        notAvaileble.add("GC61M2N");
        notAvaileble.add("GC61M5P");
        notAvaileble.add("GC61M7Q");
        notAvaileble.add("GC68QYF");
        notAvaileble.add("GC6EKP4");
        notAvaileble.add("GC6QJED");
        notAvaileble.add("GC12DR6");

        ArrayMap<String, Integer> trackableCount = new ArrayMap<>();
        trackableCount.put("GC2588R", 1);
        trackableCount.put("GC4D5JY", 1);
        trackableCount.put("GC5BC60", 1);
        trackableCount.put("GC6QJED", 5);
        trackableCount.put("GC1RDE7", 3);
        trackableCount.put("GC2RMER", 7);

        final AtomicInteger increment = new AtomicInteger(0);
        final AtomicBoolean canceled = new AtomicBoolean(false);
        parser.parse(stream, caches, new ICancel() {
            @Override
            public boolean cancel() {
                return canceled.get();
            }
        }, new CheckCacheStateParser.ProgressIncrement() {
            @Override
            public void increment() {
                if (increment.incrementAndGet() == 4) {
                    canceled.set(true);
                }

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        assertThat("Increment must smaller then 101, but was " + increment.get(), increment.get() < 101);

        for (int i = 0, n = increment.get(); i < n; i++) {
            Cache cache = caches.get(i);
            String gcCode = cache.getGcCode();

            if (archieved.contains(gcCode, false)) {
                assertThat("Cache must archived:" + gcCode, cache.isArchived());
            } else {
                assertThat("Cache must not archived:" + gcCode, !cache.isArchived());
            }

            if (notAvaileble.contains(gcCode, false)) {
                assertThat("Cache must not available:" + gcCode, !cache.isAvailable());
            } else {
                assertThat("Cache must available:" + gcCode, cache.isAvailable());
            }

            if (trackableCount.containsKey(gcCode)) {
                assertThat("Cache trackable count must be " + trackableCount.get(gcCode) + "on Cache:" + gcCode, cache.NumTravelbugs == trackableCount.get(gcCode));
            } else {
                assertThat("Cache trackable count must be 0 on Cache:" + gcCode, cache.NumTravelbugs == 0);
            }
        }
    }

}