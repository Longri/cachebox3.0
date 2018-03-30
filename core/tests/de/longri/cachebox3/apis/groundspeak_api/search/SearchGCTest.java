/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import de.longri.cachebox3.sqlite.dao.LogDAO;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 14.04.17.
 */
class SearchGCTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {
        String expected = TestUtils.getResourceRequestString("testsResources/SearchGc_request.txt",
                isDummy ? null : apiKey);

        Database testDB = TestUtils.getTestDB(false);

        SearchGC searchGC = new SearchGC(testDB, apiKey, "GC1T33T");

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);
        searchGC.getRequest(json);

        String actual = writer.toString();
        assertEquals(expected, actual, "Should be equals");

        testDB.close();
        testDB.getFileHandle().delete();
    }


    @Test
    void parseJsonResult() throws IOException {
        final InputStream resultStream = TestUtils.getResourceRequestStream("testsResources/SearchGc_result.txt");
        final Database testDB = TestUtils.getTestDB(false);
        final SearchGC searchGC = new SearchGC(testDB, apiKey, "GC1T33T") {

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

                assertEquals(value, ApiResultState.IO);

                CacheList3DAO list3DAO = new CacheList3DAO();
                CacheList cacheList = new CacheList();
                list3DAO.readCacheList(testDB, cacheList, null, true, true);


                assertEquals(1, cacheList.size);
                AbstractCache abstractCache = cacheList.pop();

                assertEquals(false, abstractCache.isArchived());
                assertEquals(true, abstractCache.isAvailable());
                assertEquals("GC1T33T", abstractCache.getGcCode().toString());
                assertEquals(0, abstractCache.getWaypoints().size);
                assertEquals(CacheTypes.Traditional, abstractCache.getType());
                assertEquals(CacheSizes.other, abstractCache.getSize());
                assertEquals("Germany", abstractCache.getCountry(testDB).toString());
                assertEquals(new Date(1243753200000L), abstractCache.getDateHidden(testDB));
                assertEquals(3f, abstractCache.getDifficulty());
                assertEquals("", abstractCache.getHint(testDB).toString());
                assertEquals(12, abstractCache.getFavoritePoints());
                assertEquals(true, abstractCache.isFound());
                assertEquals("1260177", abstractCache.getGcId().toString());
                assertTrue(abstractCache.getLongDescription(testDB).startsWith("<div style=\"text-align:center;\">Eine Hunderunde gedreht und mal "));
                assertEquals("Nur ein Berg", abstractCache.getName().toString());
                assertEquals("Wurzellisel", abstractCache.getOwner().toString());
                assertEquals("Wurzellisel", abstractCache.getPlacedBy().toString());
                assertEquals("\r\n", abstractCache.getShortDescription(testDB).toString());
                assertEquals(2f, abstractCache.getTerrain());
                assertEquals("http://coord.info/GC1T33T", abstractCache.getUrl(testDB).toString());
                assertEquals(2, abstractCache.getApiState(testDB));
                assertEquals(52.579267, abstractCache.getLatitude());
                assertEquals(13.381983, abstractCache.getLongitude());

                // Attribute Tests

                ArrayList<Attributes> positiveList = new ArrayList<>();
                ArrayList<Attributes> negativeList = new ArrayList<>();

                {
                    positiveList.add(Attributes.Dogs);
                    positiveList.add(Attributes.Recommended_for_kids);
                    positiveList.add(Attributes.Available_at_all_times);
                    positiveList.add(Attributes.Available_during_winter);
                    positiveList.add(Attributes.Ticks);
                    positiveList.add(Attributes.Bicycles);
                    positiveList.add(Attributes.Stealth_required);

                    negativeList.add(Attributes.Wheelchair_accessible);
                    negativeList.add(Attributes.Horses);
                    negativeList.add(Attributes.Campfires);

                }

                TestUtils.assetCacheAttributes(testDB, abstractCache, positiveList, negativeList);

                LogDAO dao = new LogDAO();
                Array<LogEntry> logList = dao.getLogs(testDB, null);


                //check Logs
                assertEquals(10, logList.size);
                LogEntry logEntry = logList.first();

                assertEquals(AbstractCache.GenerateCacheId(abstractCache.getGcCode().toString()), logEntry.CacheId);
                assertEquals(677446155, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Heute fand das Event  GC73332 Eiergolf"));
                assertEquals(new Date(1492196400000L), logEntry.Timestamp);
                assertEquals("TeamReitenwolfgang", logEntry.Finder);

                logEntry = logList.get(logList.size - 1);

                assertEquals(AbstractCache.GenerateCacheId(abstractCache.getGcCode().toString()), logEntry.CacheId);
                assertEquals(663746391, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Ein freundliches Hallo an Wurzellisel,"));
                assertEquals(new Date(1486497600000L), logEntry.Timestamp);
                assertEquals("kerbholz", logEntry.Finder);


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

        testDB.close();
        testDB.getFileHandle().delete();
    }


    @Test
    void parseJsonExpiredResult() throws IOException {
        final InputStream resultStream = TestUtils.getResourceRequestStream("testsResources/SearchGc_ExpiredResult.txt");
        final CB_List<AbstractCache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        Database testDB = TestUtils.getTestDB(false);
        final SearchGC searchGC = new SearchGC(testDB, apiKey, "GC1T33T") {
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
                assertTrue(value == ApiResultState.EXPIRED_API_KEY, "ApiResult must be 'Key Expired'");
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
        testDB.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testDB.getFileHandle().delete();
    }


    @Test
    void testOnline() {
        if (isDummy) return;
        final Database testDB = TestUtils.getTestDB(false);
        final SearchGC searchGC = new SearchGC(testDB, apiKey, "GC1T33T") {
        };

        //results
        final long gpxFilenameId = 10;
        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final ApiResultState[] VALUE = new ApiResultState[1];
        searchGC.postRequest(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                VALUE[0] = value;
                WAIT.set(false);
            }
        }, gpxFilenameId);

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        assertThat("API key expired, can't test!", VALUE[0] != ApiResultState.EXPIRED_API_KEY);
        assertEquals(ApiResultState.IO, VALUE[0]);


        CacheList3DAO list3DAO = new CacheList3DAO();
        CacheList cacheList = new CacheList();
        list3DAO.readCacheList(testDB, cacheList, null, true, true);

        assertEquals(1, cacheList.size);
        AbstractCache abstractCache = cacheList.pop();

        assertEquals(false, abstractCache.isArchived());
        assertEquals(true, abstractCache.isAvailable());
        assertEquals("GC1T33T", abstractCache.getGcCode().toString());
        assertEquals(0, abstractCache.getWaypoints().size);
        assertEquals(CacheTypes.Traditional, abstractCache.getType());
        assertEquals(CacheSizes.other, abstractCache.getSize());
        assertEquals("Germany", abstractCache.getCountry(testDB).toString());
        assertEquals(new Date(1243753200000L), abstractCache.getDateHidden(testDB));
        assertEquals(3f, abstractCache.getDifficulty());
        assertEquals("", abstractCache.getHint(testDB).toString());
        assertEquals(12, abstractCache.getFavoritePoints());
        assertEquals(true, abstractCache.isFound());
        assertEquals("1260177", abstractCache.getGcId().toString());
        assertTrue(abstractCache.getLongDescription(testDB).startsWith("<div style=\"text-align:center;\">Eine Hunderunde gedreht und mal "));
        assertEquals("Nur ein Berg", abstractCache.getName().toString());
        assertEquals("Wurzellisel", abstractCache.getOwner().toString());
        assertEquals("Wurzellisel", abstractCache.getPlacedBy().toString());
        assertEquals("\r\n", abstractCache.getShortDescription(testDB).toString());
        assertEquals(2f, abstractCache.getTerrain());
        assertEquals("http://coord.info/GC1T33T", abstractCache.getUrl(testDB));
        assertEquals(2, abstractCache.getApiState(testDB));
        assertEquals(52.579267, abstractCache.getLatitude());
        assertEquals(13.381983, abstractCache.getLongitude());

        // Attribute Tests

        ArrayList<Attributes> positiveList = new ArrayList<>();
        ArrayList<Attributes> negativeList = new ArrayList<>();

        {
            positiveList.add(Attributes.Dogs);
            positiveList.add(Attributes.Recommended_for_kids);
            positiveList.add(Attributes.Available_at_all_times);
            positiveList.add(Attributes.Available_during_winter);
            positiveList.add(Attributes.Ticks);
            positiveList.add(Attributes.Bicycles);
            positiveList.add(Attributes.Stealth_required);

            negativeList.add(Attributes.Wheelchair_accessible);
            negativeList.add(Attributes.Horses);
            negativeList.add(Attributes.Campfires);

        }

        TestUtils.assetCacheAttributes(testDB, abstractCache, positiveList, negativeList);


        testDB.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testDB.getFileHandle().delete();
    }

    @Test
    public void testOnlineLite() {
        if (isDummy) return;

        final Database testDB = TestUtils.getTestDB(false);
        final SearchGC searchGC = new SearchGC(testDB, apiKey, "GC1T33T") {

        };

        searchGC.setIsLite(true);

        //results
        final long gpxFilenameId = 10;
        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final ApiResultState[] VALUE = new ApiResultState[1];
        searchGC.postRequest(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {

                VALUE[0] = value;
                WAIT.set(false);


                WAIT.set(false);
            }
        }, gpxFilenameId);

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertThat("API key expired, can't test!", VALUE[0] != ApiResultState.EXPIRED_API_KEY);
        assertEquals(ApiResultState.IO, VALUE[0]);

        CacheList3DAO list3DAO = new CacheList3DAO();
        CacheList cacheList = new CacheList();
        list3DAO.readCacheList(testDB, cacheList, null, true, true);


        assertEquals(1, cacheList.size);
        AbstractCache abstractCache = cacheList.pop();

        assertEquals(false, abstractCache.isArchived());
        assertEquals(true, abstractCache.isAvailable());
        assertEquals("GC1T33T", abstractCache.getGcCode().toString());
        assertEquals(0, abstractCache.getWaypoints().size);
        assertEquals(CacheTypes.Traditional, abstractCache.getType());
        assertEquals(CacheSizes.other, abstractCache.getSize());
        assertEquals("", abstractCache.getCountry(testDB).toString());
        assertEquals(3f, abstractCache.getDifficulty());
        assertEquals("", abstractCache.getHint(testDB).toString());
        assertEquals(12, abstractCache.getFavoritePoints());
        assertEquals(true, abstractCache.isFound());
        assertEquals("1260177", abstractCache.getGcId().toString());
        assertEquals("", abstractCache.getLongDescription(testDB).toString());
        assertEquals("Nur ein Berg", abstractCache.getName().toString());
        assertEquals("Wurzellisel", abstractCache.getOwner().toString());
        assertEquals("Wurzellisel", abstractCache.getPlacedBy().toString());
        assertEquals("", abstractCache.getShortDescription(testDB).toString());
        assertEquals(2f, abstractCache.getTerrain());
        assertEquals("http://coord.info/GC1T33T", abstractCache.getUrl(testDB).toString());
        assertEquals(2, abstractCache.getApiState(testDB));
        assertEquals(52.579267, abstractCache.getLatitude());
        assertEquals(13.381983, abstractCache.getLongitude());

        // Attribute Tests

        ArrayList<Attributes> positiveList = new ArrayList<>();
        ArrayList<Attributes> negativeList = new ArrayList<>();

        TestUtils.assetCacheAttributes(testDB, abstractCache, positiveList, negativeList);


        testDB.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testDB.getFileHandle().delete();
    }


}
