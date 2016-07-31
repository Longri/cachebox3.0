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

import android.content.Context;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseManager;

/**
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class AndroidDatabaseManager implements DatabaseManager {

    private Context context;

    public AndroidDatabaseManager() {
        AndroidApplication app = (AndroidApplication) Gdx.app;
        context = app.getApplicationContext();
    }

    @Override
    public Database getNewDatabase(FileHandle dbfileHandle, int databaseVersion, String databaseCreateQuery, String dbOnUpgradeQuery) {
        return new AndroidDatabase(this.context, dbfileHandle, databaseVersion, databaseCreateQuery, dbOnUpgradeQuery);
    }



}
