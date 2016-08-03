package de.longri.cachebox3.settings;


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
