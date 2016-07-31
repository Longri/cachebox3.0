package de.longri.cachebox3.settings;

public class Audio {

	public Audio(String path, boolean absolute, boolean mute, float volume) {
		super();
		this.Path = path;
		this.Class_Absolute = absolute;
		this.Mute = mute;
		this.Volume = volume;
	}

	public Audio(Audio value) {
		Path = value.Path;
		Volume = value.Volume;
		Mute = value.Mute;
		Class_Absolute = value.Class_Absolute;
	}

	public String Path;
	public float Volume;
	public boolean Mute;
	public boolean Class_Absolute;

	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof Audio) {
			ret = true;
			Audio aud = (Audio) obj;
			if (!Path.equalsIgnoreCase(aud.Path))
				ret = false;
			if (Class_Absolute != aud.Class_Absolute)
				ret = false;
			if (Mute != aud.Mute)
				ret = false;
			if (Volume != aud.Volume)
				ret = false;
		}

		return ret;
	}
}
