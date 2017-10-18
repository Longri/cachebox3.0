/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.sqlite;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.dao.CacheListDAO;
import de.longri.cachebox3.types.CacheList;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for convert ACB V2.x DB to CB3 DB
 * Created by Longri on 18.10.2017.
 */
@RunWith(JUnitPlatform.class)
class DatabaseConvert {

    static {
        TestUtils.initialGdx();
    }

    @Test
    void convert() throws SQLiteGdxException {

        // copy testDb
        FileHandle testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3");
        FileHandle copyDbFileHandle = testDbFileHandle.parent().child("testDb.db3");
        if(copyDbFileHandle.exists()){
            // delete first
            assertThat("TestDB must be deleted for cleanup", copyDbFileHandle.delete());
        }
        testDbFileHandle.copyTo(copyDbFileHandle);
        assertThat("TestDB must exist", copyDbFileHandle.exists());

        // open DataBase and read CacheList
        Database.Data = new Database(Database.DatabaseType.CacheBox);
        Database.Data.startUp(testDbFileHandle);
        CacheList tmpCacheList = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(tmpCacheList, "", true, Config.ShowAllWaypoints.getValue());
        Database.Data.Query = tmpCacheList;
        assertThat("TestDB must have 33 Caches but has:" + Database.Data.Query.size, Database.Data.Query.size == 33);


        Database cb3Database = new Database(Database.DatabaseType.CacheBox3);
        cb3Database.startUp(copyDbFileHandle);

        // cleanup
//        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }

}