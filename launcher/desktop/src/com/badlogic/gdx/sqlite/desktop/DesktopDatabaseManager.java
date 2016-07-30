
package com.badlogic.gdx.sqlite.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.*;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.sql.*;

/**
 * @author M Rafay Aleem
 */
public class DesktopDatabaseManager implements DatabaseManager {

    private class DesktopDatabase implements Database {

        private SQLiteDatabaseHelper helper = null;

        private final FileHandle dbFileHandle;
        private final int dbVersion;
        private final String dbOnCreateQuery;
        private final String dbOnUpgradeQuery;

        private Connection connection = null;
        private Statement stmt = null;

        private DesktopDatabase(FileHandle dbFileHandle, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
            this.dbFileHandle = dbFileHandle;
            this.dbVersion = dbVersion;
            this.dbOnCreateQuery = dbOnCreateQuery;
            this.dbOnUpgradeQuery = dbOnUpgradeQuery;
        }

        @Override
        public void setupDatabase() {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                Gdx.app.log(DatabaseFactory.ERROR_TAG,
                        "Unable to load the SQLite JDBC driver. Their might be a problem with your build path or project setup.", e);
                throw new GdxRuntimeException(e);
            }
        }

        @Override
        public void openOrCreateDatabase() throws SQLiteGdxException {
            String DB_URL = this.dbFileHandle.file().getAbsolutePath();

            if (helper == null) helper = new SQLiteDatabaseHelper(DB_URL, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);

            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_URL);
                stmt = connection.createStatement();
                helper.onCreate(stmt);
            } catch (SQLException e) {
                throw new SQLiteGdxException(e);
            }
        }

        @Override
        public void closeDatabase() throws SQLiteGdxException {
            try {
                stmt.close();
                connection.close();
            } catch (SQLException e) {
                throw new SQLiteGdxException(e);
            }
        }

        @Override
        public void execSQL(String sql) throws SQLiteGdxException {
            try {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                throw new SQLiteGdxException(e);
            }
        }

        @Override
        public DatabaseCursor rawQuery(String sql) throws SQLiteGdxException {
            DesktopCursor lCursor = new DesktopCursor();
            try {
                ResultSet resultSetRef = stmt.executeQuery(sql);
                lCursor.setNativeCursor(resultSetRef);
                return lCursor;
            } catch (SQLException e) {
                throw new SQLiteGdxException(e);
            }
        }

        @Override
        public DatabaseCursor rawQuery(DatabaseCursor cursor, String sql) throws SQLiteGdxException {
            DesktopCursor lCursor = (DesktopCursor) cursor;
            try {
                ResultSet resultSetRef = stmt.executeQuery(sql);
                lCursor.setNativeCursor(resultSetRef);
                return lCursor;
            } catch (SQLException e) {
                throw new SQLiteGdxException(e);
            }
        }

        @Override
        public void commit() {
            try {
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public PreparedStatement prepareStatement(String sql) {
            try {
                return connection.prepareStatement(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void setAutoCommit(boolean autoCommit) {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void endTransaction() {
            try {
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setTransactionSuccessful() {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Database getNewDatabase(FileHandle dbFileHandle, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
        return new DesktopDatabase(dbFileHandle, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
    }

    @Override
    public DatabaseCursor getNewDatabaseCursor(ResultSet rs) {
        return new DesktopCursor(rs);
    }

    @Override
    public DatabaseCursor getNewDatabaseCursor(ResultSet rs, int rowcount) {
        return new DesktopCursor(rs, rowcount);
    }

}
