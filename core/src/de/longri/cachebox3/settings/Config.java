/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.settings;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.settings.types.*;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 02.08.16.
 */
public class Config extends Settings {
    static final Logger log = LoggerFactory.getLogger(Config.class);

    public static void AcceptChanges() {
        if (settingsList.dirtyList.size > 0)
            writeToDB();
        else
            log.debug("no Setting are dirty, don't write to DB");
    }


    static final AtomicBoolean inWrite = new AtomicBoolean(false);

    /**
     * Return true, if setting changes need restart
     *
     * @return
     */
    private static synchronized boolean writeToDB() {
        if (inWrite.get()) {
            log.warn("Config is in write state, can't run again! try again at 1 sec");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (inWrite.get()) {
                log.warn("Config is in write state, can't run again!");
                return false;
            }
        }
        log.debug("Start write Config to DB!");
        log.debug("Set Config in write to true");
        inWrite.set(true);
        final Array<SettingBase<?>> dirtyList = new Array<>();
        while (settingsList.dirtyList.size > 0) {
            dirtyList.add(settingsList.dirtyList.pop());
        }

        log.debug("Start writing {} settings changed", dirtyList.size);

        try {
            // Write into DB
            SettingsDAO dao = new SettingsDAO();

            final Database data = Database.Data;
            final Database settingsDB = Database.Settings;


            //splitt into local and global list!
            final Array<SettingBase<?>> localList = new Array<>();
            final Array<SettingBase<?>> globalList = new Array<>();
            boolean isAPI = false;
            while (dirtyList.size > 0) {
                SettingBase<?> setting = dirtyList.pop();
                if (setting.name.equals("GcAPIStaging") || setting.name.equals("GcAPI")) {
                    isAPI = true;
                }
                if (setting.getStoreType() == SettingStoreType.Local) {
                    localList.add(setting);
                } else {
                    globalList.add(setting);
                }
            }

            final AtomicBoolean WAITLOCAL = new AtomicBoolean(true);
            final AtomicBoolean WAITGLOBAL = new AtomicBoolean(true);


            if (localList.size > 0) {
                if (!(data == null || !data.isStarted())) {
                    log.debug("Start Async writing Config to local");
                    CB.postAsync(new NamedRunnable("write settings local") {
                        @Override
                        public void run() {
                            writeToDB(data, localList, WAITLOCAL);
                        }
                    });
                } else {
                    log.warn("Can't write local Config, DB's not started");
                    WAITLOCAL.set(false);
                }
            } else {
                WAITLOCAL.set(false);
            }

            if (globalList.size > 0) {
                if (!(settingsDB == null || !settingsDB.isStarted())) {
                    log.debug("Start Async writing Config to global");
                    CB.postAsync(new NamedRunnable("write settings global") {
                        @Override
                        public void run() {
                            writeToDB(settingsDB, globalList, WAITGLOBAL);
                        }
                    });
                } else {
                    log.warn("Can't write global Config, DB's not started");
                    WAITGLOBAL.set(false);
                }
            } else {
                WAITGLOBAL.set(false);
            }

            while (WAITGLOBAL.get() || WAITLOCAL.get()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (isAPI) {
                log.debug("ApiKey changed, reset ApiCheck and set new expired time");
                CB.postOnGlThread(new NamedRunnable("Config") {
                    @Override
                    public void run() {
                        //reset ApiKey validation
                        GroundspeakAPI.resetApiIsChecked();

                        //set config stored MemberChipType as expired
                        Calendar cal = Calendar.getInstance();

                        Config.memberChipType.setExpiredTime(cal.getTimeInMillis());
                        Config.AcceptChanges();
                    }
                });

            }
        } catch (Exception e) {
            log.error("Error on store Config", e);
        } finally {
            log.debug("Set Config in write to false");
            inWrite.set(false);
        }
        return false;
    }

    private static void writeToDB(Database db, Array<SettingBase<?>> settingsList, AtomicBoolean wait) {
        if (settingsList != null && settingsList.size > 0) {
            final GdxSqlitePreparedStatement deleteStatement = db.myDB.prepare("DELETE FROM Config WHERE [Key] = ?;");
            final GdxSqlitePreparedStatement insertStatement = db.myDB.prepare("REPLACE INTO Config VALUES(?,?,?,?,?) ;");

            db.myDB.beginTransaction();
            while (settingsList.size > 0) {
                SettingBase<?> setting = settingsList.pop();
                try {
                    if (setting.isDefault()) {
                        deleteStatement.bind(setting.getName()).commit().reset();
                    } else {
                        if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                            insertStatement.bind(setting.getName(), null, setting.toDBString()
                                    , Long.toString(setting.expiredTime)).commit().reset();
                        } else {
                            insertStatement.bind(setting.getName(), setting.toDBString(), null
                                    , Long.toString(setting.expiredTime)).commit().reset();
                        }
                    }
                } catch (Exception e) {
                    log.error("Store Setting", e);
                }
            }
            db.myDB.endTransaction();
            deleteStatement.close();
            insertStatement.close();
        }
        wait.set(false);
    }

