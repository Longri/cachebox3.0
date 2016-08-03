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

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by Longri on 03.08.16.
 */
public class RobovmCursor implements SQLiteGdxDatabaseCursor {

    private ResultSet rs;
    private PreparedStatement ps;
    private int rowcount;

    public RobovmCursor(ResultSet rs, PreparedStatement ps) {
        this.rs = rs;
        this.ps = ps;
    }

    public RobovmCursor(ResultSet rs, int rowcount, PreparedStatement ps) {
        this.rs = rs;
        this.rowcount = rowcount;
        this.ps = ps;
    }

    @Override
    public void moveToFirst() {
        try {
            if (rs.isBeforeFirst()) {
                rs.next();
            }
            if (rs.isFirst())
                return ;
             rs.first();
        } catch (Exception e) {

        }
    }

    @Override
    public boolean isAfterLast() {

        try {
            return rs.isAfterLast();
        } catch (Exception e) {
            return true;
        }

    }

    @Override
    public void moveToNext() {
        try {
            rs.next();
        } catch (SQLException e) {

        }
    }

    @Override
    public void close() {
        try {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rs = null;
        ps = null;
    }

    @Override
    public String getString(int columnIndex) {

        try {
            return rs.getString(columnIndex + 1);
        } catch (SQLException e) {
            return null;
        }

    }

    @Override
    public boolean next() {
        return false;
    }


    @Override
    public long getLong(int columnIndex) {

        try {
            return rs.getLong(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }

    }



    @Override
    public int getInt(int columnIndex) {
        try {
            return rs.getInt(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }



    @Override
    public boolean isNull(int columnIndex) {

        try {
            if (rs.getObject(columnIndex + 1) == null || rs.getObject(columnIndex + 1).toString().length() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

    }

//    @Override
//    public boolean isNull(String column) {
//
//        try {
//            if (rs.getObject(column) == null || rs.getObject(column).toString().length() == 0) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (SQLException e) {
//            return false;
//        }
//
//    }

    @Override
    public byte[] getBlob(int columnIndex) {
        return new byte[0];
    }

    @Override
    public double getDouble(int columnIndex) {
        try {
            return rs.getDouble(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public float getFloat(int columnIndex) {
        return 0;
    }


    @Override
    public short getShort(int columnIndex) {
        try {
            return rs.getShort(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }



    @Override
    public int getCount() {
        return rowcount;
    }
}
