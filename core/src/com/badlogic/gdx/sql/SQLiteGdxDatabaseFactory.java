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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A factory class that creates new database objects and returns references to them. See
 * {@link SQLiteGdxDatabaseFactory#getNewDatabase(FileHandle)} for more details.
 *
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class SQLiteGdxDatabaseFactory {

    public static final String ERROR_TAG = "DATABASE";
    private static final String androidClassname = "com.badlogic.gdx.sqlite.android.AndroidDatabaseManager";
    private static final String desktopClassname = "com.badlogic.gdx.sqlite.desktop.DesktopDatabaseManager";
    private static final String robovmClassname = "com.badlogic.gdx.sqlite.robovm.RobovmDatabaseManager";

    private static SQLiteGdxDatabaseManager databaseManager = null;

    public static void setDatabaseManager(SQLiteGdxDatabaseManager manager) {
        databaseManager = manager;
    }

    /**
     * This is a factory method that will return a reference to an existing or a not-yet-created database. You will need to
     * manually call methods on the {@link SQLiteGdxDatabase} object to setup, open/create or close the database. See {@link SQLiteGdxDatabase} for
     * more details. <b> Note: </b> dbOnUpgradeQuery will only work on an Android device. It will be executed when you increment
     * your database version number. First, dbOnUpgradeQuery will be executed (Where you will generally perform activities such as
     * dropping the tables, etc.). Then dbOnCreateQuery will be executed. However, dbOnUpgradeQuery won't be executed on
     * downgrading the database version.
     *
     * @param dbFileHandle The FileHandle of the database.
     * @return Returns a {@link SQLiteGdxDatabase} object pointing to an existing or not-yet-created database.
     */
    public static SQLiteGdxDatabase getNewDatabase(FileHandle dbFileHandle) {
        chkDatabaseManager();
        return databaseManager.getNewDatabase(dbFileHandle);
    }

    private static void chkDatabaseManager() {
        if (databaseManager == null) {
            switch (Gdx.app.getType()) {
                case Android:
                    try {
                        databaseManager = (SQLiteGdxDatabaseManager) Class.forName(androidClassname).newInstance();
                    } catch (Throwable ex) {
                        throw new GdxRuntimeException("Error getting database: " + androidClassname, ex);
                    }
                    break;
                case HeadlessDesktop:
                case Desktop:
                    try {
                        databaseManager = (SQLiteGdxDatabaseManager) Class.forName(desktopClassname).newInstance();
                    } catch (Throwable ex) {
                        throw new GdxRuntimeException("Error getting database: " + desktopClassname, ex);
                    }
                    break;
                case Applet:
                    throw new GdxRuntimeException("SQLite is currently not supported in Applets by this libgdx extension.");
                case WebGL:
                    throw new GdxRuntimeException("SQLite is currently not supported in WebGL by this libgdx extension.");
                case iOS:
                    try {
                        databaseManager = (SQLiteGdxDatabaseManager) Class.forName(robovmClassname).newInstance();
                    } catch (Throwable ex) {
                        throw new GdxRuntimeException("Error getting database: " + robovmClassname, ex);
                    }
                    break;
            }
        }
    }


    private SQLiteGdxDatabaseFactory() {
    }

}
