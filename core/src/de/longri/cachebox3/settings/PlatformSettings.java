package de.longri.cachebox3.settings;

public class PlatformSettings {
	// ------ setPlatformSetting ------
	public interface IPlatformSettings {
		public SettingBase<?> Read(SettingBase<?> setting);

		public void Write(SettingBase<?> setting);

	}

	public static IPlatformSettings platformSettingsListener;

	public static void setPlatformSettings(IPlatformSettings listener) {
		platformSettingsListener = listener;
	}

	public static SettingBase<?> ReadSetting(SettingBase<?> setting) {
		if (platformSettingsListener != null)
			setting = platformSettingsListener.Read(setting);
		return setting;
	}

	public static <T> void WriteSetting(SettingBase<T> setting) {
		if (platformSettingsListener != null)
			platformSettingsListener.Write(setting);
	}

	/**
	 * @return True, if platform settings are set
	 */
	public static boolean canUsePlatformSettings() {
		return (platformSettingsListener != null);
	}

}
