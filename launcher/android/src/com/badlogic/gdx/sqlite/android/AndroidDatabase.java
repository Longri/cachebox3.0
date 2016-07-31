package com.badlogic.gdx.sqlite.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.sqlite.CacheboxDatabase;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.Map;

/**
 * Created by Longri on 30.07.16.
 */
public class AndroidDatabase implements Database {

    final org.slf4j.Logger log = LoggerFactory.getLogger(AndroidDatabase.class);
    private SQLiteDatabaseHelper helper;
    private SQLiteDatabase database;
    private Context context;

    private final FileHandle dbFileHandle;
    private final int dbVersion;
    private final String dbOnCreateQuery;
    private final String dbOnUpgradeQuery;

    public AndroidDatabase(Context context, FileHandle dbFileHandle, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
        this.context = context;
        this.dbFileHandle = dbFileHandle;
        this.dbVersion = dbVersion;
        this.dbOnCreateQuery = dbOnCreateQuery;
        this.dbOnUpgradeQuery = dbOnUpgradeQuery;
    }

    @Override
    public void setupDatabase() {
        helper = new SQLiteDatabaseHelper(this.context, dbFileHandle, null, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
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
    public DatabaseCursor rawQuery(String sql) throws SQLiteGdxException {
        AndroidCursor aCursor = new AndroidCursor();
        try {
            Cursor tmp = database.rawQuery(sql, null);
            aCursor.setNativeCursor(tmp);
            return aCursor;
        } catch (SQLiteException e) {
            throw new SQLiteGdxException(e);
        }
    }

    @Override
    public DatabaseCursor rawQuery(DatabaseCursor cursor, String sql) throws SQLiteGdxException {
        AndroidCursor aCursor = (AndroidCursor) cursor;
        try {
            Cursor tmp = database.rawQuery(sql, null);
            aCursor.setNativeCursor(tmp);
            return aCursor;
        } catch (SQLiteException e) {
            throw new SQLiteGdxException(e);
        }
    }

    @Override
    public void commit() {
        database.setTransactionSuccessful();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {

        SQLiteStatement statement = database.compileStatement(sql);

        return null;
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
        database.endTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    @Override
    public long insert(String tablename, CacheboxDatabase.Parameters val) {
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
    public long update(String tablename, CacheboxDatabase.Parameters val, String whereClause, String[] whereArgs) {

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

            log.debug( sb.toString());
        }

        return database.delete(tablename, whereClause, whereArgs);
    }

    @Override
    public long insertWithConflictReplace(String tablename, CacheboxDatabase.Parameters val) {
        log.debug( "insertWithConflictReplace @Table:" + tablename + "Parameters: " + val.toString());
        ContentValues values = getContentValues(val);
        return database.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public long insertWithConflictIgnore(String tablename, CacheboxDatabase.Parameters val) {
        log.debug("insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());
        ContentValues values = getContentValues(val);
        return database.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private ContentValues getContentValues(CacheboxDatabase.Parameters val) {
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
