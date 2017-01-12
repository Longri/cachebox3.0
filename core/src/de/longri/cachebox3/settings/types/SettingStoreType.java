package de.longri.cachebox3.settings.types;

/**
 * Wo wird der Settings Wert Abgespeichert? <br>
 * <br>
 * Global = config.db3<br>
 * Local = aktuelle DB<br>
 * Platform = über den PlatformConector
 * 
 * @author Longri
 */
public enum SettingStoreType {
	Global, Local, Platform;
}
