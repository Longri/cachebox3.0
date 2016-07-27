/*
 * Copyright (C) 2016 team-cachebox.de
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
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map.Entry;


/**
 * Created by Longri on 26.07.16.
 */
public class Database {
    public static Database Data;
    public static Database FieldNotes;
    public static Database Settings;

    private final org.slf4j.Logger log;

    private Connection myDB = null;

    private String databasePath;

    protected boolean newDB = false;

    public long DatabaseId = 0; // for Database replication with WinCachebox

    public long MasterDatabaseId = 0;

    protected int latestDatabaseChange = 0;

    public enum DatabaseType {
        CacheBox, FieldNotes, Settings
    }


    /***
     * Wenn die DB neu erstellt wurde ist der Return Wert bei der ersten Abfrage True
     *
     * @return
     */
    public boolean isDbNew() {
        return newDB;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    protected DatabaseType databaseType;

    public Database(DatabaseType databaseType) throws ClassNotFoundException {

        System.setProperty("sqlite.purejava", "true");
        Class.forName("org.sqlite.JDBC");

        this.databaseType = databaseType;

        log = LoggerFactory.getLogger("Database." + databaseType);

        switch (databaseType) {
            case CacheBox:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseChange;
                break;
            case FieldNotes:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseFieldNoteChange;
                break;
            case Settings:
                latestDatabaseChange = DatabaseVersions.LatestDatabaseSettingsChange;
        }
    }


    public boolean startUp(String databasePath) {
        try {
            log.debug("DB Startup : " + databasePath);
        } catch (Exception e) {
            // gibt beim splash - Start: NPE in Translation.readMissingStringsFile
            // Nachfolgende Starts sollten aber protokolliert werden
        }

        this.databasePath = databasePath;

        Initialize();

        int databaseSchemeVersion = GetDatabaseSchemeVersion();
        if (databaseSchemeVersion < latestDatabaseChange) {
            AlterDatabase.alter(this,databaseSchemeVersion);
            SetDatabaseSchemeVersion();
        }
        SetDatabaseSchemeVersion();
        return true;
    }


    private int GetDatabaseSchemeVersion() {
        int result = -1;
        Cursor c = null;
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

    public void WriteConfigString(String key, String value) {
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

    public String ReadConfigString(String key) throws Exception {
        String result = "";
        Cursor c = null;
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

    public String ReadConfigLongString(String key) throws Exception {
        String result = "";
        Cursor c = null;
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

    public void WriteConfigLong(String key, long value) {
        WriteConfigString(key, String.valueOf(value));
    }

    public long ReadConfigLong(String key) {
        try {
            String value = ReadConfigString(key);
            return Long.valueOf(value);
        } catch (Exception ex) {
            return 0;
        }
    }

    public void Close() {
        try {
            log.debug("close DB:" + databasePath);
            myDB.close();
            myDB = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Initialize() {
        if (myDB == null) {
            if (!Gdx.files.absolute(databasePath).exists())
                Reset();

            //check folder
            FileHandle file = Gdx.files.local(databasePath);

            FileHandle dir = file.parent();
            if (!dir.exists()) {
                dir.mkdirs();
            }


            try {
                log.debug("open data base: " + databasePath);
                myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            } catch (Exception exc) {
                return;
            }
        }
    }

    public void Reset() {
        // if exists, delete old database file
        FileHandle file = Gdx.files.absolute(databasePath);
        if (Gdx.files.absolute(databasePath).exists()) {
            log.debug("RESET DB, delete file: " + databasePath);
            file.delete();

            try {
                log.debug("create data base: " + databasePath);
                myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                myDB.commit();
                myDB.close();

            } catch (Exception exc) {
                log.error("createDB", exc);
            }
        }
    }

    public Cursor rawQuery(String sql, String[] args) {
        if (myDB == null)
            return null;

        StringBuilder sb = new StringBuilder("RAW_QUERY :" + sql + " ARGs= ");
        if (args != null) {
            for (String arg : args)
                sb.append(arg + ", ");
        } else
            sb.append("NULL");
        log.debug(sb.toString());


        ResultSet rs = null;
        PreparedStatement statement = null;
        try {

            statement = myDB.prepareStatement(sql);

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement.setString(i + 1, args[i]);
                }
            }
            rs = statement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // TODO Hack to get Rowcount
        ResultSet rs2 = null;
        int rowcount = 0;
        PreparedStatement statement2 = null;
        try {

            statement2 = myDB.prepareStatement("select count(*) from (" + sql + ")");

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement2.setString(i + 1, args[i]);
                }
            }
            rs2 = statement2.executeQuery();

            rs2.next();

            rowcount = Integer.parseInt(rs2.getString(1));
            statement2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                statement2.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

        return new Cursor(rs, rowcount, statement);
    }


    public void execSQL(String sql) {
        if (myDB == null)
            return;

        log.debug("execSQL : " + sql);

        Statement statement = null;
        try {
            statement = myDB.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }


    public long update(String tablename, Parameters val, String whereClause, String[] whereArgs) {


        StringBuilder sb = new StringBuilder("Update @ Table:" + tablename);
        sb.append("Parameters:" + val.toString());
        sb.append("WHERECLAUSE:" + whereClause);

        if (whereArgs != null) {
            for (String arg : whereArgs) {
                sb.append(arg + ", ");
            }
        }

        log.debug(sb.toString());


        if (myDB == null)
            return 0;

        StringBuilder sql = new StringBuilder();

        sql.append("update ");
        sql.append(tablename);
        sql.append(" set");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            sql.append("=?");
            if (i != val.size()) {
                sql.append(",");
            }
        }

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            if (whereArgs != null) {
                for (int k = 0; k < whereArgs.length; k++) {
                    st.setString(j + k + 1, whereArgs[k]);
                }
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }


    public long insert(String tablename, Parameters val) {
        if (myDB == null)
            return 0;
        StringBuilder sql = new StringBuilder();

        sql.append("insert into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
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
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            log.debug("INSERT: " + sql);
            return st.execute() ? 0 : 1;

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public long delete(String tablename, String whereClause, String[] whereArgs) {
        StringBuilder sb = new StringBuilder("Delete@ Table:" + tablename);
        sb.append("WHERECLAUSE:" + whereClause);

        if (whereArgs != null) {
            for (String arg : whereArgs) {
                sb.append(arg + ", ");
            }
        }

        log.debug(sb.toString());


        if (myDB == null)
            return 0;
        StringBuilder sql = new StringBuilder();

        sql.append("delete from ");
        sql.append(tablename);

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            if (whereArgs != null) {
                for (int i = 0; i < whereArgs.length; i++) {
                    st.setString(i + 1, whereArgs[i]);
                }
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }


    public void beginTransaction() {
        try {
            log.trace("begin transaction");
            if (myDB != null)
                myDB.setAutoCommit(false);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }


    public void setTransactionSuccessful() {
        try {
            log.trace("set Transaction Successful");
            if (myDB != null)
                myDB.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void endTransaction() {
        try {
            log.trace("endTransaction");
            if (myDB != null)
                myDB.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public long insertWithConflictReplace(String tablename, Parameters val) {
        if (myDB == null)
            return 0;

        log.debug("insertWithConflictReplace @Table:" + tablename + "Parameters: " + val.toString());
        StringBuilder sql = new StringBuilder();

        sql.append("insert OR REPLACE into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
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
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }


    public long insertWithConflictIgnore(String tablename, Parameters val) {
        if (myDB == null)
            return 0;

        log.debug("insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());
        log.debug("insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());

        StringBuilder sql = new StringBuilder();

        sql.append("insert OR IGNORE into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
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
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }


    public int getCacheCountInDB(String filename) {

        if (myDB == null)
            return 0;

        int count = 0;
        Connection myDB = null;
        try {
            myDB = DriverManager.getConnection("jdbc:sqlite:" + filename);

            Statement statement = myDB.createStatement();
            ResultSet result = statement.executeQuery("select count(*) from caches");
            // result.first();
            count = result.getInt(1);
            result.close();
            myDB.close();
        } catch (SQLException e) {
            // String s = e.getMessage();
        }
        return count;
    }

}