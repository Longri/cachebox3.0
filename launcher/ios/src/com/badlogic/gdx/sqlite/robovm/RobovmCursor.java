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

import com.badlogic.gdx.sql.DatabaseCursor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author truongps (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class RobovmCursor implements DatabaseCursor {

    ResultSet nativeCursor;

    public RobovmCursor(ResultSet resultSet) {
        setNativeCursor(resultSet);
    }


    @Override
    public byte[] getBlob(int columnIndex) {
        // return nativeCursor.getBlob(columnName);
        return null;
    }

    @Override
    public double getDouble(int columnIndex) {
        try {
            return nativeCursor.getDouble(columnIndex + 1);
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public float getFloat(int columnIndex) {
        try {
            return nativeCursor.getFloat(columnIndex + 1);
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public int getInt(int columnIndex) {
        try {
            return nativeCursor.getInt(columnIndex + 1);
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public long getLong(int columnIndex) {
        try {
            return nativeCursor.getLong(columnIndex + 1);
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public short getShort(int columnIndex) {
        try {
            return nativeCursor.getShort(columnIndex + 1);
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public String getString(int columnIndex) {
        try {
            return nativeCursor.getString(columnIndex + 1);
        } catch (SQLException e) {
            return "";
        }
    }

    @Override
    public boolean next() {
        try {
            return nativeCursor.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int getCount() {
        try {
            return nativeCursor.getRow();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public void close() {
        try {
            nativeCursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveToFirst() {
        try {
            nativeCursor.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAfterLast() {
        try {
            return nativeCursor.isAfterLast();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void moveToNext() {
        next();
    }

    @Override
    public boolean isNull(int i) {
        try {
            return nativeCursor.getObject(i) == null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setNativeCursor(ResultSet resultSet) {
        this.nativeCursor = resultSet;
    }

}
