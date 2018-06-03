/*
 * Copyright (C) 2011-2017 team-cachebox.de
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
package de.longri.cachebox3.settings.types;


import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.GdxSqliteCursor;

public class SettingsDAO {
    public void writeToDatabase(Database database, SettingBase<?> setting) {

        if (setting.isDefault()) {
            //delete entry from Database if exist
            GdxSqliteCursor cursor = database.rawQuery("SELECT * FROM Config WHERE Key=?", new String[]{setting.name});
            if (cursor != null && cursor.getCount() > 0)
                database.execSQL("DELETE FROM Config WHERE Key='" + setting.name + "'");
        } else {
            Object dbString = setting.toDbValue();
            if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                database.WriteConfigLongString(setting.name, dbString);
            } else
                database.writeConfigString(setting.name, dbString);
            database.writeConfigDesiredString(setting.name, Long.toString(setting.expiredTime));
        }


    }

    public SettingBase<?> readFromDatabase(Database database, SettingBase<?> setting) {
        try {
            String dbString = null;

            if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                if (setting.name.endsWith("Local")) {
                    try {
                        dbString = database.readConfigString(setting.name.substring(0, setting.name.length() - 5));
                    } catch (Exception ex) {
                        dbString = null;
                    }
                    if (dbString == null)
                        dbString = database.readConfigLongString(setting.name);
                } else {
                    dbString = database.readConfigLongString(setting.name);
                }
            }

            if (dbString == null) {
                dbString = database.readConfigString(setting.name);
            }

            if (dbString == null) {
                setting.loadDefault();
            } else {
                setting.fromDbvalue(dbString);
            }

            String desiredString = database.readConfigDesiredString(setting.name);
            if (desiredString == null) {
                setting.setExpiredTime(-1L);
            } else {
                long time = Long.parseLong(desiredString);
                setting.setExpiredTime(time);
            }

            setting.clearDirty();
        } catch (Exception ex) {
            setting.loadDefault();
        }

        return setting;
    }

    public void writeToPlatformSettings(SettingBase<?> setting) {
        PlatformSettings.WriteSetting(setting);
    }

    public SettingBase<?> readFromPlatformSetting(SettingBase<?> setting) {
        setting = PlatformSettings.ReadSetting(setting);
        return setting;
    }
}
