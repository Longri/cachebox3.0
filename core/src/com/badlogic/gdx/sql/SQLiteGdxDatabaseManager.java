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

import com.badlogic.gdx.files.FileHandle;

/**
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public interface SQLiteGdxDatabaseManager {

    /**
     * This method will return a reference to an existing or a not-yet-created database. You will need to manually call methods on
     * the {@link SQLiteGdxDatabase} object to setup, open/create or close the database. See {@link SQLiteGdxDatabase} for more details. <b> Note:
     * </b> dbOnUpgradeQuery will only work on an Android device. It will be executed when you increment your database version
     * number. First, dbOnUpgradeQuery will be executed (Where you will generally perform activities such as dropping the tables,
     * etc.). Then dbOnCreateQuery will be executed. However, dbOnUpgradeQuery won't be executed on downgrading the database
     * version.
     *
     * @param dbFileHandle     The name of the database.
     * @param dbVersion        number of the database (starting at 1); if the database is older, dbOnUpgradeQuery will be used to upgrade
     *                         the database (on Android only)
     * @param dbOnCreateQuery  The query that should be executed on the creation of the database. This query would usually create
     *                         the necessary tables in the database.
     * @param dbOnUpgradeQuery The query that should be executed on upgrading the database from an old version to a new one.
     * @return Returns a {@link SQLiteGdxDatabase} object pointing to an existing or not-yet-created database.
     */
    public SQLiteGdxDatabase getNewDatabase(FileHandle dbFileHandle, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery);

//	SQLiteGdxDatabaseCursor getNewDatabaseCursor(ResultSet rs, int rowcount);
//
//	SQLiteGdxDatabaseCursor getNewDatabaseCursor(ResultSet rs);
}
