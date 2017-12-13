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

import de.longri.cachebox3.sqlite.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;


public class SettingsList extends ArrayList<SettingBase<?>> {
    final static Logger log = LoggerFactory.getLogger(SettingsList.class);

    public static SettingsList that;

    private static final long serialVersionUID = -969846843815877942L;

    private boolean isLoaded = false;

    public SettingsList() {
        that = this;

        // add Member to list
        Member[] mbrs = this.getClass().getFields();

        for (Member mbr : mbrs) {
            if (mbr instanceof Field) {
                try {
                    Object obj = ((Field) mbr).get(this);
                    if (obj instanceof SettingBase<?>) {
                        add((SettingBase<?>) obj);
                    }
                } catch (IllegalArgumentException e) {

                    e.printStackTrace();
                } catch (IllegalAccessException e) {

                    e.printStackTrace();
                }
            }

        }

        mbrs = null;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public static SettingBase<?> addSetting(SettingBase<?> setting) {

        if (that == null)
            try {
                throw new InstantiationException("Settings List not initial");
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            }
        that.add(setting);

        return setting;
    }

    @Override
    public boolean add(SettingBase<?> setting) {
        if (!that.contains(setting)) {
            return super.add(setting);
        }
        return false;
    }

    protected SettingsDAO createSettingsDAO() {
        return new SettingsDAO();
    }

//    /**
//     * Return true, if setting changes need restart
//     *
//     * @return
//     */
//    public boolean WriteToDB() {
//
//        Database settings = Database.Settings;
//        Database data = Database.Data;
//
//        // Write into DB
//        SettingsDAO dao = createSettingsDAO();
//        settings.beginTransaction();
//
//        try {
//            if (data != null)
//                data.beginTransaction();
//        } catch (Exception ex) {
//            // do not change Data now!
//            data = null;
//        }
//
//        boolean needRestart = false;
//
//        try {
//            for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
//                SettingBase<?> setting = it.next();
//                if (!setting.isDirty())
//                    continue; // is not changed -> do not
//
//                if (SettingStoreType.Local == setting.getStoreType()) {
//                    if (data != null)
//                        dao.writeToDatabase(data, setting);
//                } else if (SettingStoreType.Global == setting.getStoreType()) {
//                    dao.writeToDatabase(settings, setting);
//                }
//
//                if (setting.needRestart) {
//                    needRestart = true;
//                }
//
//                setting.clearDirty();
//
//            }
//            return needRestart;
//        } finally {
//            settings.endTransaction();
//            if (data != null)
//                data.endTransaction();
//        }
//
//    }

    public void LoadFromLastValue() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.loadFromLastValue();
        }
    }

    public void SaveToLastValue() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.saveToLastValue();
        }
    }

    public void LoadAllDefaultValues() {
        for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext(); ) {
            SettingBase<?> setting = it.next();
            setting.loadDefault();
        }
    }
}
