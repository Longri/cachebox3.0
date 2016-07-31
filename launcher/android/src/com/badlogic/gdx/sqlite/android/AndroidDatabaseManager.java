
package com.badlogic.gdx.sqlite.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseManager;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.sqlite.CacheboxDatabase;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.Map;

/**
 * @author M Rafay Aleem
 */
public class AndroidDatabaseManager implements DatabaseManager {

    private Context context;

    public AndroidDatabaseManager() {
        AndroidApplication app = (AndroidApplication) Gdx.app;
        context = app.getApplicationContext();
    }

    @Override
    public Database getNewDatabase(FileHandle dbfileHandle, int databaseVersion, String databaseCreateQuery, String dbOnUpgradeQuery) {
        return new AndroidDatabase(this.context, dbfileHandle, databaseVersion, databaseCreateQuery, dbOnUpgradeQuery);
    }



}
