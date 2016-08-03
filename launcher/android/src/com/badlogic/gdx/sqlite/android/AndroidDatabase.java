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
package com.badlogic.gdx.sqlite.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxDatabase;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.sqlite.Database;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class AndroidDatabase implements SQLiteGdxDatabase {

    final org.slf4j.Logger log = LoggerFactory.getLogger(AndroidDatabase.class);
    private SQLiteDatabaseHelper helper;
    private SQLiteDatabase database;
    private Context context;

    private final FileHandle dbFileHandle;


    public AndroidDatabase(Context context, FileHandle dbFileHandle) {
        this.context = context;
        this.dbFileHandle = dbFileHandle;
    }

    @Override
    public void setupDatabase() {
        helper = new SQLiteDatabaseHelper(this.context, dbFileHandle, null);
    }

    @Override
    public void openOrCreateDatabase() throws SQLiteGdxException {
        if (helper == null) setupDatabase();

        try {
            database = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            throw new SQLiteGdxException(e);
        }
    }

    @Override
    public void closeDatabase() throws SQLiteGdxException {
        try {
            helper.close();
        } catch (SQLiteException e) {
            throw new SQLiteGdxException(e);
        }
    }

    @Override
    public void execSQL(String sql) throws SQLiteGdxException {
        try {
            database.execSQL(sql);
        } catch (SQLException e) {
            throw new SQLiteGdxException(e);
        }
    }

    @Override
    public SQLiteGdxDatabaseCursor rawQuery(String sql, String[] args) throws SQLiteGdxException {
        AndroidCursor aCursor = new AndroidCursor();
        try {
            Cursor tmp = database.rawQuery(sql, args);
            aCursor.setNativeCursor(tmp);
            return aCursor;
        } catch (SQLiteException e) {
            throw new SQLiteGdxException(e);
        }
    }
    
    @Override
    public void setAutoCommit(boolean autoCommit) {
        if (autoCommit) {
            database.endTransaction();
        } else {
            database.beginTransaction();
        }
    }


    @Override
    public void endTransaction() {
        try {
            database.endTransaction();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void setTransactionSuccessful() {
        try {
            database.setTransactionSuccessful();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public long insert(String tablename, Database.Parameters val) {
        ContentValues values = getContentValues(val);

        long ret = -1;
        try {
            log.debug("INSERT into: " + tablename + "values: " + values.toString());
            database.insert(tablename, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public long update(String tablename, Database.Parameters val, String whereClause, String[] whereArgs) {

        if (CB.isLogLevel(CB.LOG_LEVEL_DEBUG)) {
            StringBuilder sb = new StringBuilder("Update Table:" + tablename);
            sb.append("Parameters:" + val.toString());
            sb.append("WHERECLAUSE:" + whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            log.debug(sb.toString());
        }

        try {
            ContentValues values = getContentValues(val);
            return database.update(tablename, values, whereClause, whereArgs);
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public long delete(String tablename, String whereClause, String[] whereArgs) {
        if (CB.isLogLevel(CB.LOG_LEVEL_DEBUG)) {
            StringBuilder sb = new StringBuilder("Delete@ Table:" + tablename);
            sb.append("WHERECLAUSE:" + whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            log.debug(sb.toString());
        }

        return database.delete(tablename, whereClause, whereArgs);
    }

    @Override
    public long insertWithConflictReplace(String tablename, Database.Parameters val) {
        log.debug("insertWithConflictReplace @Table:" + tablename + "Parameters: " + val.toString());
        ContentValues values = getContentValues(val);
        return database.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public long insertWithConflictIgnore(String tablename, Database.Parameters val) {
        log.debug("insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());
        ContentValues values = getContentValues(val);
        return database.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private ContentValues getContentValues(Database.Parameters val) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            Object o = entry.getValue();
            if (o instanceof Boolean)
                values.put(entry.getKey(), (Boolean) entry.getValue());
            else if (o instanceof Byte)
                values.put(entry.getKey(), (Byte) entry.getValue());
            else if (o instanceof byte[])
                values.put(entry.getKey(), (byte[]) entry.getValue());
            else if (o instanceof Double)
                values.put(entry.getKey(), (Double) entry.getValue());
            else if (o instanceof Float)
                values.put(entry.getKey(), (Float) entry.getValue());
            else if (o instanceof Integer)
                values.put(entry.getKey(), (Integer) entry.getValue());
            else if (o instanceof Long)
                values.put(entry.getKey(), (Long) entry.getValue());
            else if (o instanceof Short)
                values.put(entry.getKey(), (Short) entry.getValue());
            else if (o instanceof String)
                values.put(entry.getKey(), (String) entry.getValue());
            else
                values.put(entry.getKey(), entry.getValue().toString());
        }
        return values;
    }

}
