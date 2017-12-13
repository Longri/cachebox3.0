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

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.settings.types.*;
import de.longri.cachebox3.sqlite.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by Longri on 02.08.16.
 */
public class Config extends Settings {
    static final Logger log = LoggerFactory.getLogger(Config.class);

    public static void AcceptChanges() {
        writeToDB();
    }

    /**
     * Return true, if setting changes need restart
     *
     * @return
     */
    private static boolean writeToDB() {

        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                // Write into DB
                de.longri.cachebox3.settings.types.SettingsDAO dao = new de.longri.cachebox3.settings.types.SettingsDAO();

                Database data = Database.Data;
                Database settingsDB = Database.Settings;

                if (data == null || settingsDB == null || !data.isStarted() || !settingsDB.isStarted())
                    return;

                try {
                    if (data != null)
                        data.beginTransaction();
                } catch (Exception ex) {
                    // do not change Data now!
                    data = null;
                }

                settingsDB.beginTransaction();

                boolean needRestart = false;

                try {
                    for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = de.longri.cachebox3.settings.types.SettingsList.that.iterator(); it.hasNext(); ) {
                        de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
                        if (!setting.isDirty())
                            continue; // is not changed -> do not

                        if (de.longri.cachebox3.settings.types.SettingStoreType.Local == setting.getStoreType()) {
                            if (data != null)
                                dao.writeToDatabase(data, setting);
                        } else if (de.longri.cachebox3.settings.types.SettingStoreType.Global == setting.getStoreType() || (!de.longri.cachebox3.settings.types.PlatformSettings.canUsePlatformSettings() && de.longri.cachebox3.settings.types.SettingStoreType.Platform == setting.getStoreType())) {
                            dao.writeToDatabase(settingsDB, setting);
                        } else if (de.longri.cachebox3.settings.types.SettingStoreType.Platform == setting.getStoreType()) {
                            dao.writeToPlatformSettings(setting);
                            dao.writeToDatabase(settingsDB, setting);
                        }

                        if (setting.needRestart()) {
                            needRestart = true;
                        }


                        if (setting.name.equals("GcAPIStaging") || setting.name.equals("GcAPI")) {
                            //reset ApiKey validation
                            GroundspeakAPI.resetApiIsChecked();

                            //set config stored MemberChipType as expired
                            Calendar cal = Calendar.getInstance();
                            Config.memberChipType.setExpiredTime(cal.getTimeInMillis());
                        }


                        setting.clearDirty();

                    }
                    return;
                } finally {
                    settingsDB.endTransaction();
                    if (data != null)
                        data.endTransaction();
                }

            }
        }, false);
        return false;
    }

    public static void readFromDB(boolean wait) {
        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                Database data = Database.Data;
                Database settingsDB = Database.Settings;
                // Read from DB

                //load all config entries hold in DB
                ObjectMap<String, DbSettingValues> localMap = new ObjectMap<>();
                ObjectMap<String, DbSettingValues> globalMap = new ObjectMap<>();

                if (data != null) {
                    SQLiteGdxDatabaseCursor cursor = data.rawQuery("SELECT * FROM Config", null);
                    cursor.moveToFirst();
                    while (cursor.next()) {
                        String key = cursor.getString(0);
                        DbSettingValues values = new DbSettingValues();
                        values.value = cursor.getString(1);
                        values.longString = cursor.getString(2);
                        values.desired = cursor.getString(3);
                        localMap.put(key, values);
                    }
                }

                if (settingsDB != null) {
                    SQLiteGdxDatabaseCursor cursor = settingsDB.rawQuery("SELECT * FROM Config", null);
                    cursor.moveToFirst();
                    while (cursor.next()) {
                        String key = cursor.getString(0);
                        DbSettingValues values = new DbSettingValues();
                        values.value = cursor.getString(1);
                        values.longString = cursor.getString(1);
                        values.desired = cursor.getString(1);
                        globalMap.put(key, values);
                    }
                }


                de.longri.cachebox3.settings.types.SettingsDAO dao = new de.longri.cachebox3.settings.types.SettingsDAO();
                for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = de.longri.cachebox3.settings.types.SettingsList.that.iterator(); it.hasNext(); ) {
                    de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
                    String debugString;

                    boolean isPlatform = false;
                    boolean isPlattformoverride = false;

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
                    } else if (de.longri.cachebox3.settings.types.SettingStoreType.Global == setting.getStoreType() || (!PlatformSettings.canUsePlatformSettings() && de.longri.cachebox3.settings.types.SettingStoreType.Platform == setting.getStoreType())) {
                        DbSettingValues values = localMap.get(setting.name);
                        if (values == null) {
                            setting.loadDefault();
                        } else {
                            setting.fromDBString(values.longString == null ? values.value : values.longString);
                        }
                    } else if (SettingStoreType.Platform == setting.getStoreType()) {
                        isPlatform = true;
                        setting = dao.readFromPlatformSetting(setting);
                    }

                    if (setting instanceof SettingEncryptedString) {// Don't write encrypted settings in to a log file
                        debugString = "*******";
                    } else {
                        debugString = setting.getValue().toString();
                    }

                    if (isPlatform) {
                        if (isPlattformoverride) {
                            log.debug("Override Platform setting [" + setting.name + "] from DB to: " + debugString);
                        } else {
                            log.debug("Override PlatformDB setting [" + setting.name + "] from Platform to: " + debugString);
                        }
                    } else {
                        if (!setting.getValue().equals(setting.getDefaultValue())) {
                            log.debug("Change " + setting.getStoreType() + " setting [" + setting.name + "] to: " + debugString);
                        } else {
                            log.debug("Default " + setting.getStoreType() + " setting [" + setting.name + "] to: " + debugString);
                        }
                    }
                }
                log.debug("Settings are loaded");
            }
        }, wait);
    }

    public static void LoadFromLastValue() {
        for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = de.longri.cachebox3.settings.types.SettingsList.that.iterator(); it.hasNext(); ) {
            de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
            setting.loadFromLastValue();
        }
    }

    public static void SaveToLastValue() {
        for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = de.longri.cachebox3.settings.types.SettingsList.that.iterator(); it.hasNext(); ) {
            de.longri.cachebox3.settings.types.SettingBase<?> setting = it.next();
            setting.saveToLastValue();
        }
    }

    public static void LoadAllDefaultValues() {
        for (Iterator<de.longri.cachebox3.settings.types.SettingBase<?>> it = SettingsList.that.iterator(); it.hasNext(); ) {
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
