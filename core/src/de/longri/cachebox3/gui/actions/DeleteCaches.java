package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.activities.BlockUiProgress_Activity;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeleteCaches {

    private final Logger log = LoggerFactory.getLogger(DeleteCaches.class);
    private BlockUiProgress_Activity blockUi;

    public void deleteCaches(String selectStatement) {
        AtomicBoolean waitForBlockUIStarted = new AtomicBoolean(true);
        blockUi = null;
        CB.postOnGlThread(new NamedRunnable("Show BlockUi for Delete Caches") {
            @Override
            public void run() {
                blockUi = new BlockUiProgress_Activity(Translation.get("DeleteCaches"));
                waitForBlockUIStarted.set(false);
                blockUi.show();
            }
        });
        CB.wait(waitForBlockUIStarted);

        //check if Filter set to delete whole Database
        int wholeCount = Database.Data.getCacheCountOnThisDB();

        LongArray deleteCacheIdList = new LongArray();
        Array<CharSequence> deleteCacheGcCodeList = new Array<>();
        CacheList3DAO dao = new CacheList3DAO();
        dao.readCacheListIDs(Database.Data, deleteCacheIdList, deleteCacheGcCodeList, selectStatement);
        int filteredCacheCount = deleteCacheIdList.size;
        if (wholeCount == filteredCacheCount) {
            log.debug("Filter is set to delete whole Database");
            Database.Data.beginTransaction();
            Database.Data.execSQL("DELETE FROM Attributes;");
            Database.Data.execSQL("DELETE FROM CacheCoreInfo;");
            Database.Data.execSQL("DELETE FROM CacheInfo;");
            Database.Data.execSQL("DELETE FROM CacheText;");
            Database.Data.execSQL("DELETE FROM Category;");
            Database.Data.execSQL("DELETE FROM GPXFilenames;");
            Database.Data.execSQL("DELETE FROM Images;");
            Database.Data.execSQL("DELETE FROM Logs;");
            Database.Data.execSQL("DELETE FROM PocketQueries;");
            Database.Data.execSQL("DELETE FROM Replication;");
            Database.Data.execSQL("DELETE FROM Waypoints;");
            Database.Data.execSQL("DELETE FROM WaypointsText;");
            Database.Data.endTransaction();

            if (Database.Data.cacheList != null) {
                Database.Data.cacheList.clear();
            }
            EventHandler.fire(new CacheListChangedEvent());
        } else {
            log.debug("delete {} Caches", filteredCacheCount);

            Array<AbstractCache> queryDeleteList = new Array<>();

            GdxSqlitePreparedStatement attributesStatement = Database.Data.myDB.prepare("DELETE FROM Attributes WHERE id = ?");
            GdxSqlitePreparedStatement cacheCoreInfoStatement = Database.Data.myDB.prepare("DELETE FROM CacheCoreInfo WHERE id = ?");
            GdxSqlitePreparedStatement cacheInfoStatement = Database.Data.myDB.prepare("DELETE FROM CacheInfo WHERE id = ?");
            GdxSqlitePreparedStatement cacheTextStatement = Database.Data.myDB.prepare("DELETE FROM CacheText WHERE id = ?");
            GdxSqlitePreparedStatement imagesStatement = Database.Data.myDB.prepare("DELETE FROM Images WHERE CacheId = ?");
            GdxSqlitePreparedStatement logsStatement = Database.Data.myDB.prepare("DELETE FROM Logs WHERE CacheId = ?");
            GdxSqlitePreparedStatement waypointsStatement = Database.Data.myDB.prepare("DELETE FROM Waypoints WHERE CacheId = ?");
            GdxSqlitePreparedStatement waypointsTextStatement = Database.Data.myDB.prepare("DELETE FROM WaypointsText WHERE GcCode = ?");

            Database.Data.beginTransaction();
            for (int i = deleteCacheIdList.size - 1; i >= 0; i--) {
                long cacheId = deleteCacheIdList.get(i);
                if (Database.Data.cacheList != null) {
                    queryDeleteList.add(Database.Data.cacheList.getCacheById(cacheId));
                }
                attributesStatement.bind(cacheId).commit().reset();
                cacheCoreInfoStatement.bind(cacheId).commit().reset();
                cacheInfoStatement.bind(cacheId).commit().reset();
                cacheTextStatement.bind(cacheId).commit().reset();
                imagesStatement.bind(cacheId).commit().reset();
                logsStatement.bind(cacheId).commit().reset();
                waypointsStatement.bind(cacheId).commit().reset();
                waypointsTextStatement.bind(deleteCacheGcCodeList.get(i)).commit().reset();
            }
            Database.Data.endTransaction();
            attributesStatement.close();
            cacheCoreInfoStatement.close();
            cacheInfoStatement.close();
            cacheTextStatement.close();
            imagesStatement.close();
            logsStatement.close();
            waypointsStatement.close();
            waypointsTextStatement.close();

            while (queryDeleteList.size > 0) {
                AbstractCache delCache = queryDeleteList.pop();
                if (Database.Data.cacheList != null) {
                    Database.Data.cacheList.removeValue(delCache, true);
                }
            }
            if (Database.Data.cacheList != null) {
                Database.Data.cacheList.setUnfilteredSize(Database.Data.getCacheCountOnThisDB());
            }
        }
        Database.Data.execSQL("VACUUM;");
        deleteImages(deleteCacheGcCodeList);
        if (blockUi != null) blockUi.finish();

        final int deletedCacheCount = filteredCacheCount;
        CB.postOnNextGlThread(() -> CB.viewmanager.toast(Translation.get("DeletedCaches", Integer.toString(deletedCacheCount))));

        deleteCacheIdList.clear();
        deleteCacheGcCodeList.clear();
    }

    private void deleteImages(Array<CharSequence> deleteCacheGcCodeList) {
        while (deleteCacheGcCodeList.size > 0) {
            CharSequence gcCode = deleteCacheGcCodeList.pop();
            FileHandle imageFolder = Gdx.files.absolute(DescriptionImageGrabber.getImageFolderPath(gcCode));
            if (imageFolder.exists() && imageFolder.isDirectory()) {
                imageFolder.deleteDirectory();

                //delete parent, if empty
                FileHandle parent = imageFolder.parent();
                if (parent.exists() && parent.isDirectory() && parent.list().length == 0) {
                    parent.delete();
                }
            }
        }
    }

}
