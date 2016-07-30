package com.badlogic.gdx.sqlite.robovm;

import SQLite.JDBCDriver;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;

import java.io.File;
import java.sql.*;

/**
 * @author truongps
 */
public class RobovmDatabase implements Database {
    static final String TAG = "RobovmDatabase";


    private final String dbName;
    private final int dbVersion;
    private final String dbOnCreateQuery;
    private final String dbOnUpgradeQuery;

    Connection connection;
    Statement statement;

    public RobovmDatabase(String dbName, int dbVersion, String dbOnCreateQuery,
                          String dbOnUpgradeQuery) {
        this.dbName = dbName;
        this.dbVersion = dbVersion;
        this.dbOnCreateQuery = dbOnCreateQuery;
        this.dbOnUpgradeQuery = dbOnUpgradeQuery;
    }

    @Override
    public void setupDatabase() {

    }

    @Override
    public void openOrCreateDatabase() throws SQLiteGdxException {
        JDBCDriver jdbcDriver = new JDBCDriver();
        try {
            String DB_URL = "sqlite:/"
                    + (new File(System.getenv("HOME"), "Library/local/" + dbName))
                    .getAbsolutePath();
            connection = jdbcDriver.connect(DB_URL, null);
            statement = connection.createStatement();
        } catch (Exception e) {
            throw new SQLiteGdxException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void closeDatabase() throws SQLiteGdxException {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new SQLiteGdxException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void execSQL(String sql) throws SQLiteGdxException {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new SQLiteGdxException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public DatabaseCursor rawQuery(String sql) throws SQLiteGdxException {
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            RobovmCursor databaseCursor = new RobovmCursor(resultSet);
            return databaseCursor;
        } catch (SQLException e) {
            throw new SQLiteGdxException(e);
        }
    }

    @Override
    public DatabaseCursor rawQuery(DatabaseCursor cursor, String sql)
            throws SQLiteGdxException {
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            ((RobovmCursor) cursor).setNativeCursor(resultSet);
            return cursor;
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
