package de.longri.cachebox3.types;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class ImmutableCacheTest {


    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3", true);
        copyDbFileHandle = testDbFileHandle.parent().child("testImutableCache.db3");
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
    static void tearDown() {
        cb3Database.close();
        System.gc();
//        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }


    @Test
    void getUrl() {

        MutableCache mutableCache = new MutableCache(0, 0);
        mutableCache.setId(123456789l);
        String url = "HTTP://team-cachebox.de";

        mutableCache.setUrl(url);
        DaoFactory.CACHE_DAO.writeToDatabase(cb3Database, mutableCache, false);


        AbstractCache cache = DaoFactory.CACHE_DAO.getFromDbByCacheId(cb3Database, 123456789l, false, true);

        String testUrl = cache.getUrl().toString();
        assertThat("Url must charSequenceEquals", url.equals(testUrl));


    }

}