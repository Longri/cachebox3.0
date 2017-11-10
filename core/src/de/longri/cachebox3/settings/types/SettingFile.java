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

import de.longri.cachebox3.CB;

public class SettingFile extends SettingLongString {
    private String ext = "*";

    public SettingFile(String name, SettingCategory category, SettingMode modus, String defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        super(name, category, modus, defaultValue, StoreType, usage);
    }

    public SettingFile(String name, SettingCategory category, SettingMode modus, String defaultValue, SettingStoreType StoreType, SettingUsage usage, String ext) {
        super(name, category, modus, defaultValue, StoreType, usage);
        this.ext = ext;
    }

    @Override
    public void setValue(String value){
        super.setValue(value.replace(CB.WorkPath,"?"));
    }

    public String getExt() {
        return ext;
    }
}
