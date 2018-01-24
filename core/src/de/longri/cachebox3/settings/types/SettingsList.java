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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.sqlite.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;


public class SettingsList extends Array<SettingBase<?>> {
    final static Logger log = LoggerFactory.getLogger(SettingsList.class);


    private boolean isLoaded = false;
    public final Array<SettingBase<?>> dirtyList = new Array<>();

    public SettingsList() {

    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public SettingBase<?> addSetting(SettingBase<?> setting) {
        this.add(setting);
        return setting;
    }

    @Override
    public void add(SettingBase<?> setting) {
        if (!this.contains(setting, true)) {
            super.add(setting);
            setting.dirtyList = this.dirtyList;
        }
    }


    public void loadFromLastValue() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.loadFromLastValue();
        }
    }

    public void saveToLastValue() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.saveToLastValue();
        }
    }

    public void loadAllDefaultValues() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.loadDefault();
        }
    }
}
