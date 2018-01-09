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
package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheList;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 23.10.2017.
 */
class CacheList3DAOTest {

    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    static void beforeAll() throws SQLiteGdxException {

        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3");
        copyDbFileHandle = testDbFileHandle.parent().child("testCacheListDAO.db3");
        if (copyDbFileHandle.exists()) {
            // delete first
            assertThat("TestDB must be deleted for cleanup", copyDbFileHandle.delete());
        }
        testDbFileHandle.copyTo(copyDbFileHandle);
        assertThat("TestDB must exist", copyDbFileHandle.exists());

        // open DataBase
        cb3Database = new Database(Database.DatabaseType.CacheBox3);
        cb3Database.startUp(copyDbFileHandle);

    }

    @AfterAll
    static void cleanUpRecources() {
        cb3Database.close();
        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }


    @Test
    void readCacheList() {

        AbstractCacheListDAO DAO = new CacheList3DAO();
        CacheList caches = new CacheList();

        DAO.readCacheList(cb3Database, caches, "", true, true);

        // Cachelist is Async loading, so wait a moment
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("CacheList must have 731 Caches but has:" + caches.size, caches.size == 731);

        AbstractCache cache = caches.getCacheById(14731588679189319L);
        assertThat("Cache must have one Waypoint but has:" + cache.getWaypoints().size, cache.getWaypoints().size == 1);


        {// check conversion of CacheSizes
            AbstractCache cache1 = caches.GetCacheByGcCode("GC1Z18H");
            assertThat("CacheSize must be 'Other', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.other);

            cache1 = caches.GetCacheByGcCode("GC2DCGK");
            assertThat("CacheSize must be 'Micro', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.micro);

            cache1 = caches.GetCacheByGcCode("GC5Q51E");
            assertThat("CacheSize must be 'Small', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.small);

            cache1 = caches.GetCacheByGcCode("GC3CTWZ");
            assertThat("CacheSize must be 'Regular', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.regular);

            cache1 = caches.GetCacheByGcCode("GC58TMQ");
            assertThat("CacheSize must be 'Large', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.large);
        }


        // read Caches by Type
        String statement = "SELECT * FROM CacheCoreInfo core WHERE Type=" + CacheTypes.Multi.ordinal();
        DAO.readCacheList(cb3Database, caches, statement, true, true);

        // Cachelist is Async loading, so wait a moment
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("CacheList must have 117 Caches but has:" + caches.size, caches.size == 117);


    }

}