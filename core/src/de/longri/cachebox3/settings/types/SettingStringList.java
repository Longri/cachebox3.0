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

import org.slf4j.Logger;

/**
 * @author Longri  2016
 */
public class SettingStringList extends SettingBase<String[]> {
    final static Logger log = org.slf4j.LoggerFactory.getLogger(SettingStringList.class);

    public SettingStringList(String name, SettingCategory category, SettingMode modus, String[] defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        this(name, category, modus, defaultValue, StoreType, usage, false);
    }

    public SettingStringList(String name, SettingCategory category, SettingMode modus, String[] defaultValue, SettingStoreType StoreType, SettingUsage usage, boolean desired) {
        super(name, category, modus, StoreType, usage, desired);
        this.defaultValue = defaultValue;
    }

    @Override
    public Object toDbValue() {

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (String str : value) {
            sb.append(str);
            if (idx++ < value.length - 1)
                sb.append(SettingString.STRING_SPLITTER);
        }
        return sb.toString();
    }

    @Override
    public boolean fromDbvalue(Object dbString) {
        try {
            value = ((String)dbString).split(SettingString.STRING_SPLITTER);
            return true;
        } catch (Exception ex) {
            value = defaultValue;
            return false;
        }
    }

    @Override
    public SettingBase<String[]> copy() {
        SettingBase<String[]> ret = new SettingStringList(this.name, this.category, this.mode, this.defaultValue, this.storeType, usage);
        ret.value = this.value;
        ret.lastValue = this.lastValue;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingStringList))
            return false;

        SettingStringList inst = (SettingStringList) obj;
        if (!(inst.name.equals(this.name)))
            return false;
        if (this.value == null) {
            if (inst.value == null)
                return true;
            return false;
        }
        if (!ifValueEquals(inst.value))
            return false;

        return true;
    }

    @Override
    protected boolean ifValueEquals(String[] newValue) {

        if (value.length != newValue.length)
            return false;

        for (int i = 0; i < value.length; i++) {
            if (!value[i].equals(newValue[i]))
                return false;
        }

        return true;
    }
}
