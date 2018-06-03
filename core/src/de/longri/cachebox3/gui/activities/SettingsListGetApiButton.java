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
package de.longri.cachebox3.gui.activities;


import de.longri.cachebox3.settings.types.*;

public class SettingsListGetApiButton<T> extends SettingBase<T> {

    public SettingsListGetApiButton(String name, SettingCategory category, SettingMode modus, SettingStoreType StoreType, SettingUsage usage) {
        this( name,  category,  modus,  StoreType,  usage,false);
    }

    public SettingsListGetApiButton(String name, SettingCategory category, SettingMode modus, SettingStoreType StoreType, SettingUsage usage,boolean desired) {
        super(name, category, modus, StoreType, usage, desired);

    }

    @Override
    public Object toDbValue() {

        return null;
    }

    @Override
    public boolean fromDbvalue(Object dbString) {

        return false;
    }

    @Override
    public SettingBase<T> copy() {
        // can't copy this obj
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingsListGetApiButton<?>))
            return false;

        SettingsListGetApiButton<?> inst = (SettingsListGetApiButton<?>) obj;
        if (!(inst.name.equals(this.name)))
            return false;
        if (inst.value != this.value)
            return false;

        return true;
    }
}
