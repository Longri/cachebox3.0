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
package com.badlogic.gdx.sql;

import de.longri.cachebox3.sqlite.Database;

import java.sql.PreparedStatement;

/**
 * This public interface contains the necessary methods to setup and execute queries on a database. The factory method
 * {@link SQLiteGdxDatabaseFactory#getNewDatabase(com.badlogic.gdx.files.FileHandle, int, String, String)} will return a database object that implements this
 * interface. The typical sequence of method calls should be as follows:
 * <ul>
 * <li>{@link SQLiteGdxDatabase#setupDatabase()}</li>
 * <li>{@link SQLiteGdxDatabase#openOrCreateDatabase()}</li>
 * <li>{@link SQLiteGdxDatabase#execSQL(String)} OR</li>
 * <li>{@link SQLiteGdxDatabase#rawQuery(String)} OR</li>
 * <li>{@link SQLiteGdxDatabase#rawQuery(SQLiteGdxDatabaseCursor, String)}</li>
 * <li>{@link SQLiteGdxDatabase#closeDatabase()}</li>
 * </ul>
 *
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public interface SQLiteGdxDatabase {

    /**
     * This method is needed to be called only once before any database related activity can be performed. The method performs the
     * necessary procedures for the database. However, a database will not be opened/created until
     * {@link SQLiteGdxDatabase#openOrCreateDatabase()} is called.
     */
    public void setupDatabase();

    /**
     * Opens an already existing database or creates a new database if it doesn't already exist.
     *
     * @throws SQLiteGdxException
     */
    public void openOrCreateDatabase() throws SQLiteGdxException;

    /**
     * Closes the opened database and releases all the resources related to this database.
     *
     * @throws SQLiteGdxException
     */
    public void closeDatabase() throws SQLiteGdxException;

    /**
     * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     *
     * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
     * @throws SQLiteGdxException
     */
    public void execSQL(String sql) throws SQLiteGdxException;

    /**
     * Runs the provided SQL and returns a {@link SQLiteGdxDatabaseCursor} over the result set.
     *
     * @param sql the SQL query. The SQL string must not be ; terminated
     * @return {@link SQLiteGdxDatabaseCursor}
     * @throws SQLiteGdxException
     */
    public SQLiteGdxDatabaseCursor rawQuery(String sql) throws SQLiteGdxException;

    /**
     * Runs the provided SQL and returns the same {@link SQLiteGdxDatabaseCursor} that was passed to this method. Use this method when you
     * want to avoid reallocation of {@link SQLiteGdxDatabaseCursor} object. Note that you shall only pass the {@link SQLiteGdxDatabaseCursor} object
     * that was previously returned by a rawQuery method. Creating your own {@link SQLiteGdxDatabaseCursor} and then passing it as an object
     * will not work.
     *
     * @param cursor existing {@link SQLiteGdxDatabaseCursor} object
     * @param sql    the SQL query. The SQL string must not be ; terminated
     * @return the passed {@link SQLiteGdxDatabaseCursor}.
     * @throws SQLiteGdxException
     */
    public SQLiteGdxDatabaseCursor rawQuery(SQLiteGdxDatabaseCursor cursor, String sql) throws SQLiteGdxException;

    public void commit();

    public PreparedStatement prepareStatement(String sql);

    public void setAutoCommit(boolean autoCommit);

    public void endTransaction();

    public void setTransactionSuccessful();

    public long insert(String tablename, Database.Parameters val);

    public long update(String tablename, Database.Parameters val, String whereClause, String[] whereArgs);

    public long delete(String tablename, String whereClause, String[] whereArgs);

    public long insertWithConflictReplace(String tablename, Database.Parameters val);

    public long insertWithConflictIgnore(String tablename, Database.Parameters val);
}