    public static void readFromDB(boolean wait) {
        CB.postOnGlThread(new NamedRunnable("Config") {
            @Override
            public void run() {
                Database data = Database.Data;
                Database settingsDB = Database.Settings;
                // Read from DB

                //load all config entries hold in DB
                ObjectMap<String, DbSettingValues> localMap = new ObjectMap<>();
                ObjectMap<String, DbSettingValues> globalMap = new ObjectMap<>();

                if (data != null) {
                    GdxSqliteCursor cursor = data.rawQuery("SELECT * FROM Config", (String[]) null);
                    cursor.moveToFirst();
                    while (cursor.next()) {
                        String key = cursor.getString(0);
                        DbSettingValues values = new DbSettingValues();
                        values.value = cursor.getString(1);
                        values.longString = cursor.getString(2);
                        if (cursor.getColumnCount() > 3)
                            values.desired = cursor.getString(3);
                        localMap.put(key, values);
                    }
                }

                if (settingsDB != null) {
                    GdxSqliteCursor cursor = settingsDB.rawQuery("SELECT * FROM Config", (String[]) null);
                    cursor.moveToFirst();
                    while (cursor.next()) {
                        String key = cursor.getString(0);
                        DbSettingValues values = new DbSettingValues();
                        values.value = cursor.getString(1);
                        values.longString = cursor.getString(2);
                        if (cursor.getColumnCount() > 3)
                            values.desired = cursor.getString(3);
                        globalMap.put(key, values);
                    }
                }


                for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = settingsList.iterator(); it.hasNext(); ) {
                    de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();


                    if (de.longri.cachebox3.settings.types.SettingStoreType.Local == setting.getStoreType()) {
                        if (data == null)
                            setting.loadDefault();
                        else {
                            DbSettingValues values = localMap.get(setting.name);
                            if (values == null) {
                                setting.loadDefault();
                            } else {
                                setting.fromDBString(values.longString == null ? values.value : values.longString);
                            }
                        }
                    } else if (de.longri.cachebox3.settings.types.SettingStoreType.Global == setting.getStoreType()) {
                        DbSettingValues values = globalMap.get(setting.name);
                        if (values == null) {
                            setting.loadDefault();
                        } else {
                            setting.fromDBString(values.longString == null ? values.value : values.longString);
                        }
                    }

                    if (!setting.isDefault()) {
                        String debugString;
                        if (setting instanceof SettingEncryptedString) {// Don't write encrypted settings in to a log file
                            debugString = "*******";
                        } else {
                            debugString = setting.getValue().toString();
                        }
                        log.debug("Change " + setting.getStoreType() + " setting [" + setting.name + "] to: " + debugString);
                    }

                }
                log.debug("Settings are loaded");
            }
        }, wait);
    }

    public static void LoadFromLastValue() {
        for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = settingsList.iterator(); it.hasNext(); ) {
            de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
            setting.loadFromLastValue();
        }
    }

    public static void SaveToLastValue() {
        for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = settingsList.iterator(); it.hasNext(); ) {
            de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
            setting.saveToLastValue();
        }
    }

    public static void LoadAllDefaultValues() {
        for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = settingsList.iterator(); it.hasNext(); ) {
            de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
            setting.loadDefault();
        }
    }


    private static class DbSettingValues {
//        CREATE TABLE Config (
//            [Key]      NVARCHAR (30)  NOT NULL,
//            Value      NVARCHAR (255),
//            LongString NTEXT,
//            desired    NTEXT
//        );

        private String value, longString, desired;

    }

}
