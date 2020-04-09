/* 
 * Copyright (C) 2011-2020 team-cachebox.de
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

public class SettingIntArray extends SettingInt {

    private Integer values[];

    public SettingIntArray(String name, SettingCategory category, SettingMode modus, int defaultValue, SettingStoreType StoreType, SettingUsage usage, Integer arr[]) {
        super(name, category, modus, defaultValue, StoreType, usage);
        values = arr;
    }

    public Integer[] getValues() {
        return values;
    }

    public int getIndex() {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value)
                return i;
        }
        return -1;
    }

    public int getValueFromIndex(int index) {
        return values[index];
    }
}
