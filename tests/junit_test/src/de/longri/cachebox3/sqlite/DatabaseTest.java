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
package de.longri.cachebox3.sqlite;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 08.11.2017.
 */
class DatabaseTest {

    private static FileHandle workpath;

    @BeforeAll
    public static void setUp() {
        TestUtils.initialGdx();
        workpath = TestUtils.getResourceFileHandle("testsResources", true).child("TestNewDB");
        workpath.mkdirs();
    }

    @AfterAll
    public static void tearDown() {
        workpath.deleteDirectory();
    }


    @Test
    void createNewDB() {
        // test for issue #165

        Database testDb = new Database(Database.DatabaseType.CacheBox3);
        Database.createNewDB(testDb, workpath, "createNewDB", true, true);

        assertThat("Database schema version must be last version", testDb.getDatabaseSchemeVersion() == DatabaseVersions.LatestDatabaseChange);


        //check all tables
        String[] tableNames = new String[]{"Config", "Category", "GPXFilenames", "Images",
                "Logs", "PocketQueries", "Replication", "TbLogs", "Trackable", "CacheCoreInfo",
                "Attributes", "CacheText", "CacheInfo", "Waypoints", "WaypointsText"};
        for (String tableName : tableNames) {

            boolean exist = false;
            try {
                exist = testDb.isTableExists(tableName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            assertThat("Table '" + tableName + "' must exist", exist);

        }

    }

    @Test
    void createNewInMemoryDB() {
        // test for issue #165

        Database testDb = new Database(Database.DatabaseType.CacheBox3);
        Database.createNewInMemoryDB(testDb, "createNewDB");

        assertThat("Database schema version must be last version", testDb.getDatabaseSchemeVersion() == DatabaseVersions.LatestDatabaseChange);


        //check all tables
        String[] tableNames = new String[]{"Config", "Category", "GPXFilenames", "Images",
                "Logs", "PocketQueries", "Replication", "TbLogs", "Trackable", "CacheCoreInfo",
                "Attributes", "CacheText", "CacheInfo", "Waypoints", "WaypointsText"};
        for (String tableName : tableNames) {

            boolean exist = false;
            try {
                exist = testDb.isTableExists(tableName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            assertThat("Table '" + tableName + "' must exist", exist);

        }

    }

}