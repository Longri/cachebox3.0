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

public class SettingsDAO {
    public void WriteToDatabase(Database database, SettingBase<?> setting) {
        String dbString = setting.toDBString();
        if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
            database.WriteConfigLongString(setting.name, dbString);
        } else
            database.WriteConfigString(setting.name, dbString);
    }

    public SettingBase<?> ReadFromDatabase(Database database, SettingBase<?> setting) {
        try {
            String dbString = null;

            if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                if (setting.name.endsWith("Local")) {
                    try {
                        dbString = database.ReadConfigString(setting.name.substring(0, setting.name.length() - 5));
                    } catch (Exception ex) {
                        dbString = null;
                    }
                    if (dbString == null)
                        dbString = database.ReadConfigLongString(setting.name);
                } else {
                    dbString = database.ReadConfigLongString(setting.name);
                }
            }

            if (dbString == null) {
                dbString = database.ReadConfigString(setting.name);
            }

            if (dbString == null) {
                setting.loadDefault();
            } else {
                setting.fromDBString(dbString);
            }

            setting.clearDirty();
        } catch (Exception ex) {
            setting.loadDefault();
        }

        return setting;
    }

    public void WriteToPlatformSettings(SettingBase<?> setting) {
        PlatformSettings.WriteSetting(setting);
    }

    public SettingBase<?> ReadFromPlatformSetting(SettingBase<?> setting) {
        setting = PlatformSettings.ReadSetting(setting);
        return setting;
    }
}
