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

/**
 * The audio settings contain several settings!<br>
 * <br>
 * Path to the sound file<br>
 * Absolute or ClassPath<br>
 * Mute<br>
 * Volume<br>
 * <br>
 * These are written with the separator '#' in a string.<br>
 *
 * @author Longri
 */
public class SettingsAudio extends SettingBase<Audio> {

    public SettingsAudio(String name, SettingCategory category, SettingMode modus, Audio defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        this(name, category, modus, defaultValue, StoreType, usage, false);
    }

    public SettingsAudio(String name, SettingCategory category, SettingMode modus, Audio defaultValue, SettingStoreType StoreType, SettingUsage usage, boolean desired) {
        super(name, category, modus, StoreType, usage, desired);
        this.defaultValue = defaultValue;
        this.value = new Audio(defaultValue);
    }

    @Override
    public int compareTo(SettingBase<Audio> arg0) {
        // no sort
        return 0;
    }

    @Override
    public Object toDbValue() {
        String ret = "";
        ret += "#" + value.Path;
        ret += "#" + String.valueOf(value.Volume);
        ret += "#" + String.valueOf(value.Mute);
        ret += "#" + String.valueOf(value.Class_Absolute);
        return ret;
    }

    @Override
    public boolean fromDbvalue(Object dbString) {
        String[] values = ((String)dbString).split("#");
        value.Path = values[1];
        value.Volume = Float.parseFloat(values[2]);
        value.Mute = Boolean.parseBoolean(values[3]);
        value.Class_Absolute = Boolean.parseBoolean(values[4]);
        return false;
    }

    @Override
    public SettingBase<Audio> copy() {
        SettingBase<Audio> ret = new SettingsAudio(this.name, this.category, this.mode, this.defaultValue, this.storeType, this.usage);
        ret.value = this.value;
        ret.lastValue = this.lastValue;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingsAudio))
            return false;

        SettingsAudio inst = (SettingsAudio) obj;
        if (!(inst.name.equals(this.name)))
            return false;
        if (inst.value.Mute != this.value.Mute)
            return false;
        if (inst.value.Volume != this.value.Volume)
            return false;
        if (inst.value.Class_Absolute != this.value.Class_Absolute)
            return false;
        if (!inst.value.Path.equals(this.value.Path))
            return false;

        return true;
    }
}
