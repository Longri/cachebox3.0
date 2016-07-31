
package com.badlogic.gdx.sqlite.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import de.longri.cachebox3.sqlite.CacheboxDatabase;

import java.sql.*;
import java.util.Map;

/**
 * @author M Rafay Aleem
 */
public class DesktopDatabaseManager implements DatabaseManager {


    @Override
    public Database getNewDatabase(FileHandle dbFileHandle, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
        return new DesktopDatabase(dbFileHandle, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
    }

}
