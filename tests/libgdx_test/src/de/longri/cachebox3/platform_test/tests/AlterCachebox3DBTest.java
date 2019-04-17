

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.sqlite.*;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.gdx.sqlite.GdxSqlite;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.SQLiteGdxException;
import de.longri.cachebox3.platform_test.AfterAll;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 17.01.2018.
 */
public class AlterCachebox3DBTest {


    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    public static void beforeAll() throws SQLiteGdxException, PlatformAssertionError {

        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/cacheboxV2.db3", true);
        copyDbFileHandle = testDbFileHandle.parent().child("alterDBtest.db3");
        if (copyDbFileHandle.exists()) {
            // delete first
            assertThat("TestDB must be deleted for cleanup", copyDbFileHandle.delete());
        }
        testDbFileHandle.copyTo(copyDbFileHandle);
        assertThat("TestDB must exist", copyDbFileHandle.exists());

    }

    @AfterAll
    public static void cleanUpRecources() {
        cb3Database.close();
    }

    @Test
    public void alterCachebox3DB() throws PlatformAssertionError {


        GdxSqlite tempDB = new GdxSqlite(testDbFileHandle);
        tempDB.openOrCreateDatabase();

        //get schema version
        GdxSqliteCursor cursor = tempDB.rawQuery("SELECT Value FROM Config WHERE [Key] like 'DatabaseSchemeVersionWin'");
        cursor.moveToFirst();
        int version = Integer.parseInt(cursor.getString(0));

        assertThat("Unconverted DB version must be 1027", version == 1027);

        // open DataBase and alter
        cb3Database = new Database(Database.DatabaseType.CacheBox3);

        long start = System.currentTimeMillis();
        cb3Database.startUp(copyDbFileHandle);
        assertThat("Converted DB version must be 1030", cb3Database.getDatabaseSchemeVersion() == 1030);

        System.out.println("DB converted in " + (System.currentTimeMillis() - start) + " ms");
    }
}
