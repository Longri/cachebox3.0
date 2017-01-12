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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class PlatformSettings {


    private static Preferences prefs;


    private static void checkPrefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("CacheboxPreferences");
        }
    }


    public static SettingBase<?> ReadSetting(SettingBase<?> setting) {
        checkPrefs();
        if (setting instanceof SettingString) {
            String value = prefs.getString(setting.getName(), "");
            ((SettingString) setting).setValue(value);
        } else if (setting instanceof SettingBool) {
            boolean value = prefs.getBoolean(setting.getName(), ((SettingBool) setting).getDefaultValue());
            ((SettingBool) setting).setValue(value);
        } else if (setting instanceof SettingInt) {
            int value = prefs.getInteger(setting.getName(), ((SettingInt) setting).getDefaultValue());
            ((SettingInt) setting).setValue(value);
        }
        setting.clearDirty();
        return setting;
    }

    public static <T> void WriteSetting(SettingBase<T> setting) {
        checkPrefs();

        if (setting instanceof SettingBool) {
            prefs.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
        } else if (setting instanceof SettingString) {
            prefs.putString(setting.getName(), ((SettingString) setting).getValue());
        } else if (setting instanceof SettingInt) {
            prefs.putInteger(setting.getName(), ((SettingInt) setting).getValue());
        }
        // Commit the edits!
        prefs.flush();
    }

    /**
     * @return True, if platform settings are set
     */
    public static boolean canUsePlatformSettings() {
        return true;
    }

}
