package de.longri.cachebox3.settings.types;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import de.longri.cachebox3.settings.SettingBase;

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
