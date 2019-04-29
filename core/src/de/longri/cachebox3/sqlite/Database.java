/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqlite;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Database {


    public final static DateFormat cbDbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final AtomicInteger EXCLUSIVE_ID = new AtomicInteger(-1);
    private Logger log;
    public static Database Data;
    public static Database Drafts;
    public static Database Settings;
    public GdxSqlite myDB;
    public final CacheList cacheList;
    private String inMemoryName;

    public Database(GdxSqlite db) {
        cacheList = new CacheList();
        myDB = db;
    }

    /**
     * @return Set To CB.Categories
     */
    public Categories gpxFilenameUpdateCacheCount() {
        GdxSqliteCursor cursor = null;
        try {
            cursor = rawQuery("select GPXFilename_ID, Count(*) as CacheCount from CacheInfo where GPXFilename_ID is not null Group by GPXFilename_ID", (String[]) null);

            if (cursor != null) {
                cursor.moveToFirst();

                ObjectMap<Long, Parameters> set = new ObjectMap<>();
                while (cursor.isAfterLast() == false) {
                    long gpxFilename_ID = cursor.getLong(0);
                    long cacheCount = cursor.getLong(1);

                    Parameters val = new Parameters();
                    val.put("CacheCount", cacheCount);
                    set.put(gpxFilename_ID, val);
                    cursor.moveToNext();
                }
                cursor.close();

                for (ObjectMap.Entry entry : set) {
                    update("GPXFilenames", (Parameters) entry.value, "ID = " + entry.key, null);
                }
            }

            delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0");
            delete("GPXFilenames", "ID not in (Select GPXFilename_ID From CacheInfo)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Categories categories = new Categories();
        return categories;
    }

    public boolean isStarted() {
        if (myDB == null) return false;
        if (myDB.isOpen()) return true;
        return false;
    }

    public Array<LogEntry> getLogs(AbstractCache abstractCache) {
        if (abstractCache == null) // if no cache is selected!
            return new Array<>();
        return getLogs(abstractCache.getId());
    }

    public Array<LogEntry> getLogs(long id) {
        Array<LogEntry> result = new Array<LogEntry>();

        GdxSqliteCursor reader = this.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId = \"" + Long.toString(id) + "\"", (String[]) null);
        if (reader != null) {
            try {
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    LogEntry logent = getLogEntry(reader, true);
                    if (logent != null)
                        result.add(logent);
                    reader.moveToNext();
                }
            } finally {
                reader.close();
            }
        }
        return result;
    }

    private static LogEntry getLogEntry(GdxSqliteCursor reader, boolean filterBbCode) {
        LogEntry retLogEntry = new LogEntry();
        retLogEntry.CacheId = reader.getLong(0);
        String sDate = reader.getString(1);
        try {
            retLogEntry.Timestamp = Database.cbDbFormat.parse(sDate);
        } catch (ParseException e) {
        }
        retLogEntry.Finder = reader.getString(2);
        retLogEntry.Type = LogTypes.values()[reader.getInt(3)];
        // retLogEntry.TypeIcon = reader.getInt(3);
        retLogEntry.Comment = reader.getString(4);
        retLogEntry.Id = reader.getLong(5);

        if (filterBbCode) {
            retLogEntry.Comment = LogEntry.filterBBCode(retLogEntry.Comment);
        }
        return retLogEntry;
    }

    public CharSequence getCharSequence(String sql, String[] args) {
        return new CharSequenceArray(getString(sql, args));
    }

    public String getString(String sql, String[] args) {
        GdxSqliteCursor cursor = null;
        try {
            cursor = this.rawQuery(sql, args);
            if (cursor != null) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    String result = cursor.getString(0);
                    return result != null ? result : "";
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return "";
    }

    public FileHandle getFileHandle() {
        return databasePath;
    }

    public AbstractCache getFromDbByGcCode(String gcCode, boolean withWaypoints, boolean fullData) {
        if (this.databaseType != DatabaseType.CacheBox3) throw new RuntimeException("Is no Cachebox Data DB");
        return DaoFactory.CACHE_DAO.getFromDbByGcCode(this, gcCode, withWaypoints, fullData);
    }

    public enum DatabaseType {
        CacheBox3, Drafts, Settings
    }

    protected DatabaseType databaseType;

    public Database(DatabaseType databaseType) {
        super();
        this.databaseType = databaseType;

        switch (databaseType) {
            case CacheBox3:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseChange;
                cacheList = new CacheList();
                break;
            case Drafts:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseDraftChange;
                cacheList = null;
                break;
            case Settings:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseSettingsChange;
                cacheList = null;
                break;
            default:
                cacheList = null;
        }
    }


    protected FileHandle databasePath;

    protected boolean newDB = false;

    /***
     * Wenn die DB neu erstellt wurde ist der Return Wert bei der ersten Abfrage True
     *
     * @return
     */
    public boolean isDbNew() {
        return newDB;
    }


    public long DatabaseId = 0; // for Database replication with WinCachebox
    public long MasterDatabaseId = 0;
    protected int latestDatabaseChange = 0;

    public void startUp() {
        //open in memory DB

//        log = LoggerFactory.getLogger("DB:" + inMemoryName);
        log = LoggerFactory.getLogger("EMPTY");

        //reset version
        shemaVersion.set(-1);

        log.debug("startUp Database: " + inMemoryName);
        if (myDB != null) {
            log.debug("Database is open ");

            log.debug("Database is the same");
            if (!myDB.isOpen()) {
                log.debug("Database was close so open now");
                myDB.openOrCreateDatabase();
            }

            // is open
            return;
        }

        log.debug("Initial database: " + inMemoryName);
        initialize();

//        endTransaction();
        int databaseSchemeVersion = getDatabaseSchemeVersion();
        log.debug("DatabaseSchemeVersion: " + databaseSchemeVersion);
        if (databaseSchemeVersion < latestDatabaseChange) {
            log.debug("Alter Database to SchemeVersion: " + latestDatabaseChange);
            alterDatabase(databaseSchemeVersion);
            SetDatabaseSchemeVersion();
        }


        if (databaseType == DatabaseType.CacheBox3) { // create or load DatabaseId for each
            DatabaseId = readConfigLong("DatabaseId");
            if (DatabaseId <= 0) {
                DatabaseId = new Date().getTime();
                writeConfigLong("DatabaseId", DatabaseId);
            }
            // Read MasterDatabaseId. If MasterDatabaseId > 0 -> This database
            // is connected to the Replications Master of WinCB
            // In this case changes of Waypoints, Solvertext, Notes must be
            // noted in the Table Replication...
            MasterDatabaseId = readConfigLong("MasterDatabaseId");
        }
        return;
    }


    public boolean startUp(final FileHandle path) throws SQLiteGdxException {

        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final AtomicBoolean io = new AtomicBoolean(false);

        CB.postOnGlThread(new NamedRunnable("StartUp Database on MainThread") {
            @Override
            public void run() {
                log = LoggerFactory.getLogger("DB:" + path.nameWithoutExtension());

                FileHandle parentDirectory = path.parent();

                if (!parentDirectory.exists()) {
                    WAIT.set(false);
                    throw new SQLiteGdxException("Directory for DB doesn't exist: " + parentDirectory.file().getAbsolutePath());
                }

                //reset version
                shemaVersion.set(-1);

                log.debug("startUp Database: " + Utils.getFileName(path));
                if (myDB != null) {
                    log.debug("Database is open ");
                    if (databasePath.file().getAbsolutePath().equals(path.file().getAbsolutePath())) {
                        log.debug("Database is the same");
                        if (!myDB.isOpen()) {
                            log.debug("Database was close so open now");
                            myDB.openOrCreateDatabase();
                        }

                        // is open
                        io.set(true);
                    }
                    log.debug("Database is changed! close " + Utils.getFileName(databasePath));
                    if (myDB != null && myDB.isOpen()) myDB.closeDatabase();
                    myDB = null;
                }


                databasePath = path;
                log.debug("Initial database: " + Utils.getFileName(databasePath));
                initialize();

//
                int databaseSchemeVersion = getDatabaseSchemeVersion();
                log.debug("DatabaseSchemeVersion: " + databaseSchemeVersion);
                if (databaseSchemeVersion < latestDatabaseChange) {
                    log.debug("Alter Database to SchemeVersion: " + latestDatabaseChange);
                    alterDatabase(databaseSchemeVersion);
                    SetDatabaseSchemeVersion();
                }


                if (databaseType == DatabaseType.CacheBox3) { // create or load DatabaseId for each
                    DatabaseId = readConfigLong("DatabaseId");
                    if (DatabaseId <= 0) {
                        DatabaseId = new Date().getTime();
                        writeConfigLong("DatabaseId", DatabaseId);
                    }
                    // Read MasterDatabaseId. If MasterDatabaseId > 0 -> This database
                    // is connected to the Replications Master of WinCB
                    // In this case changes of Waypoints, Solvertext, Notes must be
                    // noted in the Table Replication...
                    MasterDatabaseId = readConfigLong("MasterDatabaseId");
                }
                WAIT.set(false);
            }
        });

        while (WAIT.get()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return true;
    }


    public String getPath() {
        return this.databasePath.file().getAbsolutePath();
    }


    private AtomicInteger shemaVersion = new AtomicInteger(-1);

    public int getDatabaseSchemeVersion() {

        if (shemaVersion.get() >= 0) return shemaVersion.get();

        if (!isTableExists("Config")) {
            return -1;
        }

        int result = -1;
        GdxSqliteCursor cursor = null;
        try {
            cursor = rawQuery("select Value from Config where [Key] like 'DatabaseSchemeVersionWin'");
        } catch (Exception exc) {
            return -1;
        }
        if (cursor == null) return -1;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String databaseSchemeVersion = cursor.getString(0);
                result = Integer.parseInt(databaseSchemeVersion);
                cursor.moveToNext();
            }
        } catch (Exception exc) {
            result = -1;
        }
        if (cursor != null) {
            cursor.close();
        }
        shemaVersion.set(result);
        return result;
    }

    private void SetDatabaseSchemeVersion() {
        Parameters val = new Parameters();
        val.put("Value", latestDatabaseChange);
        long anz = update("Config", val, "[Key] like 'DatabaseSchemeVersionWin'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", "DatabaseSchemeVersionWin");
            insert("Config", val);
        }
        // for Compatibility with WinCB
        val.put("Value", latestDatabaseChange);
        anz = update("Config", val, "[Key] like 'DatabaseSchemeVersion'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", "DatabaseSchemeVersion");
            insert("Config", val);
        }
        shemaVersion.set(latestDatabaseChange);
    }

    public boolean isTableExists(String tableName) {
        GdxSqliteCursor cursor = null;
        try {
            cursor = rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName
                    + "' COLLATE NOCASE", (String[]) null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                if (tableName.equals(name)) {
                    return true;
                }
                cursor.moveToNext();
            }
        } catch (Exception exc) {
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    public void writeConfigString(String key, Object value) {
        Parameters val = new Parameters();
        val.put("Value", value);
        long anz = update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            insert("Config", val);
        }
    }

    public void WriteConfigLongString(String key, Object value) {
        Parameters val = new Parameters();
        val.put("LongString", value);
        long anz = update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            insert("Config", val);
        }
    }

    public String readConfigString(String key) throws Exception {
        String result = "";
        GdxSqliteCursor cursor = null;
        boolean found = false;
        try {
            cursor = rawQuery("select Value from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result = cursor.getString(0);
                found = true;
                cursor.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        } finally {
            cursor.close();
        }

        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public String readConfigLongString(String key) throws Exception {
        String result = "";
        GdxSqliteCursor cursor = null;
        boolean found = false;
        try {
            cursor = rawQuery("select LongString from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result = cursor.getString(0);
                found = true;
                cursor.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        } finally {
            cursor.close();
        }


        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public void writeConfigDesiredString(String key, String value) {
        Parameters val = new Parameters();
        val.put("desired", value);
        long anz = update("Config", val, "[Key] like '" + key + "'", null);
    }

    public String readConfigDesiredString(String key) throws Exception {

        if (shemaVersion.get() < 1028) return null;

        String result = "";
        GdxSqliteCursor cursor = null;
        boolean found = false;
        try {
            cursor = rawQuery("select desired from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result = cursor.getString(0);
                found = true;
                cursor.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        } finally {
            cursor.close();
        }


        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public void writeConfigLong(String key, long value) {
        writeConfigString(key, String.valueOf(value));
    }

    public long readConfigLong(String key) {
        try {
            String value = readConfigString(key);
            return Long.valueOf(value);
        } catch (Exception ex) {
            return 0;
        }
    }

    // Zur Parameter �bergabe and die DB
    public static class Parameters extends HashMap<String, Object> {

        /**
         *
         */
        private static final long serialVersionUID = 6506158947781669528L;
    }


    protected void alterDatabase(int lastDatabaseSchemeVersion) {


        switch (databaseType) {
            case CacheBox3:
                new AlterCachebox3DB().alterCachebox3DB(this, lastDatabaseSchemeVersion);
                break;
            case Drafts:
                beginTransaction();
                try {

                    if (lastDatabaseSchemeVersion <= 0) {
                        // First Initialization of the Database
                        // FieldNotes Table
                        execSQL("CREATE TABLE [FieldNotes] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [GcId] nvarchar (255) NULL, [name] nchar (255) NULL, [CacheType] smallint NULL, [Url] nchar (255) NULL, [Timestamp] datetime NULL, [Type] smallint NULL, [FoundNumber] int NULL, [Comment] ntext NULL);");

                        // Config Table
                        execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                        execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1002) {
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [Uploaded] BOOLEAN DEFAULT 'false' NULL");
                    }
                    if (lastDatabaseSchemeVersion < 1003) {
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [GC_Vote] integer default 0");
                    }
                    if (lastDatabaseSchemeVersion < 1004) {
                        execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (15) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [path] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                        execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                        execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (15) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                        execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                        execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1005) {
                        execSQL("ALTER TABLE [Trackable] ADD COLUMN [TypeName] ntext NULL");
                        execSQL("ALTER TABLE [Trackable] ADD COLUMN [LastVisit] datetime NULL");
                        execSQL("ALTER TABLE [Trackable] ADD COLUMN [Home] ntext NULL");
                        execSQL("ALTER TABLE [Trackable] ADD COLUMN [TravelDistance] integer default 0");
                    }
                    if (lastDatabaseSchemeVersion < 1006) {
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbFieldNote] BOOLEAN DEFAULT 'false' NULL");
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbName] nvarchar (255)  NULL");
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TbIconUrl] nvarchar (255)  NULL");
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TravelBugCode] nvarchar (15)  NULL");
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [TrackingNumber] nvarchar (15)  NULL");
                    }
                    if (lastDatabaseSchemeVersion < 1007) {
                        execSQL("ALTER TABLE [FieldNotes] ADD COLUMN [directLog] BOOLEAN DEFAULT 'false' NULL");
                    }
                } catch (Exception exc) {
                    log.error("alterDatabase", exc);
                } finally {
                    endTransaction();
                }
                break;
            case Settings:
                beginTransaction();
                try {
                    if (lastDatabaseSchemeVersion <= 0) {
                        // First Initialization of the Database
                        final DatabaseSchema schema = new DatabaseSchema();
                        execSQL(schema.CONFIG_TABLE);
                        execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
                        return;
                    }
                    if (lastDatabaseSchemeVersion < 1002) {
                        // Long Text Field for long Strings
                        execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
                    }
                    if (lastDatabaseSchemeVersion < 1003) {
                        // Long Text Field for long Strings
                        execSQL("ALTER TABLE [Config] ADD [desired] ntext NULL;");
                    }
                    if (lastDatabaseSchemeVersion < 1004) {
                        // add primary key on [Key]
                        String CREATE = "CREATE TABLE ConfigCopy (\n" +
                                "    [Key]      NVARCHAR (30)  NOT NULL\n" +
                                "                              PRIMARY KEY\n" +
                                "                              UNIQUE,\n" +
                                "    Value      NVARCHAR (255),\n" +
                                "    LongString NTEXT,\n" +
                                "    desired    NTEXT\n" +
                                ");";
                        String COPY = "INSERT INTO ConfigCopy SELECT * FROM Config;";
                        String DROP = "DROP TABLE Config;";
                        String RENAME = "ALTER TABLE ConfigCopy RENAME TO Config;";

                        String SQL = CREATE + COPY + DROP + RENAME;
                        execSQL(SQL);
                    }
                    if (lastDatabaseSchemeVersion < 1005) {
                        //Extend Config with Blob
                        execSQL("ALTER TABLE [Config] ADD [blob] BLOB;");
                    }
                } catch (Exception exc) {
                    log.error("alterDatabase", exc);
                } finally {
                    endTransaction();
                }
                break;
        }
    }


    public static boolean waypointExists(Database database, String gcCode) {
        GdxSqliteCursor cursor = database.rawQuery("select GcCode from Waypoints where GcCode=?", new String[]{gcCode});
        {
            if (cursor == null) return false;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                try {
                    cursor.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            cursor.close();

            return false;
        }
    }

    public static String createFreeGcCode(Database database, String cacheGcCode) {
        String suffix = cacheGcCode.substring(2);
        String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
        String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < firstCharCandidates.length(); i++)
            for (int j = 0; j < secondCharCandidates.length(); j++) {
                String gcCode = firstCharCandidates.substring(i, i + 1) + secondCharCandidates.substring(j, j + 1) + suffix;
                if (!waypointExists(database, gcCode))
                    return gcCode;
            }
        throw new RuntimeException("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
    }


    public static String getNote(long cacheId) {
        String resultString = "";
        GdxSqliteCursor cursor = Database.Data.rawQuery("select Notes from Caches where Id=?", new String[]{String.valueOf(cacheId)});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            resultString = cursor.getString(0);
            break;
        }
        cursor.close();
        return resultString;
    }

    /**
     * geänderte Note nur in die DB schreiben
     *
     * @param cacheId
     * @param value
     */
    public static void setNote(long cacheId, String value) {
        Parameters args = new Parameters();
        args.put("Notes", value);
        args.put("HasUserData", true);

        Database.Data.update("Caches", args, "id=" + cacheId, null);
    }

    public static void setFound(long cacheId, boolean value) {
        Parameters args = new Parameters();
        args.put("found", value);
        Database.Data.update("Caches", args, "id=" + cacheId, null);
    }

    public static String getSolver(long cacheId) {
        GdxSqliteCursor cursor = null;
        try {
            String resultString = "";
            cursor = Database.Data.rawQuery("select Solver from Caches where Id=?", new String[]{String.valueOf(cacheId)});
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                resultString = cursor.getString(0);
                break;
            }
            return resultString;
        } catch (Exception ex) {
            return "";
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * geänderten Solver nur in die DB schreiben
     *
     * @param cacheId
     * @param value
     */
    public static void setSolver(long cacheId, String value) {
        Parameters args = new Parameters();
        args.put("Solver", value);
        args.put("HasUserData", true);

        Database.Data.update("Caches", args, "id=" + cacheId, null);
    }

    /**
     * @param minToKeep      Config.settings.LogMinCount.getValue()
     * @param LogMaxMonthAge Config.settings.LogMaxMonthAge.getValue()
     */
    public void deleteOldLogs(int minToKeep, int LogMaxMonthAge) {

        log.debug("deleteOldLogs but keep " + minToKeep + " and not older than " + LogMaxMonthAge);
        if (LogMaxMonthAge == 0) {
            // Setting are 'immediately'
            // Delete all Logs and return
            // TODO implement this
        }

        ArrayList<Long> oldLogCaches = new ArrayList<Long>();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, -LogMaxMonthAge);
        // hint:
        // months are numbered from 0 onwards in Calendar
        // and month and day have leading zeroes in logs Timestamp
        String TimeStamp = (now.get(Calendar.YEAR)) + "-" + String.format("%02d", (now.get(Calendar.MONTH) + 1)) + "-" + String.format("%02d", now.get(Calendar.DATE));

        // #############################################################################
        // get CacheId's from Caches with older logs and having more logs than minToKeep
        // #############################################################################
        {
            GdxSqliteCursor cursor = null;
            try {
                String command = "SELECT cacheid FROM logs WHERE Timestamp < '" + TimeStamp + "' GROUP BY CacheId HAVING COUNT(Id) > " + String.valueOf(minToKeep);
                log.debug(command);
                cursor = Database.Data.rawQuery(command, (String[]) null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    long tmp = cursor.getLong(0);
                    if (!oldLogCaches.contains(tmp))
                        oldLogCaches.add(cursor.getLong(0));
                    cursor.moveToNext();
                }
                cursor.close();
            } catch (Exception ex) {
                log.error("deleteOldLogs", ex);
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        // ###################################################
        // get Logs
        // ###################################################
        {
            GdxSqliteCursor cursor = null;
            try {
                beginTransaction();
                for (long oldLogCache : oldLogCaches) {
                    ArrayList<Long> minLogIds = new ArrayList<Long>();
                    String command = "select id from logs where cacheid = " + String.valueOf(oldLogCache) + " order by Timestamp desc";
                    log.debug(command);
                    int count = 0;
                    cursor = Database.Data.rawQuery(command, (String[]) null);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        if (count == minToKeep)
                            break;
                        minLogIds.add(cursor.getLong(0));
                        cursor.moveToNext();
                        count++;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (long id : minLogIds)
                        sb.append(id).append(",");
                    // now delete all Logs out of Date but keep the ones in minLogIds
                    String delCommand;
                    if (sb.length() > 0)
                        delCommand = "deleteIcon FROM Logs WHERE Timestamp<'" + TimeStamp + "' AND cacheid = " + String.valueOf(oldLogCache) + " AND id NOT IN (" + sb.toString().substring(0, sb.length() - 1) + ")";
                    else
                        delCommand = "deleteIcon FROM Logs WHERE Timestamp<'" + TimeStamp + "' AND cacheid = " + String.valueOf(oldLogCache);
                    log.debug(delCommand);
                    Database.Data.execSQL(delCommand);
                }
                endTransaction();
            } catch (Exception ex) {
                log.error("deleteOldLogs", ex);
            } finally {
                if (cursor != null) cursor.close();
                endTransaction();
            }
        }
    }


    // DB Funktionen

    public void initialize() {
        if (myDB == null) {

            if (inMemoryName != null) {
                try {
                    log.debug("open data base: " + inMemoryName);
                    myDB = new GdxSqlite();
                    myDB.openOrCreateDatabase();
                } catch (Exception exc) {
                    log.error("Can't open Database", exc);
                }
            } else {
                if (!databasePath.exists())
                    reset();

                try {
                    log.debug("open data base: " + databasePath);
                    myDB = new GdxSqlite(databasePath);
                    myDB.openOrCreateDatabase();

                    //set PRAGMAS
                    myDB.execSQL("PRAGMA synchronous = OFF");
                    myDB.execSQL("PRAGMA journal_mode = MEMORY");


                } catch (Exception exc) {
                    log.error("Can't open Database", exc);
                }
            }
        }
    }

    public void reset() {
        // if exists, delete old database file
        if (databasePath.exists()) {
            log.debug("RESET DB, delete file: " + databasePath);
            databasePath.delete();
        }

        try {
            log.debug("create data base: " + databasePath);
            myDB = new GdxSqlite(databasePath);
            myDB.openOrCreateDatabase();
            myDB.closeDatabase();

        } catch (Exception exc) {
            log.error("createDB", exc);
        }
    }

    public synchronized GdxSqliteCursor rawQuery(String sql) {
        if (myDB == null) return null;
        try {
            return myDB.rawQuery(sql);
        } catch (SQLiteGdxException e) {
            log.error("rawQuerry:" + sql, e);
        }
        return null;
    }

    public synchronized GdxSqliteCursor rawQuery(String sql, String[] args) {
        if (myDB == null) return null;
        try {

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    sql = sql.replaceFirst("\\?", "'" + args[i] + "'");
                }
            }
            return myDB.rawQuery(sql);
        } catch (SQLiteGdxException e) {
            log.error("rawQuerry:" + sql, e);
        }
        return null;
    }

    public synchronized void rawQuery(String sql, GdxSqlite.RowCallback callback) {
        if (myDB == null) return;
        myDB.rawQuery(sql, callback);
    }

    public void execSQL(String sql) {
        try {
            myDB.execSQL(sql);
        } catch (SQLiteGdxException e) {
            log.error("execSQL", e);
        }
    }


    public synchronized long update(String tablename, Parameters val, String whereClause, String[] whereArgs) {

        if (val == null || val.size() <= 0) {
            //nothing to update
            return 0;
        }

        if (CB.isLogLevel(CB.LOG_LEVEL_DEBUG)) {
            StringBuilder sb = new StringBuilder("Update @ Table:" + tablename);
            sb.append("Parameters:" + val.toString());
            sb.append("WHERECLAUSE:" + whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            log.debug(sb.toString());
        }

        if (myDB == null)
            return 0;

        StringBuilder sql = new StringBuilder();

        sql.append("update ");
        sql.append(tablename);
        sql.append(" set");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            sql.append("=?");
            if (i != val.size()) {
                sql.append(",");
            }
        }

        if (!whereClause.isEmpty()) {
            if (!whereClause.toLowerCase().contains("where")) {
                sql.append(" WHERE");
            }
            sql.append(" ");
            sql.append(whereClause);
        }
        GdxSqlitePreparedStatement st = null;
        try {
            st = myDB.prepare(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.bind(j, entry.getValue());
            }

            if (whereArgs != null) {
                for (int k = 0; k < whereArgs.length; k++) {
                    st.bind(j + k + 1, whereArgs[k]);
                }
            }
            st.commit();
            return myDB.changes();

        } catch (SQLiteGdxException e) {
            if (e.getMessage().contains("near")) {
                log.error("UPDATE:", sql.toString());
            } else {
                log.error("UPDATE:" + sql.toString(), e);
            }
            return 0;
        } finally {
            try {
                if (st != null) st.close();
            } catch (SQLiteGdxException e) {
                e.printStackTrace();
            }
        }
    }

    public long insert(String tablename, Parameters val) {
        StringBuilder sql = new StringBuilder();

        sql.append("insert into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        GdxSqlitePreparedStatement st = null;
        try {
            st = myDB.prepare(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.bind(j, entry.getValue());
            }

            log.debug("INSERT: " + sql);
            st.commit();
            return myDB.changes();
        } catch (SQLiteGdxException e) {
            log.error("INSERT", e);
            return 0;
        } finally {
            try {
                if (st != null) st.close();
            } catch (SQLiteGdxException e) {
                e.printStackTrace();
            }
        }
    }

    public int delete(String tablename, String whereClause) {
        StringBuilder sql = new StringBuilder();

        sql.append("delete from ");
        sql.append(tablename);

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }

        myDB.execSQL(sql.toString());
        return myDB.changes();
    }

    public void beginTransaction() {
        log.debug("begin transaction");
        if (EXCLUSIVE_ID.get() != -1) {
            log.warn("Can't start Transaction is Exclusive for ID: " + EXCLUSIVE_ID.get());
            return;
        }

        if (myDB != null)
            myDB.beginTransaction();
    }

    public void endTransaction() {
        log.debug("end transaction");
        if (EXCLUSIVE_ID.get() != -1) {
            log.warn("Can't end Transaction is Exclusive for ID: " + EXCLUSIVE_ID.get());
            return;
        }
        if (myDB != null)
            myDB.endTransaction();
    }

    public void endTransactionExclusive(int id) {
        if (EXCLUSIVE_ID.get() != id)
            throw new RuntimeException("Wrong Exclusive ID: " + id);
        EXCLUSIVE_ID.set(-1);
        if (myDB != null)
            myDB.endTransaction();
    }

    public void beginTransactionExclusive(int id) {
        if (EXCLUSIVE_ID.get() != -1)
            throw new RuntimeException("Can't begin Transaction Exclusive! ID is Exclusive: " + EXCLUSIVE_ID.get());
        EXCLUSIVE_ID.set(id);
        if (myDB != null)
            myDB.beginTransaction();
    }

    public void insertWithConflictReplace(String tablename, Parameters val) {
        insert(tablename, val, "INSERT OR REPLACE into ");
    }

    public void insertWithConflictIgnore(String tablename, Parameters val) {
        insert(tablename, val, "INSERT OR IGNORE into ");
    }

    private void insert(String tablename, Parameters val, String sqlInsert) {
        StringBuilder sql = new StringBuilder();

        sql.append(sqlInsert);
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        GdxSqlitePreparedStatement st = null;
        try {
            st = myDB.prepare(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.bind(j, entry.getValue());
            }

            st.commit();


        } catch (SQLiteGdxException e) {
            e.printStackTrace();
        } finally {
            try {
                st.close();
            } catch (SQLiteGdxException e) {
                e.printStackTrace();
            }
        }
    }


    public void close() {
        if (myDB == null) return;
        try {
            myDB.closeDatabase();
        } catch (SQLiteGdxException e) {
            log.error("Close Database {}", databaseType, e);
        }
    }

    public void open() {
        if (myDB == null) return;
        try {
            myDB.openOrCreateDatabase();
        } catch (SQLiteGdxException e) {
            log.error("Open Database {}", databaseType, e);
        }
    }

    public int getCacheCountOnThisDB() {
        try {
            return getCacheCount(this.myDB);
        } catch (SQLiteGdxException e) {
            return -1;
        }
    }

    public boolean isInMemory() {
        return inMemoryName != null;
    }

    // Static methods ##############################################################################################


    public static int getCacheCountInDB(String absolutePath) {
        try {
            GdxSqlite tempDB = new GdxSqlite(Gdx.files.absolute(absolutePath));
            tempDB.openOrCreateDatabase();
            int count = getCacheCount(tempDB);
            tempDB.closeDatabase();
            return count;
        } catch (Exception exc) {
            return -1;
        }
    }

    private static int getCacheCount(GdxSqlite tempDB) throws SQLiteGdxException {
        //get schema version
        GdxSqliteCursor cursor = tempDB.rawQuery("SELECT Value FROM Config WHERE [Key] like 'DatabaseSchemeVersionWin'");
        cursor.moveToFirst();
        int version = Integer.parseInt(cursor.getString(0));
        cursor.close();
        if (version < 1028) {
            cursor = tempDB.rawQuery("SELECT COUNT(*) FROM caches");
        } else {
            cursor = tempDB.rawQuery("SELECT COUNT(*) FROM CacheCoreInfo");
        }


        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public static boolean createNewDB(Database database, FileHandle rootFolder, String newDB_Name, boolean ownRepository, boolean... dontStoreConfig) {

        final Logger logger = LoggerFactory.getLogger("CREATE NEW DB");

        if (CB.viewmanager != null) CB.viewmanager.setNewFilter(FilterInstances.ALL); // in case of JUnit
        FileHandle dbFile = rootFolder.child(newDB_Name + ".db3");
        try {
            database.close();
            database.startUp(dbFile);
        } catch (SQLiteGdxException e) {
            logger.error("Create new DB", e);
            return true;
        }
        // OwnRepository?
        if (ownRepository) {
            String folder = "?/" + newDB_Name + "/";

            Config.DescriptionImageFolderLocal.setValue(folder + "Images");
            Config.MapPackFolderLocal.setValue(folder + "Maps");
            Config.SpoilerFolderLocal.setValue(folder + "Spoilers");
            Config.TileCacheFolderLocal.setValue(folder + "Cache");
            if (dontStoreConfig == null) {
                Config.AcceptChanges();
            }
            logger.debug(
                    newDB_Name + " has own Repository:\n" + //
                            Config.DescriptionImageFolderLocal.getValue() + ", \n" + //
                            Config.MapPackFolderLocal.getValue() + ", \n" + //
                            Config.SpoilerFolderLocal.getValue() + ", \n" + //
                            Config.TileCacheFolderLocal.getValue()//
            );

            // Create Folder?
            boolean creationOK = Utils.createDirectory(Config.DescriptionImageFolderLocal.getValue());
            creationOK = creationOK && Utils.createDirectory(Config.MapPackFolderLocal.getValue());
            creationOK = creationOK && Utils.createDirectory(Config.SpoilerFolderLocal.getValue());
            creationOK = creationOK && Utils.createDirectory(Config.TileCacheFolderLocal.getValue());
            if (!creationOK)
                logger.debug(
                        "Problem with creation of one of the Directories:" + //
                                Config.DescriptionImageFolderLocal.getValue() + ", " + //
                                Config.MapPackFolderLocal.getValue() + ", " + //
                                Config.SpoilerFolderLocal.getValue() + ", " + //
                                Config.TileCacheFolderLocal.getValue()//
                );
        }
        return false;
    }

    public static void createNewInMemoryDB(Database database, String dbName) {
        final Logger logger = LoggerFactory.getLogger("CREATE NEW DB");

        if (CB.viewmanager != null) CB.viewmanager.setNewFilter(FilterInstances.ALL); // in case of JUnit

        database.inMemoryName = dbName;

        try {
            database.close();
            database.startUp();
        } catch (SQLiteGdxException e) {
            logger.error("Create new DB", e);
        }
    }


    public static Date getDateFromDataBaseString(String dateString) {
        if (dateString == null || dateString.isEmpty()) return new Date();

        try {
            return Database.cbDbFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

}
