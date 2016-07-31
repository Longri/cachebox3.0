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

package com.badlogic.gdx.sqlite.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxRuntimeException;

/**
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class AndroidCursor implements SQLiteGdxDatabaseCursor {

    private Cursor cursor = null;

    @Override
    public byte[] getBlob(int columnIndex) {
        try {
            return cursor.getBlob(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the blob", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public double getDouble(int columnIndex) {
        try {
            return cursor.getDouble(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the double", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public float getFloat(int columnIndex) {
        try {
            return cursor.getFloat(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the float", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public int getInt(int columnIndex) {
        try {
            return cursor.getInt(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the int", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public long getLong(int columnIndex) {
        try {
            return cursor.getLong(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the long", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public short getShort(int columnIndex) {
        try {
            return cursor.getShort(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the short", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public String getString(int columnIndex) {
        try {
            return cursor.getString(columnIndex);
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in getting the string", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public boolean next() {
        try {
            return cursor.moveToNext();
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in moving the cursor to next", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public int getCount() {
        int count = -1;
        try {
            count = cursor.getCount();
            return count;
        } catch (SQLiteException e) {
            throw new SQLiteGdxRuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            cursor.close();
        } catch (SQLiteException e) {
            Gdx.app.log(SQLiteGdxDatabaseFactory.ERROR_TAG, "There was an error in closing the cursor", e);
            throw new SQLiteGdxRuntimeException(e);
        }
    }


    @Override
    public void moveToFirst() {
        cursor.moveToFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor.isAfterLast();
    }

    @Override
    public void moveToNext() {
        cursor.moveToNext();
    }

    @Override
    public boolean isNull(int i) {
        return cursor.isNull(i);
    }

    public void setNativeCursor(Cursor cursorRef) {
        cursor = cursorRef;
    }
}
