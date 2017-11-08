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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 08.11.2017.
 */
class DatabaseTest {

    private static FileHandle workpath;

    @BeforeAll
    static void setUp() {
        TestUtils.initialGdx();
        workpath = TestUtils.getResourceFileHandle("testsResources").child("TestNewDB");
    }


    @Test
    void createNewDB() {
        // test for issue #165

        Database testDb = new Database(Database.DatabaseType.CacheBox3);
        Database.createNewDB(testDb, workpath, "testDB", true);

        assertThat("Database schema version must be last version", testDb.getDatabaseSchemeVersion() == DatabaseVersions.LatestDatabaseChange);

    }

}