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
package com.badlogic.gdx.sqlite.robovm;

import SQLite.JDBCDriver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxDatabase;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.sqlite.Database.Parameters;
import de.longri.cachebox3.utils.exceptions.NotImplementedException;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map.Entry;

/**
 * Created by Longri on 03.08.16.
 */
public class RobovmDatabase implements SQLiteGdxDatabase {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(RobovmDatabase.class);
    private final FileHandle dbFileHandle;

    Connection myDB = null;


    public RobovmDatabase(FileHandle dbFileHandle) throws ClassNotFoundException{
        this.dbFileHandle = dbFileHandle;
    }

    @Override
    public void setupDatabase() {

    }

    @Override
    public void openOrCreateDatabase() throws SQLiteGdxException {
        JDBCDriver jdbcDriver = new JDBCDriver();
        try {
            String DB_URL = "sqlite:/" + this.dbFileHandle.file().getAbsolutePath();
            myDB = jdbcDriver.connect(DB_URL, null);
        } catch (Exception e) {
            throw new SQLiteGdxException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void closeDatabase() {
        try {
            myDB.close();
            myDB = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public RobovmCursor rawQuery(String sql, String[] args) {
        if (myDB == null)
            return null;

        if (CB.isLogLevel(CB.LOG_LEVEL_DEBUG)) {
            StringBuilder sb = new StringBuilder("RAW_QUERY :" + sql + " ARGs= ");
            if (args != null) {
                for (String arg : args)
                    sb.append(arg + ", ");
            } else
                sb.append("NULL");
            log.debug(sb.toString());
        }

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

        return new RobovmCursor(rs, rowcount, statement);
    }

    @Override
    public SQLiteGdxDatabaseCursor rawQuery(SQLiteGdxDatabaseCursor cursor, String sql) throws SQLiteGdxException {
       throw new NotImplementedException("rawQuery");
    }

    @Override
    public void commit() {
        throw new NotImplementedException("commit");
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        throw new NotImplementedException("prepareStatement");
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        try {
            myDB.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
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

    @Override
    public long update(String tablename, Parameters val, String whereClause, String[] whereArgs) {

        if (CB.isLogLevel(CB.LOG_LEVEL_DEBUG)) {
            StringBuilder sb = new StringBuilder("Update @ Table:" + tablename);
            sb.append("Parameters:" + val.toString());
            sb.append("WHERECLAUSE:" + whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            log.debug(sb.toString());
        }

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

    @Override
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

//    @Override
//    public void beginTransaction() {
//        try {
//            log.trace("begin transaction");
//            if (myDB != null)
//                myDB.setAutoCommit(false);
//        } catch (SQLException e) {
//
//            e.printStackTrace();
//        }
//    }

    @Override
    public void setTransactionSuccessful() {
        try {
            log.trace("set Transaction Successful");
            if (myDB != null)
                myDB.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endTransaction() {
        try {
            log.trace("endTransaction");
            if (myDB != null)
                myDB.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
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

    @Override
    public long insertWithConflictIgnore(String tablename, Parameters val) {
        if (myDB == null)
            return 0;

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


}
