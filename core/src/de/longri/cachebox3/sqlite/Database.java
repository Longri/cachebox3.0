/* 
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import com.badlogic.gdx.sql.SQLiteGdxDatabase;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class Database {
    private final static Logger log = LoggerFactory.getLogger(Database.class);
    public static Database Data;
    public static Database Drafts;
    public static Database Settings;
    private SQLiteGdxDatabase myDB;
    public CacheList Query;

    /**
     * @return Set To CB.Categories
     */
    public Categories gpxFilenameUpdateCacheCount() {
        // welche GPXFilenamen sind in der DB erfasst
        beginTransaction();
        try {
            SQLiteGdxDatabaseCursor reader = rawQuery("select GPXFilename_ID, Count(*) as CacheCount from CacheInfo where GPXFilename_ID is not null Group by GPXFilename_ID", null);
            reader.moveToFirst();

            while (reader.isAfterLast() == false) {
                long GPXFilename_ID = reader.getLong(0);
                long CacheCount = reader.getLong(1);

                Parameters val = new Parameters();
                val.put("CacheCount", CacheCount);
                update("GPXFilenames", val, "ID = " + GPXFilename_ID, null);

                reader.moveToNext();
            }

            delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
            delete("GPXFilenames", "ID not in (Select GPXFilename_ID From CacheInfo)", null);
            reader.close();
            setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            endTransaction();
        }

        //TODO ???
        Categories categories = new Categories();
        return categories;
    }

    public boolean isStarted() {
        if (myDB == null) return false;
        if (myDB.isOpen()) return true;
        return false;
    }

    public static Array<LogEntry> getLogs(AbstractCache abstractCache) {
        Array<LogEntry> result = new Array<LogEntry>();
        if (abstractCache == null) // if no cache is selected!
            return result;


        //TODO Qerry with args not working on iOS
//      SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc", new String[]{Long.toString(cache.Id)});
        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId = \"" + Long.toString(abstractCache.getId()) + "\"", null);


        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            LogEntry logent = getLogEntry(abstractCache, reader, true);
            if (logent != null)
                result.add(logent);
            reader.moveToNext();
        }
        reader.close();
        return result;
    }

    private static LogEntry getLogEntry(AbstractCache abstractCache, SQLiteGdxDatabaseCursor reader, boolean filterBbCode) {
        int intLogType = reader.getInt(3);
        if (intLogType < 0 || intLogType > 13)
            return null;

        LogEntry retLogEntry = new LogEntry();
        retLogEntry.CacheId = reader.getLong(0);
        String sDate = reader.getString(1);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            retLogEntry.Timestamp = iso8601Format.parse(sDate);
        } catch (ParseException e) {
        }
        retLogEntry.Finder = reader.getString(2);
        retLogEntry.Type = LogTypes.values()[reader.getInt(3)];
        // retLogEntry.TypeIcon = reader.getInt(3);
        retLogEntry.Comment = reader.getString(4);
        retLogEntry.Id = reader.getLong(5);

        if (filterBbCode) {
            int lIndex;
            while ((lIndex = retLogEntry.Comment.indexOf('[')) >= 0) {
                int rIndex = retLogEntry.Comment.indexOf(']', lIndex);
                if (rIndex == -1)
                    break;
                retLogEntry.Comment = retLogEntry.Comment.substring(0, lIndex) + retLogEntry.Comment.substring(rIndex + 1);
            }
        }
        return retLogEntry;
    }

    public void disableAutoCommit() {
        myDB.setAutoCommit(false);
    }


    public CharSequence getCharSequence(String sql, String[] args) {
        return new CharSequenceArray(getString(sql, args));
    }

    public String getString(String sql, String[] args) {
        SQLiteGdxDatabaseCursor cursor = null;
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
                Query = new CacheList();
                break;
            case Drafts:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseDraftChange;
                break;
            case Settings:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseSettingsChange;
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


    public boolean startUp(FileHandle databasePath) throws SQLiteGdxException {

        FileHandle parentDirectory = databasePath.parent();

        if (!parentDirectory.exists()) {
            throw new SQLiteGdxException("Directory for DB doesn't exist: " + parentDirectory.file().getAbsolutePath());
        }


        log.debug("startUp Database: " + Utils.GetFileName(databasePath));
        if (myDB != null) {
            log.debug("Database is open ");
            if (this.databasePath.file().getAbsolutePath().equals(databasePath.file().getAbsolutePath())) {
                log.debug("Database is the same");
                if (!myDB.isOpen()) {
                    log.debug("Database was close so open now");
                    myDB.openOrCreateDatabase();
                }

                // is open
                return true;
            }
            log.debug("Database is changed! close " + Utils.GetFileName(this.databasePath));
            if (myDB != null && myDB.isOpen()) myDB.closeDatabase();
            myDB = null;
        }


        this.databasePath = databasePath;
        log.debug("Initial database: " + Utils.GetFileName(databasePath));
        initialize();

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
        return true;
    }


    public String getPath() {
        return this.databasePath.file().getAbsolutePath();
    }


    protected int getDatabaseSchemeVersion() {

        if (!isTableExists("Config")) {
            return -1;
        }

        int result = -1;
        SQLiteGdxDatabaseCursor c = null;
        try {
            c = rawQuery("select Value from Config where [Key] like ?", new String[]{"DatabaseSchemeVersionWin"});
        } catch (Exception exc) {
            return -1;
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String databaseSchemeVersion = c.getString(0);
                result = Integer.parseInt(databaseSchemeVersion);
                c.moveToNext();
            }
        } catch (Exception exc) {
            result = -1;
        }
        if (c != null) {
            c.close();
        }

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
    }

    public boolean isTableExists(String tableName) {
        SQLiteGdxDatabaseCursor cursor = null;
        try {
            cursor = rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName
                    + "' COLLATE NOCASE", null);
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

    public void writeConfigString(String key, String value) {
        Parameters val = new Parameters();
        val.put("Value", value);
        long anz = update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            insert("Config", val);
        }
    }

    public void WriteConfigLongString(String key, String value) {
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
        SQLiteGdxDatabaseCursor c = null;
        boolean found = false;
        try {
            c = rawQuery("select Value from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result = c.getString(0);
                found = true;
                c.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        } finally {
            c.close();
        }

        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public String readConfigLongString(String key) throws Exception {
        String result = "";
        SQLiteGdxDatabaseCursor c = null;
        boolean found = false;
        try {
            c = rawQuery("select LongString from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result = c.getString(0);
                found = true;
                c.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        c.close();

        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public void writeConfigDesiredString(String key, String value) {
        Parameters val = new Parameters();
        val.put("desired", value);
        long anz = update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            insert("Config", val);
        }
    }

    public String readConfigDesiredString(String key) throws Exception {
        String result = "";
        SQLiteGdxDatabaseCursor c = null;
        boolean found = false;
        try {
            c = rawQuery("select desired from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result = c.getString(0);
                found = true;
                c.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        c.close();

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
                    setTransactionSuccessful();
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
                        execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                        execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
                    }
                    if (lastDatabaseSchemeVersion < 1002) {
                        // Long Text Field for long Strings
                        execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
                    }
                    if (lastDatabaseSchemeVersion < 1003) {
                        // Long Text Field for long Strings
                        execSQL("ALTER TABLE [Config] ADD [desired] ntext NULL;");
                    }
                    setTransactionSuccessful();
                } catch (Exception exc) {
                    log.error("alterDatabase", exc);
                } finally {
                    endTransaction();
                }
                break;
        }
    }


    public static boolean waypointExists(String gcCode) {
        SQLiteGdxDatabaseCursor c = Database.Data.rawQuery("select GcCode from Waypoints where GcCode=?", new String[]{gcCode});
        {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                try {
                    c.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            c.close();

            return false;
        }
    }

    public static String createFreeGcCode(String cacheGcCode) throws Exception {
        String suffix = cacheGcCode.substring(2);
        String firstCharCandidates = "CBXADEFGHIJKLMNOPQRSTUVWYZ0123456789";
        String secondCharCandidates = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < firstCharCandidates.length(); i++)
            for (int j = 0; j < secondCharCandidates.length(); j++) {
                String gcCode = firstCharCandidates.substring(i, i + 1) + secondCharCandidates.substring(j, j + 1) + suffix;
                if (!waypointExists(gcCode))
                    return gcCode;
            }
        throw new Exception("Alle GcCodes sind bereits vergeben! Dies sollte eigentlich nie vorkommen!");
    }


    public static String getNote(long cacheId) {
        String resultString = "";
        SQLiteGdxDatabaseCursor c = Database.Data.rawQuery("select Notes from Caches where Id=?", new String[]{String.valueOf(cacheId)});
        c.moveToFirst();
        while (!c.isAfterLast()) {
            resultString = c.getString(0);
            break;
        }
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
        try {
            String resultString = "";
            SQLiteGdxDatabaseCursor c = Database.Data.rawQuery("select Solver from Caches where Id=?", new String[]{String.valueOf(cacheId)});
            c.moveToFirst();
            while (!c.isAfterLast()) {
                resultString = c.getString(0);
                break;
            }
            return resultString;
        } catch (Exception ex) {
            return "";
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
            try {
                String command = "SELECT cacheid FROM logs WHERE Timestamp < '" + TimeStamp + "' GROUP BY CacheId HAVING COUNT(Id) > " + String.valueOf(minToKeep);
                log.debug(command);
                SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(command, null);
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    long tmp = reader.getLong(0);
                    if (!oldLogCaches.contains(tmp))
                        oldLogCaches.add(reader.getLong(0));
                    reader.moveToNext();
                }
                reader.close();
            } catch (Exception ex) {
                log.error("deleteOldLogs", ex);
            }
        }

        // ###################################################
        // get Logs
        // ###################################################
        {
            try {
                beginTransaction();
                for (long oldLogCache : oldLogCaches) {
                    ArrayList<Long> minLogIds = new ArrayList<Long>();
                    String command = "select id from logs where cacheid = " + String.valueOf(oldLogCache) + " order by Timestamp desc";
                    log.debug(command);
                    int count = 0;
                    SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(command, null);
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        if (count == minToKeep)
                            break;
                        minLogIds.add(reader.getLong(0));
                        reader.moveToNext();
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
                setTransactionSuccessful();
            } catch (Exception ex) {
                log.error("deleteOldLogs", ex);
            } finally {
                endTransaction();
            }
        }
    }


    // DB Funktionen

    public void initialize() {
        if (myDB == null) {
            if (!databasePath.exists())
                reset();

            try {
                log.debug("open data base: " + databasePath);
                myDB = SQLiteGdxDatabaseFactory.getNewDatabase(databasePath);
                myDB.openOrCreateDatabase();
            } catch (Exception exc) {
                log.error("Can't open Database", exc);
                return;
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
            myDB = SQLiteGdxDatabaseFactory.getNewDatabase(databasePath);
            myDB.openOrCreateDatabase();
            myDB.setTransactionSuccessful();
            myDB.closeDatabase();

        } catch (Exception exc) {
            log.error("createDB", exc);
        }
    }


    public SQLiteGdxDatabaseCursor rawQuery(String sql, String[] args) {
        if (myDB == null) return null;
        try {
            return myDB.rawQuery(sql, args);
        } catch (SQLiteGdxException e) {
            log.error("rawQuerry", e);
        }
        return null;
    }

    public void execSQL(String sql) {
        try {
            myDB.execSQL(sql);
        } catch (SQLiteGdxException e) {
            log.error("execSQL", e);
        }
    }


    public long update(String tablename, Parameters val, String whereClause, String[] whereArgs) {
        return myDB.update(tablename, val, whereClause, whereArgs);
    }

    public long insert(String tablename, Parameters val) {
        return myDB.insert(tablename, val);
    }

    public long delete(String tablename, String whereClause, String[] whereArgs) {
        return myDB.delete(tablename, whereClause, whereArgs);
    }

    public void beginTransaction() {
        // log.trace("begin transaction");
        if (myDB != null)
            myDB.setAutoCommit(false);
    }

    public void setTransactionSuccessful() {
        //  log.trace("begin transaction");
        if (myDB != null)
            myDB.setTransactionSuccessful();
    }

    public void endTransaction() {
        // log.trace("begin transaction");
        if (myDB != null)
            myDB.endTransaction();
    }

    public long insertWithConflictReplace(String tablename, Parameters val) {
        return myDB.insertWithConflictReplace(tablename, val);
    }

    public long insertWithConflictIgnore(String tablename, Parameters val) {
        return myDB.insertWithConflictIgnore(tablename, val);
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


    // Static methods ##############################################################################################


    public static int getCacheCountInDB(String absolutePath) {
        try {
            SQLiteGdxDatabase tempDB = SQLiteGdxDatabaseFactory.getNewDatabase(Gdx.files.absolute(absolutePath));
            tempDB.openOrCreateDatabase();

            //get schema version
            SQLiteGdxDatabaseCursor cursor = tempDB.rawQuery("SELECT Value FROM Config WHERE [Key] like ?", new String[]{"DatabaseSchemeVersionWin"});
            cursor.moveToFirst();
            int version = Integer.parseInt(cursor.getString(0));

            if (version < 1028) {
                cursor = tempDB.rawQuery("SELECT COUNT(*) FROM caches", null);
            } else {
                cursor = tempDB.rawQuery("SELECT COUNT(*) FROM CacheCoreInfo", null);
            }


            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            tempDB.closeDatabase();
            return count;
        } catch (Exception exc) {
            return -1;
        }
    }

    public static boolean createNewDB(Database database, FileHandle rootFolder, String newDB_Name, Boolean ownRepository) {
        FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));
        FileHandle dbFile = rootFolder.child(newDB_Name + ".db3");
        try {
            SQLiteGdxDatabase db = SQLiteGdxDatabaseFactory.getNewDatabase(dbFile);
            db.openOrCreateDatabase();
            database.close();
            database.startUp(dbFile);
        } catch (SQLiteGdxException e) {
            log.error("Create new DB", e);
            return true;
        }
        // OwnRepository?
        if (ownRepository) {
            String folder = "?/" + newDB_Name + "/";

            Config.DescriptionImageFolderLocal.setValue(folder + "Images");
            Config.MapPackFolderLocal.setValue(folder + "Maps");
            Config.SpoilerFolderLocal.setValue(folder + "Spoilers");
            Config.TileCacheFolderLocal.setValue(folder + "Cache");
            Config.AcceptChanges();
            log.debug(
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
                log.debug(
                        "Problem with creation of one of the Directories:" + //
                                Config.DescriptionImageFolderLocal.getValue() + ", " + //
                                Config.MapPackFolderLocal.getValue() + ", " + //
                                Config.SpoilerFolderLocal.getValue() + ", " + //
                                Config.TileCacheFolderLocal.getValue()//
                );
        }
        return false;
    }
}
