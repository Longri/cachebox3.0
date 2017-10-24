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
package de.longri.cachebox3.apis.groundspeak_api.search;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.lists.CB_List;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 17.04.17.
 */
class SearchGcOwnerTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {
        String expected = TestUtils.getResourceRequestString("testsResources/SearchGcOwner_request.txt",
                isDummy ? null : apiKey);

        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)
        SearchGCOwner searchGC = new SearchGCOwner(apiKey, 30, searchCoord, 50000, "bros", (byte) 2);

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);
        searchGC.getRequest(json);

        String actual = writer.toString();
        assertEquals(expected, actual);
    }


    @Test
    void parseJsonResult() throws IOException {
        final InputStream resultStream = TestUtils.getResourceRequestStream("testsResources/SearchGcOwner_result.txt");
        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)


        final CB_List<AbstractCache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        final SearchGCOwner searchGC = new SearchGCOwner(apiKey, 30, searchCoord, 50000, "bros", (byte) 2) {
            protected void writeLogToDB(final LogEntry logEntry) {
                logList.add(logEntry);
            }

            protected void writeImagEntryToDB(final ImageEntry imageEntry) {
                imageList.add(imageEntry);
            }

            protected void writeCacheToDB(final AbstractCache cache) {
                cacheList.add(cache);
            }
        };

        final AtomicBoolean WAIT = new AtomicBoolean(true);

        Net.HttpResponse response = new Net.HttpResponse() {
            @Override
            public byte[] getResult() {
                return new byte[0];
            }

            @Override
            public String getResultAsString() {
                return null;
            }

            @Override
            public InputStream getResultAsStream() {
                return resultStream;
            }

            @Override
            public HttpStatus getStatus() {
                return null;
            }

            @Override
            public String getHeader(String name) {
                return null;
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }
        };

        searchGC.handleHttpResponse(response, new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {

                assertEquals(23, cacheList.size);
                AbstractCache abstractCache = cacheList.first();

                assertEquals(false, abstractCache.isArchived());
                assertEquals(true, abstractCache.isAvailable());
                assertEquals("GC18JGX", abstractCache.getGcCode());
                assertEquals(2, abstractCache.getWaypoints().size);

                AbstractWaypoint waypoint = abstractCache.getWaypoints().first();
                assertEquals("PA18JGX", waypoint.getGcCode());
                assertEquals("Parkmöglichkeit", waypoint.getDescription(Database.Data));
                assertEquals("Parking", waypoint.getTitle());
                assertEquals(CacheTypes.ParkingArea, waypoint.getType());
                assertEquals(52.633667, waypoint.getLatitude());
                assertEquals(13.375917, waypoint.getLongitude());

                AbstractWaypoint userWaypoint = abstractCache.getWaypoints().peek();
                assertEquals("CO18JGX", userWaypoint.getGcCode());
                assertEquals("", userWaypoint.getDescription(Database.Data));
                assertEquals("Corrected Coordinates (API)", userWaypoint.getTitle());
                assertEquals(CacheTypes.Final, userWaypoint.getType());
                assertEquals(52.616666666666667, userWaypoint.getLatitude());
                assertEquals(13.366666666666667, userWaypoint.getLongitude());
                assertEquals(true, userWaypoint.isUserWaypoint());

                assertEquals(CacheTypes.Traditional, abstractCache.getType());
                assertEquals(CacheSizes.small, abstractCache.getSize());
                assertEquals("Germany", abstractCache.getCountry());
                assertEquals(new Date(1200211200000L), abstractCache.getDateHidden());
                assertEquals(1.5f, abstractCache.getDifficulty());
                assertEquals("bücken!", abstractCache.getHint(Database.Data));
                assertEquals(0, abstractCache.getFavoritePoints());
                assertEquals(false, abstractCache.isFound());
                assertEquals("768551", abstractCache.getGcId());
                assertTrue(abstractCache.getLongDescription(Database.Data).startsWith("Vom empfohlenen Parkplatz beträgt die Wegstrecke etwa 500 m. Der Cac"));
                assertEquals("Weideblick", abstractCache.getName());
                assertEquals("bros", abstractCache.getOwner());
                assertEquals("bros", abstractCache.getPlacedBy());
                assertEquals("Ein weiterer Cache im Tegeler Fließtal", abstractCache.getShortDescription(Database.Data));
                assertEquals(2f, abstractCache.getTerrain());
                assertEquals("http://coord.info/GC18JGX", abstractCache.getUrl());
                assertEquals(2, abstractCache.getApiState());
                assertEquals(52.62965, abstractCache.getLatitude());
                assertEquals(13.372317, abstractCache.getLongitude());

                assertEquals(2, abstractCache.getWaypoints().size);

                // Attribute Tests

                ArrayList<Attributes> positiveList = new ArrayList<>();
                ArrayList<Attributes> negativeList = new ArrayList<>();

                {
                    positiveList.add(Attributes.Dogs);
                    positiveList.add(Attributes.Recommended_for_kids);
                    positiveList.add(Attributes.Takes_less_than_an_hour);
                    positiveList.add(Attributes.Scenic_view);
                    positiveList.add(Attributes.Public_transportation);
                    positiveList.add(Attributes.Bicycles);


                }

                TestUtils.assetCacheAttributes(abstractCache, positiveList, negativeList);


                //check Logs
                assertEquals(230, logList.size);
                LogEntry logEntry = logList.first();

                assertEquals(AbstractCache.GenerateCacheId(abstractCache.getGcCode().toString()), logEntry.CacheId);
                assertEquals(678589990, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Fast in Berlin, fast auf'm Lan"));
                assertEquals(new Date(1492455600000L), logEntry.Timestamp);
                assertEquals("w2kurlgeo", logEntry.Finder);

                logEntry = logList.last();

                assertEquals(AbstractCache.GenerateCacheId("GC3FHRP"), logEntry.CacheId);
                assertEquals(664057049, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Heute war ich am Morgen hier bei Neptun zu Besuch."));
                assertEquals(new Date(1486756800000L), logEntry.Timestamp);
                assertEquals("RJCK", logEntry.Finder);


                WAIT.set(false);
            }
        });

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    void testOnline() {
        if (isDummy) return;
        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)
        final CB_List<AbstractCache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        final SearchGCOwner searchGC = new SearchGCOwner(apiKey, 30, searchCoord, 50000, "bros", (byte) 2) {
            protected void writeLogToDB(final LogEntry logEntry) {
                logList.add(logEntry);
            }

            protected void writeImagEntryToDB(final ImageEntry imageEntry) {
                imageList.add(imageEntry);
            }

            protected void writeCacheToDB(final AbstractCache cache) {
                cacheList.add(cache);
            }
        };

        //results
        final long gpxFilenameId = 10;
        final AtomicBoolean WAIT = new AtomicBoolean(true);

        try {
            searchGC.postRequest(new GenericCallBack<ApiResultState>() {
                @Override
                public void callBack(ApiResultState value) {

                    try {
                        assertEquals(23, cacheList.size);
                        AbstractCache abstractCache = cacheList.first();

                        assertEquals(false, abstractCache.isArchived());
                        assertEquals(true, abstractCache.isAvailable());
                        assertEquals("GC18JGX", abstractCache.getGcCode());
                        assertEquals(2, abstractCache.getWaypoints().size);

                        AbstractWaypoint waypoint = abstractCache.getWaypoints().first();
                        assertEquals("PA18JGX", waypoint.getGcCode());
                        assertEquals("Parkmöglichkeit", waypoint.getDescription(Database.Data));
                        assertEquals("Parking", waypoint.getTitle());
                        assertEquals(CacheTypes.ParkingArea, waypoint.getType());
                        assertEquals(52.633667, waypoint.getLatitude());
                        assertEquals(13.375917, waypoint.getLongitude());

                        assertEquals(CacheTypes.Traditional, abstractCache.getType());
                        assertEquals(CacheSizes.other, abstractCache.getSize());
                        assertEquals("Germany", abstractCache.getCountry());
                        assertEquals(new Date(1200211200000L), abstractCache.getDateHidden());
                        assertEquals(1.5f, abstractCache.getDifficulty());
                        assertEquals("bücken!", abstractCache.getHint(Database.Data));
                        assertEquals(0, abstractCache.getFavoritePoints());
                        assertEquals(false, abstractCache.isFound());
                        assertEquals("768551", abstractCache.getGcId());
                        assertTrue(abstractCache.getLongDescription(Database.Data).startsWith("Vom empfohlenen Parkplatz beträgt die Wegstrecke etwa 500 m. Der Cac"));
                        assertEquals("Weideblick", abstractCache.getName());
                        assertEquals("bros", abstractCache.getOwner());
                        assertEquals("bros", abstractCache.getPlacedBy());
                        assertEquals("Ein weiterer Cache im Tegeler Fließtal", abstractCache.getShortDescription(Database.Data));
                        assertEquals(2f, abstractCache.getTerrain());
                        assertEquals("http://coord.info/GC18JGX", abstractCache.getUrl());
                        assertEquals(2, abstractCache.getApiState());
                        assertEquals(52.62965, abstractCache.getLatitude());
                        assertEquals(13.372317, abstractCache.getLongitude());

                        // Attribute Tests

                        ArrayList<Attributes> positiveList = new ArrayList<>();
                        ArrayList<Attributes> negativeList = new ArrayList<>();

                        {
                            positiveList.add(Attributes.Dogs);
                            positiveList.add(Attributes.Recommended_for_kids);
                            positiveList.add(Attributes.Takes_less_than_an_hour);
                            positiveList.add(Attributes.Scenic_view);
                            positiveList.add(Attributes.Public_transportation);
                            positiveList.add(Attributes.Bicycles);
                        }

                        TestUtils.assetCacheAttributes(abstractCache, positiveList, negativeList);


                        //check Logs
                        assertEquals(230, logList.size);
                        LogEntry logEntry = logList.first();

                        assertEquals(AbstractCache.GenerateCacheId(abstractCache.getGcCode().toString()), logEntry.CacheId);
                        logEntry = logList.last();

                        assertEquals(AbstractCache.GenerateCacheId("GC3FHRP"), logEntry.CacheId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    WAIT.set(false);
                }
            }, gpxFilenameId);
        } catch (Exception e) {
            e.printStackTrace();
            WAIT.set(false);
        }

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
