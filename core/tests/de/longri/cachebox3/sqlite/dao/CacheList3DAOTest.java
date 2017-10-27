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
        CacheList caches = DAO.readCacheList(cb3Database, null, "", true, true);
        assertThat("CacheList must have 33 Caches but has:" + caches.size, caches.size == 33);

        AbstractCache cache = caches.getCacheById(24827321826689620L);
        assertThat("Cache must have one Waypoint but has:" + cache.getWaypoints().size, cache.getWaypoints().size == 1);


        // read Caches by Type
        String where = "Type=" + CacheTypes.Multi.ordinal();
        DAO.readCacheList(cb3Database, caches, where, true, true);
        assertThat("CacheList must have 7 Caches but has:" + caches.size, caches.size == 7);

        System.out.println(ClassLayout.parseInstance(cache).toPrintable());
    }

}