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
package com.badlogic.gdx.sqlite.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxRuntimeException;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This is a Desktop implementation of the public interface {@link SQLiteGdxDatabaseCursor}. Note that columns in JDBC are not zero-based
 * and hence +1 has been added to accomodate for this difference.
 *
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class DesktopCursor implements SQLiteGdxDatabaseCursor {

    /**
     * Reference of {@code CachedRowSetImpl} Class Type created for both forward, backward, and random traversing the records, as for ResultSet Class Type
     * sqlite does not support other than forward traversing
     */
    private CachedRowSetImpl resultSet = null;

    @Override
    public byte[] getBlob(int columnIndex) {
        try {
            Blob blob = resultSet.getBlob(columnIndex + 1);
            return blob.getBytes(1, (int) blob.length());
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the blob", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public double getDouble(int columnIndex) {
        try {
            return resultSet.getDouble(columnIndex + 1);
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the double", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public float getFloat(int columnIndex) {
        try {
            return resultSet.getFloat(columnIndex + 1);
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the float", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public int getInt(int columnIndex) {
        try {
            return resultSet.getInt(columnIndex + 1);
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the int", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public long getLong(int columnIndex) {
        try {
            return resultSet.getLong(columnIndex + 1);
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the long", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public short getShort(int columnIndex) {
        try {
            return resultSet.getShort(columnIndex + 1);
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the short", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public String getString(int columnIndex) {
        try {
            return resultSet.getString(columnIndex + 1);
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the string", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public boolean next() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in moving the cursor to next", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public int getCount() {
        return getRowCount(resultSet);
    }


    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in closing the cursor", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public void moveToFirst() {
        try {
            resultSet.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAfterLast() {
        try {
            return resultSet.isAfterLast();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void moveToNext() {
        try {
            resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isNull(int i) {
        try {
            return resultSet.getObject(i) == null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getRowCount(ResultSet resultSet) {


        if (resultSet == null) {
            return 0;
        }

        try {

            resultSet.last();

            return resultSet.getRow();

        } catch (SQLException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error counting the number of results", e);
            throw new SQLiteGdxRuntimeException(e);
        } finally {

            try {


                resultSet.beforeFirst();


            } catch (SQLException e) {
                Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error counting the number of results", e);
            }
        }
    }

    public void setNativeCursor(ResultSet resultSetRef) {
        try {
            resultSet = new CachedRowSetImpl();
            resultSet.populate(resultSetRef);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
