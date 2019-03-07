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
import static org.junit.jupiter.api.Assertions.*;

class ImmutableCacheTest {


    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3",true);
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
        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }


    @Test
    void getUrl() {

        ImmutableCache cache = new ImmutableCache(0, 0);
        String url = "HTTP://team-cachebox.de";

        MutableCache mutableCache = cache.getMutable(cb3Database);
        mutableCache.setId(123456789l);

        mutableCache.setUrl(url);
        DaoFactory.CACHE_DAO.writeToDatabase(cb3Database, mutableCache,false);


        cache = (ImmutableCache) DaoFactory.CACHE_DAO.getFromDbByCacheId(cb3Database, 123456789l, false);

        String testUrl = cache.getUrl(cb3Database);
        assertThat("Url must equals", url.equals(testUrl));


    }

}