package com.badlogic.gdx.sqlite.robovm;

import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseManager;

import java.sql.ResultSet;


/**
 * @author truongps
 */
public class RobovmDatabaseManager implements DatabaseManager {

    @Override
    public Database getNewDatabase(String dbName, int dbVersion,
                                   String dbOnCreateQuery, String dbOnUpgradeQuery) {
        return new RobovmDatabase(dbName, dbVersion, dbOnCreateQuery,
                dbOnUpgradeQuery);
    }


    @Override
    public DatabaseCursor getNewDatabaseCursor(ResultSet rs) {
        return new RobovmCursor(rs);
    }

    @Override
    public DatabaseCursor getNewDatabaseCursor(ResultSet rs, int rowcount) {
        return new RobovmCursor(rs, rowcount);
    }
}
