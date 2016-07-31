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

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class SQLiteDatabaseHelper {

    private final String dbName;
    private final int dbVersion;
    private final String dbOnCreateQuery;
    private final String dbOnUpgradeQuery;

    public SQLiteDatabaseHelper(String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
        this.dbName = dbName;
        this.dbVersion = dbVersion;
        this.dbOnCreateQuery = dbOnCreateQuery;
        this.dbOnUpgradeQuery = dbOnUpgradeQuery;
    }

    public void onCreate(Statement stmt) throws SQLException {
        if (dbOnCreateQuery != null) stmt.executeUpdate(dbOnCreateQuery);
    }

    public void onUpgrade(Statement stmt, int oldVersion, int newVersion) throws SQLException {
        if (dbOnUpgradeQuery != null) {
            stmt.executeUpdate(dbOnUpgradeQuery);
            onCreate(stmt);
        }
    }

}
