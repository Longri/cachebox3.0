package com.badlogic.gdx.sqlite.robovm;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseManager;


/**
 * @author truongps
 */
public class RobovmDatabaseManager implements DatabaseManager {

    @Override
    public Database getNewDatabase(FileHandle dbFileHandle, int dbVersion,
                                   String dbOnCreateQuery, String dbOnUpgradeQuery) {
        return new RobovmDatabase(dbFileHandle, dbVersion, dbOnCreateQuery,
                dbOnUpgradeQuery);
    }

//
//    @Override
//    public DatabaseCursor getNewDatabaseCursor(ResultSet rs) {
//        return new RobovmCursor(rs);
//    }
//
//    @Override
//    public DatabaseCursor getNewDatabaseCursor(ResultSet rs, int rowcount) {
//        return new RobovmCursor(rs, rowcount);
//    }
}
