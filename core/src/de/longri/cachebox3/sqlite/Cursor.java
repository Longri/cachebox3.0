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
package de.longri.cachebox3.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by Longri on 26.07.16.
 */
public class Cursor {

    private ResultSet rs;
    private PreparedStatement ps;
    private int rowcount;

    public Cursor(ResultSet rs, PreparedStatement ps) {
        this.rs = rs;
        this.ps = ps;
    }

    public Cursor(ResultSet rs, int rowcount, PreparedStatement ps) {
        this.rs = rs;
        this.rowcount = rowcount;
        this.ps = ps;
    }


    public boolean moveToFirst() {
        try {
            if (rs.isBeforeFirst()) {
                rs.next();
            }
            if (rs.isFirst())
                return true;
            return rs.first();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAfterLast() {

        try {
            return rs.isAfterLast();
        } catch (Exception e) {
            return true;
        }

    }


    public boolean moveToNext() {
        try {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }


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


    public String getString(int columnIndex) {

        try {
            return rs.getString(columnIndex + 1);
        } catch (SQLException e) {
            return null;
        }

    }


    public String getString(String column) {

        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }

    }


    public long getLong(int columnIndex) {

        try {
            return rs.getLong(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }

    }


    public long getLong(String column) {

        try {
            return rs.getLong(column);
        } catch (SQLException e) {
            return 0;
        }

    }


    public int getInt(int columnIndex) {
        try {
            return rs.getInt(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getInt(String column) {
        try {
            return rs.getInt(column);
        } catch (SQLException e) {
            return 0;
        }
    }

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


    public boolean isNull(String column) {

        try {
            if (rs.getObject(column) == null || rs.getObject(column).toString().length() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

    }


    public double getDouble(int columnIndex) {
        try {
            return rs.getDouble(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }


    public double getDouble(String column) {
        try {
            return rs.getDouble(column);
        } catch (SQLException e) {
            return 0;
        }
    }


    public short getShort(int columnIndex) {
        try {
            return rs.getShort(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }


    public short getShort(String column) {
        try {
            return rs.getShort(column);
        } catch (SQLException e) {
            return 0;
        }
    }


    public int getCount() {
        return rowcount;
    }

}