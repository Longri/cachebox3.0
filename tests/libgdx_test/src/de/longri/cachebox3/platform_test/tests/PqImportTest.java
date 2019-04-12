

//  Don't modify this file, it's created by tool 'extract_libgdx_test

package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.gpx.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import de.longri.cachebox3.gui.activities.PqListItem;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItemInterface;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.DatabaseVersions;
import de.longri.cachebox3.utils.ICancel;
import de.longri.gdx.sqlite.SQLiteGdxException;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

public class PqImportTest {

    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();
    }


    @Test
    public void importNow() throws PlatformAssertionError {

        //create test DB
        Database testDb = new Database(Database.DatabaseType.CacheBox3);
        Database.createNewInMemoryDB(testDb, "createNewDB");

        assertThat("Database schema version must be last version", testDb.getDatabaseSchemeVersion() == DatabaseVersions.LatestDatabaseChange);


        //create import list
        Array<ListViewItemInterface> selectedItems = new Array<>();
        final FileHandle[] testFile = new FileHandle[1];
        PocketQuery pq = new PocketQuery() {
            @Override
            public FileHandle download(FileHandle folder, ICancel iCancel, final IncrementProgressBytesListener listener) {
                FileHandle oriFile = TestUtils.getResourceFileHandle("testsResources/gpx/GS_PQ.zip", true);
                testFile[0] = oriFile.parent().child("TestPQ.zip");
                oriFile.copyTo(testFile[0]);
                if (testFile[0].exists()) return testFile[0];
                return null;
            }
        };

        PocketQuery pq2 = new PocketQuery() {
            @Override
            public FileHandle download(FileHandle folder, ICancel iCancel, final IncrementProgressBytesListener listener) {
                FileHandle oriFile = TestUtils.getResourceFileHandle("testsResources/gpx/GS_PQ.zip", true);
                testFile[0] = oriFile.parent().child("TestPQ2.zip");
                oriFile.copyTo(testFile[0]);
                if (testFile[0].exists()) return testFile[0];
                return null;
            }
        };


        PqListItem pqListItem = new PqListItem(0, pq, null);
        selectedItems.add(pqListItem);
        PqListItem pqListItem2 = new PqListItem(0, pq2, null);
        selectedItems.add(pqListItem2);

        final AtomicBoolean WAIT = new AtomicBoolean(true);

        PqImport importer = new PqImport(testDb);
        importer.importNow(selectedItems, new PqImport.IReadyHandler() {
            @Override
            public void ready(int importedCaches, int importedWaypoints, int importedLogs) {
                WAIT.set(false);
            }
        });


        while (WAIT.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        testFile[0].delete();

    }

}
