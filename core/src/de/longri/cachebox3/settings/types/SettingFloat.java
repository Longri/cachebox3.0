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

public class SettingFloat extends SettingBase<Float> {

    public SettingFloat(String name, SettingCategory category, SettingMode modus, float defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        this(name, category, modus, defaultValue, StoreType, usage, false);
    }

    public SettingFloat(String name, SettingCategory category, SettingMode modus, float defaultValue, SettingStoreType StoreType, SettingUsage usage, boolean desired) {
        super(name, category, modus, StoreType, usage, desired);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public Object toDbValue() {
        return String.valueOf(value);
    }

    @Override
    public boolean fromDbvalue(Object dbString) {
        try {
            value = Float.valueOf((String) dbString);
            return true;
        } catch (Exception ex) {
            value = defaultValue;
            return false;
        }
    }

    @Override
    public SettingBase<Float> copy() {
        SettingBase<Float> ret = new SettingFloat(this.name, this.category, this.mode, this.defaultValue, this.storeType, this.usage);
        ret.value = this.value;
        ret.lastValue = this.lastValue;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingFloat))
            return false;

        SettingFloat inst = (SettingFloat) obj;
        if (!(inst.name.equals(this.name)))
            return false;
        if (inst.value != this.value)
            return false;

        return true;
    }
}
